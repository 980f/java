/**
* Title:        CardSubtotalsFormat<p>
* Description:  The canned format for the CaredSubtotals table<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: CardSubtotalsFormat.java,v 1.20 2004/02/07 07:13:15 mattm Exp $
*/

// +++ generalize this to handle any kind of subtotals ???

package net.paymate.web.table.query;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Txn
import  net.paymate.jpos.data.*; // CardNumber
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  java.io.OutputStream;
import net.paymate.lang.StringX;

public class CardSubtotalsFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CardSubtotalsFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new CardSubtotalsFormatEnum()).numValues()];
  static {
    theHeaders[CardSubtotalsFormatEnum.PayTypeCol] = new HeaderDef(AlignType. LEFT, "Payment Type");
    theHeaders[CardSubtotalsFormatEnum.InstitutionCol] = new HeaderDef(AlignType.LEFT, "Issuer");
    theHeaders[CardSubtotalsFormatEnum.InstSubCountCol] = new HeaderDef(AlignType.RIGHT, "Issuer Count");
    theHeaders[CardSubtotalsFormatEnum.InstSubTotalCol] = new HeaderDef(AlignType.RIGHT, "Issuer Total");
    theHeaders[CardSubtotalsFormatEnum.CountCol] = new HeaderDef(AlignType.RIGHT, "Count");
    theHeaders[CardSubtotalsFormatEnum.SumCol] = new HeaderDef(AlignType.RIGHT, "Total");
  }

  private static final int PAYTYPELEN = 1;
  private static final int INSTLEN    = 2;
  private static final int PAYTYPEINSTLEN = PAYTYPELEN+INSTLEN;

  private SubTotaller totaller = null;
  private TextList subtotalNames = null;
  public CardSubtotalsFormat(LoginInfo linfo, SubTotaller totaller, String title) {
    super(title, linfo.colors(), theHeaders, null);
    this.totaller = totaller;
  }

  protected RowEnumeration rows() {
    return this;
  }
  protected HeaderDef[] fabricateHeaders() {
    return headers;
  }
  public int numColumns() {
    return headers.length;
  }

  // overload the output function so that we can get the subtotallers names from reality
  // instead of from the storeauth table
  // (a change to the storeauth table doesn't mean old, no-longer-supported txn types shouldn't be displayed and counted!)
  public void output(OutputStream out) {
    primeNames();
    super.output(out);
  }

  private SubTotaller paytypesOnly = new SubTotaller();

  // hackish but effective
  private void primeNames() {
    TextList names = totaller.subtotalNames().sort();
    TextList newNames = new TextList();
    String lastpt = "";
    SubTotaller newTotaller = new SubTotaller();
    for(int i = names.size(); i-->0;) {
      String oldname = names.itemAt(i);
      String newname = StringX.fill(oldname, ' ', PAYTYPEINSTLEN, false);
      String pt = StringX.left(newname, PAYTYPELEN);
      Accumulator acc = paytypesOnly.getAccumulator(pt);
      if(!StringX.equalStrings(pt, lastpt)) { // add a paytype only if we need one
        newNames.add(pt); // ONLY 2 chars!  This is the mark of a paytype without institution!
        lastpt = pt;
      }
      newNames.add(newname);
      Accumulator oldacc = totaller.getAccumulator(oldname);
      Accumulator newacc = newTotaller.getAccumulator(newname);
      newacc.add(oldacc);
      acc.add(oldacc);
    }
    newTotaller.reCalcGrand();
    paytypesOnly.reCalcGrand();
    totaller = newTotaller;
    subtotalNames = newNames.sort();
    dbg.VERBOSE("names=\n"+subtotalNames);
  }

  public boolean hasMoreRows() {
    return currentRow < (subtotalNames.size() - 1);
  }
  private int currentRow = -1;
  String currentName = null;
  Accumulator currentAccumulator = null;

  private boolean light = false;
  private boolean justPaytype = false;
  int paytypecount = 0;

  public TableGenRow nextRow() {
    currentName = subtotalNames.itemAt(++currentRow);
    justPaytype = (currentName.length() == PAYTYPELEN);
    if (justPaytype) {
      paytypecount++;
    }
    currentAccumulator =
        justPaytype ?
        paytypesOnly.getAccumulator(currentName) :
        totaller.getAccumulator(currentName);
    light = (paytypecount % 2) == 0;
    return this;
  }
  public Element column(int col) {
    String str = " ";
    switch(col) {
      case CardSubtotalsFormatEnum.PayTypeCol: {
        if(justPaytype) {
          // convert the shortname to a longname:
          char chr = StringX.charAt(currentName, 0);
          PayType in = new PayType(chr);
          str = (in != null) ? in.Image() : "Unknown["+chr+"]";
        } else {
          str = "";//"&#160;&#160;&#160;|-";
        }
      } break;
      case CardSubtotalsFormatEnum.InstitutionCol: {
        if(justPaytype) {
          str = "";
        } else {
          // convert the shortname to a longname:
          String inst = StringX.right(currentName, INSTLEN);
          Institution in = CardIssuer.getFrom2(inst);
          str = (in != null) ? in.FullName() : "Unknown["+inst+"]";
        }
      } break;
      case CardSubtotalsFormatEnum.InstSubCountCol: {
        if(justPaytype) {
          str = "";
        } else {
          str = String.valueOf(currentAccumulator.getCount());
        }
      } break;
      case CardSubtotalsFormatEnum.InstSubTotalCol: {
        if(justPaytype) {
          str = "";
        } else {
          str = moneyImage(currentAccumulator.getTotal());
        }
      } break;
      case CardSubtotalsFormatEnum.CountCol: {
        if(justPaytype) {
          str = String.valueOf(currentAccumulator.getCount());
        } else {
          str = "";
        }
      } break;
      case CardSubtotalsFormatEnum.SumCol: {
        if(justPaytype) {
          str = moneyImage(currentAccumulator.getTotal());
        } else {
          str = "";
        }
      } break;
    }
    return new StringElement(str);
  }

   protected boolean light(int count) {
     return light;
   }

  protected int footerRows() {
    return 1;
  }

  private static String moneyImage(long money) {
    LedgerValue amount = new LedgerValue(UnsettledTransactionFormat.moneyformat); //+_+ to keep the drawer report and this report looking the same
    amount.setto(money);
    return amount.Image();
  }

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case CardSubtotalsFormatEnum.PayTypeCol: {
          ret = "TOTAL:";
        } break;
        case CardSubtotalsFormatEnum.CountCol: {
          ret = Long.toString(paytypesOnly.Count());
        } break;
        case CardSubtotalsFormatEnum.SumCol: {
          ret = moneyImage(paytypesOnly.Total());
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

}
//$Id: CardSubtotalsFormat.java,v 1.20 2004/02/07 07:13:15 mattm Exp $
