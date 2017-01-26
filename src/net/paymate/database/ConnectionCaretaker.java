package net.paymate.database;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/database/ConnectionCaretaker.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.util.*;
import net.paymate.lang.ThreadX;

public class ConnectionCaretaker implements Runnable {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ConnectionCaretaker.class);

  // the daemon for keeping it alive ...

  private ConnectionPool cp = null;

  public ConnectionCaretaker(ConnectionPool cp) {
    this.cp = cp;
  }

  public synchronized void stop() {
    stopped = true;
    thread = null;
    // +++ interrupt thread!
  }
  public synchronized void start() {
    stopped = false;
    thread = new Thread(this, "CaretakerFor_"+cp);
    thread.setDaemon(true);
    thread.start();
  }
  public boolean isStopped() {
    return stopped;
  }
  private Thread thread = null;
  // this next one is really prebuild
  private int prebuild = 0; // how many extras to keep around for speed; default to 0 so that we don't screw up the validator
  private boolean stopped = true;
  private int intervalSecs = 5;
  public void setPrebuild(int more) {
    prebuild = more;
  }
  public void setIntervalSecs(int secs) {
    intervalSecs = secs;
  }
  public void run() {
    try{
      dbg.VERBOSE("ConnectionFifo.run() entered ");
      cp.prebuild(prebuild); // create them all; just first time
      while(!stopped && (thread == Thread.currentThread())){ // @IPFIX@ if this loop dies, the thread dies.  put the try inside the loop ???  kill the whole program instead (restart) ???
        try {
          cp.checkUnused(); // keepalive the unused/available connections, to keep them from going stale
          cp.spamUsed();       // output to the log file what connections are in use
          ThreadX.sleepFor(Ticks.forSeconds(intervalSecs));
        } catch (Exception any){
          dbg.Caught(any);
        }
      }
    } catch (Throwable panic){
      dbg.Caught(panic);
    } finally {
      dbg.VERBOSE("ConnectionFifo.run() exits");
    }
  }
}
