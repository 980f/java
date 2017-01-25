package net.paymate.connection;

/**
* Title:
* Description:  record of Stoodin Table
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Id: StandinRecord.java,v 1.6 2001/10/02 17:06:37 mattm Exp $
*/

import net.paymate.ISO8583.data.TransactionID;
import java.sql.ResultSet;
import java.util.Date;

public class StandinRecord {
  public String terminalPuid;
  public TransactionID realTid;
  public TransactionID originalTid;
  public ClerkIdInfo originalClerk;

  /**
  * status changes:
  * starts Received.
  * set to Processing when shoved at mainsail.
  * set to Failed if we DON'T get an authresponse (gave up or record is defective)
  * set to Done when we get an auth response
  * as elsewhere Done doesn't mean approved, and failed doesn't mean declined
  * ... see the tranjoiur record for that info
  */

  public static final StandinRecord New(String termid,TransactionID real, TransactionID tid,ClerkIdInfo clerk){
    StandinRecord newone=new StandinRecord();
    newone.terminalPuid=termid;
    newone.realTid=real;
    newone.originalTid=tid;
    newone.originalClerk=clerk;
    return newone;
  }

  private StandinRecord() {
    //@see NEW
  }

}
//$Id: StandinRecord.java,v 1.6 2001/10/02 17:06:37 mattm Exp $
