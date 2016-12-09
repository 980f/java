package net.paymate.connection;

import java.io.*;
import net.paymate.Main;
import net.paymate.ISO8583.data.*;
import net.paymate.awtx.RealMoney;
import net.paymate.util.*;
import net.paymate.ISO8583.data.TransferType;
import net.paymate.terminalClient.Receipt;
import net.paymate.database.PayMateDB;

/**
*  Description of the Class
*
* @title        $Source: /cvs/src/net/paymate/connection/Standin.java,v $
* @copyright    (C) 2000, Paymate.net
* @version      $Revision: 1.60 $
* @author       paymate.net
* @created      July 4, 2001
*/
public class Standin implements ConnectionCallback {
  /**
  *  Description of the Field
  */
  protected boolean isActive = true;
  public boolean online(){
    return !isActive;
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
  boolean loadedFromDisk = false;

  private final static ErrorLogStream dbg = new ErrorLogStream(Standin.class.getName());
  private final static ErrorLogStream batchDbg = new ErrorLogStream(Standin.class.getName() + ".batch");
  /**
  * @param  action  contains request and reply that failed to get a good response from the server.
  *  we go into STANDin mode and MAY create a "successful" reply depending
  *  upon the details of the particular request.
  *  Else we will make a "failed" reply, but we always will return an object.
  */

  private static boolean disabled = false;

  /**
  * @param  myParent  needed for connection attributes
  */
  public Standin(ConnectionClient myParent) {
    dbg.Enter("making Standin:" + myParent.name());
    try {
      this.myParent = myParent;
      isActive = false;
      countlock = new Monitor(myParent.name() + Standin.class.getName());
      agent= TxnAgent.New(myParent,myParent.name()+".BGND");
    }
    finally {
      dbg.Exit();
    }
  }

  /**
  *  whenever the state MIGHT change we make sure background is grinding away.
  *
  * @param  tothis  The new Standin value
  */
  public void setStandin(boolean tothis) {
    if(isActive&&!tothis ){
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
  }

  /**
  *  Description of the Method
  *
  * @return    Description of the Returned Value
  */
  //  public int timeout() {
    //    return takeOverTime;
  //  }

  /**
  * @return    quantities of backlogged items.
  */
  public StandinStatus status() {
    try {
      countlock.getMonitor();
      return new StandinStatus(backlog.size(), recipes.size());
    }
    finally {
      countlock.freeMonitor();
    }
  }

  private void doLocalListing(BatchRequest br ,BatchReply bratch ){
    bratch.ranger.setFormatter(Receipt.Formatter());
    if (br.isClosing) {
      // can't close while in standin, but can print report of what is cached
      tryAgainLater(bratch,"Can only print, not close, while offline.");
    } else {
      Backlog batchBacklog = backlog.clone(batchDbg);
      ActionRequest arq = null;
      FormattedLineItem section=FormattedLineItem.winger("PARTIAL LIST");
      bratch.stuff(section);
      while ((arq = batchBacklog.next()) != null) {
        if (!(arq instanceof StoodinRequest)) {
          batchDbg.WARNING("backlog has non-stoodin request!");
        } else {
          BatchLineItem bli = blight((StoodinRequest) arq);
          if (bli != null) {
            bratch.addItem(bli);
          }
        }
      }
      bratch.status.setto(ActionReplyStatus.Success);
      bratch.ranger.include(Safe.Now());
      bratch.stuffHeader(section);
      bratch.close(myParent.termInfo.getNickName(), br.isClosing, br.clerk.Name());
      //+++ get the terminalName from somewhere and replace +++  better yet, do away with terminal names in the tranjour table
    }
  }

  /**
  * @param  request  Description of Parameter
  * @return          null to make TxnThred try it for real, without leaving standing mode
  *  called from TxnThread of parent client
  */

  public ActionReply whileStandin(ActionRequest request) {
    dbg.Enter("whileStandin");
    try {
      ActionReply ar = ActionReply.For(request);
      switch (request.Type().Value()) {
        case ActionType.stoodin:{
          //always attempt this
          //we get here if we are using a stoodin to try to wake up server
          return null;
        }
        //break;

        case ActionType.batch:{
          doLocalListing((BatchRequest) request,(BatchReply) ar);
        } break;
        case ActionType.clerkLogin: {/*always succeeds in standin*/
          LoginReply crep=(LoginReply) ar;
          crep.clerkCap.canREFUND=false;
          crep.clerkCap.canMOTO=false;
          ar.status.setto(ActionReplyStatus.SuccessfullyFaked);
        } break;
        case ActionType.receiptStore: {
          if (storeReceipt(((ReceiptStoreRequest) request))) {
            //if stores ok then
            ar.status.setto(ActionReplyStatus.SuccessfullyFaked);
          } else {
            //if it fails to be saved in NV storage
            completeFailure(ar,"Couldn't save electronic copy of receipt, please print a store copy","23");
            //... but the above never shows anywhere... +_+
          }
        } break;
        default: {
          //most cases
          if (request.isFinancial()) {
            dbg.VERBOSE("financial request");
            StoodinRequest inProgress = new StoodinRequest();
            FinancialRequest frek = (FinancialRequest) request;
            //cast FUE
            FinancialReply fry = (FinancialReply) ar;
            inProgress.add(frek);
            inProgress.add(fry);   //tid is Zero()
            //the reply's time must match the tranjour records trnastarttime.
            fry.tid = TransactionID.NewOffline(TransactionTime.forTrantime(frek.requestInitiationTime), inProgress.stanner((int) System.currentTimeMillis()), this.myParent.termInfo.id());
            dbg.VERBOSE("financial request: " + frek.Type().Image());
            switch (frek.OperationType().Value()) {
              case TransferType.ReEntry:
              //a sale that indirectly refers to another sale
              case TransferType.Sale: {
                if(frek.canStandin()){
                  RealMoney amt = frek.Amount();
                  fry.status.setto(ActionReplyStatus.SuccessfullyFaked);
                  //regardless of approval
                  if (amt.compareTo(si.slim.perTxn) <= 0) {
                    //true is good
                    dbg.VERBOSE("amount is small enough");
                    if (backlog.totalOk(si.slim.total)) {
                      //true is good
                      dbg.VERBOSE("total is small enough");
                      //approve must be set before attempting to store...
                      fry.setResponse("00");
                      fry.setApproval(inProgress.authful((int) frek.Amount().Value()));
                      if (!storeTxn(inProgress)) {
                        //if it fails to be saved in NV storage
                        dbg.VERBOSE("couldn't store txn");
                        completeFailure(fry,"Couldn't save txn data for later processing","19");
                        //regardless of approval
                      }
                    } else {
                      //reject
                      dbg.VERBOSE("over total:" + si.slim.total.Image() + " is > " + backlog.CentsOutstanding());
                      completeDecline(fry,"offline authorizer total.limit exceeded","61");
                    }
                  }
                  else {
                    dbg.VERBOSE("too big:" + amt.Image() + " exceeds " + si.slim.perTxn);
                    completeDecline(fry,"phone auth required, amount > store limit","61");
                  }
                } else {
                  completeDecline(fry,"temporarily unavailable","93");
                }
              } break;
              case TransferType.Reversal:{                //aka Voids
                backlog.doLocalVoid((ReversalRequest)frek,(ReversalReply)fry);
              } break;
              default: {
                completeDecline(fry,"temporarily unavailable","93");
              } break;
            }
            //end transfer type switch
          }
          else {
            //admin's
            //will send them on through, in case server has restarted already.
            return null;
          }
        }
        break;
      }
      return ar;
    }
    finally {
      dbg.Exit();
    }
  }


  /**
  *  caller is responsible for deciding what fault is worth standing in for.
  *  called from TxnThread of parent client
  *
  * @param  action  Description of Parameter
  * @return         Description of the Returned Value
  */
  public ActionReply thisFailed(Action action) {
    dbg.Enter("thisFailed");
    try {
      if (disabled) {
        dbg.WARNING("feature disabled!");
        return action.reply;
      }
      else {
        setStandin(true);
        //usual start is here
        return whileStandin(action.request);
      }
    }
    finally {
      dbg.Exit();
    }
  }


  /**
  *  Description of the Method
  *
  * @return    Description of the Returned Value
  */
  public void sendNext() {
    ActionRequest chained = nextRequest();
    if(chained != null){
      send(chained);
    }
  }

  private void bedone(Action action) {
    try {
      if(action.request instanceof canBacklog){
        dbg.VERBOSE("deleting file");
        Backlog.markDone((canBacklog) action.request);
      }
    }
    finally {
      sendNext();
    }
  }


  /**
  *  Description of the Method
  *
  * @param  action  Description of Parameter
  */
  public void ActionReplyReceipt(Action action) {
    dbg.Enter("backGroundReply");
    try {
      if(!Action.isComplete(action)){//timeout consumed it
        dbg.VERBOSE("timedout/incomplete");
        return ;
      }
      if (action.reply.Succeeded()) {
        dbg.VERBOSE("Successfully did an " + action.TypeInfo());
        bedone(action);
      }
      else {
        dbg.WARNING("failed," + action.reply.status.Image() + " on a " + action.Type().Image());
        if (action.reply.ComFailed()) {
          //better be due to timeout else we spam!
          //if failure was instant we need to sleep for awhile or we spam debuggers
          dbg.WARNING("sleeping so as to not spam server");
          ThreadX.sleepFor(net.paymate.terminalClient.Appliance.txnHoldoff());
          send(action.request); //try again forever.
        }
        else {
          dbg.ERROR("permanently failed!");
          dbg.ERROR(action.request.toString());
          dbg.ERROR(action.reply.toString());
          //server now stores info on anything that it fails, we can drop it.
          bedone(action);
        }
      }
    }
    finally {
      dbg.Exit();
    }
  }

  /**
  * @return sacrificial request to ensure something is being sent.
  */
  private ActionRequest primer(){
    StandinStatus snap=status();
    return new UpdateRequest(snap.txnCount,snap.rcpCount);
  }

  /**
  *  start reducing the backlog.
  *  we must spawn the first request, but after that we will use chaining to get the rest
  */
  public void startBacklog() {
    dbg.Enter("startBacklog");
    try {
      if (!loadedFromDisk) {
        //runs once
        dbg.WARNING("first start");
        Safe.createDir(path());
        backlog = new TxnBacklog(path());
        recipes = new ReceiptBacklog(path());
        loadedFromDisk = true;
        //would like to send the next only when needed...
        send(primer());
      } else {
        StandinStatus snap=status();
        dbg.WARNING(snap.toSpam("restart, counts("));
        sendNext();
      }
    } finally {
      dbg.Exit();
    }
  }

  private void send(ActionRequest request){
    request.setCallback(this);
    agent.Post(request);
  }

  public void Stop() {
    agent.Stop();
  }

  /**
  *  pick something from backlog and make a request for it
  *
  * @return    Description of the Returned Value
  */
  ActionRequest nextRequest() {
    dbg.Enter("nextRequest");
    try {
      countlock.getMonitor();
      return (backlog.isEmpty()) ? recipes.next() : backlog.next();
    }
    finally {
      countlock.freeMonitor();
      dbg.Exit();
    }
  }

  ////////////////////
  // local storage

  /**
  *  store a receipt
  *
  * @param  receipt  Description of Parameter
  * @return          true is disk seems happy.
  */
  boolean storeReceipt(ReceiptStoreRequest receipt) {
    dbg.Enter("storeReceipt");
    try {
      countlock.getMonitor();
      if (recipes.register(receipt)) {
        startBacklog();
        //regardless of standin if there is anything in either storage area we start
        return true;
      }
      return false;
    }
    catch (Exception any) {
      return false;
    }
    finally {
      countlock.freeMonitor();
      dbg.Exit();
    }
  }


  /**
  *  Description of the Method
  *
  * @return    Description of the Returned Value
  */
  File path() {
    return Main.LocalFile(myParent.name());
  }


  /**
  * @param  rek  on disk
  * @return      true for succes in saving
  */
  private boolean storeTxn(StoodinRequest rek) {
    dbg.Enter("storeTxn");
    try {
      countlock.getMonitor();
      if (backlog.register(rek)) {
        startBacklog();//regardless of standin if there is anything in either storage area we start
        return true;  //success
      }
      return false;
    }
    catch (Exception oops) {
      dbg.Caught(oops);
      return false;
    }
    finally {
      countlock.freeMonitor();
      dbg.Exit();
    }
  }

  /**
  * @param  rek  is prepared for conversion into a human readable line.
  * @return      a batch item from our txn storage
  */
  public static final BatchLineItem blight(StoodinRequest rek) {
    BatchLineItem bli = null;
    dbg.Enter("blight");
    try {
      // someday will also do checks... have to make two classes of BLitems.
      if (rek.request instanceof CardRequest) {
        if(rek.voidme==null){
          bli = new BatchLineItem();
          bli.card = ((CardRequest) rek.request).card.accountNumber;
          bli.date = rek.reply.refTime;
          bli.saleamount=rek.request.LedgerAmount().setFormat(BatchReply.batchmoney);
          bli.stan = rek.reply.tid.stan();
        }
      }
    }
    catch (ClassCastException oops) {
      // stub
    }
    finally {
      dbg.Exit();
      return bli;
    }
  }



  /**
  *  complete a trnasaction marking it as failed
  *
  * @param  fry  reply to set
  * @param  why  error message for printer
  * @param  rc   error code for clerk
  */
  private static final void completeFailure(ActionReply fry, String why, String rc) {
    dbg.VERBOSE("completeFailure: Unable to perform transaction: fry="+fry+", why="+why+", rc="+rc);
    fry.Errors.Add(why);
    fry.setResponse(rc);
    fry.status.setto(ActionReplyStatus.FailureSeeErrors);
  }

  static final void tryAgainLater(ActionReply fry, String why) {
    fry.Errors.Add(why);
    fry.setResponse("61");
    fry.status.setto(ActionReplyStatus.TryAgainLater);
  }

  static final void completeDecline(FinancialReply fry, String why, String rc) {
    fry.setApproval("Try Again Later");
    fry.Errors.Add(why);
    fry.setResponse(rc);
    fry.status.setto(ActionReplyStatus.SuccessfullyFaked);
  }

  static final void succeed(ActionReply fry){
    fry.setResponse("00");
    fry.setState(ActionReplyStatus.SuccessfullyFaked);
    fry.status.setto(ActionReplyStatus.SuccessfullyFaked);
  }

}
//$Id: Standin.java,v 1.60 2001/11/17 06:16:58 mattm Exp $
