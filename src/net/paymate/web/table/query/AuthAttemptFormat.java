/**
* Title:        AuthAttemptFormat<p>
* Description:  The canned query for the Auth Attempt (Authorizer Messages) screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: AuthAttemptFormat.java,v 1.13 2004/02/19 10:15:29 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Txn
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo, UserSession
import  net.paymate.web.table.*; //DBTableGen
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import net.paymate.lang.StringX;
import  net.paymate.data.UniqueId;

public class AuthAttemptFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthAttemptFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AuthAttemptFormatEnum()).numValues()];
  static {
    theHeaders[AuthAttemptFormatEnum.AuthCol]     = new HeaderDef(AlignType.LEFT , "Processor");
    theHeaders[AuthAttemptFormatEnum.RequestCol]  = new HeaderDef(AlignType.LEFT , "Request");
    theHeaders[AuthAttemptFormatEnum.ResponseCol] = new HeaderDef(AlignType.LEFT , "Response");
    theHeaders[AuthAttemptFormatEnum.StartTimeCol]= new HeaderDef(AlignType.LEFT , "Time");
    theHeaders[AuthAttemptFormatEnum.TraceCol]    = new HeaderDef(AlignType.RIGHT, "Msg#");
    theHeaders[AuthAttemptFormatEnum.TxnCol]      = new HeaderDef(AlignType.RIGHT, "Txn trace");
  }

  public AuthAttemptFormat(LoginInfo linfo, AuthAttemptRow row, String title) {
    super(linfo.colors(), title, row, null, linfo.ltf());
    this.row = row;
    // don't show the request column if this person is not gawdlike
    isagawd = linfo.isaGod();
    HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
    System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
    headers = myHeaders;
    if(!isagawd) {
      headers[AuthAttemptFormatEnum.RequestCol] = new HeaderDef(AlignType.LEFT, "");
    }
  }

  private boolean isagawd = false;
  private AuthAttemptRow row = null;
  private int requests  = 0;
  private int responses = 0;
  private int txns = 0;
  private Ticks ticker = new Ticks();
  private UTC time = UTC.Now(); // used in nextRow()
  private UTC endtime = UTC.Now(); // used in nextRow()

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      if(tgr != null) {
        time.setto(row.authstarttime);
        String diff = "--";
        if(StringX.NonTrivial(row.authendtime)) {
          endtime.setto(row.authendtime);
          diff = ""+Ticks.toIntSeconds(endtime.getTime() - time.getTime());
        }
        String localDTime = ltf.format(time) + " +" + diff; //UTC#
        setColumn(AuthAttemptFormatEnum.StartTimeCol, localDTime);
        setColumn(AuthAttemptFormatEnum.AuthCol, row.authname);
        if(isagawd) { // only show requests if gawdlike user
          byte [ ] before = DataCrypt.databaseDecode(row.authrequest, new UniqueId(row.authattemptid));
          EasyUrlString eus = new EasyUrlString();
          eus.setrawto(before);
          setColumn(AuthAttemptFormatEnum.RequestCol,
                    new TextList(eus).asParagraph("<BR>\n"));
          if (StringX.NonTrivial(row.authrequest)) {
            requests++;
          }
        }
        setColumn(AuthAttemptFormatEnum.ResponseCol, easyURL2web(row.authresponse));
        if(StringX.NonTrivial(row.authresponse)) {
          responses++;
        }
        setColumn(AuthAttemptFormatEnum.TraceCol, row.authattemptid);
        boolean isTxn = StringX.NonTrivial(row.txnid);
        if(isTxn) {
          txns++;
        }
        String url = net.paymate.web.page.Acct.txnUrl(row.txnid);
        String txnurl = isTxn ? new A(url, row.txnid).toString() : "";
        setColumn(AuthAttemptFormatEnum.TxnCol, txnurl);
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
        case AuthAttemptFormatEnum.StartTimeCol: {
          ret = "COUNT:";
        } break;
        case AuthAttemptFormatEnum.RequestCol: {
          ret = isagawd ? (""+requests) : ""; // only show req count if gawd
        } break;
        case AuthAttemptFormatEnum.ResponseCol: {
          ret = "" + responses;
        } break;
        case AuthAttemptFormatEnum.TxnCol: {
          ret = "" + txns;
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

}

//$Id: AuthAttemptFormat.java,v 1.13 2004/02/19 10:15:29 mattm Exp $
