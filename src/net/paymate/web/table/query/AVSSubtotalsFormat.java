/**
* Title:        CardSubtotalsFormat<p>
* Description:  The canned format for the CaredSubtotals table<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: AVSSubtotalsFormat.java,v 1.1 2004/02/07 07:13:15 mattm Exp $
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

public class AVSSubtotalsFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AVSSubtotalsFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AVSSubtotalsFormatEnum()).numValues()];
  static {
    theHeaders[AVSSubtotalsFormatEnum.InstitutionCol] = new HeaderDef(AlignType.LEFT, "Issuer");
    theHeaders[AVSSubtotalsFormatEnum.AVSCodeCol] = new HeaderDef(AlignType.RIGHT, "AVS Code");
    theHeaders[AVSSubtotalsFormatEnum.AVSDescriptionCol] = new HeaderDef(AlignType.LEFT, "Description");
    theHeaders[AVSSubtotalsFormatEnum.CountCol] = new HeaderDef(AlignType.RIGHT, "Count");
    theHeaders[AVSSubtotalsFormatEnum.SumCol] = new HeaderDef(AlignType.RIGHT, "Total");
  }

  private static final int AVSCODELEN = 1;
  private static final int INSTLEN    = 2;
  private static final int INSTAVSCODELEN = INSTLEN+AVSCODELEN;

  private SubTotaller totaller = null;
  private TextList subtotalNames = null;
  public AVSSubtotalsFormat(LoginInfo linfo, SubTotaller totaller, String title) {
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
    subtotalNames = totaller.subtotalNames();
    subtotalNames.sort();
    dbg.VERBOSE("names=\n"+subtotalNames);
    super.output(out);
  }

  public boolean hasMoreRows() {
    return currentRow < (subtotalNames.size() - 1);
  }
  private int currentRow = -1;
  String currentName = null;
  Accumulator currentAccumulator = null;

  private boolean light = false;
  private boolean justInstitution = false;
  private int institutioncount = 0;
  private Accumulator nullAccumulator = new Accumulator();

  public TableGenRow nextRow() {
    currentName = subtotalNames.itemAt(++currentRow);
    justInstitution = (currentName.length() == INSTLEN);
    currentAccumulator = justInstitution ?
        nullAccumulator : totaller.getAccumulator(currentName);
    if (justInstitution) {
      institutioncount++;
    }
    light = (institutioncount % 2) == 0;
    dbg.VERBOSE("accumulator for [" + currentName + "] = "+currentAccumulator.toString());
    return this;
  }
  public Element column(int col) {
    String str = " ";
    switch(col) {
      case AVSSubtotalsFormatEnum.AVSCodeCol: {
        str = justInstitution ? "" : StringX.subString(currentName, INSTLEN, INSTAVSCODELEN);
      } break;
      case AVSSubtotalsFormatEnum.AVSDescriptionCol: {
        str = justInstitution ? "" : AVSDecoder.AVSDecode(StringX.left(currentName, INSTLEN), StringX.subString(currentName, INSTLEN, INSTAVSCODELEN));
      } break;
      case AVSSubtotalsFormatEnum.InstitutionCol: {
        if(justInstitution) {
          // convert the shortname to a longname:
          String inst = StringX.left(currentName, INSTLEN);
          Institution in = CardIssuer.getFrom2(inst);
          str = (in != null) ? in.FullName() : "Unknown["+inst+"]";
        } else {
          str = "";
        }
      } break;
      case AVSSubtotalsFormatEnum.CountCol: {
        str = justInstitution ? "" : String.valueOf(currentAccumulator.getCount());
      } break;
      case AVSSubtotalsFormatEnum.SumCol: {
        LedgerValue amount = new LedgerValue(UnsettledTransactionFormat.moneyformat); //+_+ to keep the drawer report and this report looking the same
        amount.setto(currentAccumulator.getTotal());
        str = justInstitution ? "" : amount.Image();
      } break;
    }
    return new StringElement(str);
  }

   protected boolean light(int count) {
     return light;
   }
}
//$Id: AVSSubtotalsFormat.java,v 1.1 2004/02/07 07:13:15 mattm Exp $
