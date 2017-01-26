package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/AuthAttemptsPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

import net.paymate.web.page.*;
import net.paymate.web.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.lang.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.TimeZone;
import net.paymate.web.table.query.*;
import net.paymate.util.*;

public class AuthAttemptsPage extends Acct {
  public AuthAttemptsPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(authAttempts(ezp, linfo));
  }

  private static final String TERMINALWIDGET = "TERMINAL";
  private static final String DATEWIDGET = "DATE";

  public ElementContainer authAttempts(EasyProperties ezp, LoginInfo linfo) {
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    TimeZone tz = linfo.ltf().getZone();
    // check to see what the terminal and date constraints are
    // if you didn't get any, do the first terminal in the list (alphabetically ascending), and today
    Terminalid terminalid = new Terminalid(ezp.getInt(TERMINALWIDGET));
    Terminalid [ ] terminalids = db.getTerminalidsForStore(linfo.store.storeId());
    // is this one of our terminals?  Find out.  If not, get the first one.
    boolean is = false;
    for(int i = terminalids.length; i-->0;) {
      if(terminalids[i].value() == terminalid.value()) {
        is = true;
        break;
      }
    }
    if(!is) {
     if(terminalids.length > 0) {
      terminalid = terminalids[0];
     } else {
      terminalid = new Terminalid(); // just to be safe
     }
    }
    // setup the date ...
    DateInput tempnow = DateInput.Now(tz); // used as a default in case there weren't any values sent
    String reqyear = StringX.TrivialDefault(ezp.getString(SearchPage.date1year), tempnow.year);
    String reqmonth = StringX.TrivialDefault(ezp.getString(SearchPage.date1month), tempnow.month);
    String reqday = StringX.TrivialDefault(ezp.getString(SearchPage.date1day), tempnow.day);
    DateInput starttime = new DateInput(reqyear,reqmonth,reqday,"00","00",null,tz);
    starttime.beginningOfDay(); // beginning of day to end of day
    DateInput endtime = new DateInput(starttime); // makes a copy
    endtime.beginningNextDay(); // set it to the beginning of the next date ///// end of day, though
    TimeRange tr = TimeRange.Create();
    tr.include(starttime.toUTC());
    tr.include(endtime.toUTC());
    String datestr = reqmonth+"/"+reqday+"/"+reqyear;// +++ need to define the format for time differnt from the format for day so that we can specidy that we want a day only printed, and how
    // define the form
    TR tr2 = new TR();
    TR tr3 = new TR();
    String url = Acct.key() + "?" + AdminOp.authMsgsOp.url();
    Form f = NewPostForm(url).
        addElement(new Table().setCellSpacing(4).setCellPadding(0).
                   setBorder(0).addElement(tr2).addElement(tr3));
    Option [] terminals = new Option[terminalids.length];
    for(int i = 0; i < terminals.length; i++) {
      Terminalid terminal = terminalids[i];
      terminals[i] = new Option(terminal.toString()).addElement(db.getTerminalInfo(terminal).getNickName());
      if(terminalid.equals(terminal)) {
        terminals[i].setSelected(true);
      }
    }
    tr2.addElement(new TD().addElement("Terminal: ").addElement(new Select(TERMINALWIDGET, terminals)));
    ElementContainer d1 = new ElementContainer();
    d1.addElement(PayMatePage.input(Input.TEXT, SearchPage.date1month, reqmonth, PayMatePage.SIZEANDLENGTH, PayMatePage.SIZEANDLENGTH))
      .addElement(" / ")
      .addElement(PayMatePage.input(Input.TEXT, SearchPage.date1day, reqday, PayMatePage.SIZEANDLENGTH, PayMatePage.SIZEANDLENGTH))
      .addElement(" / 20 ")
      .addElement(PayMatePage.input(Input.TEXT, SearchPage.date1year, reqyear, PayMatePage.SIZEANDLENGTH, PayMatePage.SIZEANDLENGTH));
    tr3.addElement(new TD().addElement("Day [MM/DD/YYYY]: ").addElement(d1));
    tr3.addElement(new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Display")));
    // and then output the content and widgets
    String title = "Authorizer Messages for terminal " + db.getTerminalInfo(terminalid).getNickName() + " on " + datestr;
    // since the screen should only have one day's worth of txns on one terminal, don't paginate
    ec.addElement(f).addElement(new AuthAttemptFormat(
        linfo, db.getAuthAttempts(terminalid, tr), title));
    return ec;
  }
}