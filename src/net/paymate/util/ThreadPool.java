/**
 * Title:        ThreadPool
 * Description:  Thread pool.  Recycles threads.  Makes systems faster.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ThreadPool.java,v 1.3 2001/07/19 01:06:55 mattm Exp $
 */

package net.paymate.util;

// +++ search for places that do "new Thread("
// +++ and replace with a function to get one from here

public class ThreadPool {

  public static final Thread newThread(Runnable toRun, String name) {
    Thread ret = null;
    // +++ first, wade through the pool looking for an unused thread
    // +++ next, mark the thread as used
    // +++ finally,
    return ret;
  }

  public ThreadPool() {
  }
}
