package net.paymate.serial;

import net.paymate.net.*;


/**
 * Title:        $Source: /cvs/src/net/paymate/serial/PacketServer.java,v $
 * Description:  receive packets from port and respond thereunto
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.serial.*;
import net.paymate.lang.MathX;
import net.paymate.lang.ReflectX;

public class PacketServer /*will extract PortServer later.*/ implements Runnable {
  protected static final ErrorLogStream dbg= ErrorLogStream.getForClass(PacketServer.class);

  private PacketReceiver recognizer;//defines how packets are parsed from a serial stream
  private PacketService  service;  //called when packet or control char  received.
  private Port           port;
  private Server         thread;

  static InstanceNamer threadNamer= new InstanceNamer("PacketServer");

//  int initialTimeout;
  /**
   * this array contains all characters that might be received as control chars.
   * I.e. chars that are part of the protocol outside of the packet and its framing.
   */
  protected int nakers[]={Ascii.ACK,Ascii.NAK,Ascii.ENQ,Ascii.EOT};//default is superset of most protocols.

/**
 * @param nakchars
 */
  protected PacketServer setnakers(int []nakchars){
    nakers=nakchars;
    return this;
  }

 /**
 * @return whether @oaram bite is a control char.
 */
  protected boolean isAckChar(int bite){
    for (int cf=nakers.length;cf-->0;){
      if(nakers[cf]==bite){
        return true;
      }
    }
    return false;
  }

  /**
   * @param recognizer parses the incoming stream.
   */
  public PacketServer setReceiver(PacketReceiver recognizer) {
    dbg.WARNING("attaching to recog:"+recognizer.toString());
    this.recognizer=recognizer;
    return this;
  }

  public PacketReceiver getReceiver() {
    return recognizer;//defines how packets are parsed from a serial stream
  }

  public PacketServer attachTo(PacketService  service){
    dbg.WARNING("attaching to service:"+service);
    this.service=service;
    return this;
  }

  /**
   * @param port is where we get our bytes from. It needs to be already opened.
   */
  public PacketServer attachTo(Port port){
    dbg.WARNING("attaching to port:"+port.toSpam());
    this.port=port;
    if(service !=null && port!=null){
      service.onConnect();
    }
    return this;
  }

  public boolean Start(){
    if(thread==null){
      thread=new Server(threadNamer.Next(),this,true);
    }
    return thread.Start();
  }

  protected boolean resynch(int incoming){
    recognizer.restart();
    return doByte(incoming); //recurse in case it is stx
  }

  protected boolean doByte(int incoming) {
    boolean resynch = false;
    try {
      if (!recognizer.escaping() && isAckChar(incoming)) { //check for line protocol outside scope of packet recognition
        dbg.WARNING("line event");
        resynch = service.onControlEvent(incoming);
      } else {
        if (recognizer.receive(incoming)) { //if byte is not atrocious
          if (recognizer.isWellFormed()) { //if we have a packet
            dbg.WARNING("packet event");
            service.onPacket(recognizer.body());
            recognizer.restart(); //for protocols without an explicit start we need this.
          } else {
            if (recognizer.isDone()) { // we have an lrc error or equivalent
              resynch = service.onControlEvent(Receiver.PacketError);
            } else {
              //normal bytes get us here.
            }
          }
        } else { //individually unacceptible byte, at a minimum not allowed at this place in the packet.
          dbg.ERROR("rejected:" + recognizer.toSpam(incoming));
          Buffer fubar = recognizer.body();
          dbg.ERROR("buffer:" + ReflectX.shortClassName(fubar) + " " + MathX.ratio(fubar.used(), fubar.allocated()));
          dbg.ERROR("content:" + fubar.toSpam());
          resynch = service.onControlEvent(Receiver.ByteError);
        }
      }
    }
    finally {
      if (resynch) {
        resynch(incoming);
      }
      return true;
    }
  }

/**
 * extracted in case we try some prefetch speedup games.
 */
  protected boolean doByte(){
    int incoming=port.ezRead();//4debug
    dbg.Enter("doByte:"+incoming);
    if(incoming<0){      //line errors and timeouts and port closes and such.
      dbg.WARNING("Event:"+Receiver.imageOf(incoming));
      //the following should be made configurable, with default baseclass behavior.
      service.onControlEvent(incoming);
      return true;
    }
    return doByte(incoming); //no basis yet for ending the service.
  }

  public void run() {
    dbg.WARNING("Entering run()");
    while(doByte()){
      //do nothing else.
    }
    dbg.WARNING("Exiting run()");
  }

/**
 * @param service handles packets that this class collects
 * @param PacketReceiver recognizes packets from bytes shoved at it
 * @parm port is a source of bytes.
 */
  protected PacketServer(PacketService  service, PacketReceiver recognizer, Port port){
    attachTo(service);
    setReceiver(recognizer);
    attachTo(port); //at which time bytes might start flowing.
  }

  /**
   * @return a new server doing stx/etx/lrc dance
   */
  public static PacketServer ServeVisaBasic(PacketService service, int size,Port port){
    return new PacketServer(service, PacketReceiver.BasicVisa(size),port);
  }

  /**
   * @return a packet server suitable for CR terminated human typed input.
   */
  public static PacketServer ServeTTY(PacketService service, int size,Port port){
    return new PacketServer(service, PacketReceiver.BasicTTY(size),port);
  }

  /**
   * other types of packetizers use this
   */
  public static PacketServer Create(PacketService  service, PacketReceiver recognizer, Port port){
    return new PacketServer(service,recognizer,port);
  }
}
//$Id: PacketServer.java,v 1.16 2003/07/27 05:35:13 mattm Exp $