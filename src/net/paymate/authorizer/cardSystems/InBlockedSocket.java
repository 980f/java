package net.paymate.authorizer.cardSystems;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/InBlockedSocket.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

import java.io.*;
import java.net.*;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.authorizer.*;

public class InBlockedSocket implements Runnable/*, TimeBomb */{

  private static final ErrorLogStream dbg = new ErrorLogStream(InBlockedSocket.class.getName());

  private boolean bCanSendRecords;
  private Authorizer handler = null;
  private int bufferSize = 1024;
  protected Socket socket=null;
  private IPSpec ips[] = null;
  private static final Counter nameCounter = new Counter();
//  private long timeoutMs = 35000;

//  private static final Alarmer alarmer = new Alarmer();

  public InBlockedSocket(IPSpec ips[], int bufferSize, Authorizer handler, String hostname/*, long timeoutMs*/) throws Exception {
    this.bufferSize = bufferSize;
    this.handler = handler;
    this.ips = ips;
//    this.timeoutMs = timeoutMs;
    handler.println("Received " + ips.length + " IPs for connecting.");
  }

  public byte [] request = null;
  public byte [] reply = null;
  Thread towaken = null;

  private Thread myThread = null;
  public void sendRequest(byte [] request) {
    this.request = request;
    towaken = Thread.currentThread();
    myThread = new Thread(this, InBlockedSocket.class.getName()+"-"+nameCounter.incr());
    myThread.start();
  }

/*
  public void onTimeout() {
    if(socket != null) {
      socket.close();
    }
  }
*/


// @@@ +++ make sure that serverside standin db stamping and txncomplete dbstamping are mutually exclusive regarding fields.

  public void run() {
    try {
      handler.println("InBlockedSocket starting run method");
      int sent = 0;
      int received = 0;
      VisaBuffer rcvr=MAuthRec.forReply(); //should be passed in.
      try {
        // get a socket ...
        for(int i = ips.length; i -->0;) {
          IPSpec ip = ips[i];
          try {
            handler.println("Attempting connection to: " + ip.toString());
            socket = new Socket(ip.address, ip.port);
// +++ @@@ is marked dead ???         (kill should)
            handler.println("InBlockedSocket made connection to: " + ip.toString());
            handler.println("InBlockedSocket is starting.");
            break;
          } catch(UnknownHostException unknownhostexception) {
            handler.println("InBlockedSocket failed to find: "  + ip.toString() + " " + unknownhostexception);
            socket = null;
          } catch(IOException ioexception) {
            handler.println("InBlockedSocket failed to make socket connection to "  + ip.toString() + " " + ioexception);
            socket = null;
          }
        }
        if(socket == null) {
          throw new Exception("Unable to create any sockets!");
          // +++ send email notification!
        }
        // get the output stream
        DataOutputStream oStream=null;
        try {
          oStream = new DataOutputStream(socket.getOutputStream());
        } catch(Exception exception) {
          handler.println("SPPSocketThread.init() failed to get outputstream from socket " + exception);
          throw exception;
        }
        // get the input stream
        DataInputStream iStream=null;
        try {
          iStream = new DataInputStream(socket.getInputStream());
        } catch(SocketException socketexception) {
          handler.println("SPPObjectSocketThread.blockOnGetInputStream() failed to get inputstream from socket " + socketexception);
          throw socketexception;
      } catch(Exception exception) {
          handler.println("SPPObjectSocketThread.blockOnGetInputStream() failed " + exception);
          throw exception;
        }
        // start the guy who will kill you if you take too long
//      alarmer.Set(new Alarmum(dynamite, timeoutMs));
        // send the message
        oStream.write(request);
        oStream.flush();
        sent = request.length;
        handler.writes.add(sent);
        handler.println("InBlockedSocket sent [" + new String(request) + "]");//+++ escape the string ourselves
        // receive the reply
        int bite = -1;
        //receive directly into a VisaBuffer, use its "complete" function to terminate the loop
        //you have implemented a subset of that here.
        while((bite = iStream.read())!=-1){//while stream not closed..., and not EOF, I guess
          if(!rcvr.append(bite)){//get a true if byte was added ok, including lrc check!
            handler.println("Error adding bite["+bite+"] to rcvr!");
          }
          if(rcvr.isComplete()){ //you have a properly 'shaped' packet
            handler.println("Rcvr is complete!");
            break;
          }
        }
      } catch(Exception exception2) {
        handler.println("InBlockedSocket had possible error after send[" + sent + "]/receive[" + received + "] bytes: " + exception2);
      } finally {
        if(rcvr.isOk()){//its content also passes checksummed
          handler.println("Rcvr packet is good.");
          //this is a complete good response
          reply = rcvr.packet();
        } else {
          handler.println("Rcvr is NOT okay! Complete but trashed response.");
          //this is a complete but trashed response
          reply = rcvr.packet();
        }
        handler.println("Received so far: [" + rcvr.packet()+"]");//previously swampwed us with nulls.
        received = reply.length;
        handler.reads.add(received);
        handler.println("InBlockedSocket received [" + new String(reply) + "]");
        handler.handleResponse(new CSResponseNotificationEvent(this));
        handler.println("InBlockedSocket is closing.");
        kill();
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      // +++ @@@ Log to problems.log
    }
  }

  public boolean kill() {
    try {
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
// $Id: InBlockedSocket.java,v 1.9 2001/11/03 13:16:38 mattm Exp $
