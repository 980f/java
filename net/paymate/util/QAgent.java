package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/QAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

public class QAgent implements Runnable {
  private QActor actor;
  private ObjectFifo fifo;
  private ErrorLogStream dbg=ErrorLogStream.Debug;//20011031  by default spew. previously null()
  private long keepAlive=Ticks.forSeconds(1000.0);        //an arbitrary keep alive time, 4debug
  private boolean amStopped=true;

//  private Runnable agent; //a run() that this guy keeps running
  private boolean killed=false; //need class cleanThread
  protected Thread thread;
  private String myname;
  protected Object inputWakeupLock=new Object();

  public QAgent config(double keepalive){//consider mutexing
    this.keepAlive=Ticks.forSeconds(keepalive);
    return this;
  }

  public QAgent config(int keepaliveTix){//consider mutexing
    this.keepAlive=keepaliveTix;
    return this;
  }

  public QAgent config(ErrorLogStream dbg){//consider mutexing
    this.dbg=ErrorLogStream.NonNull(dbg);
    dbg.VERBOSE("debugging QAgent:"+thread.getName());
    return this;
  }

  public final synchronized boolean Post(Object arf){//MAIN access point
    dbg.VERBOSE("Posting:"+Safe.ObjectInfo(arf));
    return put(arf)>0; //not checking types yet
  }

  public void run(){//implements Runnable
    amStopped=false;
    try{
      dbg.VERBOSE("run() entered");
      while(!killed){
        Object todo=fifo.next();
        if(todo==null){
          dbg.VERBOSE("going to sleep");
          ThreadX.waitOn(inputWakeupLock,keepAlive);
          continue;
        }
        try {
          dbg.VERBOSE("about to runone");
          actor.runone(todo);
        }
        catch (Exception any){
          dbg.Caught(any);
        }
      }
    }
    catch (Throwable panic){
      dbg.Caught(panic);
    }
    finally {
      amStopped=true;
      dbg.VERBOSE("run() exits");
    }
  }

  public synchronized void Clear(){
    fifo.Clear();
    try {
      ErrorLogStream.Debug.ERROR("start thread:"+thread.getName());
      thread.start(); //
    }
    catch (IllegalThreadStateException ignore) {
      ErrorLogStream.Debug.WARNING(ignore.toString());
    }
  }

  private void inputNotify(){
    ThreadX.notify(inputWakeupLock);
  }

  public synchronized int put(Object obj){
    int size=fifo.put(obj);
    inputNotify();
    return size;
  }

  public synchronized int atFront(Object obj){
    int size=fifo.atFront(obj);
    inputNotify();
    return size;
  }

  /**
   * @return true if overwrote rather than added
   * @param obj defines uniqueness via .equals(obj)
   * @param atfront is where to put obj if it isn't replacing something
   * this version always replaces old with new.
   */
  public synchronized boolean putUnique(Object obj,boolean atfront){
    if(fifo.replace(obj)){
      return true; //does NOT notify. Shoudl be ok to do so.
    } else {
      if(atfront){
        atFront(obj);
      } else {
        put(obj);
      }
      return false;
    }
  }

  public int removeAny(Object obj){
    return fifo.removeAny(obj);
  }

  public synchronized boolean Stop() /*throws QAgentStopper*/ {
      killed=true;
//NO! restarts thread!    Clear();
      return Stopped(); //first attempt to stop...
  }

  /**
   * @return whether run loop has exited
   */
  public boolean Stopped() {//and try to make it stop.
    try {
      if(killed && !amStopped){
        dbg.WARNING("Trying to stop");
        thread.interrupt();
      }
      return amStopped;
    }
    finally {
      actor.Stop();
    }
  }

  private QAgent (String threadname, QActor actor){
    myname=threadname;
    thread=new Thread(this,myname);
    killed=false;
    thread.setDaemon(true);
    this.actor=actor;
    fifo=new ObjectFifo();
  }

  public static QAgent New(String threadname, QActor agent){
    return new QAgent(threadname, agent);
  }

  /**
   * This exists for reporting purposes only.  DO NOT use it to iterate!
   */
  public final int Size() {
    return fifo.Size();
  }

}
//$Id: QAgent.java,v 1.11 2001/11/17 00:38:35 andyh Exp $