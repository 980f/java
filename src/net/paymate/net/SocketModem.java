package net.paymate.net;
/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SocketModem.java,v $</p>
 * <p>Description: connect to a modem (actually just a serial port) through a socket </p>
 * <p>Copyright: Copyright (c) 2001-2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.35 $
 * @todo: make config file contain multiple server definitions, push definition down a level, add top level list of active configs.
 * @todo: HANGUP when closing service
 * @todo: localModemInits ignores errors, should do some retries....
 * @todo: if we aren't going to also wait for a line terminator after strings then replace the switch() in sendAndWait with if(StringX.NonTrivial(expected))
 */

import net.paymate.serial.*;
import net.paymate.util.*;
import net.paymate.util.timer.*; // StopWatch
import net.paymate.*;
import net.paymate.serial.*; // Port
import net.paymate.data.*;
import net.paymate.io.IOX;
import net.paymate.io.Streamer;
import java.io.*; // streams
import java.net.*; // sockets
import java.util.*; // EventListener
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;
import net.paymate.io.StreamEventListener;

public class SocketModem implements StreamEventListener {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SocketModem.class, ErrorLogStream.VERBOSE);

  //configuration
  private SocketModemConfig cfg           = null;

  //service side
  private SocketModemByteServer  socket        = null;
  private InputStream       socketIn      = null;
  private OutputStream      socketOut     = null;

  //service to resource connectors
  private Streamer          socket2serial = null;
  private Streamer          serial2socket = null;

  //resource side
  private InputStream       modemIn       = null;
  private OutputStream      modemOut      = null;
  private Port              port          = null;

  //////////
  // state, +_+ enumerate
  private boolean          connected     = false;
  private boolean          stopped       = false;
  private boolean          serialready   = false;
  private Object           waiter        = new Object(); // JUST for waiting on streaming of data!
  private byte[]           EOL; // set to CRL or LF, depending on EOLusesLF

  public SocketModem(SocketModemConfig cfg) {
    try{
      dbg.Enter("constructor");
      this.cfg = cfg;
  dbg.ERROR("cfg.listenPort = " + cfg.listenPort);
      socket = new SocketModemByteServer(cfg.socketModemName+".SocketByteServer", cfg.listenPort, this, dbg, true, cfg.sleeptimeSeconds);
      // Be sure to call start() on this object after you have constructed it (not here)
      EOL = cfg.EOLusesLF ? CRLFBYTES : CRBYTES;
      // startup the port
      // open the modem, if you can
      port = PortProvider.makePort(cfg.portParams.getPortName());
      if(port.openas(cfg.portParams)) {
        modemIn = port.rcv();
        modemOut = port.xmt();
      } else {
        dbg.ERROR("Unable to Open port ["+cfg.portParams.getPortName()+"]!");
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  // +_+ put these in a modem command class
  // requests
  private static final String OnlineEscapeSequence = "+"+"++"; // to prevent hits when searching for our + + + comments
//  private static final String NoEcho               = "ATE0";
//  private static final String OnHook               = "ATH0";
//  private static final String SpeakerOnUntilConnect= "ATM1";
//  private static final String Dial                 = "ATDT,";
  private static final byte[] CRLFBYTES            = { 0x0D, 0x0A,};
  private static final byte[] CRBYTES              = { 0x0D};
  // replies,
  private String OK         = "OK"; // Command successfully executed
  private String CONNECT    = "CONNECT"; // Modem connected to line
  private String RING       = "RING"; // Incoming ring signal detected
  private String NOCARRIER  = "NO CARRIER"; // Carrier detect failed/carrier dropped
  private String ERROR      = "ERROR"; // Command is invalid
  private String NODIALTONE = "NO DIALTONE"; // No tone during interval set with S6
  private String BUSY       = "BUSY"; // Busy signal detected
  private String NOANSWER   = "NO ANSWER"; // Remote end never answered
  private String ENQUEUE    = new String(Ascii.byteAsArray(Ascii.ENQ)); // ENQ

  private boolean send(String toSend){
    try{
      dbg.Enter("send");
      if(toSend!=null){//allow trivials to become CRLF's
        try {
          byte [] bytes = toSend.getBytes();
          dbg.ERROR("Sending: " + Ascii.bracket(bytes));
          modemOut.write(bytes);
          modemOut.write(EOL);
  // --- testing
  modemOut.flush();
          return true;
        } catch (IOException ioe) {
          dbg.Caught(ioe);
          return false;
        }
      }
      return true;//we successfully sent nothing
    } catch(Exception e) {
      dbg.Caught(e);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  private boolean receive(String expectedResult, int forMillis) {
    try{
      dbg.Enter("receive");
  dbg.ERROR("rcv 0");
      StopWatch sw = new StopWatch();
      for(int expecting=0;expecting<expectedResult.length();){
  dbg.ERROR("rcv 1");
        try {
          if(sw.millis() > forMillis) {
            return false;
          }
          int onebyte=modemIn.read();
  dbg.ERROR("rcv onebyte="+Receiver.imageOf(onebyte));
          if(onebyte==Receiver.TimedOut) {
  dbg.ERROR("rcv EOF, continuing");
            continue;// line is idle
          }
          if(onebyte<0){//stream event, not character
  dbg.ERROR("rcv stream event:"+onebyte+", returning false");
            return false;
          }
          if(expectedResult.charAt(expecting)!=onebyte){
  dbg.ERROR("rcv mismatch; expected"+Ascii.bracket(expecting)+"=\""+Ascii.image(StringX.charAt(expectedResult,expecting))+"\"!=onebyte "+Ascii.image(onebyte)+", returning false");
            expecting=0;//none of our expected values contains its starting characters within its body. i.e. no "ABABC"
            continue;//ignore leading trash
          }
          expecting++;
  dbg.ERROR("rcv incr'd expecting:"+expecting);
        } catch(IOException ioex){
          dbg.Caught("while waiting for string got:",ioex);
          return false; // no exceptions are normal
        }
      }
  dbg.ERROR("rcv returning true");
      return true;//all expected chars received.
    } catch(Exception e) {
      dbg.Caught(e);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  /**
   * @returns whether or not the proper response was received
   */
  private boolean sendAndWait(String toSend, String expectedResult, int forMillis) {
    try{
      dbg.Enter("sendAndWait");
      boolean sent = send(toSend);
      dbg.ERROR("sent="+sent);
      if(sent){
  dbg.ERROR("ExpectedResult=["+expectedResult+"], length = "+StringX.lengthOf(expectedResult));
        switch (StringX.lengthOf(expectedResult)) {
          case ObjectX.INVALIDINDEX:
  dbg.ERROR("case ObjectX.INVALIDINDEX:");
          case 0:   //npthing to wait for
  dbg.ERROR("case 0:");
          return true;
  //        case 1:  //one byte is a control char.
  //        return receive((byte)expectedResult.charAt(0),forMillis);
          default:
  dbg.ERROR("default");
          boolean ret = receive(expectedResult, forMillis);
  dbg.ERROR("ret="+ret);
          return ret;
        }
      }
      return false;//else send failed
    } catch(Exception e) {
      dbg.Caught(e);
      return false;//else send failed
    } finally {
      dbg.Exit();
    }
  }

  // modem specific AT commands, general ones such as phone number come from socket.
  // need to specify what the appropriate responses are !!!
  protected boolean localModemInits(){
    try{
      dbg.Enter("localModemInits");
      // quick hack :
      sendAndWait(OnlineEscapeSequence, null, 0); // disregard reply
      TextList initlist = TextList.Empty();
      initlist.wordsOfSentence(cfg.phonenumber);
      int size = initlist.size();
      for(int i = 0; i < size; i++) {
        if(i == size-1) {
          return sendAndWait(initlist.itemAt(i), ENQUEUE, cfg.connectTimeout); // disregard reply
        } else {
          sendAndWait(initlist.itemAt(i), OK, cfg.localTimeout); // disregard reply
        }
      }
      return false;
    } catch(Exception e) {
      dbg.Caught(e);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  protected void onSocketConnect(InputStream in, OutputStream out) {
    try{
      dbg.Enter("onSocketConnect");
      // set the connected bit to true
      connected = true;
      closed = false;
      socketIn = in;
      socketOut = out;
      stopped = false;
      port.reallyFlushInput(300);  // +++ parameterize
      localModemInits(); // +++ check return value ???
      //setup return channel to socket first, so that we don't lose first bytes back from modem
      socket2serial = Streamer.Unbuffered(modemIn, socketOut, this, true); // ignore EOF's from the modem since it sends them after each transmission
      //setup channel to modem last, when there is absolutely nothing else needing to be done.
      serial2socket = Streamer.Unbuffered(socketIn, modemOut, this, false /* true testing */);
      // wait until one of them croaks
      // +++ need to be able to listen to the stream and see if any bytes have been transferred and timeout if they take too long!!!
      while(!stopped) {
        ThreadX.waitOn(waiter, 1111); // sleep in 1-second intervals [+_+ make configurable?]
        dbg.ERROR("Still waiting...");
      }
      if(cfg.justOnce) {
        stop();
        System.exit(0);
      } else {
        closeConnection();
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  /**
   * double cover ourselves.
   * also do this when stop() 'ing.
   */
  public void finalize() {
    try{
      dbg.Enter("finalize");
      closeModem();
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  private void hangup(){
    sendAndWait(OnlineEscapeSequence, OK, cfg.localTimeout);
    sendAndWait("ATH0", OK, cfg.localTimeout);
  }

  private boolean closed = false;
  private void closeModem() {
    try{
      dbg.Enter("closeModem");
      if(!closed) {
        IOX.Close(modemIn);
        modemIn = null;
        IOX.Close(modemOut);
        modemOut = null;
        try {
          port.reallyFlushInput(200); // just to make it work right
          port.close();
        } catch (Exception e) {
          dbg.Caught(e);
        }
        port = null;
        closed = true;
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  /**
   * diconnect the socket end
   */
  private void closeConnection() {
    try{
      dbg.Enter("closeConnection");
      IOX.Close(socketIn);
      socketIn = null;
      IOX.Close(socketOut);
      socketOut = null;
  //the closing of the above is the only means we have to stop the streamers.
  //we shall presume that works and so we can drop our references for cleanliness'es sake
      try {
        socket2serial.StopNoClose();
      } catch (Exception e) {
        // don't care
      }
      socket2serial   = null;
      try {
        serial2socket.StopNoClose();
      } catch (Exception e) {
        // don't care
      }
      serial2socket   = null;
      hangup();
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  public void notify(EventObject event) {
    try{
      dbg.Enter("notify");
      Object streamer = event.getSource();
      if((streamer == socket2serial) || (streamer == serial2socket)) {      // NOTIFY!
        dbg.ERROR("Notifying ... Just got an event notification from the streamer:" + streamer);
        stopped = true;
        ThreadX.notify(waiter);
      } else {
        dbg.ERROR("Not my streamer! " + streamer);
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

  public boolean start() {
    try{
      dbg.Enter("start");
      return socket.Start();
    } catch(Exception e) {
      dbg.Caught(e);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  public boolean stop() {
    try{
      dbg.Enter("stop");
      dbg.ERROR("stop()ing SocketModem");
      closeConnection();
      // NPCAuth calls this before abandoning object.
      // it will then try to create a new one so we have to close the modem as well.
      socket.Stop();
      closeModem();
      return true;
    } catch(Exception e) {
      dbg.Caught(e);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  public boolean isRunning() {
    boolean ret = false;
    SocketModemByteServer localcopy = socket;
    try {
      ret = (localcopy != null) && localcopy.isRunning();
    } catch (Exception ex) {
      // bitch ?
    } finally {
      return ret;
    }
  }

  // for each byte received, send it out the modem
  // take what you get back from the modem and send it out the lineserver [when to stop receiving bytes and send the array ???]
  // maybe sleep a few seconds, then get what's available and send it out?

  // To test this, pass it the parameter of your COM port for your modem.
  // When it comes up, connect to it via telnet: telnet 127.0.0.1 24135
  // When you get a telnet connection, send it "ati3".  You should get back verbose text from the modem.
  // ATM1
  // ATDT3278598
  // all params come from this class's properties file.
  public static final void main(String [] args) {
    try{
      dbg.Enter("main");
      Main app=new Main(SocketModem.class);
      app.stdStart(args);
      //force levels here when you get sick of dicking with logcontrol.properties
      dbg.setLevel(LogSwitch.WARNING);
      dbg.ERROR("Version "+Revision.Rev()+' '+Revision.Buildid());
      SocketModem server;//will be vector.
      EasyCursor all=Main.props();
      boolean justOnce = true;
      String serverlist=all.getString("serve");
      //someday will iterate (over server being a list) { // +++ NO!  See SocketModemPool for that.
        all.push(serverlist);
        try {
          SocketModemConfig cfg = new SocketModemConfig("SocketModem.Server",all );
          justOnce = cfg.justOnce;
          server=new SocketModem(cfg);
        } finally {
          all.pop();
        }
      //}
      if(!server.start()) {
        System.out.println("Unable to start server!  Unable to bind socket?");
      }
      do {
        // just for testing; since we didn't make the Server thread User instead of Daemon
        ThreadX.sleepFor(Ticks.forSeconds(2)); // once a minute, output the threadlist
        System.out.println("Threads:\n"+ThreadX.fullThreadDump().toString());
      } while(!(justOnce && !server.isRunning()));
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }
}

/////////////////

class SocketModemByteServer extends ByteServer {
  private ErrorLogStream dbg = null;
  public SocketModemByteServer(String name, int listenPort, SocketModem sm, ErrorLogStream dbg, boolean isDaemon, double sleeptimeSeconds) {
    super(name, listenPort, isDaemon, sleeptimeSeconds);
dbg.ERROR("listenPort = " + listenPort);
    this.sm = sm;
    this.dbg = dbg;
  }
  SocketModem sm = null;
  public void onAccept(Socket so) {
    try{
      dbg.Enter("onAccept");
      InputStream is = null;
      OutputStream os = null;
      try {
        is = so.getInputStream();
      } catch (Exception e) {
        dbg.Caught(e);
      }
      try {
        os = so.getOutputStream();
      } catch (Exception e) {
        dbg.Caught(e);
      }
      sm.onSocketConnect(is, os);
      dbg.ERROR("Leaving SocketModemByteServer.onAccept()");
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }

}

//$Id: SocketModem.java,v 1.35 2003/07/27 19:36:56 mattm Exp $
