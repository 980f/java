/* $Id: Message.java,v 1.33 2001/11/14 01:47:49 andyh Exp $ */
package net.paymate.ISO8583.factory;

import net.paymate.data.Value;

import net.paymate.data.ContentType;
import net.paymate.data.ContentValid;
import net.paymate.awtx.RealMoney;
import net.paymate.ISO8583.data.*;
import net.paymate.util.ErrorLogStream;//debug
import net.paymate.util.Bool;//bitmapmanip
import net.paymate.util.Safe;//java's own throws exceptions on negative longs
import net.paymate.util.TextList;

import java.io.*;
import java.util.*;

/* {
  @see isomessage.html
} */

public class Message
{
  private static final ErrorLogStream dbg=new ErrorLogStream(Message.class.getName(), ErrorLogStream.WARNING);
  public Protocol protocol=new LegacyProtocol();//@@@ hack, need a new constructor.

  public int Type;//100,200,400,800

  protected static final int bitspermap=64;
  protected static final int xisoflag=128;
  protected static final int charspermap=bitspermap/4;

  protected static final int numBits=3*bitspermap;

  protected boolean IsPresent[]=new boolean[numBits];
  protected String Value[]=new String[numBits];

  int Length;
  boolean Above64;
  boolean Above128;
  long iso[]=new long [3]; //4debug, packed bit array

  void resetMap(){
    Length=0;
    Above64=false;
    Above128=false;

    for(int i=iso.length;i-->0;){
      iso[i]=0;
    }
  }

  public static final String bitName(int isobit){
    return "Field["+Integer.toString(isobit+1)+"] ";
  }

  public static final boolean IsValidBit(int isobit){
    return isobit>0 && isobit<numBits && isobit!=bitspermap &&isobit!=xisoflag;
  }
/**
 *
 */
  public String ValueFor(int isobit, String onMissing){
    if(IsValidBit(isobit)){
      return IsPresent[isobit]? Value[isobit]: Safe.OnTrivial(onMissing, "");
    }
    return bitName(isobit)+"is an Invalid ISO8583 Bit Number";
  }

  public String ValueFor(int isobit){
    return ValueFor(isobit,null);
  }

  public boolean setField(int isobit,String newValue){
    if(IsValidBit(isobit)){
      FieldDef fd=protocol.FieldSpec(isobit);

      if( fd.legalLength(newValue) && ContentValid.legalChars(fd.ctype, newValue)){//also check for bad length...
        IsPresent[isobit]=true;
        Value[isobit]=newValue;
        return true;
      } else {
        dbg.WARNING("Content Refused "+bitName(isobit)+newValue);
        //should we drop the present value? ???
        RemoveValueFor(isobit);//yes, kill a field, do NOT let stale data persist
        //or fill with a default
        //NO, don't have any such values yet
      }
    } else {
      dbg.WARNING("Bad Field # in set"+bitName(isobit));
    }
    return false;
  }

  public boolean setField(int isobit,RealMoney value){
    return setField(isobit,value.toString()); //Image() is for human use
  }

  public boolean setField(int isobit,LedgerValue value){
    return setField(isobit,value.toString()); //Image() is for human use
  }

  public boolean setField(int isobit,long value){
    if(IsValidBit(isobit)){
      FieldDef fd=protocol.FieldSpec(isobit);
      switch(fd.ctype.Value()){
        case ContentType.arbitrary :
        case ContentType.purealpha :
        case ContentType.alphanum  :
        case ContentType.date      :
        case ContentType.time      :
        case ContentType.zulutime  :
        case ContentType.money     :
        case ContentType.ledger    :
        default:                      return false;

        //no dedicated types, yet
        case ContentType.cardnumber: //special type coming soon, til then it is a long
        case ContentType.decimal   : return setField(isobit,Long.toString(value,10));
        case ContentType.hex       : return setField(isobit,Long.toString(value,16));
      }
    }
    return false;
  }

  public boolean isPresent(int isobit){
    return IsValidBit(isobit)? IsPresent[isobit]: false;
  }

//  public boolean copyField(Message original,int isobit){
//    return setField(isobit,original.ValueFor(isobit));
//  }

  public boolean copyField(Message original,int isobit){
    if(original.isPresent(isobit)){
      return setField(isobit,original.ValueFor(isobit));
    } else {
      return false;
    }
  }

  public boolean copyField(Message original,int isobit,String onMissing){
    return setField(isobit,original.ValueFor(isobit,onMissing));
  }

  public void RemoveValueFor(int isobit){
    if(IsValidBit(isobit)){
      Value[isobit]=null;  //to release storage earlier rather than later
      IsPresent[isobit]=false;
    }
  }

  public void RemoveAll(){
    resetMap();
    Type=0;
    Length=0;
    for(int i=Value.length;i-->0;){
      IsPresent[i]=false;
      Value[i]=null;
    }
  }

  public Message(Protocol protocol){
    this.protocol=protocol;
    RemoveAll();
  }

  public Message(Protocol protocol,String isomsg, int offset){
    this(protocol);
    Parse(isomsg, offset);
  }

  public Message(Protocol protocol,String isomsg){
    this(protocol,isomsg, 0);
  }


  public int nextPresent(int isobit){//bit # of next Present field
    if(isobit<0){
      isobit=0;
    }

    while(++isobit<numBits){ //the other illegal values will never be present
      if (IsPresent[isobit]) {
        return isobit;
      }
    }
    return -1;
  }

  public boolean Parse(String isoblock, int offset){ //build from packed string, return error list
    return Parse(isoblock, offset, null);
  }

  public boolean Parse(String isoblock, int offset, TextList tl){ //build from packed string, return error list
    //static final ErrorLogStream dbg= new ErrorLogStream("MessageParser",false);
    if(tl == null) {
      tl = new TextList(); // ONLY to avert exceptions; not passed back
    }
    int isobit = 0;
    try {
      dbg.Enter("Parse");
      isoCursor ink=new isoCursor(isoblock,offset);//ink== in-coming
      //read message length
      int totalLength=(int) ink.decimal(4);
      if(!ink.stillHave(totalLength) ){
        tl.add("String is too short, no fields read");
        return false;
      }

      RemoveAll(); //erase previous contents, especially reset all IsPresent bits

      Length=totalLength; //4debug
      Type=(int) ink.decimal(4); //0800, 0400, stuff like that

      tl.add("Type: " + Type);

      int mi;//map index
      for(mi=0;mi<iso.length;mi++){
        //an array of bits from a long from 16 asciihex characters:
        boolean[] submap=Bool.MapFromLong(ink.hexadecimal(charspermap));

        //a 2-D array [map#,bit-in-map] was costly elsewhere so:
        for(int i=1;i< bitspermap;i++){//the 0th bit is not for a field.
          IsPresent[mi*bitspermap+i]=submap[i];
        }
        if(!submap[0]){// successively bits 0,64,xisoflag
          break; //coz there are no more map words
        }
      }

      FieldDef fd;

      //now we can process via the IsPresent flags:
      String asRead;//will hold incoming values-as-text
      for(isobit=nextPresent(0);isobit>0;isobit=nextPresent(isobit)){
        fd=protocol.FieldSpec(isobit);//get attributes
        if(fd.isobit==isobit){   //if valid field specification
          if(fd.isFixed()){//fixed length
            asRead=ink.nextPiece(fd.length);
          } else {
            //variableLength number of digits is a field that is length
            //of variable content that follows.
            int vlength= (int) ink.decimal(fd.variableLength);
            asRead=ink.nextPiece(vlength);
          }
          tl.add(bitName(isobit)+"is '"+asRead+"'");
          if(!setField(isobit,asRead)){
            tl.add(bitName(isobit)+" Failed, is "+asRead);
          }
        } else {
          tl.add(bitName(isobit)+" is Not known");
          return false; //parsing has to stop coz we don't know where next field is.
        }
      }
      return true;
    } catch(Exception caught){
      tl.add("Exception parsing "+bitName(isobit)+caught);
      dbg.Caught(caught);
      return false;
    } finally {
      for(int i = 0; i < tl.size(); i++) {
        dbg.VERBOSE(tl.itemAt(i));
      }
      dbg.Exit();
    }
  }

  protected String Map(int which, boolean bit0){//collect bits into character string
    StringBuffer amap=new StringBuffer(charspermap);

    int isobit=which*bitspermap;//index of first bit for this map
    int hexvalue= bit0?8:0; //bit 0 of this map is not in the IsPresent array
    for(int chi=charspermap;chi-->0;){//number we need to generate
      for(int bph=8; bph>0; bph>>=1){ //little endian numbering
        if(IsPresent[isobit++]){//bph== b_it p_er h_ex digit
          hexvalue+=bph;
        }
      }
      amap.append(Character.forDigit(hexvalue,16));
      hexvalue= 0;//this is carefully placed due to msb nonsense
    }
    iso[which]=Safe.parseLong(amap.toString(),16);//4debug
    return amap.toString().toUpperCase();
  }

  protected int fieldLength(int isobit){
    FieldDef fd= protocol.FieldSpec(isobit);
    return fd.isFixed()?fd.length: (fd.variableLength+Value[isobit].length());
  }

  protected int regenMap(){//compute total length and see which bitmaps are req'd
    resetMap();
    Length=0;

    //length of what Follows length field.
    //the always present fields:
    Length+=4;//transcode
    Length+=charspermap;//bitmap 1
    //other bitmaps tended to at end of processing
    int isobit;
    for(isobit=numBits;isobit-->0;){
      if(IsPresent[isobit]){
        Length+=fieldLength(isobit);
        if(isobit>xisoflag){
          Above128=true;
        } else if(isobit>bitspermap){
          Above64=true;
        }
      }
    }

    if(Above128){
      Above64=true;
      /*might not have been anything between 64..127 but we still need its map
      to space out to the third one. I didn't use IsPresent[64] and
      IsPresent[128] because that would required excluding them from
      the length calculations which is more complex than the special logic here
      */
      Length+=charspermap;//bitmap 3
    }

    if(Above64){
      Length+=charspermap;//bitmap 2
    }

    return Length;
  }


  public String toString(){
    int msglen=regenMap();
    StringBuffer pack=new StringBuffer(msglen); //should now be efficient to append
//packHeader()
    pack.append(ContentDefinition.Padded(msglen,4));//legacy
    pack.append(ContentDefinition.Padded(Type,4));//generic
//packMaps()
    pack.append(Map(0,Above64));
    if(Above64){
      pack.append(Map(1,Above128));
    }
    if(Above128){
      pack.append(Map(2,false /*no 4th map ever.*/));
    }
//packContent
    for(int isobit=0;isobit<numBits;isobit++){//order matters
      if(IsPresent[isobit]){
        FieldDef fd=protocol.FieldSpec(isobit);
        if(!fd.formatInto(pack,Value[isobit])){
          //couldn't add field to message
        }
//        if(fd!=FieldDef.Invalid){
//          if (fd.isFixed()){
//            //pad the field
//            pack.append(ContentDefinition.Padded(Value[isobit],fd.length,fd.ctype));
//          } else {
//            //pack padded length
//            pack.append(ContentDefinition.Padded(Value[isobit].length(),fd.variableLength));
//            //pack data
//            pack.append(Value[isobit]);
//          }
//        } else {
//          //+_+ create dummy field?
//          // or just kill the whole thing?
//        }
      }
    }
    return pack.toString();
  }

  public void dumpTo(PrintStream out){
    Field arf=new Field();//to get to a psueod static member of the trueenum
    out.println("Length:"+Length);
    out.println("Type:"+Type);
    for(int isobit=nextPresent(0);isobit>0;isobit=nextPresent(isobit)){
      out.print(bitName(isobit));
      out.println("=["+ValueFor(isobit)+"], "+arf.TextFor(isobit));
    }
  }

  public static final void main(String [] argv){
    if(argv.length==0){
      System.out.println("usage: java etc. isostring");
      System.exit(0);//tester
    }
    Message msg=new Message(new LegacyProtocol());
    for(int i=argv.length;i-->0;){
      msg.Parse(argv[i],0);
      msg.dumpTo(System.out);
    }
  }

}
//$Id: Message.java,v 1.33 2001/11/14 01:47:49 andyh Exp $
