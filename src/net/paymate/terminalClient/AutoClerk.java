package net.paymate.terminalClient;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/terminalClient/AutoClerk.java,v $
 * Description:  Automated Device feeds Clerk interface.
 * @see net.paymate.terminalClient.ConsoleClerk
 * @see net.paymate.ncr.EptTerminal
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.27 $
 * @todo: infinite loop on Problem[22] **Manager Required** ? approve instead of cancel?
 * @todo: didn't have Return privelege on auto login's priveleges.
 * threads:
 *  on its serial ports posterm gets posts and its qagent calls ask()
 *  on authresponse posterm gets a post and its qagent calls authresponse() then ask()
 *  on ept serial port eptclerk does posterm Posts, which responds on its qagent thread
 *    the response to a post on ept.serial thread might come back on posterm.qagent thread while
 *    serial thread is still active. Posterm WAITS for ask() to complete before proceeding.
 *    eptclerk can hang posterm by hanging in eptclerk's ask().
 *
 */
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.net.*;
import net.paymate.connection.*;
import net.paymate.lang.ReflectX;
import net.paymate.jpos.data.*;
import net.paymate.awtx.*;
/// for tester
import net.paymate.awtx.print.PrinterModel;

public class AutoClerk implements isEasy {
  protected ErrorLogStream dbg;//will be created by extended classes. serial side
  protected ErrorLogStream dpg;//ditto, posterm threads
  private static ErrorLogStream dbstatic;//for extended class consturction

  protected int width;

  protected PosTerminal posterm;//not BaseTerminal because of lack of receipt management...
  protected AutoClerkState progress;

//configuration
  private ClerkIdInfo clerk;
  protected boolean greekem=false; //whether to truncate returned card numbers, in case the register doesn't
  protected String greekfix;
  protected boolean standalone=false; //true when there is no other clerkpad
  //config set by extension:
  protected boolean amountImpliesFunction;
  protected boolean alwaysSetFunction;

  final static String greekemKey=           "greekem";
  final static String greekfixKey=          "greekfix";
  final static String widthKey=             "debug.width";
  final static String clerkKey=             "clerk";
  final static String standaloneKey=        "standalone";
//these are not set on the base class. Only extensions can invoke these settings.
//the keys are defined here for covnenience.
  protected final static String amountImpliesFunctionKey="amountImpliesFunction";
  protected final static String alwaysSetFunctionKey=    "alwaysSetFunction";

  public void save(EasyCursor ezc){
    ezc.setObject(clerkKey,clerk);
    ezc.setBoolean(greekemKey,greekem);
    ezc.setString(greekfixKey,greekfix);
    ezc.setInt(widthKey,width);
    ezc.setBoolean(standaloneKey,standalone);
  }

  public void load(EasyCursor ezc){
    clerk=  (ClerkIdInfo)ezc.getObject(clerkKey,ClerkIdInfo.class);
    greekem=ezc.getBoolean(greekemKey,true);//if not specified then truncate card numbers.
    greekfix=ezc.getString(greekfixKey); //accept trivials.
    width= ezc.getInt(widthKey);
    standalone=ezc.getBoolean(standaloneKey);
   }

  public String greeked(CardNumber acctnum){
    if(CardNumber.NonTrivial(acctnum)){
      return greekem? acctnum.Greeked(greekfix) : acctnum.Image();
//      return acctnum.Image();
    } else {
      return "not available";
    }
  }

  public ClerkIdInfo clerk(){
    if(clerk==null){
      clerk=ClerkIdInfo.Auto(this);
    }
    return clerk;
  }


/**
 * @return true if object is posted to posterminal
 */
  protected boolean Post(Object obj){
    if(posterm!=null){
      dbg.VERBOSE("posting:"+ ReflectX.shortClassName(obj));
      if (obj instanceof Problem) {
        dbg.VERBOSE(obj.toString());
      }
      return posterm.Post(obj);
    } else {
      dbg.WARNING("no posterm");
      return false;
    }
  }

  protected boolean PostClerkEvent(int clerkevent){
    if(posterm!=null){
      return posterm.postClerkEvent(clerkevent);
    } else {
      return false;
    }
  }

  protected boolean postFunction(int functioncode){
    Functions topost=new Functions(functioncode);
    dbg.VERBOSE("posting function code:"+topost.Image());
    return Post(topost);
  }

  /**
   * allow extensions to react to state changes
   * @return previous state
   */
  protected int Become(int AutoClerkStateValue){
    try {
      return progress.Value();
    } finally {
      progress.setto(AutoClerkStateValue);
    }
  }

  protected boolean setFunction(long cents){
    if(alwaysSetFunction){
      return postFunction( (amountImpliesFunction&&cents<0) ?Functions.Return : Functions.Sale );
    } else {
      return true; //+_+ can we detect if posterm is in correct state?
    }
  }

  protected boolean startSale(long cents){
    dpg.Enter("startSale");
    try {
      if(cents!=0){
        dpg.VERBOSE("cents:"+cents);
        if(setFunction(cents)){
          if(cents<0){
            cents=-cents;
          }
          Become(AutoClerkState.authing);
          return Post(RealMoney.Zero().setto(cents));
        } else {
          dpg.ERROR("failed to set function");
          return false;
        }
      } else {
        dpg.VERBOSE("zero cents");
        return false;//didn't happen
      }
    }
    finally {
      dpg.Exit();
    }
  }
/**
 * emulate pressing cancel/reset on this question, do not let human interface offer it to user.
 */
  protected boolean cancelThis(int clerkitem){
    Cancellation topost=new Cancellation(clerkitem);
    dpg.VERBOSE("cancelling:"+topost);
    Post(topost);
    return true;
  }

  protected boolean beIdle(){
    Become(AutoClerkState.idle);
    return false;
  }

  protected PosTerminalState posstate;
/**
 * guess at PosTerm's state by what question is being asked.
 * need to move to PosTerminal class.
 */
  private static int inferState(int clerkitem){
    switch (clerkitem) {
      default: return PosTerminalState.Invalid();
      case ClerkItem.BootingUp       : return PosTerminalState.Occupied ;
      case ClerkItem.ClerkID         : return PosTerminalState.Cancellable ;
      case ClerkItem.ClerkPasscode   : return PosTerminalState.Cancellable ;
      case ClerkItem.SaleType        : return PosTerminalState.Ready ;
      case ClerkItem.PreApproval     : return PosTerminalState.Cancellable ;
      case ClerkItem.PaymentSelect   : return PosTerminalState.Cancellable ;
      case ClerkItem.CreditNumber    : return PosTerminalState.Cancellable ;
      case ClerkItem.BadCardNumber   : return PosTerminalState.Cancellable ;
      case ClerkItem.CreditExpiration: return PosTerminalState.Cancellable ;
      case ClerkItem.BadExpiration   : return PosTerminalState.Cancellable ;
      case ClerkItem.CreditName      : return PosTerminalState.Cancellable ;
      case ClerkItem.CheckBank       : return PosTerminalState.Cancellable ;
      case ClerkItem.CheckAccount    : return PosTerminalState.Cancellable ;
      case ClerkItem.CheckNumber     : return PosTerminalState.Cancellable ;
      case ClerkItem.License         : return PosTerminalState.Cancellable ;
      case ClerkItem.MTA             : return PosTerminalState.Cancellable ;
      case ClerkItem.RefNumber       : return PosTerminalState.Cancellable ;
      case ClerkItem.SalePrice       : return PosTerminalState.Ready ;
      case ClerkItem.NeedApproval    : return PosTerminalState.Cancellable ;
      case ClerkItem.NeedSig         : return PosTerminalState.Loafing ;
      case ClerkItem.WaitApproval    : return PosTerminalState.Occupied ;
      case ClerkItem.ApprovalCode    : return PosTerminalState.Loafing ;
      case ClerkItem.Problem         : return PosTerminalState.Cancellable ;
      case ClerkItem.OverrideCode    : return PosTerminalState.Cancellable ;
      case ClerkItem.SecondCopy      : return PosTerminalState.Loafing ;
      case ClerkItem.WaitAdmin       : return PosTerminalState.Occupied ;
      case ClerkItem.SVOperation     : return PosTerminalState.Ready ;
      case ClerkItem.ShowForm        : return PosTerminalState.Occupied ;
      case ClerkItem.TerminalOp      : return PosTerminalState.Occupied ;
    }
  }

  boolean alreadyLoggedin;
  /**
 * can filter all questions being directed to human clerk interfaces.
 * @return true if question has been handled/answered.
 * Note: ask() will NOT be reentered when post() is called, because ask() is only called
 * on the posterminal qagent thread.
 */
  public boolean ask(int clerkitem){// @see ClerkItem.Enum
    posstate.setto(inferState(clerkitem));
    switch (clerkitem) {
      default:                        return false; //ask someone else
      case ClerkItem.ClerkID: //join
      case ClerkItem.ClerkPasscode:   {
        if(!alreadyLoggedin){
          alreadyLoggedin=true; //try only once even if it fails. especially if it fails!
          return Post(clerk());
        } else {
          return false; //allow human entry
        }
      }
      case ClerkItem.SaleType: { //this is our only sure sign of a cancel
        Become(AutoClerkState.idle);
      } return false;
//      case ClerkItem.PreApproval:
//      case ClerkItem.PaymentSelect:
//      case ClerkItem.CreditNumber:
//      case ClerkItem.BadCardNumber:
//      case ClerkItem.CreditExpiration:
//      case ClerkItem.BadExpiration:
//      case ClerkItem.CreditName:
//      case ClerkItem.CheckBank:
//      case ClerkItem.CheckAccount:
//      case ClerkItem.CheckNumber:
//      case ClerkItem.License:
//      case ClerkItem.RefNumber:      //attempting a void
      case ClerkItem.SalePrice:  Become(AutoClerkState.awake); return false; //posterm is ready for sales action.
//      case ClerkItem.NeedApproval:  //use "luby's" mode to deal with this.
//      case ClerkItem.NeedSig:        //been authed, waiting for signature.
      case ClerkItem.WaitApproval: { //can check this against our state
          dpg.WARNING("authing ");//can't get note as to what is being auth'ed without digging into posterm.
      } return false;
//      case ClerkItem.ApprovalCode:  //been authed, but we use a special function to deal with this.
      case ClerkItem.Problem:       return standalone&& cancelThis(clerkitem);//+++gotta say OK
      case ClerkItem.OverrideCode:  return standalone&& cancelThis(clerkitem);//+++we are really screwed, doCancel.
      case ClerkItem.SecondCopy:    return cancelThis(clerkitem);  //+++ must defeat via McDonald's stuff
//      case ClerkItem.WaitAdmin:      //batchlistings etc. Don't need to wait here.
//      case ClerkItem.SVOperation:   //won't happen
      case ClerkItem.BootingUp:     return beIdle();
    }
  }

  TextList spewer(String firstline){
    TextList spew=new TextList();
    spew.add(StringX.OnTrivial(firstline,"AutoClerk got response"));
    return spew;
  }

/**
 * spews diagnostics on a financial transaction.
 */
  protected void authResponse(PaymentRequest cardreq, PaymentReply reply){
    dpg.Enter("UponReply");
    TextList spew=spewer("Card Transaction");
    try {
      STAN echo = cardreq.sale.stan;
      spew.add("STAN",echo.value());
      spew.add("Account",greeked(cardreq.card.accountNumber));
      spew.add("Expiry",cardreq.card.expirationDate.mmYY());//#diagnostic output
      if(reply.Succeeded()){
        spew.add("Approval",reply.Approval());
      } else {
        spew.add("Declined",reply.Approval());
      }
    }
    finally {
      dpg.VERBOSE(spew.asParagraph(OS.EOL));
      dpg.Exit();
    }
  }

/**
 * spew batch listing to debug stream
 */
  protected void batchResponse(BatchRequest request, BatchReply reply){
    dpg.Enter("UponReply");
    TextList spew=spewer("Batch Response");
    try {
      BatchListingFormatter blif=BatchListingFormatter.Create(reply,posterm.reception.Formatter());
      PrinterModel dump=PrinterModel.BugPrinter(100);
      dump.print(blif.header);
      dump.print(blif.body);
    }
    finally {
      dpg.VERBOSE(spew.asParagraph(OS.EOL));
      dpg.Exit();
    }
  }

  /**
   * posterminal calls this when it gets anything from the sinet host
   * posTerm will finish its own processing before dealing with whatever gets posted here.
   */
  public void onReply(Action action){
    dpg.Enter("onReply");
    dpg.VERBOSE("while in state "+progress+ " received action:"+ (Action.NonTrivial(action)?action.TypeInfo():"trivial"));
    try {
      switch (action.Type().Value()) {
        case ActionType.payment: {
//          if(progress.is(AutoClerkState.authing)){
            authResponse((PaymentRequest ) action.request,(PaymentReply)action.reply);
            Become(AutoClerkState.responded);
//          } else {
//            dbg.ERROR("action ignored 'coz state is "+progress);
//          }
        } break;
        case ActionType.batch: {
            batchResponse((BatchRequest)action.request,(BatchReply)action.reply);
            Become(AutoClerkState.idle);
          } break;
        default:
          break;
      }
    } catch (Exception ex) {
      dpg.Caught("in onReply",ex);
    } finally {
      dpg.Exit();
    }
  }

  protected void commonStart(){
    dpg.VERBOSE("AutoClerk.commonStart");
    if(!ClerkIdInfo.NonTrivial(clerk)){
      clerk=ClerkIdInfo.Auto(this);
    }
  }

  public AutoClerk setLink(PosTerminal posterm) {
    dpg.VERBOSE("AutoClerk.setLink");
    this.posterm=posterm;
    commonStart();
    return this;
  }

  protected AutoClerk (){
    if(dbstatic==null) dbstatic=ErrorLogStream.getForClass(AutoClerk.class);
    dbg= ErrorLogStream.getForClass(getClass());
    dbg.WARNING("main debugger just instantiated, level setto:"+dbg.myLevel());
    dpg= ErrorLogStream.getExtension(getClass(),"post");
    dpg.WARNING("post debugger just instantiated, level setto:"+dbg.myLevel());
    width=80;
    progress= new AutoClerkState();
    posstate= new PosTerminalState();
  }

  public static AutoClerk makeFrom(EasyCursor ezc){
    ezc.push("AutoClerk");
    if(dbstatic==null) dbstatic=ErrorLogStream.getForClass(AutoClerk.class);
    try {
      AutoClerk newone;
      dbstatic.VERBOSE(ezc.asParagraph(OS.EOL));
      String classname=   ReflectX.stripNetPaymate(ezc.getString("class"));
      if(classname.endsWith("ConsoleClerk")){
        newone= net.paymate.terminalClient.ConsoleClerk.Create(ezc);
      } else if(classname.endsWith("EptClerk")){
        newone= net.paymate.ncr.EptClerk.Create(ezc);
      } else {
        return null;
      }
      newone.load(ezc); //all extensions should super.load()
      return newone;
    }
    catch(Exception any){
      dbstatic.Caught("AutoClerk.Create",any);
      return null;
    } finally {
      ezc.pop();
    }
  }

}
//$Id: AutoClerk.java,v 1.27 2005/03/02 05:23:07 andyh Exp $