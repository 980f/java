package net.paymate.net;

/* $Source: /cvs/src/net/paymate/net/CertifiedSocket.java,v $ */

import net.paymate.connection.Constants;//---misplaced class
import net.paymate.Main;
import net.paymate.util.*;
import net.paymate.util.timer.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;
import javax.net.*;
import java.security.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.cert.*;
import com.sun.net.ssl.*;

public class CertifiedSocket {
  protected static final ErrorLogStream dbg=new ErrorLogStream(CertifiedSocket.class.getName(), ErrorLogStream.ERROR);

  protected Socket socket = null;//may or may not be SSL. if we care we can 'instanceof'
  protected OutputStream os;
  protected InputStream is;

  public int contextCreationSecs = -1;
  boolean retrying = false;//--- never set to true...

  public boolean ok(){
    return socket!=null && is!=null && os!=null;
  }
  /////////////////////////
  //only one server may be referenced per appliance
  public static Constants cfg;//needed by connectionClient HTTP formatter
  protected static String [] suites = null;

  protected static SSLContext context = null;
  protected static SocketFactory factory = null;

  public boolean isLocal(boolean ifLan) {
    byte [] octet= socket.getInetAddress().getAddress() ;
    return octet[0]==127 || (ifLan && //disallow gateways:
    octet[0]==192&& octet[1]==168 && octet[2]== 1 && (octet[3]>1&&octet[3]<254));
  }

  public static final String socketSpam(Socket ret){
    try {
      return  "Socket [Remote=" + ret.getInetAddress().getHostAddress() + ":" + ret.getPort() +
      ", local=" + ret.getLocalAddress().getHostAddress() + ":" + ret.getLocalPort() +
      "] R/S=" + ret.getReceiveBufferSize() + "/" + ret.getSendBufferSize() +
      " options: keepalive=" + ret.getKeepAlive() +
      ", SoLinger=" + ret.getSoLinger() +
      ", SoTimeout=" + ret.getSoTimeout() +
      ", TcpNoDelay=" + ret.getTcpNoDelay();
    }
    catch(NullPointerException npe){
      return "Socket [nullobject]";
    }
    catch(SocketException sex){
      return "Socket [exceptional:"+sex+"]";
    }
  }
  ////////////////////////
  private Socket makeSecureSocket(){
    SSLSocket socket = null;
    try {
      socket = (SSLSocket)factory.createSocket(cfg.ipSpec.address, cfg.ipSpec.port);
      setSecurity(socket);
    }
    catch(ClassCastException cce){
      dbg.Caught("Could not create SSLSocket from SocketFactory!",cce);
    }
    catch(NullPointerException npe){
      dbg.Caught("Could not createSocket from SocketFactory!",npe);
    }
    catch (Exception e) {
      dbg.ERROR("createSocket Error:"+e.toString());
      socket =null;
    } finally {
      return socket;
    }
  }

  private Socket makePlainSocket() {//---
    Socket ret = null;
    dbg.Enter("makePlainSocket");
    try {
      contextCreationSecs = 0;
      ret = new Socket(cfg.ipSpec.address, cfg.ipSpec.port);
      dbg.WARNING(socketSpam(ret));
    }
    catch(NoRouteToHostException ce){//network is down
      dbg.ERROR(ce.toString());
    }
    catch(ConnectException ce) {//sevice is down
      dbg.ERROR(ce.toString());
    }
    catch(java.net.SocketException ce){//when firewall stopped us going out. "network subsystem has failed"
      dbg.ERROR(ce.toString());
    }
    catch(Exception caught) {
      dbg.Caught(caught);
    } finally {
      dbg.Exit();
      return ret;
    }
  }

  private boolean assertFactory(boolean fresh){
    if(fresh) {
      factory = null;
      context = null;
    }
    if(context == null) {
      dbg.WARNING("Making Context For:"+cfg.toString());
      StopWatch sw = new StopWatch();
      context = Trustee.makeContext(cfg.keyStore);
      sw.Stop();
      contextCreationSecs = (int)Math.round(sw.seconds());
      dbg.VERBOSE("Context creation took " + contextCreationSecs + " seconds.");
    }
    if(context == null) {
      dbg.ERROR("Could not create context!");
      return false;
    }
    if(factory == null) {
      StopWatch sw = new StopWatch();
      SSLSocketFactory ssfc = context.getSocketFactory();
      sw.Stop();
      dbg.VERBOSE("getSocketFactory took " + Math.round(sw.seconds()) + " seconds.");
      if(ssfc == null) {
        dbg.ERROR("Could not get SSLSocketFactory from context!");
        return false;
      }
      factory = ssfc.getDefault();
    }
    if(factory == null) {
      dbg.ERROR("Could not get default SocketFactory from SSLSocketFactory!");
      return false;
    }
    return true; //we got a factory
  }

  private void setSecurity(SSLSocket socket){
    socket.setEnableSessionCreation(true); //allow for session ID for fast reconnect
    socket.setNeedClientAuth(true);   //certificate on ????
    socket.setUseClientMode (true);   // Do the SSL handshaking
    socket.setEnabledCipherSuites(suites);
  }

  protected Socket open(boolean fresh) {
    dbg.Enter("open");
    try {
      if(socket!=null){
        close();
      }
      if(cfg.doSecured) {
        if(assertFactory(fresh)){//we have a facotry so:
          socket= makeSecureSocket();
        }
      } else {
        socket = makePlainSocket ();
      }
      if(socket != null) {//cache streams
        os = socket.getOutputStream();
        is = socket.getInputStream();
      } else {
        dbg.ERROR("No Socket to [" + cfg.ipSpec.address + ":" + cfg.ipSpec.port + "]");
      }
    }
    //the possible net exceptions are broken out so that as we discover why each
    //occurs we are ready to come up with a dedicated response
    catch (java.net.BindException caught){
      //Address in use: JVM_Bind
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.net.ConnectException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.net.NoRouteToHostException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.net.ProtocolException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.net.SocketException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.net.UnknownHostException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.net.UnknownServiceException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    catch (java.io.IOException caught){
      dbg.Caught(caught);      //+++ actually do something
    }
    finally {
      if(socket == null) {
        dbg.ERROR("Socket failed to initialize/create.");
      }
      dbg.Exit();
      return socket;
    }
  }

  public Socket open(int timeoutMs) {
    return open(true,timeoutMs);
  }

  protected Socket open(boolean fresh, int timeoutMs) {
    Socket s = open(fresh);
    if(s != null) {
      try {
        s.setSoTimeout(timeoutMs);
      } catch (java.net.SocketException se) {
        dbg.ERROR("open: SocketException setting timeout - " + se);
      }
    }
    return s;
  }

  public Socket reopen(int timeoutMs) {
    return open(false, timeoutMs);
  }

  Tracer tracer=new Tracer("CS.CLOSE");
  public boolean close() {
    dbg.Enter("close");
    tracer.mark("closing");
    try {
      if(os != null) {
        os.close();
        os = null;
      }
      tracer.mark("is.close");
      if(is != null) {
        is.close();
        is = null;
      }
      tracer.mark("secure.close");
      if(socket != null) {
        socket.close();
        socket = null;
      }
      return true;
    }
    catch (javax.net.ssl.SSLException ex){//not published as being thrown ...
      String sslError=ex.getLocalizedMessage();
      if(sslError.indexOf("untrusted server cert chain")>=0){//+_+ vulneralbe to change in ssl library, need to wrap that.
        //known causes:<ol>
        //<li>incorrect clock setting
        //</ol>
        tracer.Caught(ex);
      }
      tracer.Caught(ex);
      return false;
    }
    catch (java.io.IOException ex){
      tracer.Caught(ex);
      return false;
    }
    finally {
      dbg.Exit();
    }
  }

  /**
  * can expand into a crlf eater later.
  */
  private boolean swallow(int onebyte){
    return onebyte==13 || onebyte==0;
  }

  public byte [] readAll() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    long howMany = Streamer.swapStreams(socket.getInputStream(), baos);
    if(howMany == -1) {
      // +++ note that nothing was received at all
      dbg.WARNING("readAll(): nothing received!");
    }
    return baos.toByteArray();
  }

  public String readAllNoNulls() throws IOException {
    byte [] bytes = readAll();
    // filter out nulls
    StringBuffer chars = new StringBuffer(bytes.length);
    for(int i = 0; i < bytes.length; i++) {
      if(bytes[i] != 0) {
        chars.append((char)bytes[i]);
      }
    }
    return chars.toString();
  }

  public String read() {
    String ret = null;
    StopWatch readtime=new StopWatch();
    try {
      dbg.Enter("read");
      ret = readAllNoNulls();
    } catch(InterruptedIOException arf){
      dbg.ERROR("@timeout:"+(ret==null?ret:ret.substring(0,Math.min(30, ret.length())))+"...");
    } catch (java.net.SocketException caught){
      dbg.ERROR("SocketException"+caught);
    } catch (Exception t) {
      dbg.Caught(t);
    } finally {
      dbg.VERBOSE("Read took:"+readtime.millis());
      dbg.VERBOSE("and returned:"+ret);
      dbg.Exit();
      return ret;
    }
  }
  /**
  * we are depending upon autoflush being set!!
  */
  public boolean write(String toWrite) {
    retrying = false;
    String name = "unknown";
    dbg.Enter("write");
    try {
      name = socket.getInetAddress().getHostName() + ":" + socket.getPort();
      PrintWriter toServer = new PrintWriter(new OutputStreamWriter(os), true);
      try {
        dbg.VERBOSE("sending to " + name + " ... " + toWrite);
        toServer.println(toWrite);
        return true;
      } catch (Exception t) {
        // +++ handle this situation
        // if the socket got disconnected, reconnect and try again (how many times?)
        dbg.Caught("error sending to " + name + " ... " + toWrite, t);
        return false;
      }
    } catch (Exception t) {
      // +++ handle this situation
      dbg.Caught("error getting writers for socket " + name, t);
      return false;
    } finally {
      retrying = false;
      dbg.Exit();
    }
  }

  public boolean OnFinalize(){ // +++ --- when does this, if ever, get called?
    return close();
  }

  /**
  * @ return new socket, already opened- no choice about that.
  */
  public static CertifiedSocket New(int toMillis){
    return new CertifiedSocket(toMillis);
  }

  private CertifiedSocket(int toMillis){
    open(true,toMillis);
  }

  public static final boolean Initialize(Constants clump, String [] sweets){
    cfg=clump;
    suites = sweets;
    return true;//open(true) != null;
  }

  /////////////////////////
  // tester

  public static String Usage() {
    // +++ eventually add keystore parameter
    return "Usage: java net.paymate.util.Tester net.paymate.net.CertifiedSocket host:portnumber";
  }

  public static void Test(String[] argv) {
    Main app=new Main(CertifiedSocket.class);
    app.stdStart(argv);
  }

}
//$Id: CertifiedSocket.java,v 1.57 2001/10/17 22:07:23 andyh Exp $
