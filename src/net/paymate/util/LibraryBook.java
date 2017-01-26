package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/LibraryBook.java,v $
 * Description:  A single boolean that you can checkout and return.
 *               If someone has already checked it out, you cannot do so until they have returned it.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

public class LibraryBook {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LibraryBook.class);

  public LibraryBook() {
  }

  private static Counter mutexCounter = new Counter(); // just for naming and counting

  private boolean value = false;
  private Monitor checkoutMutex = new Monitor("checkoutMonitor_"+mutexCounter.incr());

  // for standins
  public boolean Checkout() {
    boolean gotit = false;
    checkoutMutex.getMonitor();
    try {
      if(!value) {
        value = true;
        gotit = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      checkoutMutex.freeMonitor();
      return gotit;
    }
  }

  public void Return() {
    checkoutMutex.getMonitor();
    try {
      if(value) {
        value = false;
      } else {
        // +++ ???
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      checkoutMutex.freeMonitor();
    }
  }

  public boolean isCheckedOut() {
    return value;
  }

}
