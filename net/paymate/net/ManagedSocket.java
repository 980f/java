/**
 * Title:        ManagedSocket
 * Description:  Manages socket connections.  Automates reading of input on a separate thread.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ManagedSocket.java,v 1.9 2001/10/02 17:06:39 mattm Exp $
 *
 * This class is responsible for managing a thread to do read from the socket,
 * and to generate an event when something is received.
 *
 * This class does not deal with reconnects at this time.
 *
 */

// +++ replace all uses of sockets that read with this

// ++++++++++++++++++++ NOT DONE OR TESTED YET!!!!!
// ++++++++++++++++++++ refer to the secureconnection stuff on how they did it

package net.paymate.net;
import  java.net.Socket;
import  java.io.*;
import  net.paymate.util.*;

public abstract class ManagedSocket implements Runnable, AtExit  {
  protected static final ErrorLogStream dbg = new ErrorLogStream(ManagedSocket.class.getName());

  private Socket socket = null;
  private Thread thread = null;
  private String name = null;
  private boolean live = false;

  private static final boolean canChangeThread(Thread thread) {
    if((thread != null) && thread.isAlive()) {
      return false;
    }
    return true;
  }

  public String getName() {
    return name;
  }

  public boolean start(Socket socket, Thread thread) {
// +++ mutex
    if(((socket!=this.socket) || (thread!=this.thread)) && !canChangeThread(thread)) {
// +++ bitch
      return false;
    }
    if(socket == null) {
// +++ bitch
      return false;
    }
    if(thread == null) {
      thread = new Thread(this);
    }
    this.socket = socket;
    this.thread = thread;
    thread.setName(name);
    live = true;
    thread.start();
    return thread.isAlive();
  }

  public boolean start(Socket socket) {
    return start(socket, thread);
  }

  public boolean start() {
    return start(socket, thread);
  }

  public boolean stop() {
    live = false;
    if(socket != null) {
      try {
        socket.close();
      } catch (Exception e) {
        dbg.Caught("stop() excepted closing socket", e);
      }
    }
    /// +++ join() ??? to wait on the thread to close ???
    return true;
  }

  public ManagedSocket(String name) {
    this.name = name;
  }

  /**
   * Overload this function so that you can deal with incoming data.
   */
  public abstract void receivedData(int datum);

  private Monitor thisMonitor = new Monitor("ManagedSocket");

  /**
   * Don't call this function.  It is for internal (thread) use only.
   */
  public void run() {
    // synhronize the entire contents of this function on 'this' (with markers),
    // as this function should never be run more than once!
    try {
      thisMonitor.getMonitor();
      while(live) {
        try {
          InputStream is = socket.getInputStream();
// +++ test for null
          int datum = is.read();
          try {
            receivedData(datum);
          } catch (Exception e2) {
            dbg.Caught(e2);
            // +++ deal with it

          }
        } catch (Exception e) {
          dbg.Caught(e);
          // +++ handle the different kinds of exceptions and recover from them.
        }
      }
    } finally {
      thisMonitor.freeMonitor();
    }
  }

  private static final Object writersBlock = new Object();
  private static final Monitor writersBlockMonitor = new Monitor("writersBlock");
  private static final int sleepDurationMs = 50;

  // this function will possibly get called by LOTS of different threads!
  public boolean write(byte [] data) {
    // the synchronization of this block will prevent other threads from writing to it, as flood control.
    boolean sent = false;
    try {
      writersBlockMonitor.getMonitor();
      OutputStream os = socket.getOutputStream();
// +++ test for null
      os.write(data);
      sent = true;
      // only try to send for 35 seconds (+++ get from config)
    } catch (Exception e) {
      dbg.Caught(e);
      // +++ bitch
    } finally {
      writersBlockMonitor.freeMonitor();
      return sent;
    }
  }

}


/*
2) PayMate.net will no longer send echo requests to CardSystems every 2
minutes.  We MIGHT send echo requests just before sending the first
request after a reconnect, to be sure the socket is connected, but this
requires testing to see if it is really beneficial.  Chances are good
that we WON'T do this.

4) Strongbox needs to handle timeout reversals, even if the original
transaction was never received (correctly), so that we can send
them. Testing may be required.

5) PayMate.net will modify our copy of the SecureConnection software so
that it will continually attempt communication retries (resend the
entire message that was in progress when the socket dropped) until some
kind of response is received.  It will hold back the flow of all
messages from the terminal that sent the original failed message until
the retry is successful (FloodManagement).

6) Gail will research which event codes are generated for timeouts and
for socket disconnects in the maverick authorizer module.  Matt will be
sure they are mapped to PayMate.net-desired response codes.
(Want to differentiate SecureConnection timeouts from
CardSystems-generated BankNotSupported codes, and also from socket
disconnects. Note: ActionCode="N", suspect 91 for timeouts and 92 for
SocketDrops.  See events table.)

7) Darla is sending Matt the M-format spec (per separate 3/13 17:17 CDT
phone call), so that he can more easily perform FloodManagement.

8) Once 1-7 are resolved, PayMate.net will begin recertification.

Use separate threads to read from and write to sockets.  Use the thread
that WANTS to write to write the socket, and create a separate
per-socket thread to read from it.  Create a class to do this, and then
extend the class for certain uses.  See the Java Threading book for
how/why.

PersistentSocket
Runnable class that contains a thread and provides for socket-like
functions (write).  Extend the class to overload certain default functions:
1) The class uses a thread to read from the socket.
2) If the read thread or a thread performing a write get an error, the
socket is marked as unusable and destroyed and recreated by the read thread.
3) The write function caches the thing being written, and only after a
successful write does it let the content go, otherwise, it hangs on to
it to resend after the socket is recreated.
4) The onFailure() function is called when a critical error occurs, just
after the socket is marked unusable.  A recreate() function is provided
to allow the user to call it in the extension of onFailure().  The
default behavior of onFailure() is to mark the socket as unusable,
recreate the socket, and if needed, send any data that didn't go the
first time.
5) In a given period of time, the reconnection will only happen a
certain number of times.
6) Wait n milliseconds before reconnecting.

CertifiedSocket is a layer above this (uses one of these).

+++ Create a separate write thread that uses a queue?

When the keepalive option is set for a TCP socket and no data has been exchanged
across the socket in either direction for 2 hours (NOTE: the actual value is
implementation dependent), TCP automatically sends a keepalive probe to the peer.
This probe is a TCP segment to which the peer must respond. One of three
responses is expected:
1. The peer responds with the expected ACK. The application is not notified
(since everything is OK). TCP will send another probe following another 2 hours
of inactivity.
2. The peer responds with an RST, which tells the local TCP that the peer host
has crashed and rebooted. The socket is closed.
3. There is no response from the peer. The socket is closed.
The purpose of this option is to detect if the peer host crashes.
Valid only for TCP socket: SocketImpl

*/
