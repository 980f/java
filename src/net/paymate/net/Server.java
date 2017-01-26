package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/Server.java,v $</p>
 * <p>Description:    wraps java's thread's illegitimate stop()
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

import net.paymate.util.*;
import java.io.*;

public class Server implements Runnable {
  static final ErrorLogStream dbg= ErrorLogStream.getForClass(Server.class);
  Thread reader;              //input "polling"
  Runnable runnee;
  protected boolean killed =false;

  private String name = "unnamed";
  private boolean isDaemon = false;

  public boolean isRunning(){
    return !killed && reader!=null && reader.isAlive();
  }

  public Server(String name, Runnable runnee, boolean isDaemon) {
    this.name = name;
    this.isDaemon = isDaemon;
    if(runnee==null){
      runnee=this;
    }
    this.runnee=runnee;
  }

  public Server(String name, boolean isDaemon) {
    this(name, null, isDaemon);
  }

  public Runnable Service(){
    return runnee;
  }

  /**
   * Combine ThreadX, QAgent, and this class into a new base called Agent into np.lang.
   * Agent has a final run function with a parameterized do/while(Continue) loop allowing stopping of the loop via:
   *   Continue (does an interrupt and continues looping)
   *   Break (interrupt and stop looping)
   *   End (stop looping, but finish the current loop)
   * The loop calls an abstract runone() function on each loop.
   * All Agents are registered, allowing external manual management.
   */

  public boolean Start(){
    dbg.VERBOSE("Start()ing: runnee="+runnee+", name="+name);
    try {
      if(isRunning()) {
        return false;
      }
      if(reader == null) {
        reader=new Thread(runnee, name);
        reader.setDaemon(isDaemon);
      }
      reader.start();
      return true;
    } catch(Exception ex){
      dbg.Caught("While starting",ex);
      return false;
    }
  }

  public void Stop(){//for use in panics only
    dbg.ERROR("Killed!  Stop() called:\n"+dbg.whereAmI());
    killed=true;
    reader.interrupt();
  }

  /**
   * run() should poll "killed" in its loop, especially checking it when an interrupt occurs.
   */
  public void run(){
    //dies right away.
    dbg.ERROR("OOPS!  Empty run!");
  }

}
//$Id: Server.java,v 1.9 2002/07/09 17:51:30 mattm Exp $