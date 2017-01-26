package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/UDPServer.java,v $
 * Description:  base UDP server.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import net.paymate.util.*; // ErrorLogStream
import java.net.*; // DatagramSocket
import net.paymate.lang.StringX;

public abstract class UDPServer implements Runnable {
  protected ErrorLogStream dbg ;

  private int port;
  private String sourceip;
  private DatagramSocket dsock;
  private Thread myThread = null;
  private String name = null;
  int bufferSize;

  private UDPServer() {
    // NO!
  }

  public UDPServer(String name, boolean isDaemon) {
    this.name = name;
    dbg= ErrorLogStream.getExtension(UDPServer.class,name);//each instance gets its own debugger
    myThread = new Thread(this, name);
    myThread.setDaemon(isDaemon); //someone had set this to false. Most users will want it to be true.
  }

  public UDPServer(int port, int bufferSize, String name, boolean isDaemon, String sourceip) {
    this(name, isDaemon);
    configure(port,bufferSize,sourceip);
  }

  public UDPServer(int port, int bufferSize, String name, boolean isDaemon) {
    this(name, isDaemon);
    configure(port,bufferSize);
  }

  /**
   * configure and start the server
   * @param port is served at any available local ip address
   * @param buffersize is the maximum UDP packet received. bigger ones get truncated without warning.
   */
  public UDPServer configure(int port, int bufferSize, String sourceip){//this was buried inside the constructor, and the run()
    this.port = port;
    this.bufferSize = bufferSize;
    this.sourceip = sourceip;
    try {
      if(StringX.NonTrivial(sourceip)) {
        dsock = new DatagramSocket(port, InetAddress.getByName(sourceip));
      } else {
        dsock = new DatagramSocket(port);
      }
      dsock.setReceiveBufferSize(bufferSize);
      myThread.start();
    } catch(Exception sockit2me){
      dbg.Caught(sockit2me);
    }
    return this;
  }

  public UDPServer configure(int port, int bufferSize){//this was buried inside the constructor, and the run()
    return configure(port, bufferSize, null);
  }


  public boolean cont = true;

  public void kill() {
    cont = false;
    try {
      dsock.close();
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  public void run() {
    try {
      dbg.WARNING("entering receive loop");
      while(cont) {
        try {
          DatagramPacket p = new DatagramPacket(new byte[bufferSize], bufferSize);
          dbg.WARNING("about to receive");
          dsock.receive(p);
          dbg.WARNING("about to handlePacket");
          handlePacket(p);
        } catch (Exception ex) {
          dbg.Caught(ex);
        }
      }
    } catch (Exception ex2) {
      dbg.Caught(ex2);
    } finally {
      dsock.close();
      dbg.WARNING("service stopped");
    }
  }

  protected abstract void handlePacket(DatagramPacket p);

}