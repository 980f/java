package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/PacketReceiver.java,v $
 * Description:  receive packets recognized by delimiters.
 * Copyright:    Copyright (c) 2001-2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.22 $
 * @todo: add count-in-header capability
 */
import net.paymate.data.*;
import net.paymate.serial.*;
import net.paymate.util.*;
import net.paymate.lang.ReflectX;
import java.io.*;
import net.paymate.io.ByteFifo;

public class PacketReceiver {
  protected Packetizer packer;
  //state flags
  protected boolean inbody;
  protected boolean escaped; //previous char was escape, don't interpret next one.
  private boolean donein;
  protected boolean inlrc;

  protected Exception lastError;

  public Buffer body(){
    return packer.body();
  }

  public boolean isDone(){
    return donein;
  }

  /**
   * @warn: when we become done we must reset flags indicating that next char is not to be interpreted
   * else protocol chars don't get handled.
   */

  protected synchronized PacketReceiver setDone(){
    inlrc=false;
    inbody=false;   //can be true if counted non-lrc packet
    if(escaped){ //if true we are likely fucking up
      //dbg.ERROR("setting done while escaped!");
    }
    escaped=false;
    donein=true;
    return this;
  }

  /**
   * @return whether we have a well formed packet.
   */
  public boolean isWellFormed(){
    return donein && packer!=null && packer.lrcOk();
  }

  public synchronized PacketReceiver restart(){
    lastError=null;
    inlrc=false;
    inbody=false;
    escaped=false;
    donein=false;
    if(packer!=null){
      packer.reset();
      if( ! packer.has(packer.stxchar)){//if no specific stx
        processStx();//then we are ready for body chars upon each restart.
      }
    }
    return this;
  }

/**
 * @--deprecated use PacketServe and get control events.
 */
  protected void controlPacket(int cc){
    if(packer!=null){
      restart();
      if(packer.buffer==null){
        packer.attachTo(Buffer.New(1));
      }
      packer.buffer.append(cc);
      setDone();//deprecated, control char as a one byte packet.
    }
  }

  public PacketReceiver startBuffer(Buffer buffer){
    packer.attachTo(buffer);
    return restart();
  }
/**
 * @return whether the given bite is the possible control char.
 */
  protected boolean isControl(int bite,int controlchar){
    return packer.has(controlchar) && bite==controlchar;
  }

  //default implementations
  /**
   * processStx() will get called if an stx char is defined and encountered.
   * it can also be called directly for when there is no STX character.
   */
  public synchronized boolean processStx(){
    //consider dbg.WARNING if buffer is not empty
    packer.reset();
    inbody=true;
    return true;
  }

  /**
   * processEtx() will get called if an etx char is defined and encountered.
   * it can also be called directly for when there is no eTX character,
   * such as when packet header contains a length.
   */
  public synchronized boolean processEtx(){
    if(packer.haslrc){
      inlrc=true;
      packer.lrc.checksum(packer.buffer);// or we could do this incremenatlly on receive.
      if(packer.has(packer.etxchar)){ //&& include ETX in sum
        packer.lrc.checksum(packer.etxchar);
      }
      if(true){
        receive(packer.afterEtx());
      }
    } else {
      setDone();//no lrc so we are finished
    }
    return true;
  }

  /**
   * @return still worth receiving bytes.
   */
  protected synchronized boolean processLrc(int incominglrc){
    boolean stillok=packer.lrc.test(incominglrc);
    if(packer.lrc.complete()){//last byte received, does not look at whether the checksum is good.
      setDone();//lrc received
      return stillok;
    } else {
      return stillok;
    }
  }

  /** @todo: migrate these to protocol object
   * things to return from receive:
   */
  private boolean inputIgnored=true;
  private boolean inputRefused=true;


 /**
  * put @param bite gotten from an inputstream.read() function into the buffer
  * @return if byte was acceptible. at this layer all we need is room for it.
  */
  public synchronized boolean receive(int bite){
    if(!donein){
      if(bite == Receiver.EndOfInput){//java.io's end of file
    //beware: serial drivers have been known to give back "eof" when there is no data available.
    //   if so you will have to deal with that above this layer and not call this function.
        return donein;//if false then unexpected end of input.
      }
      if(inlrc){//can get ANY bite value as an lrc.
        return processLrc(bite);
      }
      if(escaped){//has to go before bite is looked at
        escaped=false;
        return packer.buffer.append(bite);//take it verbatim
      }
      if(isControl(bite,packer.escapechar)){//escape next char
        escaped=true;
        return inputIgnored; //the escape itself does not go into buffer.
      }
      //precedes check of inbody flag so that a second stx is a restart.
      if(isControl(bite,packer.stxchar)){
      //if inbody then this is a restart, we might signal those to someone.
        return processStx();
      }
      if(isControl(bite,packer.etxchar)){
        return inbody? processEtx() : inputIgnored; //ignore if not in body.
      }
      if(inbody){
        return packer.buffer.append(bite);
      } else {
        return inputIgnored;//trash getting ignored.
      }
    }
    else { //overrun error
      return inputRefused; //we are refusing
    }
  }
/**
 * return whether we have a complete and properly wrapped buffer.
 */
  public boolean receptionComplete(){
    return donein;
  }

/**
 * next char is NOT to be interpreted by others.
 */
  public synchronized boolean escaping(){
    return escaped || inlrc;
  }

  /**
   * process full set of bytes collected by some other receiver
   */
  public synchronized boolean receive(Buffer wad){
    restart();//even for defective input
    if(wad!=null){
      BufferParser bp=BufferParser.Slack().Start(wad);
      while(bp.remaining()>0){
        receive(bp.getByte());
      }
    }
    return isWellFormed();
  }

  //can implement a generic fairly stupid receiver here.

  //can implement a generic fairly stupid send-receive pairing here.

  public String toSpam(int eventcode){
    return Receiver.imageOf(eventcode)+" SLDE:"+inbody+inlrc+donein+escaped+" "+ReflectX.shortClassName(packer); //previous char was escape, don't interpret next one.
  }

  protected PacketReceiver(Packetizer packer) {
    this.packer=packer;
    restart();
  }

  public static PacketReceiver BasicVisa(int size){
    return new PacketReceiver(Packetizer.Ascii(size));
  }

  public static PacketReceiver BasicTTY(int size){
    return new PacketReceiver(Packetizer.TTY(size));
  }

  public static PacketReceiver MakeFrom(Packetizer othertype){
    return new PacketReceiver(othertype);
  }

  ///////////////
  public void simReception(Packetizer pck){
    ByteFifo simdata=new ByteFifo(true/*blocking*/);
    pck.writeOn(simdata.getOutputStream());
    InputStream is=simdata.getInputStream();
    try {
      while(is.available()>0 && receive(is.read())){
      //process what we got until reception is unhappy
      }
    }
    catch (Exception ex) {
      //ignored
    }
  }

}
//$Id: PacketReceiver.java,v 1.22 2004/01/19 17:03:26 mattm Exp $