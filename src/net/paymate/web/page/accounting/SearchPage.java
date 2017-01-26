/**
 * Title:        SearchPage
 * Description:  Used to find transactions based on rough estimates of data
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: SearchPage.java,v 1.10 2004/04/08 09:09:54 mattm Exp $
 */

package net.paymate.web.page.accounting;
import net.paymate.web.page.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
import net.paymate.data.*; // CardRange, etc.
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.util.*;
import  java.util.*;
import  net.paymate.database.*;//for search range types; TxnFilter
import net.paymate.lang.StringX;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.web.table.query.*;

public class SearchPage extends Acct {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SearchPage.class);

  public SearchPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(txnSearch(ezp, linfo));
  }

  public static final String card1tag = "card1"; // only search for a particular cardnow
//  public static final String card2tag = "card2";
  public static final String amount1tag = "amount1";
  public static final String amount2tag = "amount2";
  public static final String merch1tag = "merch1";
  public static final String merch2tag = "merch2";
  public static final String stan1tag = "stan1";
  public static final String stan2tag = "stan2";
  public static final String appr1tag = "appr1";
  public static final String appr2tag = "appr2";
  public static final String date1month = "d1mon";
  public static final String date1day = "d1day";
  public static final String date1year = "d1yr";
  public static final String date1hour = "d1hr";
  public static final String date1minute = "d1min";
  public static final String date2month = "d2mon";
  public static final String date2day = "d2day";
  public static final String date2year = "d2yr";
  public static final String date2hour = "d2hr";
  public static final String date2minute = "d2min";

  private static final String optional ="-";// "to (optional)";

  public static final Element defaultPage(String comment /* like instructions */, String url, String merchRefLabel) {
    Form form = NewPostForm(url);

    Input searchbutton = new Input(Input.SUBMIT, SUBMITBUTTON, "Search");
    TD td4 = new TD(searchbutton);
    td4.setAlign(AlignType.CENTER/*RIGHT*/).setColSpan(4);
    TR tr4 = new TR(td4);


    Table t = new Table();
    Element header=searchHeader();

    t.addElement(header);
    ElementContainer d1 = new ElementContainer();
    d1.addElement(input(Input.TEXT, date1month, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(input(Input.TEXT, date1day, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(input(Input.TEXT, date1year, "", 4, 4))
      .addElement(" ")
      .addElement(input(Input.TEXT, date1hour, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" : ")
      .addElement(input(Input.TEXT, date1minute, "", SIZEANDLENGTH, SIZEANDLENGTH));
    ElementContainer d2 = new ElementContainer();
    d2.addElement(input(Input.TEXT, date2month, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(input(Input.TEXT, date2day, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(input(Input.TEXT, date2year, "", 4, 4))
      .addElement(" ")
      .addElement(input(Input.TEXT, date2hour, "", SIZEANDLENGTH, SIZEANDLENGTH))
      .addElement(" : ")
      .addElement(input(Input.TEXT, date2minute, "", SIZEANDLENGTH, SIZEANDLENGTH));

    t.addElement(rowPrompt("Card Number (full or last4) ", Input.TEXT, card1tag, "",
                           null, null, null, null)) // we only do a card at a time now (no range)
//                           optional, Input.TEXT, card2tag, ""))
     .addElement(rowPrompt("Date [mm/dd/yyyy HH:MM] ", d1, optional, d2))
     // +++ make a link on the .Date prompt to show a table that shows how to convert to 24-hour time from AM/PM
     // +++ insert a tiny "current month" calendar, that stuffs the date field when days on it are clicked, with left and right arrows for skipping between months.
     .addElement(rowPrompt("Amount ", Input.TEXT, amount1tag, "", optional, Input.TEXT, amount2tag, ""))
     .addElement(rowPrompt("Txn # ", Input.TEXT, stan1tag, "", optional, Input.TEXT, stan2tag, ""))
     .addElement(rowPrompt(StringX.TrivialDefault(merchRefLabel, "Merchant Ref # "), Input.TEXT, merch1tag, "", optional, Input.TEXT, merch2tag, ""))
     .addElement(rowPrompt("Approval ", Input.TEXT, appr1tag, "", optional, Input.TEXT, appr2tag, ""));
     // +++ add selectors for store and terminal
    t.addElement(header);

    t.addElement(tr4);

    form.addElement(new Center(t));

    if(comment !=null) {
      form.addElement(BRLF)
          .addElement(comment)
          .addElement(BRLF);
    }

    return form;
  }

  private static final TR searchHeader() {
    TR tr  = new TR();
    tr.addElement(new TH("(exact if ending left blank)"));
    tr.addElement(new TH("Start"));
    tr.addElement(new TH(""));
    tr.addElement(new TH("End"));
    return tr;
  }

  public static ElementContainer txnSearch(EasyProperties req, LoginInfo linfo) {
    int LIMIT = linfo.assoc.browselines();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    TxnFilter tf=new TxnFilter();
    ElementContainer ec = new ElementContainer();
    String carder = req.getString(SearchPage.card1tag);
    tf.card=new CardRange(carder, carder);
    tf.amount=new MoneyRange(req.getString(SearchPage.amount1tag),req.getString(SearchPage.amount2tag));
    tf.stan =new StanRange(req.getString(SearchPage.stan1tag),req.getString(SearchPage.stan2tag));
    tf.appr =new AuthRange(req.getString(SearchPage.appr1tag),req.getString(SearchPage.appr2tag));
    tf.merch=new MerchRefRange(req.getString(SearchPage.merch1tag),req.getString(SearchPage.merch2tag));
    TimeZone tz = linfo.ltf().getZone();
    DateInput date1 = new DateInput(
      req.getString(SearchPage.date1year),
      req.getString(SearchPage.date1month),
      req.getString(SearchPage.date1day),
      req.getString(SearchPage.date1hour),
      req.getString(SearchPage.date1minute),
      null,
      tz);
    DateInput date2 = new DateInput(
      req.getString(SearchPage.date2year),
      req.getString(SearchPage.date2month),
      req.getString(SearchPage.date2day),
      req.getString(SearchPage.date2hour),
      req.getString(SearchPage.date2minute),
      null,
      tz);
    tf.setTimeRange(date1, date2);
    // now that we have built the filter, run the query
    TableGen utf = null;
    ec.addElement(PayMatePage.BRLF)
        .addElement(new Center(new Font().setSize("+2").addElement("Search")))
        .addElement(PayMatePage.BRLF);
    if(tf.NonTrivial()){
      // if parameters passed, do the search
      dbg.VERBOSE("TxnFilter is nontrivial");
      try {
        if (tf.card.NonTrivial() && !tf.card.isValid()) {
          ec.addElement("Card is not valid!").addElement(BRLF);
        }
        TxnRow stmt = db.findTransactionsBy(linfo.store.storeId(), tf, LIMIT);
        // filter out the cards that don't match !!! (hash and last4 could be the same, but card is different)
        // if the filter has a full card number in it, be sure and check every entry!
        // don't worry.  The filter query limits the number of results to 100.
        Vector rows = new Vector(100); // vector of properties
        while(stmt.next()) {
          if(! StringX.equalStrings(stmt.card().accountNumber.Image(), carder)) {
            rows.add(stmt.txnid());
          }
        }
        Txnid [ ] skiprows = new Txnid [rows.size()];
        for(int i = 0; i < skiprows.length; i++) {
          skiprows[i] = (Txnid)rows.elementAt(i);
        }
        dbg.ERROR("Attempt to rewind resultset did " + (stmt.rewind() ? "" : "NOT ") + "work!");
        utf = new UnsettledTransactionFormat(linfo, stmt, null, null,
                                             "Search Results (limited to 100 records)"
                                             /* +++ add more text to say what the search was for */,
                                             fullURL(AdminOp.SearchAdminOp.url()),
                                             true /* countLosses */, false /*we will never create this page in archive mode*/);
      } catch (Exception t) {
        dbg.Caught("generatePage: Exception performing transaction query!",t);
      }
    }
    if(utf == null) {
      // if no parameters passed or there was an error, give the parameters page
      // +++ and print the instructions, too
      ec.addElement(defaultPage(null/* for now --- */, key() + "?" +
                                           AdminOp.SearchAdminOp.url(),
                                           linfo.store.merchreflabel));
    } else {
      // return the results as a table
      ec.addElement(utf);
    }
    return ec;
  }


}
