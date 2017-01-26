package net.paymate.web;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/StatusServer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.net.*;  // UDPServer
import net.paymate.util.*; // Service
import java.net.*;         // DatagramPacket
import net.paymate.database.PayMateDB;
import net.paymate.database.PayMateDBDispenser;

public class StatusServer extends Service {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StatusServer.class);

  private StatusServerRaw myServer = null;
  private int port;
  private int bufferSize = StatusPacket.TOTALLEN;
  private String logfileName = null;
  private ApplianceTrackerList apptrk = null;

  public StatusServer(ServiceConfigurator cfg, ApplianceTrackerList apptrk) {
    super("IPStatSrvr", cfg);
    this.apptrk = apptrk;
    initLog();
    up(); // loads the parameters & starts the listening thread
  }

  /* package */ void handlePacket(PayMateDB db, StatusPacket sp) {
    byte [] bytes = sp.toBytes();
    readBytes.add((bytes != null) ? bytes.length : 0);
    println("RCVD: " + Ascii.bracket(bytes) + ": " + sp.Spam());
    if(db != null) {
      apptrk.logUpdate(sp);
    }
  }

  private static final Accumulator readBytes = new Accumulator();
  public String svcTxns() {
    return ""+readBytes.getCount();
  }
  public String svcReads() {
    return printByteStats(readBytes);
  }
  public boolean isUp() {
    return (myServer != null);
  }
  public void down() {
    if(myServer != null) {
      myServer.kill();
      myServer = null;
    }
    if(logFile != null) {
      logFile.AtExit();
      logFile = null;
    }
    markStateChange();
  }
  public void up() {
    if(!isUp()) {
      port = configger.getIntServiceParam(serviceName(), "port", 4040);
      String logfileName = serviceName()+"."+port;
      myServer = new StatusServerRaw(port, bufferSize, this, dbg, logfileName /* used to name the socket */);
    }
    println(serviceName() + " is up!");
    markStateChange();
  }
}

class StatusServerRaw extends UDPServer {

  private StatusServer myServer = null;
  private ErrorLogStream dbg;
  public StatusServerRaw(int port, int bufferSize, StatusServer myServer, ErrorLogStream dbg, String name) {
    super(port, bufferSize, name, true);
    // the socket is already created and listening now !!!
    this.myServer = myServer;
    this.dbg = dbg;
  }

  protected void handlePacket(DatagramPacket p) {
    StatusPacket sp = StatusPacket.fromDatagramPacket(p);
    if(myServer==null) {
      if(dbg != null) {
        dbg.ERROR("handlePacket(): myServer is null!  packet["+sp+"]");
      }
    } else {
      myServer.handlePacket(PayMateDBDispenser.getPayMateDB(), sp);
    }
  }

}
