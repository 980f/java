package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/ByteServer.java,v $</p>
 * <p>Description:    implements an accept() thread
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

import java.net.*;
import java.io.*;
import net.paymate.util.*; // Safe, dbg
import net.paymate.lang.ThreadX;

public abstract class ByteServer extends Server {

  private ServerSocket ss = null;
  private int port = -1;

  // non blocking
  public ByteServer(String name, int port, boolean isDaemon) {
    this(name, port, isDaemon, false, -1);
  }

  public ByteServer(String name, int port, boolean isDaemon, boolean blocking, double sleeptimeSeconds) {
    super(name, isDaemon);
    this.port = port;
    this.sleeptimeSeconds = sleeptimeSeconds;
    this.blocking = blocking;
  }

  // blocking
  public ByteServer(String name, int port, boolean isDaemon, double sleeptimeSeconds) {
    this(name, port, isDaemon, true, sleeptimeSeconds);
  }

  private boolean blocking = false;
  private double sleeptimeSeconds = 3;

  public void run() {
    while(!killed) {
      Socket so = null;
      try {
        if(ss == null) {
          dbg.WARNING("binding port");
          ss = new ServerSocket(port);
        }
        dbg.WARNING("accepting connections at "+ss.getLocalPort() + " ...");
        so = ss.accept();
        dbg.WARNING("accepted connection; handling connection ...");
        onAccept(so);
        dbg.WARNING("handled connection.");
      } catch(java.net.BindException portinuse){
        dbg.ERROR("port in use:"+port);
        killed=true;
      } catch(Throwable caught) {
        dbg.Caught(caught);
      }
      if(blocking) {
        // Now, close the stuff, since we might rebind ...
        SocketX.Close(so);
        SocketX.Close(ss); // since we might rebind!
        ss = null;
        dbg.ERROR("kiled == " + killed + ", sleeping for " + sleeptimeSeconds +
                  " seconds...");
        if (!killed) {
          dbg.ERROR("sleeping for " + sleeptimeSeconds + " seconds...");
          ThreadX.sleepFor(sleeptimeSeconds);
        }
      }
    }
  }

  /**
   * onAccept() should either exit quickly or monitor "killed"
   */
  public abstract void onAccept(Socket so);

}
//$Id: ByteServer.java,v 1.11 2003/07/27 05:35:11 mattm Exp $