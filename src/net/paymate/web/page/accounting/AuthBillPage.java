package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/AuthBillPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.AuthStoreFullRow;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.AuthBillingReportFormat;
import net.paymate.data.sinet.business.*;

public class AuthBillPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthBillPage.class);

  public AuthBillPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(monthlyAuthReport(opcodeused, ezp));
  }

  // +++ allow multiple authids and multiple storeids and multiple months

  /**
   * Use the timezone of the store for now.
   *   // +++ throw this to a background thread, then have it email whoever when it is done, but have it say "in progress" now.
   *
   * +++ On the services page?
   *    Dropdown of all of the authorizers, and an ALL option at the top.
   *    Dropdown of all months in the system. eg: 200003 - 200204, in reverse order
   */
  private final ElementContainer monthlyAuthReport(AdminOpCode opcodeused, EasyProperties ezp) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    AuthManager authman = ConnectionServer.THE().authmgr;
    String root = OS.TempRoot();
    String month  = ezp.getString("month"); // YYYYMM
    Authid authid = new Authid(ezp.getInt("authid")); // if this is invalid, will do ALL auths.
    Storeid storeid = new Storeid(ezp.getInt("storeid")); // if this is invalid, will do ALL stores.

    AuthStoreFullRow authStoreFull = db.getFullAuthStore(authid, storeid);
    TextList results = new TextList();
    // +++ validate the month and auth first !!!!  Send back an error if either are invalid !!! +++

    while(authStoreFull.next()) { // Skip through the resultset and generate a page for each record
      Authid thisauth = authStoreFull.authid();
      Storeid thisstore = authStoreFull.storeid();
      Store store = StoreHome.Get(thisstore);
      Authorizer auth = authman.findAuthById(thisauth);
      // +++ in the future, just stuff a request into the report generator service and don't make the browser wait. +++
      // generate a report for this store + auth.
      // get the date range from the timezone and month
      // the easiest way to do this is to manually roll over the day, I think
      LocalTimeFormat utcto = LocalTimeFormat.Utc(AUTHRPTFORMAT);
      Date toroller = utcto.parse(month);
      Calendar cal = Calendar.getInstance(utcto.getZone());
      cal.setTime(toroller);
      cal.add(Calendar.MONTH, 1);
      String tomonthstr = utcto.format(cal.getTime())+"01000000"; // now, we are done with utcto
      String frommonthstr = month+"01000000";
      TimeRange daterange = TimeRange.Create();
      LocalTimeFormat ltf = LocalTimeFormat.New(store.timeZoneStr(), "yyyyMMddHHmmss");
      Date fromdate = ltf.parse(frommonthstr);
      Date todate   = ltf.parse(tomonthstr);
      UTC fromUTC = UTC.New(fromdate.getTime());
      UTC toUTC   = UTC.New(todate.getTime());
      daterange.include(fromUTC);
      daterange.include(toUTC);
      ltf = LocalTimeFormat.New(store.timeZoneStr(), store.receipttimeformat);
      String daterangestr = ltf.format(fromdate) + " - " + ltf.format(todate) + " " + store.timeZoneStr();
      // going to use again to output in the report ...
      ltf = LocalTimeFormat.New(store.timeZoneStr(), store.receipttimeformat);
      // generate the HEADER from the resultset contents and passed parameters.
      TextList merchantids = db.getMerchantIds(thisauth, authStoreFull.storeid());

      Table headerTable = new Table();
      headerTable.setBorder(0);
      headerTable.addElement((new TR()).addElement((new TD()).addElement("Authorizer:")).addElement((new TD()).addElement(authStoreFull.authname)));
      headerTable.addElement((new TR()).addElement((new TD()).addElement("Date:")).addElement((new TD()).addElement(daterangestr)));
      headerTable.addElement((new TR()).addElement((new TD()).addElement("PayMate Merchant Name:")).addElement((new TD()).addElement(store.storename)));
      headerTable.addElement((new TR()).addElement((new TD()).addElement("PayMate Merchant #:")).addElement((new TD()).addElement(String.valueOf(thisstore))));
      headerTable.addElement((new TR()).addElement((new TD()).addElement(authStoreFull.authname + " Merchant ID #'s:")).addElement((new TD()).addElement(merchantids.asParagraph(" / "))));
      String headerTableString = headerTable.toString();

      String subtitle = authStoreFull.authname + " [" + thisauth + "] / " +
          store.storename + " [" + thisstore + "] / " + daterangestr;
      String summarytitle = "Authorizer Summary Report: " + subtitle;
      String detailtitle = "Authorizer Detail Report: " + subtitle;
/*
* Each detail item will be for a batch closing.  All terminals for that store will be included.  That detail row will have the following columns:
    * Date/Time of batch - in the timezone of the store
    * BatchNumber
    * TerminalName
    * authtermid for that terminal
    * Counts (not amounts) for paytype * transfertype (CCSales, CCReturns, GCRedemptions, GCAddValues, GCBalanceInquiries, Voids; but only those with endauthtime != null)
    * A total count of all txns.
*/
      SubTotaller grandtotaller = new SubTotaller();
      AuthBillingReportFormat bodyElement =
          new AuthBillingReportFormat(ColorScheme.PLAIN, grandtotaller, "Details",
                                      ltf, thisauth, thisstore, daterange);
      StringElement body = new StringElement(bodyElement.toString()); // do the generation NOW so that the grandtotaller is ready!
/*
          *  The totals for each of those columns
          * A note about the per-txn rate (The rate ($0.0200 per for now) will be stored and displayed at 4 decimal places.)
          * Get the rates from the authorizer +++ auth.calculateTxnRate(TransferType tt, PayType pt, Institution in); use them to fill out the footer!
          * The total charge for the bill.
          * eg:
                * Description               Rate          * Count     =     Fee
                * CCSales                    0.0200          #                    $
                * CCReturns                  . . .
                * GCRedemptions
                * GCAddValues
                * GCBalanceInquiries
                * Voids
                * Settlements
                * =============
                * TTL
*/
      ElementContainer summary = new ElementContainer();

      Table outerFooterTable = new Table();
      summary.addElement(outerFooterTable);
      TR oftTR = new TR();
      outerFooterTable.addElement(oftTR);
      TD oftTDft = new TD();
      TD oftTDlegend = new TD();
      oftTR.addElement(oftTDft).addElement(oftTDlegend);

      Table footerTable = new Table();
      oftTDft.addElement(footerTable);
      footerTable.setBorder(1);
      footerTable.addElement((new THead()).
        addElement((new TH()).addElement("Description")).
        addElement((new TH()).addElement("Rate")).
        addElement((new TH()).addElement("* Count")).
        addElement((new TH()).addElement("= Fee")));
      TextList tl = grandtotaller.subtotalNames().sort();
      long totalFee = 0;
      LedgerValue ratePrinter = new LedgerValue("0.0000");
      LedgerValue feePrinter  = new LedgerValue("###,###,##0.0000");
      TextList ttlist = new TextList();
      TextList ptlist = new TextList();
      TextList inlist = new TextList();
      for(int i = 0; i < tl.size(); i++) {
        String name = tl.itemAt(i);
        TR tr = new TR();
        footerTable.addElement(tr);
        char tts = StringX.charAt(name, 0);
        char pts = StringX.charAt(name, 1);
        String ins = StringX.subString(name, 2, 4);
        long rate  = 0;
        if(StringX.equalStrings(name, AuthBillingReportFormat.SETTLEMENTKEY)) {
          tr.addElement((new TD()).addElement(AuthBillingReportFormat.SETTLEMENTKEY));
          rate  = (long)MathX.ratio(auth.calculateSubmittalRate(), 100); // ++++ @@@ %%% how to handle hundredths of a cent?
        } else {
          tr.addElement((new TD()).addElement(tts + " " + pts + " " + ins));
          TransferType tt = new TransferType(tts);
          PayType      pt = new PayType(pts);
          Institution  in = CardIssuer.getFrom2(ins);
          ttlist.assurePresent(tts);
          ptlist.assurePresent(pts);
          inlist.assurePresent(ins);
          rate  = (long)MathX.ratio(auth.calculateTxnRate(tt, pt, in), 100); // ++++ @@@ %%% how to handle hundredths of a cent?
        }
        long count = grandtotaller.getAccumulator(name).getTotal();
        long fee   = rate * count;
        totalFee += fee;
        tr.addElement((new TD()).addElement(ratePrinter.setto(rate).Image()).setAlign(AlignType.RIGHT)).
          addElement((new TD()).addElement(String.valueOf(count)).setAlign(AlignType.RIGHT)).
          addElement((new TD()).addElement(feePrinter.setto(fee).Image()).setAlign(AlignType.RIGHT));
      }
      footerTable.addElement((new TR()).
        addElement((new TD()).addElement("Total:")).
        addElement((new TD()).addElement(Entities.NBSP)).
        addElement((new TD()).addElement(""+grandtotaller.Total()/*totalCount*/).setAlign(AlignType.RIGHT)).
        addElement((new TD()).addElement(feePrinter.setto(totalFee).Image()).setAlign(AlignType.RIGHT)));
      // finish the legend
      Table legend = new Table();
      oftTDlegend.addElement(legend);
      THead legendTitle = new THead();
      legendTitle.addElement((new THead()).addElement(new TH("CODE")).addElement(new TH("Description")));
      legend.addElement(legendTitle);
      for(int i = 0; i < ttlist.size(); i++) {
        String code = ttlist.itemAt(i);
        TransferType tt = new TransferType(StringX.charAt(code, 0));
        legend.addElement(legendElement(code, tt.Image()));
      }
      for(int i = 0; i < ptlist.size(); i++) {
        String code = ptlist.itemAt(i);
        PayType pt = new PayType(StringX.charAt(code, 0));
        legend.addElement(legendElement(code, pt.Image()));
      }
      for(int i = 0; i < inlist.size(); i++) {
        String code = inlist.itemAt(i);
        Institution in = CardIssuer.getFrom2(code);
        if(in!=null) {
          legend.addElement(legendElement(code, in.FullName()));
        }
      }

      // now, write out to files ...
      String prefix = root + File.separator + "Month"+month+".Auth"+thisauth+".Store"+thisstore;
      String suffix = ".html";

      Document detaildoc = new Document();
      detaildoc.setDoctype(new Doctype.Html40Transitional()/*  new Doctype("HTML", "", "")*/)
          .appendTitle(detailtitle).appendTitle(BRLF)
          .appendBody(headerTableString)
          .appendBody(BRLF)
          .appendBody(new HR())
          .appendBody(body.addElement(BRLF));
      String detailfilename = prefix+"-DETAIL"+suffix; // might write over one if it is a redo
      results.add(writeDocToFile(detailfilename, detaildoc));

      Document summarydoc = new Document();
      summarydoc.setDoctype(new Doctype.Html40Transitional()/*  new Doctype("HTML", "", "")*/)
          .appendTitle(summarytitle).appendTitle(BRLF)
          .appendBody(headerTableString)
          .appendBody(BRLF)
          .appendBody(new HR())
          .appendBody(summary.addElement(BRLF));
      String summaryfilename = prefix+"-SUMMARY"+suffix; // might write over one if it is a redo
      results.add(writeDocToFile(summaryfilename, summarydoc));
    }
    ElementContainer ec = new ElementContainer();
    ec.addElement("Report results: ").addElement(BRLF).addElement(results.asParagraph(BRLF)).addElement(BRLF);
    return ec;
  }
  private static final String AUTHRPTFORMAT = "yyyyMM";
}