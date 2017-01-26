package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/VisaBuffer.java,v $
 * Description:  stx/etx/ lrc (sometimes) ack/nak/enq gizmo, soon to be obsolete
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: VisaBuffer.java,v 1.2 2004/01/09 11:46:04 mattm Exp $
 * NOTE that even when the LRC byte is not desired it is allocated for and often computed.
 * the "omitLRC" only keeps it from being checked or sent.
 */

import net.paymate.util.*;
import java.io.*;
import net.paymate.lang.StringX;

public class VisaBuffer extends LrcBufferBase {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(VisaBuffer.class, ErrorLogStream.WARNING);

  private static final int framingLength=3;

  protected byte acker=0;

  public void reset(){
    super.reset();
    acker=0;
    parser=0;
  }

  public boolean wasAckNaked(){
    return acker!=0;
  }
//////////
//wehn VisaBuffer is sent over the net the LRC is often left out.
  boolean omitLRC=false;
  public VisaBuffer setClipLRC(){
    omitLRC=true;
    return this;
  }

   ///////////////

  protected VisaBuffer(int maxsize,boolean rcv) {
    super(maxsize+framingLength,rcv);//added space for framing chars so that all other users could ignore them.
    lrcStart=1;//omit leading stx
  }

  public static VisaBuffer NewSender(int maxsize){
    VisaBuffer vb = new VisaBuffer(maxsize,false);
    vb.append(Ascii.STX);
    return vb;
  }

  public static VisaBuffer NewReceiver(int maxsize){
    return new VisaBuffer(maxsize,true);
  }

  /**
   * @param body is UNFRAMED message
   * @return new VisaBuffer with correct framing.
   */
  public static VisaBuffer FrameThis(byte [] body){
    VisaBuffer newone=NewSender(body.length);
    newone.append(body);
    newone.end();
    newone.parserStart();
    return newone;
  }

  /**
   * @return a new VisaBuffer made from the byte array of a complete one.
   * @param body is actually a completely framed message.
   * we even preserve a possible bad lrc byte.
   */
  public static VisaBuffer FromBytes(byte [] body){
    VisaBuffer newone=NewSender(body.length-framingLength);
    newone.stuff(body);
    newone.parserStart();
    return newone;
  }


  //////////////////
  // overrides placing of LRC on output
  public byte [] packet(){
    return omitLRC? extract(0,nexti-1): super.packet();
  }

  /**
   * @return innerds without any framing.
   */
  public byte [] body(){
    int header = 1;
    int trailer = framingLength-header;
    if(isReceiver && omitLRC) {
      --trailer;
    }
    byte [] ret = extract(header,nexti-trailer-header);//receivers do not always, but writers do always generates lrc even if 'omitlrc' true
    dbg.VERBOSE("body():header="+header+", trailer="+trailer+"nexti="+nexti+"extract("+header+","+nexti+"-"+trailer);
    dbg.VERBOSE("body():however, etx is at position: " + (new String(buffer)).indexOf(Ascii.ETX));
    dbg.VERBOSE("body():returning:"+Ascii.bracket(ret));
    return ret;
  }

//  public boolean start(int size){
//    dbg.WARNING("START called from:\n"+dbg.whereAmI());
//    super.start(size);
//  }


  /**
  * @return whether character was acceptible
  */
  public boolean append(byte b){
    dbg.Enter("append "+ Ascii.image(b));
    try {
      if(expectinglrc){  //lrc byte can be anything, so we must not interpret it here
        dbg.VERBOSE("Checking " + Ascii.image(b));
        return super.append(b);//append CHECKS lrc, and returns whether it matches.
      } else {
        dbg.VERBOSE("Appending  " + Ascii.image(b));
        switch(b) {
          case Ascii.ENQ:
          case Ascii.ACK:
          case Ascii.NAK: {
            //if already have data then this is a fubar ...
            acker=b;
            return end();//don't accept any more input
          }
          case Ascii.STX:{
            if(nexti!=0){
              ++errorCount;//but don't inspect this until after etx
              reset();
            }
            return super.append(b);//+_+ could choose to strip this on reception.
          }
          case Ascii.ETX: {
            return end();
          }
          default: {
            return super.append(b);//append body character OR CHECKS lrc
          }
        }
      }
    }
    finally {
      dbg.VERBOSE("packet=" + Ascii.bracket(packet()));
      dbg.Exit();
    }
  }

/**
 * attach both the etx, and for xmitters the lrc
 */
  public boolean end(){
    try {
      return super.append(Ascii.ETX) && super.end();
    } finally {
    //override super class's rule on expecting an lrc:
      expectinglrc =  isReceiver && ! omitLRC  && !wasAckNaked(); //this reduces the number of places that omitLRC needs to be inspected.
      if(!expectinglrc){
        lrc=0; //simplifies "ok" logic.
      }
    }
  }

  /**
   * @return true when buffer is either ready to ship or successfully received
   * note on reception and E71Buffer extension class: ETX==paritized(ETX) so we
   * don't have to override this function.
   */
  public boolean isOk(){
    if(wasAckNaked()){
      return acker==Ascii.ACK;
    }
    return super.isOk() && bight(0)==Ascii.STX && isComplete();
  }

  ///////////////
  // foramtting extensions

  public VisaBuffer endRecord() {
    append(Ascii.RS);
    return this;
  }

  public VisaBuffer endFrame() {
    append(Ascii.FS);
    return this;
  }

  public VisaBuffer emptyFrames(int howmany) {
    while(howmany-->0){
      append(Ascii.FS);
    }
    return this;
  }

  public VisaBuffer appendFrame(String ofBytes) {
    append(StringX.TrivialDefault(ofBytes, "").getBytes());//+_+ need to force encoding
    append(Ascii.FS);
    return this;
  }

  public VisaBuffer appendNumericFrame(long num, int min) {
    String number = Long.toString(num);
    int len = number.length();
    if(min<0){
      min=0; //avert cascading errors
    }
    if(len < min) {
      appendNumeric(min, number);
      append(Ascii.FS);
    } else {
      appendFrame(number);
    }
    return this;
  }

  /**
   * insert @param num with a decimal point @param decimals before the end of frame
   * with @param min minimum digits.
   * if min< decimals+2 then it is forced to that value
   * i.e. for decimals=2 the minimum "min" yields "9.99"
   */
  public VisaBuffer appendFrameWithDecimalPoint(long num, int min, int decimals) {
    min=Math.max(decimals+2,min);
    String number = Long.toString(num);
    int len = number.length();
    if(len>decimals){//then we can do normal insertion of decimal point
      appendNumeric(min-decimals-1,StringX.subString(number,0,len-decimals));
      append('.');
      appendFrame(StringX.tail(number,decimals));
    } else {
      append("0.");
      appendNumericFrame(num,min-2);//#2== "0.".length
    }
    return this;
  }

  public VisaBuffer appendWithDecimalPoint(long num, int width, int decimals) {
    int base = IntegralPower.raise(10, decimals).power;
    // +++ @todo use width to determine if we should use int or long
    appendInt(width-1-decimals,(int)(num)/base);//len-2 chars from left of string
    append(".");
    appendInt(decimals,(int)(num)%base);//last two chars of string#2== "0.".length
    return this;
  }



//////////////////////////////////////////////////////
// parser components
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

  public byte getByte(){
    return (byte)bight(parser++);
  }

  public String getFixed(int len){
    try {
      if(parser+len<nexti){
        return String.valueOf(subString(parser,len));
      } else {
        return "";
      }
    }
    finally {
      parser+=len;//even when we have run off end, we run off it some more!
    }
  }

  public int getDecimalInt(int len){
    return StringX.parseInt(getFixed(len));
  }

  public int getDecimalFrame(){
    return StringX.parseInt(getROF());
  }

  public long getDecimalLong(int len){
    return StringX.parseLong(getFixed(len));
  }
  public long getLongDecimalFrame(){
    return StringX.parseLong(getROF());
  }

  /** for reading encrypted PINs and the like.
   * @return a long from exactly 16 hex digits
   */
  public long getHex16(){
    return StringX.parseLong(getFixed(16),16);
  }

  public String getUntil(int delim){
    int start=parser;
    while(parser<nexti){
      if(delim == bight(parser++)){//not a string so we don't have indexOf()
        return String.valueOf(subString(start,parser-start-1));//-1 deletes delim from returned string
      }
    }
    return "";
  }

  public String getROF(){//rest of frame
    return getUntil(Ascii.FS);
  }

  public String getROB() { // rest of buffer [may not be a frame end!]
    return getFixed(nexti - parser -1);
  }

  public String getMsgType(){//up to next '.'
    return getUntil('.');
  }

  public TextList fields(){
    TextList fields=new TextList();
    parserStart();
//    boolean gotmore=true;
    while(parser<nexti){//should be a method for this. gotMore()
      fields.add(getROF());
    }
    return fields;
  }

  ///////////////
  public TextList dump(TextList spam){
    if(spam==null){
      spam=new TextList();
    }
    spam.add("vb.omit lrc",omitLRC);
    spam.add("vb.acker", Ascii.image(acker));

    spam.add("vb.isOk()", isOk());
    spam.add("vb.isComplete()", isComplete());
    spam.add("vb.hasEnded()", hasEnded());


    return super.dump(spam);
  }

  public void dump(String msg){
    TextList spam=new TextList();
    spam.add(msg);
    System.out.println(dump(spam).asParagraph());
    System.out.println(toSpam());
  }

}
//$Id: VisaBuffer.java,v 1.2 2004/01/09 11:46:04 mattm Exp $
