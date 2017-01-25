package net.paymate.database;
/**
* Title:        PayMateDB<p>
* Description:  structured queries for the DBMS (etc)<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: PayMateDB.java,v 1.37 2001/11/17 20:06:36 mattm Exp $
*/

import  net.paymate.database.*;
import  net.paymate.util.*;
import  net.paymate.database.ours.query.*;
import  net.paymate.database.ours.*;
import  net.paymate.database.ours.table.*;
import  net.paymate.connection.*;
import  net.paymate.ISO8583.data.*;
import  net.paymate.web.LoginInfo;
import  java.io.File;
import  java.util.Date; //explicit to resolve conflict between this and java.sql.date
import  java.sql.*;
import  java.text.*;
import  java.io.*;
import  java.util.*;
import  net.paymate.terminalClient.*;
import  net.paymate.ISO8583.data.LedgerValue; // amounts
import  net.paymate.data.*;
import  net.paymate.awtx.*;
import  net.paymate.jpos.data.*;
import  net.paymate.util.timer.*;
import  net.paymate.authorizer.*; //getStoodins(StandinList...)

public class PayMateDB extends DBMacros implements DBConstants, Database {
  private static final ErrorLogStream dbg = new ErrorLogStream(PayMateDB.class.getName());

  private static final String EMPTYSTRING = "";
  private static final String SPACE = " ";

  public final static int NOTANUMBER = -1; //we should choose another value, like 0x800000000

  public PayMateDB(DBConnInfo connInfo) {
    super(connInfo);
  }

  public final static int LASTAUTOSTAN=99999; //last stan we can use in server
  // made symbolic as we may share stan range with client stand-in.
  ///////////////////////////////////////
  //  protected static TimeZone tz;
  /**
  * formatter for transtarttime field
  */

  //////////////////////
  // transtarttime is our major time reference for transactions.
  //////// Time/date stuff

  public final static String transtarttimeFormat = "yyyyMMddHHmmss";//maimsail's choice
  protected static final LocalTimeFormat trantime=LocalTimeFormat.Utc(transtarttimeFormat);
/**
 * can't have a default range, we use this in places that must start with an empty range
 */
  public static final TimeRange TimeRange(){
    return TimeRange.Create(transtarttimeFormat,TRANSTARTTIME);
  }

/**
 * @return a timerange for the lifetime of the database
 */

  public static final TimeRange ForAllTime(){
    return TimeRange.Create(transtarttimeFormat,TRANSTARTTIME).
    setStart(LocalTimeFormat.genesis).setEnd(Safe.Now());
  }

//  private static final Monitor tranTimeMonitor = new Monitor("PayMateDB.trantime");

  /**
  * @param dbvalue string value of time from databse transstartime field or equivalent
  */
  public static final Date tranUTC(String dbvalue){
    return TransactionTime.tranUTC(dbvalue);
  }
  /**
  * convert @param date into string for transtarttime field
  */
  public static final String forTrantime(Date utc){
    return TransactionTime.forTrantime(utc);
  }
  /**
  * convert @param utcTix into string for transtarttime field
  */

  public static final String forTrantime(long utcTix){
    return forTrantime(new Date(utcTix));
  }

  /**
   * default format for time displays at stores.
   * (PLEASE make i tbe a fixed legnth regardless of time/date!)
   */

  ////////////////////

  public static final QueryString StoreIs(int storenum){
    return QueryString.Clause(EMPTYSTRING).nvPair(STOREID,storenum);
  }

  public static final QueryString TimeIs(String tstime){
    return QueryString.Clause(EMPTYSTRING).nvPair(TRANSTARTTIME,tstime);
  }

  public static final QueryString TimeAfter(String time){
    return QueryString.Clause(EMPTYSTRING).
    Open("").nGTEQv(TRANSTARTTIME,time).Close();
  }

  public static final QueryString ApplianceIs(String applidnum){
    return QueryString.Clause(EMPTYSTRING).nvPair(APPLIANCEID,applidnum);
  }

  public static final QueryString TerminalIs(int termidnum){
    return QueryString.Clause(EMPTYSTRING).nvPair(TERMINALID,termidnum);
  }

  public static final QueryString TerminalIs(TerminalID T) {
    return QueryString.Clause(EMPTYSTRING).append(TerminalIs(T.terminalID)).and().append(StoreIs(T.storeid));
  }

  public static final QueryString TerminalNamed(String nickname){
    return QueryString.Clause(EMPTYSTRING).nvPair(TERMINALNAME,nickname);
  }

  public static final QueryString ApprovedTxns(){
    QueryString toReturn = QueryString.Clause(EMPTYSTRING).
      Open(""). // new ...
        Open("").
          nvPair(ACTIONCODE,"A").
        Close().
        or().
        Open("").
          not().isEmpty("tranjour."+/* --- possible cause of bug !!! STOODINSTAN --- */CLIENTREFTIME).//nvPair("tranjour."+/* --- possible cause of bug !!! STOODINSTAN --- */CLIENTREFTIME, "").
        Close().
      Close().
      and().append(NoVoids()).
      and().not().nvPair("voidtransaction","Y");
      dbg.VERBOSE("ApprovedTxns() returning: " + toReturn);
    return toReturn;
  }

  public static final QueryString NoVoids(){
    return  QueryString.Clause(EMPTYSTRING).not().nvPair(MESSAGETYPE,"0400");
  }


  // +++ put these into the querystring !!! ???
  public static final QueryString StoreAgrees(){
    return  QueryString.Clause(EMPTYSTRING).matching(store.name(),terminal.name(),STOREID);
  }

  /////////////////////////////////////////////////
  protected static final QueryString tiftQuery(TxnRow rec){//excised for testing
    return QueryString.Select(TERMINALID).from(terminal.name()).
    where(TerminalNamed(rec.cardacceptortermid)).and(StoreIs(Safe.parseInt(rec.storeid)));
  }

  public TerminalInfo getTerminalInfo(int terminalId) {
    TerminalInfo ti = new TerminalInfo(terminalId);
    Statement stmt =null;
    try {
      stmt = query(genTerminalInfoQuery(terminalId));
      ResultSet rs = getResultSet(stmt);
      if(next(rs)) {
        ti=TermFromRS(rs);
        ti.si = storeFromRS(rs);
      }
    } catch(Exception arf){
      dbg.ERROR("Swallowed in getTerminalInfo:"+arf);
    } finally {
      closeStmt(stmt);
      return ti;
    }
  }

  public QueryString genTerminalInfoQuery(int terminalID) {
    return QueryString.
    SelectAllFrom("terminal").comma(store.name()).
    where().nnPair("store."+STOREID, "terminal."+STOREID).
    and().nvPair("terminal."+TERMINALID, terminalID);
  }

  public int getOriginalTerminal(TxnRow rec) {
    return getIntFromQuery(tiftQuery(rec));
  }

//  public TerminalInfo termInfoFromTranjour(TxnRow rec) {
//    String terminalid = getStringFromQuery(tiftQuery(rec));//cabove
//    return getTerminalInfo(terminalid);
//  }

  public static final QueryString genStoreInfo(int storeid){
    return QueryString.SelectAllFrom(store.name()).where(StoreIs(storeid));
  }

  public double getStoreMaxTxnLimit(int storeid) {
    return getDoubleFromQuery(genStoreMaxTxnLimit(storeid), 0);
  }

  public static final QueryString genStoreMaxTxnLimit(int storeid) {
    return QueryString.Select("maxtransamount").
      from("paytype").
      where().nvPair("storeid", storeid).
      and().nvPair("paymenttypecode", "VS"); // just use Visa's
  }

  public double getStoreMaxSITxnLimit(int storeid) {
    return getDoubleFromQuery(genStoreMaxSITxnLimit(storeid), 0);
  }

  public static final QueryString genStoreMaxSITxnLimit(int storeid) {
    return QueryString.Select("standinlimit").
      from("store").
      where().nvPair("storeid", storeid);
  }

  public double getStoreMaxSITtlTxnLimit(int storeid) {
    return getDoubleFromQuery(genStoreMaxSITtlTxnLimit(storeid), 0);
  }

  public static final QueryString genStoreMaxSITtlTxnLimit(int storeid) {
    return QueryString.Select("storestandintotal").
      from("store").
      where().nvPair("storeid", storeid);
  }

  public static final QueryString genTerminal(int termid){
    return QueryString.SelectAllFrom(terminal.name()).comma(enterprise.name()).comma(store.name()).where(TerminalIs(termid)).and().matching(terminal.name(),store.name(),STOREID).and().matching(store.name(), enterprise.name(), ENTERPRISEID);
  }

  static final QueryString assocQry(LoginInfo li){
    // --- Note: possible bug
    return QueryString.Select("storeaccess.storeacl").comma("associate.*").
    from(storeaccess.name()).comma(terminal.name()).comma(associate.name()).comma(enterprise.name()).
    where().nvPair("storeaccess.storeid",li.storeid).
    and().matching("associate","storeaccess","associateid").
    and().matching("associate",enterprise.name(),"enterpriseid").
    and().nvPair("associate.loginname",li.clerk.Name()).
    and().nvPair("enterprise.enterpriseid",li.enterpriseID).
    and().nvPair("associate.encodedpw",li.clerk.Password());
  }

  // temporary
  int getStoreFromEnterprise(int eid) {
    return getIntFromQuery(QueryString.Select("storeid").from(store.name()).where().nvPair(store.enterpriseid.name(),eid));
  }

  /**
   * return 0=NOLOGIN, 1=GOODLOGIN, 2+ =MOREINFONEEDED (failed due to too many matches)
   */
  public int getLoginInfo(ClerkIdInfo clerk, int enterpriseID, int terminalID, LoginInfo li) {
    int ret = 0;
    li.clear(); // so that legacy stuff doesn't hang around ... just in case
    li.clerk        = clerk;
    li.enterpriseID = enterpriseID;
    li.terminalID   = terminalID;

    // terminalid != 0 for appliances, enterpriseid != 0 for webusers

    try {
      // if there is a terminalid, this is an appliance (not web) login, so get the storeid and terminalinfo
      if(li.terminalID > 0) {
        // first, get the terminal info:
        Statement stmTerminal=null;
        try {
          stmTerminal = query(genTerminal(li.terminalID));
          ResultSet rs = getResultSet(stmTerminal);
          if(next(rs)) { /// shouldn't be multiples since terminalid's are supposed to be unique
            li.storeid      = getIntFromRS(STOREID, rs);
            li.terminalName = getStringFromRS(TERMINALNAME, rs);
            li.enterpriseID = getIntFromRS(ENTERPRISEID, rs); // set this so that the login can proceed
            li.ti=TermFromRS(rs);
          }
        } catch (Exception e) {
          //+++ log to events table. as loginfailure
          dbg.Caught("Error retreiving terminal info for login; user=" + clerk.toSpam() +", termguid=" + terminalID + ", entid="+enterpriseID+":", e);
        } finally {
          closeStmt(stmTerminal);
        }
      }
      if(li.enterpriseID < 1) {
        // give it a shot ...
        QueryString countem = QueryString.Select("count(distinct enterpriseid)").from(associate).where().nvPair("loginname", li.clerk.Name());
        int count = getIntFromQuery(countem, 0);
        if(count > 1) {
          ret = count;
        } else if(count == 1){
          li.enterpriseID = getIntFromQuery(QueryString.Select("enterpriseid").from(associate).where().nvPair("loginname", li.clerk.Name()), 0);
        } // else you are screwed; bad login ...
      }
      if(ret < 2) {
        if((li.enterpriseID > 0) && (li.storeid > 0)) {
          // get the storeid with that
          // +++ This is temporary!  We need to get the rest of multistore going before changing this.
          li.storeid = getStoreFromEnterprise(li.enterpriseID);
        }
        // we need this in order to be able to get the store info
        if(li.storeid>0) {
          // now the store info
          Statement stmStore=null;
          try {
            stmStore = query(genStoreInfo(li.storeid));
            if(stmStore!=null) {
              ResultSet rs = getResultSet(stmStore);
              if(next(rs)) {
                li.enterpriseID = getIntFromRS(store.enterpriseid.name(), rs); // --- overwrites, is this okay?  conditionalize it ? +++
                String timefmt  = getStringFromRS(store.receipttimeformat.name(), rs);
                String javatz   = getStringFromRS(store.javatz.name(), rs);
                li.ltf=LocalTimeFormat.New(TimeZone.getTimeZone(javatz), Safe.OnTrivial(timefmt, ReceiptFormat.DefaultTimeFormat));
                dbg.VERBOSE("Timezone set to " + li.ltf.getZone().getDisplayName() + " from " + timefmt + " for user " + li.clerk.Name());
                li.authTermId = getStringFromRS(store.authtermid.name(), rs);
                li.companyName = getStringFromRS(store.storename.name(), rs);
              }
            }
          } catch (Exception e) {
            //+++ log to events table. as loginfailure
            dbg.Caught("Error retreiving store info for login; user=" + li.clerk.toSpam() +", termguid=" + li.terminalID + ", entid="+li.enterpriseID+", storeid="+li.storeid+":", e);
          } finally {
            closeStmt(stmStore);
          }
          // we need this in order to get the user login info
  //        if(li.enterpriseID > 0) {
            // now the permissions, etc, from the associate
            Statement stmUser=null;
            try {
              stmUser = query(assocQry(li));
              if(stmUser!=null) {
                ResultSet rs = getResultSet(stmUser);
                if(next(rs)) {
                  li.permissions   = getStringFromRS("storeacl", rs) + getStringFromRS("enterpriseacl", rs);
                  li.longName      = getStringFromRS("firstname", rs) + SPACE + getStringFromRS("lastname", rs);//middle initial ignored +_+
                  li.colorschemeid = getStringFromRS("colorschemeid", rs); // from associate
                  li.associd      = getIntFromRS("associateid", rs);
                }
              }
            } catch (Exception e) {
              //+++ log to events table. as loginfailure
              dbg.Caught("Error retreiving user info for login; user=" + li.clerk.toSpam() +", termguid=" + li.terminalID + ", entid="+li.enterpriseID+", storeid="+li.storeid+":", e);
            } finally {
              closeStmt(stmUser);
            }
  //        }
        }
        if((li.storeid > 0) && Safe.NonTrivial(li.permissions)) {
          ret = 1;
          // how many did you get?
          //+++ log to events table. as ClerkLogin, even if same clerk as previous login.
        }
      }
    } catch (Exception caught) {
      dbg.ERROR("Exception retreiving login info for user=" + clerk.toSpam() +", termguid=" + terminalID + ":");
      //+++ log to events table. as loginfailure
      dbg.Caught(caught);
    } finally {
      return ret;
    }
  }

  public String enterpriseFromTerminalID(String terminalID) {
    String result = getStringFromQuery(genEnterpriseFromTerminalQuery(terminalID), 0);
    return result;
  }

  /**
  * get tranjour record from a standinin.
  *ins: terminalid storeid orignalstanid time
  *out: stan
  */
  public QueryString findTransactionBy(LoginInfo linfo, TransactionID stoodin){
    return QueryString.SelectAllFrom(tranjour.name()).
    where(). nvPair(CARDACCEPTORTERMID,linfo.terminalName).
    and().   nvPair(STOREID,linfo.storeid).
    and().   nvPair(STOODINSTAN,stoodin.stan()).
    and().   nvPair(CLIENTREFTIME,stoodin.time)
    ;
  }

  /**
   * first used for storing receipts associated with a stoodin request.
   */
  public TransactionID findStandin(TransactionID stoodin,LoginInfo linfo){
    Statement stmt=null;
    try {
      stmt = query(findTransactionBy(linfo,stoodin));
      ResultSet rs = getResultSet(stmt);
      if(next(rs)) {
        stoodin = TransactionID.New(rs.getString(TRANSTARTTIME),rs.getString(STAN),linfo.storeid);
      }
    } catch(Exception ex){
      // ++
    } finally {
      closeStmt(stmt);
      return stoodin;
    }
  }

  private QueryString genMarkRecordUpdate(TransactionID tid,FinancialRequest request){
    QueryString qs = QueryString.
    Update(tranjour.name()).
    SetJust(CLIENTREFTIME, forTrantime(request.requestInitiationTime) ).
    where(transactionIs(tid, tranjour.name()));
    return qs;
  }

  public boolean markClientTime(TransactionID tid,FinancialRequest request){
    QueryString qs = genMarkRecordUpdate(tid,request);
    if(update(qs) != 1) {
      dbg.ERROR("markClientTime(): Did not properly insert: " + qs);
      return false;
    } else {
      dbg.VERBOSE("markClientTime(): Properly inserted: " + qs);
      return true;
    }
  }

  // returns the result from update()
  // +++ put a mutexed database call here when we switch to using integer txnid's.
  // +++ How do we get the txnid back?  Look it up by the parameters used to create it, for now.
  // @@@ use the return id to get the txnid !!!
  public int startTxn(TxnRow record) {
    int result = 0;
    result = update(genStartTxn(record));
    return result;
  }

  public static QueryString genStartTxn(TxnRow record) {
    return QueryString.Insert(tranjour.name(), record.toProperties());
  }

  public static QueryString genAuthIds() {
    return QueryString.Select("authid").from("authorizer");
  }

  public String [] getAuthIds() {
    return getStringsFromQuery(genAuthIds());
  }

  public static final QueryString genAuthId(String authname) {
    return QueryString.Select("authid").from("authorizer").where().nvPair("authname", authname);
  }

  public int getAuthId(String authname) {
    return getIntFromQuery(genAuthId(authname));
  }

  // +++ for DBMacros ???
  public static final void rowsToProperties(ResultSet rs, EasyCursor ezc, String nameColName, String valueColName) {
    try {
      if(rs != null) {
        while(next(rs)) {
          String name  = getStringFromRS(nameColName , rs);
          String value = getStringFromRS(valueColName, rs);
          ezc.setString(name, value);
        }
      } else {
        dbg.ERROR("Can't convert rows to properties if resultset is null!");
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  public static final QueryString genServiceParams(String serviceName) {
    return QueryString.Select("paramname").comma("paramvalue").from("servicecfg").where().nvPair("servicename", serviceName);
  }

  public EasyCursor getServiceParams(String serviceName) {
    EasyCursor ezc = new EasyCursor();
    Statement stmt = null;
    try {
      stmt = query(genServiceParams(serviceName));
      rowsToProperties(getResultSet(stmt), ezc, "paramname", "paramvalue");
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      closeStmt(stmt);
      return ezc;
    }
  }

  public static final QueryString genServiceParam(String serviceName, String paramname) {
    return QueryString.Select("paramvalue").from("servicecfg").where().nvPair("servicename", serviceName).and().nvPair("paramname", paramname);
  }

  public String getServiceParam(String serviceName, String paramname, String defaultValue) {
    return Safe.TrivialDefault(getStringFromQuery(genServiceParam(serviceName, paramname)), defaultValue);
  }

  public QueryString genAuthClassFromId(int authid) {
    return QueryString.Select("authclass").from("authorizer").where().nvPair("authid", authid);
  }

  public String getAuthClass(int authid) {
    return getStringFromQuery(genAuthClassFromId(authid));
  }

  public QueryString genAuthNameFromId(int authid) {
    return QueryString.Select("authname").from("authorizer").where().nvPair("authid", authid);
  }

  public String getAuthName(int authid) {
    return getStringFromQuery(genAuthNameFromId(authid));
  }

  public QueryString genAuthIdForStore(int storeid) {
    return QueryString.Select("authid").from("store").where().nvPair("storeid", storeid);
  }

  public int getAuthIdForStore(int storeid) {
    return getIntFromQuery(genAuthIdForStore(storeid));
  }

  public static final QueryString genAuthIdForApplianceName(String applianceName) {
    return QueryString.Select("authid").from("store").comma("appliance").where().matching("store", "appliance", "storeid").and().nvPair("appliancename", applianceName);
  }

  public int getAuthIdForApplianceName(String applianceName) {
    return getIntFromQuery(genAuthIdForApplianceName(applianceName));
  }

  public QueryString genChangeAuth(TxnRow record, int authid) {
    return QueryString.Update(tranjour.name()).
      SetJust("authid", authid).
      where().append(transactionIs(record.tid(), tranjour.name()));
  }

  public void changeAuth(TxnRow record, int authid) {
    update(genChangeAuth(record, authid)); // +++ check return value
  }

  public void stampAuthDone(TxnRow record, TxnRow original) {
    // stamp the new txn as completed successfully
    record.authendtime = forTrantime(Safe.Now());
    update(genStampAuthDone(record)); // +++ check return value
    if((original != null) && record.isGood()) {
      // stamp original as voided if record was an approved void
      original.voidtransaction = "Y";
      update(genStampVoidTxn(original)); // +++ check return value; txn might be in history!
    }
  }

  public static QueryString genStampVoidTxn(TxnRow original) {
    String table = tranjour.name();
    QueryString qs = QueryString.
      Update(table).
      SetJust("voidtransaction", original.voidtransaction).
      where(transactionIs(original.tid(), table));
    return qs;
  }

  // this is only interested in respcode, authcode, and ?
  public static QueryString genStampAuthDone(TxnRow record) {
    String table = tranjour.name();
    QueryString qs = QueryString.
      Update(table).
      SetJust("actioncode", record.actioncode).comma("").
      nvPair("authidresponse", record.authidresponse).comma("").
      nvPair("hostresponsecode", record.hostresponsecode).comma("").
      nvPair("hosttracedata", record.hosttracedata).comma("").
      nvPair("responsecode", record.responsecode).comma("").
      nvPair("authendtime", record.authendtime).comma("").
      nvPair("tranendtime", record.tranendtime). // stamps tranendtime & authendtime
      where(transactionIs(record.tid(), table));
    return qs;
  }

  public void stampAuthStandin(TxnRow record) {
    // stamp the record.authendtime and record.authstarttime with null
    record.authendtime = "";
    record.authstarttime = "";
    update(genStampAuthStandin(record)); // +++ check return value
  }

  public static QueryString genStampAuthStandin(TxnRow record) {
    String table = tranjour.name();
    QueryString qs = QueryString.
      Update(table).
      SetJust("authendtime", "").comma("").// stamps authendtime & authstarttime as ""
      nvPair("authstarttime", "").comma("").
      nvPair("clientreftime", record.clientreftime).comma("").
      nvPair("stoodinstan", record.stoodinstan).
      where(transactionIs(record.tid(), table));
    return qs;
  }

//  public void setAuthSeq(TxnRow record) {
//    // just sets the authseq field based on the new sequence (overwrites)
//    update(genSetAuthSeq(record)); // +++ check return value
//  }

  public static final QueryString genSetAuthSeq(TxnRow record) {
    int seq = (int)Long.parseLong(record.authseq);
    String table = tranjour.name();
    QueryString qs = QueryString.
      Update(table).
      SetJust("authseq", record.authseq).
      where(transactionIs(record.tid(), table));
    return qs;
  }

  public void getStoodins(StandinList stoodins, String authname) {
    // load the list of txns that need processing from tables, sticking each into the list
    QueryString qs = genStoodins(authname);
    Statement stmt = query(qs);
    if(stmt == null) {
      dbg.ERROR("Error performing genStoodins query!  Cannot load old standins!");
    } else {
      try {
        ResultSet rs = getResultSet(stmt);
        if(rs == null) {
          dbg.ERROR("Error extracting ResultSet from performing genStoodins query!  Cannot load old standins!");
        } else {
          while(next(rs)) {
            String transtarttime=getStringFromRS("transtarttime", rs);
            String stan=getStringFromRS("stan", rs);
            int storeid=getIntFromRS("storeid", rs);
            String transactionamount=getStringFromRS("transactionamount", rs);
            stoodins.add(TransactionID.New(transtarttime, stan, storeid), (new RealMoney(transactionamount)).Value());
          }
        }
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
  }

  public static final QueryString genStoodins(String authid) {
    QueryString qs = QueryString.Select("storeid").comma("transtarttime").comma("stan").comma("transactionamount").
    from(tranjour.name()).
    where("").isEmpty("tranendtime").
    and().nvPair("authid", authid).
    orderbyasc("transtarttime"); // try to get them in the order they arrived.
    return qs;
  }

  public String getPaymentTypeFromCardNo(TxnRow record) {
    return getStringFromQuery(genPaymentTypeFromCardNo(record));
  }

  public QueryString genPaymentTypeFromCardNo(TxnRow record) {
    String firstsix = Safe.subString(record.cardholderaccount, 0, 6); // +++ @@@ test !!!
    QueryString qs = QueryString.
      Select("paymenttypecode").
      from("card").
      where().nvPair("authid", record.authid).
      and().nvPair("transactiontype", record.transactiontype).
      and("lowbin < '" + firstsix + "'").
      and("highbin > '"+ firstsix +"'");
    // eg: DC, DS, AE, VS, MC, C1; get from card table
    return qs;
  }

  public void stampAuthStart(TxnRow record) {
    record.authstarttime = forTrantime(Safe.Now());
    update(genStampAuthStart(record)); // +++ check return value
  }

  public static final QueryString genStampAuthStart(TxnRow record) {
    String table = tranjour.name();
    QueryString qs = QueryString.
      Update(table).
      SetJust("authstarttime", record.authstarttime). // stamps authstarttime
      where(transactionIs(record.tid(), table));
    return qs;
  }

  private static final Monitor stano = new Monitor("stano");//a mutex
  // --- since we will be doing all table accesses via servlets, this will work
  // --- if that ever changes, will have to let the database do locking of table, etc.

  /**
  * @param store a numerical name like '0000000010005001'
  */
  protected static final QueryString upstan(int storeid,int stan) {
    return QueryString.Update(store.name()).SetJust("stanometer",stan).where(StoreIs(storeid));
  }

  private int getNextStanForStore(int storeid) {
    int stan = NOTANUMBER;
    try {
      stano.getMonitor();
      try {
        stan = getIntFromQuery( QueryString.Select("stanometer").from(store.name()).where(StoreIs(storeid)), 0); // +++ just get this the first time, after that, look it up in a list every time instead of going to the database (use np.util.Counter)
        if(stan < 1) {
          dbg.ERROR("STAN IS AN INVALID VALUE!"); // +++ this should email
        } else {
          // save it back
          if(update(upstan(storeid,++stan)) == -1) {
            // +++ spew error
            dbg.ERROR("COULD NOT UPDATE STAN VALUE!"); // +++ this should email
            stan = NOTANUMBER;
          }
        }
      } catch (Exception e) {
        dbg.Caught(e); // +++ this should email
      }
    } finally {
      stano.freeMonitor();
      return stan;
    }
  }

  public TransactionID newtransaction(int store,long reftime){
    int stan = getNextStanForStore(store);
    if(stan<0){
      return TransactionID.Zero();
    }
    return TransactionID.New(forTrantime(reftime),stan,store);
  }

  public TransactionID newtransaction(int store){
    return newtransaction(store,Safe.utcNow());
  }

  public static final QueryString genEnterpriseFromTerminalQuery(String terminalID) {
    return QueryString.Select("enterpriseid").from(store.name()).comma(terminal.name()).
    where().nvPair("terminal."+TERMINALID,terminalID).and().matching(terminal.name(),store.name(),STOREID);
  }

  public static final QueryString genStoresQuery(int enterpriseID) {
    return QueryString.Select("store.storeid").comma("storename").comma("store.address1").comma("store.address2").
    comma("store.city").comma("store.state").comma("store.zipcode").comma("store.country").
    comma("EnterpriseName").comma("javatz").comma("storehomepage").
    from("enterprise").comma(store.name()). //this group has  a name
    where().nvPair("store.enterpriseid",enterpriseID).
    and().matching(enterprise.name(),store.name(),"Enterpriseid").
    orderbyasc("storename");
  }

  public StoreInfoRow storesInfoQuery(int enterpriseID) {
    Statement q = query(genStoresQuery(enterpriseID));
    if(q == null) {
      dbg.ERROR("storesInfoQuery: q = null!");
    }
    return StoreInfoRow.NewSet(q);
  }

  public static final QueryString genAssociatesQuery(int enterpriseID) {
    return QueryString.Select("associateid").comma("loginname").comma("lastname").//{
      comma("firstname").comma("middleinitial").comma("enterpriseacl").comma("colorschemeid").
      from("associate").where().nvPair("enterpriseid",enterpriseID).
    orderbyasc("lastname");//}
  }

  public AssociateRow associateQuery(int enterpriseID) {
    Statement q = query(genAssociatesQuery(enterpriseID));
    if(q == null) {
      dbg.ERROR("associateQuery: q = null!");
    }
    return AssociateRow.NewSet(q);
  }

  public TxnRow unsettledTxnsQuery(String terminalname, Date starttime, int storeid) {
    Statement q = query(genUnsettledTxnsQuery(terminalname, starttime, storeid));
    if(q == null) {
      dbg.ERROR("unsettledTxnsQuery: q = null!");
    }
    return TxnRow.NewSet(q);
  }

  protected static final QueryString caidOfEnterprise(int enterpriseID){
    return QueryString.Select(STOREID).from(store.name()). // +++ maybe can use max?  Which is faster?
    where().nvPair(store.fieldname("enterpriseid"),enterpriseID);
  }

  public static final QueryString timeClause(String op,String cftime){
    return QueryString.Clause(EMPTYSTRING).append(TRANSTARTTIME).append(op).value(cftime);
  }

  static final QueryString AllClosings(){
    return QueryString.Select("max("+drawer.fieldname(TRANSTARTTIME)+")"). // +++ fix
    from(drawer.name()).
    where().join(drawer.fieldname(TERMINALID), terminal.fieldname(TERMINALID));
  }

  public Statement runEnterpriseDrawerQuery(int enterpriseid) {
    QueryString qs = genEnterpriseDrawersQuery(enterpriseid);
    return query(qs);
  }

  public static final QueryString genEnterpriseDrawersQuery(int enterpriseid) {
    // note: || means concatenate
    return QueryString.Select(drawer.allFields()).
    comma("store.STORENAME").comma("associate.FIRSTNAME || ' ' || associate.LASTNAME as associateName").
    comma(terminal.name()+"."+TERMINALNAME).
    from(drawer.name()).comma(terminal.name()).comma("associate").
    comma(store.name()).comma(enterprise.name()).
    where("").join(terminal.fieldname(TERMINALID),drawer.fieldname(TERMINALID)).
    and().join("associate."+drawer.ASSOCIATEID,drawer.fieldname(drawer.ASSOCIATEID)).
    and().join(store.fieldname(STOREID),terminal.fieldname(STOREID)).
    and().join(store.fieldname(ENTERPRISEID), enterprise.fieldname(ENTERPRISEID)).
    and().nvPair(enterprise.fieldname(ENTERPRISEID), enterpriseid).
    orderbydesc(TRANSTARTTIME);
  }

  public QueryString genBookmarkQuery(int bmid) {
    // note: || means concatenate
    QueryString qs = QueryString.Select(drawer.allFields()).
    comma("store.STORENAME").comma("associate.FIRSTNAME || ' ' || associate.LASTNAME as associateName").
    comma(terminal.name()+"."+TERMINALNAME).
    from(drawer.name()).comma(terminal.name()).comma("associate").comma("store");
    qs.where("").nvPair(drawer.drawerid.name(),bmid).
    and().join(terminal.name()+"."+TERMINALID,drawer.name()+"."+TERMINALID).
    and().join("associate."+drawer.ASSOCIATEID,drawer.name()+"."+drawer.ASSOCIATEID).
    and().join("store."+STOREID,terminal.name()+"."+STOREID).
    orderbydesc(TRANSTARTTIME);
    return qs;
  }

  // Problem is that dbvalidator is not a valid associateid, and enterpriseid should be filled in correctly!
  // On second thought, when this one is created, it will create a GIGANTIC closing since it is hte first one ever recorded.
  // Since we don't want to allow them to access that first one, we will leave it like it is, not displaying on the drawer closing screen.

  public TxnRow getDrawerClosingCursor(int bmid, LocalTimeFormat ltf) {
    // +++ find the duplicate of this code in this file and resolve the two into a single function -->
    // first, get the info from the drawer closing indicated.
    Statement stmt = null;
    ResultSet tqrs = null;
    int terminalid = 0;
    int storeid    = 0;
    String toDate  = null;
    String title   = null;
    try {
      stmt = query(genBookmarkQuery(bmid));
      tqrs = getResultSet(stmt);
      next(tqrs);
      terminalid     = getIntFromRS(drawer.terminalid.name()    , tqrs);
      storeid = getIntFromRS(drawer.storeid.name(), tqrs);
      toDate         = getStringFromRS(drawer.transtarttime.name() , tqrs);
// <-- find the duplicate of this code in this file and resolve the two into a single function +++
      title          = "Drawer Closing by " +
      getStringFromRS("ASSOCIATENAME"         , tqrs) + " of " +
      getStringFromRS(DBConstants.TERMINALNAME, tqrs)
      + " at " + ltf.format(PayMateDB.tranUTC(toDate));
      // we are done with this one
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
    }
    tqrs = null;
    // then, use that info to get the info for that closing and the previous drawer closing for that terminal
    // +++ use the drawer search queries that will be used for the drawer search screen here.
    TimeRange tr = PayMateDB.TimeRange().setStart(LocalTimeFormat.genesis); // from the beginning of paymate (0 fucksup)
    tr.setEnd(toDate); // until the closing of the drawer
    QueryString qs = QueryString.
    SelectAllFrom(drawer.name()).
    where().nvPair(drawer.terminalid.name(), terminalid).
    timeInRange(tr).orderbydesc(drawer.transtarttime.name());
    TerminalID T = null;
    try {
      stmt = query(qs);
      // then, run the query to get the records from tranjour
      T = new TerminalID(terminalid, storeid);
      //make a standard tranStartTime range & start it off going from start of company through now.
      tr=ForAllTime();
      ResultSet rs = getResultSet(stmt);//all closings for this terminal
      if(rs == null) {
        dbg.ERROR("result set is null!");
      } else {
        for(int i = 0;next(rs) && (i < 2); ++i) {
          String str = getStringFromRS(drawer.transtarttime.name(), rs);
          if(i==0){
            tr.setEnd(str);
          } else if(i==1){
            tr.setStart(str); // too few records means it starts at paymate's genesis
          }
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt); // don't need this stmt anymore
      stmt = null;
    }
    TerminalInfo tinfo = getTerminalInfo(terminalid);
    TxnRow trow = TxnRow.NewSet(query(genBatchQuery(storeid, tinfo.getNickName(),tr,false)));
    if(trow != null) {
      trow.setTitle(title);
    }
    return trow;
  }

  public static final QueryString genBatchQuery(int storeid, String terminalName, TimeRange ranger, boolean onlyContribute) {
    return QueryString.Select(tranjour.allFields()).//{
      append(genWhichTxnsSubquery(storeid, terminalName, ranger, onlyContribute)).
      orderbydesc(ranger.fieldName());//}
  }

  public static final QueryString genUnsettledTxnsQuery(String terminalname, Date starttime, int storeid) {//+=>ent2store
    TimeRange ranger = ForAllTime().setStart(starttime);
    return genBatchQuery(storeid, terminalname, ranger, false);
  }

  public static final QueryString genWhichTxnsSubquery(int storeid, String terminalName, TimeRange ranger, boolean onlyContribute) {
    return QueryString.Clause("").
      from(tranjour.name()).
      where("").
      append(genInnerWhereTxnsSubquery(terminalName, ranger, onlyContribute)).
      and().nvPair("tranjour."+STOREID,storeid);
  }

  public static final QueryString genInnerWhereTxnsSubquery(String terminalName, TimeRange ranger, boolean onlyContribute) {
    return QueryString.Clause("").
      append(onlyContribute ? ApprovedTxns() : NoVoids()).
      and().nvPair("tranjour."+CARDACCEPTORTERMID,terminalName).
      and().
      Open(""). //approved at the time submitted
      Open("").isEmpty("tranjour.stoodinstan").andRange(ranger.fieldName(),ranger).Close().
      or(). //standin at the time swiped
      Open("").not().isEmpty("tranjour.stoodinstan").andRange("tranjour.CLIENTREFTIME",ranger). Close().
      Close();
  }

  public QueryString genDrawerCardSubtotals(int storeid, String terminalName, TimeRange ranger) {
    return QueryString.Select("paymenttypename").comma("").
      append(genSelectTxnSubquery()).
      from("paytype").comma(" outer "+tranjour.name()).
      where("").
      matching("paytype", tranjour.name(), "paymenttypecode").
      and().matching("paytype", tranjour.name(), "storeid").
      and().nvPair("paytype."+STOREID,storeid).
      and().append(genInnerWhereTxnsSubquery(terminalName, ranger, true)).
      groupby("paymenttypename").
      orderbyasc("paymenttypename");
  }

  public final QueryString genSelectTxnSubquery() {
    return QueryString.Clause("count(stan) as counter").
      comma("sum(DECODE(processingcode,'200030',transactionamount*-1,transactionamount)) as sumer");
  }

  public final QueryString genTxnTotalsSubquery(int storeid, String terminalName, TimeRange ranger) {
    return QueryString.Clause("").
      append(genSelectTxnSubquery()).
      append(genWhichTxnsSubquery(storeid, terminalName, ranger, true));
  }

  public QueryString genTerminalTotal(String storeid, String cardacceptortermid, String starttime) {
    return genTerminalTotal(Safe.parseInt(storeid), cardacceptortermid, starttime);
  }

  public QueryString genTerminalTotal(int storeid, String cardacceptortermid, String starttime) {
    TimeRange ranger = ForAllTime().setStart(starttime);
    return QueryString.Select("").append(genTxnTotalsSubquery(storeid, cardacceptortermid, ranger));
  }

  public static final QueryString genCloseShift(TerminalID T, TimeRange ranger,LoginInfo li) {
    DrawerTable tbm = drawer;
    return QueryString.
    Insert(tbm.name()).
    Open(tbm.transtarttime.name()).
    comma(tbm.terminalid.name()).
    comma(tbm.associateid.name()).
    comma(tbm.storeid.name()).
    comma(tbm.enterpriseid.name()).
    Close()
    .Values(ranger.two()).
    comma(""+T.terminalID).
    comma(""+li.associd).
    comma(""+T.storeid).
    comma(""+li.enterpriseID).
    Close();
  }

  /**
  * @return a database TimeRange used for querying for records associated with the range of closings for that terminal, from most recent to oldest.
  */
  public TimeRange getPendingRange(TerminalID T){
    return TimeRange().setStart(getPendingStartTime(T)).setEnd(Safe.utcNow());
  }

  public Date getPendingStartTime(TerminalID T){
    return tranUTC(getPendingStartTimeStr(T));
  }

  public String getPendingStartTimeStr(TerminalID T){
    return getStringFromQuery(genPendingStartTimeQuery(T));
  }

  /**
   * Get the newest (latest) CLOSING drawer for this terminal.
   */
  public QueryString genPendingStartTimeQuery(TerminalID T) {
    return QueryString.Select(" max("+TRANSTARTTIME+") as " +TRANSTARTTIME).
      from(drawer.name()).
      where(TerminalIs(T));
  }

  public TxnRow getTranjourRecordfromTID(TransactionID tid, String terminalId) {
    TxnRow rec = null;
    QueryString pending = genTransactionFromQuery(tid, terminalId, tranjour.name());
    Statement stmt = query(pending);
    if(stmt != null) {
      try {
        int count = 0;
        ResultSet rs = getResultSet(stmt);
        if(rs != null)  {
          // if there is only one, use it, otherwise error
          while(next(rs)) {
            count++;
            if(count > 1) {
              rec = null;
            } else {
              rec = TxnRow.NewOne(rs);
            }
          }
        }
        if(rec == null) {
          dbg.VERBOSE("getTranjourRecordfromTID: found " + count + " records from query: " + pending);
        }
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
    return rec;
  }

  /**
  * add into a WHERE clause
  * +_+ review the trims below, they should not be needed. tid normalizes its stuff.
  */
  private static final QueryString transactionIs(TransactionID tid, String table) {
    //String table = tranjour.name()
    return QueryString.Clause("")
    .nvPair(table + "."+STOREID, tid.caid)
    .and().nvPair(table + ".stan",tid.stan())
    .and(TimeAfter(TransactionTime.forTrantime(tranUTC(tid.time).getTime()-Ticks.forDays(1))));
  }

  // --- schizophrenic
  private QueryString genTransactionFromQuery(TransactionID tid, String terminalId, String table) {
    dbg.WARNING("genTransactionFromQuery(): tid="+tid.image()+", terminalId=" + terminalId + ", table="+table);
    // if you don't have it, generate the appropriate storeid from the received terminalId
    // if you don't have the time, find all txn's with that time
    table = Safe.TrivialDefault(table, EMPTYSTRING).trim();
    QueryString qs = QueryString.SelectAllFrom(table);
    boolean knowTime = (Safe.parseLong(tid.time) != 0);
    boolean knowCAID = (tid.caid != 0);
    if(knowCAID && knowTime) {
      qs.where().append(transactionIs(tid, table)); // this is sort of a test of transactionIs()
    } else {
      if(knowCAID) {
        qs.where().nvPair(table + "."+STOREID, tid.caid);
      } else {
        qs.comma(terminal.name()).
        where().matching(table,"terminal",STOREID).
        and().nvPair("terminal."+TERMINALID,terminalId.trim());
      }
      qs.and().nvPair(table + ".stan",tid.stan().trim());
      if(knowTime) {
        qs.and(TimeIs(tid.time.trim()));
      }
    }
    return qs;
  }

  public static final TransactionID getTIDfromResultSet(ResultSet rs){
    return TransactionID.New(DBMacros.getStringFromRS(TRANSTARTTIME, rs),
    DBMacros.getStringFromRS("stan", rs),
    DBMacros.getIntFromRS(STOREID, rs));
  }

  static final String tranAfter(Date cutoff){
    return QueryString.Clause(TRANSTARTTIME+">").quoted(TransactionTime.forTrantime(cutoff)).toString();
  }

  static final String tranAfter(long cutoff){
    return tranAfter(new Date(cutoff)).toString();
  }

  public static final ProcessingCode ProcessingCode(FinancialRequest freq){
    return ProcessingCode.New(freq.sale.type);
  }

  /**
   * @return query matching store,amount,account,and type of transaction
   */
  private QueryString findMatchingCore(LoginInfo linfo, FinancialRequest req){
    String accountnum;
    if(req instanceof CardRequest){
      accountnum=((CardRequest) req).card.accountNumber.Image();
    } else if(req instanceof CheckRequest){
      MICRData stripe =((CheckRequest) req).check;
      //the last term below is usually pretty stupid.
      //obviously mainsail tacked check abilities onto the ass end of credit.
      accountnum=stripe.Transit+stripe.Account+stripe.Serial;
    } else {
      accountnum="0";
    }
    String poscode= ProcessingCode(req).Image();

    return QueryString.//{
      SelectAllFrom(tranjour.name()).
      where().nvPair(STOREID,linfo.storeid).
      and().nvPair(AMOUNT,req.Amount().Image("#0.00")).
      and().nvPair(ACCOUNT,accountnum).
      and().nvPair("processingcode",poscode);
      //}
  }

  /**
   * @return from any terminal in that store.
   */
  QueryString findSimilar(LoginInfo linfo, FinancialRequest req){
    return findMatchingCore(linfo,req).orderbydesc(TRANSTARTTIME);
  }

/**
 * @return find an exact match, findSimilar+same time and terminal as well
 */
  QueryString findExactly(LoginInfo linfo, FinancialRequest req){
    return findMatchingCore(linfo,req).
    and().nvPair(CARDACCEPTORTERMID,linfo.terminalName).
    and().nvPair(CLIENTREFTIME, forTrantime(req.requestInitiationTime)).
    orderbydesc(TRANSTARTTIME);//yes, compare client time, but order by server time-should only be one.
  }

  public TxnRow getTransactionForRetry(LoginInfo linfo, FinancialRequest req, boolean matchTime) {
    TxnRow trans = null;
    /** fields needed for query:
    *    date+time (subtracting could cause the time to roll back to yesterday) (retrieved in query function, diff passed in),
    *    amount (passed in),
    *    TERMINALNAME (calc'd from LoginInfo passed in),
    *    storeid (could have the same TERMINALNAME in two different stores) (calc'd from LoginInfo passed in),
    *  what else?
    */
    Statement stmt = query(matchTime? findExactly(linfo,req): findSimilar(linfo,req));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          dbg.VERBOSE("found a potential retry's record");
          trans=TxnRow.NewOne(rs);
        } else {
          dbg.VERBOSE("no potential retry");
        }
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
    return trans;
  }

  void insertStoreFromEnterprise(QueryString qs, int eid) {
    qs.
      and().nQuery(STOREID,store.name(),
        QueryString.Clause(EMPTYSTRING).
        where().nvPair(store.name()+".enterpriseid",eid));
  }

  public TxnRow findTransactionBy(int enterpriseID, TranjourFilter filter){
    ResultSet rs = null;
    // build the query string
    QueryString clauses = QueryString.Clause(EMPTYSTRING).where(NoVoids());
    insertStoreFromEnterprise(clauses,enterpriseID);
    clauses.andRange(ACCOUNT,filter.card);
    // handle the amounts
    clauses.andRange(AMOUNT,filter.amount);
    // handle the stans
    clauses./*andModularRange*/andRange(STAN,filter.stan);
    // handle the approvals
    clauses.andRange("AUTHIDRESPONSE",filter.appr);
    // handle the dates
    clauses.andRange(TRANSTARTTIME,filter.time);
    return TxnRow.NewSet(query(QueryString.Select("transtarttime, *").from(tranjour.name()).append(clauses).orderbydesc("1")));
  }

  protected static final String unescape(String escaped) {
    return Safe.unescapeAll(escaped);
  }

  static final StoreInfo storeFromRS(ResultSet rs){
    StoreInfo si = new StoreInfo();

    String name        = getStringFromRS("storename", rs);
    String address1    = getStringFromRS("address1", rs);
    String city        = getStringFromRS("city", rs);
    String state       = getStringFromRS("state", rs);
    String country     = getStringFromRS("country", rs);
    si.setNameLocation(name, address1, city, state, country);
    si.setIdentificationCode(getStringFromRS(STOREID, rs));
    si.timeZoneName = getStringFromRS("javatz",rs);
    si.slim=getLimitsFromRS(rs);
    return si;
  }

  static final StandinLimit getLimitsFromRS(ResultSet rs) {
    return new StandinLimit(new RealMoney(getStringFromRS("STANDINLIMIT", rs)),
    new RealMoney(getStringFromRS("STORESTANDINTOTAL", rs)));
  }

  static final ReceiptFormat receiptInfoFromRS(ResultSet rs){
    ReceiptFormat newone= new ReceiptFormat();
    // unescape is needed for embedded '\n's in the receipt headers and footers
    newone.Header  = unescape(getStringFromRS("receiptHeader", rs)); // "siNet POS services\n9420 Research Blvd.\nAustin,TX 78759"; // +++ get this!
    newone.Tagline = unescape(getStringFromRS("receiptTagline", rs)); //"Bringing the Net\nto your business"; // +++ get this!

    newone.TimeFormat=Safe.OnTrivial(unescape(getStringFromRS("receiptTimeFormat", rs)).trim(), ReceiptFormat.DefaultTimeFormat);
    newone.showSignature=getBooleanFromRS("receiptShowSig", rs); // defaults to false
    newone.abide  = Safe.OnTrivial(getStringFromRS("receiptabide", rs), newone.abide);
    return newone;
  }

  static final TerminalCapabilities termcapFromRS(ResultSet rs){
    EasyCursor ezp=new EasyCursor();
    ezp.setBoolean(TerminalCapabilities.freePassKey , getBooleanFromRS(store.freepass.name(), rs));  //#lubys#, but Taco probably wouldn't mind.
    ezp.setBoolean(TerminalCapabilities.autoApproveKey, getBooleanFromRS(store.autoapprove.name(), rs));  //#lubys# must be true, taco would be surprised! but still probably wouldn't mind
//the followin gdo not yet have database fields behind them:
    ezp.setBoolean(TerminalCapabilities.creditAllowedKey,getBooleanFromRS(store.creditallowed.name(), rs)); // +++ make this default to false AFTER it is added to the database as true for everyone
    // begin cheecks
    ezp.setBoolean(TerminalCapabilities.checksAllowedKey,getBooleanFromRS(store.checksallowed.name(), rs));
    ezp.setBoolean(TerminalCapabilities.alwaysIDKey,true);
// end checks
// needed for debit
    ezp.setBoolean(TerminalCapabilities.debitAllowedKey,getBooleanFromRS(store.debitallowed.name(), rs));
// end debit
// needed for debit push
    ezp.setBoolean(TerminalCapabilities.pushDebitKey,false);
    ezp.setString(TerminalCapabilities.debitPushThresholdKey,"$0.00");//of course this can be cents. // INTEGER
// end debit push
    ezp.setBoolean(TerminalCapabilities.autoCompleteKey,/*true testing:*/false); //

    return new TerminalCapabilities(ezp);
  }

  static final StoreConfig storeFig(ResultSet rs){
    StoreConfig cfg=new StoreConfig();
    cfg.si= storeFromRS(rs);
    cfg.receipt= receiptInfoFromRS(rs);
    cfg.termcap= termcapFromRS(rs);
    return cfg;
  }

  /**
  * available fields: (excluding deprecated and stupid ones)
  * terminalid char 32 NOT NULL
  * storeid INTEGER NOT NULL
  * modelcode char 4 NOT NULL
  * terminalname char 8 NOT NULL
  */
  static final TerminalInfo TermFromRS(ResultSet rs){
    TerminalInfo newone=new TerminalInfo(getIntFromRS(TERMINALID, rs));
    newone.setNickName(getStringFromRS(TERMINALNAME, rs));
    newone.equipmenthack=getStringFromRS("modelcode", rs);
    dbg.VERBOSE("TermFromRS:"+newone.toSpam());
    return newone;
  }

  /**
  * @return query for appliance info
  */
  static final QueryString ApplianceInfoQry(String applianceID){
    return
    QueryString.SelectAllFrom(store.name()).
    comma(terminal.name()).
    where(ApplianceIs(applianceID)); //
  }

  /**
  * @return multiple terminal connection
  */
  public ConnectionReply getApplianceInfo(String applianceID) {
    Statement apple = query(ApplianceInfoQry(applianceID)); // appliance query, returns all terminals for this appliance
    ConnectionReply tlr = new ConnectionReply(applianceID);
    if(apple != null) {
      try {
        ResultSet rs = getResultSet(apple);
        if(rs != null && next(rs)) {
          //pick store info off of first terminal, will be the same for all
          tlr.cfg= storeFig(rs);
          tlr.add(TermFromRS(rs));
          while(next(rs)){//each one is a terminal
            tlr.add(TermFromRS(rs));
          }
          //in the future we investigate hardware table and build "testpos.properties" here.
          tlr.status.setto(ActionReplyStatus.Success);//moved here so that we can make variants.
        }
      } catch (Exception t) {
        dbg.ERROR("Exception getting terminal login info!");
        dbg.Caught(t);
      } finally {
        closeStmt(apple);
      }
    }
    return tlr;
  }

  public static final QueryString genLogApplianceUpdate(
      String applName, String revision, long requestInitiationTime, long srvrTime, long freeMemory,
      long totalMemory, int activeCount, int activeAlarmsCount, int txnCount, int rcptCount) {
    boolean includeCounts = ((txnCount != -1) && (rcptCount != -1));
    QueryString qsr = QueryString.Update("appliance").Clause("set").
      Open("rptRevision").
      comma("rptApplTime").
      comma("rptTime").
      comma("rptFreeMem").
      comma("rptTtlMem").
      comma("rptThreadCount").
      comma("rptAlarmCount");
    if(includeCounts) {
      qsr = qsr.comma("rptStoodTxn").comma("rptStoodRcpt");
    }
    qsr = qsr.
      Close().
      Values(revision).
      comma(forTrantime(requestInitiationTime)).
      comma(forTrantime(srvrTime)).
      comma(""+freeMemory).
      comma(""+totalMemory).
      comma(""+activeCount).
      comma(""+activeAlarmsCount);
    if(includeCounts) {
      qsr = qsr.comma(""+txnCount).comma(""+rcptCount);
    }
    qsr = qsr.
      Close().
      where().nvPair("applname", applName);
    return qsr;
  }

  public void logApplianceUpdate(String applName, String revision, long requestInitiationTime, long srvrTime, long freeMemory, long totalMemory, int activeCount, int activeAlarmsCount, int txnCount, int rcptCount) {
    update(genLogApplianceUpdate(applName, revision, requestInitiationTime, srvrTime, freeMemory, totalMemory, activeCount, activeAlarmsCount, txnCount, rcptCount));
  }

  public static final QueryString genApplianceTerminalStore(String applName) {
    return QueryString.
    Select("storename").comma("terminalname").
    from("terminal").comma("store").comma("appliance").
    where().nvPair("applname", applName).
    and().matching("appliance", "terminal", "applianceid").
    and().matching("store", "terminal", "storeid");
  }

  public String getApplianceTerminalStore(String applName) {
    Statement stmt = query(genApplianceTerminalStore(applName));
    String ret = "error extracting appliance info from database";
    if(stmt != null) {
      try {
        String store = null;
        String terminals = "";
        ResultSet rs = getResultSet(stmt);
        while(next(rs)) {
          if(store == null) {
            store = getStringFromRS("storename", rs);
          }
          terminals += getStringFromRS("terminalname", rs);
        }
        ret = "Store: "+store+"\nTerminals: "+terminals;
      } catch (Exception ex) {
        dbg.Caught(ex);
      } finally {
        closeStmt(stmt);
      }
    }
    return ret;
  }

  public QueryString genApplianceRowQuery() {
    return QueryString.
      Select("applianceid").comma("applname").comma("storeid").comma("storename").comma("rptRevision").
      comma("rptApplTime").comma("rptTime").comma("rptFreeMem").comma("rptTtlMem").comma("rptThreadCount").
      comma("rptAlarmCount").comma("rptStoodTxn").comma("rptStoodRcpt").
      from("appliance").comma("store").
      where().matching("appliance", "store", "storeid").
      orderbyasc("storeid").comma("applname");
  }

  public ApplianceRow getApplianceRowQuery() {
    return ApplianceRow.NewSet(query(genApplianceRowQuery()));
  }

  //SELECT storename FROM terminal, store where terminal.storeid=store.storeid and applianceid='0060EF218533'
  private final QueryString StorenameQuery(String applianceId) {
    return QueryString.Select("storename").
      from(store.name()).
      comma(terminal.name()).
      where().matching(store.name(),terminal.name(),terminal.STOREID).
      and().nvPair(terminal.applianceid.name(), applianceId);
  }

  public String getStorenameForAppliance(String applianceId){
    return getStringFromQuery(StorenameQuery(applianceId));
  }

  // SELECT terminalid, terminalname FROM terminal WHERE applianceid = '0060EF218533' order by terminalname asc
  private final QueryString ApplianceTerminalsQuery(String applianceId) {
    return QueryString.Select(terminal.terminalid.name()).comma(terminal.terminalname.name()).
      from(terminal.name()).
      where().nvPair(terminal.applianceid.name(), applianceId).
      orderbyasc(terminal.terminalname.name());
  }

  // format elsewhere, just return a TextList +++
  private static final String div = "<BR>"; // ", "
  public String getTerminalsForAppliance(String applianceId){
    String ret = "";
    Statement apple = query(ApplianceTerminalsQuery(applianceId)); // appliance query, returns all terminals for this appliance
    if(apple != null) {
      try {
        ResultSet rs = getResultSet(apple);
        if(rs != null) {
          while(next(rs)){//each one is a terminal; get rid of this next block when we have an appliances table! ---
            if(Safe.NonTrivial(ret)) {
              ret += div;
            }
            ret += rs.getString(terminal.terminalname.name()).trim()+"/"+rs.getString(terminal.terminalid.name());
          }
        }
      } catch (Exception t) {
        dbg.ERROR("Exception getting appliance terminals info!");
        dbg.Caught(t);
      } finally {
        closeStmt(apple);
      }
    }
    return ret;
  }

  /**
   * Temporary until we get the tables fixed up
   */
  public QueryString genTerminalsForStore(int storeid) {
    return QueryString.
      Select("").append("enterprisename").comma("storename").comma(terminal.terminalname.name()).
      comma("modelcode").
      comma(enterprise.fieldname(ENTERPRISEID)).
      comma(terminal.fieldname(terminal.storeid)).
      comma(terminal.terminalid.name()).
      from(terminal.name()).comma(store.name()).comma(enterprise.name()).
      where("").nvPair(terminal.fieldname(STOREID), storeid).
      and().matching(store.name(),enterprise.name(),ENTERPRISEID).
      and().matching(terminal.name(),store.name(),terminal.storeid.name()).
//      and(terminal.terminalid.name()).not().in().parenth(QueryString.Select(ASSOCIATEID).from("associate")).
      orderbyasc(terminal.terminalname.name());
  }
  public TerminalPendingRow getTerminalsForStore(int storeid) {
    Statement stmt =null;
    try {
      stmt = query(genTerminalsForStore(storeid));
    } catch(Exception arf){
      dbg.ERROR("Swallowed in getTerminalsForEnterprise:"+arf);
    } finally {
      return TerminalPendingRow.NewSet(stmt);
    }
  }
  public QueryString genTerminalPendingRow(int terminalid) {
    return QueryString.
    Select("").append(terminal.terminalid.name()).comma(terminal.terminalname.name()).
    comma(terminal.fieldname(terminal.storeid)).
    comma(enterprise.fieldname(ENTERPRISEID)).
    comma(enterprise.fieldname("ENTERPRISENAME")).
    comma(terminal.fieldname("MODELCODE")).
    comma(store.fieldname("storename")).
    from(terminal.name()).comma(store.name()).comma(enterprise.name()).
    where("").nvPair(terminal.fieldname(terminal.terminalid), terminalid).
    and().matching(store.name(),enterprise.name(),ENTERPRISEID).
    and().matching(terminal.name(),store.name(),terminal.storeid.name()).
//    and(terminal.terminalid.name()).not().in().parenth(QueryString.Select(ASSOCIATEID).from("associate")).
    orderbyasc(terminal.terminalname.name());
  }
  public TerminalPendingRow getTerminalPendingRow(int terminalid) {
    TerminalPendingRow row =null;
    Statement stmt = null;
    try {
      stmt = query(genTerminalPendingRow(terminalid));
      ResultSet rs = getResultSet(stmt);
      if(next(rs)) {
        row = TerminalPendingRow.NewOne(rs);
      }
    } catch(Exception arf){
      dbg.ERROR("Swallowed in getTerminalPendingRow:"+arf);
    } finally {
      closeStmt(stmt);
      return row;
    }
  }

  // get the totals for this drawer:
  public void getTerminalTotals(TerminalPendingRow tpr) {
    // Get last closing time.  Use drawerid's in the txn to prevent this query!
    tpr.lastCloseTime(getPendingStartTimeStr(new TerminalID(Safe.parseInt(tpr.terminalid), Safe.parseInt(tpr.storeid))));
    tpr.apprAmount(0);
    tpr.apprCount(0);
    Statement stmt = query(genTerminalTotal(tpr.storeid, tpr.terminalName, tpr.lastCloseTime()));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          int count = getIntFromRS("counter", rs);
          long amount = (long)(getDoubleFromRS("sumer", rs) * 100);
          tpr.apprCount(count);
          tpr.apprAmount(amount);
        }
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
  }

  public CardSubtotalsRow getDrawerCardSubtotals(int bmid) {
    // +++ part of this code is duplicated elsewhere in this class; find it and make one function out of it
    Statement stmt = null;
    int terminalid = 0;
    int storeid    = 0;
    String toDate  = null;
    try {
      stmt = query(genBookmarkQuery(bmid));
      if(stmt != null) {
        ResultSet tqrs = getResultSet(stmt);
        next(tqrs);
        terminalid= getIntFromRS(drawer.terminalid.name()      , tqrs);
        storeid   = getIntFromRS(drawer.storeid.name()         , tqrs);
        toDate    = getStringFromRS(drawer.transtarttime.name(), tqrs);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
      stmt = null;
    }
    // then, use that info to get the info for that closing and the previous drawer closing for that terminal
    TimeRange tr = PayMateDB.TimeRange().setStart(LocalTimeFormat.genesis); // from the beginning of paymate (0 fucksup)
    tr.setEnd(toDate); // until the closing of the drawer
    QueryString qs = QueryString.
    SelectAllFrom(drawer.name()).
    where().nvPair(drawer.terminalid.name(), terminalid).
    timeInRange(tr).orderbydesc(drawer.transtarttime.name());
    TerminalID T = null;
    try {
      stmt = query(qs);
      // then, run the query to get the records from tranjour
      T = new TerminalID(terminalid, storeid);
      //make a standard tranStartTime range & start it off going from start of company through now.
      tr=ForAllTime();
      ResultSet rs = getResultSet(stmt);//all closings for this terminal
      if(rs == null) {
        dbg.ERROR("result set is null!");
      } else {
        for(int i = 0;next(rs) && (i < 2); ++i) {
          String str = getStringFromRS(drawer.transtarttime.name(), rs);
          if(i==0){
            tr.setEnd(str);
          } else if(i==1){
            tr.setStart(str); // too few records means it starts at paymate's genesis
          }
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt); // don't need this one anymore
    }
    TerminalInfo tinfo = getTerminalInfo(terminalid);
    // now we have a time range and a Terminalid.  Let's gen the report now ...
    return CardSubtotalsRow.NewSet(query(genDrawerCardSubtotals(storeid,tinfo.getNickName(),tr)));
  }

  public CardSubtotalsRow getUnsettledCardSubtotals(int storeid, String terminalname, Date starttime) {
    TimeRange ranger = ForAllTime().setStart(starttime);
    Statement q = query(genDrawerCardSubtotals(storeid, terminalname, ranger));
    if(q == null) {
      dbg.ERROR("getUnsettledCardSubtotals: q = null!");
    }
    return CardSubtotalsRow.NewSet(q);
  }

/////////////////////////////////////
//////// Receipts

  // +++ need to code this so can also return a list of errors ?
  // +_+ what are the restrictions here?  Can any user do this?
  // (currently, anybody who can login at all can get ANY receipt in the system, if they know the path to it)
  // +++ remove the terminalID requirement here?
  // this works :
  public String getReceipt(TransactionID tid, int terminalID) {
    boolean knowCAID = (tid.caid != 0);
    QueryString qs = QueryString.Select("receiptfile").from("receipt");
    if(knowCAID){
      qs.where().nvPair("receipt."+STOREID,tid.caid);
    } else {
      // +_+ this might be wrong, I think I just fixed it, though
      qs.comma("terminal").
      where().matching("receipt","terminal",STOREID).
      and().nvPair("terminal."+TERMINALID,terminalID);
    }
    qs.and().nvPair("stan",tid.stan()).
    and(TimeIs(tid.time));
    return  getStringFromQuery(qs);
  }

  public TextList logReceipt(TransactionID tid, String filepath, String who, String terminalId) {
    TextList tl = new TextList();
    // first, check to see if there is a transaction macthing that tid
    TxnRow txn = getTranjourRecordfromTID(tid, terminalId);
    if(txn == null) {
      tl.add("Transaction not found: " + tid.image());
    } else {
      // if so, check to see if the file in filepath exists
      File file = new File(filepath);
      if(!file.exists() || !file.isFile()) {
        tl.add("File not found: " + filepath);
      } else {
        // if so, insert the record in the receipt table
        QueryString qs = QueryString.Insert("receipt").
        Open(STOREID).   comma("stan").
        comma(TRANSTARTTIME).   comma("receiptfile").
        Close().
        Values(""+tid.caid).       commaQuoted(tid.stan()).
        commaQuoted(tid.time).  commaQuoted(filepath).
        Close();
        int success = update(qs);
        if(success < 1) {
          tl.add("Error inserting the record into the database!  File '" +
          filepath + "' belongs to tid '" + tid.image() +
          "'.  Tell someone who can fix it!");
        } else {
          // and return a successful operation (null)
        }
      }
    }
    //    return (tl.size() > 0) ? (TextList)null : tl;//this caused an exception, and is assbackwards to boot
    return tl;
  }

  // +++ make with QueryString!  (or toss)
  private static final QueryString genPossibleMSDupsQuery(String sincedate) {
    return QueryString.Clause(
        "SELECT transtarttime[1,8] as tdate, store.storename, cardacceptortermid as term,  cardholderaccount as card, expirationdate as exp, transactionamount as amt, "+
        "count(transactionamount) as N1,  count(transtarttime[1,8]) as N2,  min(stan) as stan1,  max(stan) as stan2,  min(tranjour.stoodinstan) as sistan1,  max(tranjour.stoodinstan) as sistan2,  max(tranjour.clientreftime) as last_date, max(voidtransaction) as V1,  "+
        "min(voidtransaction) as V2, min(actioncode) as A1, max(actioncode) as A2, min(transtarttime) as time1, max(transtarttime) as time2 "+
        "from tranjour, paytype, store "+
        "where not store.storeid = '000000000076001' "+//+++
        "and store.storeid = txn.storeid and "+
        "paytype.storeid = txn.storeid and "+
        "paytype.paymenttypecode = tranjour.paymenttypecode and "+
        "messagetype = '0200'  and "+
        "not processingcode = '200030'  "+
        "and transtarttime > '" + sincedate + "'  "+
        "and transactionamount > 0.01  "+
        "group by storename, cardacceptortermid, cardholderaccount, expirationdate,  transactionamount, transtarttime[1,8] "+
        "having count(transactionamount) > 1 "+
        "and count(transtarttime[1,8]) > 1  "+
        "and not (max(actioncode) = min(actioncode) and max(actioncode) in ('D', 'S'))"+
        "order by 1 desc, 2 asc, 3 desc"
    );
  }

  public Statement getPossibleMSDups(String sincedate) {
    return query(genPossibleMSDupsQuery(sincedate));
  }

  //////////////////////////////////////////
  // validator stuff

  // +++ eventually an enumeration?
  private static final int DONE = 0;
  private static final int ALREADY = 1;
  private static final int FAILED = 2;

  private Monitor PayMateDBclassMonitor = new Monitor("PayMateDB.class");

  private static final Tracer trc = new Tracer(PayMateDB.class.getName()+".Validator");

  public static final boolean init(DBConnInfo dbc, PrintStream backupPs) {
    DBMacros.init(backupPs);
    return Validate(dbc);
  }

  public static final boolean Validate(DBConnInfo dbc) {
    PayMateDB db = new PayMateDB(dbc);
    db.validate();
    return db.validated();
  }

  protected final void validate() {
    // +++ have a profile that we use to pass all of the functions like fieldExists() that is kept up-to-date and refreshes parts of itself when needed. (listener?)
    try {
      PayMateDBclassMonitor.getMonitor();
      if(validated()) {
        return;
      }
      dbg.ERROR("Validating database ...");
      super.validate();
      // +++ make this behave as:
      /*
      add fields
      add tables
      transmute/create/stuff data for any fields newly added // +++ that can be done by creating an array of "newlyCreated" that can be skipped through to trigger data stuffing events
      drop fields
      drop tables
      */
      // +++ can be done by comparing a list of "desired" to "exists"

      // informix (serial): You must apply a unique index to this column to prevent duplicate serial numbers.

      liberty4();

      // +++ later, drop receipt table in favor of new storage mechanism
      // +++ switch to integers for all id's

      // place the date here for any new ones you add ...
//    update(QueryString.Clause("UPDATE STATISTICS HIGH")); // should always complete, no need to check return value; might take minutes

      validated(true);
    } catch (Exception e) {
      trc.Caught(e);
    } finally {
      trc.Exit();
      PayMateDBclassMonitor.freeMonitor();
    }
  }

/*
>>KEEP (; some are temporary):
>>1+14+14+8+6+16+8+28+4+2+40+4+14+16+6+2+6+2+1+6+76+37+14+2+10+14+1=352
>>actioncode
>>authendtime - want to start using these
>>authstarttime - want to start using these
>>authorizername
>>authidresponse
>>cardacceptorid - for now
>>cardacceptortermid - for now
>>cardholderaccount
>>expirationdate
>>hostresponsecode (only need this or responsecode?)
>>hosttracedata // put stuff from cs in it
>>messagetype
>>stoodinstan // has our standin stuff in it
>>clientreftime // has our standin stuff in it
>>originalstan
>>paymenttypecode
>>processingcode
>>responsecode (only need this or hostresponsecode?)
>>standinindicator - want to start using this instead of the modify* fields
>>stan // for now
>>track1data
>>track2data
>>tranendtime
>>transactiontype CR/DB/etc
>>transactionamount
>>transtarttime
>>voidtransaction
*/

  private final void liberty4() {
dbg.ERROR("VALIDATING AUTHORIZER ...");
    validateAuthorizer();
dbg.ERROR("VALIDATING CARD ..."); // only for looking up paytype+institution for cards
    // cardid SERIAL [PK]
    // paytype CHAR(2) index
    // institute CHAR(2) index
    // lowbin CHAR(6) index + ...
    // highbin CHAR(6) ... + index
    if(tableExists("bin")) {
      update(QueryString.Clause("rename table bin to card"));
    }
    dropField("card", "comparelength");
    dropField("card", "checkexpdateflag");
    dropField("card", "manentryallowflag");
    dropField("card", "authorizername_2");
    dropField("card", "authorizername_3");
    dropField("card", "validcardlength_1");
    dropField("card", "validcardlength_2");
    dropField("card", "validcardlength_3");
    dropField("card", "modcheck");
    dropField("card", "modifyemployee");
    dropField("card", "modifydatetime");
    if(fieldExists("card", "authorizername_1")) {
      update(QueryString.Clause("RENAME COLUMN card.authorizername_1 TO authorizername"));
    }
    if(fieldExists("card", "lowbinnumber")) {
      update(QueryString.Clause("RENAME COLUMN card.lowbinnumber TO lowbin"));
    }
    if(fieldExists("card", "highbinnumber")) {
      update(QueryString.Clause("RENAME COLUMN card.highbinnumber TO highbin"));
    }
    if(fieldExists("card", "authorizername") && fieldExists("card", "transactiontype")) {
      update(QueryString.Clause("delete from card where not transactiontype='CR' or not authorizername='MAVERICK'"));
    }
    dropField("card", "authorizername");
    if(!fieldExists("card", "cardid")) {
      int rowsub = getIntFromQuery(QueryString.Clause("select min(rowid) from card"))-1;
      update(QueryString.Clause("alter table card add cardid integer"));
      // populate it
      update(QueryString.Clause("update card set cardid = rowid-"+rowsub));
      update(QueryString.Clause("alter table card modify cardid serial")); // +++ keep fingers crossed
      validatorAddPrimaryKey("cardpk", "card", "cardid");
    }
    if(fieldExists("card", "transactiontype")) {
      update(QueryString.Clause("rename column card.transactiontype to paytype"));
    }
    if(fieldExists("card", "paymenttypecode")) {
      update(QueryString.Clause("rename column card.paymenttypecode to institution"));
    }
    validatorAddIndex("cardpt", "card", "paytype");
    validatorAddIndex("cardin", "card", "institution");
    validatorAddIndex("cardbinrange", "card", "lowbin, highbin");
dbg.ERROR("VALIDATING ENTERPRISE ...");
    dropField("enterprise", "countrycode");
    if(Safe.equalStrings("CHAR", getColumnDataType("enterprise", "enterpriseid"), true)) {
      // enterpriseid ...
        // convert to integer first, then subtract 99999 EVERYWHERE and THEN convert to serial!
        // enterprise
      update(QueryString.Clause("ALTER TABLE enterprise MODIFY enterpriseid INTEGER"));
      update(QueryString.Clause("update enterprise set enterpriseid=(enterpriseid - 99999)"));
        // associate
      update(QueryString.Clause("ALTER TABLE associate MODIFY enterpriseid INTEGER NOT NULL"));
      update(QueryString.Clause("update associate set enterpriseid=(enterpriseid - 99999)"));
        // store
      update(QueryString.Clause("ALTER TABLE store MODIFY enterpriseid INTEGER NOT NULL"));
      update(QueryString.Clause("update store set enterpriseid=(enterpriseid - 99999)"));
        // enterprise again
      update(QueryString.Clause("ALTER TABLE enterprise MODIFY enterpriseid SERIAL"));
    }
    validatorAddPrimaryKey("enterprisepk", "enterprise", "enterpriseid");
    dropIndex("ist_entid");
    update(QueryString.Clause("alter table store add constraint FOREIGN KEY (enterpriseid) REFERENCES enterprise (enterpriseid) CONSTRAINT stfk_entid"));
dbg.ERROR("VALIDATING HISTORY ...");
    dropTable("history");
dbg.ERROR("VALIDATING TERMINAL & DRAWER ...");
    dropField("terminal", "termidforauth");
    dropField(terminal.name(), associate.colorschemeid.name());
      // and cleanup the incestuous terminal->associate link
    update(QueryString.Clause("delete from terminal where " + (fieldExists("terminal", "p_terminalid") ? "p_" : "") + "terminalid in (select "+(fieldExists("associate", "loginname")?"loginname":"associateid")+" from associate)"));
    // txnbookmark cleanup
    if(tableExists("txnbookmark")) {
      validatorAddIndex("bmi_caid", "txnbookmark", "CardAcceptorID");
      dropField("txnbookmark", "enterpriseid");
      dropField("TXNBOOKMARK", "EVENTTYPE");
      update(QueryString.Clause("ALTER TABLE txnbookmark MODIFY bookmarkid SERIAL"));
      update(QueryString.Clause("rename column txnbookmark.bookmarkid to drawerid"));
      update(QueryString.Clause("rename table txnbookmark to drawer"));
    }
    // terminal
    // change terminalid [in terminal and drawer] into a serial.
    String [] termForn = {
      "drawer",
    };
    String [] termFornIdx = {
      "bmtermid",
    };
    String [] termFornFK = {
      "bmtermid",
    };
    serializeExistingKey("terminal", "terminalid", termForn, "pk_terminalid", termFornIdx, termFornFK, "cardacceptorid, applianceid, terminalid", "CHAR");
    // +++ some records in drawer might be orphaned!!!! Fix them !!!
// +++ next time, after checking on the null drawer.terminalid's (to be sure they aren't critical):
//    update(QueryString.Clause("delete from drawer where terminalid is null"));
//    update(QueryString.Clause("alter table drawer modify terminalid integer not null"));
// +++ on a different pass, delete the renamed fields:
//     alter table terminal drop p_terminalid
//     alter table drawer drop p_terminalid
    validatorAddPrimaryKey("pkdr_drawerid", "drawer", "drawerid");
    // misc little things
    if(fieldExists("tranjour", "modifydatetime")) {
      update(QueryString.Clause("rename column tranjour.modifydatetime to STOODINSTAN"));
    }
    if(fieldExists("tranjour", "modifyemployee")) {
      update(QueryString.Clause("rename column tranjour.modifyemployee to CLIENTREFTIME"));
    }
    // +++ maybe test for these ?
    update(QueryString.Clause("alter table tranjour modify originalstan integer"));
    if(fieldExists("tranjour", "systemtraceauditno")) {
      update(QueryString.Clause("alter table tranjour modify systemtraceauditno integer not null"));
      update(QueryString.Clause("rename column tranjour.systemtraceauditno to STAN"));
    }
    if(fieldExists("receipt", "systemtraceauditno")) {
      update(QueryString.Clause("alter table receipt  modify systemtraceauditno integer not null"));
      update(QueryString.Clause("rename column receipt.systemtraceauditno to STAN"));
    }
dbg.ERROR("VALIDATING ASSOCIATE ...");
    dropField("associate", "managerkey");
    if(!fieldExists(associate.name(), associate.colorschemeid.name())) {
      validatorAddField(associate, associate.colorschemeid);
      update(QueryString.Clause("update associate set colorschemeid='MONEY'"));
    }
    validatorAddIndex("ai_encodedpw", "associate", "encodedpw");
    dropTableConstraint("storeaccess", "pk_sid_assid"); // ignore any exceptions on this about not existing
    String [] assForn = {
      "drawer",
      "storeaccess",
    };
    String [] assFornIdx = {
      "bmassocid",
      "ipk_associdsa",
    };
    String [] assFornFK = {
      null,
      null,
    };
    dropIndex("ipk_enteridass");
    dropIndex("ipk_associateid");
    serializeExistingKey("associate", "associateid", assForn, "pk_associd", assFornIdx, assFornFK, "enterpriseid, associateid", "CHAR");
    // +++ some records in drawer & storeaccess might be orphaned!!!! Fix them !!!
    if(fieldExists("associate", "p_associateid")) {
      update(QueryString.Clause("rename column associate.p_associateid to loginname"));
    }
    update(QueryString.Clause("delete from storeaccess where associateid is null"));
    // ignore exception about not existing on this:
    update(QueryString.Clause("alter table ASSOCIATE add constraint FOREIGN KEY (enterpriseid) REFERENCES enterprise (enterpriseid) CONSTRAINT asfk_entid"));
    update(QueryString.Clause("alter table drawer add constraint FOREIGN KEY (associateid) REFERENCES associate (associateid) CONSTRAINT drfk_assid"));
dbg.ERROR("VALIDATING SERVICECFG ...");
    validateServicecfg();
dbg.ERROR("VALIDATING STORE ...");
    dropIndex("if1_store");
    dropIndex("if2_store");
    dropIndex("if3_store");
    dropIndex("if4_store");
    dropIndex("if5_store");
    dropField("store", "mbrusagegroup");
    dropField("store", "nonmbrusagegroup");
    dropField("store", "activitygroup");
    dropField("store", "membergroup");
    dropField("store", "policygroup");
    dropField("store", "addeddatetime");
    dropField("store", "storenumber");
    dropField("store", "countrycode");
    dropField("store", "region");
    dropField("store", "currencycode");
    dropField("store", "logonstatus");
    dropField("store", "storecutinsaf");
    dropField("store", "controllertype");
    dropField("store", "chkrlogonpinreqd");
    dropField("store", "mgrlogonpinreqd");
    dropField("store", "inputtimeout");
    dropField("store", "processtimeout");
    dropField("store", "messagetimeout");
    dropField("store", "maxpinretries");
    dropField("store", "negcheckprovider");
    dropField("store", "negcheckmbrcode");
    dropField("store", "msopermode");
    dropField("store", "companyid");
    dropField("store", "modifyemployee");
    dropField("store", "modifydatetime");
    validatorAddIndex("si_name", "STORE", "storename");
    validatorAddMissingFields(store);
    mergeEnterpriseStoreAndStore();
    update(QueryString.Clause("delete from store where cardacceptorid IN ('000000000200001', '000000000100001')"));
      // +++ next time: drop table enterprisestore, or do it manually later if all goes well
      // deprecate store 999 (for now; we will create again later)
    String [] store999 = {"store", "paytype", "storeaccess", "receipt","terminal","tranjour","drawer"};
    for(int i = store999.length; i-->0;) {
      String store999table = store999[i];
      update(QueryString.Clause("delete from " + store999table + " where cardacceptorid = '000000000999001'"));
    }
    String [] storeForn = {
      "drawer",
      "storeaccess",
      "terminal",
      "paytype",
    };
    String [] storeFornIdx = {
      "ipk_store",
    };
    String [] storeFornFK = {
      null,
      null,
    };
    serializeExistingKey("store", "cardacceptorid", storeForn, "pk_store", storeFornIdx, storeFornFK, "enterpriseid, storename", "CHAR");
    // +++ some records in drawer, storeaccess, terminal, and paytype might be orphaned!!!! Fix them !!!
    // now, all tables will have both p_cardacceptorid and storeid fields
    // store.standinlimit
    if(fieldExists("store", "standinlimit")) {
      update(QueryString.Clause("rename column store.standinlimit TO oldsilimit"));
      update(QueryString.Clause("alter table store add silimit integer"));
      update(QueryString.Clause("update store set silimit = oldsilimit*100"));
    } // +++ kill oldsilimit later
    // store.storestandintotal
    if(fieldExists("store", "storestandintotal")) {
      update(QueryString.Clause("rename column store.storestandintotal TO oldstoresitotal"));
      update(QueryString.Clause("alter table store add sitotal integer"));
      update(QueryString.Clause("update store set sitotal = sitotal*100"));
    } // +++ kill oldstoresitotal later
dbg.ERROR("VALIDATING APPLIANCE ...");
    // applianceid SERIAL(4)
    // applname    CHAR(32)
    // storeid     INTEGER(4)
    // rptRevision CHAR(12)
    // rptTime     CHAR(14) // to be consistent
    // rptApplTime CHAR(14) // to be consistent
    // rptFreeMem  INTEGER(4)
    // rptTtlMem   INTEGER(4)
    // rptThreadCount INTEGER(4)
    // rptAlarmCount  INTEGER(4)
    // rptStoodTxn    INTEGER(4)
    // rptStoodRcpt   INTEGER(4)
    if(!tableExists("appliance")) {
      update(QueryString.Clause("create table appliance (applianceid SERIAL, applname CHAR(32), storeid INTEGER, rptRevision CHAR(12), rptApplTime CHAR(14), rptTime CHAR(14), rptFreeMem INTEGER, rptTtlMem INTEGER, rptThreadCount INTEGER, rptAlarmCount INTEGER, rptStoodTxn INTEGER, rptStoodRcpt INTEGER)"));
      // ignore bitches about the next line
      update(QueryString.Clause("alter table appliance add constraint FOREIGN KEY (storeid) REFERENCES store (storeid) CONSTRAINT apfk_storeid"));
    }
    validatorAddPrimaryKey("appliancepk", "appliance", "applianceid");
    // temporarily fix the terminal table
    if(Safe.equalStrings(getColumnDataType("appliance", "applianceid"), "CHAR")) {
      update(QueryString.Clause("rename column terminal.applianceid TO applname"));
    }
    if(fieldExists("terminal", "storeid")) {
      update(QueryString.Clause("rename column terminal.storeid TO oldstoreid"));
    }
    if(!fieldExists("terminal", "applianceid")) {
      addField("terminal", "applianceid", "integer");
      // ignore bitches about the next line
      update(QueryString.Clause("alter table terminal add constraint FOREIGN KEY (applianceid) REFERENCES appliance (applianceid) CONSTRAINT tefk_applid"));
    }
    // populate the appliance table ...
    if(getIntFromQuery(QueryString.Clause("select count(*) from appliance")) < 1) {
      Statement stmt = query(QueryString.Clause("select distinct applname, oldstoreid from terminal"));
      if(stmt != null) {
        try {
          ResultSet rs = getResultSet(stmt);
          if(rs != null) {
            while(next(rs)) {
              String applname = getStringFromRS("applname", rs);
              int storeid = getIntFromRS("oldstoreid", rs);
              UniqueId id = new UniqueId();
              update(QueryString.Clause("insert into appliance (applname, storeid) values ('"+applname+"', "+storeid+")"), id);
              update(QueryString.Clause("update terminal set applianceid = "+id.value()+" where applname='"+applname+"' and oldstoreid="+storeid+""), id);
              // +++ check for duplicates later, and then do the NOT NULL and UNIQUE stuff
            }
          } else {
            dbg.ERROR("!!!! RESULTSET IS NULL!!!");
          }
        } catch (Exception ex) {
          dbg.Caught(ex);
        } finally {
          closeStmt(stmt);
        }
      }
    }
dbg.ERROR("VALIDATING STOREAUTH ..."); // was PAYTYPE
    // storeauthid serial
    // storeid integer [FK: store.storeid]
    // paytype CHAR(2)
    // institution CHAR(2)
    // authid INTEGER [FK: authorizer.authid]
    // authmerchid CHAR(?) from where?  [id for the store with that authorizer]
    // authtermid CHAR(?) from where?
    // maxtxnlimit INTEGER
    dropIndex("if2_paytype");
    dropField("paytype", "cashbacklimit");
    dropField("paytype", "floorlimits");
    dropField("paytype", "standinlimit");
    dropField("paytype", "pinlessresub");
    dropField("paytype", "settleinstid");
    dropField("paytype", "cardtype");
    dropField("paytype", "modifyemployee");
    dropField("paytype", "modifydatetime");
    dropField("paytype", "transpriority");
    validatorAddIndex("pti_name", "paytype", "paymenttypename");
    if(tableExists("paytype")) {
      update(QueryString.Clause("rename table paytype to STOREAUTH"));
    }
    if(fieldExists("storeauth", "paymenttypecode")) {
      update(QueryString.Clause("rename column storeauth.paymenttypecode to institution"));
    }
    if(fieldExists("storeauth", "transactiontype")) {
      update(QueryString.Clause("rename column storeauth.transactiontype to paytype"));
    }
    if(fieldExists("storeauth", "merchantid")) {
      update(QueryString.Clause("rename column storeauth.merchantid to authmerchid"));
    }
    if(!fieldExists("storeauth", "storeauthid")) {
      int rowsub = getIntFromQuery(QueryString.Clause("select min(rowid) from storeauth"))-1;
      update(QueryString.Clause("alter table storeauth add storeauthid integer"));
      // populate it
      update(QueryString.Clause("update storeauth set storeauthid = rowid-"+rowsub));
      update(QueryString.Clause("alter table storeauth modify storeauthid serial"));
      validatorAddPrimaryKey("storeauthpk", "storeauth", "storeauthid");
    }
    update(QueryString.Clause("alter table storeauth add constraint FOREIGN KEY (storeid) REFERENCES store (storeid) CONSTRAINT safk_storeid"));
    if(!fieldExists("storeauth", "authid")) {
      update(QueryString.Clause("alter table storeauth add authid integer"));
      // populate it
      int authid = getIntFromQuery(QueryString.Clause("select authid from authorizer where authname='MAVERICK'"));
      update(QueryString.Clause("update table storeauth set authid="+authid));
    }
    if(fieldExists("store", "authtermid")) {
      update(QueryString.Clause("rename column store.authtermid to oldtermid"));
    }
    if(!fieldExists("storeauth", "authtermid")) {
      update(QueryString.Clause("alter table storeauth add authtermid CHAR(10)"));
      Statement stmt = query(QueryString.Clause("select distinct oldtermid, storeid from store"));
      if(stmt != null) {
        try {
          ResultSet rs = getResultSet(stmt);
          if(rs != null) {
            while(next(rs)) {
              String oldtermid = getStringFromRS("oldtermid", rs);
              int storeid = getIntFromRS("storeid", rs);
              update(QueryString.Clause("update storeauth set authtermid = '"+oldtermid+"' where storeid="+storeid));
              // +++ check for duplicates later, and then do the NOT NULL and UNIQUE stuff
            }
          } else {
            dbg.ERROR("!!!! RESULTSET IS NULL!!!");
          }
        } catch (Exception ex) {
          dbg.Caught(ex);
        } finally {
          closeStmt(stmt);
        }
      }
      // +++ later, delete authtermid from store
    }
    if(fieldExists("storeauth", "maxtransamount")) {
      update(QueryString.Clause("rename column storeauth.maxtransamount to oldmax"));
    }
    if(!fieldExists("storeauth", "maxtxnlimit")) {
      update(QueryString.Clause("alter table storeauth add maxtxnlimit integer"));
      update(QueryString.Clause("update storeauth set maxtxnlimit=oldmax*100"));
      // +++ delete oldmax later
    }

/*
DONE:
APPLIANCE
ASSOCIATE
AUTHORIZER
CARD
DRAWER
ENTERPRISE
SERVICECFG
STORE + ENTERPRISESTORE [except for dropping ENTERPRISESTORE]
STOREAUTH
TERMINAL


TODO:
A) Create the TXN table [and TXNHIST table later; identical except that txnid is SERIAL in TXN and INTEGER in TXNHIST]
txnid SERIAL(4)
authid INTEGER(4)
terminalid INTEGER(4)
drawerid INTEGER(4)
institution CHAR(2)
stoodinstan INTEGER(4)
clientreftime CHAR(14)
paytype CHAR(2)
transfertype CHAR(2)
manual CHAR(1)
responsecode CHAR(2) [was hostresponsecode: use hostreponsecode if available, else use responsecode]
transactionamount INTEGER(4)
actioncode CHAR(1)
authstarttime CHAR(14)
authendtime CHAR(14)
authresponsemsg CHAR(16) +++ need to check other authorizers for what length is needed.
approvalcode CHAR(6) [was authidresponse]
authseq INTEGER(4)
authtermid CHAR(10) ?
cardholderaccount CHAR(19) [was 28; truncate where have to]
expirationdate CHAR(4)
authtracedata CHAR(40)
originalstan INTEGER(4)
paytype CHAR(2)
responsecode CHAR(2) ?
stan INTEGER(4)
track1data CHAR(77) [was 76]
track2data CHAR(37)
transtarttime CHAR(14)
tranendtime CHAR(14)
voided CHAR(1) [was voidtransaction]
//    validatorAddIndex("ti_termname", "tranjour", "cardacceptortermid");
//    validatorAddIndex("ti_acctnum", "tranjour", "cardholderaccount");
//    validatorAddIndex("ti_modempl", "tranjour", "modifyemployee");
//    validatorAddIndex("ti_proccode", "tranjour", "processingcode");
//    validatorAddIndex("ti_amount", "tranjour", "transactionamount");
//    validatorAddIndex("ti_action", "tranjour", "actioncode");
//    validatorAddIndex("ti_authname", "tranjour", "authorizername");
//    validatorAddIndex("ti_mdt", "tranjour", "modifydatetime");
//    validatorAddIndex("ti_paytype", "tranjour", "paymenttypecode");
//    validatorAddIndex("ti_stan", "tranjour", "systemtraceauditno");
//    validatorAddIndex("ti_tranend", "tranjour", "tranendtime");
//    validatorAddIndex("ti_voided", "tranjour", "voidtransaction");
//    validatorAddIndex("ti2_transtart", "tranjour", "transtarttime");

B) Skip through the old TRANJOUR table, inserting and translating its records into the TXN table, while renaming receipt files.
   1) Convert the AUTHORIZERNAME to the new AUTHID [do this by looking up the text in the authorizer table, defaulting to PAYMATE if it isn't found, and translating the MAVERICK one to CARDSYSTEMS]
   2) Convert the CARDACCEPTORID + CARDACCEPTORTERMID fields into the new TERMINALID field by looking them up in the terminals table.
   3) Convert the STOODINSTAN [MODIFYDATETIME?] field to an INTEGER (by converting it in code first, since some values have text in them).
   4) Convert the CLIENTREFTIME [MODIFYEMPLOYEE?] field from CHAR(16) to CHAR(14)
   5) Convert the MESSAGETYPE + PROCESSINGCODE + posentrymode fields to new fields, dropping all check txns along the way
     TRANSACTIONTYPE -> PAYTYPE
     MESSAGETYPE -> 400 = transfertype of reversal
     PROCESSINGCODE -> 200030 = transfertype of sale
                       003000 = transfertype of return
     posentrymode -> ?1? = manual of T, all others F
   6) Remove the responsecode field [and just use hostresponsecode?]?
   7) TRANSACTIONAMOUNT: Convert to integer cents from decimal dollars
   8) add drawerid
   9) TXN/TXNHIST needs: storeid, authid, paytypecode, instid, transfertype [sale, return, reversal, reentry; done with an enumeration in code], manual [aka entrysource, boolean], etc.
  10) Use the info from the old table and the inserted TXNID from the new table to rename the receipt file, in place.  If it fails, LOG IT (but move on)! (Don't deal with compression right now.)
C) Do all the drawer work ...
D) INSTITUTION [do with enumeration extension]: institution [PC/GC/IC/AE/DS/DC/MC/VS/...], mintxnlimit [eg: $1 for DS, AE, etc.]
  . in subsequent release, create a table for this and add a mintxnamount field [eg: $1.00 for DS/AE].
E) PAYTYPE [do with enumeration extension, drop table]: paytype [CR/DB/OD/SV/ID/EB/CK...]
F) Create ALL of those primarykeys, foreignkeys, and indexes!
    associate needs a unique index based on associateid+loginname
    validatorAddIndex("isa_stas", "storeaccess", "storeid, associateid");
G) Put all of the NOT NULL constraints on the foreign-key-linked fields (etc).
H) Fixup these queries: tranjour.terminalid ----> terminal.applianceid ----> appliance.storeid
  . Cheap terminals page eg:
  select terminal.*
  from terminal, appliance
  where terminal.applianceid = appliance.applianceid
  and storeid = 4
  order by terminalname asc
  . Cheap, useless eg:
  select tranjour.*
  from tranjour, terminal, appliance
  where terminal.applianceid = appliance.applianceid
  and tranjour.terminalid = terminal.terminalid
  and storeid=4
  order by transtarttime desc

HAVE TO: create a new pending table & translate everything from tranjour into it, adding the txnid serial field before stuffing.
CANNOT DO THIS: alter table tranjour modify txnid serial
HAVE TO: drop the receipt table and rename all the files since creating a new receipt table is a waste of time, and since the info in the receipt table is not going to be in use anymore

LATER:
. Eventually, we may want to create separate records in a different table for every attempt we make to authorize a txn with the authorizer.  The hosttracedata[40], authstarttime[14], authendtime[14], authidresponse[6], and hostresponsecode[2] would go there.
. Eventually, we may want to DROP the track data when moving txns into TXNHIST.
. Need to make colorscheme table and/or have the ColorSchemeID be shorter. This could be accomplished in many ways WITHOUT creating a table.
*/
  }

  /**
   * Eventually do these things in a more "standardized" way
   */
  private void validateAuthorizer() {
    // authorizer:
    // authid SERIAL; primary key: authidpk
    // authname CHAR(8); unique index: iuau_name // for reporting on screens
    // authclass CHAR(80); index: iau_class // the long classname for class loading later, eventually make smaller by making it relative to net.paymate.authorizer?
    if(!tableExists("authorizer")) {
      update(QueryString.Clause("create table authorizer (authid SERIAL, authname CHAR(8), authclass CHAR(80)"));
    } else {
      dropField("authorizer", "servername");
      dropField("authorizer", "authorizerstatus");
      dropField("authorizer", "reversaltype");
      dropField("authorizer", "description");
      dropField("authorizer", "lastdowntime");
      dropField("authorizer", "recinstidcode");
      dropField("authorizer", "securityrelcntrl");
      dropField("authorizer", "retryinterval");
      dropField("authorizer", "resubinterval");
      dropField("authorizer", "resubmittallimit");
      dropField("authorizer", "revresubinterval");
      dropField("authorizer", "revresublimit");
      dropField("authorizer", "reconcilationflag");
      dropField("authorizer", "recontime");
      dropField("authorizer", "hostrecontime");
      dropField("authorizer", "forwardinstidcode");
      dropField("authorizer", "modifyemployee");
      dropField("authorizer", "modifydatetime");
      dropIndex("authorizer_key");
      dropIndex("network");
    }
    // validate the individual fields
    // authid:
    if(!fieldExists("authorizer", "authid")) {
      update(QueryString.Clause("delete from authorizer")); // remove all records (we will stuff later)
      update(QueryString.Clause("alter table authorizer add authid serial")); // add the serial field
    }
    // authname
    if(!fieldExists("authorizer", "authname") && fieldExists("authorizer", "authorizername")) {
      update(QueryString.Clause("rename column authorizer.authorizername to authname"));
    }
    // authclass
    if(!fieldExists("authorizer", "authclass")) {
      update(QueryString.Clause("alter table authorizer add authclass CHAR(80)")); // large classnames; no biggy, few rows
    }
    // primary/foreign keys and indexes:
    validatorAddPrimaryKey("authidpk", "authorizer", "authid");
    validatorAddIndex("iuau_name", "authorizer", "authname"); // +++ make it unique !!!
    validatorAddIndex("iau_class", "authorizer", "authclass");
    // ensure proper content:
    if(fieldExists("authorizer", "authclass") && fieldExists("authorizer", "authid") && (getIntFromQuery(QueryString.Clause("select count(*) from authorizer")) < 2)) {
      update(QueryString.Clause("insert into authorizer (authname, authclass) values ('PAYMATE', 'net.paymate.authorizer.NullAuthorizer')"));
      update(QueryString.Clause("insert into authorizer (authname, authclass) values ('MAVERICK', 'net.paymate.authorizer.cardSystems.CardSystemsAuth')"));
    }
  }

  private void validateServicecfg() {
    //SERVICECFG:
    //. servicecfgid SERIAL; primarykey: servicecfgpk
    //. servicename  CHAR(16); index
    //. paramname    CHAR(16); index
    //. paramvalue   CHAR(320)
    // unique index: servicename+paramname
    if(!tableExists("servicecfg")) {
      update(QueryString.Clause("create table servicecfg (servicecfgid serial, servicename CHAR(16), paramname CHAR(16), paramvalue CHAR(320)"));
    }
    // primarykeys and indexes:
    validatorAddPrimaryKey("servicecfgpk", "servicecfg", "servicecfgid");
    validatorAddIndex("isc_svcname", "servicecfg", "servicename");
    validatorAddIndex("isc_paramname", "servicecfg", "paramname");
    validatorAddIndex("isc_svcparam", "servicecfg", "servicename, paramname"); // +++ needs to be unique???
    // ensure proper content:
    if(getIntFromQuery(QueryString.Clause("select count(*) from servicecfg")) < 1) {
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('MAVERICK', 'timeout', '35000')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('MAVERICK', 'connectPort', '2010')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('MAVERICK', 'connectIPAddress', '207.247.99.115 207.247.99.116')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('MAVERICK', 'buffersize', '1024')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('MAVERICK', 'lowseq', '1')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('MAVERICK', 'highseq', '9999')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('CONNECTIONSERVER', 'receiptPath', '/receipts')"));
      boolean isSolaris = OS.isSolaris();
      String servername = (isSolaris ? "helios" : (OS.isWin2K() ? "spore" : "andysomething"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'computername', '"+servername+"')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'backupPath', '/data/backups')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'newsFile', 'data/config/news')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'statusMacid', '"+(isSolaris?"HELIOSSERVER":"unknownSrvr")+"')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'statusServer', '64.92.151.10')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'statusIntervalMs', '"+(isSolaris?60000:0)+"')"));
      update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('USERSESSION', 'defaultColorscheme', '"+(isSolaris?"MONEY":"TRANQUILITY")+"')"));
      String [] grouplists = {
        "alertList",
        "bootupList",
        "staleAppliance",
      };
      for(int i = grouplists.length; i-->0;) {
        try {
          String cc = "";
          try {
            File listfile=new File(File.separator + "paymate.cfg",grouplists[i]);
            BufferedReader burf=new BufferedReader(new FileReader(listfile));
            String oneaddress;
            while((oneaddress=burf.readLine())!=null){//defering substance check allows for blank lines
              if(Safe.hasSubstance(oneaddress)&&oneaddress.charAt(0)!='#'){//must be in first column of file
                cc += ((Safe.NonTrivial(cc)) ? "," : "")+oneaddress.trim();
              }
            }
          } catch (Exception e2) {
            dbg.Caught(e2);
          }
          String servicename = "unset";
          if(grouplists[i].equalsIgnoreCase("alertList")) {
            servicename = "MAVERICK";
          } else if (grouplists[i].equalsIgnoreCase("bootupList")) {
            servicename = "CONNECTIONSERVER";
          } else if (grouplists[i].equalsIgnoreCase("staleAppliance")) {
            servicename = "APPLIANCETRACKER";
          }
          update(QueryString.Clause("insert into servicecfg (servicename, paramname, paramvalue) values ('"+servicename+"', '"+grouplists[i]+"', '"+cc+"')"));
        } catch (Exception e) {
          dbg.Caught(e);
        }
      }
    }
  }

  private final void serializeExistingKey(String primaryTable, String fieldName, String [] secondaryTable, String primaryTableConstraint, String [] secondaryTableIndexe, String [] secondaryTableConstraint, String skiporder, String oldtype) {
    try {
      String testedtype = getColumnDataType(primaryTable, fieldName);
      dbg.ERROR("oldtype = " + oldtype + ", testedtype = " + testedtype);
      if(Safe.equalStrings(oldtype, testedtype, true)) {
        if(secondaryTable.length != secondaryTableConstraint.length) {
          dbg.ERROR("secondaryTable.length != secondaryTableConstraint.length  !!");
        } else {
          if(Safe.NonTrivial(primaryTableConstraint)) {
            dropTableConstraint(primaryTable, primaryTableConstraint);
          }
          for(int i = secondaryTable.length; i-->0;) {
            String constraint = secondaryTableConstraint[i];
            if(Safe.NonTrivial(constraint)) {
              dropTableConstraint(secondaryTable[i], constraint);
            }
          }
          for(int i = secondaryTableIndexe.length; i-->0;) {
            dropIndex(secondaryTableIndexe[i]);
          }
          String tmpname = "P_"+fieldName;
          update(QueryString.Clause("rename column "+primaryTable+"."+fieldName+" to "+tmpname));
          for(int i = secondaryTable.length; i-->0;) {
            update(QueryString.Clause("rename column "+secondaryTable[i]+"."+fieldName+" to "+tmpname));
          }
          update(QueryString.Clause("alter table "+primaryTable+" add "+fieldName+" integer")); // +++ make serial
          for(int i = secondaryTable.length; i-->0;) {
            update(QueryString.Clause("alter table "+secondaryTable[i]+" add "+fieldName+" integer" /* +++ not null*/));
          }
          Statement sus = query(QueryString.Clause("select * from "+primaryTable+(Safe.NonTrivial(skiporder) ? " order by " + skiporder : "")));
          if(sus != null) {
            try {
              ResultSet rs = getResultSet(sus);
              int idometer = 0;
              while(next(rs)) {
                String id = getStringFromRS(tmpname, rs);
                String ext = " set " + fieldName + " = " + (++idometer) + " where " + tmpname + " = '" + id + "'";
                update(QueryString.Clause("update " + primaryTable + ext));
                for(int i = secondaryTable.length; i-->0;) {
                  update(QueryString.Clause("update " + secondaryTable[i] + ext));
                }
              }
            } catch (Exception ie) {
              dbg.Caught(ie);
            } finally {
              closeStmt(sus);
            }
          }
          update(QueryString.Clause("alter table "+primaryTable+" modify "+fieldName+" serial"));
          String pkname = "pka_"+fieldName;
          validatorAddPrimaryKey(pkname, primaryTable, fieldName);
          for(int i = secondaryTable.length; i-->0;) {
            String fkname = "fka_"+fieldName;
            update(QueryString.Clause("alter table "+secondaryTable[i]+" add constraint FOREIGN KEY ("+fieldName+") REFERENCES "+primaryTable+" ("+fieldName+") CONSTRAINT "+fkname));
          }
          // +++ on a different pass, delete the renamed fields
        }
      }
    } catch (Exception e3) {
      dbg.Caught(e3);
    }
  }

  private final void mergeEnterpriseStoreAndStore() {
    Statement sus = null;
    try {
      if(tableExists("enterprisestore")) {
        sus = query(QueryString.Clause("select * from store"));
        if(sus != null) {
          ResultSet rs = getResultSet(sus);
          while(next(rs)) {
            String caid = getStringFromRS("cardacceptorid", rs);
            update(QueryString.Clause(
"UPDATE store SET "+
"(debitallowed, checksallowed, creditallowed, authtermid, autoapprove, enterpriseid, freepass, javatz, receiptabide, receiptheader, receiptshowsig, receipttagline, receipttimeformat, storehomepage, stanometer) = " +
"('F'         , 'F'          , 'T'          , (SELECT authtermid, autoapprove, enterpriseid, freepass, javatz, receiptabide, receiptheader, receiptshowsig, receipttagline, receipttimeformat, storehomepage, stanometer FROM enterprisestore " +
"WHERE cardacceptorid='" + caid + "')) WHERE cardacceptorid='" + caid + "'"
));
          }
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      if(sus != null) {
        closeStmt(sus);
      }
    }
  }

/*
  // installed for liberty1 (at least)
  private final void liberty1() {
    // first you may have to deal with the constraints
    dropTableConstraint("RespCode", "f1_RespCode");
    dropTableConstraint("Tranjour", "f1_Tranjour");
    dropTableConstraint("Activity_List", "f1_Activity_List");
    dropTableConstraint("Customers", "f1_Customers");
    dropTableConstraint("Store", "f1_Store");
    dropTableConstraint("Store", "f2_Store");
    dropTableConstraint("Store", "f3_Store");
    dropTableConstraint("Store", "f4_Store");
    dropTableConstraint("Store", "f5_Store");
    dropTableConstraint("Settlement_Stores", "f1_Settlement_Sto");
    dropTableConstraint("Settlement_Stores", "f2_Settlement_Sto");
    dropTableConstraint("PayType", "f1_PayType");
    dropTableConstraint("PayType", "f2_PayType");
    dropTableConstraint("Bad_Check_History", "f1_Bad_Check_Hist");
    dropTableConstraint("Bad_Check_History", "f2_Bad_Check_Hist");
    dropTableConstraint("Bad_Check_History", "f3_Bad_Check_Hist");
    dropTableConstraint("Bad_Check_History", "f4_Bad_Check_Hist");
    dropTableConstraint("Bad_Check_History", "f5_Bad_Check_Hist");
    dropTableConstraint("Payment_History", "f1_Payment_Histor");
    dropTableConstraint("Payment_History", "f2_Payment_Histor");
    dropTableConstraint("Check_Events", "f1_Check_Events");
    dropTableConstraint("Check_Events", "f2_Check_Events");
    dropTableConstraint("Check_Events", "f3_Check_Events");
    dropTableConstraint("Check_Events", "f1_Check_Event_Lo");
    dropTableConstraint("ID_List", "f1_ID_List");
    dropTableConstraint("ID_List", "f2_ID_List");
    dropTableConstraint("ID_Priority", "f1_ID_Priority");
    dropTableConstraint("ID_Priority", "f2_ID_Priority");
    dropTableConstraint("Member_cc", "f1_Member_cc");
    dropTableConstraint("Member_cmnt", "f1_Member_cmnt");
    dropTableConstraint("Members", "f1_Members");
    dropTableConstraint("Members", "f2_Members");
    dropTableConstraint("Members", "f3_Members");
    dropTableConstraint("Members", "f4_Members");
    dropTableConstraint("Members", "f5_Members");
    dropTableConstraint("Member_Detail", "fp_Member_Detail");
    dropTableConstraint("Member_Checks", "f1_Member_Checks");
    dropTableConstraint("Member_Checks", "f2_Member_Checks");
    dropTableConstraint("Demographics", "fp_Demographics");
    dropTableConstraint("Negative_IDs", "f1_Negative_IDs");
    dropTableConstraint("Negative_IDs", "f2_Negative_IDs");
    dropTableConstraint("Negative_Vendors", "f1_Negative_Vendo");
    dropTableConstraint("Negative_Vendors", "f2_Negative_Vendo");
    dropTableConstraint("Payment_IDs", "f1_Payment_IDs");
    dropTableConstraint("Payment_IDs", "f2_Payment_IDs");
    dropTableConstraint("Positive_IDs", "f1_Positive_IDs");
    dropTableConstraint("Positive_IDs", "f2_Positive_IDs");
    dropTableConstraint("Required_Sets", "f1_Required_Sets");
    dropTableConstraint("Required_IDs", "f1_Required_IDs");
    dropTableConstraint("Required_IDs", "f2_Required_IDs");
    dropTableConstraint("Required_IDs", "f3_Required_IDs");
    dropTableConstraint("Usage_Limits", "f1_Usage_Limits");
    dropTableConstraint("Usage_Limits", "f1_Usage_Limits");
    dropTableConstraint("ACH_Members", "f1_ACH_Members");
    dropTableConstraint("ACH_Members", "f2_ACH_Members");
    dropTableConstraint("ACH_Members", "f3_ACH_Members");
    dropTableConstraint("ACH_Batches", "f1_ACH_Batches");
    dropTableConstraint("GC_History", "f1_GC_History");
    dropTableConstraint("GC_Reissues", "f1_GC_Reissues");
    dropTableConstraint("GC_Reissues", "f2_GC_Reissues");
    // drop all of those tables we don't want (77 tables):
    dropTable("ACH_BATCHES");// (empty)
    dropTable("ACH_BUILD_BATCHES");// (empty)
    dropTable("ACH_CODES");// (contents will probably end up in sourcecode)
    dropTable("ACH_CONFIGURATIONS");// (1 entry, looks like ACH group settings)
    dropTable("ACH_ENTRIES");// (empty)
    dropTable("ACH_MEMBERS");// (empty)
    dropTable("ACH_SETTLEMENT");// (empty)
    dropTable("ACH_STATUS");// (9 entries will end up in code)
    dropTable("ACH_TYPES");// (8 entries will end up in code)
    dropTable("ACTION_CODES");// (6 entries will end up in code)
    dropTable("ACTIVITY_GROUPS");// (3 unused entries; was for linking activitygroup settings to stores)
    dropTable("ACTIVITY_LIST");// (ditto, see above)
    dropTable("BAD_CHECK_CODES");// (47 entries will end up in code)
    dropTable("BAD_CHECK_HISTORY");// (empty, will rebuild later if needed)
    dropTable("BATCHMGT");// (replacing with the txnbookmark table)
    dropTable("CARD_TYPES");// (6 entries will be in code)
    dropTable("CHECK_EVENT_LOG");// (will redesign if needed later)
    dropTable("CHECK_EVENTS");// (another hostreponse -> clientresponse mapper)
    dropTable("CHECK_TYPES");// (will end up in code)
    dropTable("COMMANDS");// (database scripts)
    dropTable("CONFIGURATION");// (system configs; we are replacing with our own soon)
    dropTable("CUSTOMERS");// (redesign when needed)
    dropTable("DBHISTORY");// (more scripting crap)
    dropTable("DEACTIVATEDCODES");// (will end up in code)
    dropTable("DEMOGRAPHICS");// (empty)
    dropTable("DRIVERSLIC");// (masks; will end up in code, but need to save to file for extraction later)
    dropTable("EMPLOYEE");// (unused except for mainsail direct use)
    dropTable("EVENTLOG");// (unused except by mainsail)
    dropTable("EVENTS");// (mainsail mapping; unneeded)
    dropTable("GC_ACCOUNTS");// (1; gift certificates; will redesign when needed)
    dropTable("GC_CONFIGURATION");// (ditto)
    dropTable("GC_HISTORY");// (ditto)
    dropTable("GC_REISSUES");// (empty, ditto)
    dropTable("GC_VALUES");// (ditto, will end up in code)
    dropTable("GC_VENDORS");// (will redo, empty)
    dropTable("GEOGRAPHICS");// (ditto)
    dropTable("ID_LIST");// (will redo when needed)
    dropTable("ID_PRIORITY");// (ditto)
    dropTable("ID_TYPES");// (redo later; in code)
    dropTable("INQUIRIES");// (redo later)
    dropTable("ISOFLDS");// (not used)
    dropTable("KEYTABLE");// (redo later)
    dropTable("MEMBER_CC");// (empty, unused)
    dropTable("MEMBER_CCAVAIL");// (ditto)
    dropTable("MEMBER_CHECKS");// (ditto)
    dropTable("MEMBER_CMNT");// (ditto)
    dropTable("MEMBER_CMNTTYPE");// (ditto)
    dropTable("MEMBER_DETAIL");// (ditto)
    dropTable("MEMBER_GROUPS");// (1, unused)
    dropTable("MEMBER_TYPES");// (3, unused)
    dropTable("MEMBERS");// (2, unused)
    dropTable("NEGATIVE_IDS");// (empty)
    dropTable("NEGATIVE_VENDORS");// (empty)
    dropTable("PAYMENT_HISTORY");// (unused)
    dropTable("PAYMENT_IDS");// (unused, redo when needed)
    dropTable("POLICY_GROUPS");// (unused)
    dropTable("POSITIVE_IDS");// (unused)
    dropTable("REASON_CODES");// (will end up in code, not needed right now; equivalent of our ActionReplyStatus)
    dropTable("RECON");// (empty)
    dropTable("RECON_CODES");// (unused, will end up in code)
    dropTable("REQUIRED_IDS");// (unused)
    dropTable("REQUIRED_SETS");// (unused)
    dropTable("RESPCODE");// (in code) -- or should be (I think I checked these, and we have them)
    dropTable("RETCHECK");// (empty)
    dropTable("SAFQUEUE");// (empty; mainsail standin only)
    dropTable("SCHEDULEDJOBS");// (unused; scripting)
    dropTable("SETTLEMENT_CODES");// (unused)
    dropTable("SETTLEMENT_FILES");// (empty)
    dropTable("SETTLEMENT_INST");// (1 bogus; unused)
    dropTable("SETTLEMENT_STORES");// (empty)
    dropTable("STAN");// (used for mainsail internals only, we don't need it)
    dropTable("STATES");// (unused; in code)
    dropTable("STORE_AUTHORIZERS");// (putting into stores table)
    dropTable("STOREBATCH");// (do our own later / txnbookmark)
    dropTable("USAGE_GROUPS");// (1; unused)
    dropTable("USAGE_LIMITS");// (1; unused; redo when we need it)
    dropTable("VENDOR_CODES");// (empty)
    // drop all of those tranjour fields we don't want (saves 245 bytes per row):
    dropField("tranjour", "acquireridcode");
    dropField("tranjour", "altidtype");
    dropField("tranjour", "batchnumber");// - will add back in later as a different data type
    dropField("tranjour", "cashbackamount");
    dropField("tranjour", "checktype");
    dropField("tranjour", "demomodeindicator");
    dropField("tranjour", "employeenumber");
    dropField("tranjour", "eventnumber");
    dropField("tranjour", "hostprocessingcode");
    dropField("tranjour", "hostsettlementdate");
    dropField("tranjour", "laterespindicator");// - not even sure what this was for
    dropField("tranjour", "logtodisktimer");// - not even sure what this was for; use for "receipt" logging
//time? ?????
//posconditioncode ?
//posentrymode ?
    dropField("tranjour", "postracedata");
    dropField("tranjour", "recinstidcode");
    dropField("tranjour", "retrievalrefno");
    dropField("tranjour", "reversalindicator");
    dropField("tranjour", "reversaltype");
    dropField("tranjour", "servername");// ? maybe put "helios" in here?
    dropField("tranjour", "settleinstid");
    dropField("tranjour", "settlementdate");// - will add later with different data type
    dropField("tranjour", "settlementfile");
    dropField("tranjour", "switchdatetime");
//    dropField("tranjour", "timeoutindicator");// - if still not approved, indicates we need to retry
    dropField("tranjour", "transactiondate");
    dropField("tranjour", "transactiontime");
    dropField("tranjour", "transgtrid");
    dropField("tranjour", "transmissiontime");
    dropField("tranjour", "vouchernumber");
    // drop all of those other fields we don't want:
    dropField("associate", "addedemployee");
    dropField("associate", "addeddatetime");
    dropField("associate", "modifyemployee");
    dropField("associate", "modifydatetime");
    dropField("enterprise", "addedemployee");
    dropField("enterprise", "addeddatetime");
    dropField("enterprise", "modifyemployee");
    dropField("enterprise", "modifydatetime");
//    dropField("enterprisestore", "addedemployee");
//    dropField("enterprisestore", "addeddatetime");
//    dropField("enterprisestore", "modifyemployee");
//    dropField("enterprisestore", "modifydatetime");
    dropField("receipt", "modifyemployee");
    dropField("receipt", "modifydatetime");
    dropField("receipt", "addeddatetime");
    dropField("receipt", "addedemployee");
    dropField("storeaccess", "addedemployee");
    dropField("storeaccess", "addeddatetime");
    dropField("storeaccess", "modifyemployee");
    dropField("storeaccess", "modifydatetime");
    dropField("terminal", "addedemployee");
    dropField("terminal", "addeddatetime");
    dropField("terminal", "modifyemployee");
    dropField("terminal", "modifydatetime");
    // other unused fields
    dropField("enterprise", "associateid");
//    dropField("enterprisestore", "daylightsavings");
//    dropField("enterprisestore", "weblogo");
    dropField("terminal", "browsable");
    dropField("terminal", "printermodel");

    // liberty1: added new authorization to tranjour
//    validatorAddField(tranjour, tranjour.authstat);
    // liberty1: added new authtermid to tranjour
    validatorAddField(tranjour, tranjour.authtermid);
    // liberty1
    validatorAddField(store, store.autoapprove);
    validatorAddField(store, store.freepass);
    validatorAddField(store, store.authtermid);
    stuffAuthTermId();
    // liberty1: added new authseq to tranjour
    validatorAddField(tranjour, tranjour.authseq);
  }
*/

  // +++ generalize and move into DBMacros!
  private final boolean dropTableConstraint(String tablename, String constraintname) {
    String functionName = "dropTableConstraint";
    int success = FAILED;
    String toDrop = "drop constraint " + tablename + "." + constraintname;
    try {
      trc.Enter(functionName);
      trc.mark(toDrop);
      success = (tableExists(tablename)) ?
                update(QueryString.Clause("ALTER TABLE " + tablename + " DROP CONSTRAINT " + constraintname)) :
                ALREADY;
    } catch (Exception e) {
      // muffle
      //trc.Caught(e);
    } finally {
      trc.ERROR(functionName + ": '" + toDrop +
        ((success == ALREADY) ?
         " Can't perform since table doesn't exist." :
         ((success==DONE)?
          "' Succeeded!":
          "' FAILED!  Constraint probably didn't exist.")));
      trc.mark("");
      trc.Exit();
      return (success!=FAILED);
    }
  }

//  private final void stuffAuthTermId() {
//    String functionName = "stuffAuthTermId";
//    String tablename = store.name();
//    String fieldname = store.authtermid.name();
//    try {
//      trc.Enter(functionName);
//      trc.mark("Stuffing field " + tablename + "." + fieldname);
//      if(fieldExists(tablename, fieldname)) {
//        Statement stores = null;
//        TextList storesToFix = new TextList();
//        try {
//          stores = query(QueryString.Select(STOREID).from(store.name()).where().isEmpty(store.authtermid.name()));
//          ResultSet rs = getResultSet(stores);
//          while(next(rs)) {
//            String caid = getStringFromRS("cardacceptorid", rs);
//            storesToFix.add(caid);
//          }
//        } catch (Exception e) {
//          trc.Caught(e);
//        } finally {
//          closeStmt(stores);
//        }
//        if(storesToFix.size() == 0) {
//          dbg.ERROR("No stores need authtermid set.");
//        } else {
//          for(int istore = storesToFix.size(); istore-->0; ) {
//            Statement termstmt = null;
//            String caid = storesToFix.itemAt(istore);
//            try {
//              termstmt = query(QueryString.Clause("select distinct termidforauth from terminal where cardacceptorid='"+caid+"'"));
//              ResultSet termrs = getResultSet(termstmt);
//              if(next(termrs)) {
//                String authtermid = getStringFromRS("termidforauth", termrs);
//                trc.ERROR((update(QueryString.Clause("update store set authtermid = '" + authtermid + "' where cardacceptorid='"+caid+"'"))>0 ? "SUCCEEDED" : "DID NOT SUCCEED") +" in updating authtermid in store for cardacceptorid="+caid);
//              } else {
//                trc.ERROR("No terminal.termidforauth records found for fixing store '" + caid + "'.  This store might not have any terminals.");
//              }
//            } catch (Exception e) {
//              trc.Caught(e);
//            } finally {
//              closeStmt(termstmt);
//            }
//          }
//        }
//      }
//    } catch (Exception e) {
//      trc.Caught(e);
//    } finally {
//      trc.Exit();
//    }
//  }

  private final int validatorAddMissingFields(TableProfile table) {
    ColumnProfile [] cps = table.columns();
    int count = 0;
    for(int i = 0; i < cps.length; i++) {
      count += validatorAddField(table, cps[i]);
    }
    return count;
  }

  private final int validatorAddField(TableProfile table, ColumnProfile column) {
    String functionName = "validatorAddField(fromProfile)";
    int success = FAILED;
    try {
      trc.Enter(functionName);
      trc.mark("Add field " + table.name() + "." + column.name());
      if(!fieldExists(table.name(), column.name())) {
        boolean did = addField(table, column);
        success = (did ? DONE : FAILED);
        trc.ERROR((did ? "Added" : "!! COULD NOT ADD") + " field " + table.name() + "." + column.name());
      } else {
        success = ALREADY;
        trc.ERROR("Field " + table.name() + "." + column.name() + " already added.");
      }
    } catch (Exception e) {
      trc.Caught(e);
    } finally {
      if(success == FAILED) {
        trc.ERROR(functionName + ":" + " FAILED!");
      }
      trc.mark("");
      trc.Exit();
      return success;
    }
  }

  private final int validatorAddField(String table, String field, String etc) {
    String functionName = "validatorAddField";
    int success = FAILED;
    try {
      trc.Enter(functionName);
      trc.mark("Add field " + table + "." + field);
      if(!fieldExists(table, field)) {
        boolean did = addField(table, field, etc);
        success = (did ? DONE : FAILED);
        trc.ERROR((did ? "Added" : "!! COULD NOT ADD") + " field " + table + "." + field);
      } else {
        success = ALREADY;
        trc.ERROR("Field " + table + "." + field + " already added.");
      }
    } catch (Exception e) {
      trc.Caught(e);
    } finally {
      if(success == FAILED) {
        trc.ERROR(functionName + ":" + " FAILED!");
      }
      trc.mark("");
      trc.Exit();
      return success;
    }
  }

  private final int validatorAddTable(TableProfile table) {
    String functionName = "validatorAddTable";
    int success = FAILED;
    try {
      trc.Enter(functionName);
      trc.mark("Add table " + table.name());
      if(!tableExists(table.name())) {
        boolean did = createTable(table);
        success = (did ? DONE : FAILED);
        trc.ERROR((did ? "Added" : "!! COULD NOT ADD") + " table " + table.name());
      } else {
        success = ALREADY;
        trc.ERROR("Table " + table.name() + " already added.");
      }
    } catch (Exception e) {
      trc.Caught(e);
    } finally {
      if(success == FAILED) {
        trc.ERROR(functionName + ":" + " FAILED!");
      }
      trc.mark("");
      trc.Exit();
      return success;
    }
  }

  // +++ generalize internal parts and move to DBMacros!
  private final int validatorAddPrimaryKey(String constraintName, String tableName, String fieldExpression) {
    String functionName = "validatorAddPrimaryKey";
    int success = FAILED;
    try {
      trc.Enter(functionName);
      trc.mark("Add Primary Key " + constraintName + " for " + tableName + ":" + fieldExpression);
      if(!primaryKeyExists(tableName, constraintName)) {
        QueryString qs = QueryString.PrimaryKeyConstraint(constraintName, tableName, fieldExpression);
        if(update(qs) != -1) {
          success = DONE;
          trc.ERROR(functionName + ": SUCCEEDED!");
        }
      } else {
        success = ALREADY;
        trc.ERROR("Primary Key " + constraintName + " for table " + tableName + " already added.");
      }
    } catch (Exception e) {
      trc.Caught(e);
    } finally {
      if(success == FAILED) {
        trc.ERROR(functionName + ": FAILED!");
      }
      trc.mark("");
      trc.Exit();
      return success;
    }
  }

  // +++ generalize internal parts and move to DBMacros!
  private final int validatorAddIndex(String indexName, String tableName, String fieldExpression) {
    String functionName = "validatorAddIndex";
    int success = FAILED;
    try {
      trc.Enter(functionName);
      trc.mark("Add Index " + indexName + " for " + tableName + ":" + fieldExpression);
      if(!indexExists(indexName)) {
        QueryString qs = QueryString.CreateIndex(indexName, tableName, fieldExpression);
        if(update(qs) != -1) {
          success = DONE;
          trc.ERROR(functionName + ": SUCCEEDED!");
        }
      } else {
        success = ALREADY;
        trc.ERROR("Index " + indexName + " already exists!");
      }
    } catch (Exception e) {
      trc.Caught(e);
    } finally {
      if(success == FAILED) {
        trc.ERROR(functionName + ": FAILED!");
      }
      trc.mark("");
      trc.Exit();
      return success;
    }
  }

  //////////////////////////////////////////
  //query debugger
  public static final void spout(String s){
    // +_+ use DBG!  No system.out in program if at all possible!
    //==> this is for use by a main. A reasonable exception to the above rule.
    // --- not considering the dbg's already exist once the program loads *and are outputting any errors that are being generated already*
    // --- create a PrintFork to do your html'izing and use that.
    System.out.print("<tr><td>");
    System.out.println(s);
    System.out.print("</td></tr>");
  }
  public static final void spout(QueryString s){
    spout(s.toString());
  }

  public static final void main(String argv[]){
    TerminalID T=new TerminalID(10000,5);
    TimeRange ranger=TimeRange.Create("MMddHHmmss",TRANSTARTTIME);
    ranger.setEnd(Safe.Now()).setStart(LocalTimeFormat.genesis);
    LoginInfo li=new LoginInfo();
    li.clerk.setName("%userid%").setPass("%rawpassword%");
    li.enterpriseID=10000;
    TxnRow reck= TxnRow.forTesting();

    boolean all=(argv.length==0);
    if(all){
      argv=new String[1];
      System.out.println("<table border>");
    }
    for(int i=argv.length;i-->0;){
      String methodname=argv[i];
      if(!all){
        spout(methodname+":");
      }
      if(all|| methodname.equalsIgnoreCase("tiftQuery")){//1
        spout(tiftQuery(reck));
      }
      if(all|| methodname.equalsIgnoreCase("assocQry")){//1
        spout(assocQry(li));
      }
      if(all||methodname.equalsIgnoreCase("upstan")){//1
        spout(upstan(5,90210));
      }
      if(all|| methodname.equalsIgnoreCase("StoreIs")){//1
        spout(StoreIs(5));
      }
      if(all|| methodname.equalsIgnoreCase("caidOfEnterprise")){//1
        spout(caidOfEnterprise(10000));
      }
      if(all|| methodname.equalsIgnoreCase("TerminalNamed")){//1
        spout(TerminalNamed("%termname%"));
      }
      if(all|| methodname.equalsIgnoreCase("AllTxnsButVoids")){//1
        spout(NoVoids());
      }
      if(all|| methodname.equalsIgnoreCase("StoreAgrees")){//1
        spout(StoreAgrees());
      }
      if(all|| methodname.equalsIgnoreCase("genStoresQuery")){//1
        spout(genStoresQuery(10000));
      }
      if(all|| methodname.equalsIgnoreCase("genAssociatesQuery")){//1
        spout(genAssociatesQuery(10000));
      }
      if(all|| methodname.equalsIgnoreCase("genStoreInfo")){//1
        spout(genStoreInfo(5));
      }
      if(all|| methodname.equalsIgnoreCase("ApplianceInfoQry")){//1
        spout(ApplianceInfoQry("%macidgoeshere%"));
      }
      if(all|| methodname.equalsIgnoreCase("genTerminal")){//1
        spout(genTerminal(10000));
      }
      if(all|| methodname.equalsIgnoreCase("TerminalIs")){//1
        spout(TerminalIs(10000));
        spout(TerminalIs(T));
      }
      if(all|| methodname.equalsIgnoreCase("genEnterpriseFromTerminalQuery")){//1
        spout(genEnterpriseFromTerminalQuery("%macidgoeshere%"));
      }
      if(all|| methodname.equalsIgnoreCase("genCloseShift")){//1
        spout(genCloseShift(T,ranger,li));
      }
      if(all|| methodname.equalsIgnoreCase("timeClause")){//1
        spout(timeClause(">=","%yyyymmddhhmss%"));
      }
      if(all|| methodname.equalsIgnoreCase("genStartTxn")){
        spout(genStartTxn(reck));
      }
      if(!all){
        spout( "not known");
      }
    }
    if(all){
      System.out.println("</table>");
    }
  }

}
//$Id: PayMateDB.java,v 1.37 2001/11/17 20:06:36 mattm Exp $
