/* $Id: PosTerminal.java,v 1.259 2001/11/17 00:38:35 andyh Exp $ */
package net.paymate.terminalClient;

import net.paymate.ivicm.Configure;

import net.paymate.data.*;
import net.paymate.jpos.Terminal.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.awt.*;
import net.paymate.connection.*;
import net.paymate.awtx.*;
import net.paymate.awtx.print.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;

import net.paymate.*;
import jpos.*;
import net.paymate.ISO8583.data.*;
import java.io.*;
import java.util.*;

/**
* terminal logic
*/
class PosTerminal implements ConnectionCallback,net.paymate.jpos.Terminal.Listener,QActor  {
  private ErrorLogStream dbg=new ErrorLogStream(PosTerminal.class.getName());
  final static String rev = "$Revision: 1.259 $";
  ////////////////////////////
  //
  public final boolean postClerkEvent(int clerkevent){//convenience function
    return Post(new ClerkCommand(new ClerkEvent(clerkevent)));
  }

  // the event sequencer:
  private QAgent incoming;//will extend this later with a class that has a gazillion specific Post's
  /**
  * @return true if object accepted.
  */
  public final boolean Post(Object arf){//MAIN access point
    return incoming.Post(arf); //not checking types yet
  }

//  private void chain(Object arf){
//    dbg.VERBOSE("chaining:"+QAgent.ObjectInfo(arf));
//    incoming.atFront(arf);//makes this most likely next to process
//  }

  //////////////
  // display refresh levels
  private static final int showAll=2;
  private static final int showChange=1;
  private static final int showNothing=0;

  private void shower(int showlevel){
    switch(showlevel){
      case showAll:     formIsStale=true;
      case showChange:  showAndTell();
      case showNothing: ;//leave HID's alone.
    }
  }
  //
  //////////////
//  boolean diewhendone=false;
  public void runone(Object todo){//implemments QActor
    dbg.Enter("Processing:"+Safe.ObjectInfo(todo));
    int shat=showChange;//0= do nothing, 1==run showandtell 2==reshowall
    try {
      //next three were once a 'jpos listener'
      if(todo instanceof net.paymate.jpos.Terminal.Event){
        shat=handle((net.paymate.jpos.Terminal.Event) todo);
      }
      else if(todo instanceof jpos.JposException){
        shat=handle((jpos.JposException) todo);
      }
      else if(todo instanceof jpos.events.ErrorEvent){
        shat=handle((jpos.events.ErrorEvent) todo);
      }
      // originally from clerk interface
      else if(todo instanceof ClerkIdInfo){
        shat=OnClerkId((ClerkIdInfo) todo);
      }
      else if(todo instanceof RealMoney){
        shat=OnSaleAmount((RealMoney) todo);
      }
      else if(todo instanceof Functions){
        shat=Handle((Functions) todo);
      }
      else if(todo instanceof PaySelect){
        shat=Handle((PaySelect) todo);
      }
      else if(todo instanceof TransferType){
        shat=setTransferType(((TransferType)todo).Value());
      }
      else if(todo instanceof EntrySource){
        sale.type.source.setto(((EntrySource)todo).Value());
        shat=1;
      }
      else if(todo instanceof SigningOption){
        shat=Handle((SigningOption) todo);
      }
      else if(todo instanceof ClerkCommand){
        shat=Handle((ClerkCommand) todo);
      }
      else if(todo instanceof Remedies){
        shat=Handle((Remedies) todo);
      }

      //        else if(todo instanceof ClerkEvent){//inside of ClerkCmmand
        //          shat=Handle((ClerkEvent) todo);
      //        }
      else if(todo instanceof Action){//only action source is response from server
        shat=Handle((Action) todo);
      }
      else if(todo instanceof StoreConfig){
        shat=acceptStoreConfig((StoreConfig)todo);
      }
      // ORIGINALLY DEBUG, COOPTED FOR INTERNAL USE AS WELL:
      else if(todo instanceof TerminalCommand){
        shat=Handle((TerminalCommand) todo);
      }
      else if(todo instanceof OurForm){
        shat=assertForm(((OurForm)todo).Id().Value());
      }
      else {
        dbg.ERROR("Unknown object"+Safe.ObjectInfo(todo));
        shat=2;
      }
    }
    catch(Exception any){
      dbg.Caught(any);
      shat=2;
    }
    finally {
      dbg.VERBOSE("shat is:"+shat);
      shower(shat);
      dbg.Exit();
    }
  }

  public void Stop(){//see QAgent
    //thread manager wants us to die.
  }

  private final int fetchSignature(String trigger){
    dbg.VERBOSE(" sig fetch caused by "+trigger);
//    if(tranny.is(FTstate.WaitSig)){
      dbg.WARNING("attempting to fetch signature");
      GOTO(FTstate.SigOnWire);
      JposException oops= jTerm.former.EndForm(); //and hope that creates events to move us along...
      if(oops!=null){
        dbg.ERROR("jpos exception on get signature:"+oops.getMessage());
      }
      //need to yank on JUST clerk interface to get "get sig" to go away...
      //if we change form on the enTouch we might lose the signature being fetched.
      return showChange;
//    }
//    else {
//      return showChange;
//    }
  }

  private final int Handle(SigningOption todo){
    dbg.VERBOSE("Handling SigningOption:"+todo.Image());
    if(tranny.is(FTstate.WaitSig)){
    switch(todo.Value()){
      case SigningOption.DoneSigning:{
        fetchSignature("clerk");
      } break;
      case SigningOption.SignPaper:{
        skipSignature();
      } break;
      case SigningOption.VoidTransaction:{
        lateCancel();
      } break;
    }
    }
    return showAll;
  }

  public void Handle(Event jte){  //legacy jpos listner
    Post(jte);
  }
  public void Handle(jpos.JposException jte){  // legacy jpos listner
    Post(jte);
  }
  public void Handle(jpos.events.ErrorEvent jape){  //  legacy jpos listner
    Post(jape);
  }

  ////////////////////////////
  //human related data sources and sinks
  private Wrapper jTerm; //human interfaces
  private PrinterModel printer;// a FreqUsed piece of jTerm

  /* exposed for IPTerminal */ ClerkUI clerkui; //sequential clerk data entry and display
  private IdMangler id=new IdMangler();
  private IPTerminal terminalServer=null;
  ///////////////////////////////////////
  // transaction agent
  private ConnectionClient connectionClient;
  // the reference to the appliance is needed so that a terminal can report a system wide problem.
  // we might move those functions to be static on Appliance.
  private Appliance myApple;

  ///////////////////////////////////////
  //configuration
  private Clerk clerk;
  /*package*/ TerminalInfo termInfo;
  public String id(){//const
    return termInfo!=null?""+termInfo.id():"";
  }
  private boolean autoSale=true;    //when ending one transaction start a regular sale, +_+ omve into term caps
  private TerminalCapabilities caps=new TerminalCapabilities();

  ////////////////////////
  //data accumulators
  private MSRData card;
  private PINData customerPin;
  private MICRData check;
  private CheckIdInfo checkId;
  private SigData sigdata;  //+_+ get a Hancock in here. NO! not until hancock defers parsing!!!.

  /////////////////////////
  //transaction modes
  private SaleInfo sale;
  private boolean offlinetranny=false;
  private boolean isaMoto=false;
  //orphan state flags (one's not integrated into the data they are flagging)
  private boolean customerAccepts=false;
  private boolean terminalLoggedin=false;//now just means 'message has been printed'
  private int loginRetries=0; //4debug
  private boolean checkIntended=false;
  private boolean idRequired=false;
  private boolean sigRequired=false;//cached from Request construction.
  private boolean amDying=false;

  private TransactionID lastTid= TransactionID.Zero();
  private FinancialReply lastFrep;

  private boolean needAnotherCopy=false;
  private ActionRequest tip;

  private FTstate tranny= new FTstate();//state of above action

  private void GOTO(int FTstatecode){//4debug
    GOTO(new FTstate(FTstatecode));
  }

  private void GOTO(FTstate next){//4debug
    dbg.VERBOSE("STATECHANGE:"+tranny.Image()+" => "+next.Image());
    dbg.ERROR("Card#tracer:"+card.toSpam());
    tranny.setto(next);
  }

  private ReceiptManager reception=new ReceiptManager();
  private Advert currentAdvert=new Advert();

  private final int PrintCurrentCoupon(){
    if(currentAdvert.hasCoupon()){
      String banner=Fstring.centered("COUPON",printer.textWidth(),'*');
      printer.println(banner);
      printer.print(currentAdvert.coupon);
      printer.println(banner);
    } else {
      //+_+ use FormattedItems
      TextList nocoupon=new TextList("No coupon available",printer.textWidth(),true/*word-wrap*/);
      printer.print(nocoupon);
    }
    printer.formfeed();
    return showAll;
  }

  //to allow for intelligent forms changes on otherwise stateless form manager:
  private OurForm currentForm;
  private boolean formIsStale=true;

  /**
  * abuses ClerkItem enumeration to describe card status
  */
  private final int evaluateCard(MSRData card){
    MSRData.spammarama(card,sale.wasSwiped());//outputs to MSRdata's debug stream.

    if(!MSRData.beenPresented(card))    {
      return ClerkItem.PaymentSelect;
    }
    if(card.accountNumber.isTrivial())  {
      return ClerkItem.CreditNumber;
    }
    if(!card.okAccount()){
      clerkui.preLoad(ClerkItem.BadCardNumber,card.accountNumber.Image());
      return ClerkItem.BadCardNumber;
    }
    if(card.expirationDate.isTrivial()) {
      return ClerkItem.CreditExpiration;
    }
    if(!card.okExpiry()){
      clerkui.preLoad(ClerkItem.BadExpiration,card.expirationDate.Image());
      return ClerkItem.BadExpiration;
    }
    if(MSRData.Cool(card,sale.wasSwiped())) {
      return ClerkItem.NeedApproval;
    }
    //something really bad must have happened
    return ClerkItem.PaymentSelect;
  }

  /**
   * if we have signature then all patron can do is wait for things to be over.
   */
  private final int sigorwait(){
    return sigRequired && sigdata.isTrivial() ? POSForm.SignCard : POSForm.WaitCompletion;
  }

/**
 * @return what should we be asking / showing patron?
 */
  private final int /*POSForm*/ NeedFromPatron(){
    if(amDying)             return POSForm.NotInService;
    if(!terminalLoggedin)   return POSForm.NotInService;
    if(!clerk.isLoggedIn()) return POSForm.ClerkLogin;
    switch(tranny.Value()){
      case FTstate.DoneBad:   return POSForm.SeeClerk;
      //the following is the state when admin Functions are active
      case FTstate.NoInfo:    return POSForm.ClerkLogin;
      case FTstate.WaitAuth:  return sigorwait();
      //wiatsig only happens wehn we ahve an auth and still need a sig
      case FTstate.WaitSig:   return POSForm.SignCard;
      //sigonwire happens when we are asking the enouthc to hand over a sig NOW.
      case FTstate.SigOnWire: return POSForm.WaitCompletion;
      case FTstate.DoneGood:  return POSForm.WaitCompletion;
      case FTstate.Incomplete: {//prompt people for enugh input to start one
        if(sale.type.op.is(TransferType.Reversal)){
          return POSForm.WaitApproval;//waitCompletion was confusing.
        }

        switch(sale.type.payby.Value()){
          default:{ //blow
            return POSForm.FubarForm;
          }
          case PayType.Debit:{
            if(sale.money.isValid()){
              if(customerAccepts){//this is set by entry of PIN
                return POSForm.WaitApproval;//should be approving already +_+
              } else {
                return POSForm.WaitPatronDebit;
              }
            } else {
              return POSForm.WaitClerkDebit;
            }
          } //break;

          case PayType.Credit:{
            int arf=evaluateCard(card);
            if(sale.money.isValid()){
              if(arf!=ClerkItem.NeedApproval){
                //something is wrong with card
                return POSForm.SwipeAgain;
              }
              if(customerAccepts){
                dbg.WARNING("@sigorwait should be authing");
                return sigorwait();//expect to be requesting something soon
              } else {
                return POSForm.WaitPatronCredit;
              }
            } else {
              return POSForm.WaitClerkCredit;
            }
          }

          case PayType.Check: {
            if(sale.money.isValid()){
              if(!id.isOk()){
                return POSForm.NeedID;
              }
              if(customerAccepts){
                return POSForm.WaitApproval;//should be approving already +_+
              } else {
                return POSForm.WaitPatronCheck;
              }
            } else {
              return POSForm.WaitClerkCheck;
            }
          }

          case PayType.Unknown:{
            return POSForm.IdleAd;
          }
        } //end switch paytype
      } //break; //case Inompletye
    }//end switch FTstate
    return POSForm.FubarForm;//--- hopefully the equivalent of elevator music ... (eg girl from iponema on an organ (sp))
  }

  private boolean expectingIDcard=false;

  private final void showBestForm(ClerkItem justasked){
    int /*POSForm*/ bestest;
    //below we are using "ClerkLogin" to lock patron out, should create a form for that.
    bestest= justasked.is(ClerkItem.SaleType)? POSForm.ClerkLogin : NeedFromPatron();//4debug
    assertForm(bestest);
  }

  private String wap="*-*-*";//4debug
  private final int wait(String why){
    clerkui.preLoad(ClerkItem.WaitApproval,wap=why);
    return ClerkItem.WaitApproval;//4inline return
  }

  private final int whyNotLoggedIn(){
    return ClerkItem.BootingUp;
  }

  private boolean waitadmin=false;
  private final int /*ClerkItem*/ NeedFromClerk(){//scan state and ask for most important item
    dbg.Enter("NeedFromClerk");
    try {
      if(amDying)           return ClerkItem.Problem; //which should alreahave the reason for death
      if(!terminalLoggedin) return whyNotLoggedIn();
      if(!clerk.isLoggedIn()){
        if(clerk.idInfo.NonTrivial()) return wait("Clerk Login");
        if(Safe.NonTrivial(clerk.idInfo.Name())) return ClerkItem.ClerkPasscode;
        return ClerkItem.ClerkID;
      }
      if(needAnotherCopy){//quick fix for delaying printout of 2nd copy.
        return ClerkItem.SecondCopy;
      }
      if(waitadmin){
        return ClerkItem.WaitAdmin;
      }
      switch(tranny.Value()){
        case FTstate.DoneBad:   return ClerkItem.Problem; //which will be "declined" or such
        //the following is the state when admin Functions are active
        case FTstate.WaitAuth:  return wait("sale/return/void");
        //wiatsig only happens wehn we ahve an auth and still need a sig
        case FTstate.WaitSig:   return ClerkItem.NeedSig;
        //sigonwire happens when we are asking the enouthc to hand over a sig NOW.
        case FTstate.SigOnWire: return ClerkItem.ApprovalCode;
        case FTstate.DoneGood:  return ClerkItem.ApprovalCode;
        case FTstate.NoInfo:    return ClerkItem.SaleType;//aka "functions"
        case FTstate.Incomplete: {//prompt people for enugh input to start one
          if(sale.type.op.is(TransferType.Unknown)){//should ne in NoInfo state
            return ClerkItem.SaleType;
          }
          if(offlinetranny&& !sale.isOffline()){
            return ClerkItem.PreApproval;
          }
          if(sale.type.op.is(TransferType.Reversal)){
            return ClerkItem.RefNumber;
          }
          if(sale.type.op.is(TransferType.ReEntry)){
            return ClerkItem.RefNumber;//coincidentally same as for Reversals...don't merge
          }
          if(!sale.money.isValid()) {
            return ClerkItem.SalePrice;
          }
          if(!sale.type.isComplete()) {
            return ClerkItem.PaymentSelect; //+_+ crude
          }
          if(MSRData.beenPresented(card)){
            int eval=evaluateCard(card);
            if (eval!=ClerkItem.NeedApproval){
              return eval;
            }
            if(!customerAccepts)    {
              return ClerkItem.NeedApproval;
            }
            return wait("card approval"); //+_+ shouldn't be able to get here if actor is idle
          }
          if(check.isPresent) {
            if(!check.TransitOk())  return ClerkItem.CheckBank; //a misnomer
            if(!check.AccountOk())  return ClerkItem.CheckAccount; //may insist on rescan if >1
            if(!check.SerialOk())   return ClerkItem.CheckNumber; //may insist on rescan if >1
            //          urg.Message("Picking question idOk is:"+id.isOk());
            if(!id.isOk())          return ClerkItem.License;//+_+ no state input yet
            return wait("check approval"); //+_+ shouldn't be able to get here if actor is idle
          }
          dbg.ERROR("Unknown sale state");
        } break;
      }
      dbg.ERROR("NC: tranny="+tranny.Image()+sale.spam());
      showProblem("PleaseStart Over",tranny.Image());//first text must be 16 chars..
      return ClerkItem.Problem;
    }
    finally {
      dbg.Exit();
    }
  }

  private final ClerkItem askBestQuestion(){
    int quid=NeedFromClerk();
    ClerkItem clrkItem = new ClerkItem(quid);
    Inform("Asking for: " + clrkItem.Image() + "[" + quid + "] / " + clerkui.QuestionFor(quid).prompt);

    switch(quid){  //some questions get modified before being asked each time:
      case ClerkItem.SalePrice:{//modify prompt for type of sale
        clerkui.askInPrompt(quid,sale.amountHint(),theSaleAmount().Image());
      } break;
      case ClerkItem.ApprovalCode:{
        clerkui.askInPrompt(quid,lastFrep.Approval(),lastTid.stan());
      } break;
      case ClerkItem.RefNumber:{
        if(lastTid.isComplete()){
          clerkui.preLoad(quid,lastTid.stan());//dynaprompt didn't seem to work.
        }
        clerkui.ask(quid);
      } break;
      //+_+ move other askInPormpts to here.
      default: clerkui.ask(quid);  break;
    }
    dbg.VERBOSE("Just set clerkui to ask [" + clrkItem.Image()+"] "+clerkui.WhatsUp());
    return clrkItem;
  }

  private ClerkItem lastAsked;
  private final void showAndTell(){//just about always called these guys together:
    lastAsked= askBestQuestion();
    showBestForm(lastAsked);
  }

  private final void manCheck(int cci,String micrfield){
    switch(cci){
      case ClerkItem.CheckBank:     check.Transit=micrfield; return;
      case ClerkItem.CheckAccount:  check.Account=micrfield; return;
      case ClerkItem.CheckNumber:   check.Serial= micrfield; return;
    }
  }

  //////////////////////////////////////////////////////
  private final int printStoreCopy(){
    reception.rePrint(printer);//cheap fix to get duplicate
    needAnotherCopy=false;
    return onCancel();//and return to base state.
  }

  private static final String NameOfGod="ADM";

  /**
  * process a clerk login attempt
  */
  private final int OnClerkId(ClerkIdInfo cid){//
    clerk=new Clerk();
    if(cid.Name().equals(NameOfGod) && CastSpell(Safe.parseLong(cid.Password()))){
      //should leave some trace behind, don't you think?
      return showAll;
    } else {
      clerk.idInfo=cid;
      if(!VerboseSpawn("Clerk Id",new LoginRequest())){
        clerk.idInfo.killPassword();//stops infinite approvla at DrawerClosing.
      }
    }
    return showChange;
  }

  private final void lostReceipt(){
    showProblem("Rcpt not saved","print one 2 keep");
    needAnotherCopy=true;
  }

  private final int lateCancel(){//cancel after tranny might have been sent
    //we agreed to merge with following void    finishReceipt(false);//push paper so far
    issueVoid(lastTid,false);  //we are trusting that this question is only asked at appropriate times
    return showAll;
  }

  /**
  * erase any data related to patron.
  */
  private final void clearCustomerData() {
    customerAccepts=false;
    card.Clear();
    check.Clear();
    id.Clear();
    customerPin.Clear();
  }

/**
 * @return can we blow off what we are doing without financial ambiguity?
 */
  private boolean cancellable(){
    switch (tranny.Value()) {
//      case FTstate.NoInfo:
//      case FTstate.Incomplete:
//      case FTstate.DoneBad:
//      case FTstate.DoneGood:
      default: return true;//powerup , and otherwise hope to survive

      case FTstate.WaitSig: //must use lateCancel
      case FTstate.SigOnWire://ditto
      case FTstate.WaitAuth:
      return false;
    }
  }

  private final int onCancel(){
    dbg.VERBOSE("onCancel");
    if(cancellable()) {//former decision was meaningless 'waiting for any kind of response'
      //BUT KEEP RECEIPT
      clerkui.Clear();
      sale.Clear();
      clearCustomerData();
      sigdata.Clear();
      if(autoSale){
        normalSale();
      } else {
        NoSale();
      }
      return showAll;
    } else {
      dbg.ERROR("Can't cancel right now! FTstate="+tranny.Image());
      //here we should print something educational, as to why we are waiting and
      // how much longer it might take.
      return showAll;
    }

    /** this place is chosen for the garbage collection becuase: <ol>
    *  <li> we get here when the clerk panics
    *  <li> we get here after every transaction, at which time we have discarded
    *       most of the data related to that transaction.
    *  <li> we can invoke it via just about any interface in the system.
    *  </ol>
    */
    // all gc()'s removed. we try to not generate garbage in the first place now.
    //    dbg.VERBOSE("gc() by posterminal");
    //    System.gc();
  }

  private final int onAmountOk(){// customer sez ok
    dbg.VERBOSE("Amount Ok'd");
    customerAccepts=true;
    return attemptTransaction(); //send it off to have it approved
  }

  private final int onAmountOk(String passcode){//clerk is stating that customer ok's
    dbg.VERBOSE("onAmountOk:"+passcode);
    if(myApple.myInfo.cfg.termcap.freePass() || clerk.Passes(passcode)){
      return onAmountOk();
    } else {
      dbg.VERBOSE("clerk ok'ing amount ignored.");
      return showAll;//clerk is impatient with patron
    }
  }

  private final RealMoney theSaleAmount(){//fue
    return sale.Amount();
  }

  ///////////////////////////////////////////
  private static final MajicEvent MajicPrices[]={//static: all terms share operations list.
    new MajicEvent(2668L,TerminalCommand.Reconnect), //boot
    new MajicEvent(5455L,TerminalCommand.Shutdown),  //kill
    new MajicEvent(18438L,TerminalCommand.Quiet),   //quiet
    new MajicEvent(74688L,TerminalCommand.Shout),  //shout
    new MajicEvent(7663L,TerminalCommand.Pond),   //pond==ip terminal on
    new MajicEvent(7633L,TerminalCommand.Poff),  //poff==ipterminal off
    new MajicEvent(783L,TerminalCommand.Reload),  //update root
    new MajicEvent(273L,TerminalCommand.Reinstall),  //update all
    new MajicEvent(946L,TerminalCommand.Identify),  //who
  };

  private final boolean CastSpell(long key){
    //+_+ sure would be nice if hash tables had a decent lnline initializer..
    //..I would still probably wrap the lookup tho'
    for(int i=MajicPrices.length;i-->0;){
      MajicEvent spell=MajicPrices[i]; //fue
      if(spell.isEventFor(key)){
        Appliance.BroadCast(spell);
        return true;
      }
    }
    return false;
  }

  private final int OnSaleAmount(RealMoney cents){//from clerkui,
    if(cents.Value()==0){
      clerkui.gotoFunction();
      return showChange;
    } else {
      sale.money.setAmount(cents);
      if(caps.autoApprove() && !sale.type.payby.is(PayType.Debit)){
        return onAmountOk();//which will also attemptTranscaction
      } else {
        return attemptTransaction();
      }
    }
  }

  private final int setTransferType(int /*TransferType*/ tft){

    offlinetranny=false;
    isaMoto=false;
    clearCustomerData();//fixes "card number used on next transaction" bug.

    if(tft==TransferType.Unknown){
//      customerAccepts=false;
//      offlinetranny=false;
//      isaMoto=false;
//      clearCustomerData();//fixes "card number used on next transaction" bug.
      GOTO(FTstate.NoInfo);
    } else {
      GOTO(FTstate.Incomplete);
    }
    sale.type.op.setto(tft);
    return showChange;
  }

  private final void startPinEntry(){
    jTerm.cardReader.Flush();//et1k returns EC error code if swiping is enabled
    jTerm.pinEntry.Acquire(PINPadConst.PPAD_MSG_AMOUNTOK,card.accountNumber,theSaleAmount().Value());
  }

  private final void freshReceipt(FinancialRequest request){
    reception.start(request,printer,termInfo,clerk.idInfo);
    sigRequired=request.getsSignature();
  }

  private final void issueVoid(TransactionID oldid,boolean fresh){
    ReversalRequest voider=new ReversalRequest(oldid);
    if(!VerboseSpawn("sending reversal", voider)){
      Inform("Reversal not sent!");
    } else {
      if(fresh){
        freshReceipt(voider);
      } else {
        //adding to partial receipt. (lateCancel)
        sigRequired=false;//per 20011012 discussion with RWT
        reception.rip.setItem(voider);
      }
    }
  }

  /**
  * originally called when doing voids, now called with any retrospective transaction
  */
  private final int onVoid(String tidStr){//needs new name!!!
    dbg.Enter("onVoid");
    try {
      TransactionID oldid=TransactionID.New(tidStr);//clerk entered image of refnum.
      switch(sale.type.op.Value()){
        case TransferType.Reversal:{
          issueVoid(oldid,true);
        } break;
        case TransferType.ReEntry:{
          //+++ get pre auth, get new amount, get manager passcode...
          showProblem("NOT PERMITTED","see manager");
        } break;
      }//end switch
      return showChange;
    } catch (Exception caught) {
      dbg.Caught(caught);
      return showAll;
    } finally {
      dbg.Exit();
    }
  }

  private final ActionRequest VoidForReply(FinancialReply reply){
    return new ReversalRequest(reply.tid);
  }

  private final int interpretReply(FinancialReply reply){
    dbg.Enter("interpretReply");
    try{
      reception.onReply(reply);
      if(reply.Response.isApproved()){
        if(sigRequired){
          dbg.VERBOSE("signing is req'd");
          if(!sigdata.isTrivial()){//signed before approavl received.
            dbg.VERBOSE("storing signature, presigned");
            return finishReceipt(true);
          } else {
            dbg.VERBOSE("and we will wait for it.");
            GOTO(FTstate.WaitSig);
          }
        } else {
          return finishReceipt(false);//implies we store VOID receipts...along with checks.
        }
      } else {
        dbg.ERROR("REJECTED:"+reply.Approval());
        clerkui.onRejected(reply.Approval());//should have the action code in it
        GOTO(FTstate.DoneBad); //this controls the human views
        reception.unsigned();
      }
      dbg.VERBOSE("nothing more to do");
      return showChange;
    }
    catch(Exception arf){
      dbg.Caught(arf);
      return showAll;
    }
    finally {
      dbg.Exit();
    }
  }

  private final int onVoidReply(ReversalReply reply){
    dbg.VERBOSE("in onVoidReply");
    return interpretReply((FinancialReply)reply);
  }

  private final int finishReceipt(boolean signed){//store to server.
    try {
      if(signed){
        reception.signed(sigdata);
      } else {
        reception.unsigned();
      }
      if(!VerboseSpawn ("store receipt",new ReceiptStoreRequest(reception.Receipt(),lastTid))){
        lostReceipt();//i.e. the receipt was not sent to the server.
      }
    } finally {
      dbg.VERBOSE("finishReceipt: FTState = " + tranny.Image() + ", beDoneIfApproved = " + caps.beDoneIfApproved());
      if(caps.beDoneIfApproved()){
        onCancel(); //on receipt SENT
      } else {
        GOTO(FTstate.DoneGood);
      }
      return showAll;
    }
  }

  private final int onSignature(SigData siggy){
    sigdata=siggy;
    dbg.VERBOSE("sig received while:"+tranny.Image());
    switch(tranny.Value()){
      case FTstate.WaitAuth://sig before approval.
        return showChange;
      case FTstate.SigOnWire:
      case FTstate.WaitSig:{//signed after approval, will be 'abend' if declined
        return finishReceipt(true);
      }
    }
    return showChange;
  }

  private final int onBadSignature(){
    if(tranny.is(FTstate.SigOnWire)){
      GOTO(FTstate.WaitSig);
      return showAll;
    } else {
      return showChange;
    }
  }

  private final int skipSignature(){//clerk is skipping sig cap step
    try {
      if(tranny.is(FTstate.WaitSig)){
        finishReceipt(false);
        needAnotherCopy=true;
      }
    } finally{
      return showAll;
    }
  }

  private final void saleIfUnknown(){
    if (sale.type.op.is(TransferType.Unknown)) {
      sale.type.setto(new TransferType(TransferType.Sale));
    }
  }

  private final int onPaymentPresented(int paytype,boolean notManual,int entrysource){
    sale.type.setto(new PayType(paytype)).setto(new EntrySource(notManual ? entrysource : EntrySource.KeyedIn));
    saleIfUnknown();
    return attemptTransaction();
  }

  private final int onCheck(MICRData theScan, boolean reallyScanned){
    check=theScan;
    check.isPresent=true;//+_+ expected the scan to be marked already
    clerkui.autoEnterIf(ClerkItem.SalePrice);
    return onPaymentPresented(PayType.Check,reallyScanned,EntrySource.MICRed);
  }

  private final int startSomeSale(int paytype,  boolean manualling){
    try {
      sale.type.setto(new PayType(paytype));
      if(manualling){
        manHandled();
        saleIfUnknown();
      }
    } catch(NullPointerException npe){
      //we get these during startup, and don;t care if this function works anyway.
      dbg.WARNING("NPE in startSomeSale");
    } finally {
      return showChange;
    }
  }

  private final int startCreditSale(boolean manly){
    if(manly){
      card.Clear();
      card.beenPresented(true);
    }
    return startSomeSale(PayType.Credit,manly);
  }

  private final int startDebitSale(){
    return startSomeSale(PayType.Debit,false); // alwasy auto
  }

  private final int startCashSale(){
    return startSomeSale(PayType.Cash,true); //always manual (data entry)
  }

  private final int startCheckSale(boolean manly){
    if(manly){
      check.Clear();
      check.isPresent=true; //back door to faking it.
    }
    idRequired=caps.AlwaysID();
    return startSomeSale(PayType.Check, manly);
  }

  private final int onCard(MSRData theSwipe,boolean reallySwiped){
    card=theSwipe;
    card.ParseFinancial();
    dbg.VERBOSE("Mod10 check:"+Mod10.spam(card.accountNumber));
    return onPaymentPresented(PayType.Credit,reallySwiped,EntrySource.Swiped);
  }

  private final int onApproval(FinancialReply reply,FinancialRequest request){
    dbg.VERBOSE("in onApproval");
    return interpretReply(lastFrep=reply);
  }

  private final void showProblem(String one,String two){
    GOTO(FTstate.DoneBad);
    //16 is 'displayWidth' +_+
    clerkui.showProblem(Fstring.centered(one,16,'*'),two);
  }

  private final int onFailure(Action action){//return possible remedial request
    try {
      ActionReplyStatus why=action.reply.status; //fue
      switch(why.Value()){
        default: {
          showProblem("NETWORK ERROR",action.reply.status.Image());//alh:even if the error is from an admin operation.
        } break;
        case ActionReplyStatus.InvalidLogin:{
          showProblem("INVALID LOGIN","Type more Slowly");
          clerk.Clear();//else locks with "APPROVING..."
        } break;
        case ActionReplyStatus.InvalidTerminal:{
          showProblem("CALL FOR SERVICE","Try Cycling Power");
          //          return null;
        } break;
      }
      reception.fault(action.reply,printer);
      switch(action.request.Type().Value()){//tiny failure demons go here
        case ActionType.unknown:{ //if this occurs on certain reply types we lock!
          //+++++ gotta do something to keep from infinite looping...
        } break;
        case ActionType.clerkLogin:{//must clear else we get infinite "approving...Clerk Login loop"
          clerk.Clear();
        } break;
      }
      return showAll;
    } finally {
      onCancel();//GAWD not having this was a big bug! we were dependent upon a clerk pressing CLEAR to do this.
    }
  }


  /**
  * @param reply is the cfg from a ConnectionReply
  */
  private final int acceptStoreConfig(StoreConfig reply){
    dbg.Enter("StoreConfig");
    try {
      //4 configuration debug
      clerkui.preLoad(ClerkItem.ClerkID,termInfo.getNickName());
      clerkui.preLoad(ClerkItem.ClerkPasscode,""+termInfo.id());
      //end configuration debug
      connectionClient.setStoreInfo(reply.si);
      id.setLocale(reply.si.State);
      caps= reply.termcap;
      //rework some forms:
      String realHeader = reply.receipt.Header; //Safe.unescapeAll() put into transport layer
      OurForms.applyStoreInfo(realHeader);
      FormSetUp("On store config");
      // set receipt info
      Receipt.setOptions(reply.receipt,reply.si.timeZoneName);

      if(!terminalLoggedin){//show terminal stuff
        try {
          Receipt loggerin=new Receipt();
          loggerin.setItem(reply,termInfo,connectionClient.online());
          loggerin.print(printer,0);
        } catch (Exception t) {
          // don't die if this fails (the printout is just for testing, anyway)
        }
      }
      terminalLoggedin=true;
      onCancel();//+_+ can we afford to delete this line?
      return showAll;
    }
    finally {
      dbg.Exit();
    }
  }

  private final void showIdentity(){
    Inform(termInfo.getNickName());//+++ finish terminfo spam and use it here
    clerkui.flash(termInfo.getNickName());
    selectForm(POSForm.ClerkLogin);
    net.paymate.terminalClient.IviForm.Legend identifier=
    new net.paymate.terminalClient.IviForm.Legend(1,1,termInfo.getNickName(),"1");
    jTerm.former.displayLegend(identifier);
    ThreadX.sleepFor(10.0);//in case some other event is about to fire
  }

  /**
  * @return true if check data can be accepted
  */
  private final boolean readyForCheck(){
    return tranny.is(FTstate.Incomplete);//very pessimistic for now.
  }

  ///////////////////////////////////////////////////////////////////
  boolean amTransacting(){
    switch (tranny.Value()){
      case FTstate.WaitAuth  :
      case FTstate.WaitSig   :
      case FTstate.SigOnWire :
      case FTstate.DoneGood  :
      case FTstate.DoneBad   :
        return true;
      case FTstate.NoInfo    :
      case FTstate.Incomplete:
      default:
        return false;
    }
  }

  private final int attemptTransaction() {//a financial transaction, not an admin.
    try {
      dbg.Enter("attemptTransaction");
      TextList info=new TextList();
      if(amTransacting()){
        Inform("Already Transacting");
        return showChange;
      }
      else
      if(!clerk.idInfo.NonTrivial()){
        Inform("Clerk Id Info missing?");
        return showChange;
      }
      else
      if(!sale.type.isComplete()){
        Inform("What type of sale???");
        return showChange;
      }
      else
      if(!sale.money.isValid()){
        Inform("Sale amount not set");
        return showChange;
      }
      else
      if(!customerAccepts){
        Inform("Waiting on customer approval");
        return showChange;
      }
      else { //look deeper
        ActionRequest request;
        switch(sale.type.payby.Value()){
          default: {
            Inform("Not Yet Implemented!");
          } return showChange;
          case PayType.Debit: {
            if(!MSRData.Cool(card,sale.wasSwiped())){
              Inform("Card data missing, invalid, or incomplete");
              return showChange;
            }
            //--- delete when we can send requests upon approval...under test
            if(!sale.wasSwiped()) {//card but not swiped ...
              info.add("Debit Must Be Swiped");
              //security: (make sure swipe data erased if clerk does any manual entry)
              card.Clear();
              return showChange;
            }
            if(!customerPin.NonTrivial()){
              info.add("NeedPin");
              return showChange;
            }
            Inform("Preparing Debit request");
            request=new DebitRequest(sale,card,customerPin);
          } break;
          case PayType.Credit: {
            if(!sale.wasSwiped()) {//card but not swiped ...
              info.add("Manual Card request");
              //security: (make sure swipe data erased if clerk does any manual entry)
              card.clearTracks();
              //proceed
            } else {
              dbg.VERBOSE("card age is:"+card.age());
//              //we decided to not insist on fresh swipes
//              if(card.age()>Ticks.forSeconds(29.3)){
//                Inform("Card stale, swipe again");
//                card.Clear();
//                return showAll;//kick both interfaces
//              }
            }

            if(!MSRData.Cool(card,sale.wasSwiped())){
              Inform("Card data missing, invalid, or incomplete");
              return showChange;
            }
            Inform("Preparing Credit request");
            request=new CreditRequest(sale,card);
          } break;
          case PayType.Check: {
            if(!MICRData.Cool(check)){
              Inform("Check data missing, invalid, or incomplete");
              return showChange;
            }
            if(idRequired && !id.isOk()){
              Inform("ID required for Check");
              return showChange;
            }
            Inform("Preparing check request");
            request=new CheckRequest(sale,check,id.Info());//even if id not ok.
          } break;
        }//validation switch
        lastTid=TransactionID.Zero();//erase previous so that cancels can't accidentally cancel the wrong thing.
        if(VerboseSpawn("attempting transaction", request)){//will set tranny to either filled or sent.
          if(request.isFinancial()){
            freshReceipt((FinancialRequest) request);
            //now we can clear the values, since this is already sent and handled.
            clearCustomerData();
          }
        } else {
          Inform("Request not sent!");
        }
      }
      return showChange;
    } catch (Exception caught) {
      dbg.Caught(caught);
      return showAll;
    } finally {
      dbg.Exit();
    }
  }

  /** insert fields common to all requests
  * @return true if data actually gets stuffed
  * @param request newly minted request
  */
  private final boolean completeRequest(ActionRequest request){
    if(request==null){
      return false;
    }
    request.setCallback(/*actor*/this);
    if(clerk.idInfo.NonTrivial()){
      request.clerk=clerk.idInfo;
      request.terminalId = id();
    } else {
      Inform("Need clerk name and password");
      return false;
    }
    return true;
  }

  /**
  @return true if we spawn a transaction
  */
  private final boolean VerboseSpawn(String label,ActionRequest request){
    boolean success=false;
    try {
      if(completeRequest(request)){//side effect: load common fields
        dbg.VERBOSE("About to startaction on " + request);
        if(connectionClient.StartAction(request)){
//          myState=Busy;
          success = true;
          tip=request;
          //                cancelledTip=false;
          if(tip.isFinancial()){
            wait("auth code");
            GOTO(FTstate.WaitAuth);
          } else {
            wait("reply (admin)");
            waitadmin=true;//added to blokc users from doing anything
          }
        } else {
          dbg.ERROR("Spawn:overrun");
        }
      }
      //do protected stuff
    } finally {
      Inform(label+(success?" Started":" Not Spawned"));
      dbg.VERBOSE("Verbosespawn of "+request.TypeInfo()+ " success is:"+success);
      return success;
    }
  } //and when action is done the next method is called:

  private final void onBatchReply(BatchReply bratch,boolean isCloser){
    Receipt.PrintBatchList(printer,bratch);
  }

  /** respond to whatever type of reply we get, regardless of what we asked for
  */
  public void ActionReplyReceipt(Action action){//interface ConnectionCallback
    Post(action);
  }

  private final int Handle(Action action){
    return HandleAction(action);
  }

  private final int HandleAction(Action action){
    dbg.Enter("HandleAction:"+action.TypeInfo());
    try {
      waitadmin=false;
      Inform(action.historyRec());
      if(action.request.isFinancial()){
        lastTid=Action.tidOf(action);//store for "void previous"
        if(amSpamming){
          if(action.reply.Succeeded()){
            ActionRequest next= nextSpam((FinancialReply)action.reply);
            return showNothing;//spammer is stealthy
          } else {
            //print lots of info on spammer failure...
          }
        }
      }
      if (!action.reply.Succeeded()) {
        return onFailure(action);
      } else {
        switch(action.reply.Type().Value()){
          default: {
            dbg.ERROR("Ignoring a "+action.reply.Type().Image());
          } break;

          case ActionType.batch: {
            onBatchReply((BatchReply)action.reply,((BatchRequest)action.request).isClosing);
          } break;

          case ActionType.reversal:{
            return onVoidReply((ReversalReply)action.reply);//should always return null
          } //break;

          case ActionType.receiptStore:{
            dbg.VERBOSE("ActionReplyReceipt().ActionType.receiptStore: calling onCancel()");
            return onCancel(); //we are now finished, if we fail then clerk has to figure that out
          } //break;

          case ActionType.card: dbg.VERBOSE("Treating Card as if Credit");
          case ActionType.check://same as credit, differences handled at deeper layers
          case ActionType.debit://humm, differences seem to be pushing down into 'onApproval'
          case ActionType.credit:{
            dbg.VERBOSE("Processing financial reply");
            return onApproval((FinancialReply)action.reply,(FinancialRequest) action.request);
          } //break;
          case ActionType.clerkLogin:{
            Inform("Login reply");
            clerk.onLogIn((LoginReply)action.reply);
            //              if(action.reply.Succeeded()){
              connectionClient.standin.startBacklog();//goose# (on clerk login)
            //              }
            onCancel();
//            if(autoSale){
//              normalSale();
//            } else {
//              NoSale();
//            }
          } break;
          case ActionType.toprinter: {
            //no supporting class yet.
            //will take a FormattedLines and dump to
            //receipt printer.
          } break;
          case ActionType.tolog:{
            Inform("Logging message from server");
            dbg.ERROR(((MessageReply)action.reply).body );
          } break;
          case ActionType.toclerk:{
            Inform(((MessageReply)action.reply).body );
          } break;
        }//end successful action switch on type
      }
      return showAll;
    } catch (Exception caught) {
      dbg.Caught(caught);//+_+
      return showAll;
    } finally {
      dbg.Exit();
    }
  }

  private boolean amSpamming=false;
  private int spamPhase=-1;
  private CreditRequest spammer = null;

  private  void SpamServer(boolean b){
    amSpamming=b;
    if(b){
      spamPhase=-1;
      spammer = new CreditRequest(new SaleInfo(sale),new MSRData(card));
      attemptTransaction();
    } else {
      //any cleanup???
      spammer = null;
      spamPhase=-1;
    }
  }

  // sale, void, return, void; rinse and repeat
  private final ActionRequest nextSpam(FinancialReply reply){
    ActionRequest ar = null;
    CreditRequest cr = null;
    switch(++spamPhase){
      default: spamPhase=0;
      case 0: {
        ar = VoidForReply(reply);
      } break;
      case 1: {
        cr = new CreditRequest(spammer);
        cr.sale.type.op.setto(TransferType.Sale);
        ar = cr;
      } break;
      case 2: {
        ar = VoidForReply(reply);
      } break;
      case 3: {
        cr = new CreditRequest(spammer);
        cr.sale.type.op.setto(TransferType.Return);
        ar = cr;
      } break;
    }
    return ar;
  }

  private final void refreshForm(){
    dbg.Enter("refreshForm");
    try {
      if(currentForm==null){
        dbg.WARNING("before any form selected");
        return; //forms not yet initialized
      }
      if(currentForm.isSwiper){
        dbg.VERBOSE("Form accepts swipes");
        jTerm.cardReader.Acquire();
        expectingIDcard=currentForm.idSwipe;
        //pick swipe text variants here
      } else {
        jTerm.cardReader.Flush();//which has side effect of disabling as well as discarding input
        expectingIDcard=false; //4debug
      }

      if(currentForm.showsAmount){//must follow forms.Acquire!
        String saleText;
        if(theSaleAmount().Value()>0){
          saleText= OurForm.ValuePair(sale.amountHint(), theSaleAmount().Image());
        } else {
          saleText= OurForm.Bannerize(sale.noAmountHint());
        }
        dbg.VERBOSE("Form shows txn amount:"+saleText);
        jTerm.former.displayLegend(currentForm.AmountLegend(saleText));
      }
    } finally {
      dbg.Exit();
    }
  }

  private final void selectForm(int formIndex){//resend even if it is same form
    dbg.VERBOSE("Selecting form#"+formIndex);
    currentForm=OurForms.Find(formIndex);
    if(currentForm==null){
      currentForm=OurForms.Find(POSForm.FubarForm);//and we will blow if that doesn't exist
      if(currentForm==null){
        dbg.ERROR("Forms are really screwed up");      //double fault. Presume forms system is broken.
        return;
      }
    }
    if(currentForm.isPinPad){
      startPinEntry();
    } else {
      dbg.VERBOSE("acquiring form:"+currentForm.myName);
      jTerm.former.Acquire(currentForm.myName,currentForm.isStored);
      refreshForm();
    }
    formIsStale=false;
  }

  private final void killSwiper(String why){
    if(currentForm!=null && !currentForm.isSwiper){
      jTerm.cardReader.Flush();//kills pending input as well as disables swiping
      jTerm.former.displayLegend(currentForm.BannerThis(Safe.OnTrivial(why,"Please Wait")));
    }
  }

  /**change form if designated one is NOT currently being shown
  * @param pf POSForm.*
  */
  private final int assertForm(int pf){
    if(currentForm==null || formIsStale || currentForm.myNumber!=pf){
      //do we collect a signature in progress here???+_+
      selectForm(pf);
      return showChange;//don't need to refresh form a second time.
    }
    refreshForm();//update dynamic fields even if from was already showing
    return showChange;
  }


  private final int retryCheck(){//+++ this will need some work! down at messaging level!
    return attemptTransaction();
  }

  /** @return true means ignore remaining buttons
  *  @param butt a ButtonTag. value
  */

  private final int doButton(int butt){
    switch(butt) {
      default:
      case ButtonTag.NullButton: return showAll; /*will reload form*/    //break;
      case ButtonTag.ClearForm:  return showChange; /*handled by entouch*/    //  break;
      case ButtonTag.CouponDesired:   return PrintCurrentCoupon(); //break;
      case ButtonTag.DoCheck:         return startCheckSale(false);    // break;
      case ButtonTag.DoCredit:        return startCreditSale(false);  //  break;
      case ButtonTag.DoDebit:         return startDebitSale();    // break;
      case ButtonTag.DoCash:          return startCashSale();   //   break;
      case ButtonTag.CustomerCancels: return onCancel();        //   break;
      case ButtonTag.CustomerAmountOk:return onAmountOk();       //  break;
      case ButtonTag.Signed:          return fetchSignature("patron");
      case ButtonTag.DriversLicense:  return retryCheck(); //break;
      case ButtonTag.OtherCard:      return retryCheck();// break;
    }//end switch id
    //snr return showAll;
  }

  private final void onPin(PinCaptured newpin){
    if(newpin.HasData()){
      customerPin.setto(newpin.Pin());
      customerAccepts=true;
      attemptTransaction();
    } else {//capture failed
      //+_+ how far back do we go???
      doButton(ButtonTag.DoCredit);
    }
  }

  private final void onIdPresented(){
    if(id.isOk()){
      customerAccepts=true; //on id entered
      //caller had better attemptTransaction.
    } else {    //need to bug them to try again...
      customerAccepts=false; //true; //--- force it for now dammit
      Inform("ID was not Ok:"+id.Spam());
    }
  }

  private final int handle(net.paymate.jpos.Terminal.Event jte){//jpos.Terminal.Event
    switch(jte.Type().Value()){
      default:{
        //ignore unexpected events.
      } break;

      case EventType.PinAcquired:{
        onPin((PinCaptured)jte);
      } break;

      case EventType.CheckAcquired:{
        if(readyForCheck()){
          onCheck(((CheckScanned)jte).Value(),true /*reallyScanned*/);
          clerkui.loadCheck(check);
        } else {
          //CM3000 display gets fucked...
          clerkui.refresh();//showAndTell();//refreshes display
        }
        jTerm.checkReader.Acquire(); //keep it alive for rescanning
      } break;
      case EventType.CardAcquired:{
        MSRData theCard=((CardSwiped)jte).Value();//value function creates new object.
        if(TextList.NonTrivial(theCard.errors)){
          printer.println("Card has defects, still might work");
          printer.print(theCard.errors);
        }
        if(currentForm.isSwiper){
          if(expectingIDcard){
            id.onSwipe(theCard);
            onIdPresented();
            return attemptTransaction();//+_+ qualify with "payby==check" or some not yet existent "need ID"
          } else {
            return onCard(theCard ,true /*reallySwiped*/);
          }
        }
      } break;

      case EventType.SigAcquired:{
          dbg.ERROR("sigdata from forminput");
          SigData siggy = ((SigCaptured)jte).Value();
          if(siggy.isTrivial()){
            return onBadSignature();
          } else {
            return onSignature(siggy);
          }
      }// break;

      case EventType.FormButtonData:{
        FormButtonData fbd = (FormButtonData)jte;
        for(int i = fbd.button.length; i-->0;) {
          if(fbd.button[i].wasPressed) {//
            return doButton(fbd.button[i].ID); //ignore remaining buttons as only one is ever pressed
            //surveys are a different beast and we aren't using them.
          }//end if pressed
        }//end for buttons
      } break;//end case buttons
    }//end switch(type)
    return showAll;
  }

  private final int handle(jpos.JposException jape){
    dbg.WARNING("Handling:"+jape);
    return showAll;
  }

  private final int handle(jpos.events.ErrorEvent jape){
    dbg.WARNING("Handling:"+jape.toString());
    return showAll;
  }

  private final void NoSale(){//FUE
    setTransferType(TransferType.Unknown);
  }

  private final void normalSale(){//FUE
    setTransferType(TransferType.Sale);
    jTerm.checkReader.Acquire();
  }

  private final boolean clerkAllowedTo(boolean allowed){
    if(!allowed){
      showProblem("manager req'd","to do that");
    }
    return allowed;
  }

  private final void mkBatchRequest(boolean closer){
    if(true /*clerkAllowedTo(closer? clerk.prov.canClose: clerk.priv.canVOID)*/){//printing req'd for voiding
      if(!VerboseSpawn(closer?"Close Drawer":"Print Drawer",new BatchRequest(closer))){
        showProblem("Try Again Later",weareoffline);
      }
    }
  }

  private final int Handle(Functions funcode){
    boolean gotosale=true;
    dbg.Enter("Functions:"+funcode.Image());
    try {
      switch(funcode.Value()){
        default: return showAll; //swallow it.
        case Functions.reserved:{
          // accidental invocation slows down receipt printing.
          //          Receipt.setShowSignature(!Receipt.showSignature);//toggle sig printing
          //          reception.rePrint(printer);//so they can confirm the change.
        } break;
        //case Functions.Terminal:
        case Functions.ChangeUser: {//
          connectionClient.standin.setStandin(false);
          clerk.Clear(); //forget who the clerk is
        } break;
        case Functions.LastReceipt: {
          reception.rePrint(printer);
        } break;
        case Functions.PrintDrawer: {
          mkBatchRequest(false);
        } break;
        case Functions.CloseDrawer: {
          mkBatchRequest(true);
        } break;
        case Functions.PrintCoupon: {
          Handle(new ClerkCommand(ClerkEvent.PrintCoupon));
        } break;
        //+_+ make this work!
        //      case Functions.Offline:     {
          //        offlinetranny=true;
          //        isaMoto=false;
          //        setTransferType(TransferType.Sale);
        //      } break;
        case Functions.Sale:        {
          if(clerkAllowedTo(clerk.priv.canSALE)){
            gotosale=true;
          }
        } break;
        case Functions.Return:{
          if(clerkAllowedTo(clerk.priv.canREFUND)){
            setTransferType(TransferType.Return);
            gotosale=false;
          }
        } break;
        case Functions.Void: {
        dbg.ERROR("made it to functions.void");
          if(clerkAllowedTo(clerk.priv.canVOID )){
            dbg.ERROR("clerkallowed to void");
            setTransferType(TransferType.Reversal);
            gotosale=false;
          } else {
            dbg.ERROR("clerk not allowed to void");
          }
        } break;
        case Functions.ReEntry: {
          if(clerkAllowedTo(clerk.priv.canMOTO )){
            setTransferType(TransferType.ReEntry);
            gotosale=false;
          }
        } break;
      }
    } finally {
      if(gotosale){
        normalSale();
      }
      dbg.Exit();
      return showChange;
    }
  }

  private final int Handle(PaySelect ps){
    switch(ps.Value()){
      case PaySelect.Cancel:      onCancel();  break;
      case PaySelect.ManualCard:  startCreditSale(true);  break;
      case PaySelect.ManualCheck: startCheckSale(true);   break;
    }
    return showChange; //all of the above showantell
  }

  /**
  * call when manual data entry has occured
  */
  private final void manHandled(){
    sale.type.setto(new EntrySource(EntrySource.KeyedIn));
  }

  /**
  * each clerkUi punt() must have a matching case here
  */
  private final int Handle(ItemEntry aiee){//clerkui.punt
    dbg.Enter("Handle:"+aiee);
    //this didn't work as it should, it preempted next prompt    clerkui.flash("Checking ...");
    try {
      String manentry=aiee.image;
      switch(aiee.msgid){
        case ClerkItem.BadCardNumber:
        case ClerkItem.CreditNumber:{
          manHandled();
          card.accountNumber.setto(manentry);
        } break;

        case ClerkItem.BadExpiration:
        case ClerkItem.CreditExpiration:{
          manHandled();
          card.expirationDate.parsemmYY(manentry);
        } break;

        case ClerkItem.CreditName:{//+_+ we shove everything into the surname, good enough for visual displays.
          manHandled();
          card.person.Surname=manentry;
        } break;

        case ClerkItem.CheckBank: {
          manHandled();
          check.Transit=manentry;
        } break;

        case ClerkItem.CheckAccount: {
          manHandled();
          check.Account=manentry;
        } break;

        case ClerkItem.CheckNumber: {
          manHandled();
          check.Serial=manentry;
        } break;

        case ClerkItem.SalePrice: {
          OnSaleAmount(new RealMoney(manentry));
        } break;

        case ClerkItem.License:  {
          id.setLocale("TX");  // --- testing (for now)
          id.setNumber(manentry);//+_+ need to unpack state code.
          if(!id.isOk()){//then see if we have a bypass code
            id.Force(clerk.idInfo.Passes(manentry));//
          }
          onIdPresented();
        } break;

        case ClerkItem.NeedApproval:  {
          onAmountOk(manentry);
        } break;

        case ClerkItem.RefNumber:{
          //void or reentry:
          onVoid(manentry);
        } break;

      }//end switch(clerkitme)
      return attemptTransaction();
    } finally {
      dbg.Exit();
    }
  }

  private final void reportOnConnection(){
    FormattedLineItem banner=FormattedLineItem.winger("CONNECTION");
    printer.print(banner);
    printer.print("Cname:",myApple.commonhost.name);
    Date systime=Safe.Now();
    //    printer.print("Clock:", Safe.timeLocal(systime)+TimeZone.getDefault().getDisplayName(true,TimeZone.SHORT));
    //would like to access MD5 digest here instead of filesize.
    printer.print("Crock:",Revision.Version()+" (C"+Safe.fileSize("paymate.jar")+")");

    Date fallback=Safe.fileModTime("paymate.jar");
    if(systime.before(fallback)){
      //then clock has failed!!
      printer.print("Ctime:","T"+fallback.getTime());
      OS.setClock(fallback);
      //+++--- retry connection stuff.
    }

    printer.print(banner);
    printer.formfeed();
  }

  /**
  * @param cq is the clerk item that the clerk has pressed cancel upon
  */
  private final int onCancel(ClerkItem cq){
    dbg.VERBOSE("cancelling "+cq.Image());
    switch (cq.Value()) {
      case ClerkItem.SecondCopy:{
        needAnotherCopy=false;
        doClerkCancel();
      } break;

      case ClerkItem.NeedApproval:{//"reenter xyz amount"
        sale.money.amount.setto(0);//+_+ RealMoney needs a clear
      } break;

      case ClerkItem.NeedSig:{//do manual sig dance
        lateCancel();
      } break;

      case ClerkItem.BootingUp:{
        //hook for letting the clerk know that we really are trying to connect.
        dbg.VERBOSE("connection cleared");
        reportOnConnection();
      } break;

      default:{
        //the following show and tell should restart the same question...
      } break;
    }
    return showChange;
  }

  private final void whenConnecting(String backdoor){
    if(backdoor.startsWith(NameOfGod+'*')){
      CastSpell(Safe.parseLong(backdoor.substring(backdoor.indexOf('*')+1)));
    }
    else {//previous revisions behavior
      Handle(new ClerkCommand(new ClerkEvent(ClerkEvent.Reconnect)));
    }
  }

  private final int doClerkCancel(){
    switch(tranny.Value()){
      case FTstate.WaitAuth:
      //do nothing!
      return showAll;
      case FTstate.WaitSig:
      case FTstate.SigOnWire:
      return lateCancel();
      default:
      return onCancel();//4debug kill everything locally
    }
  }

  private final int Handle(Remedies todo){
    switch(todo.Value()){
    case Remedies.Void:   return doClerkCancel();  //Void if at all possible!
    case Remedies.Done:   return onCancel();   //do NOT void if complete!
    case Remedies.Retry:  return showAll;  //means attempt to retry transaction. Not yet allowed.
    case Remedies.Reprint: return printStoreCopy();
    }
    return showAll;
  }

  private final int Handle(ClerkCommand cmd){//Gui -> PosTerminal
    try {
      dbg.Enter("Handle ClerkCommand "+ cmd.kind.Image());
      if(cmd instanceof Cancellation){
        onCancel(((Cancellation)cmd).ClerkItem());
        return showAll;
      }
      if(cmd instanceof ItemEntry){
        return Handle((ItemEntry)cmd);
      }
      switch(cmd.kind.Value()){
        case ClerkEvent.Enter:{
          dbg.ERROR("Should be an ItemEntry, is:"+cmd.getClass().getName());
        } break;
        case ClerkEvent.Debug:{
          switch(((DebugCommand)cmd).debop.Value()){
            default: Inform("You pressed the Debug Button!"); break;
            case DebugOp.Refresh       :currentForm=null;       break;
            case DebugOp.ForceClerkIn  :{
              LoginReply fakerep=new LoginReply();
              Action fakeact=Action.New(new LoginRequest(), connectionClient.actionHistory);
              fakeact.reply=fakerep;
              fakerep.status=new ActionReplyStatus(ActionReplyStatus.SuccessfullyFaked);
              ActionReplyReceipt(fakeact);
            } break;
            case DebugOp.ForceLoggedIn :terminalLoggedin=true;          break;
            case DebugOp.ForceSignature: {
              //+++ restore signature override
            } break;
            case DebugOp.SimulateOn    : break;
            case DebugOp.SimulateOff   : break;
            case DebugOp.TranSpamOn    :SpamServer(true);         break;
            case DebugOp.TranSpamOff   :SpamServer(false);        break;
          }
          return showChange;
        } //break;

        case ClerkEvent.Cancel:{
          doClerkCancel();
        } break;

        case ClerkEvent.Send:{
          if(!amTransacting()){
            attemptTransaction();
          } else {
            Inform("Busy, CANCEL to try again");
            return showChange;
          }
        } break;

        case ClerkEvent.Login:{
          clerk=new Clerk(); //erase previous settings
          OnClerkId(new ClerkIdInfo(((ClerkLoginCommand)cmd).cid));
        } break;

        case ClerkEvent.Reconnect:{
          //manually exit standin
          connectionClient.standin.setStandin(false);
          //          this.myApple.notifyAll();
        } break;

        case ClerkEvent.Reprint:{
          printStoreCopy();
        } break;

        case ClerkEvent.PrintCoupon:{
          PrintCurrentCoupon();
        } break;

        case ClerkEvent.SendSignature:{
          onSignature(sigdata);
        } break;

        case ClerkEvent.Functions:{
          NoSale();
          return showAll;
        }//break;

        default: {
          dbg.ERROR("Unhandled ClerkEvent: " + cmd.kind.Image());
        } break;
      }
      return showChange;
    } catch (Exception caught) {
      dbg.Caught(caught);
      return showAll;
    } finally {
      dbg.Exit();
    }
  }

  private final void Inform(TextList msg) {
    if(terminalServer!=null) {
      terminalServer.Inform(msg);
    }
  }

  private final void Inform(String msg) {
    if(terminalServer!=null) {
      terminalServer.Inform(msg);
    }
  }

  //module initializations:
  private final void StoreForms(boolean all){
    dbg.VERBOSE("StoreForms, all="+all);
    for(int fi= POSForm.Prop.numValues();fi-->0;){
      OurForm form=OurForms.Find(fi);
      if(form==null){
        if(fi!=POSForm.FubarForm){//not a real form ...yet
          dbg.ERROR("didn't create form: "+ POSForm.Prop.TextFor(fi));
        }
      } else if(all || Safe.NonTrivial(form.pcxResource)){
        dbg.VERBOSE("StoreForm:"+form.myName);
        jTerm.former.StoreForm(form.myName);
        form.isStored=true; //can't move this action into class.OurForm without
        //making that class know about hardware...
      }
    }
  }

  private final void FormSetUp(String why){//part of constructor
    dbg.VERBOSE("FormSetup."+why);
    OurForms.applyOptions(caps);//for easy testing
//    StoreForms(false/*only if required*/);
    StoreForms(true); //store if at all possible
  }

  public void Start(EasyCursor ezp/*,Constants myHost*/){//run once
    //don't change the following order of initializations gratuitously!!
    //... there are dependencies
    dbg.Enter("Start");
    try {
      //construction deferred to here so that we would ahve a name for debugging:
      dbg.VERBOSE("constructing "+termInfo.toSpam());
      //standin2, don't reload if same info
      Configure.Load(""+termInfo.id(),termInfo.equipmentlist);
      connectionClient= new ConnectionClient(termInfo);

      //create data stores
      clerk=new Clerk();
      sale=new SaleInfo();
      card=new MSRData();
      customerPin=new PINData();
      check=new MICRData();
      checkId=new CheckIdInfo();
      sigdata=new SigData();

      //create agents
      jTerm=new Wrapper(this);//just creates shells here, real ones come in attachall
      printer = new NullPrinterModel(/*new LinePrinter("dummy")*/);//so as to be nonnull
      clerkui= new ClerkUI(this);
      ErrorLogStream.Debug.ERROR("global debugger is on");
      dbg.VERBOSE("starting thread");
      incoming.Clear();//also starts it if not already started.

      boolean startIPServer = ezp.getBoolean("debug.byip",false);
      terminalServer = new IPTerminal(ezp.getInt("debug.port",49852), this);

      if(startIPServer) {
        dbg.VERBOSE("starting terminal server");
        terminalServer.start();
      }
      else {
        dbg.VERBOSE("terminal server not started");
      }
      dbg.VERBOSE("starting backlog");
      connectionClient.standin.startBacklog(); //powerup#
      dbg.VERBOSE("making forms");
      OurForms.MakeAllForms();//precede attachAll
      dbg.VERBOSE("attaching jpos devices");
      jTerm.attachAll(""+termInfo.id());//must precede forms usage
      dbg.VERBOSE("finding printer");
      printer=jTerm.printer;//FUE/legacy
      dbg.VERBOSE("starting clerk ui");
      clerkui.Start(); //who has no autonomous events
      //legacy: //used to demo graphics printing ability
      dbg.VERBOSE("setting up coupon");
      currentAdvert.setAd(ezp.getString(".advert")).setTargaClip(ezp.getInt(".Targa.ClipLevel")).setCoupon(ezp.getString(".coupon"));
      dbg.VERBOSE("clear all state");
      onCancel(); //cancel ourselves to get into our base state
      shower(2);
    } catch(Exception all){
      dbg.Caught(all);
    }
    finally {
      dbg.Exit();
    }
  }

  private final void unlink(){//for appliance ConnectionReply
    dbg.Enter("Unlink");
    try {
      if(jTerm!=null){
        dbg.WARNING("jTerm stopping");
        jTerm.detachAll();
      }
      //we trust that these will eventually stop:
      if(connectionClient!=null){
        dbg.WARNING("CONNECTION stopping");
        connectionClient.Stop();
      }
      if(terminalServer!=null){
        dbg.WARNING("IpTerminal stopping");
        terminalServer.Stop();
      }
      //kill run queue!
      incoming.Stop();//but can't stop until we return.
    } finally {
      dbg.Exit();
    }
  }

  PosTerminal(TerminalInfo termInfo, Appliance apple) {//construct
    this.termInfo = termInfo;
    this.myApple=apple;
    incoming=QAgent.New(this.id(),this);
    incoming.config(dbg).config(300.0);//wake up occasionally even if there is nothing to do.
    // DO NOT put any function calls or code in here ! i.e. don't trigger any behavior yet
  }
  /////////////////////////////////
  // Ip terminal subroutines

  private final void Thump(boolean start){
    Executor.runProcess("thump "+(start?"start":"stop"),"thumping",0,0,null,false);
  }

/**
 * inform our peripherals,
 * disengage resources,
 * tell other terminals to do so.
 */
  private final void fancyExit(int exitcode,String why){
//    if(cancellable()){
      if(!amDying){//guard against reentrancy
        amDying=true;
        selectForm(POSForm.NotInService);
        clerkui.ask(clerkui.showProblem(Fstring.centered(why,16,'#'),ExitCode.Prop.TextFor(exitcode)));
//      ThreadX.sleepFor(3.0); //give peripherals a chance to disconnect
        unlink();
        Appliance.terminalIsDown(this,exitcode);
      }
//    } else {
//      diewhendone=true;
//    }
  }

  /**
  * execute no-operand terminal (usually debug) commands
  */

  private final int Handle(TerminalCommand tc){
    dbg.ERROR("Handling TC:"+tc.Image());
    switch(tc.Value()) {
      case TerminalCommand.GoOffline: {
        Standin().setStandin(true);
      } break;
      case TerminalCommand.GoOnline: {
        Standin().setStandin(false);
      } break;
      case TerminalCommand.Identify:{
        showIdentity();
      } break;
      case TerminalCommand.Shutdown: {
        fancyExit(ExitCode.Halt,"powering down");
      } break;
      //      case TerminalCommand.coupon: {
        //        postClerkEvent(ClerkEvent.PrintCoupon);
      //      } break;
      //      case TerminalCommand.print: {
        //        postClerkEvent(ClerkEvent.Reprint);
      //      } break;
      case TerminalCommand.Reconnect: {
        Handle(new ClerkCommand(new ClerkEvent(ClerkEvent.Reconnect)));
      } break;
      case TerminalCommand.sendSignature: {
        onSignature(SigData.MinimalFaked());
      } break;
      case TerminalCommand.Clear: {
        onCancel();
      } break;
      case TerminalCommand.Quiet:{
        fancyExit(ExitCode.StatusOff,"RESTARTING NOW");
      } break;
      case TerminalCommand.Shout:{
        fancyExit(ExitCode.StatusOn,"RESTARTING NOW");
      } break;
      case TerminalCommand.Poff:{
        if(terminalServer!=null){
          terminalServer.Stop();
        }
      } break;
      case TerminalCommand.Pond:{
        if(terminalServer!=null){
          TextList fullinfo=superSpam(null);
        }
        int newport=(terminalServer==null)?49852:terminalServer.port;
        dbg.VERBOSE("restarting terminal server");
        terminalServer = new IPTerminal(newport, this);
        terminalServer.start();
      } break;
    }
    return showChange;
  }

  /**
   * @todo add remaining members
   */
  private TextList toSpam() {
    TextList tl = new TextList();
    ErrorLogStream.objectDump(card,    "card",    tl);
    ErrorLogStream.objectDump(check,   "check",   tl);
    ErrorLogStream.objectDump(sale,    "sale",    tl);
    ErrorLogStream.objectDump(checkId, "checkId", tl);
    return tl;
  }

  /*package*/ void dump(TextList responses) {
    responses.appendMore(toSpam());
  }

  /**
  * for appliance status grabbing.
  */
  /*package*/ Standin Standin(){//4ipterm
    return connectionClient.standin;
  }

  TextList ActionStatsReport(TextList responses){//4ipterm
    // displays statistics about this running terminal (how many txns, avg time, etc)
    return connectionClient.ActionStatsReport(responses);
  }

  TextList ActionHistoryReport(TextList responses){
    return connectionClient.ActionHistoryReport(responses);
  }

  private TextList superSpam(TextList spam){
    if(spam==null){
      spam=new TextList();
    }
    spam.Add(this.id());
    spam.Add(this.caps.toSpam());
    spam.Add("");//+_+ add more spam
    return spam;
  }

  ////////////////////////////////
  // localizable text
  private static final String weareoffline="authorizer down";
  ////////////////
}

//$Id: PosTerminal.java,v 1.259 2001/11/17 00:38:35 andyh Exp $
