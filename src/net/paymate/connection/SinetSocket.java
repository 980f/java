package net.paymate.connection;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/connection/SinetSocket.java,v $
 * Description:  a socket that wraps a real one and retains the info on the host it connects to
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.15 $
 */

import net.paymate.io.Streamer;
import net.paymate.lang.StringX;
import net.paymate.net.HTTPMessage;
import net.paymate.net.HTTPMethod;
import net.paymate.net.InternetMediaType;
import net.paymate.net.SocketX;
import net.paymate.net.URIQuery;
import net.paymate.util.Ascii;
import net.paymate.util.EasyCursor;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.timer.StopWatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SinetSocket {
  private static ErrorLogStream dbg;
  private static ErrorLogStream dbd;

  private synchronized static void ensureDebuggers(){
    if(dbg==null){
      dbg = ErrorLogStream.getForClass(SinetSocket.class);
    }
    if(dbd==null){
      dbd = ErrorLogStream.getExtension(SinetSocket.class, "data");
    }
  }
  private SinetHost host; //host that created this socket

  private Socket socket;//may or may not be SSL. if we care we can 'instanceof'
  private OutputStream os;
  private InputStream is;

  /**
   * attach SinetSocket to opened system socket @param socket
   */
  private SinetSocket attach(Socket socket){
    this.socket=socket;
    try {
      if(socket!=null){
        os=socket.getOutputStream();
        is=socket.getInputStream();
      } else {
        os=null;//superfluous, but handy for breakpoints.
        is=null;
      }
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
    return this;
  }

//  private SinetSocket attach(SinetHost hostinfo,Socket socket){
//    return attach(hostinfo).attach(socket);
//  }


  public boolean open(SinetHost host,int readtimeout){
    dbg.VERBOSE("makesocket, socket read timeout will be:"+readtimeout);
    close();
    this.host=host;

    attach(host.open(readtimeout));//make a new one.
    return ok();
  }

  /**
 * @return whether object is likely to be useable for moving data
 */
  public boolean ok(){
    return socket!=null && is!=null && os!=null ;//jre1.4ism&& socket.isConnected();//isConnected added to avert NPE in getting name for debug in write()
  }
 /**
  * @return whether object is likely to be useable for moving data
  */
  public static boolean ok(SinetSocket cs){
    return cs!=null && cs.ok();
  }

  public SinetHost HostInfo(){
    return host;
  }

  private byte [] readAll() throws IOException {
    dbg.Enter("readAll");
    try {
      dbg.WARNING(socket.toString());
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Streamer streamer = Streamer.Buffered(socket.getInputStream(), baos);

      long howMany = streamer.count;

      dbd.WARNING(howMany+" bytes");
      if(howMany < 0) {
        // +++ note that nothing was received at all
        dbg.WARNING("nothing received!");
      }
      return baos.toByteArray();
    }
    finally {
      dbg.Exit();
    }
  }

  public String read() {
    String ret = null;
    StopWatch readtime=new StopWatch();
    dbg.Enter("read");
    try {
      byte [] bytes = readAll();
      // filter out nulls
      StringBuffer chars = new StringBuffer(bytes.length);
      for(int i = 0; i < bytes.length; i++) {
        if(bytes[i] != 0) {
          chars.append((char)bytes[i]);
        }
      }
      dbd.VERBOSE("null filtered out to "+chars.length()+" bytes");
      return ret=String.valueOf(chars);
    } catch(InterruptedIOException arf){
      dbg.ERROR("@timeout:"+(ret==null?ret:ret.substring(0,Math.min(30, ret.length())))+"...");
      return "timeout";
    } catch (java.net.SocketException caught){
      dbg.ERROR("SocketException"+caught);
      return "Socket Exception";
    } catch (Exception t) {
      dbg.Caught(t);
      return "Unexpected Exception";
    } finally {
      dbg.VERBOSE("Read took:"+readtime.Stop());
      dbd.VERBOSE("Received:"+ret);
      dbg.Exit();
    }
  }



  /**
  * we are depending upon autoflush being set!!
  * which we have confirmed in the first ssl library used,
  * and is pretty much required for any sane TCP socket implementation.
  */
  public boolean write(String toWrite) {
    String name = "unknown";
    dbg.Enter("write");
    try {
      if(ok()){
        name = socket.getInetAddress().getHostName() + ":" + socket.getPort();
        PrintWriter toServer = new PrintWriter(new OutputStreamWriter(os), true);
        try {
          dbd.VERBOSE("sending to " + name + Ascii.bracket(toWrite.getBytes()));
          toServer.println(toWrite);
          return true;
        } catch (Exception t) {
          // +++ handle this situation
          // if the socket got disconnected, reconnect and try again (how many times?)
          dbg.Caught("error sending to " + name + " ... " + toWrite, t);
          return false;
        }
      } else {
        dbg.ERROR("write: socket is "+socket+" streams are:"+is+os);
        return false;
      }
    } catch (Exception t) {
      // +++ handle this situation
      dbg.Caught("error getting writer for socket " + name, t);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  public ActionReply parseReply(String httpResponse){
    if(StringX.NonTrivial(httpResponse)) {//normally precheckd by caller.
      // this message has some crap in it.  get rid of it
      int tag = httpResponse.indexOf(InternetMediaType.TEXT_HTML);
      if(tag >=0) {
        tag += InternetMediaType.TEXT_HTML.length();
        httpResponse = httpResponse.substring(tag);
      }
      // unpackage the reply
      EasyCursor reply = new EasyCursor(httpResponse);
      dbd.VERBOSE("Reply's parsed properties:  " + reply);
      ActionReply ar = ActionReply.fromProperties(reply);
      return ar;
    } else {
      return null;
    }
  }

    ///////////////////////////////////////////////////////////////////////////////
  //@POOL@ httpmessages.

  private static URIQuery  uri;//protocol header never changes.
  /**
   * make header if not done so already.
   */
  private static URIQuery getUri(){
    if(uri==null){
      ConnSource toSource = new ConnSource(ConnSource.terminalObjects);
      EasyCursor props = new EasyCursor();
      props.setString(ConnSource.class.getName(), toSource.Image());
      uri =  new URIQuery(props);
    }
    return uri;
  }

  /**
   * convert request to http postable text.
   */
  private String httpMessage(ActionRequest request){
    String requestString = request.toEasyCursorString();
    SinetHost hostinfo=this.HostInfo();
    HTTPMessage message  = new HTTPMessage(new HTTPMethod(HTTPMethod.POST),
    hostinfo.ipSpec, hostinfo.UrlPath,  getUri(), requestString );
    return String.valueOf(message);
  }

  public boolean sendRequest(ActionRequest request){
    String requestMessage = httpMessage(request);
    dbg.VERBOSE("about to send:"+requestMessage);
    return write(requestMessage);
  }


  public void close() {
    dbg.VERBOSE("close() internal socket");
    SocketX.Close(socket);
    os = null;
    is = null;
    socket = null;
  }

  private SinetSocket() {
    ensureDebuggers();
  }

  public static SinetSocket Create(){
    return new SinetSocket();
  }

}
//$Id: SinetSocket.java,v 1.15 2005/03/29 06:38:46 andyh Exp $
