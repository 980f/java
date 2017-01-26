package net.paymate.web;
/**
 * Title:        $Source: /cvs/src/net/paymate/web/UserSession.java,v $
 * Description:  Associates a LoginInfo with an HttpSession
 *               (so that once someone logs in,
 *               all subsequent requests will be handled as that user)<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Revision: 1.357 $
 *
 * Notes:        In order to use the session, perform this at the beginning of the HttpServlet.doGet() (for example):
 *               UserSession user = UserSession.extract(req, true); // true means to force it to create one if one doesn't exist
 *
 */

// +++ create a UserSessionList class to manage them, then create a single instance of it.

import net.paymate.util.*;
import javax.servlet.http.*;
import net.paymate.servlet.*;
import net.paymate.lang.StringX;

public class UserSession implements HttpSessionBindingListener, AtExit {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(UserSession.class, ErrorLogStream.WARNING);

  private static final SessionTermParams notime = new SessionTermParams(1, 1); // must be smallest number greater than zero
  public void AtExit() {
    if(!linfo.isaGod()) {
      // not a god, so kill it
      setTerminatums(getHttpSession(), notime); // give em nothing
    }
  }

  public boolean IsDown() {
    return true;
  }

  // the default; to be changed when the login occurs
  public final LoginInfo linfo = new LoginInfo();

  /////////////////////////
  // session binding stuff

  public void valueBound(HttpSessionBindingEvent event) {
    dbg.VERBOSE("BOUND as " + event.getName() + " to " + event.getSession().getId());
  }

  public void valueUnbound(HttpSessionBindingEvent event) {
    dbg.VERBOSE("UNBOUND as " + event.getName() + " from " + event.getSession().getId());
    linfo.logout();
  }

  String cachedSessionID = "";
  public String sessionid() {
    if(!StringX.NonTrivial(cachedSessionID) && (hSession != null)) {
      cachedSessionID = getHttpSession().getId();
    }
    return cachedSessionID;
  }

  private HttpSession hSession = null;
  public HttpSession getHttpSession() {
    return hSession;
  }

  public static final Monitor httpSessionMonitor = new Monitor("HttpSession");

  public static final void setTerminatums(HttpSession session, SessionTermParams newOnes) {
    try {
      httpSessionMonitor.getMonitor();
      // get or create the cleanup parameters
      SessionTermParams sessionTerms = (SessionTermParams)session.getValue(SessionTermParams.sessionTermParamsKey);
      if(sessionTerms == null) { // create one
        session.putValue(SessionTermParams.sessionTermParamsKey, new SessionTermParams(newOnes));
      } else { // modify one
        sessionTerms.mergeFrom(newOnes);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      UserSession.httpSessionMonitor.freeMonitor();
    }
  }

  public static final String userSessionKey = "UserSession";
  public void bind(HttpSession session) {
    try {
      httpSessionMonitor.getMonitor();
      // add the user login stuff to the list
      session.putValue(userSessionKey, this);
      this.hSession = session;
    } finally {
      httpSessionMonitor.freeMonitor();
    }
  }
  public void unbind() {
    // +++ maybe also log the logging in and out somewhere
    try {
      httpSessionMonitor.getMonitor();
      // +++ log the user out
      // then, kill the link
      hSession.removeValue(userSessionKey);
      hSession = null;
    } finally {
      httpSessionMonitor.freeMonitor();
    }
  }

  /////////////////////////////
  // session creation stuff

  public static final UserSession extractUserSession(HttpSession session) {
    return (UserSession)session.getValue(userSessionKey);
  }

  // +++ mutex ?!?
  public static final UserSession extractUserSession(HttpServletRequest req) {
    UserSession user = null;
    try {
      httpSessionMonitor.getMonitor();
      if(req != null) {
        HttpSession session = req.getSession(true);
        user = extractUserSession(session);
        if(user == null) {
          dbg.VERBOSE("User does not exist; creating");
          user = new UserSession(session);
        }
      }
    } finally {
      httpSessionMonitor.freeMonitor();
      return user;
    }
  }

//  /////////////////////
//  // some misc stuff
//
//  public boolean isNew() {
//    return hSession.isNew();
//  }

  public void logout() {
    unbind();
//    linfo.logout();
  }

  ////////////////////////
  // constructor (private)
  private UserSession(HttpSession session) {
    dbg.VERBOSE("about to bind ...");
    bind(session);
    dbg.VERBOSE("... should be bound.");
  }

}
//$Id: UserSession.java,v 1.357 2003/10/30 21:05:12 mattm Exp $
