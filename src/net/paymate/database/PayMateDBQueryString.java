package net.paymate.database;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/database/PayMateDBQueryString.java,v $</p>
 * <p>Description: A static class that turns QueryString parts into SQL strings</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.102 $
 */

/* RULES:
 * 1: If a function returns only a subquery (not a full query), make it private
 * 2: If a function returns only a subquery (not a full query), do not start it with "gen"
 * 3: If a function returns a full query, start it with "gen"
 * 4: If a function will be used by another class, make it public (duh).
 *
 * If you don't follow the above rules, the query tester will not work for you.
 */

import net.paymate.connection.*;
import net.paymate.database.ours.Database;
import net.paymate.database.ours.query.*;
import net.paymate.data.*;
import net.paymate.util.*;
import java.util.TimeZone;
import net.paymate.jpos.data.*;
import java.lang.reflect.*;
import net.paymate.net.IPSpec;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;
import net.paymate.database.ours.table.PayMateTableEnum;

public class PayMateDBQueryString implements Database {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(
      PayMateDBQueryString.class, ErrorLogStream.VERBOSE);

  private static TableProfile [ ] TABLES = null;
  // the first time called,
  // uses reflection to get a list of the tables in the Database interface.
  public static final synchronized TableProfile [ ] getTableArray() {
    if(TABLES == null) {
      PayMateTableEnum enumer = new PayMateTableEnum();
      TableProfile [ ] TBLS = new TableProfile[enumer.numValues()];
      java.lang.Class c = Database.class;
      Field [ ] fields = c.getDeclaredFields();
      for(int tablei = TBLS.length; tablei-->0;) {
        enumer.setto(tablei);
        String name = enumer.Image(); // the tablename
        // use reflection to get the object for the tablename
        // the type is TableProfile
        Field inQuestion = null;
        for(int fieldi = fields.length; fieldi-->0;) {
          Field f = fields[fieldi];
          int modifiers = f.getModifiers();
          Class fieldClass = f.getType();
          if(StringX.equalStrings(f.getName(), name, true /* ignorecase */) &&
             TableProfile.class.isAssignableFrom(fieldClass) &&
             Modifier.isPublic(modifiers) &&
             Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
            // this is the one
            inQuestion = f;
            break;
          } else {
//            dbg.VERBOSE("Did not match: name=" + name + "f.getName()=" + f.getName());
          }
        }
        if(inQuestion == null) {
          dbg.ERROR("Cannot find TableProfile object in Database for PayMateTableEnum value " + enumer);
        } else {
          try {
            Object o = inQuestion.get(PayMateDBQueryString.class);
            if (o == null || ! (o instanceof TableProfile)) {
              dbg.ERROR("Not able to cast o(" + o + ") to TableProfile!");
            } else {
              TableProfile tp = (TableProfile) o;
              TBLS[tablei] = tp;
            }
          } catch (Exception e) {
            dbg.Caught(e);
          }
          if(TBLS[tablei] == null) {
            dbg.ERROR("Not able to assign a Database.TableProfile to PayMateTableEnum "+enumer+"!");
          }
        }
      }
      TABLES = TBLS;
    }
    return TABLES;
  }

  /**
   * @param dbvalue string value of time from databse transstartime field or equivalent
   */
  public static final UTC tranUTC(String dbvalue) {
    return UTC.New(dbvalue);
  }

  /**
   * convert @param utcTix into string for transtarttime field
   */

  public static final String forTrantime(long utcTix) {
    return forTrantime(UTC.New(utcTix));
  }

  /*package*/ static final int TIMELENGTH = 14; // all times in our system are set to 14 characters [the first 14 of LocalTimeFormat.DESCENDINGTIMEFORMAT]
  public static final String forTrantime(UTC utc) {
    return utc != null ? utc.toString(TIMELENGTH) : "";
  }

  public static final String Now() {
    return forTrantime(UTC.Now());
  }

  /**
   * default format for time displays at stores.
   * (PLEASE make it be a fixed legnth regardless of time/date!)
   */

  /**
   * Eventually do these things in a more "standardized" way
   */

  private static final TransferType TTEMPTY = new TransferType();

  static final QueryString genDropField(ColumnProfile column) {
    return QueryString.AlterTable(column.table()).dropColumn(column);
  }

  static final QueryString genDropIndex(String indexname) {
    return QueryString.DropIndex(new IndexProfile(indexname, null, new ColumnProfile[0]));
  }

  static final QueryString genDropTable(String tablename) {
    return QueryString.DropTable(TableProfile.create(new TableInfo(tablename), null, null));
  }

  static final QueryString genAddField(ColumnProfile column) {
    return QueryString.AddColumn(column);
  }

  static final QueryString genCreateIndex(IndexProfile index) {
    return QueryString.CreateIndex(index);
  }

  static final QueryString genAddPrimaryKeyConstraint(PrimaryKeyProfile primaryKey) {
    return QueryString.addKeyConstraint(primaryKey);
  }

  static final QueryString genAddForeignKeyConstraint(ForeignKeyProfile foreignKey) {
    return QueryString.addKeyConstraint(foreignKey);
  }

  static final QueryString genDropConstraint(TableProfile table, Constraint constr) {
    return QueryString.DropConstraint(table, constr);
  }

  static final QueryString genRenameColumn(String table, String oldname, String newname) {
    return QueryString.RenameColumn(table, oldname, newname);
  }

  static final QueryString genCreateTable(TableProfile tp) {
    return QueryString.generateTableCreate(tp);
  }

  static final QueryString genVacuumDatabase(boolean verbose, boolean analyze, boolean full) {
    return genVacuumAnalyzeDatabase(verbose, analyze, full, true);
  }

  static final QueryString genVacuumAnalyzeDatabase(boolean verbose, boolean analyze, boolean full, boolean vacuum) {
    return QueryString.VacuumAnalyze(null, verbose, analyze, full, vacuum);
  }

  static final QueryString genVacuum(TableProfile tp, boolean verbose, boolean analyze, boolean full) {
    return QueryString.Vacuum(tp, verbose, analyze, full);
  }

  static final QueryString genStatsRowLevel() {
    return QueryString.ShowParameter("stats_row_level"); // +++ pg parameters like this need to all go somewhere.  QueryString?
  }

  static final QueryString genTableStats(TableProfile table) {
    return QueryString.TableStats(table);
  }

  static final QueryString genTablePages(TableProfile table) {
    return QueryString.TablePages(table);
  }

  static final QueryString genDatabaseAge(String databasename) {
    return QueryString.DatabaseAge(databasename);
  }

  static final QueryString genCreateTxn(EasyProperties ezp, UniqueId id) {
    return QueryString.Insert(txn, ezp, id);
  }

  static final QueryString genCreateCard(EasyProperties ezp, UniqueId id) {
    return QueryString.Insert(card, ezp, id);
  }

  // Note that you have to get a serial field's next value to use to insert the record
  static final QueryString genSelectNextVal(ColumnProfile cp) {
    return QueryString.SelectNextVal(cp);
  }

  static final QueryString genMaxId(ColumnProfile pk) {
    return genid(pk.table()).orderbydesc(pk).limit(1);
  }

  static final QueryString genChangeFieldNullable(ColumnProfile field) {
    QueryString qs = QueryString.AlterTable(field.table()).AlterColumn(field);
    if(field.nullable()) {
      qs.DropNotNull();
    } else {
      qs.SetNotNull();
    }
    return qs;
  }

  static final QueryString genChangeFieldDefault(ColumnProfile field) {
    QueryString qs = QueryString.AlterTable(field.table()).AlterColumn(field);
    if(StringX.NonTrivial(field.columnDef)) {
      qs.SetDefault(field.dbReadyColumnDef()); // dbReadyColumnDef() quotes things that need to be quoted
    } else {
      qs.DropDefault();
    }
    return qs;
  }

  static final QueryString genSelectRecord(TableProfile tp, UniqueId id) {
    return QueryString.SelectAllFrom(tp).where().nvPair(tp.primaryKey.field, id);
  }

  static final QueryString genUpdateRecord(TableProfile tp, UniqueId id, EasyProperties ezp) {
    return QueryString.Update(tp, ezp).where().nvPair(tp.primaryKey.field, id);
  }

  public static final boolean isReadOnly(String query) {
    return QueryString.isReadOnly(query);
  }

  // +++ extend EasyProperties to include functions for converting datatypes ???
  // --- are they already in EasyProperties?  Check later.  These functions also handle "null" for the database, which is NOT standard for real-world objects!
  private static final void setProperty(EasyProperties ezp, ColumnProfile cp, String value) {
    ezp.setString(cp.name(), StringX.TrivialDefault(value, ""));
  }
  private static final void setProperty(EasyProperties ezp, ColumnProfile cp, boolean value) {
    setProperty(ezp, cp, value ? Bool.TRUE() : Bool.FALSE());
  }
  private static final void setProperty(EasyProperties ezp, ColumnProfile cp, UniqueId id) {
    setProperty(ezp, cp, UniqueId.isValid(id) ? String.valueOf(id) : ""); // an invalid id returns a value of -1, which can't be used in the database.  NULL is the DB equivalent.  We convert ""  to null later in this code.
  }
  private static final void setProperty(EasyProperties ezp, ColumnProfile cp, int id) {
    setProperty(ezp, cp, String.valueOf(id));
  }
  private static final void setProperty(EasyProperties ezp, ColumnProfile cp, long id) {
    setProperty(ezp, cp, String.valueOf(id));
  }

//  static final QueryString genDefaultsInsert(TableProfile tp, UniqueId primaryKey) {
//    EasyProperties ezp = new EasyProperties();
//    for(int i = tp.numColumns(); i-->0;) {
//      ColumnProfile cp = tp.column(i);
//      if(cp.equals(tp.primaryKey.field)) {
//        // do NOT have to set the primary key field since it will be done by QueryString.Insert()
//        // skip
//      } else {
//        setProperty(ezp, cp, cp.dbReadyColumnDef());
//      }
//    }
//    return QueryString.Insert(tp, ezp, primaryKey, true /*valuesFromDefaults*/);
//  }

  public static final QueryString whereNot(ColumnProfile column) {
    return QueryString.Clause().where().isFalse(column);
  }
  public static final QueryString whereIsTrue(ColumnProfile column) {
    return QueryString.Clause().where().isTrue(column);
  }

  static final QueryString genApplianceId(String applname) {
    return genid(appliance).where().nvPair(appliance.applname, applname);
  }

  /**
   * All txns that are in the system for this terminal will go into its drawer.
   * No exceptions.  Even pending ones.
   * The terminal shouldn't be able to do a drawer report if there are txns pending standin.
   * However, so long as they have been stoodin, it shouldn't matter if they are authed,
   * although their status might change between standing in and authing.
   * Should we prevent drawer closes so long as txns are pending auth?
   * no.  once it hits the database, it is in the drawer.
   *
   * This query will go away once we have a current drawer instead of no drawer concept.
   */
  static final QueryString genGetDrawerableTxns(Terminalid Tid) {
    // standins go into the drawer, too
    return genid(txn).
        where().nvPair(txn.terminalid, Tid).and().isEmpty(txn.drawerid);
  }

  static final QueryString genStoreAccessid(UniqueId associd, UniqueId storeid) {
    return genid(storeaccess).
        where().nvPair(storeaccess.associateid, associd).
        and().nvPair(storeaccess.storeid, storeid);
  }

  // fragment that can be used for all genid()s
  public static final QueryString genid(TableProfile tp) {
    return QueryString.Select(tp.primaryKey.field).from(tp);
  }
  private static final QueryString idDistinct(TableProfile tp) {
    return QueryString.SelectDistinct(tp.primaryKey.field).from(tp);
  }

  // leave institution null if you don't want to use it in the search
  static final QueryString genStoreAuthInfo(UniqueId storeid,
                                            String paytype,
                                            String institution) {
    QueryString qs = genid(storeauth).
        where().nvPair(storeauth.storeid, storeid).
        and().nvPair(storeauth.paytype, paytype);
    if(StringX.NonTrivial(institution)) {//.@DEBIT
      qs.and().nvPair(storeauth.institution, institution);
    }
    return qs;
  }

  static final QueryString genTermAuthIds(Terminalid termid) {
    return genid(termauth).
        where().nvPair(termauth.terminalid, termid);
  }

  static final QueryString genMerchantIds(Authid authid, UniqueId storeid) {
    return genid(storeauth).
        where().nvPair(storeauth.authid, authid).
        and().nvPair(storeauth.storeid, storeid);
  }

  static final QueryString genAllStoreAuths(UniqueId [ ] storeids) {
    TextList tl = new TextList(storeids.length);
    for(int i = storeids.length; i-->0;) {
      tl.add(storeids[i].toString());
    }
    return idDistinct(storeauth).where().inUnquoted(storeauth.storeid, tl);
  }

  /**
   * @return query for appliance's terminals info
   */
  static final QueryString genTerminalsforAppliance(UniqueId applianceid) {
    return genid(terminal).where().nvPair(terminal.applianceid, applianceid);
  }

  static final QueryString genAssociateId(String clerkname, String password, UniqueId enterpriseid) {
    return genid(associate).
        where().nvPair(associate.loginname, clerkname).
        and().nvPair(associate.enterpriseid, enterpriseid).
        and().nvPair(associate.encodedpw, password);
  }

  static final QueryString genEnterpriseIdsbyEnabledName() {
    return genid(enterprise).
        orderbydesc(enterprise.enabled).
        commaAsc(enterprise.enterprisename).
        commaAsc(enterprise.enterpriseid);
  }

//  static final QueryString genStoreids() {
//    return genid(store);
//  }

  static final QueryString genStoreidsByAscTxncount() {
    return QueryString.Select(store.storeid).comma().count(txn.txnid).
        from(store).comma(txn).comma(terminal).
        where().matching(store.storeid, terminal.storeid).
        and().matching(terminal.terminalid, txn.terminalid).
        groupby(store.storeid).
        orderbyasc(2);
  }

  static final QueryString genFindVoidForOriginal(Txnid origtxnid) {
    return genid(txn).
        where().nvPair(txn.transfertype, String.valueOf(TTEMPTY.CharFor(TransferType.Reversal))).
        and().nvPair(txn.origtxnid, origtxnid);
  }

  /**
   * @return queryt to get txnid from standin generated reference info
   */
  static final QueryString genFindTransactionBy(Terminalid termid,
                                                UTC clientreftime) {
    return genTransactionFromQuery(termid, forTrantime(clientreftime));
  }

  static final QueryString genAuthIdFromName(String authname) {
    return genid(authorizer).where().nvPair(authorizer.authname, authname);
  }

  private static final QueryString whereServiceParam(String serviceName,
      String paramname) {
    return QueryString.Clause().nvPair(servicecfg.servicename, serviceName).and().
        nvPair(servicecfg.paramname, paramname);
  }

  static final QueryString genCreateServiceParam(String serviceName,
                                                 String paramname,
                                                 String paramvalue,
                                                 UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, servicecfg.servicename, serviceName);
    setProperty(ezp, servicecfg.paramname, paramname);
    setProperty(ezp, servicecfg.paramvalue, paramvalue);
    return QueryString.Insert(servicecfg, ezp, id);
  }

  static final QueryString genStampAuthAttemptTxnidOnly(AuthattemptId aaid,
      Txnid txnid) {
    if (AuthattemptId.isValid(aaid)) {
      EasyProperties ezp = new EasyProperties();
      setProperty(ezp, authattempt.txnid, txnid);
      return genUpdateRecord(authattempt, aaid, ezp);
    }
    return null;
  }

  /**
   * This search algorithm is dependent on the fact that all overlaps are strict subsets,
   * and the smaller guys is always the one you wanta whole in one range, and
   * the smaller guy has the highest lowbin.
   * In other words, DON'T MESS THIS UP.
   */
  static final QueryString genPaymentTypeFromCardNo(String cardnumber) {
    int firstsix = StringX.parseInt(StringX.subString(cardnumber, 0, 6));
    return genBinEntry(firstsix);
  }

  // these need to be broken into getids, then get records ...
  static final QueryString genAuths() {
    return QueryString.SelectAllFrom(authorizer).
        where().not().isEmpty(authorizer.authclass);
  }
  static final QueryString genServiceParamNames(String serviceName) {
    return QueryString.
        Select(servicecfg.paramname).from(servicecfg).
        where().nvPair(servicecfg.servicename, serviceName).
        orderbyasc(servicecfg.paramname);
  }
  static final QueryString genServiceParam(String serviceName, String paramname) {
    return QueryString.Select(servicecfg.paramvalue).
        from(servicecfg).
        where().cat(whereServiceParam(serviceName, paramname));
  }
  static final QueryString genUpdateServiceParam(String serviceName,
                                                 String paramname,
                                                 String paramvalue) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, servicecfg.paramvalue, paramvalue);
    return QueryString.Update(servicecfg, ezp).
        where().cat(whereServiceParam(serviceName, paramname));
  }
  static final QueryString genStoresQuery(UniqueId enterpriseID) {
    return genid(store).
        where().nvPair(store.enterpriseid, enterpriseID).
        orderbyasc(store.storename);
  }

  static final QueryString genAuthIdsForTerminal(Terminalid terminalid) {
    return QueryString.
        SelectDistinct(termauth.authid).from(termauth).
        where().nvPair(termauth.terminalid, terminalid);
  }

  // should this actually check the storeauth and not the termauth? +++
  static final QueryString genDefaultAuthidForTerminal(Terminalid terminalid) {
    return QueryString.
        Select(termauth.authid).
        from(termauth).
        where().nvPair(termauth.terminalid, terminalid);
  }

  static final QueryString genStampAuthAttemptDone(AuthattemptId id,
      Txnid txnid, EasyUrlString authresponse) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, authattempt.authresponse, authresponse.encodedValue());
    setProperty(ezp, authattempt.authendtime, Now());
    QueryString qs = QueryString.Update(authattempt, ezp).where();
    if (AuthattemptId.isValid(id)) {
      return qs.nvPair(authattempt.authattemptid, id);
    } else if (txnid.isValid(txnid)) {
      return qs.nvPair(authattempt.txnid, txnid);
    } else {
      return null;
    }
  }

  static final QueryString genStampVoidTxn(Txnid txnid, String voidedflag) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, txn.voided, voidedflag);
    return genUpdateRecord(txn, txnid, ezp);
  }

  /**
   * txnid's for all open standins for this authorizer
   * plus txns that were not stoodin but were also not completed (unauthed txns that need auth)
   */
  static final QueryString genToAuth(Authid authid) {
    QueryString qs = genid(txn).
        where().cat(needsAuth()). // this used to be openStandins(), but we need to auth txns that weren't stoodin, too!
        and().nvPair(txn.authid, authid).
        orderbyasc(txn.txnid); // try to get them in the order they arrived.
    return qs;
  }

  static final QueryString genBinEntry(int firstsix) {
    return QueryString.SelectAllFrom(card).
        where().nLTEQv(card.lowbin, firstsix). //+_+ make a new type of range
        and().nGTEQv(card.highbin, firstsix).
        orderbydesc(card.lowbin);
  }

  static final QueryString genGetSequence(Authid authid, Terminalid terminalid) {
    return QueryString.Select(termauth.authseq).
        from(termauth).
        where().nvPair(termauth.terminalid, terminalid).
        and().nvPair(termauth.authid, authid);
  }

  static final QueryString genSetSequence(Authid authid, Terminalid terminalid,
                                          int number) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, termauth.authseq, number);
    return QueryString.Update(termauth, ezp).
        where().nvPair(termauth.terminalid, terminalid).
        and().nvPair(termauth.authid, authid);
  }

  static final QueryString genSetTxnSequence(Txnid txnid, int number) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, txn.authseq, number);
    return genUpdateRecord(txn, txnid, ezp);
  }

  static final QueryString genAssociateIdByLogin(String loginname,
                                                 UniqueId enterid) {
    return genid(associate).
        where().nvPair(associate.enterpriseid, enterid).
        and().nvPair(associate.loginname, loginname);
  }

  static final QueryString genPendingTermAuths(UniqueId storeid) {
    return QueryString.
        Select(authorizer.authname).
        comma(terminal.terminalid).
        comma(authorizer.authid).
        comma(terminal.terminalname).
        comma(termauth.termauthid).
        comma(termauth.authtermid).
        from(terminal).
        comma(termauth).
        comma(authorizer).
        where().
        nvPair(terminal.storeid, storeid).
        and().matching(terminal.terminalid, termauth.terminalid).
        and().matching(termauth.authid, authorizer.authid).
        orderbyasc(terminal.terminalname);
  }

  static final QueryString genStoreDrawersQuery(UniqueId storeid, TimeRange tr) {
    return drawerBaseQuery().
        and().nvPair(terminal.storeid, storeid).
        and().nGTEQv(drawer.transtarttime, forTrantime(tr.start().getTime())).
        and().nLTv(drawer.transtarttime, forTrantime(tr.end().getTime())).
        orderbydesc(drawer.transtarttime);
  }

  static final QueryString genBookmarkQuery(Drawerid drawerid) {
    return drawerBaseQuery().and().nvPair(drawer.drawerid, drawerid).
        orderbydesc(drawer.transtarttime);
  }

  static final QueryString genSetDrawerid(Txnid txnid, Drawerid drawerid) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, txn.drawerid, drawerid);
    return genUpdateRecord(txn, txnid, ezp);
  }

  static final QueryString genTerminalidFromDrawerid(Drawerid drawerid) {
    return QueryString.Select(drawer.terminalid).from(drawer).where().nvPair(
        drawer.drawerid, drawerid);
  }

  static final QueryString genTxnidsForBatchid(Batchid batchid) {
    return genid(txn).where().nvPair(txn.batchid, batchid);
  }

  static final QueryString genSetTxnBatchid(Txnid txnid, Batchid newBatchid) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, txn.batchid, newBatchid);
    return genUpdateRecord(txn, txnid, ezp);
  }

  static final QueryString genSetBatchTotals(Batchid batchid, int count,
                                             int amount) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, batch.txncount, count);
    setProperty(ezp, batch.txntotal, amount);
    return genUpdateRecord(batch, batchid, ezp);
  }

  // per store
  private static final QueryString drawerBaseQuery() {
    return QueryString.Select().all(drawer).
        comma(terminal.terminalname).
        comma(terminal.storeid).
        from(drawer).comma(terminal).
        where().matching(terminal.terminalid, drawer.terminalid);
  }

  static final QueryString genBatchQuery(Batchid batchid) {
    return batchBaseQuery().and().nvPair(batch.batchid,
                                         batchid).orderbydesc(batch.batchtime);
  }

  private static final QueryString batchBaseQuery() {
    return QueryString.Select().all(batch).
        comma(terminal.terminalname).
        comma(terminal.terminalid).
        comma(batch.txncount).
        comma(batch.txntotal).
        comma(authorizer.authname).
        comma(authorizer.authid).
        from(batch).comma(terminal).comma(termauth).comma(authorizer).
        where().matching(batch.termauthid, termauth.termauthid).
        and().matching(termauth.terminalid, terminal.terminalid).
        and().matching(authorizer.authid, termauth.authid);
  }

  static final QueryString genFullBatchQuery(Batchid batchid) {
    QueryString moreWheres = Batchid.isValid(batchid) ?
    QueryString.Clause().nvPair(txn.batchid, batchid) :
    QueryString.Clause().isEmpty(txn.batchid);
    return QueryString.
        SelectAll().
        cat(genWhichTxnSubqueryInner( (Terminalid)null, true, moreWheres)).
        orderbydesc(txn.txnid); //}
  }

  static final QueryString genTermAuth(Terminalid terminalid, Authid authid) {
    return QueryString.SelectAllFrom(termauth).
        where().nvPair(termauth.terminalid, terminalid).
        and().nvPair(termauth.authid, authid);
  }

  static final QueryString genGetBatch(Batchid batchid) {
    return QueryString.
        SelectAllFrom(txn).
        where().nvPair(txn.batchid, batchid).
        orderbyasc(txn.txnid); // ASC required by NPC
  }

  static final QueryString genUpdateBatchseq(Batchid batchid, int batchseq) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, batch.batchseq, batchseq);
    return genUpdateRecord(batch, batchid, ezp);
  }

  static final QueryString genGetBatchableTxns(Terminalid terminalid, Authid authid) {
    return genid(txn).
        where().nvPair(txn.terminalid, terminalid).
        and().
        Open().nvPair(txn.settleid, authid).or().
        Open().isEmpty(txn.settleid).and().nvPair(txn.authid, authid). // this is possibly a hack as this may never be a problem
        Close().
        Close().
        and().not().isEmpty(txn.authendtime). // this line is a bug fix for batches getting unauthed txns!
        and().isEmpty(txn.batchid);
  }

  static final QueryString genDeleteBatch(Batchid batchid) {
    return QueryString.DeleteFrom(batch).where().nvPair(batch.batchid, batchid); // +++ CREATE a "batchis" macro
  }

  /**
   * Create a new batch record based on what we know right now:  termauthid, batchtime, termbatchnum
   */
  static final QueryString genCreateBatch(TermAuthid termauthid, String batchtime,
                                       int termbatchnumber, UniqueId id, boolean auto) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, batch.termauthid, termauthid);
    setProperty(ezp, batch.batchtime, batchtime);
    setProperty(ezp, batch.termbatchnum, termbatchnumber);
    setProperty(ezp, batch.auto, auto);
    return QueryString.Insert(batch, ezp, id);
  }

  static final QueryString genUpdateTermauthInfo(TermAuthid termauthid,
                                                 int termbatchnum, int authseq) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, termauth.termbatchnum, termbatchnum);
    setProperty(ezp, termauth.authseq, authseq);
    return genUpdateRecord(termauth, termauthid, ezp);
  }

  static final QueryString genUpdateBatchStatus(Batchid batchid,
                                                String actioncode,
                                                String authrespmsg) {
    // !!! the authrespmsg is unfiltered and can have crap in it:
    if (StringX.NonTrivial(authrespmsg)) {
      authrespmsg = Ascii.image(authrespmsg.getBytes()).toString();
    } else {
      authrespmsg = "";
    }
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, batch.actioncode, actioncode);
    setProperty(ezp, batch.authrespmsg, authrespmsg);
    return genUpdateRecord(batch, batchid, ezp); //+_+ gets NPE on empty batch
  }

  static final QueryString genGetBatchNumberValue(Authid authid,
                                                  Terminalid terminalid) {
    return QueryString.
        Select(termauth.termbatchnum).
        from(termauth).
        where().nvPair(termauth.authid, authid).
        and().nvPair(termauth.terminalid, terminalid);
  }

  static final QueryString genTerminalAndAuthForBatch(Batchid batchid) {
    return QueryString.Select(termauth.terminalid).comma(termauth.authid).
        from(batch).comma(termauth).
        where().nvPair(batch.batchid, batchid).
        and().matching(batch.termauthid, termauth.termauthid);
  }

  static final QueryString genTerminalAndAuthForTermauth(TermAuthid termauthid) {
    return QueryString.Select(termauth.terminalid).comma(termauth.authid).
        from(termauth).
        where().nvPair(termauth.termauthid, termauthid);
  }

  // +++ and if it is in history, this will not work, which will be fine once we are rolling off batches into history
  static final QueryString genTerminalForTxnid(Txnid txnid) {
    return QueryString.Select(txn.terminalid).from(txn).where().nvPair(txn.
        txnid, txnid);
  }

  // ++ eventually make the inUnquoted lists take an int [ ], since that seems to be our new preferred parmaeter
  static final QueryString genLastAutoDrawerQuery(Terminalid [ ] terminalids) {
    TextList tl = new TextList(terminalids.length);
    for(int i = 0; i < terminalids.length; i++) {
      tl.add(terminalids[i].toString());
    }
    return QueryString.
        Select(drawer.transtarttime).
        from(drawer).
        where().inUnquoted(drawer.terminalid, tl).
        and().isTrue(drawer.auto).
        orderbydesc(drawer.transtarttime).limit(1);
  }

  static final QueryString genLastAutoBatchQuery(TextList termauthids) {
    return QueryString.
        Select(batch.batchtime).
        from(batch).
        where().inUnquoted(batch.termauthid, termauthids).
        and().isTrue(batch.auto).
        orderbydesc(batch.batchtime).limit(1);
  }

  static final QueryString genDrawerQuery(Drawerid drawerid,
                                          Terminalid terminalid,
                                          boolean onlyContribute) {
    return QueryString.
        SelectAll().
        cat(genWhichTxnsSubquery(drawerid, terminalid, onlyContribute)).
        orderbydesc(txn.txnid); //}
  }

  static final QueryString genBatchQuery(TermAuthid termauthid) {
    return QueryString.
        SelectAll().
        cat(genWhichTxnsSubquery(new Batchid(), termauthid, false)).
        orderbydesc(txn.txnid); //}
  }

  private static final QueryString genWhichTxnsSubquery(Drawerid drawerid,
      Terminalid terminalid,
      boolean onlyContribute) {
    QueryString moreWheres = Drawerid.isValid(drawerid) ?
                  QueryString.Clause().nvPair(txn.drawerid, drawerid) :
                  QueryString.Clause().isEmpty(txn.drawerid);
    //                                        do not include the terminalid in the query if the drawerid is good; superfluous!  just slows query (maybe)
    QueryString qs = genWhichTxnSubqueryInner( (Drawerid.isValid(drawerid) ? null :
                                      terminalid), onlyContribute, moreWheres);
    return qs;
  }

  // for drawers and terminals
  private static final QueryString genWhichTxnSubqueryInner(
      Terminalid terminalid, boolean onlyContribute, QueryString otherwhere) {
    QueryString qs = QueryString.Clause().
        from(txn).
        where();
    boolean somewhere = false;
    if (Terminalid.isValid(terminalid)) {
      qs.nvPair(txn.terminalid, terminalid);
      somewhere = true;
    }
    if(onlyContribute) {
      if(somewhere) {
        qs.and();
      }
      qs.cat(whichTxns(true /*onlyApproved*/, true /*onlyMoneyMovers*/, false /*onlySettlers*/));
      somewhere = true;
    }
    if(otherwhere != null) {
      if(somewhere) {
        qs.and();
      }
      qs.cat(otherwhere);
      somewhere = true;
    }
    if(!somewhere) {
      qs.cat("true"); // since we added the WHERE clause above; must satisfy it!
    }
    return qs;
  }

  private static final QueryString genWhichTxnsSubquery(Batchid batchid,
      TermAuthid termauthid, boolean justTotals) {
    QueryString otherWheres = Batchid.isValid(batchid) ?
            QueryString.Clause().nvPair(txn.batchid, batchid) :
            QueryString.Clause().isEmpty(txn.batchid);
    QueryString qs = QueryString.Clause().
        from(txn).comma(termauth).
        where().matching(termauth.terminalid, txn.terminalid).
        and().matching(termauth.authid, txn.settleid);
    if (TermAuthid.isValid(termauthid)) {
      qs.and().nvPair(termauth.termauthid, termauthid);
    }
    if(justTotals) {
      qs.and().cat(whichTxns(true /*onlyApproved*/, true /*onlyMoneyMovers*/, true /*onlySettlers*/));
    }
    if(otherWheres != null) {
      qs.and().cat(otherWheres);
    }
    return qs;
  }

  static final QueryString genStorePayInst(UniqueId storeid) {
    return QueryString.
        SelectDistinct(storeauth.paytype).comma(storeauth.institution).
        from(storeauth).
        where().nvPair(storeauth.storeid, storeid);
  }

  static final String COUNTER = "counter";
  static final String SUMER = "sumer";

  private static final SettleOp SOEMPTY = new SettleOp(); // don't set this ever!

  private static final QueryString genSelectTxnSubquery() {
    QueryString decoderReturn = QueryString.Clause().
        Decode(txn.settleop.name(),
               StringX.singleQuoteEscape(String.valueOf(SOEMPTY.CharFor(SettleOp.Return))),
               txn.settleamount.name() + "*-1", // settleamount
               "0");
    QueryString decoderSale = QueryString.Clause().
        Decode(txn.settleop.name(),
               StringX.singleQuoteEscape(String.valueOf(SOEMPTY.CharFor(SettleOp.Sale))),
               txn.settleamount.name(),
               decoderReturn.toString());
    return QueryString.Clause().
        count(txn.txnid).as(COUNTER).comma().sum(decoderSale).as(SUMER);
  }

  // +++ the last close time and last txn time need to be kept in the terminal table
  // and object, and not queried from the database!
  static final String LASTTXNTIME = "lastTxnTime";
  QueryString genTerminalPendingTotal(Terminalid terminalid) {
    return QueryString.Select(genSelectTxnSubquery()).comma().
        max(txn.clientreftime).as(LASTTXNTIME).
        cat(genWhichTxnsSubquery(new Drawerid(), terminalid, true));
  }

  static final QueryString genCreateCloseDrawer(Terminalid terminalid,
                                          String transtarttime,
                                          UniqueId associateid,
                                          UniqueId id,
                                          boolean auto) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, drawer.transtarttime, transtarttime);
    setProperty(ezp, drawer.terminalid, terminalid);
    setProperty(ezp, drawer.associateid, associateid);
    setProperty(ezp, drawer.auto, auto);
    return QueryString.Insert(drawer, ezp, id);
  }

  static final QueryString genSetDrawerTotals(Drawerid drawerid, int count,
                                              int amount) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, drawer.txncount, count);
    setProperty(ezp, drawer.txntotal, amount);
    return genUpdateRecord(drawer, drawerid, ezp);
  }

  static final QueryString genGetDrawerTotals(Drawerid drawerid) {
    return QueryString.Select(genSelectTxnSubquery()).from(txn).where().nvPair(
        txn.drawerid, drawerid).and().
        cat(whichTxns(true /*onlyApproved*/, true /*onlyMoneyMovers*/, true /*onlySettlers*/));
  }

  // +++ the last close time and last txn time need to be kept in the terminal table and object, and not queried from the database!
  /**
   * Get the newest (latest) CLOSING drawer for this terminal.
   */
  static final QueryString genPendingStartTimeQuery(Terminalid terminalid) {
    return QueryString.Select(drawer.transtarttime).
        from(drawer).
        where().nvPair(drawer.terminalid, terminalid).
        orderbydesc(drawer.transtarttime).limit(1);
  }

  // +++ with Postgres, we no longer have to keep timestamps at 14 chars!
  static final QueryString genTxnsForTime(String clientreftime) {
    if (clientreftime.length() > 14) {
      clientreftime = StringX.left(clientreftime, 14);
    }
    return QueryString.SelectAllFrom(txn).where().nvPair(txn.clientreftime,
        clientreftime.trim());
  }

  static final QueryString genTxnid(Terminalid terminalId, STAN stan) {
    return genid(txn).
        where().nvPair(txn.terminalid, terminalId).
        and().nvPair(txn.stan, stan).
        orderbydesc(txn.txnid);
  }

  // +++ should only look in the current batch ???
  static final QueryString genTxnid(Terminalid terminalId, String authrrn) {
    return genid(txn).
        where().nvPair(txn.terminalid, terminalId).
        and().nvPair(txn.authrrn, authrrn).
        orderbydesc(txn.txnid);
  }

  // doesn't need a "gen" since we are going to test the other two separately
  static final QueryString AuthAttempt(UniqueId id) {
    if(id instanceof Txnid) {
      return genAuthAttempt((Txnid)id);
    } else if(id instanceof AuthattemptId) {
      return genAuthAttempt((AuthattemptId)id);
    } else {
      return null;
    }
  }

  static final QueryString genAuthAttempt(Txnid id) {
    return QueryString.SelectAllFrom(authattempt).where().nvPair(authattempt.txnid, id);
  }

  static final QueryString genAuthAttempt(AuthattemptId id) {
    return QueryString.SelectAllFrom(authattempt).where().nvPair(authattempt.authattemptid, id);
  }

  static final QueryString genAuthAttempts(Terminalid terminalid,
                                           TimeRange times) {
    QueryString ret = QueryString.Select().all(authattempt).comma(authorizer.
        authname).
        from(authattempt).comma(authorizer).
        where().matching(authattempt.authid, authorizer.authid).
        and().nvPair(authattempt.terminalid, terminalid).
        and().nLTEQv(authattempt.authendtime, forTrantime(times.end())).
        and().nGTEQv(authattempt.authstarttime, forTrantime(times.start())).
        orderbydesc(authattempt.authattemptid);
    return ret;
  }

  // +++ with Postgres, we no longer have to keep timestamps at 14 chars!
  static final QueryString genTransactionFromQuery(Terminalid terminalId,
      String clientreftime) {
    if (clientreftime.length() > 14) {
      clientreftime = StringX.left(clientreftime, 14);
    }
    return genid(txn).
        where().nvPair(txn.terminalid, terminalId).
        and().nvPair(txn.clientreftime, clientreftime.trim());
  }

  static final QueryString genFindTransactionsBy(TxnFilter filter,
                                                 UniqueId storeid) {
    QueryString clauses = filter(filter);
    String clausestr = String.valueOf(clauses);
    if (!StringX.NonTrivial(clausestr)) {
      String panicer = "ATTEMPTED TO RUN AN 'ALL TXNS' QUERY!";
      dbg.ERROR(panicer);
      return null;
    }
    QueryString qs = null;
    qs = QueryString.Select(txn.txnid).comma().all().
        from(txn).comma(terminal).
        where().matching(terminal.terminalid, txn.terminalid).
        and().nvPair(terminal.storeid, storeid);
    return qs.cat(clauses);
  }

  private static final QueryString filter(TxnFilter filter) {
    // build the query string
    QueryString clauses = QueryString.Clause();
    if(ObjectRange.NonTrivial(filter.card)) {
      boolean justlast4 = filter.card.oneImage().length() < 5;
      if(justlast4) { // last4 uses just the last4, then opens each card to see if it matches
        clauses.and().nvPair(txn.cardlast4,
                             StringX.parseLong(filter.card.oneImage()));
      } else { // uses the hash and the last4
        clauses.and().nvPair(txn.cardlast4,
                             StringX.parseLong(filter.card.oneImage()) % 10000L);
        clauses.and().nvPair(txn.cardhash, filter.card.oneImage().hashCode());
      }
    }
    // handle the amounts
    if(ObjectRange.NonTrivial(filter.amount)) {
      clauses.and()
          .Open()
          .range(txn.amount, filter.amount, true)
          .or()
          .range(txn.settleamount, filter.amount, true)
          .Close();
    }
    // handle the stans
    clauses. /*andModularRange*/andRange(txn.stan, filter.stan);
    // handle the merchrefs
    clauses. /*andModularRange*/andRange(txn.merchref, filter.merch);
    // handle the approvals
    clauses.andRange(txn.approvalcode, filter.appr);
    // handle the dates
    clauses.andRange(txn.clientreftime /*TRANSTARTTIME*/, filter.time);
    return clauses;
  }

  static final QueryString genApplianceForTerminal(Terminalid terminalid) {
    return QueryString.Select(terminal.applianceid).from(terminal).where().
        nvPair(terminal.terminalid, terminalid);
  }

  static final QueryString genApplianceRowQuery(UniqueId storeid) {
    QueryString qs = genid(appliance);
    if(UniqueId.isValid(storeid)) {
      qs.where().nvPair(appliance.storeid, storeid);
    } else {
      // it will get all
    }
    return qs.orderbydesc(appliance.track).
          comma(appliance.storeid).
          comma(appliance.applname);
  }

  static final QueryString genTerminalsForStore(UniqueId storeid) {
    return QueryString.
        Select().all(terminal).
        from(terminal).
        where().nvPair(terminal.storeid, storeid).
        orderbyasc(terminal.terminalname);
  }

  static final QueryString genTerminalidsForStore(UniqueId storeid) {
    return genid(terminal).
        where().nvPair(terminal.storeid, storeid).
        orderbyasc(terminal.terminalname);
  }

  static final QueryString genTerminalPendingRow(Terminalid terminalid) {
    return QueryString.
        Select().all(terminal).
        from(terminal).
        where().nvPair(terminal.terminalid, terminalid);
  }

  static final QueryString genStoreBatchesQuery(UniqueId storeid, boolean showEmptyFailures, TimeRange tr) {
    QueryString qs = batchBaseQuery().
        and().nvPair(terminal.storeid, storeid).
        and().nGTEQv(batch.batchtime, forTrantime(tr.start().getTime())).
        and().nLTv(batch.batchtime, forTrantime(tr.end().getTime()));
    if (!showEmptyFailures) { // if don't show empty failures, only show approved or ones with txncount (to leave out failures with 0 txns)
      qs.and().Open().nvPair(batch.actioncode,
                             ActionCode.Approved).or().nvPair(batch.actioncode,
          ActionCode.Unknown).
          or().nGTv(batch.txncount, 0L).Close();
    }
    return qs.orderbydesc(batch.batchtime);
  }

  static final QueryString genMostRecentStoreDrawer(UniqueId storeid) {
    return QueryString.Select(drawer.transtarttime).
        from(terminal).comma(drawer).
        where().nvPair(terminal.storeid, storeid).
        and().matching(terminal.terminalid, drawer.terminalid).
        orderbydesc(drawer.transtarttime).limit(1);
  }

  static final QueryString genMostRecentStoreBatch(UniqueId storeid) {
    return QueryString.Select(batch.batchtime).
        from(terminal).comma(batch).comma(termauth).
        where().nvPair(terminal.storeid, storeid).
        and().matching(terminal.terminalid, termauth.terminalid).
        and().matching(termauth.termauthid, batch.termauthid).
        orderbydesc(batch.batchtime).limit(1);
  }

  // +++ the last submit time and last termauth txn time need to be kept in the termauth table and object, and not queried from the database!
  static final QueryString genTermAuthPendingTotal(TermAuthid termauthid) {
    return QueryString.Select(genSelectTxnSubquery()).comma().
        max(txn.clientreftime).as(LASTTXNTIME).
        cat(genWhichTxnsSubquery(new Batchid(), termauthid, true));
  }

  // +++ the last submit time and last termauth txn time need to be kept in the termauth table and object, and not queried from the database!
  static final QueryString genTermAuthLastSubmit(TermAuthid termauthid) {
    return QueryString.Select(batch.batchtime).
        from(batch).
        where().nvPair(batch.termauthid, termauthid).
        orderbydesc(batch.batchtime).limit(1);
  }

  static final QueryString genUsedTtPtIn(Authid authid, UniqueId storeid,
                                         TimeRange daterange) {
    QueryString qs = QueryString.
        SelectDistinct(txn.paytype).comma(txn.institution).comma(txn.transfertype).
        from(txn).comma(batch).comma(terminal).
        where().matching(txn.batchid, batch.batchid).
        and().nvPair(txn.authid, authid).
        and().matching(txn.terminalid, terminal.terminalid).
        and().nvPair(terminal.storeid, storeid).
        andRange(batch.batchtime, daterange, false).
        and().nGTv(txn.authendtime, "2"). // to be sure that we are not getting unauthed txns
        orderbyasc(txn.paytype).comma(txn.institution).comma(txn.transfertype);
    return qs;
  }

  /**
   * SELECT storename, storeid, authname, authid
   * from store, auth
   * [possibly where auth.authid = the authid parameter passed in]
   */
  static final QueryString genFullAuthStore(Authid authid, UniqueId storeid) {
    QueryString ret = QueryString.
        Select(store.storeid).comma(authorizer.authname).
        comma(authorizer.authid).
        from(store).comma(authorizer);
    boolean whered = false;
    if (Authid.isValid(authid)) {
      ret.where().nvPair(authorizer.authid, authid);
      whered = true;
    }
    if (UniqueId.isValid(storeid)) {
      if (whered) {
        ret.and();
      } else {
        ret.where();
        whered = true;
      }
      ret.nvPair(store.storeid, storeid);
    }
    ret.orderbyasc(authorizer.authid).comma(store.storeid);
    return ret;
  }

  static final QueryString genTermsInfoForStores(UniqueId storeid, Authid authid) {
    return QueryString.
        Select(terminal.terminalname).comma(termauth.terminalid).
        comma(termauth.authtermid).comma(termauth.termauthid).
        from(termauth).comma(terminal).
        where().matching(termauth.terminalid, terminal.terminalid).
        and().nvPair(terminal.storeid, storeid).
        and().nvPair(termauth.authid, authid);
  }

  static final QueryString genTermBatchReport(TextList termauthidinlist,
                                              TimeRange daterange) {
    QueryString qs = QueryString.
        Select(batch.termbatchnum).comma(batch.batchtime).
        comma(batch.batchid).comma(termauth.terminalid).comma(batch.authrespmsg).
        from(batch).comma(termauth).
        where().matching(batch.termauthid, termauth.termauthid).
        and().cat(batch.termauthid.fullName()).inQuoted(termauthidinlist).
        and().nvPair(batch.actioncode, ActionCode.Approved).
        andRange(batch.batchtime, daterange, false).
        orderbyasc(termauth.terminalid).comma(batch.batchtime);
    return qs;
  }

  static final QueryString genBatchTxnCounts(Batchid batchid) {
    return QueryString.
        Select(txn.paytype).comma(txn.institution).comma(txn.transfertype).
        comma().cat("count(" + txn.txnid.name() + ")").as("counter").
        from(txn).
        where().nvPair(txn.batchid, batchid).
        and().nGTv(txn.authendtime, "2"). // so that we only get auth'd txns
        groupby(txn.paytype).comma(txn.institution).comma(txn.transfertype).
        orderbyasc(txn.paytype).comma(txn.institution).comma(txn.transfertype);
  }

  // no matter how you slice it, this txn is going to take a while  :(
  static final QueryString genDupCheck(String sincedate) {
    // the short headers are for ease of reading on the report
    return QueryString.
        Select().substring(txn.transtarttime, 1, 8).as("tdate").
        comma(store.storename).
        comma(terminal.terminalname).as("term").
        comma(txn.cardhash).as("cardhash").
        comma(txn.cardlast4).as("last4").
        comma(txn.transfertype).
        comma(txn.amount).as("amt"). // dup checks are looking for auth amounts
        comma().count(txn.amount).as("N1"). // dup checks are looking for auth amounts
        comma().count(QueryString.Clause().substring(txn.transtarttime, 1, 8).toString()).as("N2").
        comma().min(txn.stan).as("stan1").
        comma().max(txn.stan).as("stan2").
        comma().sum(QueryString.Clause().Decode(txn.stoodin.fullName(),
                       "true","1","0")).as("sicount").
        comma().max(txn.clientreftime).as("last_date").
        comma().min(txn.transtarttime).as("time1").
        comma().max(txn.transtarttime).as("time2").
        comma().Open().
        castAsBigint(QueryString.Clause().max(txn.transtarttime).toString()).
        cat(" - ").
        castAsBigint(QueryString.Clause().min(txn.transtarttime).toString()).
        Close().
        as("secs").
        comma().Decode("min(stan)",
                       String.valueOf(QueryString.Clause().max(txn.stan)),
                       "'*'", "' '").as("SAME").
        from(txn).comma(storeauth).comma(store).comma(terminal).
        where().matching(store.storeid, terminal.storeid).
        and().matching(terminal.terminalid, txn.terminalid).
        and().matching(storeauth.storeid, store.storeid).
        and().matching(storeauth.paytype, txn.paytype).
        and().matching(storeauth.institution, txn.institution).
        and().nGTv(txn.amount, 1). // dup checks are looking for auth amounts
        and().nGTv(txn.transtarttime, sincedate).
        and().not().nvPair(txn.actioncode, ActionCode.Declined).
        and().not().nvPair(txn.actioncode, ActionCode.Failed).
        and().isFalse(txn.voided).
        groupby(store.storename).
        comma(terminal.terminalname).
        comma(txn.cardhash).
        comma(txn.transfertype).
        comma(txn.cardlast4).
        comma(txn.amount). // dup checks are looking for auth amounts
        comma().substring(txn.transtarttime, 1, 8).
        having().count(txn.amount).cat(" > 1"). // dup checks are looking for auth amounts
        and().count(QueryString.Clause().substring(txn.transtarttime, 1, 8).toString()).cat(" > 1").
        orderbydesc(1).
        comma().cat("2,3,4,6").
        limit(100); // no need to do too many; +++ get from configs!
  }

  static final QueryString genCreateAssociate(UniqueId enterpriseid,
                                              String loginname, String encodedpw,
                                              UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, associate.enterpriseid, enterpriseid);
    setProperty(ezp, associate.encodedpw, encodedpw);
    setProperty(ezp, associate.loginname, loginname);
    return QueryString.Insert(associate, ezp, id);
  }

  static final QueryString genCreateStore(UniqueId enterpriseid,
                                          String storename, UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, store.enterpriseid, enterpriseid);
    return QueryString.Insert(store, ezp, id);
  }

  static final QueryString genCreateStoreAccess(UniqueId storeid,
                                                UniqueId associateid,
                                                ClerkPrivileges sperms,
                                                UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, storeaccess.storeid, storeid);
    setProperty(ezp, storeaccess.associateid, associateid);
    if(sperms != null) {
      setProperty(ezp, storeaccess.ensale, sperms.canSALE);
      setProperty(ezp, storeaccess.enreturn, sperms.canREFUND);
      setProperty(ezp, storeaccess.envoid, sperms.canVOID);
      setProperty(ezp, storeaccess.enclosedrawer, sperms.canClose);
    }
    return QueryString.Insert(storeaccess, ezp, id);
  }

  static final QueryString genCreateStoreAuth(Authid authid, String authmerchid,
                                              String institution,
                                              int maxtxnlimit, String paytype,
                                              UniqueId storeid,
                                              Authid settleid,
                                              String settlemerchid,
                                              UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, storeauth.authid, authid);
    setProperty(ezp, storeauth.authmerchid, authmerchid);
    setProperty(ezp, storeauth.institution, institution);
    setProperty(ezp, storeauth.maxtxnlimit, maxtxnlimit);
    setProperty(ezp, storeauth.paytype, paytype);
    setProperty(ezp, storeauth.settleid, settleid);
    setProperty(ezp, storeauth.storeid, storeid);
    setProperty(ezp, storeauth.settlemerchid, settlemerchid);
    return QueryString.Insert(storeauth, ezp, id);
  }

  static final QueryString genCreateAppliance(String applname, UniqueId storeid, UniqueId applianceid) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, appliance.storeid, storeid);
    setProperty(ezp, appliance.applname, applname);
    return QueryString.Insert(appliance, ezp, applianceid);
  }

// no persistence of appliance log data 20040523 !
//  static final QueryString genCreateApplNetStatus(UniqueId applianceid, UniqueId id) {
//    EasyProperties ezp = new EasyProperties();
//    setProperty(ezp, applnetstatus.applianceid, applianceid);
//    return QueryString.Insert(applnetstatus, ezp, id);
//  }
//
//  static final QueryString genCreateApplPgmStatus(UniqueId applianceid, UniqueId id) {
//    EasyProperties ezp = new EasyProperties();
//    setProperty(ezp, applpgmstatus.applianceid, applianceid);
//    return QueryString.Insert(applpgmstatus, ezp, id);
//  }

  static final QueryString genCreateTerminal(String modelcode,
                                             String terminalname,
                                             UniqueId applianceid,
                                             boolean dosigcap,
                                             UniqueId storeid,
                                             UniqueId newid) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, terminal.modelcode, modelcode);
    setProperty(ezp, terminal.terminalname, terminalname);
    setProperty(ezp, terminal.applianceid, applianceid);
    setProperty(ezp, terminal.dosigcap, dosigcap);
    setProperty(ezp, terminal.storeid, storeid);
    return QueryString.Insert(terminal, ezp, newid);
  }

  static final QueryString genCreateTermauth(Authid authid,
                                             Terminalid terminalid,
                                             int termbatchnum,
                                             String authtermid, int authseq,
                                             UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, termauth.authid, authid);
    setProperty(ezp, termauth.terminalid, terminalid);
    setProperty(ezp, termauth.termbatchnum, termbatchnum);
    setProperty(ezp, termauth.authtermid, authtermid);
    setProperty(ezp, termauth.authseq, authseq);
    return QueryString.Insert(termauth, ezp, id);
  }

  /**
   * @return find an exact match, findSimilar+same time and terminal as well
   */
  static final QueryString genFindExactly(Terminalid terminalid, PaymentRequest req) {
//    String accountnum = req.hasCardInfo() ? req.card.accountNumber.Image() : "0";
    String payType = String.valueOf(req.sale.type.payby.Char());
    String transferType = String.valueOf(req.sale.type.op.Char());
    QueryString qs = genid(txn).
        where().nvPair(txn.terminalid, terminalid).
        // dup checks are looking for auth amounts
        and().nvPair(txn.amount, req.Amount().Value()).
        and().nvPair(txn.cardhash, req.card.cardHash()).
        and().nvPair(txn.cardlast4, req.card.accountNumber.last4int()).
        and().nvPair(txn.transfertype, transferType).
        and().nvPair(txn.paytype, payType).
        and().isFalse(txn.voided).
        and().nvPair(txn.stan, req.TxnReference().STAN());
    TimeRange tr = TimeRange.Create();
    UTC now = UTC.Now();
    // we search back 5 days for dups of the same stan, etc.,
    // which is 1/2 of the range of a stan (based on 999999 seconds)
    UTC then = UTC.New(now.getTime()-Ticks.forDays(5)); // +++ get '5' from configs
    tr.setStart(then);
    tr.setEnd(now);
    qs.andRange(txn.transtarttime, tr, true /* >= */);
    // we do NOT use the clientreftime above.
    // the above test is for txns that came in in the last 5 days
    // that are EXACTLY like the current txn, including identical stans
    // the frequency of this happening for 2 really different txns is incredibly minute
    // so, this is a good test
    return qs.orderbydesc(txn.txnid);
  }

  static final QueryString genApplianceTerminalsQuery(UniqueId applianceid) {
    return QueryString.Select(terminal.terminalid).comma(terminal.terminalname).
        from(terminal).
        where().nvPair(terminal.applianceid, applianceid).
        orderbyasc(terminal.terminalname);
  }

  static final QueryString genAssociateidForAssociateLoginname(String loginname) {
    return genid(associate).
        where().nvPair(associate.loginname, loginname);
  }

  static final QueryString genStoreAuths(UniqueId storeid) {
    return QueryString.
        SelectAllFrom(storeauth).
        where().nvPair(storeauth.storeid, storeid);
  }

  static final QueryString genTermAuths(Terminalid termid) {
    return QueryString.
        SelectAllFrom(termauth).
        where().nvPair(termauth.terminalid, termid);
  }

  // +++ eventually require storeid
  static final QueryString genStoreAccessesByAssoc(UniqueId associd) {
    return QueryString.
        SelectAllFrom(storeaccess).
        where().nvPair(storeaccess.associateid, associd);
  }

  static final QueryString genStoreAccessesByStore(UniqueId storeid) {
    return QueryString.
        SelectAllFrom(storeaccess).
        where().nvPair(storeaccess.storeid, storeid);
  }

  static final QueryString genAuthIdsForStore(UniqueId storeid) {
    QueryString onion = QueryString.
        Select(storeauth.settleid).from(storeauth).
        where().nvPair(storeauth.storeid, storeid);
    return QueryString.
        Select(storeauth.authid).from(storeauth).
        where().nvPair(storeauth.storeid, storeid).
        union(onion);
  }

  static final QueryString genCreateAuthAttempt(Txnid txnid, Authid authid,
                                               String authrequest, // severely encoded!
                                               Terminalid terminalid,
                                               UniqueId id) {
    EasyProperties ezp = new EasyProperties();
    setProperty(ezp, authattempt.authrequest, authrequest);
    setProperty(ezp, authattempt.authid, authid);
    setProperty(ezp, authattempt.txnid, txnid);
    setProperty(ezp, authattempt.terminalid, terminalid);
    setProperty(ezp, authattempt.authstarttime, Now());
    return QueryString.Insert(authattempt, ezp, id);
  }

  // @param onlyMoneyMovers - txn.settleop in ('S','R'); [no V,Q,M]
  // @param onlySettlers - txn.settle=true
  // @param onlyApproved - (txn.actioncode='A' or txn.stoodin=true) and not txn.voided=true
  private static final QueryString whichTxns(boolean onlyApproved, boolean onlyMoneyMovers, boolean onlySettlers) {
    boolean has = false;
    QueryString toReturn = QueryString.Clause();
    if(onlyApproved) {
      toReturn.
          Open().
          Open().
          nvPair(txn.actioncode, ActionCode.Approved).
          Close().
          or().
          Open().
          isTrue(txn.stoodin).
          Close().
          Close().
          and().not().isTrue(txn.voided);
      has = true;
    }
    if(onlyMoneyMovers) {
      if(has) {
        toReturn.and();
      }
      if(!onlySettlers) {
        onlySettlers = true; // only way to handle it, due to authonly getting modified to settle
      }
      TextList tl = new TextList();
      tl.add(String.valueOf(SOEMPTY.CharFor(SettleOp.Sale)));
      tl.add(String.valueOf(SOEMPTY.CharFor(SettleOp.Return)));
      toReturn.cat(txn.settleop.fullName()).inQuoted(tl);
    }
    if(onlySettlers) {
      if(has) {
        toReturn.and();
      }
      toReturn.isTrue(txn.settle);
      has = true;
    }
    dbg.VERBOSE("ApprovedTxns() returning: " + toReturn);
    return toReturn;
  }

  static final QueryString genStoreForTerminal(Terminalid terminalid) {
    return QueryString.Select(terminal.storeid).from(terminal).
        where().nvPair(terminal.terminalid,terminalid);
  }

  static final QueryString genProcInfoFor(Terminalid terminalid, Authid authid,
                                          UniqueId storeid, boolean settlement) {
    return QueryString.
        SelectDistinct(termauth.authtermid).
        comma(settlement ? storeauth.settlemerchid : storeauth.authmerchid).
        //add txnlimit lookup?
        from(storeauth).comma(termauth).comma(terminal).
        where().nvPair(terminal.storeid, storeid).
        and().nvPair(terminal.terminalid, terminalid).
        and().matching(settlement ? storeauth.settleid : storeauth.authid,
                       termauth.authid).
        and().nvPair(settlement ? storeauth.settleid : storeauth.authid, authid).
        and().matching(termauth.terminalid, terminal.terminalid).
        and().matching(storeauth.storeid, terminal.storeid);
  }

  /**
   * @param termid the terminal to ttl standins for
   * @returns a QueryString to get the ttl cents for all txns in standin for this terminal
   *
   * select sum(amount)
   * from txn
   * where authendtime is null
   * and terminalid = whatever
   * and ...
   */
  static final QueryString genTtlStandinsForTerminal(Terminalid termid) {
    return QueryString.
        Select().sum(txn.amount).// auth amounts are stoodin
        from(txn).
        where().nvPair(txn.terminalid, termid).
        and().cat(openStandins());
  }

  /**
   * needs to be anded with rest of where clause
   */
  private static final QueryString openStandins() {
    return needsAuth().and().isTrue(txn.stoodin);
  }

  /**
   * needs authorization
   */
  private static final QueryString needsAuth() {
    return QueryString.Clause().isEmpty(txn.authendtime).
//        and().not().isTrue(txn.voided);
        and().isTrue(txn.authz);
  }

// no persistence of appliance log data 20040523 !
//  // get the most recent (oldest chronologically) status for this appliance
//  static final QueryString genLastApplNetStatus(UniqueId applianceid) {
//    return QueryString.
//        Select(applnetstatus.applnetstatusid).
//        from(applnetstatus).
//        where().nvPair(applnetstatus.applianceid, applianceid).
//        orderbydesc(applnetstatus.srvrtime).limit(1);
//  }
//  // get the most recent (oldest chronologically) status for this appliance
//  static final QueryString genLastApplPgmStatus(UniqueId applianceid, boolean connection) {
//    return QueryString.
//        Select(applpgmstatus.applpgmstatusid).
//        from(applpgmstatus).
//        where().nvPair(applpgmstatus.applianceid, applianceid).
//        and().booleanIs(applpgmstatus.wasconnection, connection).
//        orderbydesc(applpgmstatus.srvrtime).limit(1);
//  }

  static final QueryString genStoreTxnCounts() {
    return QueryString.Select(store.storeid).
        comma().count(txn.txnid).as("txncount").
        from(txn).comma(terminal).comma(store).
        where().matching(txn.terminalid, terminal.terminalid).
        and().matching(terminal.storeid, store.storeid).
        groupby(store.storeid).
        orderbyasc(store.storeid);
  }

  static final QueryString genLastStoreTxn(UniqueId storeid) {
    return QueryString.SelectAllFrom(txn).comma(terminal).
        where().matching(txn.terminalid, terminal.terminalid).
        and().nvPair(terminal.storeid, storeid).
        orderbydesc(txn.txnid).limit(1);
  }

}

// $Id: PayMateDBQueryString.java,v 1.102 2004/05/23 19:02:47 mattm Exp $
