/*
 * $Id: PenetrationTestServlet.java,v 1.9 2001/10/02 17:06:39 mattm Exp $
 * Copyright 2000, PayMate, Inc.
 */

// prolly comment out the next line if ever really use this again
package net.paymate.servlet;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.net.InternetMediaType;
// jdk stuff
import java.net.URLDecoder;
import java.io.IOException;
// jsdk stuff
import javax.servlet.*;
import javax.servlet.http.*;
// ecs stuff
import org.apache.ecs.*;
import org.apache.ecs.html.*;

/**
 * This servlet receives HTTP requests and urldecodes them,
 * then compares them to a known string.
 * <P>
 */

// +++ DEAL WITH THREADING ISSUES!

public class PenetrationTestServlet extends HttpServlet {

  private static final ErrorLogStream dbg=new ErrorLogStream(PenetrationTestServlet.class.getName());
  private static final String defString = "You know something I could really do without?  " +
        "The Space Shuttle ... it's irresponsible.  " +
        "The last thing we should be doing is sending our grotesquely distorted DNA out into space. " +
        " -- George Carlin"; // only occurs when the servlet is inited
  static final String title = PenetrationTestServlet.class.getName();

  // here is the cleaner stuff ...
  public long maxAgeMillis() {
    return 0;
  }

  public long maxUnaccessedMillis() {
    return 0;
  }

/**
 * This code provides the penetration testing<BR>
 * Primary Responsibilities:<BR>
 * send message and receive replys about the value of a public static string<BR>
 *<BR>
 * for testing: http:<server_etc>/pentest?<urlencodedstring><BR>
*/
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    Document doc = null;
    try { //put a try here so that we can ALWAYS (or almost always) respond, even when we crash
      HttpSession session = req.getSession(true); // +++ use the stuff in SessionedServlet!
      if((session != null) && !session.isNew()) {
        // output the page header (etc.)here:
        doc = (Document) new Document();
        doc.setDoctype(new Doctype.Html40Frameset())
           .appendTitle(title)
           .appendBody(new H1(title));
        res.setContentType(InternetMediaType.TEXT_HTML);
        // get the last stored string
        String  oldStr  = (String)session.getValue(title);
        // get some parameters
        boolean getting = (req.getParameter("get") != null);
        String  setStr  = URLDecoder.decode(req.getParameter("set"));
        boolean setting = (setStr != null);
        dbg.VERBOSE("Q=" + req.getQueryString());
        if(getting || setting) {
          docLog(doc, "ACK: Test string " + (setting ? "was" : "is") + " '" + oldStr + "'.");
        } else { // !getting && !setting (just testing)
          String query = req.getQueryString();
          docLog(doc, ((query != null) && URLDecoder.decode(query).equals(oldStr)) ?
                      "ACK: String matched.":
                      "NAK: String match failed.");
        }
        if(setting) {
          session.putValue(title, setStr);
          docLog(doc, "ACK: Test string set to '" + setStr + "'.");
        }
      } else {
        docLog(doc, "NAK: Session not created yet; waiting ...");
      }
    } catch (Exception e) {
      docLog(doc, "Exception: " + e.getMessage() + ".");
    } finally {
      // output the page here:
      if(doc != null) {
        doc.output(res.getOutputStream());  // and now stream the document out
        doc = null; // to allow for timely cleanup
      }
    }
  }

  protected void docLog(Document doc, String mess) {
    if(mess != null) {
      dbg.VERBOSE(mess);
      if(doc != null) {
        doc.appendBody(mess).appendBody(new BR());
      }
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doGet(req, res);
  }

/**
 *  initializes the superclass
 */
//  public void init(ServletConfig config) throws ServletException {
//    super.init(config);
//  }

}
