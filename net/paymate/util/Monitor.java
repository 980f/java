/**
* Title:        Monitor<p>
* Description:  Implements an object-based locking system that can replace
*               all of the "synchronized" statements in the code<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: Monitor.java,v 1.18 2001/10/05 19:08:36 andyh Exp $
*/
/*
// a detailed usage statement:
void amethod() {
  try {
    yoreMon.getMonitor();
    dbg.Enter(); //<<done too soon and the log looks like the monitor didn't work
    //do protected stuff
  } finally {
    dbg.Exit(); //<<precede unlock to keep trace proper.
    yoreMon.freeMonitor();
  }
}

also note that on construciton you can supply your own debugger which has
nice messages within getMon and freeMon.


*/

package net.paymate.util;
import  java.util.*;

public class Monitor implements Comparable {
  protected Thread thread = null;
  /**
  * monitorCount tracks the nesting level of the owning thread
  */
  protected int monitorCount = 0;
  public String name = "";
  private Vector threads = new Vector();

  // this function fixes the loop / race condition which used to exist with this line:
  //  private static final ErrorLogStream DBG = new ErrorLogStream(Monitor.class.getName());
  // eg: Monitor creates ErrorLogStream, which creates LogFile, which creates Monitor, whose "class" is not yet loaded, so results in an exception and subsequent null pointers.
  private final ErrorLogStream dbg() {
    synchronized(Monitor.class) { // sync on the CLASS !
      if(d1b2g3 == null) {
        d1b2g3 = ErrorLogStream.Null();
      }
    }
    return d1b2g3;
  }
  private static ErrorLogStream D1B2G3 = null; // don't use me directly; use dbg() instead !
  private ErrorLogStream d1b2g3 = null; // don't use me directly; use dbg() instead !
  private static ErrorLogStream D1B2G3() { // don't use me directly; use dbg() instead !
    if(D1B2G3 == null) {
      D1B2G3 = new ErrorLogStream(Monitor.class.getName());
    }
    return D1B2G3;
  }

  //each monitor is linkable to some other module's debugger, typically the owner's.
  public Monitor(String name,ErrorLogStream dbg) {
    this.name = name;
    this.d1b2g3 = dbg; // see dbg(), but don't call in here or you will get a loop/construction race condition
    addMonitor(this);
  }

  public String ownerInfo(){
    synchronized(this){
      String threadpart=thread!=null?thread.toString():"noOwner";
      return name+"/"+threadpart+"*"+monitorCount;
    }
  }

  /**
  * use class debugger, which is normally OFF
  */
  public Monitor(String name) {
    this(name,null);
  }
//getMonitor is called by debug code and infinite loops if you attempt dbg within it.
  public void getMonitor() {
    synchronized(this) {
      try {
//      System.out.println("getMonitor about to lock:");
//        dbg.Enter("locking "+name);
        while (tryMonitor() == false) {
          try {
            setState(true);
            wait();
          } catch (Exception e) {
            // stub
          } finally {
            setState(false); // +++ should this be in freeMonitor()?
          }
        }
//        dbg.VERBOSE("got lock");
      } catch (Exception e2) {
        dbg().Caught(e2);
      } finally {
//        dbg.Exit();
      }
    }
  }

  // +++ what happens if someone calls this who didn't call getMonitor()?
  // +++ should teh setSTate(false) be in here so it can be looked up first?
  // no, cause someone might forget to call freeMonitor() --- maybe
  public void freeMonitor() {
    synchronized(this) {
//      dbg.Enter("freeing "+name);
      try {
        if (getMonitorOwner() == Thread.currentThread()) {
          if ((--monitorCount) == 0) {
            thread = null;
            notify();
          }
        }
      } catch (Exception e2) {
        dbg().Caught(e2);
      } finally {
//        dbg.Exit();
      }
    }
  }
  //noisy versions of the above:

  public void LOCK(String moreinfo){
    dbg().VERBOSE(moreinfo+" trying to Lock:"+ownerInfo());
    synchronized(this){
      getMonitor();
      dbg().VERBOSE("Locked by: "+moreinfo+" "+Thread.currentThread());
    }
  }

  public void UNLOCK(String moreinfo){
    synchronized(this){
      dbg().VERBOSE("Freed by: "+moreinfo+" "+Thread.currentThread());
      freeMonitor();
    }
  }


  public boolean tryMonitor() {
    boolean ret = false;
    synchronized(this) {
      try {
        if (thread == null) {
          ret = true;
          monitorCount = 1;
          thread = Thread.currentThread();
        } else {
          if (thread == Thread.currentThread()) {
            ret = true;
            monitorCount++;
          }
        }
      } catch (Exception e2) {
        dbg().Caught(e2);
      }
    }
    return ret;
  }

  public Thread getMonitorOwner() {
    Thread ret = null;
    synchronized(this) {
      try {
        ret = thread;
      } catch (Exception e2) {
        dbg().Caught(e2);
      }
    }
    return ret;
  }

  public TextList dump() {
    TextList tl = new TextList();
    synchronized(this) {
      try {
        for(int i = threads.size(); i-->0;) {
          try {
            tl.add(((Thread)(threads.elementAt(i))).toString());
          } catch(ArrayIndexOutOfBoundsException arf){
            //we modified the list while scanning it, start scan over:
            i = threads.size();
            continue;
          }
        }
      } catch (Exception e) {
        dbg().Caught(e);
      }
    }
    return tl;
  }

  // no sync required here
  private boolean setState(boolean add) {
    return setState(Thread.currentThread(), add);
  }

  private boolean setState(Thread thread, boolean add) {
    boolean allThere = true;
    synchronized(this) {
      try {
        Thread threader = findThread(thread);
        if(add) {
          if(threader != null) {
            // ERROR!
          } else {
            threads.add(thread);
          }
        } else {
          allThere = false;
          if(threader == null) {
            // ERROR!
          } else {
            threads.remove(threader);
          }
        }
      } catch (Exception e2) {
        dbg().Caught(e2);
      }
    }
    return allThere;
  }

  private Thread findThread(Thread thread) {
    Thread sThread = null;
    synchronized(this) {
      try {
        for(int i = threads.size(); i-->0;) {
          try {
            sThread = (Thread) (threads.elementAt(i));
          } catch(ArrayIndexOutOfBoundsException arf){
            //we modified the list while scanning it, start scan over:
            i = threads.size();
            continue;
          }
          if(sThread == thread) {
            break;//return sThread;
          }
        }
        if(sThread != thread) {
          sThread = null;
        }
      } catch (Exception e2) {
        dbg().Caught(e2);
      }
    }
    return sThread;
  }

  // static list stuff
  private static final WeakSet list = new WeakSet();

  public static final Vector dumpall() {
    Vector v = new Vector(); // for sorting
    synchronized(list) {
      try {
        for(Iterator i = list.iterator(); i.hasNext();) {
          v.add(i.next());
        }
      } catch (ConcurrentModificationException cme) {
        D1B2G3().Caught(cme); // ok
        v.add("--- Report aborted: ConcurrentModificationException (noncritical, just happens with multiple threads sometimes).");
      } catch (Exception e) {
        D1B2G3().Caught(e); // ok
      }
    }
    Collections.sort(v);
    return v;
  }

  protected static final void addMonitor(Monitor monitor) {
    synchronized(list) {
      try {
        list.add(monitor);
      } catch (Exception e2) {
        D1B2G3().Caught(e2); // ok
      }
    }
  }

  /*
  protected static final void deleteMonitor(Monitor monitor) {
    synchronized(list) {
      try {
        list.remove(monitor);
      } catch (Exception e2) {
        DBG.Caught(e2);
      }
    }
  }
  */

  /*
  protected static final Monitor findMonitor(Monitor monitor) {
    Monitor mon = null;
    synchronized(list) {
      try {
        for(Iterator i = list.iterator(); i.hasNext();) {
          mon = (Monitor) (i.next());
          if(mon == monitor) {
            break;//return mon;
          }
        }
        if(mon != monitor) {
          mon = null;
        }
      } catch (Exception e2) {
        DBG.Caught(e2);
      }
    }
    return null;
  }
  */

  public int compareTo(Object o) {
    int i = 0;
    try {
      i = name.compareTo(((Monitor)o).name);
    } catch (Exception e) {
      /// +++ bitch
    }
    return i;
  }

}
//$Id: Monitor.java,v 1.18 2001/10/05 19:08:36 andyh Exp $
