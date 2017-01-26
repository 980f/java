package net.paymate.authorizer.paymentech.netconnect;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/paymentech/netconnect/NetConnectPTAuth.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.21 $
 */

import net.paymate.authorizer.*;
import net.paymate.authorizer.paymentech.*;
import net.paymate.data.VisaBuffer;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.io.*;
import net.paymate.lang.*;
import java.io.*;
import java.net.*;
import net.paymate.terminalClient.PosSocket.paymentech.*;
import net.paymate.connection.*;
import net.paymate.data.MerchantInfo;

public class NetConnectPTAuth extends PaymentechAuth {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NetConnectPTAuth.class);

  public NetConnectPTAuth() {
    super();
  }

  protected void loadProperties() {
    super.loadProperties();
  }

  // OPEN A SOCKET TO THE AUTHORIZER!
  public Socket openSocket(int operation, AuthorizerTransaction tran) {
    Socket ret = null;
    Socket rawSocket = super.openSocket(operation, tran);
    if(rawSocket != null){
      try {
        ret = new NetConnectSocket(rawSocket, tran, this);
      } catch (Exception ex) {
        dbg.Caught(ex);
        PANIC(ex.toString());
      }
    }
    return ret;
  }
}

// REQUIRES A FLUSH!
class NetConnectOutputStream extends OutputStream {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NetConnectOutputStream.class);
//  private ByteFifo ba = null;
  private NetConnectSocket imp = null;
  private VisaBuffer vb = VisaBuffer.NewReceiver(70000).setClipLRC(); // ??? what maxsize
  private ByteArrayFIFO bafifo = new ByteArrayFIFO();
  private boolean closed = false;
  private Authorizer handler = null;

  public NetConnectOutputStream(NetConnectSocket imp, Authorizer handler) {
    super();
    this.imp = imp;
    this.handler = handler;
  }
  public void write(int biteint) throws IOException {
    if(closed) {
      throw new IOException("Stream is closed");
    }
    byte bite  = (byte)(biteint & 255);
//    handler.println("write(byte) called with: " + Ascii.bracket(bite));
    vb.append(bite);
    if(vb.isOk()) {
      bafifo.put(vb.packet());
      vb.reset();
    }
  }
  public void flush() throws IOException {
    if(closed) {
      throw new IOException("Stream is closed");
    }
    byte [ ] txnrequest = bafifo.nextByteArray();
    // add an ETX to the end -- gets stripped by VisaBuffer.packet():
    if(txnrequest != null) {
      // check for ETX on the end
      int last = txnrequest.length - 1;
      if(last > -1) {
        // if it doesn't have one, add it
        if(txnrequest[last] != Ascii.ETX) {
          int newlen = txnrequest.length+1;
          byte [ ] tmp = new byte [newlen];
          if(tmp.length > 0) {
            System.arraycopy(txnrequest, 0, tmp, 0, txnrequest.length);
          }
          tmp[last+1] = Ascii.ETX;
          txnrequest = tmp;
        }
      }
    }
    if(txnrequest != null) {
      dbg.WARNING("imp["+imp+"].transact("+Ascii.image(txnrequest)+")");
      imp.transact(txnrequest);
    }
  }
  public void close() throws IOException {
    // +++ write() and close() should synchronize on something to prevent issues with the 2 statements below, and similar code in write()
    closed = true;
  }
}

// this class does not need to block since the bytes will all be here before the client starts reading!
class NetConnectInputStream extends InputStream {
  private InputStream bais = null;
  public NetConnectInputStream(ByteFifo ba) {
    super();
    this.bais = ba.getInputStream();
  }
  // if you are calling this function, then there were no exceptions in the txn with PT,
  // so all can proceed as normal
  public int read() throws IOException {
    return bais.read(); // returns -1 when out of data
  }
}

class NetConnectSocket extends Socket {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(
      NetConnectSocket.class);

  private boolean used = false;
  private Socket rawSocket = null;
  private AuthorizerTransaction tran = null;
  private Authorizer handler = null;
  private EasyProperties ezout = new EasyProperties();

  public NetConnectSocket(Socket rawSocket, AuthorizerTransaction tran, Authorizer handler) throws IOException {
    if(rawSocket == null) {
      throw new IOException("rawSocket is null!");
    }
    this.rawSocket = rawSocket;
    this.tran = tran;
    this.handler = handler;
    String authmid = null;
    PTTid ptnctid = new PTTid();
    boolean stateless = false;
    boolean termCap = false;
    if(tran instanceof PTTransaction) {
      PTTransaction ptt = (PTTransaction)tran;
      authmid = ptt.merch.authmerchid;
      ptnctid.setto(ptt.merch.authtermid);
      stateless = true;
      termCap = true;
    } else if(tran instanceof PTGWTransaction) {
      PTGWTransaction ptt = (PTGWTransaction)tran;
      // get the merchantid and terminalid from the request
      PTGatewayRequest ptgwreq = (PTGatewayRequest) ptt.request;
      byte [] gwreqbytes = ptgwreq.toBytes();
      UTFrequest req=UTFrequest.From(gwreqbytes);
      authmid = req.merchid;
      ptnctid.setto(ptt.potentialGWTID);
      ptnctid.tid = req.terminal; // just in case
      stateless = true; // ALL HCS txns are stateless!!!
      termCap = false;  // by definition
    } else if(tran instanceof AuthSubmitTransaction) {
      AuthSubmitTransaction ptt = (AuthSubmitTransaction)tran;
      authmid = ptt.request.merch.authmerchid;
      ptnctid.setto(ptt.request.merch.authtermid);
      stateless = false;
      termCap = true;
    } else {
      // don't know what it is; exit
      throw new IOException("Unknown transaction type: " + tran);
    }
    // NOTE that by default the authmid has the client code at the front!  Remove it!
    if(authmid.length() >= (PaymentechAuth.CLIENTLEN+PaymentechAuth.MIDLENGTH)) {
      authmid = StringX.right(authmid, PaymentechAuth.MIDLENGTH);
    }
    // prep the socket
    proxyos = rawSocket.getOutputStream();
    proxyis = rawSocket.getInputStream();
    dis = new DataInputStream(proxyis);
    // set the parameters
    ezout.setString(USERNAME, ptnctid.username);
    ezout.setString(PASSWORD, ptnctid.password);
    ezout.setString(TRANSACTIONTYPE, stateless ? TRANSACTIONTYPESTATELESS : TRANSACTIONTYPESTATEFUL);
    ezout.setString(METHOD, termCap ? METHODTCS : METHODHCS);
    ezout.setString(TERMINALID, ptnctid.tid);
    ezout.setString(MERCHANTID, authmid);
    dbg.VERBOSE(ptnctid.spam());
  }

  public void close() throws IOException {
    try {
//      ncos.write(Ascii.EOT); // this tells the proxy server to close its PT cnxn
//      ncos.flush();
//      proxyos.write(Ascii.EOT); // this tells the proxy server to close its PT cnxn
      proxyos.flush();
    } catch (Exception ex) {
      // ???
    } finally {
      IOX.Close(ncos);
      IOX.Close(ncis);
      IOX.Close(proxyos); // raw
      IOX.Close(proxyis); // raw
      SocketX.Close(rawSocket);
      super.close();
    }
  }

  // internal streams to make local classes think they are getting regular socket streams
  private OutputStream ncos = null;
  public synchronized OutputStream getOutputStream() {
    if(ncos == null) {
      ncos = new NetConnectOutputStream(this, handler);
    }
    return ncos;
  }
  private ByteFifo response = new ByteFifo(true /*blocking*/);
  private InputStream ncis = null;
  public synchronized InputStream getInputStream() {
    if(ncis == null) {
      ncis = new NetConnectInputStream(response);
    }
    return ncis;
  }

  // +++ get these from somewhere (shared with C++ code !!!)
  public static final String MESSAGEBASE64 = "MESSAGEBASE64";
  // REQUEST ONLY
  public static final String USERNAME = "USERNAME";
  public static final String PASSWORD = "PASSWORD";
  public static final String TRANSACTIONTYPE = "TRANSACTIONTYPE";
  public static final String TRANSACTIONTYPESTATEFUL = "STATEFUL";
  public static final String TRANSACTIONTYPESTATELESS = "STATELESS";
  public static final String METHOD = "METHOD";
  public static final String METHODHCS = "HCS";
  public static final String METHODTCS = "TCS";
  public static final String BFIRSTINBATCH = "BFIRSTINBATCH";
  public static final String BFIRSTINBATCHIS = "ISFIRSTINBATCH";
  public static final String BFIRSTINBATCHNOT = "NOTFIRSTINBATCH";
  public static final String TERMINALID = "TERMINALID";
  public static final String MERCHANTID = "MERCHANTID";
  // REPLY ONLY
  public static final String EXCEPTIONMESSAGE = "EXCEPTIONMESSAGE";

  // these streams are for connecting to the NetConnectProxyServer
  private OutputStream proxyos = null;
  private InputStream proxyis = null;
  private DataInputStream dis = null;

  private int count = 0;
  private static final String CRLF = "\r\n ";
  public void transact(byte [ ] data) throws IOException {
    count++;
    VisaBuffer vb = VisaBuffer.NewReceiver(2000).setClipLRC(); // +++ optimize for max size
    boolean okay = false;
    byte [ ] reqBytes = null;
    byte [ ] respBytes = null;
    try {
      // set the message
      String datastr = new String(data);
      handler.println("Before BASE64: " + Ascii.bracket(data));
      ezout.setURLedString(MESSAGEBASE64, datastr); // and now it is encoded so it won't mess up our datastream
      // set which part of the total message this is
      ezout.setString(BFIRSTINBATCH, (count == 1 ? BFIRSTINBATCHIS : BFIRSTINBATCHNOT));
      // send the message
      String ezoutstr = ezout.toString(); // +++ filter for STX and ETX?
      reqBytes = ezoutstr.getBytes();
      // send the properties between an STX and ETX.  Use a Packet or Buffer? +++
      proxyos.write(Ascii.STX);
      proxyos.write(ezoutstr.getBytes());
      proxyos.write(Ascii.ETX);
      proxyos.flush();
      handler.println("transact() sent: \n"+ezoutstr);  // +++ PanicStream instead ???
      // receive the reply
      AuthSocketAgent.readBytes(vb, dis, handler);
      okay = vb.isComplete(); // ???
    } catch (Exception ex) {
      dbg.Caught(ex);
      // +++ PANIC?
      handler.println("Exception attempting to transact: " + ex);
      closeAndThrow(ex.toString());
    }
    byte [ ] responsebytes = vb.packet(); // this has STX and ETX (and possibly garbage at the end)
    // you will get back a properties between an STX and ETX
    // (the C++ should filter to prevent corruption of the data stream))!
    // dissect the reply (rip the authresponse, etc., out of the reply and
    // stick it in the response object)
    if((responsebytes == null) || (responsebytes.length < 1)) {
      closeAndThrow("No response received");
    }
    respBytes = responsebytes;
    String responseString = new String(responsebytes);
    if(!okay) {
      closeAndThrow("Partial reply: " + responseString);
    }
    handler.println("transact() received: \n"+responseString);
    EasyProperties ezin = EasyProperties.FromString(responseString);
    String datastr = ezin.getURLedString(MESSAGEBASE64, "");
    String exception = ezin.getString(EXCEPTIONMESSAGE);

//    // run the datastr through a VisaBuffer, to trim the crap off, IF it is an STX/ETX packet (otherwise, don't care)
//    // --- this may be moot now.  there was a bug in the way that
//    // --- the NetConnectProxyServer was sending back the strings.
//    // --- They should now be properly terminated.
//    // --- Try it without this block of code.
//    // --- If it works, remove it permanently.
//    byte [ ] databees = datastr.getBytes();
//    if((databees != null) && (databees.length > 0) && (databees[0] == Ascii.STX)) {
//      vb.reset();
//      vb.append(databees);
//      databees = vb.packet();
//      handler.println("transact() cleaned up message, now="+Ascii.bracket(datastr));
//    }

    handler.println("transact() unpacked: \nmessage="+Ascii.bracket(datastr)+"\nexception="+exception);
    // we *should* get only ONE of the above!
    if(StringX.NonTrivial(exception)) { // BAD!
      // if there was a problem, first close the socket, then throw an exception!
      closeAndThrow(exception);
    }
    OutputStream bos = response.getOutputStream();
    bos.write(datastr.getBytes());
    bos.flush(); // silly
    // done !
    // do not do this next line until SS2 is here.  It screws with other stuff!
    // handler.logAuthAttempt(reqBytes, respBytes, tran);
  }

  private void closeAndThrow(String exceptionMessage) throws IOException {
    close();
    throw new IOException(exceptionMessage);
  }

  public String toString() {
    return "NetConnectSocket_For_"+rawSocket;
  }
}
