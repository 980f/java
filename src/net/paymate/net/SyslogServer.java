package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SyslogServer.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import java.net.*;
import java.io.*;
import java.util.*;
import net.paymate.io.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.Main;

public class SyslogServer extends UDPServer {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SyslogServer.class);

  private Hashtable list = new Hashtable();
  private String name = null;

  public SyslogServer(int port, int bufferSize, String name, boolean isDaemon) {
    super(port, bufferSize, name, isDaemon);
    this.name = name;
    dbg.ERROR("Starting " + name + " ...");
  }

  private LocalTimeFormat ltf = LocalTimeFormat.Utc();

  protected synchronized void handlePacket(DatagramPacket p) {
    String from = StringX.replace(StringX.TrivialDefault(p.getAddress().toString(), p.getAddress().getCanonicalHostName()), "/", "");
    Object o = list.get(from);
    LogFile lf = null;
    if(o == null) {
      lf = new LogFile(name+from, false /*overwrite*/);
      list.put(from, lf);
    } else {
      lf = (LogFile)o;
    }
    PrintStream ps = lf.getPrintStream();
    // now we have our printstream, so ...
    int port = p.getPort();
    // get our data chunk ...
    byte [ ] bytes = p.getData();
    int len = p.getLength();
    int offset = p.getOffset();
    String data = "";
    if(bytes == null) {
      data = "NULL";
    } else {
      try {
        data = new String(bytes, offset, len);
      } catch (Exception ex) {
        dbg.Caught(ex);
        data = "SyslogServerError! Exception extracting bytes to string [" +
            ( (bytes == null) ? "NULL" : new String(bytes)) + "]";
      }
    }
    // calculate our timestamp
    UTC now = UTC.Now();
    String msg = now.toString() + "-" + data;
    ps.println(msg);
    dbg.VERBOSE("["+lf.longFilename()+"]:" + msg);
  }

  public String info() {
    String ips = "";
    int count = 0;
    for(Enumeration ennum = list.keys(); ennum.hasMoreElements(); ) {
      if(count > 0) {
        ips += ", ";
      }
      ips += (String)ennum.nextElement();
      count++;
    }
    return "Heartbeat.  Has " + count + " ip's listed: "+ips;
  }

  private static Main app;
  private static SyslogServer sls;
  private static final Service crap = null;
  public static final void main(String [ ] args) {
    LogFile.setDefaultPaths();
    app=new Main(SyslogServer.class);
    //in case we don't have a logcontrol file:
    LogSwitch.SetAll(LogSwitch.VERBOSE);
    PrintFork.SetAll(LogSwitch.VERBOSE);
    //now get overrides from file:
    app.stdStart(args); //starts logging etc. merges argv with system.properties and thisclass.properties
    EasyProperties ezp = app.props();
    int port = ezp.getInt("port", 8514);
    int buffersize = ezp.getInt("buffersize", 1024);
    String name = ezp.getString("name", "SyslogDaemon");
    sls = new SyslogServer(port, buffersize, name, true/*isdaemon*/);
    while(true) {
      ThreadX.sleepFor(120.0); // +++ get from configs
      dbg.VERBOSE(sls.info());
    }
  }

}
