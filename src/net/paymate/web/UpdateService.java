package net.paymate.web;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/UpdateService.java,v $</p>
 * <p>Description: UpdateService and assisting classes handle updating appliances
 *                 by sending them tars of their files -- ALL of them</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.12 $
 * @todo: the list of UpdateServer's should be in the class that does the creating of them, so that when it shuts down it can take its children with it.
 */

import net.paymate.Main;
import net.paymate.util.*;
import net.paymate.util.compress.tar.*;
import net.paymate.util.compress.*;
import java.net.*;
import java.util.Vector;
import java.io.*;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;
import net.paymate.io.*;
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;
import net.paymate.net.SocketX;
import net.paymate.lang.Fstring;
import net.paymate.io.ByteFifo;

// +++ generalize the stuff in here into a SocketService, SocketServer, etc., package structure for use in other places

// this class just hands out sockets and spawns the UpdateServers
class UpdateSocketServer implements Runnable {

  private ServerSocket myServer = null;
  private Thread myThread = null;
  private PrintFork dbg;
  private boolean keepRunning;
  private String name;
  private int port;

  public final Accumulator readBytes = new Accumulator();
  public final Accumulator writeBytes = new Accumulator();
  public final Counter successes = new Counter();

  public UpdateSocketServer(int port, PrintFork pf, String name) {
    this.dbg = pf;
    this.name = name;
    this.port = port;
  }

  public void start() {
    // start the thread
    if (!running) {
      myThread = new Thread(this, name); // +++ lower priority to one below that of txns?
      myThread.setPriority(Thread.NORM_PRIORITY);
      myThread.setDaemon(true);
      myThread.start();
      // the socket is already created and listening now !!!
    }
  }

  public void stop() {
    keepRunning = false;
    if (myServer != null) {
      SocketX.Close(myServer);
      // +++ myServer = null; ???
    }
  }

  public boolean isRunning() {
    return running;
  }

  Counter socketCounter = new Counter();

  private boolean running = false;
  public synchronized void run() {  // should only ever be one thread in here !
    keepRunning = true;
    running = true;
    while (keepRunning) {
      try {
        if (myServer == null) {
          // create the socketserver
          myServer = new ServerSocket(port);
        }
        dbg.WARNING("Server: Accepting....");
        Socket client = myServer.accept(); //blocks until a connection is made
        dbg.WARNING("Server: Accepted.  Spawning....");
//        ErrorLogStream localdebug = ErrorLogStream.getForName(UpdateServer.class.getName()+socketCounter.incr(), ErrorLogStream.VERBOSE);
        UpdateServer temp = new UpdateServer(client, dbg, this);
        dbg.WARNING("Server: Spawned.");
      } catch (Exception caught) {
        dbg.ERROR(caught.toString());
        dbg.ERROR(caught.fillInStackTrace().toString());
      }
    }
    myThread = null;
    running = false;
  }

}

// this is a single appliance->server updater connection
// the communicate() for this class handles the communication between the client and server
class UpdateServer extends Thread implements TarProgressDisplay {
  private PrintFork pf = null;
  private static final ErrorLogStream staticdbg = ErrorLogStream.getForClass(UpdateServer.class, ErrorLogStream.VERBOSE);

  Socket s;
  InetAddress remote = null;
  private String remoteAddress() {
    if(remote != null) {
      return remote.getHostAddress();
    } else {
      return "unknown";
    }
  }

  private UpdateSocketServer parent = null;
  public UpdateServer(Socket s, PrintFork pf, UpdateSocketServer parent) {
    super("UpdateServer");
    this.parent = parent;
    this.s = s;
    remote = s.getInetAddress();
    this.pf = pf;
    register(this);
    start();
  }

  // start static registry stuff
  /* package */ static Vector list = new Vector();
  private static void register(UpdateServer me) {
    list.add(me);
    staticdbg.VERBOSE("registering one from "+me.remoteAddress());
  }
  private static void unregister(UpdateServer me) {
    list.remove(me);
    staticdbg.VERBOSE("unregistering one from "+me.remoteAddress());
  }
  public static int listSize() {
    return list.size();
  }
  public static void stopAll() {
    // +++ can't
  }
  // end static stuff

  public void run() {
    DataOutputStream out = null;
    DataInputStream in = null;
    try {
      out = new DataOutputStream(s.getOutputStream());
      in = new DataInputStream(s.getInputStream());
      if(communicate(in, out)) {
        parent.successes.incr();
      }
    } catch (IOException ioe) {
      pf.ERROR(ioe.toString());
      pf.ERROR(ioe.fillInStackTrace().toString());
    } finally {
      pf.WARNING("Closing socket.");
      SocketX.Close(s);
      parent.writeBytes.add(writeBytes.value());
      parent.readBytes.add(readBytes.value());
      unregister(this);
    }
  }

  private static final int lenlen = 10; // from the client spec
  private static final String ACK = "ACK";

  private static LocalTimeFormat ltf = null;
  private static final synchronized String centralDate() {
    if(ltf == null) {
      TimeZone tz = TimeZone.getTimeZone("America/Chicago"); // get TZ from configs!
      ltf = LocalTimeFormat.New(tz, LocalTimeFormat.DESCENDINGTIMEFORMAT);
    }
    return ltf.format(UTC.Now());
  }

  private boolean communicate(DataInputStream in, DataOutputStream out) throws IOException {
    // +++ do a log per macid, and use the MAIN one when we don't get a macid (or whatever)
    byte [ ] buffer = new byte[4000]; // arbitrary
// (1)===========Await Byte-Count (10 BYTES) of CMF from Client
    int red = in.read(buffer, 0, lenlen);
    if(red > -1) {
      readBytes.chg(red);
    }
    if (red != lenlen) {
      pf.WARNING("Error reading CMF length from client.");
      return false;
    }
    int counter = StringX.parseInt(new String(buffer, 0, lenlen));
    pf.WARNING("Size of expected CMF: [" + counter + "]");
    // get the crappy linefeed:
    in.read();
    readBytes.incr();
// (2)=================Send an "ACK"
    writeOut(out, ACK);
// (3)=================Receive CMF
    if (counter > buffer.length) {
      buffer = new byte[counter];
    } else {
      // it is long enough already
    }
    // force 23 bytes only:
//    counter = 23 + 7;
//    counter--;
    String lines = null;
    try {
      in.readFully(buffer, 0, counter);
    } catch (Exception ex) {
      pf.ERROR("Exception readFully(): " + ex);
    } finally {
      lines = new String(buffer, 0, counter);
      pf.WARNING("Bytes read: \"" + lines + "\"");
      readBytes.chg(counter);
    }
    // get the macid from the transmission
    String macid = "";
    if(StringX.NonTrivial(lines) && (lines.length() > 11)) {
      lines = StringX.replace(lines, "\n", "");
      macid = StringX.left(lines, 12); // 12 is from the client spec
      pf.WARNING("MACID: " + macid);
    }
    if(macid.length() < 12) {
      pf.ERROR("Invalid macid: '" + macid + "'.");
      return false;
    } else {
      // we need to know when they ruffed and from what IP !
      pf.ERROR("STARTED RUF for macid '"+macid+"' from ip '"+remoteAddress()+"'.");
    }
    // +++ simplify the rest of this even more by ignoring the ACK's, and just sending:
    // "MD5AOKNOK<size><content>" and then awaiting an ACK before closing (not sure if a close throws away buffered but unsent data).
    boolean NEW = false; // rewinding or something in the client prevents this from working
    if(NEW) {
      byte[] tarcontent = generateTar(macid);
      writeOut(out, "MD5AOK"+"NOK"+Fstring.righted(String.valueOf(tarcontent.length),10,'0'));
      writeOut(out, tarcontent);
    } else {
// (4)========================Send MD5AOK|MD5NOK
// (5)========================RECV ACK
      if (!minicom(in, out, "MD5AOK")) {
        return false;
      }
// (6)=====================SEND MATCH|MISMATCH|ERR
// (7)========================RECV ACK
      if (!minicom(in, out, "NOK")) {
        return false;
      }
      byte[] tarcontent = generateTar(macid);
// (8)=======================Send the byte count of impending TAR-FILE
      if (tarcontent.length == 0) {
        /*The client is going to bail anyways if tar-file-size is zero*/
        pf.ERROR("TARFile-size is zero!");
        //but it would bail sooner!! if you send it a size of zero before quiting!!
        return false;
      } else {
        // save it to disk?
        String base = LogFile.getPath();// +++ get from configs
        String tgzfilename = "";
        try {//4debug
          tgzfilename = base+"/"+macid+"."+centralDate()+".tar.gz";
          // write the file as a tgz
          FileOutputStream fos = new FileOutputStream(tgzfilename);
          GZIPOutputStream gzout = new GZIPOutputStream(fos);
          gzout.write(tarcontent);
          gzout.flush();
          gzout.close();
          pf.ERROR("Gzipped tar to file: " + tgzfilename);
        } catch (Exception ex) {
          pf.ERROR("Error gzipping tar to file: " + tgzfilename + "!: " + ex);
        }
      }
      // enforce 10 bytes to make compatible with the current client
      String tarsize = Fstring.righted(String.valueOf(tarcontent.length), 10,'0');
// (9)=======================RECV ACK
      if (!minicom(in, out, tarsize)) {
        return false;
      }
// (10)======================SEND the TARFILE
      pf.WARNING("About to write tarcontent[" + tarcontent.length + "]...");
      writeOut(out, tarcontent);
      pf.WARNING("TAR content sent.");
    }
// (11)======================RECV ACK, and QUIT!
    if (!getAck(in, "tarcontent")) {
      // ignore errors here.  the client is stupid about this.
    }
    // we need to know when they ruffed and from what IP !
    pf.ERROR("COMPLETED RUF for macid '"+macid+"' from ip '"+remoteAddress()+"'.");
    return true;
  }

  private final boolean minicom(InputStream in, OutputStream out, String message) throws IOException {
    writeOut(out, message);
    return getAck(in, message);
  }

  private Counter readBytes = new Counter();

  private static final int acklen = 3;
  private final byte [ ] ackbuf = new byte[acklen];
  private final boolean getAck(InputStream in, String step) throws IOException {
    // for reporting, make the ackbuf readable
    for(int i = ackbuf.length; i-->0;) {
      ackbuf[i] = Ascii.SP;
    }
    // read it in
    int red = in.read(ackbuf, 0, acklen);
    if(red > -1) {
      readBytes.chg(red);
    }
    if(red != acklen) {
      pf.WARNING("Error getting ACK from " + step + "["+red+":'"+new String(ackbuf)+"'].");
      return false;
    }
    pf.WARNING("ACK received as: \"" + Ascii.bracket(ackbuf)+ "\".");
    return true;
  }

  // do not use this function to write out huges chunks of stuff.  For that, just write(bytes).
  private final void writeOut(OutputStream out, String value) throws IOException {
    writeOut(out, value.getBytes());
  }

  private Counter writeBytes = new Counter();
  private final void writeOut(OutputStream out, byte[ ] value) throws IOException {
    out.write(value);
    out.flush();
    pf.WARNING("byte ["+value.length+"] sent.");
    writeBytes.chg(value.length);
  }

  private static final String RUFDIR = "/ruf/"; // +++ parameterize!

  byte [ ] generateTar(String macid) {
//    ByteFifo fifo = new ByteFifo(1500000); // use ByteArrayOutputStream instead
//    OutputStream out = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream(1500000); // get original size from configs (optimize)
    // for now, just get ALL files!
    String homedirectory = RUFDIR + macid;
    String stripoff = StringX.subString(homedirectory, 1); // the tar stuff makes the path non-absolute!
    try {
//      out = fifo.getOutputStream();
      TarArchive archive = new TarArchive(out);
      archive.setVerbose(true); // turns on the progress logging
      archive.setRootPath(stripoff);
      File f = new File(homedirectory+"/");
      TarEntry entry = new TarEntry(f, 0100777, 040777); // this is a protected environment, so make all files & dirs RWX.
      archive.setTarProgressDisplay(this);
      archive.writeEntry(entry, true);
      archive.closeArchive();//writes EOF record and closes streams
      // +++ we need to have a history (in log file is good enough?) of what we sent to the appliance
      pf.WARNING("Tar progress notes:\n"+contents);
      pf.WARNING("Successfully created tar (in ram).");
    } catch (Exception ex) {
      pf.ERROR("Exception generating tar: "+ex);
      IOX.Close(out); // just in case
    } finally {
      return out.toByteArray();//fifo.toByteArray();
    }
  }

  TextList contents = new TextList();
  public void showTarProgressMessage(String msg) {
    contents.add(msg);
  }
}

// this class controls the internal UpdateSocketServer
public class UpdateService extends Service {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(UpdateService.class);

  // +++ create an interface to the registry to allow us to kill them, etc.

  private UpdateSocketServer myServer = null;
  private int port;

  public UpdateService(ServiceConfigurator cfg) {
    super("UpdateService", cfg);
    initLog();
    up(); // loads the parameters & starts the UpdateSocketServer
  }

  // the number of times appliances have tried to ruf
  public String svcTxns() {
    return (myServer == null) ? super.NOCOMMENT : of(myServer.successes.value(), myServer.socketCounter.value());
  }

  // the number of bytes read during each ruf cycle
  public String svcReads() {
    return (myServer == null) ? super.NOCOMMENT : printByteStats(myServer.readBytes);
  }

  // the number of bytes written during each ruf cycle
  public String svcWrites() {
    return (myServer == null) ? super.NOCOMMENT : printByteStats(myServer.writeBytes);
  }

  // the number of appliances currently ruffing
  // (allows us to see if they are going stale and not getting cleaned up;
  //  should be 0 most of the time)
  public String svcCnxns() {
    return ""+UpdateServer.listSize();
  }

  public boolean isUp() {
    return (myServer != null) && myServer.isRunning();
  }

  public void down() {
    if (myServer != null) {
      myServer.stop();
      myServer = null;
    }
    markStateChange();
  }

  public void up() {
    if (!isUp()) {
      println("Starting " + serviceName() + " ..."); // to get the pf created
      port = configger.getIntServiceParam(serviceName(), "port", 8087);
      String logfileName = serviceName() + "." + port;
      myServer = new UpdateSocketServer(port, pf, logfileName);
      myServer.start();
    }
    println(serviceName() + " is up!");
    markStateChange();
  }

  public static final void main(String [] args) {
    String listenKey = "listen";
    int defaultPort = 8087;
    if(args.length < 1) {
      System.out.println(
          "Usage: -Dlogpath:/data/logs UpdateSocketServer "+listenKey+":port\n"+
          "Additional suggested parameters: "+Main.buflogkey+":true "+Main.overlogkey+":false\n"+
          "eg: -Dlogpath:/data/logs UpdateSocketServer "+listenKey+":"+defaultPort+" "+Main.buflogkey+":true "+Main.overlogkey+":false");
    }
//    else
    {
      Main tester=new Main(UpdateSocketServer.class);
      tester.stdStart(args);
//      ErrorLogStream.Console(ErrorLogStream.OFF); // turn the console off and log to the file only
      EasyCursor props = tester.props();
      int port = props.getInt(listenKey, defaultPort);
      String name = "USS";
      LogFile lf = new LogFile("UpdateSocketServerMain", false /* overwrite */);
      UpdateSocketServer server = new UpdateSocketServer(port, lf.getPrintFork(), name);
      server.start();
      ThreadX.sleepForever();
      // use telnet to the above port and type in the following to test [strip everything and use what is in the quotes]:
      // "0000000013"ENTER
      // "0060EF2166DDNAKNAKNAKNAK"ENTER
      // Hit CTRL+C quickly as soon as it starts spewing!
    }
  }
}
