package net.paymate.lang.thread;

/**
 *  An internal class only for use with ThreadPool. It will notify the
 *  ThreadPool when it is done so that ThreadPool can get another piece of work
 *  going.
 *
 * @author     Chris Bitmead
 * @created    19 September 2001
 */
public class ThreadPoolThread extends Thread {
  ThreadPool pool;
  Runnable runnable;

  /**
   *  Create a thread belonging to a particular ThreadPool.
   *
   * @param  pool         the Threadpool this thread belongs to
   * @param  threadGroup  the ThreadGroup this thread belongs to
   * @param  runnable     Description of Parameter
   * @runnable            runnable the Runnable that represents the work we need
   *      to do.
   */
  public ThreadPoolThread(ThreadPool pool, ThreadGroup threadGroup, Runnable runnable) {
    super(threadGroup, runnable);
    this.pool = pool;
    this.runnable = runnable;
  }

  public Runnable getRunnable() {
    return runnable;
  }

  /**
   *  Do the work that this thread is allocated to do.
   */
  public void run() {
    try {
      runnable.run();
    } finally {
      pool.notifyOfWork();
    }
  }
}
