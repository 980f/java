package net.paymate.serial;

/**
* Title:        $Source: /cvs/src/net/paymate/serial/Stream.java,v $
* Description:  reception on its own thread with a callback.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Id: Stream.java,v 1.10 2001/10/30 19:37:22 mattm Exp $
*/

import java.io.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;

public class Stream extends Thread implements TimeBomb {
  private final static Tracer dbg= new Tracer(Stream.class.getName());
  Receiver receiver;

  Port port;
//  boolean swallow=true;
  boolean killed=false;
  boolean passEoi=false; //used for debuggin with a disk file.

  /////////////////////////
  // timeout component

  Alarmum timeout;
  protected void startTimeout(int delay){
    Alarmer.Defuse(timeout);
    timeout=Alarmer.New(delay,this);
    dbg.WARNING("timeoutsetto:"+delay);
  }

  public void onTimeout(){
    if(receiver!=null){
      receiver.onByte(Receiver.TimedOut);
    }
  }

  ////////////////////////
  // 'polling'
  public void run(){
    dbg.Enter("run");
    boolean saweof=false; //used to squelch some debug
    try {
      if(port.rcv()!=null){
        while(!killed){
          int timeoutticks=Receiver.TimeoutNever;
          int available=port.rcv().available();
          if (available>0) {
            dbg.VERBOSE("available:"+available);
            byte prefetch[]=new byte[available];
            port.rcv().read(prefetch);
            if(receiver!=null){
              timeoutticks=receiver.onBytes(prefetch);
            }
          }
          else {
            int b= port.rcv().read();//read even wtihout a receiver to keep input from overflowing
            if(b==Receiver.EndOfInput){
              if(saweof){
                ThreadX.sleepFor(100);//+_+ to not suck up too much system time when input line is idle.
                //i.e. some of the implementations do not block when they have no input,
                // they send 'EndOfInput' right away. We don't shut down as there may be
                //more input later.  SHould move this into the individual stream types
              }
              else {
               dbg.VERBOSE("got EndOfInput, but am still polling");
              }
              saweof=true;
            } else {
              dbg.VERBOSE("read:"+(b>=0?Safe.ox2(b):Integer.toString(b)));
            }

            if(receiver!=null && (b!=Receiver.EndOfInput || passEoi)){
              timeoutticks=receiver.onByte(b);
            }
          }
          Alarmer.Defuse(timeout);
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
    }
    catch(NullPointerException npe){
      dbg.ERROR("port object:"+port);
      dbg.ERROR("its rcv:"+port.rcv());
    }
    catch (Throwable caught) {
      dbg.Caught(caught);
    }
    finally {
      dbg.Exit();
    }
  }

  public Exception startReception(int timeoutticks){
    dbg.Enter("startReception");
    try {
      killed=false;
      start();
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
      port.xmt().write(outgoing);
      startReception(timeoutticks);
      return null;
    }
//catch npe's
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


//////////////////////
//
/**
 * close, in preparation for destruction or reinitialization
 */

 public Stream Close(){
   killed=true; //cleanly stops background thread
   interrupt(); //to break out of wait on character. in case the stream that
   //we close via the following close doesn't throw an exception on read.
   Port.Close(port);
   return this;
 }

/**
 * @param is is stream to read from
 * @param os is stream to write to
 * @return this
 * pass nulls to detach existing streams, which will be closed first.
 */
  public Stream Attach(Port port){
    Close();
    this.port=port;
    return this;
  }

/**
 * @param receiver is the thing that will receive data from the InputStream set with Attach
 */
  public Stream(Receiver receiver) {
    this.receiver=receiver;
  }

}
//$Id: Stream.java,v 1.10 2001/10/30 19:37:22 mattm Exp $
