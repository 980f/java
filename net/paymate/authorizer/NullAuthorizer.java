package net.paymate.authorizer;

import net.paymate.connection.ActionReply;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/NullAuthorizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.*;
import java.io.*;
import net.paymate.web.*; // +++ NEED TO MOVE LOGININFO to DATA (or some of its guts)
import net.paymate.net.*;
import net.paymate.util.*;

public class NullAuthorizer extends Authorizer {

  public NullAuthorizer() {
    // see Authorizer
  }

  public void init(int id, String name, PayMateDB db, String hostname, PrintStream backup, SendMail mailer) {
    super.init(id, name, db, hostname, backup, mailer);
  }

  public void handleResponse(ResponseNotificationEvent event) {
    // stub
  }

//  public boolean bringup() {
//    // stub
//    return true;
//  }

  public String status() {
    return "UP";
  }

  // stays up always
  public boolean isup() {
    // stub
    return true;
  }

  public boolean shutdown() {
    // stub
    return true;
  }

  public void initStandin() {
    // stub
  }

  public void standinProcess() {
    // stub
  }

  public void setNextSequence(TxnRow record) {
    // stub
  }

  public FinancialReply authorize(TxnRow record, TxnRow original, boolean force) {
    txnTimes.add(0);
    pf.println("Received a txn: "+record.tid().image()/*txnid*/ + " ...");
    pf.println(record.toString());
    FinancialReply ar = forTxnRow(record);
    ar.setState(ActionReplyStatus.ServerError);
    pf.println("Replying with: " + ar.toString());
    return ar;
  }
}

// $Id: NullAuthorizer.java,v 1.12 2001/11/17 20:06:36 mattm Exp $
