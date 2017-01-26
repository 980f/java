package net.paymate.authorizer.linkpoint;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LinkpointAuthorizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.23 $
 */
import net.paymate.connection.PaymentReply;
import net.paymate.authorizer.*;
import net.paymate.data.*;
import net.paymate.awtx.*;//Realmoney
import net.paymate.net.*; // ipsepc
import net.paymate.util.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.*;
import net.paymate.io.IOX;
import net.paymate.data.sinet.business.*;

public class LinkpointAuthorizer extends Authorizer implements LPConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LinkpointAuthorizer.class);
  // MUST have a public empty constructor for the loader !!!
  public LinkpointAuthorizer() {
    super();
  }
  protected void logGatewayTransaction(PayMateDB db, AuthAttempt attempt) {
    // stub; shouldn't do this!
  }
  protected PaymentReply doForce(PayMateDB db, TxnRow record) {
    return doneLocally(db, record, AuthResponse.mkDeclined("Cannot do force."));
  }
  protected AuthTransaction genTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    return new LPTransaction(record, original, storeid, slim, merch, this);
  }
  protected GatewayTransaction genGWTransaction(byte[] bytes, String potentialGWTID) {
    return null; // they don't do this!
  }

  protected boolean accepts(TransferType tt) {
    switch(tt.Value()) {
      case TransferType.Return:
      case TransferType.Reversal:
      case TransferType.Sale: {
        return true;
      } // break;
      case TransferType.Authonly:
      case TransferType.Force:
      case TransferType.Modify:
      case TransferType.Query:
      case TransferType.Unknown:
      default:{
        return false;
      } // break;
    }
  }

  public int calculateSubmittalRate() {
    return 0; // we don't send off settlements to them
  }
  public int calculateTxnRate(TransferType tt, PayType pt, Institution in) {
    return 200; // these are not sent off through this authorizer
  }
  public AuthSocketAgent genSubmittalAgent() {
    return new LPSubmittalSocketAgent(this); // an extension of AuthSocketAgent
  }
  private String path2files = "";
  private boolean live = false;
  protected void loadProperties() {
    // primer:
    loadPath2files();
    loadLive();
    dbg.ERROR("Using ClearCommerce SSL Java API Version: "+clearcommerce.ssl.JUtil.versionString());
    /** +++
     * authorizer properties needed are:
     * root of cert storage
     * number of simultaneous sockets allowed - ?
     * inherit IP and port and socket timeouts
     * duplication-error handling algorithm (changes on their side are pending) - WHAT?  I did not read anywhere that they were making changes for us (MMM)
     */
  }

/////////////////////////
/// Grabbing parameters
  protected void loadPath2files() { // primer
    // primer:
    path2files = dbd.getServiceParam(serviceName(), "storecertpath", "/data/auth/linkpoint/storecerts");
    java.io.File file = new java.io.File(path2files);
    IOX.createDirs(file);
    path2files+="/";
  }
  protected void loadLive() {
    live = dbd.getBooleanServiceParam(serviceName(), "live", false);
  }
  // +++
/// Grabbing parameters
/////////////////////////

  public boolean getLive() {
    return live;
  }
  public String getPath2files() { // eg: /data/auth/linkpoint/storecerts
    return path2files;
  }
  public String getKeyfilebase() { // eg: "key.der"
    return "key.der";
  }
  public String getCertfilebase() { // eg: cert.der"
    return /*USEXML ? ".jks" :*/ "cert.der";
  }
//  /* package */ IPSpec [] getIPs() {
//    return ips;
//  }

  protected AuthSubmitTransaction genSubmitTxn(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    AuthSubmitTransaction submittal = new AuthSubmitTransaction();
    submittal.request = new LinkpointSettlementRequest(authid, terminalid, merch);// parameters are tossed, though
    submittal.response = new LinkpointSettlementResponse();
    return submittal;
  }
  protected boolean processLocally(AuthTransaction authTran) {
    return false; // we send everything off since it is host capture on their side
  }
  protected AuthTerminalAgent genTermAgent(Terminalid termid) {
    return new LPAuthTermAgent(this, termid, bufferSize, sequencer(),
                               termbatchnum(termid), fgThreadPriority,
                               bgThreadPriority);
  }

}
//$Id: LinkpointAuthorizer.java,v 1.23 2004/03/30 03:33:08 mattm Exp $