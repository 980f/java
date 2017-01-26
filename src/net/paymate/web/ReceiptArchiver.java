package net.paymate.web;

/**
 * <p>Title: $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/web/ReceiptArchiver.java,v $</p>
 * <p>Description: Creates an archive of the entire system's txns, receipts, and
 *    drawer and batch reports</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

/*
 * index.html $: list of all stores, linking to their index.html pages.
 * storenumber/
 *            /index.html         instructions and a set of links for allTxns, batches, and drawers pages.
 *            /dates.html         every day with a txn, in reverse chronological order
 *            /batches.html       every batch, in reverse chronological order
 *            /drawers.html       every drawer, in reverse chronological order
 *            /date/
 *                 /20030312.html ... a page for every day, just like a massive drawer listing
 *            /txn/
 *                /102030.html    ... link goes to *.png instead of a webpost
 *                /102030.png     ...
 *            /batch/
 *                  /123456.html  ... link goes to ../txns/*.html instead of a webpost
 *            /drawer/
 *                   /234567.html ... link goes to ../txns/*.html instead of a webpost
 */

import net.paymate.io.LogFile;
import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.AuthStoreFullRow;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.data.sinet.business.*;
import net.paymate.web.table.*;
import net.paymate.web.table.query.*;
import net.paymate.web.page.accounting.*;
import net.paymate.database.ours.query.*;
import net.paymate.util.timer.StopWatch;

public class ReceiptArchiver implements PerTxnListener {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ReceiptArchiver.class, ErrorLogStream.VERBOSE);

  // constants, all private
  //
  private static final int PRINTINTERVAL = 1000;
  private static final String BRLF = "<BR>\n";
  // extensions
  private static final String htmlExtension = ".html";
  private static final String pngExtension  = ".png";
  // paths - absolute really means relative from the store directory�!!!
  private static final String absoluteTxnPath    = "txn/";
  private static final String absoluteBatchPath  = "batch/";
  private static final String absoluteDrawerPath = "drawer/";
  private static final String absoluteDatePath   = "date/";
  private static final String upOne        = "../";

  // functions to create filenames
  // these do NOT prepend complete appropriate paths!
  private static final String htmlFilename(String id) {
    return id + htmlExtension;
  }
  private static final String txnFilename(String txnid) {
    return deepPath(txnid) + htmlFilename(txnid);
  }
  private static final String receiptFilename(String txnid) {
    return deepPath(txnid) + txnid + pngExtension;
  }
  private static final String drawerFilename(String drawerid) {
    return deepPath(drawerid) + htmlFilename(drawerid);
  }
  private static final String batchFilename(String batchid) {
    return deepPath(batchid) + htmlFilename(batchid);
  }
  private static final String dateFilename(String dateid) {
    return deepPath(dateid) + htmlFilename(dateid);
  }
  private static final String indexFilename() {
    return htmlFilename("index");
  }
  private static final String storeDirname(UniqueId store) {
    return "store"+store;
  }
  private static final String batchesFilename() {
    return htmlFilename("batches");
  }
  private static final String drawersFilename() {
    return htmlFilename("drawers");
  }
  private static final String datesFilename() {
    return htmlFilename("dates");
  }

  // this JUST creates the path: "23/"
  // YOU have to append the filename separately!
  private static final String deepPath(String id) {
    if(StringX.NonTrivial(id)) {
      StringBuffer sb = new StringBuffer(id.length() * 2);
      int iid = StringX.parseInt(id);
      int dir = iid / 1000;
      sb.append(dir).append("/");
      return sb.toString();
    } else {
      return "";
    }
  }

  // functions for path+filename to create links, etc.
  //
  // receipts and transactions are side-by-side:
  // 123456.html
  // 123456.png
  // creates the path to put a receipt reference into a txn page
  public static final String receiptForTransaction(String txnid) {
    return upOne + receiptFilename(txnid);
  }
  // creates the path to put a transaction link into a drawer, batch, or date page
  public static final String transactionForDrawerDateOrBatch(String txnid) {
    return upOne + upOne + absoluteTxnPath + txnFilename(txnid);
  }
  // creates the path to put a drawer link into the all-drawers page
  public static final String drawerForDrawers(String drawerid) {
    return absoluteDrawerPath + drawerFilename(drawerid);
  }
  // creates the path to put a batch link into the all-batches page
  public static final String batchForBatches(String batchid) {
    return absoluteBatchPath + batchFilename(batchid);
  }
  // creates the path to put a date link into the all-dates page
  public static final String dateForDates(String date) {
    return absoluteDatePath + dateFilename(date);
  }
  public static final String dates() {
    return htmlFilename("dates");
  }
  public static final String batches() {
    return htmlFilename("batches");
  }
  public static final String drawers() {
    return htmlFilename("drawers");
  }

  // the work ...
  private LoginInfo linfo = new LoginInfo();
  private String logininfo = "";
  private PayMateDB db = null;
  private int txncount = 0;
  private String font = "";
  // which store[s] do we archive?
  private Store[] stores = StoreHome.GetAll();//StoreHome.GetAllTxnCountOrder();
  // we will output all data to this dir:
  private String archivepath = mkdirs(LogFile.getPath(), "webarchive" + UTC.Now().toString());
  private TimeRange newWebDate = TimeRange.Forever(); // get the full range of web dates for this customer !!!
  private EasyProperties forMasterIndex = new EasyProperties();
  // some arrays of per-store stuff
  private TextList [] storedates = new TextList[stores.length];
  private LocalTimeFormat [] storedayonly = new LocalTimeFormat[stores.length];
  private String [] storepaths = new String[stores.length];
  private int [] storeTxnCounts = new int [stores.length];
  private int alltxns = 0;
  private int storestxnsdone = 0;
  private int thisstoretxnsdone = 0;
  private Store store = null;
  private boolean [ ] printed = null;
  private int storei = 0;
  private boolean txnsdone = false;
  private PrintFork log = null;
  private LogFile lf = null;

  public final void receiptArchiveReport(String storeidstr) {
    try {
      lf = new LogFile("ReceiptArchiver", false);
      log = lf.getPrintFork();
    } catch(Exception ex) {
      dbg.Caught(ex);
      return;
    }
    org.postgresql.Driver.info("tail ReceiptArchiver.log for output"); // this tests that out streams are setup correctly
    try {
      StopWatch sw = new StopWatch();
      LogSwitch.SetAll(LogSwitch.WARNING);
      dbg.setLevel(LogSwitch.VERBOSE);
      db = PayMateDBDispenser.getPayMateDB();
      db.smartVacuumer.down();
      font = db.getServiceParam("AdminServlet", "receiptFont", "");
      UnsettledTransactionFormat.PTL = this; // callback to print txns and rcpts based on need and availability
      printed = new boolean[db.getMaxPk(db.txn)+1];
      Storeid storeid = new Storeid(storeidstr);
      boolean justone = Storeid.isValid(storeid);
      log.println("Finding stores ...");
      EasyProperties storeEZcounts = db.getStoreTxnTotals();
      LocalTimeFormat ltf = LocalTimeFormat.New("America/Chicago", "MM/dd/yyyy");
      long sixMonths = Ticks.forDays(365/2);
      UTC now = UTC.Now();
      log.println("* = within last 6 months");
      for(int i = stores.length; i-->0;) { // do all of this BEFORE running the txns!
        Store thisStore = stores[i];
        Storeid thisstoreid = thisStore.storeId();
        if(justone && ! thisstoreid.equals(storeid)) {
          // continue;
        } else {
          String thisstoreidstr = thisstoreid.toString();
          storeTxnCounts[i] = storeEZcounts.getInt(thisstoreidstr);
          alltxns += storeTxnCounts[i];
          TxnRow txn = db.getLastStoreTxn(thisstoreid);
          if(!txn.next()) {
            log.println("Possible error finding store: " + thisstoreid);
          }
          UTC lastdate = UTC.New(txn.clientreftime);
          log.println(((lastdate.getTime() + sixMonths >= now.getTime()) ? "*" : " ") +
                      " Found [" + thisstoreidstr + "]" + thisStore.storename +
                      " with " + storeTxnCounts[i] + " txns, ending on " + ltf.format(lastdate) + ".");
        }
      }
      // for each store we will archive,
      for(storei = 0; storei < stores.length; storei++) {
        Store thisStore = stores[storei];
        Storeid thisStoreid = thisStore.storeId();
        String storeLabel = "[" + (storei+1) + "/" + stores.length + "] " +
                    thisStoreid + ": " + thisStore.storename +
                    " with " + storeTxnCounts[storei] + " txns";
        if(justone && ! thisStoreid.equals(storeid)) {
          // continue;
        } else {
          log.println("Starting store " + storeLabel + " ...");
          log.println("Completed store " + storeLabel + " in " +
                      DateX.millisToTime(processStore()) + ".");
          // deal with statistics
          thisstoretxnsdone = 0;
          storestxnsdone += storeTxnCounts[storei];
          printStats();
        }
      }
      log.println("Printed " + txncount + " txns.");
      // create master index of store index links
      log.println(createMasterIndex(archivepath + "/" + indexFilename(), forMasterIndex));
      log.println("Done. Took: " + DateX.millisToTime(sw.Stop()) + ".");
    } catch(Throwable ex) {
      log.println("Throwable: " + ex);
      log.println("Aborted.");
      dbg.Caught(ex);
    }
  }

  // print stats every 1000 txns
  private final void printStatsIf() {
    if(MathX.multipleOf(storestxnsdone + thisstoretxnsdone, 1000)) {
      printStats();
    }
  }

  private final void printStats() {
    int count = storestxnsdone + thisstoretxnsdone;
    log.println("Txns printed: " + count + "/" + alltxns + ", " +
                Math.round(MathX.ratio(count * 100,alltxns)) + "% completed.");
  }

  // returns the number of milliseconds it took
  private final long processStore() {
    StopWatch sw = new StopWatch();
    try {
      store = stores[storei];
      storedates[storei] = new TextList();
      storedayonly[storei] = LocalTimeFormat.New(store.getTimeZone(), "yyyyMMdd");
      storepaths[storei] = mkdirs(archivepath, storeDirname(store.id()));
      String storepath = storepaths[storei];
      linfo.web_login(new net.paymate.connection.ActionRequest(),
                      new net.paymate.connection.ClerkIdInfo("PMTECH", "12124180340"),
                      store.enterpriseid, false /*checkPerms*/);
      if(!linfo.loggedIn) {
        forMasterIndex.setInt(store.storename, -1);
        log.println("!!! COULD NOT LOG INTO STORE: " + store.storename + " (probably legacy store, not used)");
      } else {
        forMasterIndex.setInt(store.storename, store.storeId().value());
        StopWatch storetimer = new StopWatch();
        logininfo = linfo.forDisplay();
        // create filenames
        String indexFile = storepath + "/" + indexFilename();
        String batchesFile = storepath + "/" + batchesFilename();
        String drawersFile = storepath + "/" + drawersFilename();
        // create the index, with instructions
        log.println(createStoreIndex(store, indexFile));
        // create the subfiles -------------------------------------
        log.println(createBatches(store, batchesFile));
        log.println(createDrawers(store, drawersFile));
        txnsdone = false;
        // get a list of all drawers for this store, and create pages for each of them
        {
          log.println("Finding drawers ...");
          mkdirs(storepath, absoluteDrawerPath);
          DrawerRow drawer = DrawerRow.NewSet(db.runStoreDrawerQuery(store.storeId(), newWebDate)); // basically running it twice now
          int count = 0;
          log.println("Printing drawers ...");
          while(drawer.next()) {
            Drawerid drawerid = new Drawerid(drawer.drawerid);
            String drawerfile = storepath + "/" + drawerForDrawers(drawerid.toString());
            dbg.VERBOSE(createDrawer(drawerfile, drawerid));
            logInterval(++count, log);
          }
          log.println("Printed " + count + " drawers for store " + store.storename);
        }
        txnsdone = true;
        // get a list of all batches for this store, and create pages for each of them
        {
          log.println("Finding batches ...");
          mkdirs(storepath, absoluteBatchPath);
          BatchesRow batch = BatchesRow.NewSet(db.runStoreBatchQuery(store.storeId(), linfo.isaGod(), newWebDate)); // basically running it twice now
          int count = 0;
          log.println("Printing batches ...");
          while(batch.next()) {
            Batchid batchid = new Batchid(batch.batchid);
            String batchfile = storepath + "/" + batchForBatches(batchid.toString());
            dbg.VERBOSE(createBatch(batchfile, batchid));
            logInterval(++count, log);
          }
          log.println("Printed " + count + " batches for store " + store.storename);
        }
        { // DAILY SUMMARIES
          log.println("Printing daily summaries for " + store.storename + " ...");
          TextList dates = storedates[storei];
          LocalTimeFormat niceDate = LocalTimeFormat.New(store.getTimeZone(), "MM/dd/yyyy");
          dates.sort(false); // this puts them in descending order
          LocalTimeFormat dayonly = storedayonly[storei];
          String datesFile = storepath + "/" + datesFilename(); // one master html page w/ links for each day.
          log.println(createDates(store, datesFile, dates, dayonly, niceDate));
          mkdirs(storepath, absoluteDatePath);
          int count = 0;
          for(int i = dates.size(); i-- > 0; ) {
            String date = dates.itemAt(i);
            UTC start = dayonly.parseUtc(date);
            dbg.VERBOSE(createDay(store,
                                  storepath + "/" + dateForDates(date),
                                  start,
                                  niceDate.format(start)));
            logInterval(++count, log);
          }
          log.println("Printed " + dates.size() + " days for store " + store.storename);
        }
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return sw.Stop();
    }
  }

  private final static boolean logInterval(int count, PrintFork log) {
    if(MathX.multipleOf(count, PRINTINTERVAL)) {
      log.println("Printed " + count);
      return true;
    }
    return false;
  }

  private static final EasyProperties empty = new EasyProperties();

  private final String createBatch(String filename, Batchid batchid) {
    // output the master list of batches for this store
    String title = "Batch " + batchid;
    BatchPage page = new BatchPage(linfo, AdminOpCode.Prop, batchid, true);
    return Acct.writeDocToFile(filename, page);
  }

  private final String createDrawer(String filename, Drawerid drawerid) {
    String title = "Drawer " + drawerid;
    DrawerPage page = new DrawerPage(linfo, AdminOpCode.Prop, drawerid, true);
    return Acct.writeDocToFile(filename, page);
  }

  private final String createDay(Store store, String filename, UTC startdate, String nicedate) {
    String title = "Date: " + nicedate;
    TimeRange range = TimeRange.Create();
    UTC end = UTC.New(startdate.getTime());
    end.changeByDays(1);
    range.setStart(startdate);
    range.setEnd(end);
    TxnFilter txnfilter = new TxnFilter();
    txnfilter.time = range;
    TxnRow rec = db.findTransactionsBy(store.storeId(), txnfilter, Integer.MAX_VALUE);
    UnsettledTransactionFormat utf = new UnsettledTransactionFormat(linfo, rec, null, null, title, null, true, true);
    PayMatePage page = new GenericPage(title, logininfo, linfo);
    page.appendBody(utf);
    return Acct.writeDocToFile(filename, page);
  }

  private final String createMasterIndex(String filename, EasyProperties stores) {
    String title = "Store List";
    PayMatePage page = new GenericPage(title, logininfo, linfo);
    TextList allnames = stores.allKeys().sort();
    String [/*row*/][/*column*/] data = new String[stores.size()][1];
    for(int i = allnames.size(); i-->0;) {
      String storename = allnames.itemAt(i);
      Storeid storeid = new Storeid(stores.getInt(storename));
      if(storeid.isValid()) {
        data[i][0] = "<a href=\"" + storeDirname(storeid) + "/" + indexFilename() + "\">" + storename + "</a>";
      } else {
        data[i][0] = storename + " [COULD NOT LOG INTO STORE -- probably legacy store, not used]";
      }
    }
    HeaderDef [] headers = new HeaderDef[1];
    headers[0] = new HeaderDef(AlignType.LEFT, "Stores, in alphabetical order");
    Element t = ArrayTableGen.output(title, linfo.colors(), data, headers);
    page.appendBody("For PayMate.net internal use only.<BR><BR>");
    page.appendBody(t);
    return Acct.writeDocToFile(filename, page);
  }

  private final String createStoreIndex(Store store, String filename) {
    String title = "Store " + store.storename + " archive index";
    PayMatePage page = new GenericPage(title, logininfo, linfo);
    ElementContainer ec = new ElementContainer();

    H2 header = new H2();
    header.addElement("Archive for store: " + store.storename);
    ec.addElement(header);

    ec.addElement(BRLF);
    ec.addElement(BRLF);

    ec.addElement("To search for a transaction or receipt by date --> ").addElement(new A(datesFilename(), "Daily Listings"));
    ec.addElement(BRLF);
    ec.addElement("To view a drawer --> ").addElement(new A(drawersFilename(), "Drawers"));
    ec.addElement(BRLF);
    ec.addElement("To view a batch --> ").addElement(new A(batchesFilename(), "Batches"));
    ec.addElement(BRLF);

    page.appendBody(ec);
    return Acct.writeDocToFile(filename, page);
  }


  private final String createBatches(Store store, String filename) {
    // output the master list of batches for this store
    String title = "Store " + store.storename + " Batches";
    BatchesPage page = new BatchesPage(linfo, AdminOpCode.Prop, null, true);
    return Acct.writeDocToFile(filename, page);
  }

  private final String createDrawers(Store store, String filename) {
    String title = "Store " + store.storename + " Drawers";
    DrawersPage page = new DrawersPage(linfo, AdminOpCode.Prop, null, true);
    return Acct.writeDocToFile(filename, page);
  }

  private final String createDates(Store store, String filename, TextList dates,
                                   LocalTimeFormat dayonly, LocalTimeFormat niceDate) {
    String title = "Store " + store.storename + " Daily Listings";
    PayMatePage page = new GenericPage(title, logininfo, linfo);
    String [/*rows*/][/*cols*/] data = new String[dates.size()][1];
    for(int i = dates.size(); i-->0;) { // reverse order is descending order
      String date = dates.itemAt(i);
      data[i][0] = "<a href=\"" + dateForDates(date) + "\">" +
          niceDate.format(dayonly.parseUtc(date)) + "</a>";
    }
    HeaderDef [] headers = new HeaderDef[1];
    headers[0] = new HeaderDef(AlignType.LEFT,
                               "Daily listings, in reverse chronological order, in " +
                               store.getTimeZone().getDisplayName() + " timezone");
    Element t = ArrayTableGen.output(title, linfo.colors(), data, headers);
    page.appendBody(t);
    return Acct.writeDocToFile(filename, page);
  }

  private static final String mkdirs(String parent, String child) {
    File temp = new File(parent, child);
    temp.mkdirs();
    return temp.getAbsolutePath();
  }

  private Counter in = new Counter(); // prevent infinite loops
  public void loadedTxn(TxnRow rec) {
    if(in.value() > 0) {
      return;
    }
    try {
      in.incr();
      Txnid txnid = rec.txnid();
      int reci = txnid.value();
      if(printed[reci]) {
        // skip
        dbg.VERBOSE("Skipping printing txn/rcpt " + reci + " ...");
      } else {
        // the current store [storei] will be the correct store for this!
        dbg.WARNING("Printing txn/rcpt " + reci + " ...");
        // add date for store
        UTC time = StringX.NonTrivial(rec.clientreftime) ? rec.refTime() : UTC.New(rec.transtarttime); //UTC#
        TextList storedate = storedates[storei];
        LocalTimeFormat storeday = storedayonly[storei];
        String timeformat = storeday.format(time);
        storedate.assurePresent(timeformat);
        // write txn detail and rcpt files
        String txnpath = mkdirs(storepaths[storei], absoluteTxnPath);
        // output the Txn
        String txnfile = txnpath + "/" + txnFilename(rec.txnid);
        TransactionPage page = new TransactionPage(linfo, AdminOpCode.Prop, empty, rec.txnid(), true, rec);
        dbg.WARNING(Acct.writeDocToFile(txnfile, page));
        String rcptfile = txnpath + "/" + receiptFilename(rec.txnid);
        // output the receipt
        String msg = net.paymate.servlet.AdminServlet.printReceipt(rec, rcptfile, db, font) ?
            "Receipt created for txn " + txnid : "!!! Receipt could NOT be created for txn " + txnid;
        dbg.WARNING(msg);
        printed[reci]=true; // only print once
        txncount++;
        thisstoretxnsdone++;
        printStatsIf();
        if(txnsdone) {
          log.println("Printing txn when it should already have been done!!!");
        }
      }
    } catch (Throwable ex) {
      dbg.Caught(ex);
    } finally {
      in.decr();
    }
  }
}

class GenericPage extends PayMatePage {
  public GenericPage(String title, String loginInfo, LoginInfo linfo) {
    super(title, loginInfo, AdminOpCode.Prop, linfo, true);
  }
}
