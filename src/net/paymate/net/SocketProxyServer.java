package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SocketProxyServer.java,v $</p>
 * <p>Description: Accepts sockets on source and then makes connections to destination and basically proxies them.</p>
 * <p>             However, this is more of a forward than a proxy.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.14 $
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;
import net.paymate.Main; // for testing
import net.paymate.util.Counter;
import net.paymate.util.EasyCursor;
import net.paymate.util.ErrorLogStream;
import net.paymate.io.Streamer;
import net.paymate.io.StreamEventListener;
import net.paymate.util.TextList;
import net.paymate.lang.ThreadX;
import net.paymate.net.SocketX;
import net.paymate.lang.MathX;
import net.paymate.util.UTC;
import net.paymate.util.Waiter;
import net.paymate.io.IOX;
import net.paymate.util.DateX;

public class SocketProxyServer extends ByteServer {

  IPSpec destination;
  Thread reportRunner;
  private long xfrTimeoutMillis;

  // configuration needed: inbound socket, outbound ip+socket
  public SocketProxyServer(String name, boolean isDaemon, int port, IPSpec destination, long reportIntervalMillis, long xfrTimeoutMillis) {
    super(name, port, isDaemon);
    this.destination = destination;
    this.xfrTimeoutMillis = xfrTimeoutMillis;
    agentList = new SocketProxyAgentList(name, reportIntervalMillis);
    dbg.ERROR("Created SocketProxyServer \""+name+"\" "+(isDaemon ? "" : "NOT ")+
              "as a daemon from "+port+" to "+destination+" with a report interval of "+
              reportIntervalMillis+" ms and a timeout interval of "+xfrTimeoutMillis+".");
  }

  // register the ProxyAgents
  private static SocketProxyAgentList agentList;

  public void onAccept(Socket attached) {
    // when you get a connection, roll it off to another thread,
    SocketProxyAgent pa = new SocketProxyAgent(agentList, attached, destination, xfrTimeoutMillis);
    // then go back to accepting
  }

  public static final void main(String [] args) {
    String fromKey = "from";
    String toKey = "to";
    String reportIntervalKey = "reportInterval";
    String timeoutIntervalKey = "timeoutInterval";
    int defaultInPort = 8443;
    int defaultReportIntervalSecs = 60;
    int defaultTimeoutIntervalSecs = 180; // 3 minutes
    String defaultTo = "192.168.1.50:8443";
    if(args.length < 1) {
      System.out.println(
          "Usage: -Dlogpath:/data/logs SocketProxyServer "+fromKey+":port "+toKey+":ip:port "+reportIntervalKey+":seconds "+timeoutIntervalKey+":seconds\n"+
          "Additional suggested parameters: "+Main.buflogkey+":true "+Main.overlogkey+":false\n"+
          "eg: -Dlogpath:/data/logs SocketProxyServer "+fromKey+":"+defaultInPort+" "+toKey+":"+defaultTo+" "+reportIntervalKey+":"+defaultReportIntervalSecs+" "+timeoutIntervalKey+":"+defaultTimeoutIntervalSecs+" "+Main.buflogkey+":true "+Main.overlogkey+":false");
    } else {
      Main tester=new Main(SocketProxyServer.class);
      tester.stdStart(args);
//      ErrorLogStream.Console(ErrorLogStream.OFF); // turn the console off and log to the file only
      EasyCursor props = tester.props();
      int port                  = props.getInt(fromKey, defaultInPort);
      long reportIntervalMillis = 1000L * props.getInt(reportIntervalKey, defaultReportIntervalSecs);
      long timeoutMillis        = 1000L * props.getInt(timeoutIntervalKey, defaultTimeoutIntervalSecs);
      IPSpec outip              = IPSpec.New(props.getString(toKey, defaultTo));
      String name = "SPS";
      boolean isDaemon = true;
      SocketProxyServer server  =
          new SocketProxyServer(name, isDaemon, port, outip, reportIntervalMillis, timeoutMillis);
      server.Start();
      ThreadX.sleepForever();
    }
  }
}

class SocketProxyAgentList implements Runnable {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SocketProxyAgentList.class);

  String name;
  Thread thread;
  boolean keeprunning = true;
  long intervalMillis;
  private Object waiter = new Object();

  public SocketProxyAgentList(String name, long intervalMillis) {
    this.name = "SocketProxyAgentList."+name;
    this.intervalMillis = intervalMillis;
    Thread thread = new Thread(this, name);
    thread.start();
  }

  public void kill() {
    keeprunning = false;
    ThreadX.notify(waiter);
  }

  public void run() {
    while(keeprunning) {
      report();
      ThreadX.waitOn(waiter,intervalMillis,true,dbg);
    }
  }

  private Vector pool = new Vector();

  public void register(SocketProxyAgent agent) {
    pool.add(agent);
    report(agent, "ADDED");
  }
  public void unregister(SocketProxyAgent agent) {
    pool.remove(agent);
    report(agent, "REMOVED");
  }

  public void report() {
    dbg.WARNING(name + " LISTING agents ...");
    for(Enumeration enum = pool.elements(); enum.hasMoreElements(); ) {
      SocketProxyAgent agent = (SocketProxyAgent)enum.nextElement();
      report(agent, "LIST", agent.lifetime());
    }
    dbg.WARNING(name + " agents LISTED.");
    TextList tl = ThreadX.ThreadDump(null);
    dbg.WARNING(name + " LISTING threads ["+tl.size()+"] ...\n"+tl);
    dbg.WARNING(name + " threads LISTED.");
  }

  public final void report(SocketProxyAgent agent, String operation) {
    report(agent, operation, MathX.INVALIDLONG);
  }
  public final void report(SocketProxyAgent agent, String operation, long lifetime) {
    dbg.WARNING(name + " " + operation + " " + agent.name() + ((MathX.INVALIDLONG==lifetime) ? "" : " "+DateX.millisToSecsPlus(lifetime)));
  }
}

class SocketProxyAgent implements Runnable, StreamEventListener {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SocketProxyAgent.class);

  Thread thread;
  IPSpec destination;
  Socket inbound;
  SocketProxyAgentList list;
  String name;
  UTC createdAt;
  long xfrTimeoutMillis;
  private static final Counter agentCounter = new Counter();

  public SocketProxyAgent(SocketProxyAgentList list, Socket inbound, IPSpec destination, long xfrTimeoutMillis) {
    this.destination = destination;
    this.inbound = inbound;
    this.list = list;
    this.xfrTimeoutMillis = xfrTimeoutMillis;
    this.createdAt = UTC.Now();
    this.name = name(inbound, destination, createdAt);
    thread = new Thread(this, "SocketProxyAgent"+agentCounter.incr());
    thread.setDaemon(true);
    thread.start();
  }

  private static final String UNKNOWN = "UNKNOWN";
  private static final String inboundAddress(Socket inbound) {
    try {
      return inbound.getInetAddress().getHostAddress();
    } catch (Exception ex) {
      dbg.Caught(ex);
      return UNKNOWN;
    }
  }
  private static final String name(Socket inbound, IPSpec destination, UTC createdAt) {
    return inboundAddress(inbound) + " -> " + destination + " [" + createdAt +"]";
  }

  public String name() {
    return name;
  }
//  public UTC created() {
//    return createdAt;
//  }
  public long lifetime() {
    return UTC.Now().skew(createdAt);
  }

  // when one side closes (how to tell?), close the other side.
  public void notify(EventObject event) {
    try{
      dbg.Enter("notify");
      Object streamer = event.getSource();
      if((streamer == streamer1) || (streamer == streamer2)) {      // NOTIFY!
        dbg.WARNING("Notifying ... Just got an event notification from the streamer:" + streamer);
        ThreadX.notify(thread);
      } else {
        dbg.ERROR("Not my streamer! " + streamer);
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  private Streamer streamer1;
  private Streamer streamer2;

  private Waiter waiter = new Waiter();

  public void run() {
    // report that we started it
    list.register(this);
    Socket outbound = null;
    InputStream inin = null;
    OutputStream inout = null;
    InputStream outin = null;
    OutputStream outout = null;
    try {
      // open a socket to an outboundport
      try {
        outbound = new Socket(InetAddress.getByName(destination.address), destination.port);
      } catch (Exception ex) {
        dbg.Caught(ex);
      } if (outbound == null) {
        dbg.ERROR("Unable to create outbound socket to " + destination + ". Giving up.");
      } else {
        boolean good = false;
        long stream1count = 0L;
        long stream2count = 0L;
        try {
          inin = inbound.getInputStream();
          inout = inbound.getOutputStream();
          outin = outbound.getInputStream();
          outout = outbound.getOutputStream();
          // be ready to receive an interrupt
          waiter.prepare(xfrTimeoutMillis, false, dbg);
          // hookup streams to allow the sockets to talk
          streamer1 = Streamer.Unbuffered(inin, outout, this, true);
          streamer2 = Streamer.Unbuffered(outin, inout, this, true);
          good = true;
        } catch (Exception ex) {
          dbg.Caught(ex);
          dbg.ERROR("Couldn't setup socket streams.");
        }
        while(good) {
          stream1count = streamer1.count;
          stream2count = streamer2.count;
          int waitstate = waiter.Start(xfrTimeoutMillis);
          if(waitstate == Waiter.Timedout) {
            // if it timedout check the last time a byte was transferred
            if((streamer1.count == stream1count) && (streamer2.count == stream2count)) {
              dbg.WARNING("Closing socket due to transfer timout:"+name());
              good = false;
            } else {
              // otherwise, loop and update our numbers
            }
          } else { // if we got here from an interrupt/notify/except, kill it
            // use the state string we got before in case the state changed since then
            dbg.WARNING("Closing socket due to state ["+Waiter.stateString(waitstate)+"]:"+name());
            good = false;
          }
        }
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      // close all of the streams
      IOX.Close(inin);
      IOX.Close(inout);
      IOX.Close(outin);
      IOX.Close(outout);
      streamer1.StopAndClose();
      streamer2.StopAndClose();
      SocketX.Close(outbound);
      SocketX.Close(inbound);
      list.unregister(this);
    }
  }
}
