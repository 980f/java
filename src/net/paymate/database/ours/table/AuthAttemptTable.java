package net.paymate.database.ours.table;

import net.paymate.database.*;
import net.paymate.database.ours.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/AuthAttemptTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

public class AuthAttemptTable extends GenericTableProfile {
  // +++++++++
  // add in the commented out stuff whenever we start logging ALL authattempts !!!

//  public final ColumnProfile actioncode   =createColumn(ACTIONCODE   ,DBTypesFiltered.CHAR,ColumnProfile.CANNULL,ColumnProfile.NOAUTO, null);
  public final ColumnProfile authattemptid=createColumn("authattemptid",DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  // convert to a millis-duration field ...
  public final ColumnProfile authendtime  =createColumn("authendtime"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile authid       =createColumn("authid"       ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  // with PG, these fields are now any length
  public final ColumnProfile authrequest  =createColumn("authrequest"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile authresponse =createColumn("authresponse" ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
//  public final ColumnProfile authresponsemsg=createColumn(AUTHRESPONSEMSG,DBTypesFiltered.TEXT,ColumnProfile.CANNULL,ColumnProfile.NOAUTO, null);
  // convert to a no-millis int4 field ...
  public final ColumnProfile authstarttime=createColumn("authstarttime",DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile terminalid   =createColumn("terminalid"   ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile txnid        =createColumn("txnid"        ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);

  IndexProfile authatti = new IndexProfile("authatti", this, terminalid);
  IndexProfile authatst = new IndexProfile("authatst", this, authstarttime);
  IndexProfile authatet = new IndexProfile("authatet", this, authendtime);
  IndexProfile aatxnidx = new IndexProfile("aatxnidx", this, txnid);
  IndexProfile aaauthidx = new IndexProfile("aaauthidx", this, authid);

  public AuthAttemptTable() {
    super(AUTHATTEMPTTABLE, logType);
    setContents("authattpk", authattemptid);
  }
}
