/**
 * Title:        StatusPage<p>
 * Description:  Displays the status of the JSERV and PayMate systems<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: StatusPage.java,v 1.53 2001/11/17 20:06:37 mattm Exp $
 */

package net.paymate.web.page;
import  net.paymate.web.*;
import  net.paymate.web.table.*;
import  net.paymate.web.table.query.*;
import  net.paymate.web.color.*;
import  net.paymate.util.*;
import  net.paymate.net.*; // sendmail
import  net.paymate.connection.*;
import  net.paymate.servlet.*;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  java.io.*;
import  javax.servlet.http.*;
// temporary?
import  java.util.*; // vector, date, timezone
import  java.text.SimpleDateFormat;
import  net.paymate.database.*;

public class StatusPage extends Document implements Entities {

  private static final ErrorLogStream dbg = new ErrorLogStream(StatusPage.class.getName());

  private static String hostname = null;

  public static final void init(String hostname) {
    if(!Safe.NonTrivial(hostname)) {
      StatusPage.hostname = "* 'computername' not set in config! *";
    } else {
      StatusPage.hostname = hostname;
    }
  }

  public static final Element printStatus(HttpSession session, TimeZone tz, ColorScheme colors, String sessionid, ConnectionServer conn, BackupAgent buagent, SendMail mailer, StatusClient statter) {
    String title = title();
    ElementContainer ec = new ElementContainer();
    String rev = net.paymate.Revision.Version();
    String revdisp = rev + ((rev.indexOf(net.paymate.Revision.WIPSTR) == -1) ? (" - " + net.paymate.Revision.jarSize()) : "");
    ec.addElement(new Center(new H2(title).addElement(" ["+revdisp+"]")))
      .addElement(PayMatePage.BRLF)
//oldcode-new ConnectionServer?I think receipts moved into the database instead of being kept outside and potentially deleted      .addElement(new AuthorizersFormat(colors, conn.authmgr, buagent, mailer, statter, conn.receiptFilePath, "Services", session.getSessionContext()))
      .addElement(PayMatePage.BRLF);

    ec.addElement(new RunTimeFormat(colors, "RunTime"))
      .addElement(PayMatePage.BRLF)
      .addElement(new TimesFormat(colors, "Times", tz));
/*
    Table t = new Table().setBorder(0).setWidth("100%");
    TR tr = new TR();
    t.addElement(tr);
    TD tdLeft= new TD().setVAlign("TOP");
    TD tdRight = new TD().setVAlign("TOP");
    tr.addElement(tdLeft).addElement(tdRight);
    tdLeft.addElement(new RunTimeFormat(colors, "RunTime"));
    tdRight.addElement(new TimesFormat(colors, "Times", tz));
    ec.addElement(t);
*/

    if(session != null) {
      ec.addElement(PayMatePage.BRLF)
        .addElement(HttpSessionTableGen.output("HttpSessions", colors, session.getSessionContext(), sessionid));
    }
    ec.addElement(PayMatePage.BRLF)
      .addElement(new StatementsFormat(colors, "Open Queries"));
    return ec;
  }

  public static final Element printThreads(ColorScheme colors, String sessionid) {
    String title = title() + " Threads";
    Element ec = new ElementContainer().
      addElement(new Center(new H2(title))).
      addElement(PayMatePage.BRLF).
      addElement(new ThreadFormat(colors, "Threads")).
      addElement(PayMatePage.BRLF).
      addElement(MonitorTableGen.output("Monitors", colors, Monitor.dumpall(), sessionid));
    return ec;
  }

  public static final Element printParams(ColorScheme color, String sessionid, PayMateDB db) {
    String title = title() + " Parameters";
    ElementContainer ec = new ElementContainer();
    ec.addElement(new Center(new H2(title)))
      .addElement(PayMatePage.BRLF)
      .addElement(new HR())
      .addElement(EasyCursorTableGen.output("Loaded Servlet Parameters", color, UserSession.grabbedParams, sessionid))
      .addElement(new HR())
      .addElement(AnyDBTableGen.output("Service Configuration Parameters", color, db.getResultSet(db.query(QueryString.SelectAllFrom("servicecfg"))), null, null))
      .addElement(new HR())
      .addElement(EasyCursorTableGen.output("System Properties", color, new EasyCursor(System.getProperties()), sessionid))
      .addElement(new HR())
      .addElement(PackageArrayTableGen.output("Packages", color, Package.getPackages(), sessionid))
      .addElement(new HR());
    return ec;
  }

  private static final String title() {
    return Safe.TrivialDefault(hostname, "Status");
  }

}
