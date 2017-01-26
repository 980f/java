package net.paymate.serial;

/**
* Title:        $Source: /cvs/src/net/paymate/serial/Stream.java,v $
* Description:  reception on its own thread with a callback.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Id: Stream.java,v 1.27 2003/07/27 05:35:14 mattm Exp $
*/

import java.io.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.lang.ThreadX;

public class Stream implements Runnable, TimeBomb {
  private static ErrorLogStream dbg= ErrorLogStream.getForClass(Stream.class);
  Receiver receiver;
  Thread readerThread;

  Port port;
//  boolean swallow=true;
  private boolean killed=false;
  private boolean running=false;

  boolean passEoi=false; //used for debuggin with a disk file.

  /////////////////////////
  // timeout component
  protected int niceness=0; //not very nice
/**
 * @return previous setting.
 */
  public int setNiceness(int millis){
    try {
      return niceness;
    }
    finally {
      niceness=port.setNiceness(millis);
    }
  }

  Alarmum timeout;
  public synchronized void startTimeout(int delay){//exposed and synched for startup problems with enCrypt
    Alarmer.Defuse(timeout);
    timeout=Alarmer.New(delay,this);
    dbg.WARNING("timeoutsetto:"+delay);
  }

  /**
 * will lock here if receiver is locked. so be it.
 */
  private int postByte(int bight){
    if(receiver!=null){
      synchronized (receiver) {//synch with SerialDevice.suckInput(), Receiver.onBytes().
        return receiver.onByte(bight);
      }
    } else {
      return Receiver.TimeoutNow; //things are so defective to get here we don't really care what we return.
    }
  }

  public void onTimeout(){
    postByte(Receiver.TimedOut);
  }


  ////////////////////////
  // 'polling'
  public void run(){
    running=true;
    dbg.Enter("run");
    boolean saweof=false; //used to squelch some debug
    InputStream is=port.rcv();
    try {
      if(port.rcv()!=null){
        while(!killed){
          int timeoutticks=Receiver.TimeoutNever;
          int available=port.rcv().available();
          if (available>0) {
            dbg.VERBOSE("available:"+available);
//            byte prefetch[]=new byte[available];
//            port.rcv().read(prefetch);
//            dbg.VERBOSE("blok:"+Ascii.image(prefetch));
            if(receiver!=null){
              synchronized (this) {//ensure block is not interleaved with new input on other threads.
                Alarmer.Defuse(timeout);
                while(available-->0){
                  timeoutticks=receiver.onByte(is.read());
                  if(timeoutticks==Receiver.TimeoutNow){
                    return; //receiver has decided it is getting garbage, wants us to quit reading.
                  }
                }
              }
            }
          } else {
            int b= port.rcv().read();//read even wtihout a receiver to keep input from overflowing
            Alarmer.Defuse(timeout);
            if(b==Receiver.EndOfInput){
              if(saweof){
                if(port.isNice()){
                  ThreadX.sleepFor(niceness);//+_+ to not suck up too much system time when input line is idle.
                //i.e. some of the implementations do not block when they have no input,
                // they send 'EndOfInput' right away. We don't shut down as there may be
                //more input later.  SHould move this into the individual stream types
                }
              }
              else {
               dbg.VERBOSE("got EndOfInput, but am still polling");
              }
              saweof=true;
            } else {
              dbg.VERBOSE("read:"+Ascii.image(b));
            }

            if(b!=Receiver.EndOfInput || passEoi){//passEoi=permission to send eoi's to receiver
              timeoutticks=postByte(b);
            }
          }

          switch (timeoutticks) {
            case Receiver.TimeoutNow:{
              return; //quit calling receiver.
            }// break;
            case Receiver.TimeoutNever:{
              //doesn't startTimeout();
            } break;
            default:{
              startTimeout(timeoutticks);
            } break;
          }
        }
      }
    } catch(NullPointerException npe){
      dbg.Caught(npe);
    } catch (Throwable caught) {
      dbg.Caught(caught);
    } finally {
      dbg.Exit();
      running=false;
    }
  }

  public Exception startReception(int timeoutticks){
    dbg.Enter("startReception");
    try {
      killed=false;
      readerThread.start();
      startTimeout(timeoutticks);
      return null;
    }
    catch(IllegalThreadStateException a){   //already started
      //is ok.
      return null;
    }
    catch (Exception ex) {
      dbg.Caught("startReception",ex);
      return ex;
    } finally {
      dbg.Exit();
    }
  }

  /**
   * for command/response exchange
   */
  public Exception startExchange(byte []outgoing,int timeoutticks){
    try {
      port.reallyFlushInput(300);//+_+ max packet size.
      port.xmt().write(outgoing);
      return startReception(timeoutticks);
    }
    catch (Exception ex) {
      return ex;
    }
  }

  public Exception write(int b){
    dbg.Enter("write");
    try {
      port.xmt().write(b);
      return null;
    }
    //+_+ SPECIFCALLY CATCH NPE'S
    catch (Exception ex) {
      dbg.Caught(ex);
      return ex;
    }
    finally {
      dbg.Exit();
    }
  }

  public Exception lazyWrite(byte [] buffer,int off, int len){
    try{
      return port.lazyWrite(buffer,off,len);
    } catch(Exception any){
      return any;
    }
  }

  public Exception lazyWrite(byte [] buffer){
    return lazyWrite(buffer,0,buffer.length);
  }

//////////////////////
//
/**
 * close, in preparation for destruction or reinitialization
 */

 public Stream Close(){
   if(readerThread!=null){
     killed=true; //cleanly stops background thread
     readerThread.interrupt(); //to break out of wait on character. in case the stream that
     //we close via the following close doesn't throw an exception on read.
     while(running){//readerTHread clears this when it exits.
      //wait for it to die
     }
   }
   Port.Close(port);
   return this;
 }

/**
 * @return this
 */
  public Stream Attach(Port port){
    Close();
    this.port=port;
    readerThread=new Thread(this,port.nickName()+".streamer");
    return this;
  }

/**
 * @param receiver is the thing that will receive data from the InputStream set with Attach
 */
  public Stream(Receiver receiver) {
    if(dbg==null) dbg= ErrorLogStream.getForClass(Stream.class);
    this.receiver=receiver;
    //set thread name
  }

}
//$Id: Stream.java,v 1.27 2003/07/27 05:35:14 mattm Exp $
