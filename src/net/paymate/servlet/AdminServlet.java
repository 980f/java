/**
 * Title:        AdminServlet<p>
 * Description:  <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: AdminServlet.java,v 1.107 2004/04/08 19:21:15 mattm Exp $
 */

package net.paymate.servlet;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.web.page.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
import net.paymate.database.*;
import net.paymate.connection.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.ecs.*;
import org.apache.ecs.html.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;
import net.paymate.io.ByteFifo;
import net.paymate.web.page.accounting.*;
import net.paymate.web.table.query.*;
import net.paymate.database.ours.query.*;
import net.paymate.terminalClient.Receipt;
// for receipt printing:
import net.paymate.jpos.awt.Hancock;
import net.paymate.jpos.Terminal.Scribe612;
import net.paymate.data.*; // TimeRange
import net.paymate.awtx.print.image.*;// PrinterModel
import net.paymate.io.IOX;
// SS2
import net.paymate.data.sinet.business.*;
import net.paymate.data.sinet.hardware.*;

// On web pages, radios cannot be used because of the way they behave.  Instead, use checkboxes or dropdowns.

/**
 * This servlet receives HTTP requests and decides which classes
 * should be called upon to fulfill a request.
 * <P>
 * DO NOT MOVE OR RENAME THIS SERVLET!  IT'S ADDRESS IS HARDCODED IN SERVER CONFIGS!
 */
public class AdminServlet extends SessionedServlet {

  static {
    try {
      ReflectX.preloadClass("net.paymate.servlet.SessionedServlet");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static final String BRLF = "<BR>\n";

  // logging facilities
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(AdminServlet.class, ErrorLogStream.WARNING);

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
      dbg.Enter("doIt");
      HttpSession session = req.getSession(true);
      Thread curThread = Thread.currentThread();
      curThread.setName("admin/" + session.getId()); // rename the thread so that we can track it.
      curThread.setPriority(WEBTHREADPRIORITY);
      // DON'T DROP THE THREAD PRIORITY (they then get starved)
      os = res.getOutputStream();
      String path = req.getPathInfo();
      service.println("IN:"+path);
      PayMatePage pmp=null;
      UserSession user = getSession(req);
      String qry = StringX.TrivialDefault(req.getQueryString(), "");
      res.setContentType(InternetMediaType.TEXT_HTML);
      IPSpec remoteIP = IPSpec.New(req.getRemoteAddr());
      EasyProperties ezp = httpServletRequest2EasyProperties(req);
      disablePageCaching(res);
      if(user == null) {
        pmp = new Login(new Center(new B(BRLF+"System down for periodic maintenance.  Please try again in a few minutes."+BRLF)),null); // thank you for using ...
      } else {
        LoginInfo linfo = user.linfo;
        // +++ maybe deal with bound objects here and deal with page generation as a function call to the object?
        // this can be a request for the following pages (for now):
        // flip through the list of files and see if they match
        // if they do, load it
        // +++ (later use a TrueEnum for these pages)
        // insist that the client starts a session before access to data is allowed
        if(!StringX.NonTrivial(path) || (path.length() < 2) || (path == "//") || (path == "/")) {
          dbg.VERBOSE("path was = " + path);
          path = Acct.key();
        }
        dbg.VERBOSE("path = " + path);
        if (isPage(path, Logout.key())) {
          user.logout();
          pmp = new Login(new Center(new B(BRLF+"Thank you for using PayMate.net!"+BRLF)),null); // thank you for using ...
        } else if(!validSession(req)) {
          pmp = new Login(isPage(path, Acct.key()) ?
            new Center(new B(BRLF+"Your login has expired.  Please login again."+BRLF)):
            null, null);
        } else if (isPage(path, Login.key())) {
          pmp = new Login((Element)null, linfo.forDisplay());
        } else if (isPage(path, Acct.ReceiptRequestor)) {
          if(loggedIn(user)) {
            res.setContentType(InternetMediaType.IMAGE_PNG);
            if (!printReceipt(ezp, os, remoteIP)) {
              res.setContentType(InternetMediaType.TEXT_PLAIN);
              os.write("Image generation error: server needs attention.".
                       getBytes());
            }
          }
          return;
        } else if (isPage(path, Acct.CSVDrawerRequestor)) {
          if(loggedIn(user)) {
            // also try: application/excel", or "text/comma-separated-values"
            res.setContentType(InternetMediaType.TEXT_TAB_SEPARATED_VALUES);
            printCsvDrawer(req, os, user);
          }
          return;
        } else {
          if(!loggedIn(user)) {
            linfo.web_login(webRequest, new ClerkIdInfo(req.getParameter(Login.USERID),
                req.getParameter(Login.PASSWORD)),
                            new Enterpriseid(req.getParameter(Login.ENTID)),
                            true);
            if(loggedIn(user) && linfo.isaGod()) { // only database administrators have this pOwEr
              dbg.WARNING("Setting terminatums to forever!");
              user.setTerminatums(session, forever); // let it live forever! --- possible security hole
            }
          }
          if(!loggedIn(user)) {
            // note that the login error could have change due to other threads hitting this first!
            String error = (user.linfo.loginError==null) ?
                "Server error.  Please try again." : user.linfo.loginError;
            ElementContainer ec2 = new ElementContainer().addElement(new B(error)).addElement(PayMatePage.BRLF);
            pmp = new Login(ec2, linfo.forDisplay());
          } else {
            // we should have a good user login here
            String query = StringX.TrivialDefault(req.getQueryString(), "");
            dbg.WARNING("Query: " + query + "\nLogin: " + linfo.forDisplay());
            pmp = generatePage(ezp, user); // THE WORK HAPPENS HERE !!!!! // +++ perhaps this should happen from a COPY?
          }
        }
      }
      // this is so that we can heavily snoop on what is going on.
      // +++ later, replace this with a forked print stream that we can close before we exit
      ByteArrayOutputStream sos = new ByteArrayOutputStream(20000); // average page size as of 20040224
      if(pmp!=null) {
        pmp.output(sos);
      } else{
        dbg.ERROR("Strange login error.  User=" + user);
      }
      sos.close();
      byte [] tooutput = sos.toByteArray();
      // stuff it with the appropriate length info
      PageStatistics.SubstituteLength(tooutput);
      os.write(tooutput);
      service.println("OUT:"+new String(tooutput));
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

  private String doing = "";

  // this is synchronized via sessionMon so that the user only gets
  // one page generated at a time (for our programming ease)
  public PayMatePage generatePage(EasyProperties ezp, UserSession session) {
    ElementContainer ec = new ElementContainer();
    LoginInfo linfo = session.linfo;
    PayMatePage pmp = null;
    AdminOpCode op = null;
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    Element elemNotes = null;
    String notes = null;
    if(linfo.sessionMon.monitorCount() > 0) { // still a thread running through it!
      return new News("Your session is still busy [" +
                      linfo.sessionMon.ownerInfo() + "] with: " + doing , linfo, AdminOp.NewsAdminOp.code());
    } else {
      try {
        linfo.sessionMon.getMonitor();
        doing = ezp.toString();
        // barf the parameters you got
        String adminOpStr = ezp.getString(AdminOp.adminPrefix);
        op = new AdminOpCode(adminOpStr); // this is the command we are doing right now ...
        dbg.WARNING("AdminOpCode = " + op + " (from adminOpStr=" + adminOpStr +
                    ")");
        dbg.ERROR("Request parameters: " + ezp.asParagraph(","));
        // check permissions
        if(session.linfo.permissionsValid(op)) { // checks for op islegal, too
          switch(op.Value()) { // keep these in alphabetical
            case AdminOpCode.appliance: {
              pmp = new AppliancePage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.applianceDeath: {
              // put on the appliance page
              // example URL to get here: adm=applianceDeath&a=7136&dc=5
              Applianceid appid = new Applianceid(ezp.getInt("a"));
              if(appid.isValid()) {
                int deathcode = ezp.getInt("dc");
                Appliance appliance = ApplianceHome.Get(appid);
                dbg.ERROR("BEFORE DC: appid=" + appid);
                if(appliance.setDeathcode(deathcode)) {
                  notes = "Appliance " + appliance + " deathcode set to " +
                      deathcode;
                } else {
                  notes = "Unable to set appliance [" + appliance +
                      "] deathcode to " + deathcode;
                }
                dbg.ERROR("AFTER DC");
              } else {
                notes = "Invalid applianceid to die:" + appid;
              }
              pmp = new AppliancesPage(linfo, op, ezp, notes);
            }
            break;
            case AdminOpCode.appliances: {
              pmp = new AppliancesPage(linfo, op, ezp, notes);
            }
            break;
            case AdminOpCode.associate: {
              pmp = new AssociatePage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.associates: {
              pmp = new AssociatesPage(linfo, op, null);
            }
            break;
            case AdminOpCode.authAttempts: {
              pmp = new AuthAttemptsPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.authBill: {
              // eg: adm=authBill&month=200304&authid=2&storeid=8
              pmp = new AuthBillPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.batch: {
              pmp = new BatchPage(linfo, op, ezp, false /*we will never create this page from here in archive mode*/);
            }
            break;
            case AdminOpCode.batches: {
              pmp = new BatchesPage(linfo, op, ezp, false /*we will never create this page from here in archive mode*/);
            }
            break;
            case AdminOpCode.defaultOp: {
              // fall out and see pmp==null, below
            }
            break;
            case AdminOpCode.deposit: {
              pmp = new DepositPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.deposits: {
              pmp = new DepositsPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.drawer: {
              pmp = new DrawerPage(linfo, op, ezp, false /*we will never create this page from here in archive mode*/);
            }
            break;
            case AdminOpCode.drawers: {
              pmp = new DrawersPage(linfo, op, ezp, false /*we will never create this page from here in archive mode*/);
            }
            break;
            case AdminOpCode.duptemp: {
              // +++ put on the txnsearch page
              // eg: adm=duptemp&since=20010909
              pmp = new Acct(linfo, op, false /*we will never create this page in archive mode*/);
              pmp.fillBody(AnyDBTableGen.output("Possible Duplicates",
                                                linfo.colors(),
                                                db.getResultSet(
                                                db.getDups(ezp.
                  getString("since") + "000000")), null));
            }
            break;
            case AdminOpCode.editRecord: {
              pmp = new RecordEditPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.enterprise: {
              pmp = new EnterprisePage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.changeEnterprise: {
              elemNotes = changeEnterprise(new Enterpriseid(ezp.getInt(Login.
                  ENTID)), linfo);
              // MUST fall out to enterprises !
            } // break;
            case AdminOpCode.enterprises: {
              pmp = new EnterprisesPage(linfo, op, elemNotes);
            }
            break;
            case AdminOpCode.newAppliance: {
              if(linfo.isaGod()) { // may not need to check this
                Applianceid applid = null;
                String pw = ezp.getString(Acct.newapplpw);
                if(linfo.assoc.passes(pw)) {
                  Appliance appliance = ApplianceHome.New(linfo.store.storeId());
                  if(appliance != null) {
                    applid = appliance.applianceid();
                    // add the new appliance to the current store & show its edit page
                    pmp = new AppliancePage(linfo, op, ezp, applid);
                  } else {
                    notes = "Can't create appliance due to unknown error.";
                  }
                } else {
                  notes = cantCreate("appliance", linfo);
                }
                if( ! Applianceid.isValid(applid)) {
                  pmp = new AppliancesPage(linfo, op, ezp, notes); // show the appliances page ...
                }
              }
            }
            break;
            case AdminOpCode.newAssociate: {
              if(linfo.isaGod()) { // may not need to check this
                String pw = ezp.getString(Acct.newassocpw);
                if(linfo.assoc.passes(pw)) {
                  // create the new associate, add storeaccess for every store & show its edit page
                  Associate assoc = session.linfo.enterprise.newAssociate();
                  // +++ check return value
                  createAllStoreAccesses(session.linfo.enterprise.stores.getAll(),
                                         assoc, false /*no perms*/, db);
                  pmp = new AssociatePage(linfo, op, assoc.associateid());
                } else {
                  notes = cantCreate("associate", linfo);
                  pmp = new AssociatesPage(linfo, op, notes); // show the associates page ...
                }
              }
            }
            break;
            case AdminOpCode.newEnterprise: {
              if(linfo.isaGod()) {
                String pw = ezp.getString(Acct.newentpw);
                if(linfo.assoc.passes(pw)) {
                  // create the new enterprise
                  Enterprise newEnt = EnterpriseHome.New(linfo.assoc.
                      associateid());
                  Enterpriseid entid = newEnt.enterpriseid();
                  { // +++ do all of this block, via SS2, *INSIDE* newEnt!
                    // 1 gawd associate
                    EnterprisePermissions gawdEPerms = new
                        EnterprisePermissions();
                    gawdEPerms.canDB = gawdEPerms.canViewAuthMsgs = gawdEPerms.
                        canWeb = true;
                    Associate newgawd = newEnt.cloneAssociate(linfo.assoc);
                    // +++ check return value
                    createAllStoreAccesses(newEnt.stores.getAll(), newgawd,
                                           true /*all accesses*/, db);
                  }
                  // switch to it
                  elemNotes = changeEnterprise(entid, linfo);
                  // show its edit page ...
                  pmp = new EnterprisePage(linfo, op, entid);
                } else {
                  notes = cantCreate("enterprise", linfo);
                  pmp = new EnterprisesPage(linfo, op, notes); // show the enterprises page ...
                }
              } else {
                dbg.ERROR(
                    "User who isn't a gawd trying to create a new enterprise!" +
                    linfo.toString());
              }
            }
            break;
            case AdminOpCode.news: {
              pmp = news(linfo, op);
            }
            break;
            case AdminOpCode.newStore: {
              if(linfo.isaGod()) {
                String pw = ezp.getString(Acct.newstorepw);
                if(linfo.assoc.passes(pw)) {
                  // create the new store
                  Store store = linfo.enterprise.newStore();
                  Storeid storeid = store.storeId();
                  // add a storeauth entry for every authid+settleid in storeauth for other stores in this enterprise
                  db.fillStoreauths(storeid);
                  // create all storeaccesses (with no access) +++ check return value
                  Associate [ ] alist = linfo.enterprise.associates.getAll();
                  for(int i = alist.length; i-->0;) {
                    createStoreAccess(alist[i], storeid, false /*no perms*/, db);
                  }
                  // show its edit page ...
                  pmp = new StorePage(linfo, op, storeid);
                } else {
                  notes = cantCreate("store", linfo);
                  pmp = new StoresPage(linfo, op, notes); // show the stores page ...
                }
              }
            }
            break;
            case AdminOpCode.newStoreauth: {
              if(linfo.isaGod()) {
                String pw = ezp.getString(Acct.newstoreauthpw);
                Storeid storeid = new Storeid(ezp.getInt(Login.STOREID));
                if(linfo.assoc.passes(pw)) {
                  // create the new storeauth
                  db.addStoreauth(storeid);
                  // show its edit page ...
                } else {
                  notes = cantCreate("storeauth", linfo);
                }
                pmp = new StorePage(linfo, op, storeid, notes);
              }
            }
            break;
            case AdminOpCode.newTermauth: {
              if(linfo.isaGod()) {
                String pw = ezp.getString(Acct.newtermauthpw);
                Terminalid termid = new Terminalid(ezp.getInt(TerminalsFormat.
                    TERMID));
                if(linfo.assoc.passes(pw)) {
                  // checks for any missing termauths in the configuration and adds them
                  db.fillTermauths(termid);
                } else {
                  // bitch +++ create some note and pass it to the terminalpage
                }
                // show its edit page ...
                pmp = new TerminalPage(linfo, op, termid);
              }
            }
            break;
            case AdminOpCode.newTerminal: {
              if(linfo.isaGod()) {
                String pw = ezp.getString(Acct.newtermpw);
                Applianceid applid = new Applianceid(ezp.getInt(
                    AppliancesFormat.APPLID));
                if(linfo.assoc.passes(pw)) {
                  // create the new terminal
                  Terminalid termid = db.addTerminal(applid, linfo.store.storeId());
                  // add a termauth entry for every authid and settleid in storeauth entries for the store that the terminal belongs to
                  db.fillTermauths(termid);
                  // show its edit page ...
                  pmp = new TerminalPage(linfo, op, termid);
                } else {
                  notes = cantCreate("terminal", linfo);
                  pmp = new AppliancePage(linfo, op, applid); // show the appliance's page ...
                }
              }
            }
            break;
            case AdminOpCode.service: {
              pmp = new ServicePage(linfo, op,
                                    session.getHttpSession().getSessionContext(),
                                    ezp);
            }
            break;
            case AdminOpCode.services: {
              pmp = new ServicesPage(linfo, op);
            }
            break;
            case AdminOpCode.store: {
              pmp = new StorePage(linfo, op, ezp, null);
            }
            break;
            case AdminOpCode.storeaccess: {
              pmp = new StoreAccessPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.storeauth: {
              pmp = new StoreAuthPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.stores: {
              pmp = new StoresPage(linfo, op, null);
            }
            break;
            case AdminOpCode.termauth: {
              pmp = new TermAuthPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.terminal: {
              pmp = new TerminalPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.terminals: {
              pmp = new TerminalsPage(linfo, op);
            }
            break;
            case AdminOpCode.TESTAPPLIANCE: {
              // put on the appliances page
              boolean on = ezp.getBoolean("SLEEPER");
              ConnectionServer.THE().SLEEPER(on);
              ec.addElement("SLEEPER turned " + (on ? "ON" : "OFF"));
            }
            break;
            case AdminOpCode.transaction: {
              pmp = new TransactionPage(linfo, op, ezp);
            }
            break;
            case AdminOpCode.txnSearch: {
              pmp = new SearchPage(linfo, op, ezp);
            }
            break;
            default: {
              // see below where pmp == null
            }
            break;
          }
        } else { // then return the default page
          // see below where pmp == null
        }
        // send the page back
      } catch(Throwable /*Exception*/ emon2) { // catch throwable here to prevent corruption of monitor ??? ++++++++++
        dbg.Caught("generatePage():", emon2);
      } finally {
        try {
          if(pmp == null) {
            pmp = defaultPage(linfo);
          }
        } catch(Exception ex) {
          dbg.Caught(ex);
        } finally {
          doing = "";
          linfo.sessionMon.freeMonitor();
          return pmp;
        }
      }
    }
  }

  // once the StoreAccess is in SS2, move all of this code into the Enterprise object !!!
  // since everything isn't SS2 yet, have to do this when adding a store or an associate
  private static StoreAccessid createStoreAccess(Associate assoc, Storeid storeid,
                                                 boolean allOrNothing, PayMateDB db) {
    ClerkPrivileges perms = new ClerkPrivileges();
    if(allOrNothing) {
      perms.grantAllForStore();
    } else {
      perms.clear(); // grants NONE for store
    }
    StoreAccessid ret = db.createStoreAccess(storeid, assoc.associateid(), perms);
    return ret;
  }
  private static boolean createAllStoreAccesses(Store [ ] slist, Associate assoc,
                                                boolean allOrNothing, PayMateDB db) {
    boolean ret = true;
    for(int i= slist.length; i-->0;) {
      ret &= StoreAccessid.isValid(createStoreAccess(assoc, slist[i].storeId(),
          allOrNothing, db));
    }
    return ret;
  }

  private PayMatePage defaultPage(LoginInfo linfo) {
    if(linfo.isaGod()) {
      return new ServicesPage(linfo, AdminOp.servicepg.code());
    } else {
      return news(linfo, AdminOp.NewsAdminOp.code()); // else, give them the news page
    }
  }

  private ElementContainer changeEnterprise(Enterpriseid enterid, LoginInfo linfo) {
    ElementContainer ec = new ElementContainer();
    try {
      // be sure there is a gawd login like this for the enterprise we are switching to ...
      Associate [] assocs = AssociateHome.GetAllByNameEnterprise(linfo.assoc.loginname, enterid);
      if(assocs.length > 0) {
        Associate assoc = assocs[0];
        ActionReplyStatus ars =
            linfo.web_login((ActionRequest)new AdminWebRequest(),
                              new ClerkIdInfo(assoc.loginname,
                                              assoc.encodedpw),
                              enterid, true /*check permissions*/);
        dbg.WARNING("login as [" + linfo.assoc + "] to enterprise "+enterid+" returned: " + ars);
        dbg.WARNING("linfo is now: " + linfo);
        ec.addElement("Enterprise is now: "+EnterpriseHome.Get(enterid).enterprisename);
      } else {
        ec.addElement(new B().addElement(new Center().addElement("No '"+linfo.assoc.loginname+"' login exists for enterprise " + enterid + "!")));
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
      ec.addElement(new B().addElement(new Center().addElement("Exception: " + ex)));
    } finally {
      return ec;
    }
  }
  private String cantCreate(String what, LoginInfo linfo) {
    String res = "Cannot create "+what+" due to invalid password!";
    dbg.WARNING(res + " linfo="+linfo);
    return res;
  }
  private PayMatePage news(LoginInfo linfo, AdminOpCode op) {
    return new News(configger.getServiceParam(service.serviceName(), "news", ""), linfo, op);
  }

  // note that the name of the link is the filename you get, so name it appropriately! [eg: drawer1234.tsv, or by terminal and time]
  public void printCsvDrawer(HttpServletRequest req, OutputStream os, UserSession user) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    String query = StringX.TrivialDefault(req.getQueryString(), "");
    // barf the parameters you got
    dbg.VERBOSE("Query: " + query);
    // query for the drawer
    Drawerid did = new Drawerid(StringX.TrivialDefault(req.getParameter(Acct.CSVDid), ""));
    Terminalid tid = new Terminalid(StringX.TrivialDefault(req.getParameter(Acct.CSVTid), ""));
    Batchid bid = new Batchid(StringX.TrivialDefault(req.getParameter(Acct.CSVBid), ""));
    TermAuthid taid = new TermAuthid(StringX.TrivialDefault(req.getParameter(Acct.CSVTaid), ""));
    CSVTransactionFormat csver = null;
    String merchRefLabel = user.linfo.store.merchreflabel;
    if(Drawerid.isValid(did)) {
      TxnRow stmt = db.getDrawerClosingCursor(did, user.linfo.ltf());
      csver = new CSVTransactionFormat(merchRefLabel, stmt, true /*countLosses*/, user.linfo.ltf());
    } else if(Terminalid.isValid(tid)) {
      TxnRow stmt = db.unsettledTxnsQuery(tid);
      csver = new CSVTransactionFormat(merchRefLabel, stmt, false /*countLosses*/, user.linfo.ltf());
    } else if(Batchid.isValid(bid)) {
      TxnRow stmt = db.getBatchCursor(bid, user.linfo.ltf());
      csver = new CSVTransactionFormat(merchRefLabel, stmt, false /*countLosses*/, user.linfo.ltf());
    } else if(TermAuthid.isValid(taid)) {
      TxnRow stmt = db.unsettledTxnsQuery(taid);
      csver = new CSVTransactionFormat(merchRefLabel, stmt, false /*countLosses*/, user.linfo.ltf());
    }
    if(csver != null) {
      try {
        os.write(csver.getBytes());
        os.flush();
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    } else {
      service.PANIC("Could not export CSV for drawerid="+did+"and terminalid="+tid+"!");
      return;
    }
  }

  public static boolean printReceipt(TxnRow rec, String outputFile, PayMateDB db, String font) {
    FileOutputStream fos = null;
    try {
      IOX.createParentDirs(outputFile);
      fos = new FileOutputStream(outputFile);
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
    return (fos==null) ? false : printReceipt(rec, fos, db, font, null);
  }

  // +++ cache the receipt in a system that removes the oldest receipts from its queue based on lifetime and queue size limits.
  // eventually make this use the security, too
  public static boolean printReceipt(TxnRow rec, OutputStream os, PayMateDB db, String rcptFont, Service serv) {
    if(rec == null) {
      return false;
    }
    Terminalid tid = rec.terminalid();
    Associateid assid = rec.associateid();
    String loginname = "";
    Associate assoc = AssociateHome.Get(assid);
    if(assoc != null) {
      loginname = assoc.loginname;
    }
    String fontName = StringX.TrivialDefault(rcptFont, PNGModel.DEFAULTFONTNAME);
    TerminalInfo ti = db.getTerminalInfo(tid);
    StoreConfig cfg = db.getStoreConfig(StoreHome.Get(db.getStoreForTerminal(tid)));
    ClerkIdInfo cii = null;
    if(StringX.NonTrivial(loginname)) {
      cii = new ClerkIdInfo(loginname, "SECRET");
    }
    // make a receipt from the txnrow
    Receipt rcpt = new Receipt(rec.extractReply(), rec.extractRequest(),
                               cfg.termcap.MerchRefPrompt(), ti,
                               cii, cfg.si.timeZoneName, cfg.receipt.TimeFormat);
    rcpt.setHeader(cfg.receipt.Header);
    rcpt.setTrailer(cfg.receipt.Tagline);
    Hancock hcsig = rec.getSignature();//sigrcpt.getSignature();
    if (Hancock.NonTrivial(hcsig)) {
      rcpt.setShowSignature(true); // makes it show a manual one if there isn't one to show, otherwise it will link to show the png one
      rcpt.setItem(hcsig);
      dbg.WARNING("printReceiptImage(): set signature to: "+hcsig);
    } else {
      dbg.WARNING("printReceiptImage(): signature is trivial!");
      rcpt.manSigning(false); // makes it show a manual one if there isn't one to show, otherwise it will link to show the png one
    }
    // create the formatter
    PNGModel png = null;
    try {
      png = new PNGModel(new Scribe612(null), os, rcpt.totalLines(), true, fontName);
    } catch (Exception t) {
      dbg.Caught("No AWT available?", t);
    }
    if(png == null) {
      if(serv != null) {
        // +++ PANIC !!!
        serv.PANIC("RED ALERT!  Unable to generate image!  Can't create PNGModel! txnidint=" + rec.txnid);
      }
      return false;
    } else {
      // print it to the output stream
      dbg.VERBOSE("printReceiptImage(): printing to stream...");
      rcpt.print(png,0);//always ask for prime receipt when printed from web, never "duplicate"
      dbg.VERBOSE("printReceiptImage(): done printing to stream.");
      return true;
    }
  }

  // if this function was called, there IS a signature image, so proceed as if we already know that
  public boolean printReceipt(EasyProperties reqezp, OutputStream os, IPSpec remoteIP) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    // barf the parameters you got
    dbg.VERBOSE("Query props: " + reqezp);
    // query for the txn
    TxnRow rec = db.getTxnRecordfromTID(new Txnid(reqezp.getInt(Acct.TransID)));
    return printReceipt(rec, os, db, db.getServiceParam(service.serviceName(), "receiptFont", PNGModel.DEFAULTFONTNAME), service);
  }

  private boolean loggedIn(UserSession user) {
    return (user != null) && user.linfo.loggedIn;
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doIt(req, res); // cause I'm really lazy
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    doIt(req, res); // cause I'm really lazy
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    if(service == null) {
      service = new AdminServletService("AdminServlet", this, configger /* will be null  :( */);
    }
  }

  // need to set the header fields to cause it to always get a new page (no caching)
  // this prevents the need for special settings on the browser (we hope)
  private static final void disablePageCaching(HttpServletResponse res) {
    res.setHeader("Pragma", "no-cache");
    res.setHeader("Expires", "-1");
    res.setHeader("Cache-Control", "no-cache");
    res.setHeader("Cache-Control", "no-store");
    res.setHeader("Cache-Control", "must-revalidate");
    res.setHeader("Cache-Control", "max-age=0");
    res.setHeader("Cache-Control", "private");
  }

}

class AdminServletService extends SessionedServletService {
  public AdminServletService(String name, AdminServlet servlet, ServiceConfigurator cfg) {
    super(name, servlet, cfg);
  }

  /*
  Sessions begun from a browser will be monitored for inactivity and disconnected
  after either 10 minutes of inactivy
  or after two hours of connectivity, with activity.
  NOTE: these are DEFAULTS.  The actual values will be in the database.
  */
  public long MAXAGEMILLIS() {
    return Ticks.forHours(2);
  }
  public long TIMEOUTMILLIS() {
    return Ticks.forMinutes(10);
  }
}

///////////////////////////////////////?/
/// Regarding page expirations and accidental caching by servers
/// see 13.2.1, 14.9, 14.32 in RFC 2616, HTTP/1.1, June 1999.
