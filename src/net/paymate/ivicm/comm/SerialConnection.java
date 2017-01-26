package net.paymate.ivicm.comm;
/* $Source: /cvs/src/net/paymate/ivicm/comm/SerialConnection.java,v $ */

import net.paymate.util.*;
//import net.paymate.ivicm.*;
import net.paymate.jpos.data.*;
import net.paymate.lang.StringX;
import net.paymate.*;
import net.paymate.lang.ReflectX;
import java.io.*;
import java.util.*;

import net.paymate.serial.*;

public class SerialConnection {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(SerialConnection.class);

  final static String VersionInfo= "$Revision: 1.37 $";
//this has to be tested with every driver we try to use.
//when we find an OS based difference we will push this into OS.java:
  final static int WAITFOREVER=0;

  public Parameters parms;
  public Port port;
  public OutputStream os;
  public InputStream is;

  protected String myName;
  private boolean amOpen;

  protected SerialConnection setStreams(OutputStream os, InputStream is){
    this.os= os;
    this.is= is;
    return this;
  }

  public void forceTimeout(int milliwait){
    //overload
  }

  protected Problem Failure(String comment) {
    return Problem.Noted("In SerialConnection:"+comment);
  }

  protected Problem Failure(Exception ex) {
    return new StifledException("Serial Connection Fault",ex);
  }

  protected Problem Failure(String comment,Exception ex) {
    return new StifledException(comment ,ex);
  }

  protected void configure( String s, Parameters serialparameters) {
    myName = s;
    parms = serialparameters;
    amOpen = false;
    dbg.WARNING("On: "+myName+" ="+parms.toSpam());
  }

  public double CharTime(int numchars){
    return parms.CharTime()*(double)numchars;
  }

/**
 * actually set the hardware to the stored parameter values
 */
  public Problem setConnectionParameters()  {
    dbg.ERROR("SetConnection:"+parms.toSpam());
    return Failure("abstract comm class");
  }

//  public boolean assertRts(boolean on){
//    return false; //overload!
//  }
  /**
   * @return null if successful, else object with notes in it.
   */
  public synchronized boolean openConnection() {
    try {
      return !amOpen;
    }
    finally {
      amOpen = true;
    }
  }

  public synchronized Problem openConnection(Receiver baseless) {
    return baseless!=null? Failure("Type not supported:"+ReflectX.shortClassName(baseless)): Failure("Null receiver");
  }
  //////////////////////
  // thread priority management
  protected boolean boosted=false; //have boosted my serial thread's priority
  public void boostCheck(){
    if(!boosted) {
      Thread.currentThread().setPriority(parms.priority);
      Thread.currentThread().setName(parms.getPortName());
      boosted = true;//do just once per program run.
    }
  }

/**
 * @return true if transitioning to closed
 */
  public synchronized boolean closeConnection() {
    try {
      return amOpen;
    } finally {
      amOpen=false;
    }
  }

  public boolean isOpen() {
    return amOpen;
  }

  public String toString() {
    return myName;
  }

  public static final boolean Connected(SerialConnection sc){
    return sc!=null && sc.isOpen();
  }

  public static final boolean canWrite(SerialConnection sc){
    return Connected(sc) && sc.os!=null;
  }

  /**
   * start and end logic per String.substring();
   */
  public Exception lazyWrite(byte [] buffer,int off, int len){
    try {
      os.write(buffer,off,len);
      return null;
    } catch(Exception whocares){
      return whocares;
    }
  }

  public Exception lazyWrite(byte [] buffer){
    return lazyWrite(buffer,0,buffer.length);
  }

/**
 * use for diagnostic reads
 */
  public int ezRead(){
    try {
      return is.read();
    }
    catch (Exception ex) {
      return Receiver.EndOfInput;//. Serial ports just don't do this.
    }
  }

/**
 * @return bytes available on input stream
 */
  public int available(){
    try {
      return is.available();
    }
    catch (Exception ex) {
      return 0; //may add error codes later. Serial ports just don't do this.
    }
  }

  /**
   * for when the underlying stream's flush() doesn't please you.
   */
  public int reallyFlushInput(int limit){
    int flushed=0;
    while(available()>0&&++flushed<limit){//recheck available() in case input is still coming in.
      ezRead();
    }
    return flushed;
  }

  /**
   * records parameters for subsequent open() commands.
   * allows for reopen() to be called by someone who doesn't know the settings.
   */
  public static SerialConnection makeConnection(Parameters given){
    SerialConnection sc= new StreamConnection();
    if(sc!=null){
      sc.configure("paymate_"+given.getPortName(),given);
    }
    return sc;
  }

/**
 * construct one from transport and critical default
 */
  public static final SerialConnection makeConnection(EasyCursor ezp,int defbaud){
    dbg.Enter("MakeConnection");
    try {
      SerialConnection sc= new StreamConnection();
      if(sc!=null){
        String portname=ezp.getString(Parameters.nameKey);
        if(StringX.NonTrivial(portname)){
          ezp.Assert(Parameters.baudRateKey,defbaud);
          sc.configure("paymate_"+portname,new Parameters(portname,ezp));
          dbg.VERBOSE("Made:"+sc.toSpam());
          return sc;
        } else {
          dbg.VERBOSE("hopelessly bad portName:"+portname);
          return null;
        }
      } else {
        dbg.ERROR("Couldn't make connection:"+ezp.asParagraph(OS.EOL));
        return null;
      }
    }
    catch(Exception ignored){
      dbg.Caught("Caught while makingConnection:",ignored);
      return null;
    } finally {
      dbg.Exit();
    }
  }

  public String toSpam(){
    return ReflectX.shortClassName(this)+":"+toString()+":"+parms.toSpam();
  }

  //////////////////////////
  //
  private static void testsetup(ErrorLogStream dbg){
    dbg=ErrorLogStream.NonNull(dbg);//protect ourselves
    dbg.setLevel(LogSwitch.VERBOSE);
    PortProvider.Config(Main.props());
  }

  public static SerialConnection fortesters(TextListIterator args,int defbaud,ErrorLogStream dbg){
    testsetup(dbg);
    dbg.VERBOSE("mfaking easy properties");
    EasyCursor ezc=new EasyCursor();
    ezc.setBoolean("present",true);
    ezc.setString(Parameters.nameKey,StringX.OnTrivial(args.next(),"/dev/ttyS1"));//serial 2
    ezc.setInt(Parameters.baudRateKey,StringX.OnTrivial(args.next(),defbaud));
    ezc.setString(Parameters.protocolKey, StringX.OnTrivial(args.next(),"N81"));
    dbg.VERBOSE("cfg:"+ezc.asParagraph());
    return SerialConnection.makeConnection(ezc,defbaud);
  }

}
//$Id: SerialConnection.java,v 1.37 2003/07/27 05:35:03 mattm Exp $
