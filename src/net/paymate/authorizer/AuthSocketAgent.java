package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthSocketAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.47 $
 */

import java.net.*; // Socket
import net.paymate.net.*; // IPSpec
import java.io.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.data.*; // packet
import net.paymate.text.Formatter;

public class AuthSocketAgent implements TimeBomb {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthSocketAgent.class, ErrorLogStream.WARNING);

  protected Socket socket = null;
  public boolean createError = false;
//  protected IPSpec ips[] = null;
  protected Packet vb = null;

  protected Authorizer handler = null; // for printing to the auth's log
  protected boolean shouldDie = false;

  public AuthSocketAgent(/*IPSpec ips[], */Packet vb, Authorizer handler) {
    this.vb = vb;
    this.handler = handler;
    String ipstr = "";
  }

  public void onTimeout() {
    kill();
  }


  // since only one thing can go through here at time
  protected final Alarmum alarmum = Alarmer.New(0, (TimeBomb)this); //0 for timeout == don't want it to go off yet

  /**
   * @return success
   */
  public boolean sendRequest(AuthTransaction authTransaction) {
    boolean ret = false;
    try {
      handler.println("sendRequest starting run method");
      int sent = 0;
      int received = 0;
      byte [] request = authTransaction.request.toBytes();//#npe's, fixed in sendRequest()
      // don't need to reset the receiver, we will destroy the whole InBs gizmo rather than reuse it.
      try {
        Alarmer.reset(handler.timeout, alarmum);  // setup an alarmer to kill me if I don't come back within TIMEOUT seconds!
        getAuthSocket(authTransaction);
        // send the message
        sent = writeBytes(request);
        handler.writes.add(sent);
        // receive the reply
        ret = readBytes(vb);
//        Alarmer.Defuse(alarmum);
      } catch(Exception exception2) {
        handler.println("AuthSocketAgent had possible error after send[" + sent + "]/receive[" + received + "] bytes: " + exception2);
        handler.ips.thisFailed(authTransaction.host);
      } finally {
        Alarmer.Defuse(alarmum); // moved here so that if an exception is caught, it is still properly defused!
        if(!shouldDie) {
          byte [] reply = vb.packet();
          handler.reads.add(reply.length);
          handler.println("AuthSocketAgent received " + Ascii.bracket(reply) );
          handler.println("AuthSocketAgent.sendRequest():---"+vb.dump(null).toString());
          authTransaction.response.process(vb);
        }
        handler.println("AuthSocketAgent is closing.");
        kill();
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      handler.PANIC("AuthSocketAgent.sendRequest()-Exception: "+t);
    }
    shouldDie = false;
    return ret;
  }

//  protected DataOutputStream oStream=null;
  protected DataInputStream iStream=null;
  protected OutputStream oStream=null;
//  protected InputStream iStream=null;

  protected void getAuthSocket(AuthorizerTransaction tran) throws Exception {
    getSocket(handler.AUTHOPERATION, tran);
  }
  protected void getSettleSocket(AuthorizerTransaction tran) throws Exception {
    getSocket(handler.SETLOPERATION, tran);
  }
  private void getSocket(int operation, AuthorizerTransaction tran) throws Exception {
    // get a socket ...
    oStream=null;
    iStream=null;
    Counter attempts = (tran != null) ? tran.socketOpenAttempts : new Counter(); // shorthand
    while(!shouldDie && (socket==null)) {
      String blurb = (operation==handler.AUTHOPERATION ? "AUTH" : "SETTLE")+
          " connection to " + handler.serviceName();
      try {
        attempts.incr();
        if(attempts.value() == 1) {
          handler.println("Attempting first time: " + blurb);
        } else {
          handler.println("Attempting again [#" +attempts.value()+ "]: "+blurb);
        }
        socket = handler.openSocket(operation, tran);
        if (shouldDie) { // killed while connecting ...
          handler.println("sendRequest killed ...");
          /*re*/kill();
        } else {
          if (socket == null) {
            handler.println("sendRequest could not (null socket) make "+blurb);
          } else {
            handler.println("sendRequest made " + blurb);
            handler.println("sendRequest is starting.");
            break;
          }
        }
      } catch(Exception exception) {
        handler.println("sendRequest failed to make " + blurb + ": " + exception);
        socket = null;
      }
    }
    if(socket == null) {
      if(!shouldDie) {
        createError = true;
      }
      throw new Exception(shouldDie ? "Killed due to timeout." : "Unable to create any sockets!");
    }
    // get the output stream
    try {
      oStream = new DataOutputStream(socket.getOutputStream());
    } catch(Exception exception) {
      handler.println("sendRequest failed to get outputstream from socket " + exception);
      throw exception;
    }
    // get the input stream
    try {
      iStream = new DataInputStream(socket.getInputStream());
    } catch(SocketException socketexception) {
      handler.println("sendRequest failed to get inputstream from socket " + socketexception);
      throw socketexception;
    } catch(Exception exception) {
      handler.println("sendRequest failed getting input stream" + exception);
      throw exception;
    }
  }

  /**
   * returns number of bytes written
   */
  protected int writeBytes(byte [] toWrite) throws IOException {
    oStream.write(toWrite);
    oStream.flush();
    int sent = toWrite.length;
    handler.println("writeBytes sent with flush " + Ascii.bracket(toWrite));
    return sent;
  }

  public static boolean readBytes(Packet vb, DataInputStream iStream, Authorizer handler) throws IOException {
    int bite = -1;
    // received directly into a Packet, using its "complete" function to terminate the loop
    while(true){//while stream not closed..., and not EOF, I guess
      if(vb.isComplete()) {
        handler.println("Completed buffer");
        break;
      }
dbg.VERBOSE("readBytes().BEFORE READ");
      bite = iStream.read();
dbg.VERBOSE("readBytes().AFTER READ");
      if(dbg.levelIs(LogSwitch.VERBOSE)) {
        handler.println("received:"+Formatter.ox2(bite));
      }
      if(bite==-1){
        handler.println("Eof on socket!");
        break;
      }
      if(!vb.append(bite)){//get a true if byte was added ok, including lrc check (if any)!
        handler.println("Error adding bite["+bite+"] to rcvr!");
        //break; removed to allow for bad bytes before stx???
      }
    }
    boolean ok = vb.isOk();
    boolean complete = vb.isComplete();
    handler.println("Rcvr is " +
                    (complete ? "" : "NOT ") +"complete" +
                    (ok ? " and" : ", but NOT") + " ok!");
    return ok;
  }
  protected boolean readBytes(Packet vb) throws IOException {
    return readBytes(vb, iStream, handler);
  }

  // this guy can't do it.  Extend this class and fill it in!
  // +++ need to find what is common between this and SubmittalSocketAgent, and make that into a shared base class.
  public boolean sendSubmittal(AuthSubmitTransaction txn) {
    return false;
  }

  public boolean kill() {
    try {
      shouldDie = true;
      if(socket != null) {
        socket.close();
        socket = null;
      }
      return true;
    } catch(IOException _ex) {
      return false;
    }
  }

}
