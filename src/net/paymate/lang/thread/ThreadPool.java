package net.paymate.lang.thread;

/**
 *  Implements a pool of threads. Only a limited number of threads can execute
 *  at once. If you try to exceed the thread limit you will block waiting. This
 *  avoids overloading a machine with threads.
 *
 * @author     Chris Bitmead
 * @created    6 September 2001
 */
public class ThreadPool {
  ThreadPoolThread[] threads;
  ThreadGroup threadGroup;
  ThreadSleeper sleeper;

  /**
   * @param  name        the name of the ThreadGroup
   * @param  maxThreads  the maximum number of threads we allow at once.
   */
  public ThreadPool(String name, int maxThreads) {
    threadGroup = new ThreadGroup(name + "-Pool");
    threads = new ThreadPoolThread[maxThreads];
    sleeper = new ThreadSleeper();
  }

  public ThreadPoolThread[] getThreads() {
    return threads;
  }

  /**
   *  Execute the given procedure when a thread slot becomes available.
   *  Remember that Thread implements Runnable so
   *  you can pass any thread to this method, although the real thread will be
   *  the ThreadPoolThread.
   *
   * @param  runnable  the runnable that represents the work that shall be done.
   */
  public boolean start(Runnable runnable) {
    boolean noBlock = true;
    while (!startNoBlock(runnable)) {
      noBlock = false;
      sleeper.waitForWork();
    }
    return noBlock;
  }

  /**
   *  Execute the given procedure if a thread slot is available.
   *  If none is available, return immediatiely.
   *  Remember that Thread implements Runnable so
   *  you can pass any thread to this method, although the real thread will be
   *  the ThreadPoolThread.
   *
   * @param  runnable  the runnable that represents the work that shall be done.
   * @return           false if there was no slot available. Try again later.
   */
  public boolean startNoBlock(Runnable runnable) {
    boolean done = false;
    for (int i = 0; i < threads.length && !done; i++) {
      if (threads[i] == null || !threads[i].isAlive()) {
        done = true;
        threads[i] = new ThreadPoolThread(this, threadGroup, runnable);
        threads[i].start();
      }
    }
    return done;
  }

  /**
   *  Notify us that a thread has completed.
   */
  public void notifyOfWork() {
    sleeper.notifyOfWork();
  }

  /**
   *  Wait for all threads to complete.
   */
  public void join() {
    for (int i = 0; i < threads.length; i++) {
      if (threads[i] != null && threads[i].isAlive()) {
        try {
          threads[i].join();
        } catch (InterruptedException e) {
          // Doesn't matter
        }
      }
    }
  }

  /**
   *  How many threads are active right now? This number can change at any time,
   *  so it is not particularly useful except for providing interesting
   *  diagnostics.
   *
   * @return    Description of the Returned Value
   */
  public int numberOfActiveThreads() {
    int rtn = 0;
    for (int i = 0; i < threads.length; i++) {
      if (threads[i] != null && threads[i].isAlive()) {
        rtn++;
      }
    }
    return rtn;
  }
}
