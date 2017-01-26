package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/DrawersPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
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
import net.paymate.web.table.query.DrawerClosingFormat;
import net.paymate.database.ours.query.DrawerRow;
import net.paymate.web.table.query.CardSubtotalsFormat;

public class DrawersPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DrawersPage.class);

  public DrawersPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezc, boolean archive) {
    super(linfo, opcodeused, archive);
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    TimeRange newWebDate = null;
    if(archive) {
      newWebDate = TimeRange.Forever(); // full range of dates for this customer!!!
    } else {
      TimeRange lastDate = TimeSearch(db.mostRecentStoreDrawer(linfo.store.storeId()), linfo.ltf());
      newWebDate = EntryForm(ec, ezc, lastDate, linfo.ltf().getZone(),
                                       this, AdminOp.DrawersAdminOp.url());
    }
    if(newWebDate != null) {
      DrawerRow set = DrawerRow.NewSet(db.runStoreDrawerQuery(linfo.store.storeId(), newWebDate));
      SubTotaller totaller = new SubTotaller(); // new stuff
      DrawerClosingFormat utf = new DrawerClosingFormat(linfo, set, "Drawer Closings", totaller, archive);
      if(utf != null) { // this is the new stuff
        ec.addElement(utf);
        if(linfo.store.enlistsummary && ! archive) {
          CardSubtotalsFormat utf2 = new CardSubtotalsFormat(linfo, totaller, "Summary");
          if(utf2 != null) {
            ec.addElement(utf2);
          }
        }
      }
    }
    fillBody(ec);
  }

  public static final TimeRange EntryForm(ElementContainer ec,
                                          EasyProperties ezp,
                                          TimeRange lastDate,
                                          TimeZone tz,
                                          Acct mypage,
                                          String adminOpUrl) {
    TimeRange webDate = TimeSearch(ezp, tz);
    TimeRange newWebDate = CheckDate(ec, webDate, lastDate);
    ec.addElement(displayForm(fullURL(adminOpUrl), newWebDate,
                              lastDate, tz, mypage));
    return newWebDate;
  }

  public static TimeRange TimeSearch(UTC webdate, LocalTimeFormat ltf) {
    TimeZone tz = ltf.getZone();
    DateInput date1 = new DateInput(webdate, tz);
    date1.nullTime();//beginningOfDay(); // sets the time to 00:00:00.000
    // build the range; since date2 is null, will use date from date1 and ENDOFDAY (which is beginning of next day)
    TimeRange tr = TimeFilter.createTimeRange(date1);
    if(TimeRange.NonTrivial(tr)) {
      return tr;
    } else {
      return TimeRange.makeSafe(null);
    }
  }

  public static TimeRange TimeSearch(EasyProperties ezp, TimeZone tz) {
    DateInput date1 = new DateInput(
        ezp.getString(SearchPage.date1year),
        ezp.getString(SearchPage.date1month),
        ezp.getString(SearchPage.date1day),
        null, null, null, // beginning Of Day
        tz);
    return TimeFilter.createTimeRange(date1);
  }

  public static final TimeRange CheckDate(ElementContainer ec, TimeRange startdatetr, TimeRange lastdatetr) {
    TimeRange ret = null;
    if(TimeRange.NonTrivial(startdatetr)) {
      if(startdatetr.NonTrivial()) {
        ret = startdatetr;
      } else {
        ec.addElement("<B><CENTER>Invalid date.  Please reenter.</CENTER></B><BR><BR>");
      }
    }
    if(ret == null) {
      ret = TimeRange.copy(lastdatetr);
    }
    return ret;
  }
  public static final Form displayForm(String url, TimeRange theday, TimeRange last,
                                       TimeZone custtz, Acct mypage) {
    // build the date entry cell
    ElementContainer d1 = new ElementContainer();

    DateInput todayYMD = ymdForUTC(UTC.Now(), 0, custtz);
    DateInput nextYMD = ymdForUTC(theday.start(), +1, custtz);
    DateInput previousYMD = ymdForUTC(theday.start(), -1, custtz);
    DateInput lastYMD = ymdForUTC(last.start(), 0, custtz);
    DateInput theYMD = ymdForUTC(theday.start(), 0, custtz);

    d1.addElement(input(Input.TEXT, SearchPage.date1month, theYMD.month, SIZEANDLENGTH, SIZEANDLENGTH))
        .addElement(" / ")
        .addElement(input(Input.TEXT, SearchPage.date1day, theYMD.day, SIZEANDLENGTH, SIZEANDLENGTH))
        .addElement(" / ")
        .addElement(input(Input.TEXT, SearchPage.date1year, theYMD.year, 4, 4))
        .addElement("  ")
        .addElement(new Input(Input.SUBMIT, SUBMITBUTTON, "GO"));
    // build the links
    Element todayLink    = mypage.makeLink(dateAwayURL(todayYMD, url), "TODAY");
    Element nextLink     = mypage.makeLink(dateAwayURL(nextYMD, url), "next >");
    Element previousLink = mypage.makeLink(dateAwayURL(previousYMD, url), "< previous");
    Element lastLink     = mypage.makeLink(dateAwayURL(lastYMD, url), "LAST");
    // build the cells
    TD goCell = new TD()
        .addElement(d1)
        .setAlign("CENTER");
    String sep = " - ";
    TD navigateCell = new TD()
        .addElement(previousLink)
        .addElement(sep)
        .addElement(lastLink)
        .addElement(sep)
        .addElement(todayLink)
        .addElement(sep)
        .addElement(nextLink)
        .setAlign("CENTER");
    // build the row
    TR tr = new TR()
        .addElement(navigateCell)
        .addElement(goCell);
    // build the table
    Table t = new Table();
    t.addElement(tr);
    t.setWidth("100%");
    // build the form
    Form form = NewPostForm(url).addElement(t);
    return form;
  }
  private static final DateInput ymdForUTC(UTC date, int changeby, TimeZone tz) {
    return DateInput.fromUTC(UTC.ChangeByDays(date, changeby), tz);
  }
  private static final String dateAwayURL(DateInput ymd, String prefixUrl) {
    return prefixUrl+"&"+SearchPage.date1month+"="+ymd.month+"&"+SearchPage.date1day+"="+ymd.day+"&"+SearchPage.date1year+"="+ymd.year;
  }
}
