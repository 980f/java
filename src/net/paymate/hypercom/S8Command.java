package net.paymate.hypercom;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/hypercom/S8Command.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;
import net.paymate.lang.Fstring;

public class S8Command extends AsciiBuffer {
  private final static int maxOperandLength=39;

//  public static final int DisplayText=0x3C;
//  public static final int SetConfig=0x92;
//  public static final int S8_2400=0x08;
  private int expectThis;
  public int expects(){
    return expectThis;
  }
  private S8Command expect(int respcode){
     expectThis=respcode;
     return this;
  }

  private S8Command (){
    super(maxOperandLength+3);
    expect(MathX.INVALIDINTEGER);//must be positive to be something expected
  }

  public S8Command setMessageType(int messagetype) {
    this.reset();
    append(Formatter.ox2(messagetype));
    append('.');//separator
    return this;
  }

  public S8Command ox2(int twohex){
    append(Formatter.ox2(twohex));
    return this;
  }

  /**
   *  is only used for inserting a key into the device.
   */
  public S8Command appendKey(long pinkey){
    String image=Long.toHexString(pinkey);
    appendNumeric(16,image);
    return this;
  }

  public S8Command appendKeySerialNumber(long ksn){
    String image=Long.toHexString(ksn);
    int shortby=20-image.length();
    if(shortby>0){
      append(Fstring.righted(image, 20, 'F'));
    } else if(shortby<0){
      //theoretical impossiblity, truncate and proceed.
      append(image.substring(0,20));
    } else {//perfectly right
      append(image);
    }
    return this;
  }

  public static S8Command Simple(int messagetype) {
    S8Command newone= new S8Command();
    return newone.setMessageType(messagetype);
  }

  public static S8Command GetRevision(){
    return Simple(0x90).expect(0x91);
  }

  public static S8Command PinEntryCancel(){
     return Simple(0x72);//no response
  }

  public static S8Command PinEntryRequest(String cardnum,int cents,boolean isCredit ){
    S8Command newone= Simple(0x70).expect(0x71) ;
    newone.appendFrame(cardnum);
    newone.append(isCredit?'C':'D');
    newone.append(Integer.toString(cents));
    return newone;
  }

  public static S8Command DisplayText(String topline,String lowerline){
    S8Command newone=Simple(0x3C);//expect no response
    newone.appendFrame(topline);
    if(StringX.NonTrivial(lowerline)){
      newone.append(lowerline); //NOT framed.
    }
    return newone;
  }

  public String toString(){//only used for debug
    return super.toSpam();
  }
}
//$Id: S8Command.java,v 1.2 2003/07/27 05:35:03 mattm Exp $