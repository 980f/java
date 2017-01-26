package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/Packetizer.java,v $
 * Description:  trivial instance of a buffer Packetizer.
 *              provides outgoing header and trailer generation,
 *              incoming header and trailer stripping and checking
 *              for communications buffers.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.14 $
 */

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.MathX;
import java.io.*;

public class Packetizer {
  Buffer buffer; //data to be wrapped
  //#escape hatch, for special buffers such as single control characters
  boolean raw;   //omit head and trailer when sending.

  //configuration for use by many Packetizers
  int stxchar;
  int etxchar;
  int escapechar;
  boolean haslrc;

  protected Checksum lrc;//variable named after most common concrete class.

  /**
   * @return unwrapped contents after read, what was given for write.
   */
  public Buffer body(){
    return buffer;
  }

  /**
   * FYI access. Let packetReceiver care about this.
   */
  public Checksum checker(){
    return lrc;
  }

  /**
   * only call when you know that packet is deemed complete by other means
   */
  public boolean lrcOk(){
    return !haslrc || (lrc!=null && lrc.matches());
  }

  /**
 * typically used only for writing, body of data to be written.
 * next call writeOn(stream) to send wrapped with header and trailer.
 */
  public Packetizer attachTo(Buffer buffer){
    this.buffer=buffer;
    resetSummer();
    return this;
  }

  /**
   * for use by PacketReceiver.
   * don't call this directly unless you really know your protocol.
   */
  public void resetSummer(){
    if(haslrc){
      if(lrc==null){
        lrc=new Checksum(); // a trivial one!!!
      } else {
        lrc.reset();
      }
    } else {
      lrc=new Checksum(); //trival summer so as to not have NPE's
    }
  }

  /**
   * erase all memory of previous usage. literally erases, doesn't just drop object references.
   */
  public void reset(){
    if(buffer==null){
      buffer=Buffer.New(1); //avert NPE's
    } else {
      buffer.reset();
    }
    resetSummer();
  }

  /**
   * @return whether the @param controlchar is a real char
   */
  protected boolean  has(int controlchar){
    return controlchar>=0;
  }

  /**
   * @return header for writing buffer, meaningless on receive buffer.
   */
  public byte[] header(){
    lrc.reset();//+_+ formally not very nice to have such a side effect. Need to make formation of checksum more explicit
    return !raw && has(stxchar) ? Ascii.byteAsArray(stxchar): Ascii.emptyArray();
  }

/**
 * number of bytes to expect in trailer.
 *
 */
  protected int trailerLen(){
    if(raw){
      return 0;
    } else {
      int trailerlen=0;
      if(haslrc){
        trailerlen+= lrc.length();
      }
      if(has(etxchar)){
        trailerlen+= 1;
      }
      return trailerlen;
    }
  }

  /**
   * @return trailer for writing buffer, wil be expected trailer if receiving (and that makes sense:)
   */
  public byte[] trailer(){
    if(raw){
      return Ascii.emptyArray();
    } else {
      byte [] newone=new byte[trailerLen()];
      int lrcat=0;
//relocated so that we can sum header lrc.reset();//lack of this should have screwed up everything that did retries! every other retry would ahve an lrc of 00 (was observed!)
      lrc.checksum(body());
      if(has(etxchar)){
        newone[0]=(byte)etxchar;
        lrcat=1;
        lrc.checksum(etxchar);
      }
      System.arraycopy(lrc.Sum(),0,newone,lrcat,lrc.length());
      return newone;
    }
  }
  /**
   * stream out so that caller can optimize copying or not etc.
   */
  public Exception writeOn(OutputStream os){
    try {
      if(os!=null){
        //note: zero length arrays result in nothing being sent.
        lrc.reset();//lack of this should have screwed up everything that did retries! every other retry would ahve an lrc of 00 (was observed!)
        os.write(header());
        os.write(body().packet());
        os.write(trailer());
        return null;
      } else {
        return new IOException("PORT NOT OPENED");
      }
    }
    catch (Exception ex) {
      return ex;
    }
  }


  /**
    * afterEtx() will get called after receiver has done its processing of etx
    * @return a character to process as the next character.
    * return super.afterEtx() for "don't add a character", which is definitely not the same as null.
    */
   public int afterEtx(){
     return Receiver.EndOfInput;
   }


  /**
   * default settings are for visa/ascii with one byte lrc
   */
  protected Packetizer() { //for copying via instantiation via reflection
    Config(Ascii.STX,Ascii.ETX,true,Ascii.DLE/*,'@'*/);
  }

  protected Packetizer (int stx,int etx,boolean lrcd,int dle){
    Config(stx,etx,lrcd,dle/*,'@'*/); //'@' turns ^A into 'A'
  }

  public Packetizer Config(int stx,int etx,boolean lrcd,int dle){
    this.stxchar=stx;
    this.etxchar=etx;
    this.haslrc=lrcd;
    if (lrcd) {
      lrc=new LrcChecksum();
    } else {
      lrc=new Checksum(); //base class is trivial
    }
    this.escapechar=dle;
    return this;
  }

  public static Packetizer Ascii(int size){
    Packetizer newone=new Packetizer();
    if(size>0){
      newone.attachTo(AsciiBuffer.Newx(size));
    } //else it is someone else's problem to provide a buffer
    return newone;
  }

  public static Packetizer TTY(int size){
    Packetizer newone=new Packetizer();
    newone.Config(MathX.INVALIDINTEGER,Ascii.LF,false,Ascii.DLE);
    newone.raw=true;
    if(size>0){
      newone.attachTo(AsciiBuffer.Newx(size));
    } //else it is someone else's problem to provide a buffer
    return newone;
  }

  public Packetizer Clone(){
    Packetizer newone=new Packetizer(stxchar,etxchar,haslrc,escapechar);
    newone.lrc= lrc.Clone();
    newone.buffer= buffer.Clone();
    return newone;
  }

}
/**
 * usage for writing:
 * create and keep indefinitely a packetizer of initial size zero.
 * for each sending:
 *  fill a buffer of the appropriate flavor.
 *  packetizer.attachto(that buffer);
 *  packetizer.writeon(some stream);
 *
 * usage for reading:
 * create and keep indefinitely a packetizer of intial size maximum-to-receive.
 * hand that to a PacketReceiver and forget about it. the packetreceiver will
 * be associated with some server and will give that server events and full packets.
 */
//$Id: Packetizer.java,v 1.14 2003/07/27 05:35:13 mattm Exp $