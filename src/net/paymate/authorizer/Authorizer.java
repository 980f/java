package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/Authorizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @todo: contain a 'service' rather than being one.
 * @version $Revision: 1.170 $
 */

import net.paymate.data.*;
import net.paymate.awtx.*;//Realmoney
import net.paymate.lang.Bool;
import net.paymate.database.*;
import net.paymate.database.ours.query.*;
import net.paymate.connection.*;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;
import net.paymate.util.*;
import net.paymate.net.*;
//import net.paymate.web.*; // +++ NEED TO MOVE LOGININFO to DATA (or some of its guts)
import net.paymate.lang.StringX;
import net.paymate.data.sinet.business.*;

public abstract class Authorizer extends Service {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Authorizer.class);

  /**
   * Needed:
   *  a name (String)
   *  an index to add to the end of the name in case we make multiples of this: (see util.Counter)
   *  a socket connection (A ManagedSocket)
   *  read and write threads for that socket
   *  a queue to put things into [includes the thread that left it here for waking up when done, along with the request and response]
   *  a status
   *  a cleaner (thread)
   *
   * Facilities:
   *  abort() -- abort the sending of a txn (causes the auth to void it if it comes back after this is called)
   *  add()   -- send a txn, puts in queue, notifies the writer thread to come get it
   *  remove()-- called by client thread after it is awakened, removes the item from queue
   *
   *  recd()  -- called by the reader thread when a reply is received, matches up the reply with a request, awakens the client thread
   *  send()  -- called by the writer thread to get a message for sending
   *
   *  init()  -- called by the AuthorizerManager to start the threads and make the socket connections [happens on a different (starter) thread?]
   */

  // services stuff:
  // read/write statistics and status info:
  // final prevents reassignments
  public final Accumulator writes = new Accumulator();
  public final Accumulator reads = new Accumulator();
  public final Accumulator txnTimes = new Accumulator();
  public final Counter timeouts = new Counter();
  public final Counter connections = new Counter();
  public final Counter connectionAttempts = new Counter();
  public final String svcTxns(){
    return ""+txnTimes.getCount();
  }
  public final String svcCnxns() {
    return of(connections.value(), connectionAttempts.value());
  }
  public final String svcPend() {
    Accumulator queued = termAgents.queuedTxns();
    return ""+printMoneyStats(queued);
  }
  public final String svcTimeouts() {
    return ""+timeouts.value();
  }
  public final String svcAvgTime() {
    return DateX.millisToSecsPlus(txnTimes.getAverage());
  }
  public final String svcWrites() {
    return printByteStats(writes);
  }
  public final String svcReads() {
    return printByteStats(reads);
  }
// end "service interface
/////////////////////////////

  public Authid id = new Authid();
  public MultiHomedSocketFactory ips = null;
  public MultiHomedSocketFactory settleIps = null;
  public final AuthTermAgentList termAgents = new AuthTermAgentList();
  public int bufferSize = 1024;
  //these timeouts are in milliseconds
  public int timeout = (int)Ticks.forSeconds(35); // for auths; guessing, but will overwrite with value from configs
  public int submitTimeout = (int)Ticks.forSeconds(60); // for batch submissions; guessing, but will overwrite with value from configs
  public int connectTimeout = (int)Ticks.forSeconds(3); // for connecting to a non-SSL socket
  public boolean upOnInit = false;

  protected PayMateDBDispenser dbd = new PayMateDBDispenser();
  protected int fgThreadPriority;
  protected int bgThreadPriority;

  // +++ mutex?
  public void init(Authid id, String servicename, int fgThreadPriority,
                   int bgThreadPriority) {
    this.fgThreadPriority = fgThreadPriority;
    this.bgThreadPriority = bgThreadPriority;
    configger = dbd;
    if(!StringX.NonTrivial(servicename)) {
      dbg.ERROR("SERVICENAME '" + servicename + "' IS EITHER NULL OR INVALID!");
      servicename = "UnknownAuthorizer"+DateX.Now(); // will cause it to become "Invalid", as it will not pick up any configuration parameters, like its classname
    }
    setInstanceName( servicename);
    this.id = new Authid(id.value());
    agentListMonitor = new Monitor(serviceName()+".TermAgentList");
    initLog();
    println(serviceName()+" inited.");
    try {
      upOnInit = dbd.getBooleanServiceParam(serviceName(),"uponinit", true);
      if(upOnInit) {
        up(); // must do this last thing in each authorizer module!
      } else {
        // do NOT remove this line!  Certain parameters are needed even if the authorizer isn't running!
        loadAllProperties(); // otherwise, just load the properties
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      println(serviceName()+" state:"+isUp());
    }
  }

  protected static final PaymentReply forTxnRow(TxnRow record) {
    return record!=null?record.extractReply():PaymentReply.Fubar("Null Record");
  }

  private Monitor agentListMonitor = null;
  protected AuthTerminalAgent getAgent(Terminalid termid) {
    AuthTerminalAgent agent = null;
    try {
      agentListMonitor.getMonitor();
      agent = termAgents.agentForTerminal(termid);
      if(agent == null) { // make one if it doesn't already exist
        agent = genTermAgent(termid);
        termAgents.add(agent);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      agentListMonitor.freeMonitor();
      return agent;
    }
  }

  public Counter sequencer() {
    return new Counter(sequenceRange.low(), sequenceRange.high(), 0); // just adjust the limits (value will get set on a per-terminal basis from database)
  }
  public Counter termbatchnum(Terminalid term) {
    return new Counter(batchNumberRange.low(), batchNumberRange.high(), dbd.getPayMateDB().getBatchNumberValue(id, term));
  }


  // @@@ +++ make sure that serverside standin db stamping and txncomplete dbstamping are mutually exclusive regarding fields.

  /////////////////////////////////////
  // extend the following ...
  //
  public Authorizer() { // required since we will be loading with the classname only!
    super("AUTH--TBD", null);
    clearIps();
  }

  private synchronized void clearIps() {
    // +++ move the synch inside on a separate object
    if(ips != null) {
      ips.shutdown();
    }
    ips = new MultiHomedSocketFactory();
    if(settleIps != null) {
      settleIps.shutdown();
    }
    settleIps = new MultiHomedSocketFactory();
  }
  /**
   * generate an AuthTerminalAgent for this terminal.
   */
  protected abstract AuthTerminalAgent genTermAgent(Terminalid termid);
  protected abstract boolean processLocally(AuthTransaction authTran);//modifies flow of reversals
  /**
   * Generate an empty auth request
   */
  protected abstract AuthTransaction genTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch);
  protected abstract GatewayTransaction genGWTransaction(byte[] bytes, String potentialGWTID);
  /**
   * Load properties specific to the extended class only:
   */
  protected abstract void loadProperties();
  /**
   * Creates the socket agent used to send batch submittals off for settlement
   */
  public abstract AuthSocketAgent genSubmittalAgent();
  /**
   * Generate an empty submittal request
   */
  protected abstract AuthSubmitTransaction genSubmitTxn(Authid authid, Terminalid terminal, MerchantInfo merch);
  /**
   * Return a rate to bill the authorizer for the particular transaction in hundredths of a cent (4 decimal places) -- might need DB access
   * NOTE: 2 cents per transaction = 200 hundredths of a cent
   */
  public abstract int calculateTxnRate(TransferType tt, PayType pt, Institution in);
  public abstract int calculateSubmittalRate();
  // Overload to do different things based on what kind of gateway message it was [only the authorizer can determine that]
  abstract protected void logGatewayTransaction(PayMateDB db, AuthAttempt attempt);
  /**
   * made overloadable so that each auth can diddle the aurhrrn and such fields
   * @param db PayMateDB instance
   * @param record TxnRow of the force record
   * @return PaymentReply showing whether it succeeded or not (etc.)
   */
  abstract protected PaymentReply doForce(PayMateDB db, TxnRow record);
  // whether or not this authorizer accepts these kinds of txns ...
  abstract protected boolean accepts(TransferType tt);
  //
  // END extend
  /////////////////////////////////////

  private boolean down = true;
  public void down() {
    if(down == false){
      termAgents.Stop();
      down = true;
      clearIps();
      markStateChange();
    }
  }
  private void loadAllProperties() {
    // first load the base class's properties:
    try {
      // timeouts and ip's might need to be changed while live, so get them from the DB!
      timeout = dbd.getIntServiceParam(serviceName(),"timeout", timeout);
      submitTimeout = dbd.getIntServiceParam(serviceName(), "submitTimeout", submitTimeout);
      connectTimeout = dbd.getIntServiceParam(serviceName(), "connectTimeout", connectTimeout);
      // +++ put bufferSize into code and don't get from DB?
      bufferSize = dbd.getIntServiceParam(serviceName(),"bufferSize", bufferSize);
      // multiple IP's or domain names are separated with a space " "
      parseIps(ips      , dbd.getServiceParam(serviceName(),"connectIPAddress", "127.0.0.1:9999"), "AUTH");
      parseIps(settleIps, dbd.getServiceParam(serviceName(),"settleIPAddress" , "127.0.0.1:9999"), "SETTLE");
    } catch (Exception e) {
      dbg.Caught("Exception attempting to load properties: ", e);
    }
    // +++ put the next section [low/high ranges] into code instead of DB?
    // sequencer adjustment:
    long low = dbd.getLongServiceParam(serviceName(), "lowseq", 1); // sorta standard
    long high = dbd.getLongServiceParam(serviceName(), "highseq", 9999); // sorta standard
    sequenceRange.setBoth(low, high);
    // batchsequencer adjustment:
    low = dbd.getLongServiceParam(serviceName(), "lowbatchseq", 1); // sorta standard
    high = dbd.getLongServiceParam(serviceName(), "highbatchseq", 9999); // sorta standard
    batchSequenceRange.setBoth(low, high);
    // termbatchnum adjustment:
    low = dbd.getLongServiceParam(serviceName(), "lowbatchnum", 1); // sorta standard
    high = dbd.getLongServiceParam(serviceName(), "highbatchnum", 9999); // sorta standard
    batchNumberRange.setBoth(low, high);
    // then, load the extended classes properties
    try {
      loadProperties();
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
  }
  public void up() {
    if(down) {
      loadAllProperties();
      // then start it all up
      down = false;
      markStateChange(" loaded "+initStandin()+" standins.");
    }
  }

  public final boolean isUp() {
    return !down;
  }

  public LongRange sequenceRange = new LongRange();
  public LongRange batchSequenceRange = new LongRange();
  public LongRange batchNumberRange = new LongRange();

  /**
   * This function exists cause the authseq set on this txn was set by the AUTHORIZER, not the SETTLER, and we need one for settlement
   * +++ add a settleauthseq field to the txn table to put this in, and the settler can set it when it does the batch.
   * For now, this is a hack to fix the problem.
   * +_+ do we really need a long for something that is usually less than 1000?
   */
  public int squeezeAuthseq(long oldAuthSeq) {
    return (int)((oldAuthSeq % (sequenceRange.high() - sequenceRange.low())) + sequenceRange.low());
  }

  // parse ips
  private final void parseIps(MultiHomedSocketFactory factory, String ipstr, String text) {
    MultiHomedHostList list = new MultiHomedHostList();
    try {
      TextList ipNames = TextList.CreateFrom(StringX.replace(ipstr, " ", ","));
      dbg.WARNING(ipNames.asParagraph("Added ips ",","));
      if(ipNames.size()>0) {
        int preferred = 0;
        for (int i=ipNames.size();i-->0;) {
          // the first one is the preferred one!
          String thisip = ipNames.itemAt(i);
          if(StringX.NonTrivial(thisip)) {
            boolean ispreferred = (i == preferred);
            String nickname =
                this.serviceName() + "_" + text + "_IP#"+(i+1) +
                "_" + thisip + (ispreferred ? "_Preferred" : "");
            MultiHomedHost host = new MultiHomedHost(nickname);
            host.creationTimeoutMs = (int)connectTimeout;
            host.ipSpec = IPSpec.New(thisip);
            host.nickName=nickname;
            if (ispreferred) {
              list.prefer(host);
            } else {
              list.alternate(host);
            }
          } else {
            // +++ bitch
          }
        }
      } else {
        // +++ bitch
      }
      factory.Initialize(list); // you can call this as many times as you want, and it will reset the list
    } catch (Exception caught){
      dbg.Caught("failure while reading ip list - ", caught);
    } finally {
      dbg.WARNING(text+" ip addresses added for ["+ipstr+"]: "+list.toString());
    }
  }

  // +++ share the logic here between the client and server standin stuff
  //the client equivalent actually DOES the txn in teh equivalent code block, for the server that means markign the database records.
  protected final AuthResponse canStandin(PayMateDB db, AuthTransaction authtrans) {
    TxnRow record = authtrans.record;
    RealMoney txnamout=record.rawAuthAmount();
    //since we rarely fail on the per txn limits we almost always need this:
    LedgerValue total=db.getTtlStandinsForTerminal(record.terminalid());
//server has its own opinion of what can be stoodin.
//note that we don't get here unless we have already tried to send this type of tran to authorizer.
    if( ! record.isCredit() || record.isReversal() || record.isReturn() || record.isQuery() || record.isModify()) { // check to see if the txn type is standinable
      return AuthResponse.mkNoResponse();
    } else if( ! authtrans.slim.NonZero()){//this check keeps us from reporting 'exceeds limit of zero' type messages
      return AuthResponse.mkNoResponse();
    } else if( ! authtrans.slim.itemOk(txnamout)) { // check to see if the txn exceeds the max standin limit +++
      return AuthResponse.mkOverLimit(txnamout,authtrans.slim.perTxn(),"Standin");
    } else if( ! authtrans.slim.totalOk(txnamout, total)) { // check to see if the txn exceeds the max total standin limit
      return AuthResponse.mkOverLimit(total,authtrans.slim.total(),"Offline");
    } else if(record.hasAuthRequest() /* is a gateway request*/ &&
              (StringX.equalStrings(record.institution, CardIssuer.Unknown.Abbreviation()) ||
              record.paytype().is(PayType.Unknown))) {
      return AuthResponse.mkDeclined("Cannot standin unknown card: " + record.institution);
    } else {
      String authcode=record.txnid().toString();//no longer have six digit restriction
      dbg.ERROR("canStandin making approval:"+authcode);
      return AuthResponse.mkApproved(authcode);
    }
  }

  // This happens when the authorizer starts!  Not before or after!
  protected final int initStandin() {
    PayMateDB db = dbd.getPayMateDB();
    Txnid [ ] txnids = db.getStoodins(id);
    for(int i = txnids.length; i -->0;) {
      // now process it ...
      // 1) Get the record from ... txn
      TxnRow record = db.getTxnRecordfromTID(txnids[i]);
      if(record == null) {
        dbg.ERROR("standinProcess:RECORD = NULL!!!! VERY BAD !!!!");
        TextList mailtext = new TextList();
        mailtext.add("Please fix it ASAP!");
        mailtext.add(String.valueOf(record));
        PANIC("error selecting txn record for txn#: " + record.txnid(), mailtext.asParagraph());
      } else {
        // 2) if it is a reversal, get the original ... from txn
        TxnRow original = null;
        if(record.isReversal()) {
          original = db.getTxnRecordfromTID(new Txnid(record.origtxnid));
          // +++ @@@ check to see if we can still void the original (did someone else do it, etc.?)
          // +++ @@@ also, is it null?
        }
        // 3) Try to send it using your own thread.  (This handles everything for us.)
        Storeid sid=db.getStoreForTerminal(record.terminalid());
        Store store = StoreHome.Get(sid);
        MerchantInfo merch = db.getAuthMerchantInfo(record.terminalid(), record.authid());
        AuthTransaction tranner = genTransaction(record, original, sid, slim(store), merch);
        standin(db, tranner);
        dbg.ERROR(serviceName() + " loading "+i+"/"+txnids.length+" standin:\n" + record.toString());
      }
    }
    return txnids.length;
  }

  protected final void standin(PayMateDB db, AuthTransaction authTran) {
    dbg.WARNING("Standing in for txn: " + authTran.record.txnid()); //email notify happens elsewhere
    try {
      AuthTerminalAgent agent = getAgent(authTran.record.terminalid());
      agent.attemptLater(db, authTran.bumpCounter()); // no waiting involved
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  /**
   * put @param auth response into record, and update @param record into database
   */
  public boolean markDone(PayMateDB db, TxnRow record,AuthResponse auth){
    dbg.ERROR("markdone:"+auth);
    record.incorporate(auth);
//@todo: discover offline debit... either create BIN or at least put into a list of candidates. hmm, don't have cardType from bin any longer ...
    record.tranendtime = net.paymate.database.PayMateDBQueryString.Now();
    if(!StringX.NonTrivial(record.authendtime)) {
      record.authendtime = record.tranendtime;
    }
    return db.updateTxn(record);//    return db.stampAuthDone(record);
  }

  protected final PaymentReply doneLocally(PayMateDB db, TxnRow record,AuthResponse auth){
    if(markDone(db, record,auth)){
      return forTxnRow(record);
    } else {
      PANIC("doneLocally() shows DB corrupted pretty badly!");
      return null;
    }
  }

  /**
   * directly void the original in the database without going to an outside agency.
   */
  private PaymentReply doVoidLocally(PayMateDB db, TxnRow record, TxnRow original){
    // stamp original as voided if record was an approved void
    dbg.ERROR("About to void the txn record!");
    boolean did = (original != null) ? stampVoided(db, original) : true; // unfound gateways may pass
    return doneLocally(db, record,did?AuthResponse.mkApproved((original != null) ? original.approvalcode : ""):AuthResponse.mkDeclined("Couldn't void original"));
  }

  /**
   * all returns are approved
   */
  private PaymentReply doReturnLocally(PayMateDB db, TxnRow record){
    //@@@ validate message content!
    try {
      AuthTerminalAgent agent = getAgent(record.terminalid());
      agent.setNextSequence(db, record);
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return doneLocally(db, record,AuthResponse.mkApproved(record.txnid().toString()));
    }
  }

  // +++ put the modifications in here?
  private PaymentReply doLocally(PayMateDB db, AuthTransaction authTran){
    switch (authTran.record.transfertype().Value()) {
      case TransferType.Reversal: return doVoidLocally(db, authTran.record,authTran.original);
      case TransferType.Return:   return doReturnLocally(db, authTran.record);
      case TransferType.Sale:     return doReturnLocally(db, authTran.record);//+_+ move standin logic here! @todo: LOOK CLOSELY
    }
    return null;
  }

  // +++ eventually put on Store object (once the StandinLimit object is in the np.datra.sinet.bus package)
  private StandinLimit slim(Store store) {
    return new StandinLimit(store.silimit, store.sitotal);
  }

  private PaymentReply tryTxn(PayMateDB db, TxnRow record, Store store, TxnRow original, MerchantInfo merch) {
    AuthTransaction authTran = genTransaction(record, original, store.storeId(), slim(store), merch);
    if(processLocally(authTran)){//we can void (or approve or decline) the original record right here and skedaddle.
      dbg.WARNING("Processing internal to server");
      return doLocally(db, authTran);
    } else {
      dbg.WARNING("sending to authorizer");
      AuthTerminalAgent agent = getAgent(authTran.record.terminalid());
      agent.attemptNow(db, authTran);//return indicates fatal error@@@
      return replyFromResponse(db, authTran, original);//normal case for real auth
    }
  }

  public final ActionReply gateway(PayMateDB db, GatewayRequest gwr, AuthAttempt attempt) {
    // create the message to pass to the authterminal
    MerchantInfo mi = db.getAuthMerchantInfo(attempt.terminalid, id);
    GatewayTransaction gwtran = genGWTransaction(gwr.origMessage(), mi.authtermid);
    // find the authterminal to send it on
    AuthTerminalAgent agent = getAgent(gwr.terminalid);
    // send it
    dbg.WARNING("sending gateway request to authorizer");
    agent.attemptNow(db, gwtran); // +++ what to do with the return value?
    AuthorizerGatewayResponse ar = (AuthorizerGatewayResponse)(gwtran.response);
    GatewayReply ret = new GatewayReply();
    if(ar != null) {
      attempt.setAuthResponse(ar.rawresponse.rawValue().getBytes());
//      db.stampAuthAttemptDone(attempt); // only stamp the authattempt done if it worked! +++ later code it to stamp the time but say it failed
      if(EasyUrlString.NonTrivial(ar.rawresponse)) {
        ret.setOrigMessage(ar.rawresponse);
        ret.setState(ar.isApproved() ? ActionReplyStatus.Success : ActionReplyStatus.GarbledReply);
      } else {
        dbg.ERROR("ar.response is trivial!!");
        ret.setState(ActionReplyStatus.HostTimedOut);
      }
    } else {
      dbg.ERROR("ar is null!!");
      ret.setState(ActionReplyStatus.HostTimedOut);
    }
    db.stampAuthAttemptDone(attempt);
    logGatewayTransaction(db, attempt);
    return ret;
  }

  public final PaymentReply stampTxnGatewayed(PayMateDB db, TxnRow record, Storeid storeid, TxnRow original, MerchantInfo merch, PaymentRequest  request, PaymentReply reply) {
    // clone of 'authorize', but doesn't actually authorizer it.
    // instead, just stamp it as authorized based on the reply
    db.stampAuthStart(record);
    db.setSequence(id, record.terminalid(), StringX.parseInt(record.authrrn), record.txnid());
    markDone(db, record,reply.auth()); // finish it here
    // need to check to see if it was a void, and if so, void the original!
    if(record.isReversal() && reply.auth().isApproved() && (original != null /* unfound gateways may pass*/)) {
      stampVoided(db, original);
    }
    return reply;
  }

  /**
   * enqueues the request for authorization by this authorizer
   * this function receives the image of the txn record instead of a request object
   * only extend this function if you don't like the way it operates!
   * @param force indicates that the client already stood it in, so we HAVE to try to process it, even if it exceeds limits
   * ?what if the original txn is being transacted while we speak?  dunno.  suggestions welcome.
   * can only happen if original is standin, can't get info to DO reversal until original has been done or stoodin.
   */
  public final PaymentReply authorize(PayMateDB db, TxnRow record, Store store, TxnRow original, MerchantInfo merch) {
    PaymentReply areply = null;
    try {
      if(record.transfertype().is(TransferType.Force)) {
        // only CREDIT can be a force!
        if(record.paytype().is(PayType.Credit)) {
          areply = doForce(db, record);
        } else {
          areply = doneLocally(db, record, AuthResponse.mkDeclined("cannot force for type " + record.paytype().Image()));
        }
      } else {
        // WE NO LONGER ALLOW LOCAL VOIDS OF PENDING AUTHS!!! +++ code this to detect actioncode='U' and return an error message (fail the void)

        // if this is a void, need to check to see if the original txn is in the standin queue, and then deal with it.
        if (record.isReversal() &&
            termAgents.removeAny(original.terminalid(), original.txnid(),
                                 original.rawAuthAmount()) > 0) { //if found in queue
          dbg.ERROR("void txn before it got sent to authorizer");
          areply = doVoidLocally(db, record, original); // void before getting auth'ed in the first place.
        } else { //send normal thing to agency
          if(record.transfertype().is(TransferType.Authonly) &&
             !record.paytype().is(PayType.Credit)) {
            areply = doneLocally(db, record, AuthResponse.mkDeclined("cannot authonly for type " + record.paytype().Image()));
          } else {
            dbg.ERROR("normal attempt");
            areply = tryTxn(db, record, store, original, merch); //normal case
          }
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
      // +++ make a real one?  yell for help?
    } finally {
      if(areply == null) {
        String subject = "txn ["+record.txnid()+"] failed in a bad way!";
        println("About to send this email message: " + subject);//---
        PANIC(subject); // this shouldn't go to card systems
        areply = PaymentReply.Fubar("auth.auth null reply");
      }
      return areply;
    }
  }

  /**
   * @param authTran: state is:
   */
  private final PaymentReply replyFromResponse(PayMateDB db, AuthTransaction authTran, TxnRow original) {
    dbg.Enter("replyFromResponse");
    try {
      TxnRow record = authTran.record;
      AuthResponse response = authTran.response;
      dbg.ERROR("response = " + response);
      if( (response==null) || response.statusUnknown()) { // (probably timed out!) and what else should be stoodin?
        response = canStandin(db, authTran);
        if(response.isApproved()) {//stoodin mechanism did approval
          boolean newStandin = !record.wasStoodin();// !Bool.For(record.stoodin); // wrap this in TxnRow
          if(newStandin) { // don't write over an old standin
            //stan is no longer generated on server.
            record.setStoodin(true);
            if(!db.updateTxn(record)){ // stamp the database with the standin authcode & time
              PANIC("Stamping database for stoodin failed! txnid="+record.txnid);
            }
          }
          standin(db, authTran); // and put this into standin queue (the original gets removed)
          // send an email about this (if we didn't already somewhere else) ------
          // but only send this email ONCE!
          if(newStandin) {
            if(isUp()) {
              PANIC("STOODIN #" + record.txnid() + " [" +
                    authTran.socketOpenAttempts.value() + " sockopenattempts] " +
                    (authTran.response.isM4() ? " [CS M4 Try Again!]" : ""));
            }
          }
        } else { // otherwise, yell for help since this txn likely DID occur, and we want to void it or reverse it, which we will do manually in the short term.
          // +++ later, we will do automatic reversals; for now, decline it
          String subject = "cnxn failed! txn=" + record.txnid() +
              ". authsocketattempts=" + ((authTran != null) ?
                                         ""+authTran.socketOpenAttempts.value() :
                                         "NULL!");
          println(subject);
          PANIC(subject);
          return doneLocally(db, record, response);
        }
      } else { // properly auth'd
        // already done in AuthTerminalAgent.transact(), so don't do again
      }
      return forTxnRow(record);
    } catch (Exception ex) {
      dbg.Caught(ex);
      return null;
    } finally {
      dbg.Exit();
    }
  }

  /**
   * Remember that the thread entering here is the client thread.  Don't make it wait too long!  Just start all of the submittals and get out.
   */
  public boolean submit(PayMateDB db, Terminalid terminalid, boolean auto) {
    boolean ret = false;
    AuthSubmitTransaction txn = null;
    AuthTerminalAgent agent = null;
    try {
      MerchantInfo merch = db.getSubmitMerchantInfo(terminalid, id);
      txn = genSubmitTxn(id, terminalid, merch); // contents of this will have to be shared in this module (raped from PaymentechAuth) once commonalities are discovered.
      txn.auto = auto;
      if(txn == null) {
        PANIC("unable to generate submittal txns for terminalid=" + terminalid);
        ret = false;
      } else {
        if(isUp()) {
          agent = getAgent(terminalid);
          if (agent == null) {
            PANIC("unable to get agent for terminalid=" + terminalid);
            ret = false;
          } else {
            // run the submission to try to get the actioncode & authrespmsg back
            dbg.ERROR("submitting to agent: " + txn);
            ret = agent.submit(txn);
          }
        } else {
          PANIC(serviceName()+" unable to submit; authorizer down for terminalid=" + terminalid);
          ret = false;
        }
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      if(!ret && (txn!=null)) {
        db.finishBatch(txn, sequenceRange, (agent != null) ? agent.termbatchnumer : null); // in case it wasn't done already (checks to see if it is needed before doing it)
      }
      return ret;
    }
  }

  public boolean stampVoided(PayMateDB db, TxnRow original) {
    original.setVoided(true);
    return db.updateTxn(original);//db.setVoidFlag(original);
  }

  // we need to create an authattempt (for debugging purposes, to be done in different code elsewhere, later)
  public void logAuthAttempt(byte [ ] reqBytes, byte [ ] respBytes, AuthorizerTransaction tran) {
    if((tran instanceof AuthTransaction) && (reqBytes != null) || (respBytes != null)) {
      AuthTransaction ptt = (AuthTransaction)tran;
      net.paymate.database.PayMateDB db = net.paymate.database.PayMateDBDispenser.getPayMateDB();
      net.paymate.data.AuthAttempt attempt = new net.paymate.data.AuthAttempt();
      attempt.setAuthRequest(reqBytes);
      attempt.setAuthResponse(respBytes);
      attempt.txnid = ptt.txnid();
      attempt.authid = id;
      attempt.terminalid = ptt.terminalid();
      db.startAuthAttempt(attempt);
      db.stampAuthAttemptDone(attempt);
    }
  }

  // OPEN A SOCKET TO THE AUTHORIZER!
  public Socket openSocket(int operation, AuthorizerTransaction tran) {
    MultiHomedSocketFactory factory = null;
    int readTimeout = 0;
    switch(operation) {
      case AUTHOPERATION: {
        factory = ips;
        readTimeout = timeout;
      } break;
      case SETLOPERATION: {
        factory = settleIps;
        readTimeout = submitTimeout;
      } break;
      default: {
        return null;
      }
    }
    MultiHomedHost host = factory.currentHost();
    //open the socket, on any error block for the timeout time so as to reduce retry rate.
    Socket ret = host.open(readTimeout, true); // this readTimeout is really not true; this is our "whole operation" timeout, but whatever ... it is the 'soTimeout' in linux.
    if(ret == null){
      boolean justfailed=factory.thisFailed(host);
    } else {
      boolean justworked=factory.thisWorked(host);//on the client this would be a premature place to call this. one should actually get an interaction to complete before deeming the host as "working"
      tran.host = host;
    }
    return ret;
  }

  // +++ enumeration
  public final static int AUTHOPERATION = 0;
  public final static int SETLOPERATION = 1;

  /**
   * used for lists of these.
   */
  public boolean equals(Object o){
    if (o instanceof String) {
      return StringX.equalStrings(serviceName(), (String)o);
    }
    if (o instanceof Authid) {
      return this.id.equals(((Authid)o));
    }
    if (o instanceof Authorizer) {
      return this==(Authorizer)o;
    }
    return false;
  }

  public final boolean isAuthService() {
    return true;
  }

  protected void PANIC_println(String toPrint) {
    super.PANIC_println(toPrint);
    println(toPrint);
  }
}
//$Id: Authorizer.java,v 1.170 2004/04/15 04:31:13 mattm Exp $