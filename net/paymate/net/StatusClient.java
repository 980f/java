package net.paymate.net;

/**
 * Title:        StatusClient
 * Description:  Performs the same functions as the [CVS]/Status-Apps/Status-Client-2.c
 *               A UDP implementation of the status-client.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: StatusClient.java,v 1.10 2001/11/03 13:16:42 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.util.timer.*;
import java.net.*;

public class StatusClient extends Thread {
  private static final ErrorLogStream dbg = new ErrorLogStream(StatusClient.class.getName());

  private static final int PORT = 4004;       //client will be connecting to this UDP port
  private String hostname = "";
  private StatusPacket sp = null;
  private InetAddress iaddr = null;
  public static final int POLLRATEDEFAULT = 0; // int so that it can be cast up and never needs casting down
  private long pollRateMillis = POLLRATEDEFAULT; // <= 0 means not to.

  public boolean cont = true;

  public StatusClient(String hostname, String ipaddress, long pollRateMillis) {
    this(hostname, ipaddress, null, pollRateMillis);
  }

  public StatusClient(String hostname, String ipaddress, String macid, long pollRateMillis) {
    super(StatusClient.class.getName());
    this.hostname = hostname;
    this.pollRateMillis = pollRateMillis;
    if(!Safe.NonTrivial(macid)) {
      macid = GetMacid.getIt();
    }
    this.sp = new StatusPacket(macid, ipaddress);
    try {
      iaddr = InetAddress.getByName(hostname);
    } catch (Exception e) {
      dbg.Caught("Attempting to resolve hostname:", e);
    }
    this.start();
  }

  public static final StatusClient NewDefault(String ipaddress) {
    return new StatusClient("64.92.151.10", ipaddress, POLLRATEDEFAULT);
  }

  private boolean on = true;

  public void shutdown() {
    on = false;
    //this.interrupt();
  }

  public void bringup() {
    on = true;
    // start();
  }

  public boolean isUp() {
    return isAlive() && on;
  }

  public void run() {
    dbg.ERROR("$Id: StatusClient.java,v 1.10 2001/11/03 13:16:42 mattm Exp $ running ...");
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
    dbg.ERROR("$Id: StatusClient.java,v 1.10 2001/11/03 13:16:42 mattm Exp $ ... stopped.");
  }

  // statistics stuff for reporting ...
  private Accumulator sentBytes = new Accumulator();
  private Counter connections = new Counter();
  private Counter sendAttempts = new Counter();
  private Accumulator sendDuration = new Accumulator();
  private boolean doing = false;
  public Accumulator sentBytes() {
    return sentBytes;
  }
  public Counter connections() {
    return connections;
  }
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
    byte [] bytes = sp.toBytes();
    DatagramPacket dp = new DatagramPacket(bytes, bytes.length, iaddr, PORT);
    DatagramSocket sockd = null;
    StopWatch sw = new StopWatch(false);
    try {
      sockd = new DatagramSocket();
      connections.incr();
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
        sentBytes.add(bytes.length);
        dbg.WARNING("Sent '" + Safe.replace(new String(bytes),"\0","^") + "'"); // allowing the \0 characters to print causes my text editor to go into hex mode!
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

  public static final void main(String [] args) {
    StatusClient sc = StatusClient.NewDefault("192.168.1.253");
    ThreadX.sleepFor(Ticks.forHours(1));
  }
}


class StatusPacket {
//  char len[3];           //Packet-length must be less than 999 bytes!
//  char ethaddress[13];   //My MAC address, NOT including the ":"s.
//  char IPaddress[14];    //My IP address, including the dots. (MMM: note that this is too small for "123.567.901.345".  needs to be 16 with \0 as the last one
// set this in toString()
//  char date[11]; // is this hex?
//0410060EF218533192.168.1.1040992580379
//^           ^           ^            ^

  private String ethernetAddress = "";
  private String ipAddress = "";

  public StatusPacket(String ethernetAddress, String ipAddress) {
    this.ethernetAddress = ethernetAddress;
    this.ipAddress = ipAddress;
  }

  private static final String ENDSTRING = "\0";
  private static final int LENLEN = 3;
  private static final int ETHADDRESSLEN = 12; // + '\0'
  private static final int IPADDRESSLEN = 13; // + '\0'
  private static final int DATELEN = 10; // + '\0'

  private Fstring ethaddress = new Fstring(ETHADDRESSLEN);
  private Fstring IPaddress = new Fstring(IPADDRESSLEN);
  private Fstring date  = new Fstring(DATELEN, '0');
  private Fstring len = new Fstring(LENLEN, '0');

  public String toString() {
    ethaddress.setto(ethernetAddress);
    IPaddress.setto(ipAddress);
    date.righted(""+(System.currentTimeMillis()/1000));
    String toSend = ethaddress + ENDSTRING + IPaddress + ENDSTRING + date + ENDSTRING;
    len.righted(""+(3 + toSend.length()));
    return len.toString() + toSend;
  }

  public byte [] toBytes() {
    return this.toString().getBytes();
  }
}
