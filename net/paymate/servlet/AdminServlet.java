/**
 * Title:        AdminServlet<p>
 * Description:  <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: AdminServlet.java,v 1.51 2001/10/24 11:01:16 mattm Exp $
 */

package net.paymate.servlet;
import  net.paymate.net.*;
import  net.paymate.util.*;
import  net.paymate.web.page.*;
import  net.paymate.web.color.*;
import  net.paymate.web.*;
import  net.paymate.connection.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.ecs.*;
import org.apache.ecs.html.*;

/**
 * This servlet receives HTTP requests and decides which classes
 * should be called upon to fulfill a request.
 * <P>
 * DO NOT MOVE OR RENAME THIS SERVLET!  IT'S ADDRESS IS HARDCODED IN SERVER CONFIGS!
 */
public class AdminServlet extends SessionedServlet {

  // logging facilities
  protected static final ErrorLogStream dbg=new ErrorLogStream(AdminServlet.class.getName(), ErrorLogStream.WARNING);

  protected static final boolean isPage(String path, String key) {
    if((path == null) || (key == null) || (key.length() > path.length())) {
      return false;
    }
    // +++ good enough for now;
    // +++ later check for other things after length (char that is not / or ? or "" is error)
    return path.regionMatches(true, 1, key, 0, key.length());
  }

  // this next one seems to work fine as a static
  protected static final AdminWebRequest webRequest = new AdminWebRequest();

  private static final SessionTermParams forever = new SessionTermParams(-1l, -1l);

  protected void doIt(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    OutputStream os = null;
    try {
      dbg.Enter("doGet");
      HttpSession session = req.getSession(true);
      Thread curThread = Thread.currentThread();
      curThread.setName("admin/" + session.getId()); // rename the thread so that we can track it.
      curThread.setPriority(Thread.NORM_PRIORITY);
      // DON'T DROP THE THREAD PRIORITY (they then get starved)
      os = res.getOutputStream();
      String path = req.getPathInfo();
      PayMatePage pmp=null;
      UserSession user = getSession(req);
      String qry = Safe.TrivialDefault(req.getQueryString(), "");
      res.setContentType(InternetMediaType.TEXT_HTML);
      if(user == null) {
        pmp = new Login(new Center(new B("<BR>System down for periodic maintenance.  Please try again in a few minutes.<BR>")),null); // thank you for using ...
      } else {
        boolean loggedIn = user.loggedIn;
        // +++ maybe deal with bound objects here and deal with page generation as a function call to the object?
        // this can be a request for the following pages (for now):
        // flip through the list of files and see if they match
        // if they do, load it
        // +++ (later use a TrueEnum for these pages)
        // insist that the client starts a session before access to data is allowed
        if(!Safe.NonTrivial(path) || (path.length() < 2) || (path == "//") || (path == "/")) {
          dbg.VERBOSE("path was = " + path);
          path = Acct.key();
        }
        dbg.VERBOSE("path = " + path);
        if (isPage(path, Logout.key())) {
          loggedIn = user.logout(session);
          pmp = new Login(new Center(new B("<BR>Thank you for using PayMate.net!<BR>")),null); // thank you for using ...
        } else if(!validSession(req)) {
          pmp = new Login(isPage(path, Acct.key()) ?
            new Center(new B("<BR>Your login has expired.  Please login again.<BR>")):
            null, null);
        } else if (isPage(path, Login.key())) {
          pmp = new Login((Element)null, user.loginInfo());
        } else if (isPage(path, UserSession.ReceiptRequestor)) {
          res.setContentType(InternetMediaType.IMAGE_PNG);
          if(!user.printReceipt(req, os)) {
            res.setContentType(InternetMediaType.TEXT_PLAIN);
            os.write("Image generation error: server needs attention.".getBytes());
          }
          return;
        } else /*if (isPage(path, Acct.key()))*/ {
          if(loggedIn && user.isNew()) { /* +++ or if it has been inactive too long */
            pmp = new Login(new ElementContainer().addElement(PayMatePage.br)
              .addElement(PayMatePage.br).addElement(new B("In order to use our services, please enable cookies from this website."))
              .addElement(PayMatePage.br).addElement(PayMatePage.br), user.loginInfo());
          } else {
            if(!loggedIn) {
              ActionReplyStatus ars = user.login(req, webRequest);
              loggedIn = UserSession.goodLogin(ars);
            }
            if(!loggedIn) {
              // note that the login error could have change due to other threads hitting this first!
              String error = (user.linfo.loginError==null) ?
                "Server error.  Please try again." : user.linfo.loginError;
              ElementContainer ec2 = new ElementContainer().addElement(new B(error)).addElement(PayMatePage.BRLF);
              pmp = new Login(ec2, user.loginInfo());
            } else {
              // we should have a good user login here
              dbg.VERBOSE("logging in now ...");
              pmp = user.generatePage(req); // THE WORK HAPPENS HERE !!!!! // +++ perhaps this should happen from a COPY?
            }
          }
        }
        if(loggedIn) {
          if(user.isaGod()) { // only database administrators have this pOwEr
            dbg.WARNING("Setting terminatums to forever!");
            user.setTerminatums(session, forever); // let it live forever! --- possible security hole
          }
        }
      }
      // this is so that we can heavily snoop on what is going on.
      // +++ later, replace this with a forked print stream that we can close before we exit
      ByteArrayFIFO snooper = new ByteArrayFIFO(1000);
      OutputStream sos = snooper.getOutputStream();
      pmp.output(sos);
      sos.close();
      byte [] tooutput = snooper.toByteArray();
      os.write(tooutput);
      outgoing.add(tooutput.length);
    } catch (Throwable e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      if(os!=null) {
        os.close();
      }
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doIt(req, res); // cause I'm really lazy
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doIt(req, res); // cause I'm really lazy
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

}
