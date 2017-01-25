package net.paymate.servlet;
/*
 * $Id: TxnServlet.java,v 1.43 2001/10/13 11:02:29 mattm Exp $
 * Copyright 2000, PayMate, Inc.
 */

import  net.paymate.util.*;
import  net.paymate.connection.*;
import  net.paymate.net.*;
import  net.paymate.web.UserSession;
import  java.io.*;
import  javax.servlet.*;
import  javax.servlet.http.*;
import  java.util.*;

/**
 * This servlet receives HTTP requests and decides which classes
 * should be called upon to fulfill a request.
 * <P>
 * DO NOT MOVE OR RENAME THIS SERVLET!  IT'S ADDRESS IS HARDCODED IN SERVER CONFIGS!
 */
public class TxnServlet extends SessionedServlet {

  // logging facilities
  protected static final ErrorLogStream dbg=new ErrorLogStream(TxnServlet.class.getName());

  /*
  Sessions begun from our client application will not be monitored for
  inactivity by the server.  However, the client application will be required
  to logout+in on a roughly 24-hour period.  This means that after a terminal
  has been logged in for 24 hours, it will be automatically logged out.
  */
  // NOTE: see SessionedServlet for more info.
  public long maxAgeMillis() {
    return Ticks.forMinutes(3);// 3 minutes should be PLENTY, since they do disconnect anyway (see next line)
     // since we aren't keeping the sockets open indefinitely yet.
  }
  public long maxUnaccessedMillis() {
    return 0;
  }

  // +++ deal with thread issues
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String reply = null;
    PrintWriter pw = null;
    Thread curThread = Thread.currentThread();
    curThread.setName("txn/" + req.getSession(true).getId()); // rename the thread so that we can track it.
    curThread.setPriority(Thread.NORM_PRIORITY+1); // bump up priority so that txn threads happen faster than admin threads
    try {
      dbg.Enter("doPost");
      pw = resp.getWriter();
      resp.setContentType(InternetMediaType.TEXT_HTML);
      // echo what we got:
      EasyCursor props = ServletReqToEasyCursor(req);
      String specialEOL = System.getProperty("line.separator");
      props.debugDump("Servlet request parameters:"+specialEOL, specialEOL, "=");
      // now grab the body
      BufferedReader br = req.getReader();
      StringWriter sw = new StringWriter();
      dbg.VERBOSE("Read " + Streamer.swapCharacters(br, sw) + " bytes from body.");
      String body = sw.toString();
      incoming.add(body.length()); // accumulate approximate
      dbg.ERROR("Request as string:" + body);//to ensure we have a record of all txn attempts
      // now deal with it:
      UserSession session = getSession(req);// +++ we really need security in this!  more than in other servlets!
      if(session == null) {
        reply = (new ActionReply(ActionReplyStatus.UnavailableDueToTxnSystemMaintenance)).toProperties().toString();
      } else {
        reply = session.replyTo(req, body, props);
      }
    } catch (Throwable t) {
      dbg.Caught(t);
    } finally {
      if(pw != null) {
        if(reply != null) {
          dbg.ERROR("Reply as props:"+reply.toString());
          pw.println(reply+"\n"); // LEAVE THE \N!
          outgoing.add(reply.length());
        } else {
          dbg.ERROR("Nothing returned since generated reply is NULL!");
        }
        pw.close();
      }
      dbg.Exit();
    }
  }

  protected static final EasyCursor ServletReqToEasyCursor(HttpServletRequest req) {
    EasyCursor props = new EasyCursor();
    for(Enumeration enump = req.getParameterNames(); enump.hasMoreElements(); ) {
      String name  = (String)enump.nextElement();
      props.setString(name, req.getParameter(name));
    }
    return props;
  }
}
