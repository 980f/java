package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/SocketRequest.java,v $
 * Description:  socket creation request, for background creation.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

import net.paymate.util.*;
import java.net.*;

public class SocketRequest {
  private ErrorLogStream dbg;
  private Waiter toAwaken;
// moved to host interface// public boolean fresh;
  public MultiHomedHost host;
  public Socket socket;  //created one is returned via this
  private boolean abandon; //externally timedout


  public SocketRequest(MultiHomedHost host, Waiter toAwaken, ErrorLogStream dbg) {
    this.monitor = new Monitor(SocketRequest.class.getName());

    this.toAwaken = toAwaken;
    this.host = host;
    this.dbg = dbg;
    abandon = false;
  }

  public boolean isAbandoned() {
    return abandon;
  }

  /**
   * this is called from a background process.
   * this invokes the routine that doesn't timeout reliably (host.makeSocket)
   */
  public void go() {
    if(isAbandoned()) {
      dbg.ERROR("Creation already abandoned!");
    } else {
      Notify(host.makeSocket(this));
    }
  }

  /**
   * used to interlock abandon() and notify() since
   * both reference and modify the abandoned flag and the socket.
   */
  Monitor monitor;
  /**
   * this should only get called by the thread that was waiting upon toAwaken,
   * so we don't both twiddling that thread.
   */
  public void Abandon() {
    try {
      monitor.getMonitor();
      if(!abandon) {
        abandon = true;
        //if abandoned we are the only ones that have a reference to the socket.
        SocketX.Close(socket);
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      toAwaken.Stop();//tell the related thread to stop waiting.
      monitor.freeMonitor();
    }
  }

  /**
   * call when socket is ready
   * socket may be null.
   */
  private void Notify(Socket socket) {
    try {
      monitor.getMonitor();
      this.socket=socket;
      if(!isAbandoned()) {
        toAwaken.Stop();//tell the related thread to stop waiting.
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      monitor.freeMonitor();
    }
  }

}
//$Id: SocketRequest.java,v 1.11 2003/09/23 20:18:19 mattm Exp $
