package net.paymate.servlet;
/*
 * $Id: TxnServlet.java,v 1.66 2004/02/21 07:31:54 mattm Exp $
 * Copyright 2000, PayMate, Inc.
 */

import  net.paymate.util.*;
import  net.paymate.connection.*;
import  net.paymate.web.ConnectionServer;
import  net.paymate.net.*;
import  net.paymate.web.UserSession;
import  java.io.*;
import  javax.servlet.*;
import  javax.servlet.http.*;
import  java.util.*;
import net.paymate.io.Streamer;
import net.paymate.web.SinetServer;

/**
 * This servlet receives HTTP requests and decides which classes
 * should be called upon to fulfill a request.
 * <P>
 * DO NOT MOVE OR RENAME THIS SERVLET!  IT'S ADDRESS IS HARDCODED IN SERVER CONFIGS!
 *
 * +++ Bump up thread prorities on all threads that handle txn stuff
 * (and leave all other threads lower, with occasionaly spikes to prevent starvation).
 * Test what effect Thread.priority++ has on TxnServlet.
 * Be sure and set the threads priority back before exiting the TxnServlet!
 *
 */
public class TxnServlet extends SessionedServlet {

  // logging facilities
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(TxnServlet.class, ErrorLogStream.VERBOSE);

  // +++ deal with thread issues
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String reply = null;
    PrintWriter pw = null;
    Thread curThread = Thread.currentThread();
    curThread.setName("txn/" + req.getSession(true).getId()); // rename the thread so that we can track it.
    curThread.setPriority(TXNTHREADPRIORITY); // bump up priority so that txn threads happen faster than admin threads
    IPSpec remoteIP = IPSpec.New(req.getRemoteAddr());
    try {
      dbg.Enter("doPost");
      pw = resp.getWriter();
      resp.setContentType(InternetMediaType.TEXT_HTML);
      // echo what we got:
      EasyProperties props = httpServletRequest2EasyProperties(req);
      String specialEOL = System.getProperty("line.separator");
      props.debugDump("Servlet request parameters:"+specialEOL, specialEOL, "=");
      service.println("IN:"+props.asParagraph(specialEOL, "="));
      // now grab the body
      BufferedReader br = req.getReader();
      StringWriter sw = new StringWriter();
      dbg.VERBOSE("Read " + Streamer.swapCharacters(br, sw) + " bytes from body.");
      String body = String.valueOf(sw);
      incoming.add(body.length()); // accumulate approximate
      dbg.ERROR("Request as string:" + body);//to ensure we have a record of all txn attempts
      // now deal with it
      UserSession session = getSession(req);
      if(session == null || down) {
        reply = String.valueOf((new ActionReply(ActionReplyStatus.UnavailableDueToTxnSystemMaintenance)).toProperties());
      } else {
        ConnectionServer cs = ConnectionServer.THE();
        if((cs != null) && cs.isUp()) {
          reply = cs.ReplyTo(body, session.linfo, props, remoteIP);
        } else {
          reply = UNAVAILABLEDUETOTXNSYSTEMAINTENANCE();
        }
      }
    } catch (Throwable t) {
      dbg.Caught(t);
    } finally {
      if(pw != null) {
        if(reply != null) {
          dbg.ERROR("Reply as props:"+reply);
          pw.println(reply+"\n"); // LEAVE THE \N!
          service.println("OUT:"+reply);
          outgoing.add(reply.length());
        } else {
          dbg.ERROR("Nothing returned since generated reply is NULL!");
        }
        pw.close();
      }
      curThread.setPriority(WEBTHREADPRIORITY); // set it back
      HttpSession session = req.getSession(false);
      if(session != null) {
        session.invalidate();
      }
//      System.gc(); // suggest to get the db connections back
      dbg.Exit();
    }
  }

  private static final String UNAVAILABLEDUETOTXNSYSTEMAINTENANCE() {
    return String.valueOf( (new ActionReply(ActionReplyStatus.
        UnavailableDueToTxnSystemMaintenance)).toProperties());
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if(service == null) {
      service = new TxnServletService("TxnServlet", this, configger /* will be null  :( */);
    }
  }

}


class TxnServletService extends SessionedServletService {
  public TxnServletService(String name, TxnServlet servlet, ServiceConfigurator cfg) {
    super(name, servlet, cfg);
  }
  /*
  Sessions begun from our client application will not be monitored for
  inactivity by the server.  However, the client application will be required
  to logout+in on a roughly 24-hour period.  This means that after a terminal
  has been logged in for 24 hours, it will be automatically logged out.
  NOTE: These are DEFAULTS.  Actual values are in the database.
  */

  public long MAXAGEMILLIS() {  // disconnect it if it has been connected this long, even if there has been activity
    return Ticks.forDays(1);    // since we aren't keeping the sockets open indefinitely yet.
  }
  public long TIMEOUTMILLIS() { // disconnect it when there is no activity for this long
    return Ticks.forMinutes(10);
  }

}
