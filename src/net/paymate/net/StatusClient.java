package net.paymate.net;

/**
 * Title:        StatusClient
 * Description:  Performs the same functions as the [CVS]/Status-Apps/Status-Client-2.c
 *               A UDP implementation of the status-client.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: StatusClient.java,v 1.33 2003/10/19 20:07:18 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.util.timer.*;
import java.net.*;
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;

public class StatusClient extends Service implements Runnable {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StatusClient.class);

  private int port = 0;       //client will be connecting to this UDP port
  private String hostname = "";
  private StatusPacket sp = null;
  private InetAddress iaddr = null;
  public static final int POLLRATEDEFAULT = 0; // int so that it can be cast up and never needs casting down
  private long pollRateMillis = POLLRATEDEFAULT; // <= 0 means not to.
  private Thread thread = null;

  public boolean cont = true;

  public StatusClient(ServiceConfigurator configger) {
    super("IPStatClient", configger, true);
    up();
  }

  private boolean on = true;

  // Service stuff:
//  public String svcCnxns() {
//    return ""+connections().value();
//  }
  public String svcTxns() {
    return of(sentBytes().getCount(), sendAttempts().value());
  }
  public String svcPend() {
    return doing() ? "1" : "0";
  }
  public String svcAvgTime() {
    return DateX.millisToSecsPlus(sendDuration().getAverage());
  }
  public String svcWrites() {
    return printByteStats(sentBytes());
  }
  public String svcNotes() {
    return status();
  }
  public boolean isUp() {
dbg.ERROR("thread = " + thread + ", thread.isAlive()=" + thread.isAlive() + ", on="+on+", cont="+cont);
    return (thread != null) && thread.isAlive() && on;
  }
  public void down() {
    on = false;
    markStateChange();
  }
  public void up() {
    this.hostname       = configger.getServiceParam    (serviceName(),"statusServer", "64.92.151.10"/*monster*/);
    this.port           = configger.getIntServiceParam (serviceName(),"statusServerPort", 4004);
    this.pollRateMillis = configger.getLongServiceParam(serviceName(),"statusIntervalMs", POLLRATEDEFAULT);
    String macid = configger.getServiceParam(serviceName(),"statusMacid", "unknownSrvr");
    if(!StringX.NonTrivial(macid)) {
      macid = GetMacid.getIt();
    }
    try {
      iaddr = InetAddress.getByName(hostname);
    } catch (Exception e) {
      dbg.Caught("Attempting to resolve hostname:", e);
    }
    String localinet = "";
    try {
      localinet = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      dbg.Caught("Attempting to resolve local hostname:", e);
    }
    sp = new StatusPacket(macid, StringX.TrivialDefault(localinet, "0.0.0.0"));
    if(thread == null) {
      thread = new Thread(this, StatusClient.class.getName());
    }
    if(!thread.isAlive()) {
      thread.setDaemon(true); // or pass a parameter into here +++
      thread.start();
    }
    on = true;
    markStateChange();
  }

  public void run() {
    dbg.ERROR("$Id: StatusClient.java,v 1.33 2003/10/19 20:07:18 mattm Exp $\npollRateMillis="+pollRateMillis+"; running ...");
    try {
      if(pollRateMillis > 0) {
        while(cont) { // +++++++++ add the onexit stuff !!!!
          if(on) {
            sendmessage();
          }
          ThreadX.sleepFor(pollRateMillis); // every minute
        }
      } else {
        dbg.WARNING("StatusApp not running since pollRateMillis is <= 0!");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    }
    dbg.ERROR("$Id: StatusClient.java,v 1.33 2003/10/19 20:07:18 mattm Exp $ ... stopped.");
  }

  // statistics stuff for reporting ...
  private Accumulator sentBytes = new Accumulator();
//  private Counter connections = new Counter();
  private Counter sendAttempts = new Counter();
  private Accumulator sendDuration = new Accumulator();
  private boolean doing = false;
  public Accumulator sentBytes() {
    return sentBytes;
  }
//  public Counter connections() {
//    return connections;
//  }
  public Counter sendAttempts() {
    return sendAttempts;
  }
  public Accumulator sendDuration() {
    return sendDuration;
  }
  public boolean doing() {
    return doing;
  }

  private void sendmessage() {
    DatagramPacket dp = sp.toDatagramPacket(iaddr, port);
    DatagramSocket sockd = null;
    StopWatch sw = new StopWatch(false);
    try {
      sockd = new DatagramSocket();
//      connections.incr();
    } catch (Exception e) {
      dbg.Caught("Attempting to create datagram socket:", e);
    }
    if(sockd != null) {
      try {
        sendAttempts.incr();
        sw.Start();
        doing = true;
        sockd.send(dp);
        doing = false;
        sendDuration.add(sw.Stop());
        sentBytes.add(dp.getLength());
        dbg.WARNING("Sent '" + sp + "' to " + iaddr.getHostAddress() + ":" + port + ".");
      } catch (Exception e) {
        dbg.Caught("Attempting to send StatusPacket datagram:", e);
      } finally {
        doing = false;
        sockd.close();
      }
    }
  }

  public String status() {
    return "" + pollRateMillis + " ms interval";
  }

}
