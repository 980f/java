package net.paymate.database;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/PayMateDB.java,v $
 * Description:  structured queries for the DBMS (etc)<p>
 * Copyright:    2000, PayMate.net3520<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Revision: 1.389 $
 */

import java.lang.reflect.*; // for testing the queries
import java.sql.*;
import java.util.*;
import net.paymate.authorizer.*;
import net.paymate.awtx.*;
import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.data.sinet.*;
import net.paymate.data.sinet.business.*;
import net.paymate.data.sinet.hardware.*;
import net.paymate.database.ours.*;
import net.paymate.database.ours.query.*;
import net.paymate.database.ours.table.*;
import net.paymate.lang.*;
import net.paymate.terminalClient.Receipt;
import net.paymate.terminalClient.ReceiptFormat;
import net.paymate.terminalClient.TerminalCapabilities;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.awt.Hancock; // for signatures

public class PayMateDB extends DBMacros implements Database, ConfigurationManager, HardwareCfgMgr /* do NOT add DBCONSTANTS here */, BusinessCfgMgr {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PayMateDB.class, ErrorLogStream.WARNING);

  static {
    appliance.foreignKeys = applianceForeignKeys;
    associate.foreignKeys = associateForeignKeys;
    authattempt.foreignKeys = authattemptForeignKeys;
    batch.foreignKeys = batchForeignKeys;
    drawer.foreignKeys = drawerForeignKeys;
    // +++ eventually enterprise.foreignKeys = enterpriseForeignKeys;
    storeaccess.foreignKeys = storeAccessForeignKeys;
    storeauth.foreignKeys = storeauthForeignKeys;
    store.foreignKeys = storeForeignKeys;
    termauth.foreignKeys = termauthForeignKeys;
    terminal.foreignKeys = terminalForeignKeys;
    txn.foreignKeys = txnForeignKeys;
  }

  private static TableProfile[] SINETTABLES;
  private static void prepareSinetTables() {
    int count = new SinetClass().numValues();
    dbg.ERROR("prepareSinetTables() setting SINETTABLES length to " + count);
    SINETTABLES = new TableProfile[count];
    for(int i = count; i-- > 0; ) {
      TableProfile tp = null;
      switch(i) {
// +++ eventually put these values into the TableProfiles themselves?
        case SinetClass.Enterprise:
          tp = enterprise;
          break;
        case SinetClass.Store:
          tp = store;
          break;
        case SinetClass.Appliance:
          tp = appliance;
          break;
// no persistence of appliance log data 20040523 !
//        case SinetClass.ApplNetStatus:
//          tp = applnetstatus;
//          break;
//        case SinetClass.ApplPgmStatus:
//          tp = applpgmstatus;
//          break;
        case SinetClass.Associate:
          tp = associate;
          break;
        case SinetClass.Terminal:
          tp = terminal;
          break;
// +++ @SS2
      }
      SINETTABLES[i] = tp;
    }
  }

  private static final Counter PayMateDBclassMonitorCounter = new Counter();

  private static final PayMateDBQueryString QS = new PayMateDBQueryString();
  // this is for the Database interface!
  public static final TableProfile[] tables = QS.getTableArray();

  // set server-based defaults
  public static final void setServerDefaultColors() {
    associate.colorschemeid.columnDef = Associate.DEFAULTCOLORS; // shouldn't be singleQuoteEscaped
  }

  public PayMateDB(DBConnInfo connInfo) {
    super(connInfo, Thread.currentThread().getName());
    PayMateDBclassMonitor = new Monitor(PayMateDB.class.getName() + "." + PayMateDBclassMonitorCounter.incr());
  }

  /**
   * @return successful db manipulation
   * @param original
   * @param record
   * @return
   */
  public boolean updateTxn(TxnRow original) {
    EasyProperties ezp = original.toProperties();
    return setRecordProperties(txn, original.txnid(), ezp) != FAILED;
  }

  public String Safeid(UniqueId id) {
    return UniqueId.isValid(id) ? id.toString() : null;
  }

  public Enterpriseid[] getAllEnterprisesEnabledNameOrder() {
    return(Enterpriseid[]) getUniqueIdExtentArrayFromQuery(QS.genEnterpriseIdsbyEnabledName(), Enterpriseid.class);
  }

  public Storeid[] getAllStoresTxnCountOrder() {
    return(Storeid[]) getUniqueIdExtentArrayFromQuery(QS.genStoreidsByAscTxncount(), Storeid.class);
  }

  public TerminalInfo getTerminalInfo(Terminalid terminalId) {
    TerminalInfo newone = null;
    if(Terminalid.isValid(terminalId)) {
      EasyProperties ezp = getRecordProperties(terminal, terminalId);
      newone = new TerminalInfo(terminalId);
      newone.setNickName(ezp.getString(terminal.terminalname.name()));
      newone.setHack(ezp.getString(terminal.modelcode.name()));
      newone.setAllowSigCap(ezp.getBoolean(terminal.dosigcap.name()));
      newone.setAskforAvs(ezp.getBoolean(terminal.enavs.name()));
      newone.setTwoCopies(ezp.getBoolean(terminal.twocopies.name()));
      newone.setCanStandinModify(ezp.getBoolean(terminal.ensimodify.name()));
      EasyProperties more = EasyProperties.FromString(ezp.getString(terminal.eqhack.name()));
      newone.moreProperties = more;
      // storeinfo stuff
      newone.si = getStoreInfo(StoreHome.Get(getStoreForTerminal(terminalId)));
      dbg.VERBOSE("getTerminalInfo:" + newone.toSpam());
    }
    return newone;
  }

  public String getTerminalName(Terminalid terminalId) {
    return getRecordProperties(terminal, terminalId).getString(terminal.terminalname.name());
  }

  public TermauthRow getTermauths(Terminalid termid) {
    Statement q = query(QS.genTermAuths(termid));
    if(q == null) {
      dbg.ERROR("TermauthRow: q = null!");
    }
    return TermauthRow.NewSet(q);
  }

  public final StoreAccessid getStoreaccess(Associateid associd, Storeid storeid) {
    return new StoreAccessid(getIntFromQuery(QS.genStoreAccessid(associd, storeid)));
  }

  // +++ eventually require storeid
  public final StoreaccessRow getStoreaccesses(Associateid associd) {
    Statement q = query(QS.genStoreAccessesByAssoc(associd));
    if(q == null) {
      dbg.ERROR("getStoreaccesses: q = null!");
    }
    return StoreaccessRow.NewSet(q);
  }

  public final StoreaccessRow getStoreaccesses(UniqueId storeid) {
    Statement q = query(QS.genStoreAccessesByStore(storeid));
    if(q == null) {
      dbg.ERROR("getStoreaccesses: q = null!");
    }
    return StoreaccessRow.NewSet(q);
  }

  private static final PayType PTEMPTY = new PayType();

  private final boolean findStoreAuthInfo(Storeid storeid, PayType paytype, String institution, RealMoney maxtxnlimitRM, TxnRow record) {
    boolean gateway = (record != null) && record.hasAuthRequest();
    long maxtxnlimit = 0;
    // this ignores the institution if it set to null!
    StoreAuthid id = new StoreAuthid(getIntFromQuery(QS.genStoreAuthInfo(storeid, String.valueOf(paytype.Char()), institution)));
    if(id.isValid()) {
      EasyProperties ezp = getRecordProperties(storeauth, id);
      // we sometimes pass null in for record when we are JUST getting the merchant
      // info and don't care about the authid or have a TxnRow to put it in
      if(record != null) {
        record.authid = ezp.getString(storeauth.authid.name());
        record.settleid = ezp.getString(storeauth.settleid.name());
      }
      maxtxnlimit = ezp.getLong(storeauth.maxtxnlimit.name());
      maxtxnlimitRM.setto(gateway ? Integer.MAX_VALUE : maxtxnlimit);
      return true;
    } else {
      return false;
    }
  }

  public final TermAuthid[] getTermauthids(Terminalid termid) {
    return(TermAuthid[]) getUniqueIdExtentArrayFromQuery(QS.genTermAuthIds(termid), TermAuthid.class);
  }

  /**
   * stuffs the record with the authid, settleid, and gets the maxtxnlimit for this paytype + institution + store
   * @return max txn limit for that card
   */
  public final RealMoney getAuthInfo(Storeid storeid, TxnRow record, MerchantInfo merch) {
    // need to stuff record with auth info and get a merchantinfo
    // find the authid, settleid, and maxtxnlimit via the storeid, paytype, and institution
    RealMoney maxtxnlimit = new RealMoney();
    // if this is not a DB card, we can first try to find it by institution ...
    boolean found = false;
    if(!record.isDebit()) { // try to look it up with institution, except debit
      found = findStoreAuthInfo(storeid, record.paytype(), record.institution, maxtxnlimit, record);
    }
    if(!found) { // if you don't find the right one, see if the store has a default one for that paytype...
      dbg.ERROR("No authorizer found for PT=" + record.paytype + ", IN= " + record.institution + ", storeid=" + storeid + ".");
      found = findStoreAuthInfo(storeid, record.paytype(), CardIssuer.Unknown.Abbreviation(), maxtxnlimit, record);
      if(!found) { // if still not found, try forgetting the institution altogether
        dbg.ERROR("No default authorizer for PT=" + record.paytype + ", institution=" + CardIssuer.Unknown.Abbreviation() + ", storeid=" + storeid);
        found = findStoreAuthInfo(storeid, record.paytype(), null, maxtxnlimit, record);
        if(!found) { // if still not found, try forgetting the paytype altogether
          dbg.ERROR("No default authorizer for PT=" + record.paytype + ", storeid=" + storeid);
          found = findStoreAuthInfo(storeid, new PayType(PayType.Unknown), null, maxtxnlimit, record);
          if(!found) { // if still not found, give up
            service.PANIC("No authorizer found for any combination of PT=" + record.paytype + ", storeid=" + storeid);
          }
        }
      }
    }

    // get the merchantinfo
    getMerchantInfo(record.terminalid(), record.authid(), /*settlement=*/ false, merch);
    return maxtxnlimit;

  }

  // used for standins & reversals
  public final MerchantInfo getAuthMerchantInfo(Terminalid terminalid, Authid authid) {
    return getMerchantInfo(terminalid, authid, /*settlement=*/ false, null);
  }

  public final MerchantInfo getSubmitMerchantInfo(Terminalid terminalid, Authid authid) {
    return getMerchantInfo(terminalid, authid, /*settlement=*/ true, null);
  }

  private final MerchantInfo getMerchantInfo(Terminalid terminalid, Authid authid, boolean settlement, MerchantInfo minfo) {
    Statement stmt = null;
    try {
      if(minfo == null) {
        minfo = new MerchantInfo(); //averts NPE's, but this merch data will evaporate
      }
      if(!Terminalid.isValid(terminalid)) {
        dbg.ERROR("getMerchantInfo():Terminalid is invalid!");
      } else if(!Authid.isValid(authid)) {
        dbg.ERROR("getMerchantInfo():Authid is invalid!");
      } else {
        Store store = StoreHome.Get(getStoreForTerminal(terminalid));
        stmt = query(QS.genProcInfoFor(terminalid, authid, store.storeId(), settlement));
        if(stmt != null) {
          ResultSet rs = getResultSet(stmt);
          if(next(rs)) {
            minfo.set(getStringFromRS(settlement ? storeauth.settlemerchid : storeauth.authmerchid, rs), getStringFromRS(termauth.authtermid, rs), store.getTimeZone(), store.merchanttype);
          }
        }
      }
    } catch(Exception ex) {
      dbg.Caught(ex);
    } finally {
      closeStmt(stmt);
      return minfo;
    }
  }

  public TextList getMerchantIds(Authid authid, Storeid storeid) {
    StoreAuthid[] storeauthids = (StoreAuthid[]) getUniqueIdExtentArrayFromQuery(QS.genMerchantIds(authid, storeid), StoreAuthid.class);
    TextList merchantids = new TextList();
    for(int i = storeauthids.length; i-- > 0; ) {
      EasyProperties ezp = getRecordProperties(storeauth, storeauthids[i]);
      merchantids.assurePresent(ezp.getString(storeauth.authmerchid.name()));
    }
    return merchantids;
  }

/////////////////

  /**
   * @param termid the terminal to ttl standins for
   * @returns the ttl cents for all txns in standin for this terminal except for the one txn
   */
  public LedgerValue getTtlStandinsForTerminal(Terminalid termid) {
    return LedgerValue.New(getLongFromQuery(QS.genTtlStandinsForTerminal(termid)));
  }

  public Associateid[] getAssociatesByLoginnameEntPW(String loginname, Enterpriseid entid, String password) {
    return(Associateid[]) getUniqueIdExtentArrayFromQuery(QS.genAssociateId(loginname, password, entid), Associateid.class);
  }

  public static final int txnCountLimit() {
    return service.txnCountLimit();
  }

  public Storeid getStoreForTerminal(Terminalid terminalid) {
    return new Storeid(getIntFromQuery(QS.genStoreForTerminal(terminalid)));
  }

  private UniqueId[] getUniqueIdExtentArrayFromQuery(QueryString qs, Class uniqueidExtent) {
    UniqueId[] array = null;
    try {
      Statement stmt = query(qs);
      if(stmt != null) {
        try {
          // prep the fields and stuff
          // +++ create a synchronized function that will lookup one of these from a list
          // +++ that we create as we find that we don't have one of them.
          // +++ this will work since they won't change during runtime.
          Constructor constructor = ReflectX.constructorFor(uniqueidExtent, int.class);
          Object[] param = new Object[1];
          // find out how many there are and build the array
          int count = 0;
          ResultSet rs = getResultSet(stmt);
          org.postgresql.jdbc3.Jdbc3ResultSet rs3 = null;
          if(rs != null) {
            rs3 = (org.postgresql.jdbc3.Jdbc3ResultSet) rs;
            count = rs3.getTupleCount();
          }
          array = (UniqueId[]) java.lang.reflect.Array.newInstance(uniqueidExtent, count);
          // fill the array
          for(int i = array.length; i-- > 0; ) {
            // get the integer
            boolean went = false;
            try {
              went = rs3.absolute(i + 1);
            } catch(Exception ex) {
              dbg.Caught(ex);
            }
            int theid = net.paymate.lang.MathX.INVALIDINTEGER;
            if(went) {
              theid = getIntFromRS(ONLYCOLUMN, rs);
            } else {
              dbg.ERROR("Could not load a tuple that the recordset said was there [" + (i + 1) + "/" + array.length + "]!");
            }
            // create the object
            param[0] = new Integer(theid); // since 'int' isn't an object, have to use 'Integer'
            array[i] = (UniqueId) constructor.newInstance(param);
          }
        } catch(Exception ex) {
          dbg.Caught(ex);
        } finally {
          closeStmt(stmt);
        }
      } else {
        dbg.WARNING("getUniqueIdExtentArrayFromQuery() stmt=null!");
      }
    } catch(Exception ex) {
      service.PANIC("UniqueIdExtentArrayFromIntArray exception! ", ex);
      dbg.Caught(ex);
    } finally {
      return array;
    }
  }

  public Associateid[] getAssociatesByLoginname(String loginname) {
    return(Associateid[]) getUniqueIdExtentArrayFromQuery(QS.genAssociateidForAssociateLoginname(loginname), Associateid.class);
  }

  /**
   * first used for storing receipts associated with a stoodin request.
   * Try to find via clietnreftime, if there is one, otherewise, use the stan if there is one.
   * @return txnid from standin generated reference info
   */
  public Txnid findStandin(Terminalid terminalid, UTC clientreftime) {
    return new Txnid(getIntFromQuery(QS.genFindTransactionBy(terminalid, clientreftime)));
  }

  /**
   * @return matching txnid, also modify tref to hold it.
   * @todo rework to also fetch stan from tables.
   * @todo (lower than getting stan) rework to honor stan and use it instead of crefTime
   */
  public Txnid findStandin(TxnReference tref) {
    tref.txnId = findStandin(tref.termid, tref.crefTime);
    // this is the i'm lazy way; check and be more deterministic later ...
    if(!Txnid.isValid(tref.txnId)) {
      tref.txnId = getTxnid(tref.termid, tref.STAN());
    }
    return tref.txnId;
  }

  /**
   * @return id of transaction row that gets created
   */
  public Txnid startTxn(TxnRow record) {
    Txnid result = getNextTxnid();
    EasyProperties ezp = null;
    if(result.isValid()) {
      record.txnid = String.valueOf(result);
      if(!StringX.NonTrivial(record.settleid) && StringX.NonTrivial(record.authid)) {
        record.settleid = record.authid;
      }
      // to be sure the service code is something legal
      record.servicecode = String.valueOf(StringX.parseInt(record.servicecode));
      ezp = record.toProperties();
      dbg.ERROR("record=" + ezp);
      int i = update(QS.genCreateTxn(ezp, result)); //modifies 'result' if successfully executed
//      // +++ ??? do something similar with other ints?
//      if ( (result.value() % ReceiptAgent.MAXDIRFILECOUNT) == 0) {
//        String msg = "Txnid just rolled over to " + result +
//            ". Time to archive receipts!";
//        dbg.ERROR(msg);
//        service.PANIC(msg);
//      }
      // here, if it exists, put the terminal's authrequest into the database out of the txnrow
      if(record.hasAuthRequest() && !AuthattemptId.isValid(record.authattempt.id)) {
        // just in case these things aren't set ...
        record.authattempt.txnid = record.txnid();
        record.authattempt.authid = record.authid();
        record.authattempt.terminalid = record.terminalid();
        startAuthAttempt(record.authattempt);
      }
    } else {
      service.PANIC("DB,TXN,INSERT,FAILED", ezp);
    }
    return result;
  }

  public void startAuthAttempt(AuthAttempt attempt) { // Txnid txnid, Authid authid, EasyUrlString authrequest) {
    dbg.VERBOSE("inserting authattempt into table ...");
    attempt.id = getNextAuthattemptId();
    String raw = attempt.authrequest.rawValue();
    String attemptreq = DataCrypt.databaseEncode( (raw != null) ? raw.getBytes() : new byte[0], attempt.id);
    int i = update(QS.genCreateAuthAttempt(attempt.txnid, attempt.authid, attemptreq, attempt.terminalid, attempt.id)); //modifies 'id' if successfully executed
    if(i != 1) {
      service.PANIC("Unable to insert authattempt record: txnid=" + attempt.txnid + ", authid = " + attempt.authid + ", request=" + attempt.authrequest.encodedValue());
    }
    dbg.VERBOSE("inserted authattempt " + attempt.id + " into table.");
  }

  public AuthAttemptRow getAuthAttempts(Terminalid terminalid, TimeRange times) {
    return AuthAttemptRow.NewSet(query(QS.genAuthAttempts(terminalid, times)));
  }

  public AuthorizerRow getAuths() {
    Statement q = query(QS.genAuths());
    if(q == null) {
      dbg.ERROR("getAuthIds: q = null!");
    }
    return AuthorizerRow.NewSet(q);
  }

  public TextList getServiceParamsNames(String serviceName) {
    TextList tl = new TextList(10, 10);
    Statement stmt = query(QS.genServiceParamNames(serviceName));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        while(next(rs)) {
          tl.add(getStringFromRS(servicecfg.paramname, rs));
        }
      } catch(Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
    return tl;
  }

  public String getServiceParam(String serviceName, String paramname, String defaultValue) {
    String paramvalue = "";
    try {
      Statement stmt = query(QS.genServiceParam(serviceName, paramname));
      boolean found = false;
      if(stmt != null) {
        try {
          ResultSet rs = getResultSet(stmt);
          if(next(rs)) {
            found = true;
            paramvalue = getStringFromRS(servicecfg.paramvalue, rs);
          }
        } catch(Exception ex) {
          dbg.Caught(ex);
        } finally {
          closeStmt(stmt);
        }
      }
      if(!StringX.NonTrivial(paramvalue)) {
        paramvalue = StringX.TrivialDefault(defaultValue);
        setServiceParam(serviceName, paramname, paramvalue, found);
      }
    } catch(Exception ex) {
      dbg.Caught(ex);
    } finally {
      return paramvalue;
    }
  }

  // this is a long way to do it, but is fine for now.  How often do these change?  Leave it slow.
  public boolean setServiceParam(String serviceName, String paramname, String paramvalue) {
    getServiceParam(serviceName, paramname, paramvalue); // does not change the value if it already exists, but inserts it if it doesn't
    return setServiceParam(serviceName, paramname, paramvalue, true); // changes the value if it already existed before this function was called
  }

  private boolean setServiceParam(String serviceName, String paramname, String paramvalue, boolean found) {
    boolean ret = false;
    int count = 0;
    Servicecfgid id = null;
    if(found) {
      count = update(QS.genUpdateServiceParam(serviceName, paramname, paramvalue));
      ret = (count > 0);
    } else {
      id = getNextServicecfgid();
      count = update(QS.genCreateServiceParam(serviceName, paramname, paramvalue, id));
      ret = (count == 1);
    }
    String cfgChgNotice = "set " + serviceName + "." + paramname + "=" + paramvalue + ", " + (found ? "" : "NOT ") + "found, ret=" + ret;
    if(validated()) {
      service.notifyCfgChanged(cfgChgNotice);
    } else {
      dbg.ERROR(cfgChgNotice);
    }
    return ret;
  }

  public double getDoubleServiceParam(String serviceName, String paramname, double defaultValue) {
    return StringX.parseDouble(getServiceParam(serviceName, paramname, String.valueOf(defaultValue)));
  }

  public long getLongServiceParam(String serviceName, String paramname, long defaultValue) {
    return StringX.parseLong(getServiceParam(serviceName, paramname, String.valueOf(defaultValue)));
  }

  public int getIntServiceParam(String serviceName, String paramname, int defaultValue) {
    return StringX.parseInt(getServiceParam(serviceName, paramname, String.valueOf(defaultValue)));
  }

  public boolean getBooleanServiceParam(String serviceName, String paramname, boolean defaultValue) {
    return Bool.For(getServiceParam(serviceName, paramname, Bool.toString(defaultValue)));
  }

  public EasyCursor getServiceParams(String serviceName, EasyCursor ezc) {
    TextList names = null;
    if(ezc == null) { // if they don't send one, give them all back ...
      ezc = new EasyCursor();
      names = getServiceParamsNames(serviceName);
    } else {
      names = ezc.allKeys();
    }
    for(int i = names.size(); i-- > 0; ) {
      String name = names.itemAt(i);
      String defaultValue = ezc.getString(name);
      ezc.setString(name, getServiceParam(serviceName, name, defaultValue));
    }
    return ezc;
  }

// unconditionally sets them based on the ezc passed in
  public EasyCursor setServiceParams(String serviceName, EasyCursor ezc) {
    TextList names = ezc.allKeys();
    for(int i = names.size(); i-- > 0; ) {
      String name = names.itemAt(i);
      String value = ezc.getString(name);
      setServiceParam(serviceName, name, value);
    }
    return getAllServiceParams(serviceName);
  }

  public EasyCursor getAllServiceParams(String serviceName) {
    return getServiceParams(serviceName, null);
  }

  public AuthorizerRow getAuthidsForTerminal(Terminalid terminalid) {
    Statement q = query(QS.genAuthIdsForTerminal(terminalid));
    if(q == null) {
      dbg.ERROR("getAuthIds: q = null!");
    }
    return AuthorizerRow.NewSet(q);
  }

  public Authid[] getAuthidsForStore(Storeid storeid) {
    return(Authid[]) getUniqueIdExtentArrayFromQuery(QS.genAuthIdsForStore(storeid), Authid.class);
  }

  public Authid getDefaultAuthidForTerminal(Terminalid terminalid) {
    return new Authid(getIntFromQuery(QS.genDefaultAuthidForTerminal(terminalid)));
  }

  public static final String Now() {
    return QS.Now();
  }

  public boolean stampAuthDone(TxnRow record) {
    // stamp the new txn as completed successfully
    record.authendtime = Now();
    boolean ok = updateTxn(record);
    if(record.hasAuthResponse()) {
      record.authattempt.txnid = record.txnid();
      stampAuthAttemptDone(record.authattempt);
    }
    return ok;
  }

  // +++ needs relocation to the authterminal object when that is done (did you mean authattempt?)
  public final void stampAuthAttemptDone(AuthAttempt attempt) {
    // here, add code to put the TxnRow's authresponse into the authattempt database, if needed
    int i = update(QS.genStampAuthAttemptDone(attempt.id, attempt.txnid, attempt.authresponse));
    if(i != 1) {
      // but ONLY if there is also an authrequest! (that is how we tell if we need to)
      dbg.WARNING("Update of authattempt for txnid=" + attempt.txnid + " resulted in " + i + " updates for response: " + attempt.authresponse.encodedValue());
    }
  }

  public void stampAuthAttemptTxnidOnly(AuthattemptId aaid, Txnid txnid) {
    update(QS.genStampAuthAttemptTxnidOnly(aaid, txnid)); // ignore return value?
  }

  public boolean setVoidFlag(Txnid txnid, boolean voidflag) {
    int ret = update(QS.genStampVoidTxn(txnid, Bool.toString(voidflag)));
    dbg.ERROR("Stamping txn voided: original.txnid=" + txnid + ", original.voided=" + voidflag + ", returned[should be 1]=" + ret);
    return ret == 1;
  }

  public Txnid[] getStoodins(Authid authid) {
    // load the list of txns that need processing from tables, sticking each into the list
    return(Txnid[]) getUniqueIdExtentArrayFromQuery(QS.genToAuth(authid), Txnid.class);
  }

  /**
   * get institution and card payment type from card itself.
   *     // eg: DC, DS, AE, VS, MC, C1
   * @param storeid is forward looking to qualify gift cards, not yet used.
   * @param record is both in and out of this funciton.
   * @todo rest of storeauth info can be read and cached NOW! rather than requeried for later.
   */

  public String getPaymentTypeFromCardNo(TxnRow record) {
    String paytype = String.valueOf(PTEMPTY.CharFor(PayType.Unknown));
    if(record != null) {
      BinEntry bin = getBinEntry(record.card().bin());
      if(bin != null) {
        record.institution = bin.issuer.Abbreviation(); // getStringFromRS(card.institution, rs);
        paytype = String.valueOf(bin.act.Char()); // getStringFromRS(card.paytype, rs);
      }
      // set to unknown if we don't know
      if(!StringX.NonTrivial(record.institution)) {
        record.institution = CardIssuer.Unknown.Abbreviation();
      }
      if(!StringX.NonTrivial(paytype)) {
        paytype = String.valueOf(PTEMPTY.CharFor(PayType.Unknown));
      }
      dbg.ERROR("PTin:" + paytype + record.institution);
    }
    return paytype;
  }

  public boolean stampAuthStart(TxnRow record) {
    record.authstarttime = Now();
    return updateTxn(record);
  }

  // SEQUENCE NUMBER(s)
  public final long getSequence(Authid authid, Terminalid terminalid) {
    return getLongFromQuery(QS.genGetSequence(authid, terminalid));
  }

  public final void setSequence(Authid authid, Terminalid terminalid, int number, Txnid txnid) {
    update(QS.genSetSequence(authid, terminalid, number));
    update(QS.genSetTxnSequence(txnid, number));
  }

  public Storeid[] storesInfoQuery(Enterpriseid enterpriseID) {
    return(Storeid[]) getUniqueIdExtentArrayFromQuery(QS.genStoresQuery(enterpriseID), Storeid.class);
  }

  public StoreauthRow getStoreAuths(Storeid storeid) {
    Statement q = query(QS.genStoreAuths(storeid));
    if(q == null) {
      dbg.ERROR("storeAuthsQuery: q = null!");
    }
    return StoreauthRow.NewSet(q);
  }

  public Associateid[] getAssociatesByLoginnameEnt(String loginname, Enterpriseid enterid) {
    return(Associateid[]) getUniqueIdExtentArrayFromQuery(QS.genAssociateIdByLogin(loginname, enterid), Associateid.class);
  }

  public DepositRow getPendingTermAuths(Storeid store) {
    return DepositRow.NewSet(query(QS.genPendingTermAuths(store)));
  }

  public TxnRow unsettledTxnsQuery(Terminalid terminalid) {
    Statement q = query(QS.genDrawerQuery(new Drawerid(), terminalid, false));
    if(q == null) {
      dbg.ERROR("unsettledTxnsQuery: q = null!");
    }
    return TxnRow.NewSet(q);
  }

  public TxnRow unsettledTxnsQuery(TermAuthid termauthid) {
    Statement q = query(QS.genBatchQuery(termauthid));
    if(q == null) {
      dbg.ERROR("unsettledTxnsQuery: q = null!");
    }
    return TxnRow.NewSet(q);
  }

  public Statement runStoreDrawerQuery(Storeid storeid, TimeRange tr) {
    return query(QS.genStoreDrawersQuery(storeid, tr));
  }

  public UTC mostRecentStoreDrawer(Storeid storeid) {
    String datestr = getStringFromQuery(QS.genMostRecentStoreDrawer(storeid));
    return QS.tranUTC(datestr);
  }

  public UTC mostRecentStoreBatch(Storeid storeid) {
    String datestr = getStringFromQuery(QS.genMostRecentStoreBatch(storeid));
    return QS.tranUTC(datestr);
  }

  public TxnRow getDrawerClosingCursor(Drawerid drawerid, LocalTimeFormat ltf) {
    // first, get the info from the drawer closing indicated.
    Statement stmt = null;
    Terminalid terminalid = new Terminalid();
    String toDate = null;
    String title = null;
    try {
      stmt = query(QS.genBookmarkQuery(drawerid));
      ResultSet tqrs = getResultSet(stmt);
      if(next(tqrs)) {
        terminalid = new Terminalid(getIntFromRS(drawer.terminalid, tqrs));
        toDate = getStringFromRS(drawer.transtarttime, tqrs);
        Associateid associd = new Associateid(getIntFromRS(drawer.associateid.name(), tqrs));
        Associate assoc = AssociateHome.Get(associd);
        String name = (assoc == null) ? "" : (assoc.firstname + " " + assoc.lastname);
        title = "Drawer Closing by " + name + " of " + getStringFromRS(terminal.terminalname, tqrs) + " at " + ltf.format(QS.tranUTC(toDate));
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
    }
    TerminalInfo tinfo = getTerminalInfo(terminalid);
    TxnRow trow = getDrawerQuery(drawerid, terminalid, false);
    if(trow != null) {
      trow.setTitle(title);
    }
    return trow;
  }

  // +++ @@@ %%% Next two functions need mutexing on terminal, so that drawers get recalc'd properly & so that tnx's drawer info is not changed concurrently.

  /**
   * set drawerid on a txn (so long as it doesn't already have one), and recalc the drawer
   */
  public boolean setDrawerid(Txnid txnid, Drawerid newdrawerid) {
    TxnRow rec = getTxnRecordfromTID(txnid);
    Drawerid olddrawerid = rec.drawerid();
    // check to see if the txn and drawer have the same terminalid associated with them !!!
    Terminalid termid = getTerminalForDrawer(newdrawerid);
    if(termid.value() != rec.terminalid().value()) {
      dbg.ERROR("Can't assign txnid " + txnid + " to drawer " + newdrawerid + " since they do not have the same terminalid!");
    } else {
      // change the drawerid
      if(!olddrawerid.isValid()) { // then it is not set yet, so you can set it.
        return setOrReleaseDrawerid(txnid, newdrawerid, olddrawerid);
      } else {
        dbg.ERROR("Can't assign txnid " + txnid + " to drawer " + newdrawerid + " since it is already assigned to " + olddrawerid + ".");
      }
    }
    return false;
  }

  public boolean releaseDrawerid(Txnid txnid) {
    // change the drawerid
    TxnRow rec = getTxnRecordfromTID(txnid);
    Drawerid olddrawerid = rec.drawerid();
    if(olddrawerid.isValid()) { // then it is set, so you can release it.
      return setOrReleaseDrawerid(txnid, new Drawerid(), olddrawerid);
    } else {
      dbg.ERROR("Can't release txnid " + txnid + " from drawer since it isn't assigned to one.");
    }
    return false;
  }

  /**
   * leave private !!!
   */
  private final boolean setOrReleaseDrawerid(Txnid txnid, Drawerid newdrawerid, Drawerid olddrawerid) {
    int count = update(QS.genSetDrawerid(txnid, newdrawerid));
    boolean ret = (count == 1);
    if(!ret) {
      dbg.ERROR("Update of txn " + txnid + " from drawer " + olddrawerid + " to drawer " + newdrawerid + " resulted in an update of " + count + " records!");
    }
    // recalc the drawer(s)
    if(Drawerid.isValid(newdrawerid)) {
      retotalClosedDrawer(newdrawerid); // recalc whether or not it worked ... just in case
    }
    if(Drawerid.isValid(olddrawerid)) {
      retotalClosedDrawer(olddrawerid); // recalc whether or not it worked ... just in case
    }
    return ret;
  }

  // update txns with NULL for the batchid where the batchid equals this one
  // note that this MUST do this record-by-record since the database gets deadlocks with big UPDATE ... WHERE commands.
  public final void clearBatchDetails(Batchid redoBatchid) { // be sure that batchid is good before calling this function!
    // select the entire list of txnids to do this with into an int array (or something)
    // give the array to a function who is responsible for setting them all to some value (null in this case)
    // that function then calls another function to do each one
    Txnid[] txns = (Txnid[]) getUniqueIdExtentArrayFromQuery(QS.genTxnidsForBatchid(redoBatchid), Txnid.class);
    int changed = setTxnBatchids(txns, null);
    // return changed; (if returns int)
  }

  public final int setTxnBatchids(Txnid[] idList, Batchid newBatchid) {
    int changed = 0;
    if(idList == null) {
      //+++ bitch
    } else {
      Txnid txnid = new Txnid();
      if(newBatchid == null) {
        newBatchid = new Batchid();
      }
      int total = idList.length;
      // order does not matter since we aren't using a transaction
      for(int i = total; i-- > 0; ) {
        txnid = idList[i];
        int updatecount = update(QS.genSetTxnBatchid(txnid, newBatchid));
        if(updatecount > ObjectX.INVALIDINDEX) {
          changed += updatecount;
        }
      }
      if(changed < total) {
        service.PANIC("BATCH." + newBatchid + ".SET ERROR: " + changed + "<" + total);
      }
    }
    return changed;
  }

  public void setBatchTotals(Batchid batchid, int count, int amount) {
    update(QS.genSetBatchTotals(batchid, count, amount)); // update the batch record for this batchid
  }

  public TxnRow getBatchCursor(Batchid batchid, LocalTimeFormat ltf) {
    // first, get the info from the batch indicated.
    Statement stmt = null;
    Terminalid terminalid = new Terminalid();
    String toDate = null;
    String title = null;
    try {
      stmt = query(QS.genBatchQuery(batchid));
      ResultSet tqrs = getResultSet(stmt);
      if(next(tqrs)) {
        toDate = getStringFromRS(batch.batchtime, tqrs);
        title = "Batch Submittal " + batchid + " at " + ltf.format(QS.tranUTC(toDate));
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
    }
    TxnRow trow = getFullBatchQuery(batchid);
    if(trow != null) {
      trow.setTitle(title);
    }
    return trow;
  }

  public Statement runStoreBatchQuery(Storeid storeid, boolean showEmptyFailures, TimeRange tr) {
    return query(QS.genStoreBatchesQuery(storeid, showEmptyFailures, tr));
  }

  public final TxnRow getFullBatchQuery(Batchid batchid) {
    return TxnRow.NewSet(query(QS.genFullBatchQuery(batchid)));
  }

///////////////////////
//// SUBMITTALS
  /**
   * creates a batch, stuffing the termauthid in it,
   * and setting the batchseq & termbatchnum in the termauth table,
   * returning the batchid,
   * along with stamping the txns
   * & adding a txnrow to the AuthSubmitTxn so that it can step through its txns
   *
   * This code is already mutexed, so we don't have to mutex the getting and setting of things
   * @return number of records (if zero, then the submittal will NOT happen, and the batch record will not be created)
   */
  public int newBatch(AuthSubmitTransaction submittal, LongRange batchSeqRange, Counter termbatchnumer, boolean auto) {
    int foundRecords = 0; // 0 means that the thing WON'T be done!
    int txncount = 0;
    int txntotals = 0;
    // find the proper termauthid from the termauth table, store timezone (use the date/time in the timezone of the store, per Beverly)
    TermAuthid termauthid = new TermAuthid(getIntFromQuery(QS.genTermAuth(submittal.request.terminalid, submittal.request.authid)));
    String batchtime = QS.forTrantime(submittal.request.batchtime);
    submittal.request.termbatchnum = (int) termbatchnumer.value();
    submittal.request.termauthid = termauthid;
    // insert the batch record
    Batchid batchid = getNextBatchid();
    if(update(QS.genCreateBatch(termauthid, batchtime, submittal.request.termbatchnum, batchid
                                /* incremented elsewhere */, auto)) != 1) {
      dbg.ERROR("Unable to create new batch record: termauthid=" + termauthid + ", batchtime=" + batchtime + ", termbatchnumber=" + termbatchnumer.value());
    } else {
      submittal.request.setBatchid(batchid, batchSeqRange); // sets the batchSequence here
      // stamp the txns with the batchid;
      foundRecords = setTxnBatchids(batchid, submittal.request.terminalid, submittal.request.authid);
      if(foundRecords == 0) {
        // delete the batch record from the db!
        if(update(QS.genDeleteBatch(batchid)) != 1) {
          dbg.ERROR("Error attempting to delete batchid # " + batchid);
        }
      } else {
        // set the now-known batchSequence
        if(update(QS.genUpdateBatchseq(batchid, submittal.request.batchseq)) != 1) {
          dbg.ERROR("Error attempting to update the batches [" + batchid + "] sequence #.");
        }
        // Get a TxnRow of all of the txns & attach to the submittal object
        submittal.records = getBatch(batchid);
      }
    }
    // return the number of records found
    return foundRecords;
  }

  // update txns with the batchid where the txn matches the WHERE
  // note that this MUST do this record-by-record since the database gets deadlocks with big UPDATE ... WHERE commands.
  private final int setTxnBatchids(Batchid batchid, Terminalid terminalid, Authid authid) {
    // select the entire list of txnids to do this with into an int array (or something)
    // give the array to a function who is responsible for setting them all to some value (null in this case)
    // that function then calls another function to do each one
    Txnid[] txns = (Txnid[]) getUniqueIdExtentArrayFromQuery(QS.genGetBatchableTxns(terminalid, authid), Txnid.class);
    return setTxnBatchids(txns, batchid);
  }

  /**
   * This is currently only used for sending batches to authorizers.
   * Therefore it will use an ascending list to pacify NPC.
   * If you need to send descending to anyone, recode with that as a parameter
   */
  public TxnRow getBatch(Batchid batchid) {
    return TxnRow.NewSet(query(QS.genGetBatch(batchid)));
  }

  // checks to see if the actinocode is D or F and if the count > 0
  public final boolean canResubmitBatch(Batchid batchid) {
    boolean ret = false;
    Statement stmt = query(QS.genBatchQuery(batchid));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          String actioncode = getStringFromRS(batch.actioncode, rs);
          int count = getIntFromRS(batch.txncount, rs);
          if( (count > 0) && (StringX.equalStrings(actioncode, ActionCode.Declined) || StringX.equalStrings(actioncode, ActionCode.Failed))) {
            ret = true;
          }
        }
      } catch(Exception ex) {
        dbg.Caught(ex);
      } finally {
        closeStmt(stmt);
      }
    }
    return ret;
  }

  public void finishBatch(AuthSubmitTransaction submittal, LongRange authseqRange, Counter termbatchnumer) { // +++ @@@ mutex ??? - should probably mutex in AuthSubmitTransaction where you set the values in the object, too, or only do that through this function (pass the values as parameters to the function)
    if(submittal.response.statusUnknown()) {
      // if actioncode is trivial, set it and authrespmsg to error states in the submittal record
      submittal.response.setTrio(ActionCode.Failed, /* don't change this one */ submittal.response.authcode(), StringX.TrivialDefault(submittal.response.message(), "PM->HOST COMM ERR"));
    }
    dbg.ERROR("updating batch: " + submittal);
    if(Batchid.isValid(submittal.request.batchid())) {
      // save the actioncode and authrespmsg to the database
      if(update(QS.genUpdateBatchStatus(submittal.request.batchid(), String.valueOf(submittal.response.action()), submittal.response.message())) != 1) {
        // if there was an error saving it
        dbg.ERROR("finishBatch(): Error closing out batch & writing batch status to DB: batchid=" + submittal.request.batchid() + ", actioncode=" + submittal.response.action() + ", rrn=" + submittal.response.authrrn());
      }
      if(submittal.response.isApproved()) {
        // if the submission succeeded; reset the sequence number and increment the batch number for the termauth
        int newauthseq = (int) authseqRange.low() - 1; // set to LOW-1 so that the next increment will start with LOW
        if(update(QS.genUpdateTermauthInfo(submittal.request.termauthid, (int) termbatchnumer.incr(), newauthseq)) != 1) { // batch number is incremented here ONLY
          // if there was an error saving it
          dbg.ERROR("finishBatch(): Error updating termauth values [writing to DB] -- termauth=" + submittal.request.termauthid);
        } else {
          dbg.ERROR("finishBatch(): updated the batch num.");
        }
      } else {
        dbg.ERROR("finishBatch(): Not incrementing the batchnum since the batch was not approved!");
      }
    } else {
      dbg.ERROR("finishBatch(): Batch was not submitted, so no record in DB!");
    }
  }

  public int getBatchNumberValue(Authid authid, Terminalid terminalid) {
    return getIntFromQuery(QS.genGetBatchNumberValue(authid, terminalid));
  }

//// END SUBMITTALS
///////////////////////

  public final Terminalid getTerminalForBatch(Batchid batchid) {
    Statement stmt = null;
    Terminalid ret = null;
    try {
      stmt = query(QS.genTerminalAndAuthForBatch(batchid));
      if(stmt != null) {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          ret = new Terminalid(getIntFromRS(terminal.terminalid, rs));
        }
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
      return ret;
    }
  }

  public final Terminalid getTerminalForDrawer(Drawerid drawerid) {
    return new Terminalid(getIntFromQuery(QS.genTerminalidFromDrawerid(drawerid)));
  }

  public final Authid getAuthForBatch(Batchid batchid) {
    Statement stmt = null;
    Authid ret = null;
    try {
      stmt = query(QS.genTerminalAndAuthForBatch(batchid));
      if(stmt != null) {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          ret = new Authid(getIntFromRS(authorizer.authid, rs));
        }
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
      return ret;
    }
  }

  public final Terminalid getTerminalForTermauth(TermAuthid termauthid) {
    Statement stmt = null;
    Terminalid ret = null;
    try {
      stmt = query(QS.genTerminalAndAuthForTermauth(termauthid));
      if(stmt != null) {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          ret = new Terminalid(getIntFromRS(terminal.terminalid, rs));
        }
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
      return ret;
    }
  }

  public final Authid getAuthForTermauth(TermAuthid termauthid) {
    Statement stmt = null;
    Authid ret = null;
    try {
      stmt = query(QS.genTerminalAndAuthForTermauth(termauthid));
      if(stmt != null) {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          ret = new Authid(getIntFromRS(authorizer.authid, rs));
        }
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
      return ret;
    }
  }

  public final Terminalid getTerminalForTxnid(Txnid txnid) {
    return new Terminalid(getIntFromQuery(QS.genTerminalForTxnid(txnid)));
  }

  public final TxnRow getDrawerQuery(Drawerid drawerid, Terminalid terminalid, boolean onlyContribute) {
    return TxnRow.NewSet(query(QS.genDrawerQuery(drawerid, terminalid, onlyContribute)));
  }

  // concatenates the paytype code and institution code to create a 3-char string for SubTotaller use
  public TextList getStorePayInst(Storeid storeid) {
    TextList tl = new TextList();
    Statement stmt = query(QS.genStorePayInst(storeid));
    if(stmt != null) {
      ResultSet rs = getResultSet(stmt);
      while(next(rs)) {
        String pt = getStringFromRS(storeauth.paytype, rs);
        int len = StringX.lengthOf(pt);
        switch(len) {
          case 0: {
            pt = " ";
          }
          break; case 1: {
            // fine; leave alone
          }
          break; default: {
            // > 1; shouldn't ever be
            pt = StringX.left(pt, 1);
          }
        }
        tl.add(pt + getStringFromRS(storeauth.institution, rs));
      }
    } else {
      dbg.ERROR("Error getting store institutions for storeid=" + storeid);
      return null;
    }
    return tl;
  }

  /**
   * close One terminal's drawer.
   */
  public Drawerid closeDrawer(Terminalid Tid, Associateid associd, boolean auto) {
    if(associd == null) {
      associd = new Associateid();
    }
    Drawerid drawerid = getNextDrawerid();
    update(QS.genCreateCloseDrawer(Tid, Now(), associd, drawerid, auto)); // stuffs the drawerid
    if(drawerid.isValid()) {
      // stamp the txns
      Txnid[] txns = (Txnid[]) getUniqueIdExtentArrayFromQuery(QS.genGetDrawerableTxns(Tid), Txnid.class);
      int total = txns.length;
      int count = 0;
      for(int i = total; i-- > 0; ) {
        int changed = update(QS.genSetDrawerid(txns[i], drawerid));
        if(changed > ObjectX.INVALIDINDEX) {
          count += changed;
        }
      }
      dbg.WARNING("Stamped " + count + " txns for drawer closing: " + drawerid + ".");
    }
    retotalClosedDrawer(drawerid);
    return drawerid;
  }

  public final void retotalClosedDrawer(Drawerid drawerid) {
    // get the numbers
    Statement stmt = null;
    int count = 0;
    int amount = 0;
    try {
      stmt = query(QS.genGetDrawerTotals(drawerid));
      if(stmt != null) {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          count = getIntFromRS(QS.COUNTER, rs);
          amount = getIntFromRS(QS.SUMER, rs);
        }
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      closeStmt(stmt);
    }
    update(QS.genSetDrawerTotals(drawerid, count, amount));
  }

  public UTC getPendingStartTime(Terminalid terminalid) {
    return QS.tranUTC(getPendingStartTimeStr(terminalid));
  }

  public String getPendingStartTimeStr(Terminalid terminalid) {
    return getStringFromQuery(QS.genPendingStartTimeQuery(terminalid));
  }

  // gets all txns that match that clientreftime -- BE CAREFUL!  Only supposed to be used by the ReceiptAgent
  public
  /* package */ TxnRow getTxnsForTime(String clientreftime) {
    dbg.WARNING("getTxnsForTime(): clientreftime=" + clientreftime);
    return TxnRow.NewSet(query(QS.genTxnsForTime(clientreftime)));
  }

  public TxnRow getTxnRecordfromTID(Terminalid terminalId, String clientreftime) {
    dbg.WARNING("getTransactionFromQuery(): terminalId=" + terminalId + ", clientreftime=" + clientreftime);
    if(!Terminalid.isValid(terminalId)) {
      return null;
    } else {
      Txnid id = new Txnid(getIntFromQuery(QS.genTransactionFromQuery(terminalId, clientreftime)));
      return getTxnRecordfromTID(id);
    }
  }

  public Txnid getTxnid(Terminalid terminalId, STAN stan) {
    return new Txnid(getIntFromQuery(QS.genTxnid(terminalId, stan)));
  }

  public Txnid getTxnidFromAuthrrn(Terminalid terminalId, String authrrn) {
    return new Txnid(getIntFromQuery(QS.genTxnid(terminalId, authrrn)));
  }

  /**
   * @return tref after finding its txnid
   */
  public TxnReference getTxnid(TxnReference tref) {
    if(STAN.isValid(tref.STAN())) { // no point searching if there is no STAN
      tref.txnId = new Txnid(getIntFromQuery(QS.genTxnid(tref.termid, tref.STAN())));
    }
    return tref;
  }

  /**
   * @return record referenced by txnid stored in some other record
   * @todo go through history table if not found in txn.
   */
  public TxnRow getTxnRecordfromTID(Txnid txnid) {
    TxnRow rec = TxnRow.NewOne(getRecordProperties(txn, txnid));
    if(rec != null) {
      rec.authattempt = loadAuthAttempt(rec.txnid());
    }
    return rec;
  }

  // pass it either an AuthAttemptid or a Txnid
  public final AuthAttempt loadAuthAttempt(UniqueId id) {
    AuthAttempt attempt = new AuthAttempt();
    loadAuthAttempt(attempt, query(QS.AuthAttempt(id)));
    return attempt;
  }

  private final void loadAuthAttempt(AuthAttempt attempt, Statement stmt) {
    // here,load the authrequest and authresponse into the record
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          byte[] attemptreq = DataCrypt.databaseDecode(getStringFromRS(authattempt.authrequest, rs), attempt.id);
          attempt.setAuthRequest(attemptreq);
          attempt.setEncodedAuthResponse(getStringFromRS(authattempt.authresponse, rs));
        }
      } catch(Exception ex) {
        dbg.Caught(ex);
      } finally {
        closeStmt(stmt);
      }
    }
  }

  public Txnid getVoidForOriginal(Txnid original) {
    return new Txnid(getIntFromQuery(QS.genFindVoidForOriginal(original)));
  }

  // dupcheck
  public TxnRow getTransactionForRetry(Terminalid terminalid, PaymentRequest req) {
    // this has to get back an array and then check each one to see if it matches (the card is in question here)!
    Txnid[] ids = (Txnid[]) getUniqueIdExtentArrayFromQuery(QS.genFindExactly(terminalid, req), Txnid.class);
    for(int i = ids.length; i-- > 0; ) {
      Txnid id = ids[i];
      if(id.isValid()) {
        dbg.VERBOSE("found a potential retry's record");
        TxnRow trans = TxnRow.NewOne(getRecordProperties(txn, id));
        if(StringX.equalStrings(trans.card().accountNumber.Image(), req.card.accountNumber.Image())) {
          return trans;
        }
      }
    }
    return null;
  }

  // search screen
  public TxnRow findTransactionsBy(Storeid storeid, TxnFilter filter, int limit) {
    return TxnRow.NewSet(query(QS.genFindTransactionsBy(filter, storeid).orderbydesc(1).limit(limit)));
  }

  // +++ eventually put someplace like the StoreInfo class?
  // --- I don't want to put StoreInfo knowledge on the pristine Store class / business package ... unless we move that class into that package, or better yet, just use the store object
  static final StoreInfo getStoreInfo(Store store) {
    StoreInfo si = new StoreInfo();
    String name = store.storename;
    String address1 = store.address1;
    String city = store.city;
    String state = store.state;
    String country = store.country;
    si.setNameLocation(name, address1, city, state, country);
    si.timeZoneName = store.timeZoneStr();
    si.type = store.merchanttype;
    si.slim = new StandinLimit(new RealMoney(store.silimit), new RealMoney(store.sitotal));
    si.enauthonly = store.enauthonly;
    si.enmodify = store.enmodify;
    return si;
  }

  static final ReceiptFormat receiptInfoFromRS(Store store) {
    ReceiptFormat newone = new ReceiptFormat();
    // unescape is needed for embedded '\n's in the receipt headers and footers
    newone.Header = StringX.unescapeAll(store.receiptheader);
    newone.Tagline = StringX.unescapeAll(store.receipttagline);
    newone.TimeFormat = StringX.OnTrivial(StringX.unescapeAll(store.receipttimeformat).trim(), ReceiptFormat.DefaultTimeFormat);
    newone.showSignature = store.receiptshowsig; // defaults to false
    newone.abide = StringX.OnTrivial(store.receiptabide, newone.abide);
    return newone;
  }

  static final TerminalCapabilities termcapFromRS(Store store) {
    EasyCursor ezp = new EasyCursor();
    // credit
    ezp.setBoolean(TerminalCapabilities.creditAllowedKey, store.creditallowed);
    // checks
    ezp.setBoolean(TerminalCapabilities.checksAllowedKey, store.checksallowed);
    ezp.setBoolean(TerminalCapabilities.alwaysIDKey, store.alwaysid);
    // debit
    ezp.setBoolean(TerminalCapabilities.debitAllowedKey, store.debitallowed);
    // debit push
    ezp.setBoolean(TerminalCapabilities.pushDebitKey, store.pushdebit);
    ezp.setLong(TerminalCapabilities.debitPushThresholdKey, store.debitpushthreshold); // cents
    // misc
    ezp.setBoolean(TerminalCapabilities.autoCompleteKey, store.autocomplete);
    ezp.setBoolean(TerminalCapabilities.freePassKey, store.freepass); //#lubys#, but Taco probably wouldn't mind.
    ezp.setBoolean(TerminalCapabilities.autoApproveKey, store.autoapprove); //#lubys# must be true, taco would be surprised! but still probably wouldn't mind
    ezp.setBoolean(TerminalCapabilities.autoQueryKey, store.autoquery);
    ezp.setBoolean(TerminalCapabilities.enMerchRefKey, store.enmerchref);
    ezp.setBoolean(TerminalCapabilities.enAutoLogoutKey, store.enautologout);
    ezp.setString(TerminalCapabilities.MerchRefPromptKey, store.merchreflabel); // it could be null
    return new TerminalCapabilities(ezp);
  }

  public final StoreConfig getStoreConfig(Store store) {
    StoreConfig cfg = new StoreConfig();
    cfg.si = getStoreInfo(store);
    cfg.receipt = receiptInfoFromRS(store);
    cfg.termcap = termcapFromRS(store);
    cfg.sigcapThreshold.setto(store.sigcapthresh);
    return cfg;
  }

  /**
   * @return multiple terminal connection, null if appliance not found
   */
  public ConnectionReply getApplianceInfo(Applianceid applianceid) {
    ConnectionReply tlr = null;
    Appliance appliance = ApplianceHome.Get(applianceid);
    if(appliance != null) {
      tlr = new ConnectionReply(appliance.applname);
      Store store = StoreHome.Get(appliance.storeid);
      tlr.cfg = getStoreConfig(store);
      tlr.status.setto(ActionReplyStatus.Success); //moved here so that we can make variants.
      // appliance query, returns all terminals for this appliance
      Terminalid[] terms = (Terminalid[]) getUniqueIdExtentArrayFromQuery(QS.genTerminalsforAppliance(applianceid), Terminalid.class);
      for(int i = terms.length; i-- > 0; ) {
        tlr.add(getTerminalInfo(terms[i]));
        //in the future we investigate hardware table and build "testpos.properties" here.
      }
    }
    return tlr;
  }

  public final Applianceid[] getAppliances(Storeid storeid) {
    return(Applianceid[]) getUniqueIdExtentArrayFromQuery(QS.genApplianceRowQuery(storeid), Applianceid.class);
  }

  public TextList getTerminalsForAppliance(Applianceid applianceid, boolean withids) {
    TextList ret = new TextList();
    Statement apple = query(QS.genApplianceTerminalsQuery(applianceid)); // appliance query, returns all terminals for this appliance
    if(apple != null) {
      try {
        ResultSet rs = getResultSet(apple);
        while(next(rs)) { //each one is a terminal; get rid of this next block when we have an appliances table! ---
          ret.add( (withids ? getStringFromRS(terminal.terminalid, rs) + ":" : "") + getStringFromRS(terminal.terminalname, rs));
        }
      } catch(Exception t) {
        dbg.ERROR("Exception getting appliance terminals info!");
        dbg.Caught(t);
      } finally {
        closeStmt(apple);
      }
    }
    return ret;
  }

  public TerminalPendingRow getTerminal(Terminalid terminalid) {
    Statement stmt = null;
    try {
      stmt = query(QS.genTerminalPendingRow(terminalid));
    } catch(Exception arf) {
      dbg.ERROR("Swallowed in getTerminal:" + arf);
    } finally {
      return TerminalPendingRow.NewSet(stmt);
    }
  }

  public TerminalPendingRow getTerminalsForStore(Storeid storeid) {
    Statement stmt = null;
    try {
      stmt = query(QS.genTerminalsForStore(storeid));
    } catch(Exception arf) {
      dbg.ERROR("Swallowed in getTerminalsForStore:" + arf);
    } finally {
      return TerminalPendingRow.NewSet(stmt);
    }
  }

  public Terminalid[] getTerminalidsForStore(Storeid storeid) {
    if(Storeid.isValid(storeid)) {
      return(Terminalid[]) getUniqueIdExtentArrayFromQuery(QS.genTerminalidsForStore(storeid), Terminalid.class);
    }
    return new Terminalid[0];
  }

  // get the totals for this drawer:
  public void getTerminalPendingTotals(TerminalPendingRow tpr) {
    Terminalid terminalid = new Terminalid(tpr.terminalid);
    tpr.lastCloseTime(getPendingStartTimeStr(terminalid)); // for display
    tpr.apprAmount(0);
    tpr.apprCount(0);
    Statement stmt = query(QS.genTerminalPendingTotal(terminalid));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          int count = getIntFromRS(QS.COUNTER, rs);
          long amount = getLongFromRS(QS.SUMER, rs);
          String lasttxntime = getStringFromRS(QS.LASTTXNTIME, rs);
          tpr.apprCount(count);
          tpr.apprAmount(amount);
          tpr.lastTxnTime(lasttxntime);
        }
      } catch(Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
  }

  // These are a kludge.  We need SS2 to do this right!
  public UTC getLastAutoDrawerTime(Storeid storeid) {
    Terminalid[] tids = getTerminalidsForStore(storeid);
    String bigtime = tids.length > 0 ? getStringFromQuery(QS.genLastAutoDrawerQuery(tids)) : "";
    return QS.tranUTC(bigtime);
  }

  public UTC getLastAutoDepositTime(Storeid storeid) {
    Terminalid[] tids = getTerminalidsForStore(storeid);
    TextList tl = new TextList(tids.length);
    for(int i = tids.length; i-- > 0; ) {
      TermAuthid[] taids = getTermauthids(tids[i]);
      for(int j = taids.length; j-- > 0; ) {
        tl.add(taids[j].toString());
      }
    }
    String bigtime = tl.size() > 0 ? getStringFromQuery(QS.genLastAutoBatchQuery(tl)) : "";
    return QS.tranUTC(bigtime);
  }

  // get last time this termauth closed
  public String getTermAuthLastSubmit(TermAuthid termauthid) {
    return getStringFromQuery(QS.genTermAuthLastSubmit(termauthid));
  }

  // here on down is the new auth bill report ...

  public TextList getUsedTtPtIn(Authid authid, Storeid storeid, TimeRange daterange) {
    Statement stmt = query(QS.genUsedTtPtIn(authid, storeid, daterange));
    TextList tl = new TextList();
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        PayInfo pi = new PayInfo();
        while(next(rs)) {
          pi.clear();
          pi.tt = getStringFromRS(txn.transfertype, rs);
          pi.pt = getStringFromRS(txn.paytype, rs);
          pi.in = getStringFromRS(txn.institution, rs);
          tl.add(pi.cat());
        }
      } catch(Exception ex) {
        dbg.Caught(ex);
      } finally {
        closeStmt(stmt);
      }
    }
    return tl;
  }

  public void getOpenBatchPendingTotals(DepositRow deposit) {
    TermAuthid termauthid = new TermAuthid(deposit.termauthid);
    deposit.setLastBatchtime(getTermAuthLastSubmit(termauthid));
    deposit.apprAmount(0);
    deposit.apprCount(0);
    Statement stmt = query(QS.genTermAuthPendingTotal(termauthid));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        if(next(rs)) {
          int count = getIntFromRS(QS.COUNTER, rs);
          long amount = getLongFromRS(QS.SUMER, rs);
          String lasttxntime = getStringFromRS(QS.LASTTXNTIME, rs);
          deposit.apprCount(count);
          deposit.apprAmount(amount);
          deposit.lastTxnTime(lasttxntime);
        }
      } catch(Exception e) {
        dbg.Caught(e);
      } finally {
        closeStmt(stmt);
      }
    }
  }

  public AuthStoreFullRow getFullAuthStore(Authid authid, Storeid storeid) {
    Statement stmt = query(QS.genFullAuthStore(authid, storeid));
    return AuthStoreFullRow.NewSet(stmt);
  }

  public TermBatchReportRow getTermBatchReport(TextList termauthidinlist, TimeRange daterange) {
    Statement stmt = query(QS.genTermBatchReport(termauthidinlist, daterange));
    return TermBatchReportRow.NewSet(stmt);
  }

  public boolean getTermsInfoForStores(Storeid storeid, Authid authid, TermBatchReportTermInfoList termInfoList, TextList tl) {
    Statement stmt = query(QS.genTermsInfoForStores(storeid, authid));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        Terminalid termid = null;
        String terminalname = null;
        String authtermid = null;
        TermAuthid termauthid = null;
        while(next(rs)) {
          termid = new Terminalid(getStringFromRS(terminal.terminalid, rs));
          terminalname = getStringFromRS(terminal.terminalname, rs);
          authtermid = getStringFromRS(termauth.authtermid, rs);
          termauthid = new TermAuthid(getStringFromRS(termauth.termauthid, rs));
          termInfoList.add(termid, terminalname, authtermid, termauthid);
          tl.assurePresent(String.valueOf(termauthid));
        }
      } catch(Exception ex) {
        dbg.Caught(ex);
      } finally {
        closeStmt(stmt);
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean getBatchTxnCounts(Batchid batchid, SubTotaller totaller) {
    // get the totals from the database:
    // 1 run this query: SELECT paytype, institution, transfertype, count(txnid) from txn where txn.batchid = [batchid] and authendtime > '2'  group by paytype, institution, transfertype order by paytype, institution, transfertype
    Statement stmt = query(QS.genBatchTxnCounts(batchid));
    if(stmt != null) {
      try {
        ResultSet rs = getResultSet(stmt);
        // 2 skip through its records and add up the totals
        PayInfo pi = new PayInfo();
        while(next(rs)) {
          pi.clear();
          pi.tt = getStringFromRS(txn.transfertype, rs);
          pi.pt = getStringFromRS(txn.paytype, rs);
          pi.in = getStringFromRS(txn.institution, rs);
          totaller.add(pi.cat(), getIntFromRS("counter", rs));
        }
      } catch(Exception ex) {
        dbg.Caught(ex);
      } finally {
        closeStmt(stmt);
      }
      return true;
    } else {
      return false;
    }
  }

  public void setSignature(TxnReference tref, Hancock signature) {
    Txnid id = tref.txnId;
    TxnRow trow = null;
    if(!Txnid.isValid(id)) {
      id = getTxnid(tref).txnId;
    }
    if(!Txnid.isValid(id)) {
      service.PANIC("Unable to find txn for receipt: " + tref);
    } else {
      trow = getTxnRecordfromTID(id);
      if(trow != null) {
        trow.setSignature(signature);
        int i = setRecordProperties(txn, id, trow.toProperties());
        if(i != 1) {
          String note = (i == 0) ? "signature probably already existed" : "unknown error";
          service.PANIC("Possible error storing signature for txn=" + id + ", i=" + i + ", " + note);
        } else {
          // everything went fine
        }
      } else {
        service.PANIC("Error finding txnrow for storing signature for txnid=" + id);
      }
    }
  }

  public Hancock getSignature(Txnid id) {
    if(Txnid.isValid(id)) {
      TxnRow row = getTxnRecordfromTID(id);
      return(row != null) ? row.getSignature() : null;
    } else {
      return null;
    }
  }

  public Statement getDups(String sincedate) {
    return query(QS.genDupCheck(sincedate));
  }

  public int getMaxPk(TableProfile tp) {
    return getIntFromQuery(QS.genMaxId(tp.primaryKey.field));
  }

  public EasyProperties getStoreTxnTotals() {
    return rowsToProperties(QS.genStoreTxnCounts(), store.storeid.name(), "txncount");
  }

  public TxnRow getLastStoreTxn(Storeid storeid) {
    return TxnRow.NewSet(query(QS.genLastStoreTxn(storeid)));
  }

//  /*package*/ QueryString webONLYpreInsert(TableProfile tp) {
//    if(tp != null) {
//      // first, determine if one of the fields is a primary index
//      PrimaryKeyProfile pkp = tp.primaryKey;
//      UniqueId primarykey = null;
//      if(pkp != null) { // if a primary is found, generate a key for it
//        primarykey = new UniqueId(getIntFromQuery(QS.genSelectNextVal(pkp.field)));
//      }
//      // go through the fields and insert default values for those that have it
//      // and insert the key for the primary key field
//      return QS.genDefaultsInsert(tp, primarykey);
//    }
//    return null;
//  }
//
//  public String webONLYpreInsert(String table) {
//    TableProfile tp = tableProfileFromName(table);
//    if(tp != null) {
//      QueryString qs = webONLYpreInsert(tp);
//      return qs != null? StringX.replace(qs.toString(), "'''", "'") : "Error creating insert for table " + table + "!";
//    } else {
//      return "Definition for table " + table + " not found!";
//    }
//  }
//
//  public int webONLYUpdate(String query, UniqueId id) throws Exception {
//    return update(QueryString.Clause().cat(query), true);
//  }
//
//  public Statement webONLYQuery(String query) throws Exception {
//    return query(QueryString.Clause().cat(query), true);
//  }

  //////////////////////////////////////////
  // validator stuff

  private Monitor PayMateDBclassMonitor = null;
  private static boolean mustValidate = false;

  public static final synchronized boolean init(DBConnInfo dbc, boolean isProduction, boolean shouldValidate) {
    boolean ret = false;
    try {
      // SS2
      prepareSinetTables();
      PayMateDBDispenser.init(dbc); // initialize the dispenser
      smartVacuumer = new SmartVacuumer(new PayMateDBDispenser());
      DBMacros.init(); // initialize the parent class
      PayMateDB db = PayMateDBDispenser.getPayMateDB();
      mustValidate = isProduction || shouldValidate;
      ret = db.validate(); // on test systems, we can turn shouldValidate to fales to prevent it
      if(smartVacuumer != null) {
        smartVacuumer.up(); // bring up the smart vacuumer
      } else {
        // +++ BITCH LIKE HELL!!!
      }
    } catch(Exception ex) {
      dbg.Caught(ex);
    } finally {
      return ret;
    }
  }

  public static SmartVacuumer smartVacuumer = null;

  TextList validatorPanicMsgs = new TextList();

  protected final boolean validate() {
    StopWatch sw = new StopWatch();
    // +++ have a profile that we use to pass all of the functions like
    // fieldExists() that is kept up-to-date and refreshes parts of itself
    // when needed. (listener?)
    try {
      PayMateDBclassMonitor.getMonitor();
      if(validated()) {
        return true; //why check it again? validated();
      }
      boolean validated = mustValidate ? doValidation() : true;
      validated(validated);
    } catch(Exception e) {
      dbv.Caught(e);
    } finally {
      PayMateDBclassMonitor.freeMonitor();
      service.PANIC("Validator completed after " + DateX.millisToTime(sw.Stop()) + ", issues[" + validatorPanicMsgs.size() + "]:", validatorPanicMsgs);
      startCaretakers();
    }
    return validated();
  }

  private static boolean ALLOWDELETEFIELDS = true;
  private static boolean ALLOWDELETETABLES = true;
  private static boolean ALLOWDELETEINDEXES = true;
  // used both before and after validation (foreground and background) ...
  private static final ColumnVector defaultNewFields = new ColumnVector();

  private boolean doValidation() throws Exception {
    /*
     As you do this, add lines to a textlist as to what you did...
     Skip through the spec'd tables list:
      Per table, check to see if spec'd table exists.
        If not, create it and add it to the ModifiedTables list.
        If so,
          Check its fields [create lists of missing, wrong, and extra fields].
            If any fields need to be modified, backup table.
            Create/modify fields.
            Add it to the ModifiedTables list.
          Check the primary key
            If a primary key exists, but doesn't match the one we need (even if we don't need one), drop it.
            If no primary key exists, create it.
          Check the foreign keys
            If any foreign keys exist that we don't need, drop them.
            If any of the needed foreign keys don't exist, create them.
          Check the indexes
            If any indexes exist that aren't needed, drop them.
            If any of the needed indexes don't exist, create them.
     See if any tables need content modifications
     Delete fields in the DeleteFields list
      For any table in the list, add it to the modified tables list
     Skip through the reality tables list:
      Per table, see if it shouldn't exist
      If it shouldn't,
        delete it
     If there are any lines in the textlist, be sure to send them in a panic!
     */
    // profile what exists:
    dbg.ERROR("Validating database ...");
    System.out.println("PayMateDB.doValidateStart: current logging looks like this:\nLogswitches:\n" + LogSwitch.listLevels() + "\nPrintforks:\n" + PrintFork.asProperties());
    prevalidate();
    DatabaseProfile dbp = profileDatabase("mainsail", ALLTABLES, true /* sort */);
    TableVector modifiedTables = new TableVector();
    ColumnVector dropFields = new ColumnVector();
    ColumnVector setNullFields = new ColumnVector();
    dbv.mark("Validating tables, spec'd->reality ...");
    for(int i = tables.length; i-- > 0; ) {
      TableProfile tpspecd = tables[i];
      dbv.mark("Validating " + tpspecd + " existence...");
      TableProfile tpreality = dbp.tableFromName(tpspecd.name());
      boolean exists = (tpreality != null);
      if(!exists) {
        // Table does not exist, create it
        int done = validateAddTable(tpspecd);
        validatePrintln("Spec'd table " + tpspecd + " DID NOT exist and was " + (done == DONE ? "" : (done == ALREADY ? "ALREADY " : "NOT ")) + "CREATED!");
        // refresh this table's reality ...
        DatabaseProfile dbptemp = profileDatabase("mainsail", tpspecd.name(), false);
        tpreality = dbptemp.tableFromName(tpspecd.name());
        exists = (tpreality != null);
      } else {
        // don't do anything
      }
      // ^ if table doesn't exist, create it, then validate it below
      // (some things are not set correctly on creation)
      if(exists) {
        // table exists, ok
        dbv.mark("Validating " + tpspecd + " fields...");
        dbv.VERBOSE("Spec'd table " + tpspecd + " EXISTS, and will have its contents checked.");
        ColumnProfile[] columnsSpecd = tpspecd.columns();
        for(int coli = columnsSpecd.length; coli-- > 0; ) {
          ColumnProfile colspecd = columnsSpecd[coli];
          ColumnProfile colreal = tpreality.column(colspecd.name());
          if(colreal == null) {
            boolean added = validateAddField(colspecd) != FAILED;
            validatePrintln("Spec'd column " + colspecd.fullName() + " DID NOT exist and was " + (added ? "" : "NOT ") + "ADDed!");
            if(added) { // get it anew
              colreal = profileTable(tpspecd).column(colspecd.name());
// Also, since PG doesn't enforce default and notnull content when a field is created,
// we must do it now.  So, be sure to change all entries to the defaults for the field, if one exists.
// Otherwise, if you set the default and notnull later,
// the notnull may not take since the default didn't cause the filling of the fill at field creation time.
              if(colspecd.columnDef != null) {
                // will set the defaults for this and other columns when done with this table
                defaultNewFields.add(colspecd);
              }
            }
          }
          if(colreal != null) {
            // Here, need to check each element of the field independently
            // type
            // @PG@ Note that in PG, to change a TYPE
            // @PG@ you create a new (temp-named) field, copy the data over, then kill the old field and rename the new field to the old fieldname
            if(!colspecd.sameTypeAs(colreal)) { //    if(!StringX.equalStrings(colspecd.type(), colreal.type(), true)) {
              validatePrintln("ValidateColumn: type mismatch! S[" + colspecd.fullName() + "]:" + colspecd.type() + ", R[" + colreal.fullName() + "]:" + colreal.type() + (changeFieldType(colreal, colspecd) ? "" : " NOT") + " Modified!");

            }
            //
            // size does not need to be checked now that we are using unlimited-length text fields.
            //
            // nullable
            if(!colspecd.sameNullableAs(colreal)) {
              setNullFields.add(colspecd); // deal with this after setting default values
              validatePrintln("ValidateColumn: nullable mismatch! S[" + colspecd.fullName() + "]:" + colspecd.nullable() + ", R[" + colreal.fullName() + "]:" + colreal.nullable() + " - to be modified later ...");
            }
            // default value
            if(!colspecd.sameDefaultAs(colreal)) {
              String right = "S[" + colspecd.fullName() + "]:'" + colspecd.columnDef + "', R[" + colreal.fullName() + "]:'" + colreal.columnDef + "'";
              // if SPECd autoincrement +++ and REALdefault starts with 'nextval(',it matched!
              if(colspecd.autoIncrement()) {
                if(colreal.columnDef.startsWith("nextval(")) {
                  dbg.WARNING("ValidateColumn: default mismatch due to SERIAL stuff " + right);
                } else {
                  validatePrintln("ValidateColumn: WARNING - default mismatch, but assuming it is due to SERIAL stuff " + right);
                }
              } else {
                validatePrintln("ValidateColumn: default mismatch! " + right + (changeFieldDefault(colspecd) ? "" : " NOT") + " Modified!");
              }
            }
          }
        }
        ColumnProfile[] columnsReal = tpreality.columns();
        // skip through reality and find extra fields
        int dropFieldsPre = dropFields.size();
        for(int coli = columnsReal.length; coli-- > 0; ) {
          ColumnProfile colreal = columnsReal[coli];
          ColumnProfile colspecd = tpspecd.column(colreal.name());
          if(colspecd == null) {
            dropFields.add(colreal);
          }
        }
        if(dropFields.size() > dropFieldsPre) {
          dbg.ERROR("The following extra fields were found:" + dropFields);
          // don't dropFields now; drop them later [after content validation, in case they are needed for it]!
        } else {
          dbg.VERBOSE("Table " + tpspecd.name() + " fields are okay.");
        }
      } else {
        validatePrintln("Spec'd table " + tpspecd + " DID NOT exist.  Looks like we tried to create it but couldn't!");
      }
    } // end for each table
    // make any changes needed to content. [this includes setting defaults in all new fields of existing tables]
    validateContent(defaultNewFields);
    // NOW handle notnull setting for new fields, mostly (only after setting defaults)
    for(int setnuli = setNullFields.size(); setnuli-- > 0; ) {
      ColumnProfile colspecd = setNullFields.itemAt(setnuli);
      validatePrintln("ValidateColumnNull: S[" + colspecd.fullName() + "]:" + colspecd.nullable() + " - setting " + (changeFieldNullable(colspecd) ? "" : " NOT") + " Modified!");
    }
    // delete fields in the deletefields list & add their table to the modifiedtables list
    for(int coli = dropFields.size(); coli-- > 0; ) {
      ColumnProfile colspecd = dropFields.itemAt(coli);
      boolean wasDropped = ALLOWDELETEFIELDS ? dropField(colspecd) : false;
      validatePrintln("Existing field " + colspecd.fullName() + " DOES NOT have a profile spec'd, and has " + (wasDropped ? "" : "NOT ") + "been DROPPED!" + (wasDropped ? "" : " You may need to do it by hand."));
    }
    // Skip through the reality tables list:
    dbv.mark("Validating table existence, reality->spec'd ...");
    // reprofiling so that we can pickup the changes made above ...
    dbp = profileDatabase("mainsail", ALLTABLES, true /* sort */);
    for(int i = dbp.size(); i-- > 0; ) {
      // Per table, see if it shouldn't exist
      TableProfile tpreality = dbp.itemAt(i);
      dbv.mark("Validating " + tpreality + " necessity ...");
      TableProfile tpspecd = tableProfileFromName(tpreality.name());
      DatabaseMetaData dbmd = getDatabaseMetadata();
      if(tpspecd == null) {
        // If it shouldn't, delete it
        boolean dropped = ALLOWDELETETABLES ? dropTable(tpreality.name()) : false;
        validatePrintln("Existing table " + tpreality + " DOES NOT have a profile spec'd, and has " + (dropped ? "" : "NOT ") + "been DROPPED!");
      } else {
        // check to see if it has any indexes or keys that are NOT in the spec'd tables & delete them.
        ResultSet rs = null;
        // check primary key
        dbv.mark("Validating " + tpreality + " primary key...");
        rs = dbmd.getPrimaryKeys(null, null, tpreality.name());
        if(next(rs)) {
          String oldPrimaryKey = getStringFromRS("PK_NAME", rs);
          if(!StringX.equalStrings(tpspecd.primaryKey.name, oldPrimaryKey, true)) { // +++ constants like this need to be put somewhere +++
            // DROP THE PRIMARY KEY !!!
            boolean dropped = dropTableConstraint(tpreality.name(), oldPrimaryKey);
            validatePrintln("Existing Primary Key " + tpreality.name() + "." + oldPrimaryKey + " IS INVALID and has " + (dropped ? "" : "NOT ") + "been DROPPED!");
          } else {
            // +++ check the content of the primary key, too [mod if needed]!
          }
        }
        closeRS(rs);
        // check foreign key
        dbv.mark("Validating " + tpreality + " foreign keys...");
        rs = dbmd.getImportedKeys(null, null, tpreality.name());
        while(next(rs)) {
          // since PG puts CRAP at the end of the string ...
          String oldForeignKey = extractFKJustName(getStringFromRS("FK_NAME", rs));
          boolean needed = false;
          // create a get for this +++
          if(tpspecd.foreignKeys != null) {
            for(int fki = tpspecd.foreignKeys.length; fki-- > 0; ) {
              ForeignKeyProfile fkp = tpspecd.foreignKeys[fki];
              if(StringX.equalStrings(fkp.name, oldForeignKey, true)) {
                needed = true;
                break;
              }
            }
          }
          if(!needed) {
            boolean dropped = dropTableConstraint(tpreality.name(), oldForeignKey);
            validatePrintln("Existing Foreign Key " + tpreality.name() + "." + oldForeignKey + " IS INVALID and has " + (dropped ? "" : "NOT ") + "been DROPPED!");
          } else {
            // +++ check the content of the foreign key, too [mod if needed]!
          }
        }
        closeRS(rs);
        // check indexes
        dbv.mark("Validating " + tpreality + " indexes...");
        rs = dbmd.getIndexInfo(null, null, tpreality.name(), false, false
                               /*true -- testing !!!*/);
        while(next(rs)) {
          String realindex = getStringFromRS("INDEX_NAME", rs);
          boolean needed = false;
          // +++ create a get() for this ...
          if(tpspecd.indexes != null) {
            for(int ii = tpspecd.indexes.length; ii-- > 0; ) {
              IndexProfile ip = tpspecd.indexes[ii];
              if(StringX.equalStrings(ip.name, realindex, true)) {
                needed = true;
                break;
              }
            }
          }
          // check to see if it is for a primary key
          if(!needed && (tpspecd.primaryKey != null) && StringX.equalStrings(tpspecd.primaryKey.name, realindex, true)) {
            needed = true; // don't try to get rid of our primary key indexes
          }
          if(!needed) {
            boolean dropped = ALLOWDELETEINDEXES ? dropIndex(realindex, tpreality.name()) : false;
            validatePrintln("Existing Index " + realindex + " IS INVALID and has " + (dropped ? "" : "NOT ") + "been DROPPED!");
          } else {
            // +++ check the content of the index, too [mod if needed]!
          }
        }
        closeRS(rs);
      }
    }
    // Do these last so that they don't affect speed!
    // For each table spec'd, go through and see if any indexes and keys are NOT in the reality tables.  Add them.
    DatabaseMetaData dbmd = getDatabaseMetadata();
    for(int i = tables.length; i-- > 0; ) {
      TableProfile tpspecd = tables[i];
      // check primary key
      dbv.mark("Validating " + tpspecd + " primary key...");
      int done = validateAddPrimaryKey(tpspecd.primaryKey);
      conditionalValidatePrintln("Specified table " + tpspecd + (done == DONE ? " ADDed" : (done == ALREADY ? " ALREADY had" : " could NOT add")) + " primary key " + tpspecd.primaryKey.name + "!", done);
      // check indexes
      dbv.mark("Validating " + tpspecd + " indexes...");
      if(tpspecd.indexes != null) {
        for(int ii = tpspecd.indexes.length; ii-- > 0; ) {
          IndexProfile ip = tpspecd.indexes[ii];
          done = validateAddIndex(ip);
          conditionalValidatePrintln("Specified table " + tpspecd + (done == DONE ? " ADDed" : (done == ALREADY ? " ALREADY had" : " could NOT add")) + " index " + ip.name + "!", done);
        }
      }
    }
    // Check the foreign keys
    // For each table spec'd, go through and see if any indexes and keys are NOT in the reality tables.  Add them.
    for(int i = tables.length; i-- > 0; ) {
      TableProfile tpspecd = tables[i];
      int done;
      dbv.mark("Validating " + tpspecd + " foreign keys...");
      if(tpspecd.foreignKeys != null) {
        for(int fki = tpspecd.foreignKeys.length; fki-- > 0; ) {
          ForeignKeyProfile fkp = tpspecd.foreignKeys[fki];
          done = validateAddForeignKey(fkp);
          conditionalValidatePrintln("Specified table " + tpspecd + (done == DONE ? " ADDed" : (done == ALREADY ? " ALREADY had" : " could NOT add")) + " foreign key " + fkp.name + "!", done);
        }
      }
    }
    postvalidate();
    dbg.ERROR("Database validated.");
    return true;
  }

  public static final synchronized void startBackgroundValidator() {
    if(bv == null) {
      bv = new BackgroundValidator(new PayMateDBDispenser());
    }
    if(mustValidate) {
      bv.up();
    }
    service.PANIC("BackgroundValidator " + (bv.isUp() ? "" : "NOT ") + "started!");
  }

  private static BackgroundValidator bv = null;

  // Performed BEFORE any other validation!
  private void prevalidate() {
    dbv.mark("prevalidate");
//    // if you rename fields here for later use,
//    // rename their indexes in the table profile so new ones get created
//    if(!fieldExists(txn.name(), paytypeconvert.name())) {
//       renameField(txn.name(), txn.paytype.name(), paytypeconvert.name()); // check return value ?
//    }
//    changeFieldNullable(paytypeconvert); // turn off the notnulls!
//    if(!fieldExists(txn.name(), transfertypeconvert.name())) {
//       renameField(txn.name(), txn.transfertype.name(), transfertypeconvert.name()); // check return value ?
//    }
//    changeFieldNullable(transfertypeconvert); // turn off the notnulls!
//    if(!fieldExists(storeauth.name(), sapaytypeconvert.name())) {
//      renameField(storeauth.name(), storeauth.paytype.name(), sapaytypeconvert.name()); // check return value ?
//    }
//    changeFieldNullable(sapaytypeconvert); // turn off the notnulls!

    // card table will be recreated and stuffed later ...
    dropTable(card.name()); // check return value ?
  }

  // Performed BETWEEN adding/modifying tables/fields and deleting tables/fields.
  private void validateContent(ColumnVector defaultNewFields) {
    dbv.mark("validateContent");
    // +++ what are the postgresql rules for adding a serial field to an existing table ???
    rebuildCardTable(); // MMM: pre 20020502, but I don't remember when
    // sets default values of new fields for every record, plus sets other values of interest, upon need
    allTablesAllExistingRows(service, GenericTableProfile.cfgType);
    // +++ delete all indexes here so that they will get recreated?
  }

  // move all of this stuff to DBValidator class, and extend for DBBGValidator

  private static double interTableSleepfor = 10.0; // seconds +++ get from configs
  private static long interRowSleepIncr = 1; // ms +++ get from configs
  private static int sleepJumpTxnCount = 10000; // increase the sleep by interRowSleepIncr every this many rows
  public static int thistablecount = 0; // how many records have been processed in this table
  public static long totalmaxid = 0; // roughly how many records will be processed
  public static long totaltablecount = 0; // how many records have been processed in previous tables
  private static StopWatch elapsed = new StopWatch(false);

  public static String bgvalidatorStatus() {
    // what percentage are we done?
    long rowsdone = Math.max(thistablecount + totaltablecount, 1L /*prevents/0*/);
    // when will we be done?
    long elapsedms = elapsed.millis();
    long avg = elapsedms / rowsdone;
    long millisLeft = (avg * totalmaxid) - elapsedms;
    UTC when = UTC.New(UTC.Now().getTime() + millisLeft);
    LocalTimeFormat ltf = LocalTimeFormat.New(TimeZone.getTimeZone("America/Chicago"), ReceiptFormat.DefaultTimeFormat);
    LedgerValue lv = new LedgerValue("##0.0"); // hidden pennies
    long donage = (long) Math.floor(1000.0 * rowsdone / Math.max(totalmaxid, 1L /*prevents/0*/)) * 10; // extra 10 is for the hidden pennies in the format
    lv.setto(donage);
    return lv.Image() + "% -> " + ltf.format(when);
  }

  private void allTablesAllExistingRows(Service logger, TableType typeToUse) {
    allTablesAllExistingRows(logger, typeToUse, new Accumulator(), new Accumulator());
  } // stub for when we don't care about the accumulators

  // this function will be run both during validation and afterwards,
  // on the background validator, while the system is up and running!
  // be sure to code it with that in mind!
  private void allTablesAllExistingRows(Service logger, TableType typeToUse, Accumulator reads, Accumulator writes) {
    // This next code handles setting values for all tables whose fields need changing.
    // You can plugin code to set a certain field values for each records in a table,
    // plus it will automatically set the default value for a
    // new field in every record of an existing table.
    //
    // set defaults for all new fields in ALL RECORDS!
    // since we have no idea of the size of this table,
    // we should actually get the key of each record in this table
    // then flip through and change every one!
    // if this table has a million records, that will take a very long time.
    // however, if this table does have a million records,
    // using a single query to make the change for all records
    // will result in a long query that will fail.
    // we can't have that.
    // let's face it.  db validation can take a very long time.
    // We only want to flip through all fields of a table ONCE.  Let's do it here.
    if(typeToUse == null) {
      logger.println("allTablesAllExistingRows typeToUse is null!");
      return;
    }
    boolean checkingLogs = typeToUse.is(TableType.log);
    int rowSleep = 0;
    Class[] functionParameters = {
        EasyProperties.class, EasyProperties.class, UniqueId.class,
    };
    Class myclass = this.getClass();
    Object[] paramlist = {
        null, null, null, };
    EasyProperties before = null;
    TextList msgsToLog = new TextList();
    elapsed.Start();
    LocalTimeFormat ltf = LocalTimeFormat.New(TimeZone.getTimeZone("America/Chicago"), ReceiptFormat.DefaultTimeFormat);
    TableRowCleanerVector trcv = new TableRowCleanerVector();
    // for each table in the database,
    for(int tpi = tables.length; tpi-- > 0; ) {
      if(checkingLogs && bv.shouldstop()) {
        break;
      }
      TableProfile tp = tables[tpi];
      logger.println("Processing table " + tp.name() + ".");
      // check to see what default values need to be set and put them into a properties
      EasyProperties newDefaults = new EasyProperties(); // must make new every time!
      for(int cpi = defaultNewFields.size(); cpi-- > 0; ) {
        ColumnProfile cp = defaultNewFields.itemAt(cpi);
        if(cp.table().compareTo(tp) == 0) {
          newDefaults.setString(cp.name(), cp.columnDef);
          logger.println("Setting default of '" + cp.columnDef + "' for new column '" + cp + "' ...");
        }
      }
      // check to see if there is code to handle the records, too
      Method m = null;
      String tableFunction = ("fix" + tp.name() + "row").toLowerCase();
      try {
        m = myclass.getDeclaredMethod(tableFunction, functionParameters);
      } catch(Exception e) {
        logger.println("Exception trying to find function " + tableFunction + ". Assuming it doesn't need work.");
      }
      boolean hasFunction = (m != null);
      boolean hasDefaults = (newDefaults.size() > 0);
      // if either has content, must flip through all records
      if(hasFunction || hasDefaults) {
        if(!tp.type.equals(typeToUse)) {
          logger.println("Skipping table " + tp.name() + ", as its type[" + tp.type.Image() + "] does not match the type we are checking[" + typeToUse.Image() + "]." + "\nIt would " + (hasFunction ? "" : "NOT ") + "have run a function!" +
                         "\nIt would need the following defaults set: \n" + newDefaults.asParagraph(","));
        } else { // this is not the correct table type
          try {
            TableRowCleaner trc = new TableRowCleaner();
            trc.hasDefaults = hasDefaults;
            trc.hasFunction = hasFunction;
            trc.m = m;
            trc.table = tp;
            trc.pk = tp.primaryKey.field;
            trc.maxid = getMaxPk(trc.table);
            trc.newDefaults = newDefaults;
            trcv.add(trc);
          } catch(Exception e) {
            dbg.Caught(e);
          }
        } // if this is the correct table type
      } // if this table has anything to work on
    } // end for every table
    // now the list is built.
    // decide how many records will be processed (very approximately)
    totalmaxid = 0;
    for(int tpi = trcv.size(); tpi-- > 0; ) {
      totalmaxid += trcv.itemAt(tpi).maxid;
    }
    totaltablecount = 0;
    thistablecount = 0;
    for(int tpi = 0; tpi < trcv.size(); tpi++) { // these need to happen in the correct order
      int changes = 0;
      TableRowCleaner trc = trcv.itemAt(tpi);
      try {
        logger.println("Beginning allTablesAllExistingRows for " + trc.table.name() + ".\nIt " + (trc.hasFunction ? "DOES " : "does NOT ") + " have function.\n" + "It will need the following defaults set: \n" + trc.newDefaults.asParagraph(","));

        // drop all indexes except primary key for this table? +++
        // when rename a field, have to drop its index first?  foreign key, too? +++

        // we MUST flip through every record to do this,
        // even though it will take forever
        // or else we could fail totally due to long transaction!
        logger.println("Updating existing rows from id = " + trc.maxid + " to 1.");
        int i = 0;
        StopWatch sw = new StopWatch(false);
        Accumulator times = new Accumulator();
        StopWatch readsw = new StopWatch(false);
        StopWatch writesw = new StopWatch(false);
        EasyProperties after = new EasyProperties(); // need a new one
        for(int idi = trc.maxid + 1; idi-- > 1; ) {
          if(checkingLogs && bv.shouldstop()) {
            break;
          }
//              sw.Reset();
          sw.Start();
          // take each index and update for that index
          UniqueId id = new UniqueId(idi);
          // does this whole record need to be loaded?  presume so if the function exists
          // if so, load it into a temporary properties, and overwrite it with the defaults
          // however, this record may not exist!!! Check first!
          readsw.Start();
          before = getRecordProperties(trc.table, id, trc.pk);
          reads.add(readsw.Stop());
          if(before.size() == 0) {
            // skip this one
            logger.println("Skipping " + trc.table.name() + "[" + idi + "] - doesn't exist.");
          } else {
            after.clear();
            after.addMore(before); // that we can copy old values into
            after.addMore(trc.newDefaults); // and then overload with new values
            if(trc.hasFunction) {
              // if not, just use the defaults properties
              // then, call the function, if it exists, to overload the other properties
              paramlist[0] = before;
              paramlist[1] = after;
              paramlist[2] = id;
              trc.m.invoke(this, paramlist); // new values
            }
            writesw.Start();
            i = setRecordProperties(before, trc.table, id, after, msgsToLog);
            long time = writesw.Stop();
            String pre = "allTablesAllExistingRows " + trc.table.name() + "[" + idi + "] ";
            switch(i) {
              case 0: {
                logger.println(pre + "no changes needed.");
              }
              break; case 1: {
                logger.println(pre + "record changed.");
                changes++;
                writes.add(time);
              }
              break; default: {
                logger.println(pre + "should have received 0 or 1 on update, but got " + i);
              }
              break;
            }
            // log the changes.
            logger.println(msgsToLog.toString());
            msgsToLog.clear();
          }
          if( ( (thistablecount % sleepJumpTxnCount) == 0) && (thistablecount > 0)) {
            if(checkingLogs) {
              rowSleep += interRowSleepIncr; // add interRowSleepIncr ms to the rowsleep every 10000 records!
            } else {
              // leave it the same
            }
            { // this block allows for better variable cleanup
              long avg = times.getAverage();
              TextList tl = new TextList();
              tl.add("GC()'ing after " + thistablecount + " records checked in table " + trc.table.name() + "...");
              tl.add("Current average per row is " + avg + " ms.");
              tl.add("Estimated total time is " + DateX.millisToTime(avg * trc.maxid) + ".");
              long elapsedms = elapsed.millis();
              tl.add("Elapsed time " + DateX.millisToTime(elapsedms) + ".");
              long millisLeft = (avg * trc.maxid) - elapsedms;
              UTC when = UTC.New(UTC.Now().getTime() + millisLeft);
              tl.add("Estimated end time is " + ltf.format(when) + ".");
              tl.add("Sleeping for " + rowSleep + " ms between rows.");
              logger.PANIC("allTablesAllExistingRows status update:", tl);
              logger.logFile.flush();
            }
            System.gc(); // hint, hint
          }
          ThreadX.sleepFor(rowSleep);
          thistablecount++;
          times.add(sw.Stop());
        }
        logger.println("allTablesAllExistingRows done, checked " + thistablecount + " and changed " + changes + " rows.");
        if(checkingLogs && !bv.shouldstop()) { // means we are in the background
          logger.println("Sleeping " + interTableSleepfor + " seconds between tables to give the system a break!");
          ThreadX.sleepFor(interTableSleepfor);
        }
      } catch(Throwable ex) {
        dbg.Caught(ex);
        logger.println("allTablesAllExistingRows NOT done, only checked " + thistablecount + " and changed " + changes + " rows.");
      }
      totaltablecount += thistablecount;
      thistablecount = 0;
    } // for each table that needs cleaning
    elapsed.Stop();
  } // end allTablesAllExistingRows

  // to create a function to handle every row in a table, write it like (this is for txn):
  // private void fixtxnrow(EasyProperties before, EasyProperties after) {
  //   write code to overwrite any field=value entries in the AFTER one,
  //   using BEFORE as a reference
  // } // it will get overwritten when you exit this function

// terminal/appliance/store fix:
//  private void fixterminalrow(EasyProperties before, EasyProperties after, UniqueId id) {
//    int termstore = before.getInt(terminal.storeid.name());
//    int applstore = getIntFromQuery(QueryString.Select(appliance.storeid).from(appliance).where().nvPair(appliance.applianceid, before.getInt(terminal.applianceid.name())));
//    if(termstore != applstore) {
//      after.setInt(terminal.storeid.name(), applstore);
//    }
//  }

//  private void fixtxnrow(EasyProperties before, EasyProperties after, UniqueId id) {
//    // CISP AND RECEIPTS
//    // old:
//    final String EXPIRATIONDATE = "expirationdate";
//    final String TRACK1DATA = "track1data";
//    final String TRACK2DATA = "track2data";
//    final String CARDHOLDERACCOUNT = "cardholderaccount";
//    final String TERMINALID = "terminalid";
//    // check for new
//    String echa = before.getString(txn.echa.name());
//    String echn = before.getString(txn.echn.name());
//    int cardhash = before.getInt(txn.cardhash.name());
//    int cardlast4 = before.getInt(txn.cardlast4.name());
//    // if no new, set them
//    if((cardlast4 == 0) && (cardhash == 0) &&
//       !StringX.NonTrivial(echn) && !StringX.NonTrivial(echa)) {
//      Terminalid xid = new Terminalid(before.getString(TERMINALID));
//      // get the data from the old fields
//      MSRData card = new MSRData();
//      card.setTrack(MSRData.T1, before.getString(TRACK1DATA));
//      card.ParseTrack1();
//      card.setTrack(MSRData.T2, before.getString(TRACK2DATA));
//      card.ParseTrack2();
//      card.accountNumber.setto(before.getString(CARDHOLDERACCOUNT));
//      card.expirationDate.parseYYmm(before.getString(EXPIRATIONDATE));
//      // set data in the new fields
//      after.setInt(txn.cardhash.name(), card.accountNumber.cardHash());
//      after.setInt(txn.cardlast4.name(), card.accountNumber.last4int());
//      //encrypt the name
//      after.setString(txn.echn.name(), TxnRow.EncodeCardholderName(card.person.CompleteName(), xid));
//      // encrypt the card
//      echa = TxnRow.encryptCardImage(card, xid);
//      after.setString(txn.echa.name(), echa);
//    }
//    // import the receipts
//    if( ! StringX.NonTrivial(before.getString(txn.signature.name())) ) {
//      // since it is trivial, see if we can find one on disk!
//      TxnReference tref = TxnReference.New(new Txnid(id.value()));
//      TextList errors = new TextList();
//      EasyCursor receipter = receiptAgent.loadReceipt(tref, errors);
//      if(receipter != null) {
//        Receipt receipt = new Receipt();
//        receipt.load(receipter);
//        Hancock signature = receipt.getSignature();
//        String sig = TxnRow.HancockToString(signature);
//        if(StringX.NonTrivial(sig)) {
//          after.setString(txn.signature.name(), sig);
//        }
//      }
//    }
//    /////////////////////////// FTF/FORCE/TIPS stuff
//
//    // txn.transfertype - what is each txns transfertype?
//    TransferType tt = new TransferType(before.getChar(txn.transfertype.name()));
//    if(!tt.isLegal()) { // only set it if is needs setting
//      tt = TransferTypeOld.from2digitCode(before.getString(transfertypeconvert.name()));
//      after.setString(txn.transfertype.name(), String.valueOf(tt.Char()));
//    }
//
//    // txn.paytype - what is each txns paytype?
//    PayType ptB4 = new PayType(before.getChar(txn.paytype.name()));
//    if(!ptB4.isLegal()) { // only set it if is needs setting
//      PayType pt = PayTypeOld.from2digitCode(before.getString(paytypeconvert.name()));
//      after.setString(txn.paytype.name(), String.valueOf(pt.Char()));
//    }
//
//    // txn.authz - what needs authorizing? see the FTF spec table for this
//    // from the old system, TT=SA,RT,QY, and TT=RV when actioncode = 'U'
//    String ac = before.getString(txn.actioncode.name());
//    boolean authz = before.getBoolean(txn.authz.name());
//    boolean forceauthz = !authz &&
//        (tt.is(TransferType.Sale) ||
//        tt.is(TransferType.Return) ||
//        tt.is(TransferType.Query) ||
//        (tt.is(TransferType.Reversal) &&
//         (StringX.equalStrings(ActionCode.Unknown, ac) ||
//          StringX.equalStrings(ActionCode.Pending, ac))));
//    // only change it if it needs to be true;
//    // must include it either way to prevent it from being set to false by accident
//    after.setBoolean(txn.authz.name(), forceauthz || authz);
//
//    // txn.settle - what needs settling?   see the FTF spec table for this
//    // from the old system, only TT=SA or RT.
//    boolean settle = before.getBoolean(txn.settle.name());
//    boolean forcesettle = !settle &&
//        (tt.is(TransferType.Sale) || tt.is(TransferType.Return));
//    // only change it if it really needs to be true;
//    // must include it either way to prevent it from being set to false by accident
//    after.setBoolean(txn.settle.name(), forcesettle || settle);
//
//    // txn.settleop - what is each txns settleop? see the FTF spec table for this
//    // all there are are RSQV, and those match our old transfertypes completely
//    SettleOp sop = new SettleOp();
//    sop.setto(before.getChar(txn.settleop.name()));
//    if(!sop.isLegal()) { // only set it if we never have
//      switch (tt.Value()) {
//        case TransferType.Sale: {
//          sop.setto(SettleOp.Sale);
//        }
//        break;
//        case TransferType.Return: {
//          sop.setto(SettleOp.Return);
//        }
//        break;
//        case TransferType.Reversal: {
//          sop.setto(SettleOp.Void);
//        }
//        break;
//        case TransferType.Query: {
//          sop.setto(SettleOp.Query);
//        }
//        break;
//      }
//      after.setChar(txn.settleop.name(), sop.Char());
//    }
//
//    // txn.settleamount - what is each txns settleamount? see the FTF spec table for this
//    // be sneaky and check for empty string!  set auth and settle equal for sales and returns
//    String settleamtstr = before.getString(txn.settleamount.name());
//    if(!StringX.NonTrivial(settleamtstr) && // only set it if it is trivial!
//       (tt.is(TransferType.Sale) ||
//        tt.is(TransferType.Return))) { // set it to auth amount if needed
//      after.setInt(txn.settleamount.name(), before.getInt(txn.amount.name()));
//    }
//  }
//
//  private void fixstoreauthrow(EasyProperties before, EasyProperties after) {
//    /////////////////////////// FTF/FORCE/TIPS stuff
//
//    // storeauth.paytype - what is each row's paytype?
//    PayType ptB4 = new PayType(before.getChar(storeauth.paytype.name()));
//    if(!ptB4.isLegal()) { // only set it if is needs setting
//      PayType pt = PayTypeOld.from2digitCode(before.getString(sapaytypeconvert.name()));
//      after.setString(storeauth.paytype.name(), String.valueOf(pt.Char()));
//    }
//  }

  // Performed AFTER any other validation!
  private void postvalidate() {
    try {
      dbv.mark("postvalidate");
      vacuumFullCfgTables();
    } catch(Exception ex) {
      dbg.Caught(ex);
    }
  }

  // performed in the background while everything else is happening.
  // called by the BackgroundValidator
  /* package */ void backgroundvalidate(Accumulator reads, Accumulator writes) {
    // this function does NOT use the validation textlist to log errors, etc.
    service.PANIC("Background validation starting...");
    StopWatch sw = new StopWatch();
    TextList bgvalErrors = new TextList();
    allTablesAllExistingRows(bv, GenericTableProfile.logType, reads, writes);
    service.PANIC("Background validation complete.  Took " + DateX.millisToTime(sw.Stop()) + ".", "VACUUM DATABASE FULL IS LIKELY NEEDED!");
  }

  private void conditionalValidatePrintln(String msg, int done) {
    if(done == ALREADY) {
      dbg.ERROR(msg);
    } else {
      validatePrintln(msg);
    }
  }

  private void validatePrintln(String msg) {
    dbv.ERROR(msg);
    validatorPanicMsgs.add(msg);
  }

  private void rebuildCardTable() {
    // delete card table and recreate it from BinEntry's
    dbg.ERROR("Rebuilding card table");
    int len = BinEntry.guesser.length; // less overhead to use locals
    for(int i = 0; i < len; i++) { //# must retain order to deal with subranges ???  the select you do should use an index, which makes the insert order moot. ---
      BinEntry be = BinEntry.guesser[i];
      if(be.high == 0) { //undo a hack for manual entry of guessers.
        be.high = be.low;
      }
      EasyCursor ezp = EasyCursor.makeFrom(be);
      // gotta change fields to the db version thereof
      ezp.setString(card.paytype.name(), String.valueOf(be.act.Char()));
      ezp.setString(card.exp.name(), Bool.toString(be.expires));
      ezp.setString(card.enMod10ck.name(), Bool.toString(be.enMod10ck));
      Cardid id = getNextCardid();
      update(QS.genCreateCard(ezp, id)); // +++ check return value?
    }
  }

  public BinEntry getBinEntry(int cardbin) {
    BinEntry ret = null;
    Statement stmt = query(QS.genBinEntry(cardbin));
    ResultSet rs = getResultSet(stmt);
    if(next(rs)) {
      int low = getIntFromRS(card.lowbin, rs);
      int high = getIntFromRS(card.highbin, rs);
      Institution inst = CardIssuer.getFrom2(getStringFromRS(card.institution, rs));
      PayType paytype = new PayType(StringX.charAt(getStringFromRS(card.paytype, rs), 0));
      AccountType act = AccountType.fromPayType(paytype);
      boolean expires = getBooleanFromRS(card.exp, rs);
      boolean enMod10ck = getBooleanFromRS(card.enMod10ck, rs);
      ret = new BinEntry(low, high, inst, act, expires, enMod10ck);
    } else {
      // +++ bitch
    }
    return ret;
  }

  private boolean vacuumFullCfgTables() {
    StopWatch sw = new StopWatch();
    service.PANIC("Vacuuming FULL the config tables, but NOT vacuuming the log tables ...");
    boolean ret = false;
    TextList msgs = new TextList();
    StopWatch tablesw = new StopWatch(false);
    for(int i = tables.length; i-- > 0; ) {
      TableProfile tp = tables[i];
      if(tp.type.is(TableType.cfg)) {
        tablesw.Start();
        boolean tempret = vacuum(tp, true, true, true);
        msgs.add("Vacuumed FULL " + tp.name() + " took " + DateX.millisToTime(tablesw.Stop()) + ".");
        ret &= tempret;
      } else {
        msgs.add("Skipped vacuuming FULL " + tp.name() + "; not cfg type.");
      }
    }
    service.PANIC("Completed vacuuming FULL the config tables only; took " + DateX.millisToTime(sw.Stop()) + ":", msgs);
    return ret;
  }

  private boolean vacuum(TableProfile tp, boolean verbose, boolean analyze, boolean full) {
    int i = update(QS.genVacuum(tp, verbose, analyze, full));
    dbg.ERROR("Vacuum of " + tp.name() + " " + printIf(verbose, "verbose ") + printIf(analyze, "analyze ") + printIf(full, "full ") + "returned " + i);
    return true;
  }

  private static final String printIf(boolean IF, String toPrint) {
    return IF ? toPrint : "";
  }

  public boolean vacuumAnalyzeDatabase(boolean verbose, boolean analyze, boolean full, boolean vacuum) {
    int i = update(QS.genVacuumAnalyzeDatabase(verbose, analyze, full, vacuum));
    dbg.ERROR("Vacuum of entire database " + printIf(verbose, "verbose ") + printIf(analyze, "analyze ") + printIf(full, "full ") + printIf(vacuum, "vacuum ") + "returned " + i);
    return true;
  }

  public StoreAuthid addStoreauth(Storeid storeid) {
    return createStoreAuth(new Authid(1), "not set", "UK", 0, "UK", storeid, new Authid(1), "not set");
  }

  // add a storeauth entry for every authid and settleid in storeauth
  // for other stores in this enterprise
  public void fillStoreauths(Storeid storeid) {
    Enterprise ent = StoreHome.Get(storeid).enterprise;
    Enterpriseid entid = ent.enterpriseid();
    Store[] stores = ent.stores.getAll();
    Storeid[] storeids = new Storeid[stores.length];
    for(int i = stores.length; i-- > 0; ) {
      storeids[i] = stores[i].storeId();
    }
    StoreAuthid[] storeauthids = (StoreAuthid[]) getUniqueIdExtentArrayFromQuery(QS.genAllStoreAuths(storeids), StoreAuthid.class);
    TextList used = new TextList();
    for(int i = storeauthids.length; i-- > 0; ) {
      EasyProperties ezp = getRecordProperties(storeauth, storeauthids[i]);
      String authidstr = ezp.getString(storeauth.authid.name());
      String settleidstr = ezp.getString(storeauth.settleid.name());
      String forUsed = authidstr + "." + settleidstr;
      if(used.contains(forUsed)) {
        // not needing to add it
      } else {
        // see if we need to add this combo
        Authid authid = new Authid(authidstr);
        String authmerchid = "not set";
        String institution = ezp.getString(storeauth.institution.name());
        int maxtxnlimit = ezp.getInt(storeauth.maxtxnlimit.name());
        String paytype = ezp.getString(storeauth.paytype.name());
        Authid settleid = new Authid(settleidstr);
        String settlemerchid = "not set";
        /* Storeauthid said = */createStoreAuth(authid, authmerchid, institution, maxtxnlimit, paytype, storeid, settleid, settlemerchid);
      }
    }
  }

  public Terminalid addTerminal(Applianceid applid, Storeid storeid) {
    return createTerminal("", null, applid, storeid, false);
  }

  public final StoreAccessid createStoreAccess(Storeid storeid, Associateid associateid, ClerkPrivileges sperms) {
    StoreAccessid id = getNextStoreAccessid();
    update(QS.genCreateStoreAccess(storeid, associateid, sperms, id));
    return id;
  }

  private final StoreAuthid createStoreAuth(Authid authid, String authmerchid, String institution, int maxtxnlimit, String paytype, Storeid storeid, Authid settleid, String settlemerchid) {
    StoreAuthid id = getNextStoreAuthid();
    update(QS.genCreateStoreAuth(authid, authmerchid, institution, maxtxnlimit, paytype, storeid, settleid, settlemerchid, id));
    return id;
  }

  private final Terminalid createTerminal(String modelcode, String terminalname, Applianceid applianceid, Storeid storeid, boolean dosigcap) {
    Terminalid id = getNextTerminalid();
    if(!StringX.NonTrivial(terminalname)) {
      terminalname = "Term" + id;
    }
    update(QS.genCreateTerminal(modelcode, terminalname, applianceid, dosigcap, storeid, id));
    return id;
  }

  public final void fillTermauths(Terminalid termid) {
    // add a termauth entry for every authid and settleid in storeauth entries for the store that the terminal belongs to
    // what is this terminal's store?
    Storeid storeid = getStoreForTerminal(termid);
    // get the list of all authids and settleids for that store
    Authid[] authids = getAuthidsForStore(storeid);
    // get a list of all authids for this terminal
    AuthorizerRow row = getAuthidsForTerminal(termid);
    // create any that aren't there
    TextList alreadyThere = new TextList();
    while(row.next()) {
      Authid prospect = row.authid();
      alreadyThere.assurePresent(prospect.toString());
    }
    for(int i = authids.length; i-- > 0; ) {
      Authid prospect = authids[i];
      if(alreadyThere.contains(prospect.toString())) {
        // skip it
      } else {
        createTermauth(prospect, termid, 1, "not set", 1);
      }
    }
  }

  private final TermAuthid createTermauth(Authid authid, Terminalid terminalid, int termbatchnum, String authtermid, int authseq) {
    TermAuthid id = getNextTermAuthid(); // generates id here for PG only
    update(QS.genCreateTermauth(authid, terminalid, termbatchnum, authtermid, authseq, id));
    return id;
  }

  private final TermAuthid getNextTermAuthid() {
    return new TermAuthid(getIntFromQuery(QS.genSelectNextVal(termauth.termauthid)));
  }

  private final Terminalid getNextTerminalid() {
    return new Terminalid(getIntFromQuery(QS.genSelectNextVal(terminal.terminalid)));
  }

  private final StoreAuthid getNextStoreAuthid() {
    return new StoreAuthid(getIntFromQuery(QS.genSelectNextVal(storeauth.storeauthid)));
  }

  private final StoreAccessid getNextStoreAccessid() {
    return new StoreAccessid(getIntFromQuery(QS.genSelectNextVal(storeaccess.storeaccessid)));
  }

  private final Txnid getNextTxnid() {
    return new Txnid(getIntFromQuery(QS.genSelectNextVal(txn.txnid)));
  }

  private final Cardid getNextCardid() {
    return new Cardid(getIntFromQuery(QS.genSelectNextVal(card.cardid)));
  }

  private final Drawerid getNextDrawerid() {
    return new Drawerid(getIntFromQuery(QS.genSelectNextVal(drawer.drawerid)));
  }

  private final Batchid getNextBatchid() {
    return new Batchid(getIntFromQuery(QS.genSelectNextVal(batch.batchid)));
  }

  private final AuthattemptId getNextAuthattemptId() {
    return new AuthattemptId(getIntFromQuery(QS.genSelectNextVal(authattempt.authattemptid)));
  }

  private final Servicecfgid getNextServicecfgid() {
    return new Servicecfgid(getIntFromQuery(QS.genSelectNextVal(servicecfg.servicecfgid)));
  }

  public EasyProperties getRecordProperties(TableProfile tp, UniqueId id, ColumnProfile ignoreColumn) {
    return colsToProperties(QS.genSelectRecord(tp, id), ignoreColumn);
  }

  public EasyProperties getRecordProperties(TableProfile tp, UniqueId id) {
    return getRecordProperties(tp, id, null /*tp.primaryKey.field*/);
  }

  public int setRecordProperties(TableProfile tp, UniqueId id, EasyProperties after) {
    // check to see which ones need to be rewritten before blindly writing
    EasyProperties before = getRecordProperties(tp, id, null);
    return setRecordProperties(before, tp, id, after, null);
  }

  public int setRecordProperties(EasyProperties before, TableProfile tp, UniqueId id, EasyProperties after, TextList messages) {
    ColumnProfile[] columns = tp.columns();
    TextList afternames = after.allKeys(); // to check for missing falses (thanks to HTML)
    EasyProperties changes = new EasyProperties();
    ColumnProfile cp = null;
    String name = null;
    TextList changesToReport = new TextList();
    for(int i = columns.length; i-- > 0; ) {
      cp = columns[i];
      name = cp.name();
      if(cp == null) {
        String report = "Skipping " + name + "; cp==null! VERY BAD!";
        dbg.ERROR(report);
        changesToReport.add(report);
      } else if(cp.equals(tp.primaryKey.field)) {
        String report = "Skipping " + name + "; PK.";
        dbg.VERBOSE(report);
        changesToReport.add(report);
      } else {
        // +++ eventually handle all datatypes independently, not just boolean
        boolean isbool = cp.numericType().is(DBTypesFiltered.BOOL);
        if(isbool) { // boolean has special issues to handle
          if(!afternames.contains(name)) {
            after.setBoolean(name, false); // this due to html's way of handling checkboxes!
          }
          boolean oldValue = before.getBoolean(name);
          boolean newValue = after.getBoolean(name);
          if(oldValue == newValue) {
            dbg.VERBOSE("Skipping " + name + ", no change.");
          } else {
            changes.setBoolean(name, newValue);
            dbg.WARNING("Adding " + name + ": b4[" + oldValue + "]!=@r[" + newValue + "].");
            changesToReport.add("Converting " + name + " from [" + oldValue + "] to [" + newValue + "].");
          }
        } else {
          String oldValue = before.getString(name);
          String newValue = StringX.removeCRs(after.getString(name)); // remove CR's due to web crap
          if(StringX.equalStrings(oldValue, newValue)) {
            dbg.VERBOSE("Skipping " + name + ", no change.");
          } else {
            changes.setString(name, newValue); // and it might have changed due to CR removal
            dbg.WARNING("Adding " + name + ": b4[" + oldValue + "]!=@r[" + newValue + "].");
            changesToReport.add("Converting " + name + " from [" + oldValue + "] to [" + newValue + "].");
          }
        }
      }
    }
    int ret;
    String msg = "";
    if(changes.size() == 0) {
      msg = "No changes for " + tp.name() + "[" + id + "].";
      ret = 0;
    } else {
      ret = update(QS.genUpdateRecord(tp, id, changes));
      if(ret != 1) {
        ret = FAILED;
      }
      msg = "Changing " + tp.name() + "[" + id + "]=" + ret + " : " + changesToReport.toString(); //changes.asParagraph(",");
      // --- this is a hack to get stores and enterprises, etc., to reload themselves if they have been changed
      // --- so that the storecron stuff works correctly, etc. ...
      if(ret == 1) {
        try {
          PayMateTableEnum pte = new PayMateTableEnum(tp.name());
          if(PayMateTableEnum.IsLegal(pte)) {
            EntityBase eb = null;
            EasyProperties ezp = null;
            switch(pte.Value()) {
              case PayMateTableEnum.enterprise: {
                eb = EnterpriseHome.Get(new Enterpriseid(id.value()));
              }
              break; case PayMateTableEnum.store: {
                eb = StoreHome.Get(new Storeid(id.value()));
              }
              break; case PayMateTableEnum.appliance: {
                eb = ApplianceHome.Get(new Applianceid(id.value()));
              }
              break; case PayMateTableEnum.associate: {
                eb = AssociateHome.Get(new Associateid(id.value()));
              }
              break;
            }
            if(eb != null) {
              ezp = getRecordProperties(tp, id);
              if(ezp != null) {
                eb.setProps(ezp);
                eb.loadFromProps();
              } else {
                // +++ ???
              }
            }
          }
        } catch(Exception ex) {
          dbg.Caught(ex);
        }
      }
    }
    // don't scream about ANY statistics changes, PLEASE
    if(messages == null) {
      if(!tp.isLogType()) {
        service.PANIC(msg);
      } else {
        dbg.WARNING(msg);
      }
    } else {
      messages.add(msg);
    }
    return ret;
  }

  public EasyProperties getTableStats(TableProfile tbl) {
    return colsToProperties(QS.genTableStats(tbl), null);
  }

  public EasyProperties getTablePages(TableProfile tbl) {
    return colsToProperties(QS.genTablePages(tbl), null);
  }

  public boolean getStatsRowLevelEnabled() {
    return StringX.equalStrings("on", getStringFromQuery(QS.genStatsRowLevel()), true
                                /*ignorecase*/);
  }

  public int getDatabaseAge() { // +++ get this database name from somewhere!
    return getIntFromQuery(QS.genDatabaseAge("mainsail"));
  }

  /////////////////////////////////////
  // the new SS2 data stuff ...

  public boolean loadEntity(EntityBase entity) {
    // switch on the type here, and run the appropriate function on db,
    SinetClass sclass = entity.getSinetClass();
    TableProfile tp = tableProfileFromName(sclass.Image());
    // +++ make the TableProfile classes use the SinetClass enumeration for their names?
    // +++ since the database is just supposed to be a cacheof those classes anyway?
    return entity.setProps(getRecordProperties(tp, entity.id(), tp.primaryKey.field));
  }

  public boolean storeEntity(EntityBase entity) {
    // switch on the type here, and run the appropriate function on db,
    SinetClass sclass = entity.getSinetClass();
    TableProfile tp = tableProfileFromName(sclass.Image());
    // +++ make the TableProfile classes use the SinetClass enumeration for their names?
    // +++ since the database is just supposed to be a cacheof those classes anyway?
    /*int something = */setRecordProperties(tp, entity.id(), entity.getProps());
    return true;
  }

  // +++ add a notes field to all tables.  Pass a string in that allows us to put an entry into that notes field.
  public UniqueId New(SinetClass sclass, UniqueId parentid) {
    if(SinetClass.IsLegal(sclass)) {
      TableProfile tp = SINETTABLES[sclass.Value()]; // maybe use the function that checks?
      if(tp != null) {
        ColumnProfile pk = tp.primaryKey.field;
        if(pk != null) {
          UniqueId id = new UniqueId(getIntFromQuery(QS.genSelectNextVal(pk)));
          if(UniqueId.isValid(id)) {
            QueryString qs = null;
            // +++ @SS2 which items are NOT NULL and need to be set?
            // check to see which fields are set to NOTNULL
            // if that field is a PK, ignore it, as it will be set anyway
            // if that field is NOT a PK, either take a passed-in value for it (Object.toString()?),
            // or fabricate it based on its type
            // this may or may not work!
            switch(sclass.Value()) {
              case SinetClass.Enterprise: // note that in this case parent is not really a parent
              default: {
                EasyProperties ezp = new EasyProperties();
                ezp.setString(enterprise.enterprisename.name(), Now()); // give it an interesting name
                qs = QueryString.Insert(tp, ezp, id); // put this into generic place in PMDBQS
              }
              break; case SinetClass.Appliance: {
                // applname field can't be null!
                qs = QS.genCreateAppliance("New Appliance " + UTC.Now().toString(), new Storeid(parentid.value()), new Applianceid(id.value()));
              }
// no persistence of appliance log data 20040523 !
//              break; case SinetClass.ApplNetStatus: {
//                qs = QS.genCreateApplNetStatus(new Applianceid(parentid.value()), id);
//              }
//              break; case SinetClass.ApplPgmStatus: {
//                qs = QS.genCreateApplPgmStatus(new Applianceid(parentid.value()), id);
//              }
              break; case SinetClass.Associate: {
                String loginname = "assoc" + id;
                String password = String.valueOf(loginname.hashCode());
                qs = QS.genCreateAssociate(parentid, loginname, password, id);
              }
              break; case SinetClass.Store: {
                qs = QS.genCreateStore(new Enterpriseid(parentid.value()), "New Store " + id, id);
              }
              break;
            }
            if(qs != null) {
              int inserted = update(qs);
              // if(inserted == 1) { yadayada  +++ check return value?
            } else {
              service.PANIC("New(): No QS constructor for " + sclass.Image());
            }
            return id;
          } else {
            service.PANIC("New(): UniqueId is not valid (could not get next id for table " + tp + ") !");
          }
        } else {
          service.PANIC("New(): Primary Key not found for TableProfile " + tp + " !");
        }
      } else {
        service.PANIC("New(): TableProfile not found for SinetClass " + sclass + " !");
      }
    } else {
      service.PANIC("New(): SinetClass is illegal !");
    }
    return null;
  }

  public TableProfile tableProfileFromName(String tablename) {
    for(int i = tables.length; i-- > 0; ) {
      TableProfile tp = tables[i];
      if(StringX.equalStrings(tp.name(), tablename, true /*ignoreCase*/)) {
        return tp;
      }
    }
    return null;
  }

  private TableProfile tableForSinetClass(SinetClass sclass) {
    TableProfile ret = null;
    try {
      boolean isLegal = SinetClass.IsLegal(sclass);
      dbg.ERROR("SinetClass[" + sclass + "] is " + (isLegal ? "" : "NOT ") + "legal.");
      if(isLegal) {
        int i = sclass.Value();
        boolean inRange = (i < SINETTABLES.length);
        dbg.ERROR("sclass.Value() of " + i + " is " + (inRange ? "" : "NOT ") + "in the range of SINETTABLES[" + SINETTABLES.length + "].");
        if(inRange) {
          ret = SINETTABLES[sclass.Value()];
          dbg.ERROR("SINETTABLES[" + sclass.Value() + "]=" + ret);
        }
      }
    } catch(Exception ex) {
      dbg.Caught(ex);
    } finally {
      return ret;
    }
  }

  // this gets the id's in no particular order.  Tuff.
  // ignore that.  now it gets them in descending numerical order
  public UniqueId[] getIds(SinetClass sclass, Class uniqueidExtentClass) {
    UniqueId[] ids = null;
    TableProfile tp = tableForSinetClass(sclass);
    if(tp == null) {
      service.PANIC("getIds() does not yet have code to handle SinetClass " + sclass + " !");
      return(UniqueId[]) java.lang.reflect.Array.newInstance(uniqueidExtentClass, 0);
    } else {
      ids = getUniqueIdExtentArrayFromQuery(QS.genid(tp).orderbydesc(tp.primaryKey.field), uniqueidExtentClass);
    }
    return ids;
  }

  ////////////////////////////////
  // HardwareCfgMgr stuff

  // +++ this func should return arrays so that the callers can decide what to do if it isn't unique
  public Applianceid getApplianceByName(String applname) {
    return new Applianceid(getIntFromQuery(QS.genApplianceId(applname)));
  }

// no persistence of appliance log data 20040523 !
//  public ApplNetStatusid getLastApplNetStatus(Applianceid parent) {
//    return new ApplNetStatusid(getIntFromQuery(QS.genLastApplNetStatus(parent)));
//  }
//
//  public ApplPgmStatusid getLastApplPgmStatus(Applianceid parent, boolean connection) {
//    return new ApplPgmStatusid(getIntFromQuery(QS.genLastApplPgmStatus(parent, connection)));
//  }
}

class TableRowCleaner {
  TableProfile table;
  int maxid;
  Method m;
  boolean hasFunction;
  boolean hasDefaults;
  ColumnProfile pk;
  EasyProperties newDefaults;
}

class TableRowCleanerVector extends Vector {
  public TableRowCleaner itemAt(int index) {
    return(TableRowCleaner) elementAt(index);
  }
}
//$Id: PayMateDB.java,v 1.389 2004/05/23 19:02:47 mattm Exp $