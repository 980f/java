/**
* Title:        CardSubtotalsFormat<p>
* Description:  The canned format for the CaredSubtotals table<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: CardSubtotalsFormat.java,v 1.2 2001/10/17 23:59:01 mattm Exp $
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

public class CardSubtotalsFormat extends RecordFormat {
  private static final ErrorLogStream dbg = new ErrorLogStream(CardSubtotalsFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new CardSubtotalsFormatEnum()).numValues()];
  static {
    theHeaders[CardSubtotalsFormatEnum.CardTypeCol] = new HeaderDef(AlignType.LEFT   , "Card Type");
    theHeaders[CardSubtotalsFormatEnum.CountCol]    = new HeaderDef(AlignType.RIGHT  , "Count");
    theHeaders[CardSubtotalsFormatEnum.SumCol]      = new HeaderDef(AlignType.RIGHT  , "Total");
  }

  CardSubtotalsRow rower = null;
  public CardSubtotalsFormat(LoginInfo linfo, CardSubtotalsRow rower, String title) {
    super(linfo.colors, title, rower, null, -1, null, linfo.ltf);
    this.rower = rower;
    headers = theHeaders;
  }

  public static final String moneyformat = "#0.00";

  private LedgerValue net   = new LedgerValue(moneyformat);
  private int  sumCount     = 0; // qty in net.

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      LedgerValue amount = new LedgerValue(moneyformat); //+_+ to keep the drawer report and this report looking the same
      if(tgr != null) {
        setColumn(CardSubtotalsFormatEnum.CardTypeCol, rower.paymenttypename);
        setColumn(CardSubtotalsFormatEnum.CountCol, rower.counter);
        amount.setto(rower.rawamount());//unsigned amount
        setColumn(CardSubtotalsFormatEnum.SumCol, amount.Image());
        net.add(amount);
        sumCount+=Integer.parseInt(rower.counter);
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
        case CardSubtotalsFormatEnum.CardTypeCol: {
          ret = "TOTAL:";
        } break;
        case CardSubtotalsFormatEnum.CountCol: {
          ret = Long.toString(sumCount);
        } break;
        case CardSubtotalsFormatEnum.SumCol: {
          ret = net.Image();
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

}
//$Id: CardSubtotalsFormat.java,v 1.2 2001/10/17 23:59:01 mattm Exp $
