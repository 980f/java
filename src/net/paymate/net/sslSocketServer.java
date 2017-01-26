// $Id: sslSocketServer.java,v 1.17 2003/06/25 05:50:30 mattm Exp $

/*  This is a Multi-Threaded SSL socketed Server. SUpports RSA and DSA/DSS
*  Signatures. It uses a keytore that has a "myserver" alias.
*  The "myserver" entry is a keypair signed by Verisign...
*
*****************************************************************************/

package net.paymate.net;
import  net.paymate.util.*;

import java.io.*;
import java.util.*;

import java.net.*;
import javax.net.ssl.*;
import javax.net.*;
import java.security.*;
import java.security.cert.*;


public class sslSocketServer extends Thread implements AtExit {
  static final protected ErrorLogStream dbg=ErrorLogStream.getForClass(sslSocketServer.class);

  private static final String suites[] =  {    "SSL_RSA_WITH_RC4_128_SHA"  };

  private boolean keepRunning = true;
  public void AtExit() {
    keepRunning = false;
  }
  public boolean IsDown() {
    return !isAlive();
  }

  SSLServerSocket ssl_ss        =null;
  SSLSocket ssl_s               =null;

  public void go(int port) {
    try {
      ssl_ss = (SSLServerSocket) Trustee.makeContext().getServerSocketFactory().createServerSocket(port);
      ssl_ss.setEnableSessionCreation(true);
      ssl_ss.setNeedClientAuth(false);
      ssl_ss.setUseClientMode (false);
      ssl_ss.setEnabledCipherSuites(suites);
    } catch(Exception caught) {
      dbg.Caught(caught);
      return;
    }

    start();
  }

  public void run() {
    while(keepRunning) {
      try {
        ssl_s = (SSLSocket)ssl_ss.accept();
        dbg.WARNING("Server: Accepting....");
        new MTSktServer(ssl_s).start();
      } catch(Exception caught) {
        dbg.Caught(caught);
      }
    }
  }

  public static final void main (String argv[]) throws IOException {
    sslSocketServer sss = new sslSocketServer();
    sss.go(Integer.parseInt(argv[0]));
  }


}

class MTSktServer extends Thread implements AtExit {
  static final protected ErrorLogStream dbg=ErrorLogStream.getForClass(MTSktServer.class);

  public void AtExit() {
    // this is just for playing, but if you ever use it for real, fill this in
  }
  public boolean IsDown() {
    // this is just for playing, but if you ever use it for real, fill this in
    return false;
  }

  Socket s;
  MTSktServer (Socket s) {
    super("MTSktServer");
    this.s = s;
  }

  public void run () {
    PrintStream out = null;
    DataInputStream in = null;

    try {
      out = new PrintStream(s.getOutputStream());
      in  = new DataInputStream(s.getInputStream());

      String str = "YeeFrigginHowdy!!!";
      out.println(str);
      out.flush();

      s.close();
    } catch (IOException ioe) {
      dbg.Caught(ioe);
    }
  }
}

//$Id: sslSocketServer.java,v 1.17 2003/06/25 05:50:30 mattm Exp $
