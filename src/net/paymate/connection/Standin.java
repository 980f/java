package net.paymate.connection;

/**
 * @title        $Source: /cvs/src/net/paymate/connection/Standin.java,v $
 * @copyright    (C) 2000, Paymate.net
 * @version      $Revision: 1.134 $
 * @author       paymate.net
 * @created      July 4, 2001
 */

import java.io.*;
import net.paymate.Main;
import net.paymate.awtx.RealMoney;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.io.IOX;
import net.paymate.terminalClient.*;
import net.paymate.lang.ThreadX;
import net.paymate.lang.ReflectX;

public class Standin implements ConnectionCallback {
  private static ErrorLogStream dbg = ErrorLogStream.getForClass(Standin.class);
  private static ErrorLogStream batchDbg = ErrorLogStream.getExtension(Standin.class, "batch");
  private static ErrorLogStream dbs = ErrorLogStream.getExtension(Standin.class, "sort");

  public final static String offlineListing = "PARTIAL LISTING";

  /**
   *  whether standin is authorizing transactions
   */
  private boolean isActive=false;
  /**
   *  whether all operations, foreground or stoodin, occur in swipe time order.
   */
  private boolean strictOrder=true;
  /**
   *
   */
//  private boolean allowNonLocalModifications=false;
  /**
   * @return whether txn attempts should go out foreground channel
   * inputs: whether we think we have a connection to server and
   *   either not doing strict order or there is nothing in backlog and nothing on the wire.
   */
  public boolean online() {
    return!isActive && (!strictOrder || (backlog.isEmpty() && !inProgress));
  }

  /**
   *  the parent connection is used to get to the network
   *  we also get to tell it to exit standin mode.
   */
  ConnectionClient myParent;
  StoreInfo si; //for access to standin limits

  /**
   *  we have our own transaction stream
   */
  TxnAgent agent;
  TxnBacklog backlog;
  ReceiptBacklog recipes;
  /**
   *  we must interlock any code that modifies the number of objects backlogged.
   *  that means both putting in and taking out of backlog.
   *  putting in is done via register(object) functions,
   *  taking out is done by nextRequest()
   *  and looking at it all is done in status()
   */
  Monitor countlock;

  /////////////////////
//  state flags
  boolean loadedFromDisk = false;
  boolean inProgress = false; //used to keep us from getting txns and receipts out of preferred order

  /**
   * cache major part of "can we standin" reasoning
   */
  private static boolean disabled = false;

  private boolean amGateway() {
    return myParent.termInfo.isGateway();
  }

  /**
   * @param  myParent  needed for connection attributes
   */
  public Standin(ConnectionClient myParent) {
    if(dbg == null) {
      dbg = ErrorLogStream.getForClass(Standin.class);
    }
    if(batchDbg == null) {
      batchDbg = ErrorLogStream.getExtension(Standin.class, "batch");
    }
    if(dbs == null) {
      dbs = ErrorLogStream.getExtension(Standin.class, "sort");

    }
    dbg.Enter("making Standin:" + myParent.name());
    try {
      this.myParent = myParent;
      isActive = false;
      strictOrder = myParent.termInfo.strictlyOrder(); //+_+ should be the looser 'isExternal' which doesn't exist yet.
      countlock = new Monitor(myParent.name() + Standin.class.getName());
      //make another agent so that we can take foreground requests while
      //catching up on standins.
      agent = TxnAgent.Clone(myParent.connman, myParent.name() + ".BGND");
      agent.setBackgroundMode(true);
    } finally {
      dbg.Exit();
    }
  }

  /**
   *  whenever the state MIGHT change we make sure background is grinding away.
   *
   * @param  tothis  The new Standin value
   */
  public void setStandin(boolean tothis) {
    if(isActive && !tothis) {
      startBacklog(); //although we expect it to usually already be started.
    }
    isActive = tothis;
  }

  /**
   *  Sets the StandinRules attribute of the Standin object
   *
   * @param  si  The new StandinRules value
   */
  public void setStandinRules(StoreInfo si) {
    this.si = si;
    disabled = !si.slim.NonZero();
  }

  /**
   * @return    quantities of backlogged items.
   */
  public StandinStatus status() {
    try {
      countlock.getMonitor();
      return new StandinStatus(backlog.size(), recipes.size());
    } finally {
      countlock.freeMonitor();
    }
  }

  private void doLocalListing(BatchRequest br, BatchReply bratch) {
    bratch.isClosed = br.isClosing;
    bratch.setOffline(true);
    if(br.isClosing) {
      // can't close while in standin, but can print report of what is cached
      tryAgainLater(bratch, "Can only print, not close, while offline.");
    } else {
      Backlog batchBacklog = backlog.clone(batchDbg);
      ActionRequest arq = null;
      while( (arq = batchBacklog.next()) != null) {
        if(arq instanceof StoodinRequest) {
          StoodinRequest sir = (StoodinRequest) arq;
          if(sir.reply.Succeeded() && sir.reply.isApproved()) {
            BatchLineItem bli = blight(sir);
            if(bli != null) {
              bratch.addItem(bli);
            }
          }
        } else {
          batchDbg.WARNING("backlog has non-stoodin request!");
        }
      }
      bratch.status.setto(ActionReplyStatus.Success);
      bratch.ranger().include(UTC.Now());
//      bratch.isClosed=false; //we never close while in standin.
//      bratch.setOffline(true);
      bratch.Errors.add(offlineListing);
    }
  }

  private AuthResponse mkApproval(PaymentRequest frek) { //frek is ignored
    return AuthResponse.mkApproved(AuthResponse.DEFAULTAUTHCODE).setRefInfo(AuthResponse.DEFAULTRRN, "");
  }

  /**
   * @param  request  being attempted
   * @return null to make TxnThred try it for real, without leaving standing mode
   *  (called on TxnThread of parent client)
   */

  public ActionReply whileStandin(ActionRequest request) {
    dbg.Enter("whileStandin");
    ActionReply ar = null;
    try {
      ar = ActionReply.For(request);
      switch(request.Type().Value()) {
        case ActionType.gateway: {
          //let it percolate back up to terminal as a gateway reply with status "com not initiated".
          //terminal then can call back to here to get its stoodin financial reply
        }
        break;
        case ActionType.store: {
          //same as gateway?
          return null; //attempt reconnect before failing
        } //break;
        case ActionType.batch: {
          if(amGateway()) {
            return null; //act like we don't have any local storage
          } else {
            doLocalListing( (BatchRequest) request, (BatchReply) ar);
          }
        }
        break;
        case ActionType.clerkLogin: { /*always succeeds in standin*/
          LoginReply crep = (LoginReply) ar;
          crep.clerkCap.canREFUND = false;
          crep.clerkCap.canMOTO = false;
          ar.status.setto(ActionReplyStatus.SuccessfullyFaked);
        }
        break;
        case ActionType.receiptStore: {
          if(storeReceipt( ( (ReceiptStoreRequest) request))) {
            //if stores ok then
            ar.status.setto(ActionReplyStatus.SuccessfullyFaked);
          } else {
            //it wasn't saved in NV storage
            completeFailure(ar, "Couldn't save electronic copy of receipt, please print a store copy");
            //... but the above never seems to show anywhere... +_+
          }
        }
        break;
        case ActionType.payment: {
          dbg.VERBOSE("financial request");
          PaymentRequest frek = (PaymentRequest) request;
          PaymentReply fry = (PaymentReply) ar;
          dbg.VERBOSE("si limits:" + si.slim.spam());
          if(si.slim.NonZero()) { //must have non-trivial settings to perform standin
            StoodinRequest newsi = StoodinRequest.New(frek, fry);
            fry.setReference(frek.TxnReference());
            dbg.VERBOSE("financial request: " + frek.Type().Image());
            RealMoney amt = frek.Amount();
            switch(frek.OperationType().Value()) {
              case TransferType.Reversal: {
                if(!backlog.doLocalVoid( (PaymentRequest) request, (PaymentReply) ar)) { //change return to reflect we may have botched the operation
                  completeFailure(ar, "void may have failed");
                }
              }
              break;
              case TransferType.Modify: { //a sale that indirectly refers to another sale
                if(myParent.termInfo.canStandinModify()) {
                  if(si.slim.itemOk(amt)) { //we only check this limit, not the total
                    if(backlog.tryLocalModify(frek, fry)) {//
                      //side effect of above has marked record, it is ready for disk
                    } else {
                      //DECLINED, inside of tryLocalModify.
                    }
                  } else {
                    completeDecline(fry, AuthResponse.mkOverLimit(amt, si.slim.perTxn(), "si"));
                  } //we don't check total, too complex to deal with.
                } else {
                  completeFailure(fry, "not allowed while offline");
                }
              }
              break;
              case TransferType.Force:
              case TransferType.Authonly: //!this was missing until after storecron5!
              case TransferType.Sale: {
                if(frek.canStandin()) { //primarily looks at card type
                  fry.status.setto(ActionReplyStatus.SuccessfullyFaked);
                  //above is true regardless of approval
                  dbg.VERBOSE("checking:" + amt.Image());
                  if(si.slim.itemOk(amt)) { //true is good
                    dbg.VERBOSE("checking total:" + backlog.CentsOutstanding());
                    if(si.slim.totalOk(amt, backlog.total())) { //true is good
                      dbg.VERBOSE("total is small enough");
                      //we have attached modifier to the original on disk.
                      fry.setAuth(mkApproval(frek));
                    } else {
                      completeDecline(fry, AuthResponse.mkOverLimit(backlog.total().plus(amt), si.slim.total(), "SI"));
                    }
                  } else { //the names above and below must be less than 7 chars to appease the omni3200 error message length limit.
                    completeDecline(fry, AuthResponse.mkOverLimit(amt, si.slim.perTxn(), "si"));
                  }
                } else { //canStandin
                  completeFailure(fry, "system is offline");
                }
              }
              break;
              case TransferType.Return: {
                completeFailure(fry, "not allowed while offline");
              }
              break;
              default: {
                completeFailure(fry, "operation not understood");
              }
              break;
            } //end transfer type switch
            if(fry.Succeeded()) { //includes stoodin declines
              if(!storeTxn(newsi)) {
                //if it fails to be saved in NV storage
                dbg.VERBOSE("couldn't store txn");
                completeFailure(fry, "Couldn't save txn data for later processing");
                //regardless of approval
              }
            }
          } else { //slim is zero
            completeFailure(fry, "server is offline"); //this and the following decline have trivially different ...
          }
        }
        break;
        default: {
          //admin's
          //will send them on through, in case server has restarted already.
          return null;
        } // break;
      }
      return ar;
    } catch(ClassCastException cce) {
      dbg.ERROR("Request Class:" + ReflectX.shortClassName(request) +
                " Reply Class:" + ReflectX.shortClassName(ar));
      dbg.Caught(cce);
      return ActionReply.Bogus("unknown class"); //which will trigger error responses.
    } finally {
      dbg.Exit();
    }
  }

  /**
   *  caller is responsible for deciding what fault is worth standing in for.
   *  should !only! be called from foreground TxnThread of connection client
   *
   * @param  action  transaction for which communications failed
   * @return         possibly stoodin reply, else passback the failed reply
   */
  public ActionReply thisFailed(Action action) {
    dbg.Enter("thisFailed");
    try {
      if(disabled) {
        dbg.WARNING("standin is disabled!");
        //this is where we would go into 'rapid failure mode' if we choose to do so.
        startBacklog(); //to test line in background
        return action.reply;
      } else {
        setStandin(true);
        //usual start is here
        return whileStandin(action.request);
      }
    } finally {
      dbg.Exit();
    }
  }

  /**
   *  make next request
   *  @return whether a request is actually sent
   */
  public boolean sendNext() {
    ActionRequest chained = nextRequest();
    if(chained != null) {
      inProgress = true; //don't need to startBacklog() again.
      send(chained);
      return true;
    } else {
      return false;
    }
  }

  private void bedone(Action action) {
    try {
      if(action.request instanceof canBacklog) {
        dbg.VERBOSE("deleting standin file");
        Backlog.markDone( (canBacklog) action.request);
        if(action.request instanceof StoodinRequest) {
          backlog.onDone( (StoodinRequest) (action.request));
        }
      }
    } catch(Exception any) {
      dbg.Caught("ignoring", any);
    } finally {
      inProgress = false;
      sendNext(); //when previous is done
    }
  }

  /**
   *  respond to completion of an (trans)action
   * @param  action  in progress
   */
  public void ActionReplyReceipt(Action action) {
    dbg.Enter("backGroundReply");
    try {
      if(!Action.isComplete(action)) { //timeout consumed it
        dbg.VERBOSE("timedout/incomplete");
        return;
      }
      if(action.reply.Succeeded()) {
        dbg.VERBOSE("Successfully did an " + action.TypeInfo());
        bedone(action);
        setStandin(false); // set online
        net.paymate.terminalClient.Appliance.BroadCastTerminalCommand(TerminalCommand.GoOnline); // tell the appliance to place all terminals & the appliance online
      } else {
        String detail = action.reply.status.Image() + " on a " + action.Type().Image();
        if(action.reply.ComFailed()) {
          //better be due to timeout else we spam!
          //if failure was instant we need to sleep for awhile or we spam debuggers
          dbg.WARNING("anitspam sleep after getting" + detail);
          ThreadX.sleepFor(net.paymate.terminalClient.Appliance.txnHoldoff());
          send(action.request); //try again forever.
        } else {
          dbg.ERROR("permanently failed! " + detail);
          dbg.ERROR(String.valueOf(action.request));
          dbg.ERROR(String.valueOf(action.reply));
          //server now stores info on anything that it fails, we can drop it.
          bedone(action);
        }
      }
    } finally {
      dbg.Exit();
    }
  }
  public void extendTimeout(int millis){}//ConnectionCallback interface
  /**
   * @return sacrificial request to ensure something is being sent.
   * per 13nov02 discussion retract to simple admin request, let all updaterequests be from appliance objects only.
   */
  private ActionRequest primer() {
//    StandinStatus snap=status();
    return new AdminRequest();
  }

  /**
   *  start reducing the backlog.
   *  we must spawn the first request, but after that we will use chaining to get the rest
   */
  public void startBacklog() {
    dbg.Enter("startBacklog");
    try {
      if(!loadedFromDisk) {
        //runs once
        dbg.WARNING("first start");
        IOX.createDir(path());
        backlog = new TxnBacklog(path());
        recipes = new ReceiptBacklog(path());
        //@todo: turn on encryption here.
        loadedFromDisk = true;
        if(!backlog.isEmpty() || !recipes.isEmpty()) { //would like to send the next only when needed...
          send(primer()); //clear our throat, so to speak.
          //bugs historically like to destroy the first attempt. If this attempt
          //gets destroyed we don't lose data.
        }
      } else {
        StandinStatus snap = status();
        dbg.WARNING(snap.toSpam("at restart "));
        if(!inProgress) { //if not already sending something
          if(!sendNext()) { //try to start the flush
            send(primer()); //but if nothing to flush then ping
          }
        }
      }
    } finally {
      dbg.Exit();
    }
  }

  private void send(ActionRequest request) {
    request.setCallback(this);
    agent.Post(request);
  }

  public void Stop() {
    agent.shutdown();
  }

  /**
   * @return  request made from next item to send from standin
   */
  ActionRequest nextRequest() {
    dbg.Enter("nextRequest");
    try {
      countlock.getMonitor();
      dbs.VERBOSE("txns at next request", backlog.files);
      return(backlog.isEmpty()) ? recipes.next() : backlog.next();
    } finally {
      countlock.freeMonitor();
      dbg.Exit();
    }
  }

  ////////////////////
  // local storage

  /**
   * @param  receipt  to store for later sending to server
   * @return true if disk seems happy.
   */
  private boolean storeReceipt(ReceiptStoreRequest receipt) {
    dbg.Enter("storeReceipt");
    try {
      countlock.getMonitor();
      if(recipes.register(receipt)) {
        startBacklog(); //regardless of standin if there is anything in either storage area we start
        return true;
      }
      return false;
    } catch(Exception any) {
      return false;
    } finally {
      countlock.freeMonitor();
      dbg.Exit();
    }
  }

  /**
   * @return  path for standin storage for owning Terminal object
   */
  File path() {
    return Main.LocalFile(myParent.name());
  }

  /**
   * @param  rek to put on disk
   * @return true for success in saving
   */
  private boolean storeTxn(StoodinRequest rek) {
    dbg.Enter("storeTxn");
    try {
      countlock.getMonitor();
      if(backlog.register(rek)) {
        startBacklog(); //regardless of standin if there is anything in either storage area we start
        return true; //success
      }
      return false;
    } catch(Exception oops) {
      dbg.Caught(oops);
      return false;
    } finally {
      countlock.freeMonitor();
      dbs.VERBOSE("txns after storeTxn", backlog.files);
      dbg.Exit();
    }
  }

  /**
   * @param  rek  is prepared for conversion into a human readable line.
   * @return      a batch item from our txn storage
   */
  public static final BatchLineItem blight(StoodinRequest rek) {
    if(rek.voidme == null) {
      return BatchLineItem.MakeFrom(rek.request, rek.reply);
    } else {
      return null;
    }
  }

  /**
   *  complete a trnasaction marking it as failed
   *
   * @param  fry  reply to set
   * @param  why  error message for printer
   */
  private static final void completeFailure(ActionReply fry, String why) {
    dbg.VERBOSE("completeFailure: Unable to perform transaction: fry=" + fry + ", why=" + why);
    fry.Errors.Add(why);
    fry.status.setto(ActionReplyStatus.FailureSeeErrors);
  }

  static final void tryAgainLater(ActionReply fry, String why) {
    fry.Errors.Add(why);
    fry.status.setto(ActionReplyStatus.TryAgainLater);
  }

  static final void completeDecline(PaymentReply fry, AuthResponse why) {
    fry.setAuth(why);
    fry.status.setto(ActionReplyStatus.SuccessfullyFaked); //we faked a decline!
  }

  static final void completeDecline(PaymentReply fry, String why) {
    completeDecline(fry, AuthResponse.mkDeclined(why));
  }

  static final void succeed(ActionReply ar) {
    if(ar instanceof PaymentReply) { //created to fix voids while in standin
      PaymentReply fry = (PaymentReply) ar;
      if(fry.auth().statusUnknown()) { //make it be approved, for things that don't need to go to an auth in the first place
        fry.setAuth(AuthResponse.mkApproved("000000")); // client does "000000" where server does "999999"
      }
    }
    ar.setState(ActionReplyStatus.SuccessfullyFaked);
  }

}
//$Id: Standin.java,v 1.134 2004/03/10 00:36:34 andyh Exp $