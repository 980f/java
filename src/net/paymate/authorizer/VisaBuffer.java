package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/VisaBuffer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: VisaBuffer.java,v 1.16 2001/10/22 23:33:37 andyh Exp $
 */

import net.paymate.util.*;
import java.io.*;

public class VisaBuffer extends LrcBufferBase  implements AsciiLow32 {

//to accomodate the encrypt which does everything the same EXCEPT
//OCCASIONALLY uses a different stx/etx pair
  protected byte stx=STX;
  protected byte etx=ETX;


//////////
//wehn VisaBuffer is sent over the net the LRC is often left out.
  boolean omitLRC=false;
  public VisaBuffer setClipLRC(){
    omitLRC=true;
    return this;
  }

   ///////////////

  protected VisaBuffer(int maxsize,boolean rcv) {
    super(maxsize,rcv);
    lrcStart=1;//omit leading stx
  }

  public static VisaBuffer NewSender(int maxsize){
    return new VisaBuffer(maxsize,false);
  }

  public static VisaBuffer NewReceiver(int maxsize){
    return new VisaBuffer(maxsize,true);
  }


  //////////////////
  // overrides placing of LRC on output
  public byte [] packet(){
    return omitLRC? extract(0,nexti-1): super.packet();
  }

  public boolean start(int size){
    super.start(size);
    return super.append(stx);
  }

  /**
  * @return whether character successfully went into buffer
  */
  public boolean append(byte b){
    if(!ended){  //lrc byte can be anything, so we must not interpret it here
      if(b==stx){
        if(nexti!=0){
          ++errorCount;//but don't inspect this until after etx
        }
        start(size);
        return true;//+_+ there are errors we could detect and report on here
      } else if(b==etx){
        return end();
      }
    }
    return super.append(b);//append body character OR CHECKS lrc
  }

/**
 * attach both the etx and the lrc
 */
  public boolean end(){
    return super.append(etx) && super.end();
  }

  /**
   * @return true when buffer is either ready to ship or successfully received
   * note on reception and E71Buffer extension class: ETX==paritized(ETX) so we
   * don't have to override this function.
   */
  public boolean isOk(){
//    if(omitLRC){//force the check to yield tru rather than trying to skip it
//      append(lrc);
//    }
    return super.isOk() && bight(0)==stx && omitLRC?hasEnded():isComplete();
  }


  ///////////////
  //
  public VisaBuffer endFrame() {
    append(FS);
    return this;
  }

  public VisaBuffer emptyFrames(int howmany) {
    while(howmany-->0){
      append(FS);
    }
    return this;
  }

  public VisaBuffer appendFrame(String ofBytes) {
    append(Safe.TrivialDefault(ofBytes, "").getBytes());//+_+ need to force encoding
    append(FS);
    return this;
  }

  /**
   * text type fixed length fields are left justified blank extended
   */
  public VisaBuffer appendAlpha(int size,String text){
    append(Fstring.fill(text,size,' '));
    return this;
  }

    /**
   * number type fixed length fields are right justified zero extended
   */
  public VisaBuffer appendInt(int size,int number){
    appendNumeric(size,Integer.toString(number));
    return this;
  }

  public VisaBuffer appendLong(int size,long number){
    appendNumeric(size,Long.toString(number));
    return this;
  }

  public VisaBuffer appendNumeric(int size,String digits){
    int overflow=digits.length()-size;
    //+++ check digits for non-decimal characters, we have that somewhere....
    if(overflow>0){
      //try to strip leading zeroes
      String clip=digits.substring(0,overflow);
      for(int i=overflow;i-->0;){
        if(clip.charAt(i)!='0'){
          throw new IllegalArgumentException();
        }
      }
      append(digits.substring(overflow));
    } else {
      append(Fstring.righted(digits,size,'0'));
    }
    return this;
  }

  /**
   * parser is left pointing to the next unread byte.
   */
  int parser=0;//!beware the stx!

  public boolean parserStart(){
    if(isOk()){
      parser=1;
      return true;
    } else {
      return false;
    }
  }

  public String getFixed(int len){
    try {
      if(parser+len<nexti){
        return subString(parser,len).toString();
      } else {
        return null;
      }
    }
    finally {
      parser+=len;//even when we have run off end, we run off it some more!
    }
  }

  private String getUntil(int delim){
    int start=parser;
    while(parser<nexti){
      if(delim == bight(parser++)){//not a string so we don't have indexOf()
        return subString(start,parser-start-1).toString();//-1 deletes delim from returned string
      }
    }
    return null;
  }

  public String getROF(){//rest of frame
    return getUntil(FS);
  }

  public String getMsgType(){//up to next '.'
    return getUntil('.');
  }

  public TextList fields(){
    TextList fields=new TextList();
    parserStart();
    boolean gotmore=true;
    while(parser<nexti){//should be a method for this. gotMore()
        fields.add(getROF());
    }
    return fields;
  }

//  ///////////////
//  public static void main(String argv[]){
//    VisaBuffer tester=VisaBuffer.NewSender(100);
//    tester.start(100);
//    tester.appendNumeric(10,"999999948");
//    tester.end();
//    System.out.println(tester.toSpam());
//  }
//
}
//$Id: VisaBuffer.java,v 1.16 2001/10/22 23:33:37 andyh Exp $
