/**
* Title:        UnsettledTransactionFormat<p>
* Description:  The canned query for the Unsettled Transactions screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: UnsettledTransactionFormat.java,v 1.82 2001/11/17 06:17:00 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Tranjour
import  net.paymate.ISO8583.data.*; // transctionid, expirationdate
import  net.paymate.jpos.data.*; // CardNumber
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements

public class UnsettledTransactionFormat extends RecordFormat {
  private static final ErrorLogStream dbg = new ErrorLogStream(UnsettledTransactionFormat.class.getName(), ErrorLogStream.WARNING);

  // +++ make a static function to generate a page of all of the possible "status"es, and maybe make an enumeration of them.
  // This page would describe each status and what it means.
  // There would be a link on the "status" header to the page.
  // The page would come up in a separate, smaller window.

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new UnsettledTransactionFormatEnum()).numValues()];
  static {
    theHeaders[UnsettledTransactionFormatEnum.TimeCol]     = new HeaderDef(AlignType.LEFT   , "Time");
    theHeaders[UnsettledTransactionFormatEnum.TermCol]     = new HeaderDef(AlignType.LEFT   , "Term"); // +++ put a link to the terminals table, with the specific one highlighted
    theHeaders[UnsettledTransactionFormatEnum.StanCol]     = new HeaderDef(AlignType.RIGHT  , "Txn #");
    theHeaders[UnsettledTransactionFormatEnum.SiCol]       = new HeaderDef(AlignType.CENTER , "si");
    theHeaders[UnsettledTransactionFormatEnum.StatusCol]   = new HeaderDef(AlignType.CENTER , "Status");
    theHeaders[UnsettledTransactionFormatEnum.ApprovalCol] = new HeaderDef(AlignType.RIGHT  , "Approval");
    theHeaders[UnsettledTransactionFormatEnum.AcctNumCol]  = new HeaderDef(AlignType.RIGHT  , "Account");
    theHeaders[UnsettledTransactionFormatEnum.ExpDateCol]  = new HeaderDef(AlignType.LEFT   , "Exp[M/Y]");
    theHeaders[UnsettledTransactionFormatEnum.SaleCol]     = new HeaderDef(AlignType.RIGHT  , "Sale");
    theHeaders[UnsettledTransactionFormatEnum.ReturnCol]   = new HeaderDef(AlignType.RIGHT  , "Return");
    theHeaders[UnsettledTransactionFormatEnum.SumCol]     =  new HeaderDef(AlignType.RIGHT  , "Net");
  }

  protected boolean voidable = false;
  private TxnRow tranjour = null;

  public static final String NOVOIDMARKER = "NV"; // until we get a better way to do this!
//above is used by usersession...
  /**
   * @Param voidable - tells the class whether or not to put up the void option,
   * so the class can be used to display old batches (from the history table instead of tranjour table)
   */
  public UnsettledTransactionFormat(LoginInfo linfo, TxnRow tranjour, boolean voidable, String title, String absoluteURL, int howMany, String sessionid) {
    super(linfo.colors, title, tranjour, absoluteURL, howMany, sessionid, linfo.ltf);
    this.tranjour = tranjour;
    this.voidable = voidable;
    HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
    System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
    myHeaders[UnsettledTransactionFormatEnum.TimeCol].title = new StringElement("Time (" + ltf.getZone().getID() + ")");
    headers = myHeaders;
  }

  public static final String moneyformat = "#0.00";

  private LedgerValue saleTotal = new LedgerValue(moneyformat);
  private int  saleCount    = 0; // qty
  private LedgerValue returnTotal  = new LedgerValue(moneyformat);
  private int  returnCount  = 0; // qty
  private LedgerValue otherTotal   = new LedgerValue(moneyformat);
  private int  otherCount   = 0; // qty
  private int  count        = 0; // number of txns (qty) +_+ why long???
  private LedgerValue net   = new LedgerValue(moneyformat);
  private int  sumCount     = 0; // qty in net.

/**
 * these record the range of the query. Seems like a job for ... TimeRanger!
 */
  private TimeRange span=PayMateDB.TimeRange();

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      LedgerValue amount = new LedgerValue(moneyformat); //+_+ to keep the drawer report and this report looking the same
      Fstring stanstr = new Fstring(5,'0');
      if(tgr != null) {
        // do the real data//+_+ move the following into TranJour
        boolean wasReversed = tranjour.wasVoided();
        boolean isReturn   = tranjour.isReturn();
        boolean isStoodin  = tranjour.wasStoodin();
        boolean inProgress = !Safe.NonTrivial(tranjour.actioncode); // +++ put on TranjorRow
        boolean isDeclined = !("A".equals(tranjour.actioncode)) && !inProgress;  // +++ put on TranjorRow
// --- testing for stoodins showing on webpage
        boolean didTransfer= !wasReversed && ((!isDeclined) || isStoodin) && ((!inProgress) || isStoodin);  // +++ put on TranjorRow
        boolean isStrike   = !didTransfer;

        count++;
        java.util.Date time = tranjour.refTime();//UTC#
        span.include(time);
        String localDTime = ltf.format(time); //UTC#
//following is used for linking to receipt: (should be in tranjour)
        TransactionID tid = tranjour.tid();

        setColumn(UnsettledTransactionFormatEnum.TimeCol, strikeText(localDTime, isStrike));
        setColumn(UnsettledTransactionFormatEnum.TermCol, strikeText(tranjour.cardacceptortermid, isStrike));
        String stan = tid.stan();
        stanstr.righted(stan);
//        setColumn(UnsettledTransactionFormatEnum.StanCol, new A(Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.d)).Image() + "&tid=" + tid.image() + (voidable ? "&"+NOVOIDMARKER+"=1":""), strikeText(stanstr.toString(), isStrike)));
        setColumn(UnsettledTransactionFormatEnum.StanCol, new A("javascript:viewtransaction('" + tid.image() + "', '" + stanstr.toString() + "')",  strikeText(stan, isStrike)));
        setColumn(UnsettledTransactionFormatEnum.SiCol, strikeText(tranjour.stoodinstan, isStrike));
        setColumn(UnsettledTransactionFormatEnum.ApprovalCol, strikeText(tranjour.authidresponse, isStrike));
        setColumn(UnsettledTransactionFormatEnum.AcctNumCol, strikeText(tranjour.cardGreeked(), isStrike));
        setColumn(UnsettledTransactionFormatEnum.ExpDateCol, strikeText(tranjour.expiry().Image(), isStrike));

        amount.setto(tranjour.rawamount());//unsigned amount
        if(isReturn) {
          setColumn(UnsettledTransactionFormatEnum.ReturnCol, strikeText(amount.Image(), isStrike));
          if(didTransfer) {
            returnTotal.add(amount);
            returnCount++;
          }
          amount.changeSign(); //negative for all other uses
        } else {
          setColumn(UnsettledTransactionFormatEnum.SaleCol, strikeText(amount.Image(), isStrike));
          if(didTransfer) {
            saleTotal.add(amount);
            saleCount++;
          }
        }
        if(didTransfer) {
          setColumn(UnsettledTransactionFormatEnum.SumCol, amount.Image());
          net.add(amount);
          sumCount++;
        } else {
          otherTotal.add(amount);
          otherCount++;
        }
        String status = "";

        // the order of this sequence is important !
        if(wasReversed) {
          status = "VOIDED";
        } else if(isDeclined) {
          status = isStoodin ? "LOSS" : "DECLINED";
        } else if(inProgress) {
          status = isStoodin ? "PEND/SI" : "PENDING";
        } else {
          // everything else list nothing (APPROVED)
        }
        setColumn(UnsettledTransactionFormatEnum.StatusCol, status);
      } else {
        dbg.WARNING("RecordFormat.next() returned null!");
      }
    } catch (Exception t2) {
      dbg.Caught("generating next row content",t2);
    } finally {
      dbg.Exit();
      return (tgr == null) ? null : this;
    }
  }

  protected int footerRows() {
    return 1;
  }
  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case UnsettledTransactionFormatEnum.TimeCol: {
          ret = hasMore() ? "SUBTOTALS:" : "TOTALS:";
          ret += " <i>(" + Safe.millisToTime(span.milliSpan())+")</i>";
        } break;
        case UnsettledTransactionFormatEnum.StanCol: {
          ret = Long.toString(count);
        } break;
        case UnsettledTransactionFormatEnum.ApprovalCol: {
          ret = " = " + Long.toString(count-otherCount);
        } break;
        case UnsettledTransactionFormatEnum.ReturnCol: {
          ret = "+ ["+returnCount+"] " + returnTotal.Image();
        } break;
        case UnsettledTransactionFormatEnum.SaleCol: {
          ret = "["+saleCount+"] " + saleTotal.Image();
        } break;
        case UnsettledTransactionFormatEnum.SumCol: {
          ret = " = ["+sumCount+"] " + net.Image();//compare to sale+return
        } break;
        case UnsettledTransactionFormatEnum.StatusCol: {
          ret = "- " + Long.toString(otherCount);
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

}

/* +++ document elsewhere ...
<SCRIPT LANGUAGE="javascript">
function viewtransaction(txnid, label) {
  winStats = 'toolbar=no,location=no,directories=no,status=yes,menubar=no,';
  winStats+= 'scrollbars=yes,width=700,height=400,left=10,top=25,';
  winStats='';
  urlString = "http://localhost/servlets/admin/Acct?adm=d&tid=" + txnid + "&NV=1";
  window.open(urlString ,"Transaction_"+label,winStats);
}
</script>

<A HREF="javascript:viewtransaction('20010625014855*000000000076001*2472', '2472')">2472</A>




open
Opens a new web browser window.

Method of Window

Implemented in

Navigator 2.0
Navigator 4.0: added several new windowFeatures

Syntax

open(URL, windowName, windowFeatures)
Parameters

URL
A string specifying the URL to open in the new window.
See the Location object for a description of the URL components.

windowName
A string specifying the window name to use in the TARGET attribute of a FORM or A tag.
windowName can contain only alphanumeric or underscore (_) characters.

windowFeatures
(Optional) A string containing a comma-separated list determining whether
or not to create various standard window features. These options are described below.

Description
In event handlers, you must specify window.open() instead of simply using open().
Due to the scoping of static objects in JavaScript,
a call to open() without specifying an object name is equivalent to document.open().

The open method opens a new Web browser window on the client,
similar to choosing New Navigator Window from the File menu of the browser.
The URL argument specifies the URL contained by the new window.
If URL is an empty string, a new, empty window is created.

You can use open on an existing window, and if you pass the empty string for the URL,
you will get a reference to the existing window, but not load anything into it.
You can, for example, then look for properties in the window.

windowFeatures is an optional string containing a comma-separated list of options
for the new window (do not include any spaces in this list).
After a window is open, you cannot use JavaScript to change the windowFeatures.
The features you can specify are:

alwaysLowered
(Navigator 4.0) If yes, creates a new window that floats below other windows, whether it is active or not.
This is a secure feature and must be set in signed scripts.

alwaysRaised
(Navigator 4.0) If yes, creates a new window that floats on top of other windows, whether it is active or not.
This is a secure feature and must be set in signed scripts.

dependent
(Navigator 4.0) If yes, creates a new window as a child of the current window.
A dependent window closes when its parent window closes.
On Windows platforms, a dependent window does not show on the task bar.

directories
If yes, creates the standard browser directory buttons, such as What's New and What's Cool.

height
(Navigator 2.0 and 3.0) Specifies the height of the window in pixels.

hotkeys
(Navigator 4.0) If no (or 0), disables most hotkeys in a new window that has no menu bar.
The security and quit hotkeys remain enabled.

innerHeight
(Navigator 4.0) Specifies the height, in pixels, of the window's content area.
To create a window smaller than 100 x 100 pixels, set this feature in a signed script.
This feature replaces height, which remains for backwards compatibility.

innerWidth
(Navigator 4.0) Specifies the width, in pixels, of the window's content area.
To create a window smaller than 100 x 100 pixels, set this feature in a signed script.
This feature replaces width, which remains for backwards compatibility.

location
If yes, creates a Location entry field.

menubar
If yes, creates the menu at the top of the window.

outerHeight
(Navigator 4.0) Specifies the vertical dimension, in pixels, of the outside boundary of the window.
To create a window smaller than 100 x 100 pixels, set this feature in a signed script.

resizable
If yes, allows a user to resize the window.

screenX
(Navigator 4.0) Specifies the distance the new window is placed from the left side of the screen.
To place a window offscreen, set this feature in a signed scripts.

screenY
(Navigator 4.0) Specifies the distance the new window is placed from the top of the screen.
To place a window offscreen, set this feature in a signed scripts.

scrollbars
If yes, creates horizontal and vertical scrollbars when the Document grows larger than the window dimensions.

status
If yes, creates the status bar at the bottom of the window.

titlebar
(Navigator 4.0) If yes, creates a window with a title bar.
To set the titlebar to no, set this feature in a signed script.

toolbar
If yes, creates the standard browser toolbar, with buttons such as Back and Forward.

width
(Navigator 2.0 and 3.0) Specifies the width of the window in pixels.

z-lock
(Navigator 4.0) If yes, creates a new window that does not rise above other windows when activated.
This is a secure feature and must be set in signed scripts.

Many of these features (as noted above) can either be yes or no.
For these features, you can use 1 instead of yes and 0 instead of no.
If you want to turn a feature on, you can also simply list the feature name in the windowFeatures string.

If windowName does not specify an existing window and you do not supply the windowFeatures parameter,
all of the features which have a yes/no choice are yes by default.
However, if you do supply the windowFeatures parameter,
then the titlebar and hotkeys are still yes by default,
but the other features which have a yes/no choice are no by default.

For example, all of the following statements turn on the toolbar option and turn off all other Boolean options:

open("", "messageWindow", "toolbar")
open("", "messageWindow", "toolbar=yes")
open("", "messageWindow", "toolbar=1")
The following statement turn on the location and directories options and turns off all other Boolean options:

open("", "messageWindow", "toolbar,directories=yes")
How the alwaysLowered, alwaysRaised, and z-lock features behave depends on the windowing hierarchy of the platform.
For example, on Windows, an alwaysLowered or z-locked browser window is below all windows in all open applications.
On Macintosh, an alwaysLowered browser window is below all browser windows,
but not necessarily below windows in other open applications.
Similarly for an alwaysRaised window.

You may use open to open a new window and then use open on that window
to open another window, and so on.
In this way, you can end up with a chain of opened windows,
each of which has an opener property pointing to the window that opened it.

Communicator allows a maximum of 100 windows to be around at once.
If you open window2 from window1 and then are done with window1,
be sure to set the opener property of window2 to null.
This allows JavaScript to garbage collect window1.
If you do not set the opener property to null, the window1 object remains,
even though it's no longer really needed.

Security
To perform the following operations using the specified screen features,
you need the UniversalBrowserWrite privilege:

To create a window smaller than 100 x 100 pixels
or larger than the screen can accommodate
by using innerWidth, innerHeight, outerWidth, and outerHeight.

To place a window off screen by using screenX and screenY.

To create a window without a titlebar by using titlebar.

To use alwaysRaised, alwaysLowered, or z-lock for any setting.
For information on security in Navigator 4.0,
see Chapter 7, "JavaScript Security," in the JavaScript Guide.

Examples
Example 1. In the following example, the windowOpener function opens a window
and uses write methods to display a message:

function windowOpener() {
   msgWindow=window.open("","displayWindow","menubar=yes")
   msgWindow.document.write
      ("<HEAD><TITLE>Message window</TITLE></HEAD>")
   msgWindow.document.write
      ("<CENTER><BIG><B>Hello, world!</B></BIG></CENTER>")
}
Example 2. The following is an onClick event handler that opens a new client window
displaying the content specified in the file sesame.html.
The window opens with the specified option settings;
all other options are false because they are not specified.

<FORM NAME="myform">
<INPUT TYPE="button" NAME="Button1" VALUE="Open Sesame!"
   onClick="window.open ('sesame.html', 'newWin',
   'scrollbars=yes,status=yes,width=300,height=300')">
</FORM>
See also
Window.close

*/

//$Id: UnsettledTransactionFormat.java,v 1.82 2001/11/17 06:17:00 mattm Exp $
