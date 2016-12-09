package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/ThreadX.java,v $
 * Description:  static utilities that are handy with threads
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.timer.*;

public class ThreadX {
/**
 * polite and properly synch'd version of Object.wait()
 * this one swallows interrupts
 * @return true if NOT notified.
 */
  public static final boolean waitOn(Object obj,long millisecs,boolean allowInterrupt,ErrorLogStream dbg){
    dbg=ErrorLogStream.NonNull(dbg);//avert NPE
    synchronized(obj){
      try{
        dbg.Enter("waitOn");
        StopWatch resleeper=new StopWatch();//to shorten successive timeouts when we sleep again after an interrupt
        while(true){
          try {
            long waitfor=millisecs-resleeper.millis();
            if(waitfor<0){
              dbg.WARNING("timed out");
              return true;
            }
            dbg.VERBOSE("waiting for "+waitfor);
            obj.wait(waitfor); //will throw IllegalArgumentException if sleep time is negative...
            dbg.VERBOSE("proceeds after "+resleeper.Stop()+"/"+millisecs);
            return resleeper.Stop()> millisecs; //preferred exit
          }
          catch(InterruptedException ie){
            if(allowInterrupt){
              dbg.VERBOSE("interrupted,exiting");
              return true;
            } else {
              dbg.WARNING("interrupted,ignored");
              continue;
            }
          }
//  added precheck in try section.
//          catch (IllegalArgumentException illarg){//
//            dbg.WARNING("timed out");
//            return true;
//          }
          catch (Exception ex) {
            dbg.Caught(ex);
            return true;
          }
        }
      } finally {
        dbg.Exit();
      }
    }
  }

  public static final boolean waitOn(Object obj,long millisecs,ErrorLogStream somedbg){
    return waitOn(obj, millisecs, false,ErrorLogStream.NonNull(somedbg));
  }
  public static final boolean waitOn(Object obj,long millisecs){
    return waitOn(obj, millisecs, false,ErrorLogStream.Null());
  }

/**
 * polite and properly synch'd version of Object.notify()
 * @return true if notify did NOT happen
 */
   public static final boolean notify(Object obj){
    synchronized (obj){
      try {
        obj.notify();
        return false;
      }
      catch (Exception ex) {//especially null pointer exceptions
        return true;
      }
    }
  }

  /**
  * returns true if it completed its sleep (was NOT interrupted)
  */
  public static final boolean sleepFor(long millisecs,boolean debugit) {
    StopWatch arf= new StopWatch();
    boolean interrupted = false;
    try {
      if(debugit){
        ErrorLogStream.Debug.ERROR("Safe.sleepFor():"+millisecs);
      }
      Thread.sleep(millisecs>0?millisecs:0);//sleep(0) should behave as yield()
    } catch (InterruptedException e) {
      interrupted = true;
      if(debugit){
        ErrorLogStream.Debug.ERROR("Safe.sleepFor() got interrupted after:"+arf.millis());
      }
    } finally {
      boolean completed = !(Thread.interrupted() || interrupted);
      if(debugit){
        ErrorLogStream.Debug.ERROR("Safe.sleepFor() sleptFor "+arf.millis()+" ms and was " + (completed?"NOT ":"") + "interrupted.");
      }
      return completed;
      //return !Thread.interrupted(); // did not work correctly
    }
  }

  public static final boolean sleepFor(long millisecs) {
    return sleepFor(millisecs,false);
  }

  public static final void sleepFor(double seconds) {
    sleepFor(Ticks.forSeconds(seconds));
  }

  /**
   * caller is responsible for trying to make the thread stop()
   */
  public static final boolean waitOnStopped(Thread mortal,long maxwait){
    mortal.interrupt();
    try {
      mortal.join(maxwait);
      return false;
    } catch (Exception ex) {
      ErrorLogStream.Debug.Caught("Safe.waitOnStop()",ex);//+_+ needs more info
      return true;
    }
  }

  public static final boolean wait(Thread me, long maxwait) {
    try {
      me.wait(maxwait);
      return false;
    } catch (InterruptedException ex) {
      return true;
    }
  }

  public static final boolean join(Thread it, long maxwait) {
    try {
      it.join(maxwait);
      return false;
    } catch (InterruptedException ex) {
      return true;
    }
  }

  public static final ThreadGroup RootThread(){
    ThreadGroup treeTop = Thread.currentThread().getThreadGroup();
    ThreadGroup tmp = null;
    while((tmp = treeTop.getParent()) != null) {
      treeTop = tmp;
    }
    return treeTop;
  }

  public static final TextList ThreadDump(ThreadGroup tg){
    if(tg==null){
      tg=RootThread();
    }
    TextList ul=new TextList();
    int threadCount = tg.activeCount();
    Thread [] list = new Thread [threadCount * 2]; // plenty of room this way
    int count = tg.enumerate(list);
    for(int i = 0; i<count; i++) {
      Thread t = list[i];
      if(t.getThreadGroup() == tg) {
        String name = t.getName() + "," + t.getPriority() + ((t.isDaemon()) ? ",daemon" : "");
        if(t instanceof ThreadReporter) {
          ThreadReporter sess = (ThreadReporter)t;
          name += sess.status();
        }
        ul.add(name);
      }
    }
    // print the child thread groups
    int groupCount = tg.activeGroupCount();
    ThreadGroup [] glist = new ThreadGroup [groupCount * 2]; // plenty of room this way
    groupCount = tg.enumerate(glist);
    for(int i = 0; i<groupCount; i++) {
      ThreadGroup g = glist[i];
      if(g.getParent() == tg) {
        ul.appendMore(ThreadDump(g));
      }
    }
    return ul;
  }


}
//$Id: ThreadX.java,v 1.4 2001/11/03 00:19:56 andyh Exp $