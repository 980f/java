package net.paymate.net;

/**
* Title:        $Source: /cvs/src/net/paymate/net/LineServer.java,v $
* Description:  single connection at a time blocking in/out server.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.30 $
* @todo //+_+ look into using Packet, make a "human command line packet"
*/

import java.net.*;
import java.io.*;

import net.paymate.util.*;
import net.paymate.io.IOX;
import net.paymate.util.timer.*;

public class LineServer extends Server implements TimeBomb {
  ErrorLogStream dbg;
  protected LineServerUser myUser;
  public LineRecognizer framer; //quick hack, opened up for Techulator

  protected boolean ignoreEOF = false; //true for serial until we fix serial port stream fuckup of returning -1 on no data available.

  public static final int NOLINEIDLETIMEOUT = 0;
  private int lineIdleTimeoutMs = NOLINEIDLETIMEOUT;
  private InputStream timedincoming = null;
  private Alarmum alarm = null;

  private boolean prepTimeout(InputStream incoming){
    if(lineIdleTimeoutMs > NOLINEIDLETIMEOUT){
      if(alarm==null){
        alarm = Alarmer.New(lineIdleTimeoutMs, this);
        Alarmer.Defuse(alarm); // just create, don't DO
      }
      timedincoming = incoming; // set AFTER creating the Alarmum, just in case
      return true;
    } else {
      Alarmer.Defuse(alarm);
      alarm=null;
      timedincoming = incoming; // set AFTER creating the Alarmum, just in case
      return false;
    }
  }

  public LineServer(String name, boolean isDaemon) { // legacy
    this(name, isDaemon, NOLINEIDLETIMEOUT);
  }

  public LineServer(String name, boolean isDaemon, int lineIdleTimeoutMs) {
    super(name, isDaemon);
    this.lineIdleTimeoutMs = lineIdleTimeoutMs;
    dbg= ErrorLogStream.getForClass(this.getClass());
    dbg.WARNING("LineServer debugging as "+dbg.myName());
  }

  private boolean endOfLineDetected(StringBuffer context,int incoming){
    if(framer==null){
      dbg.WARNING("Instantiating simple line recognizer");
      framer=SimpleLineRecognizer.Ascii(Ascii.LF);
    }
    return framer.endOfLineDetected(context,incoming);
  }


  public void onTimeout() { //#implements TimeBomb
    IOX.Close(timedincoming); // this should cause an exception in the read() in core()
  }

  private int readIncomingByte() throws java.io.NotActiveException { // will except if called outside of the proper block in core()
    Alarmer.reset(lineIdleTimeoutMs, alarm);
    try {
      return timedincoming.read();
    } catch (Exception ex) {
      throw new java.io.NotActiveException("line idle timeout");
    } finally {
      Alarmer.Defuse(alarm);
    }
  }

  protected void core(InputStream incoming,OutputStream replyTo){
    int readPtr=0;
    StringBuffer oneline=new StringBuffer(2001);//+_+ look into using Packet
    int ch;
    try {
      prepTimeout(incoming);
      while((ch=readIncomingByte())>=0 || ignoreEOF){//blocks until data available or socket gets whacked
        if(ch<0){
          dbg.ERROR("Event:"+net.paymate.serial.Receiver.imageOf(ch)+ " as char:"+  ((char)ch));
          continue;
        }
        dbg.VERBOSE( "on char"+Ascii.bracket(ch)+" Rx"+Ascii.bracket(oneline.toString()));
        oneline.append((char)ch);
        if (endOfLineDetected(oneline,ch)){
          try {
            byte []fordbg=myUser.onReception(String.valueOf(oneline).getBytes());
            if(fordbg!=null){//byte might be zero length
              dbg.WARNING("about to write "+Ascii.bracket(fordbg));
              replyTo.write(fordbg);//+++ add a flush()
              dbg.VERBOSE("sent response");
            } else {
              dbg.WARNING("formatter killing connection");
              return;
            }
          }
          finally {
            oneline.setLength(0);
          }
        }
      }
    }
    //separate catches are for debuggability...
    catch(java.io.NotActiveException ex){
      dbg.WARNING("exiting listener due to "+ex);
    }
    catch(java.net.SocketException ex){
      dbg.WARNING("exiting listener due to "+ex);
    }
    catch(java.io.IOException ex){ //broken pipe when user disconnects session
      dbg.WARNING("exiting listener due to "+ex);
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
    finally {
      dbg.WARNING("Client dropped connection");
      timedincoming = null;     // release memory.
    }
  }

  public void run(){ //gotta be real to satisfy compiler. can't be abstract
    dbg.ERROR("Extended class run() not called!");
  }

}
//$Id: LineServer.java,v 1.30 2003/12/12 18:16:13 mattm Exp $
