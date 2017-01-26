package net.paymate.terminalClient;

/**
 * Title:         $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/terminalClient/PosTerminal.java,v $
 * Description:   state logic for dumb terminals, accumulate info and do transactions etc.
 * Copyright:     Copyright (c) 2001
 * Company:       PayMate.net
 * @author        PayMate.net
 * @version       $Revision: 1.437 $
 * @todo "debit not allowed" response is too severe, should only reset to card not swiped
 * @todo: handle debit pin entry cancelled. options: quit totally, retry as credit (swipe card again)
 */

import jpos.JposException;
import net.paymate.Revision;
import net.paymate.awtx.RealMoney;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.connection.Action;
import net.paymate.connection.ActionReply;
import net.paymate.connection.ActionReplyStatus;
import net.paymate.connection.ActionRequest;
import net.paymate.connection.ActionType;
import net.paymate.connection.AdminReply;
import net.paymate.connection.BatchReply;
import net.paymate.connection.BatchRequest;
import net.paymate.connection.ClerkIdInfo;
import net.paymate.connection.ConnectionCallback;
import net.paymate.connection.LoginReply;
import net.paymate.connection.LoginRequest;
import net.paymate.connection.PaymentReply;
import net.paymate.connection.PaymentRequest;
import net.paymate.connection.ReceiptStoreRequest;
import net.paymate.connection.StoreConfig;
import net.paymate.connection.StoreReply;
import net.paymate.connection.StoreRequest;
import net.paymate.connection.TheSinetSocketFactory;
import net.paymate.data.BatchListingFormatter;
import net.paymate.data.BinEntry;
import net.paymate.data.CardIssuer;
import net.paymate.data.EntrySource;
import net.paymate.data.PayType;
import net.paymate.data.STAN;
import net.paymate.data.SaleInfo;
import net.paymate.data.TerminalInfo;
import net.paymate.data.TransferType;
import net.paymate.data.TxnReference;
import net.paymate.io.IOX;
import net.paymate.jpos.Terminal.FormButtonData;
import net.paymate.jpos.common.JposWrapper;
import net.paymate.jpos.data.FormProblem;
import net.paymate.jpos.data.MICRData;
import net.paymate.jpos.data.MSRData;
import net.paymate.jpos.data.PINData;
import net.paymate.jpos.data.Problem;
import net.paymate.jpos.data.SigData;
import net.paymate.jpos.data.StifledException;
import net.paymate.lang.Fstring;
import net.paymate.lang.ReflectX;
import net.paymate.lang.StringX;
import net.paymate.lang.ThreadX;
import net.paymate.lang.TrueEnum;
import net.paymate.util.Ascii;
import net.paymate.util.EasyCursor;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.FormattedLineItem;
import net.paymate.util.LocalTimeFormat;
import net.paymate.util.LogSwitch;
import net.paymate.util.QActor;
import net.paymate.util.QAgent;
import net.paymate.util.QReceiver;
import net.paymate.util.TextList;
import net.paymate.util.UTC;

import java.util.Comparator;
import java.util.TimeZone;

/**
 * terminal logic
 */
public /*for terminalInfo hackers*/
    class PosTerminal extends BaseTerminal implements QReceiver, ConnectionCallback, QActor {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass( PosTerminal.class );//for all loop activity
  private static final ErrorLogStream adbg = ErrorLogStream.getExtension( PosTerminal.class, "async" );//for all external threads

  final static String rev = "$Revision: 1.437 $";

  private AutoClerk autoclerk;//quick hack, will make formalized interface some other day.

  private void presentAuth( Action action ) {
    if ( autoclerk != null ) {
      autoclerk.onReply( action );
    }
  }

  // the event sequencer:
  private QAgent incoming;//will extend this later with a class that has a gazillion specific post's

  /**
   * @return true if object accepted.
   */
  public boolean Post( Object arf ) {//MAIN access point
    System.out.println( "posting " + arf );
    return incoming.Post( arf ); //not checking types yet
  }

  /**
   * posting a terminal command <B>was</B> a drastic operation.
   * it purged the queue before posting its item.
   * it would be nice to implement a sorter for posterminal and only have
   * certain tc's blow the posterm away.
   */
  public boolean Post( TerminalCommand tc ) {
//the standin commands should not do this, so none do:    incoming.Clear();
    return incoming.putUnique( tc );
  }

  //////////////
  // display refresh levels
  interface show {
    int All = 2;
    int Change = 1;
    int Nothing = 0;
  }

  private void shower( int showlevel ) {
    switch ( showlevel ) {
      case show.All:
        formIsStale = true;//coerce redraw of forms/sigcapture pad
      case show.Change:
        showAndTell();
      case show.Nothing:
        ;//leave HID's alone.
    }
  }
  //
  //////////////
  //@todo change all names to Handle, then use reflection to do the case statement.

  public void runone( Object todo ) {//implements QActor

    String objInfo = ReflectX.ObjectInfo( todo );
    System.out.println( "RUNONE entered for a " + objInfo );
    dbg.Enter( "Processing:" + objInfo );
    int shat = show.Change;//
    try {
      if ( todo instanceof Problem ) {
        shat = handle( (Problem) todo );
      } else if ( todo instanceof PINData ) {
        shat = handle( (PINData) todo );
      } else if ( todo instanceof MICRData ) {
        shat = handle( (MICRData) todo );
      } else if ( todo instanceof MSRData ) {
        shat = handle( (MSRData) todo );
      } else if ( todo instanceof SigData ) {
        shat = handle( (SigData) todo );
      } else if ( todo instanceof FormButtonData ) {
        shat = handle( (FormButtonData) todo );
      } else if ( todo instanceof ButtonTag ) {
        shat = doButton( (ButtonTag) todo );
      }
      // originally from clerk interface
      else if ( todo instanceof ClerkIdInfo ) {
        shat = OnClerkId( (ClerkIdInfo) todo );
      } else if ( todo instanceof DrawerMenu ) {
        shat = Handle( (DrawerMenu) todo );
      } else if ( todo instanceof StoreMenu ) {
        shat = Handle( (StoreMenu) todo );
      } else if ( todo instanceof RealMoney ) {
        shat = OnSaleAmount( (RealMoney) todo );
      } else if ( todo instanceof Functions ) {
        shat = Handle( (Functions) todo );
      } else if ( todo instanceof GiftCard_Op ) {
        shat = Handle( (GiftCard_Op) todo );
      } else if ( todo instanceof PaySelect ) {
        shat = Handle( (PaySelect) todo );
      } else if ( todo instanceof SpecialOp ) {
        shat = Handle( (SpecialOp) todo );
      } else if ( todo instanceof TransferType ) {
        shat = Handle( (TransferType) todo );
      } else if ( todo instanceof EntrySource ) {
        shat = Handle( (EntrySource) todo );
      } else if ( todo instanceof SigningOption ) {
        shat = Handle( (SigningOption) todo );
      } else if ( todo instanceof ClerkCommand ) {
        shat = Handle( (ClerkCommand) todo );
      } else if ( todo instanceof Remedies ) {
        shat = Handle( (Remedies) todo );
      } else if ( todo instanceof STAN ) {
        shat = Handle( (STAN) todo );
      }
      //from paymate server
      else if ( todo instanceof Action ) {//only action source is response from server
        shat = Handle( (Action) todo );
      } else if ( todo instanceof ActionReply ) {//unsolicited things like auto-close listings
        shat = Handle( (ActionReply) todo );
      } else if ( todo instanceof StoreConfig ) {
        shat = acceptStoreConfig( (StoreConfig) todo );
      }
      // ORIGINALLY DEBUG, COOPTED FOR INTERNAL USE AS WELL:
      else if ( todo instanceof TerminalCommand ) {
        shat = Handle( (TerminalCommand) todo );
      } else if ( todo instanceof OurForm ) {
//++4debug        shat=assertForm(((OurForm)todo).Id().Value());
      } else if ( todo instanceof JposException ) {
        shat = Handle( (JposException) todo );
      } else {
        dbg.ERROR( "Unknown object" + ReflectX.ObjectInfo( todo ) );
        shat = show.All;
      }
    }
    catch ( Exception any ) {
      dbg.Caught( any );
      shat = show.All;
    }
    finally {
      dbg.VERBOSE( "shat is:" + shat );
      shower( shat );
      dbg.Exit();
      System.out.println( "RUNONE exited" );
    }
  }

  private int Handle( JposException e ) {
    String hint = e.getMessage();
    if ( hint.indexOf( "not opened" ) > 0 ) {
      showProblem( "L4100 offline", "it appears" );
      if ( jTerm instanceof JposWrapper ) {
        JposWrapper jposWrapper = (JposWrapper) jTerm;
        jposWrapper.findControls();
      }
    } else {
      showProblem( hint, "L4100" );
    }
    return show.Change;
  }

  /**
   * request that entouch return the signature.
   */
  private final int fetchSignature( String trigger ) {//@ivitrio@
    dbg.VERBOSE( " sig fetch caused by " + trigger );
    if ( sigFormShowing() ) {//only pay attention if we are expecting it
      dbg.WARNING( "attempting to fetch signature" );
      sigstate.setto( SigState.asked );
      jTerm.endSigcap( true ); //and hope that creates events to move us along...gotta finish purging jpos indirect stuff
    }
    //need to yank on JUST clerk interface to get "get sig" to go away...
    //if we change form on the enTouch we might lose the signature being fetched:
    return show.Change;
  }

  private final int Handle( EntrySource todo ) {
    sale.type.source.setto( todo.Value() );//copy value, don't hold on to reference.
    return show.Change;
  }

  private final int handle( Problem p ) {
    dbg.ERROR( "Problem:" + p );
    if ( p instanceof FormProblem ) {
      //showProblem causes the form that is a problem to be redisplayed.
//      showProblem("C.A.T. Failure","cycle enTouch");
      return show.Nothing;
    }
    if ( p instanceof StifledException ) {
      dbg.Caught( "stifled:", ((StifledException) p).getNextException() );
    }

    return show.All;
  }

  private final int Handle( STAN stan ) {
    sale.stan = STAN.NewFrom( stan.value() );
    //# do NOT attempt transaction.
    return show.All;
  }

  /**
   * a signingoption is an answer to "Get Signature"
   */
  private final int Handle( SigningOption todo ) {
    dbg.VERBOSE( "Handling SigningOption:" + todo.Image() );
    if ( tranny.is( FTstate.WaitSig ) ) {
      switch ( todo.Value() ) {
        default: //can't spend forever tracing down why "StartOver" gets invoked even when display shows donesigning
        case SigningOption.DoneSigning:
          {
            return fetchSignature( "clerk" );
          } //break;
        case SigningOption.SignPaper:
          {
            return skipSignature();//occurs only when wiating for an electronic sig.
          } //break;
        case SigningOption.StartOver:
          {
            return onBadSignature();
          } //break;
      }
    }
    return show.All; //shouldn't get here.
  }

  ////////////////////////////
  //human related data sources and sinks
  private PeripheralSet jTerm; //human interfaces
  private PrinterModel printer;// a FreqUsed piece of jTerm

  /* exposed for IPTerminal */
  ClerkUI clerkui; //sequential clerk data entry and display
//  private IdMangler id=new IdMangler();
//  private IPTerminal terminalServer=null;
  ///////////////////////////////////////

  ///////////////////////////////////////
  //configuration
  private boolean autoSale = true;    //when ending one transaction start a regular sale, +_+ omve into term caps
  private StoreConfig store;
//////////
  SigState sigstate;
  InfoReqd need;

  ////////////////////////
  //data accumulators
  private MSRData card;
  private PINData customerPin;
  private MICRData check;
//  private CheckIdInfo checkId;
  private SigData sigdata;  //+_+ get a Hancock in here. NO! not until hancock defers parsing!!!.
  private TxnReference lastTxn; //all we need to know about the previous transaction
  private TxnReference txnBeingChanged; //+_+ need to push this and others into a Uinfo

  /////////////////////////
  //transaction modes
  private SaleInfo sale;
  //orphan state flags (one's not integrated into the data they are flagging)
  private boolean customerHasAccepted = false;
  private boolean terminalLoggedin = false;//now just means 'login message has been printed'
  private int loginRetries = 0; //4debug
  private boolean checkIntended = false;
  private boolean amDying = false;
  private PaymentReply lastFrep;
  private ActionRequest tip;

  private FTstate tranny = new FTstate();//state of above action

  private void GOTO( int FTstatecode ) {//4debug
    GOTO( new FTstate( FTstatecode ) );
  }

  private void GOTO( FTstate next ) {//4debug
    dbg.VERBOSE( "STATECHANGE:" + tranny.Image() + " => " + next.Image() );
    //    dbg.ERROR("Card#tracer:"+card.toSpam());
    tranny.setto( next );
  }

  /*private*/
  ReceiptManager reception = new ReceiptManager();

  //to allow for intelligent forms changes on otherwise stateless form manager:
  private boolean formIsStale = true;

  /**
   * @return whether avs street info is desired but not present
   */
  boolean needAvsStreet() {
    return need.AVSstreet && !StringX.NonTrivial( card.avsInfo().address() );
  }

  /**
   * @return whether avs zip info is desired but not present
   */
  boolean needAvsZip() {
    return need.AVSzip && !StringX.NonTrivial( card.avsInfo().zip() );
  }

  private boolean needTrack2() {
    return need.track2 && !card.looksSwiped();
  }

  /**
   * abuses ClerkItem enumeration to describe card status
   */
  private final int evaluateCard( MSRData card ) {
    MSRData.spammarama( card, sale.wasSwiped() );//outputs to MSRdata's debug stream.
    if ( needTrack2() ) {
      return ClerkItem.PaymentSelect;
    }
    if ( card.accountNumber.isTrivial() ) {//never entered
      return ClerkItem.CreditNumber;
    }
    if ( !card.okAccount() ) {//entered value fails sanity checks
      clerkui.preLoad( ClerkItem.BadCardNumber, card.accountNumber.Image() );
      return ClerkItem.BadCardNumber;
    }
    if ( need.expiration ) {
      if ( card.expirationDate.isTrivial() ) {//never entered
        return ClerkItem.CreditExpiration;
      }
      if ( !card.okExpiry() ) {//failed validity checks
        clerkui.preLoad( ClerkItem.BadExpiration, card.expirationDate.Image() );
        return ClerkItem.BadExpiration;
      }
    }
    if ( MSRData.Cool( card, sale.wasSwiped() ) ) {//did we miss some check?
      return ClerkItem.NeedApproval;  //no, all we need now is confirmation of txn as a whole
    }
    //something really bad must have happened
    return ClerkItem.PaymentSelect;
  }

  private boolean gotMerchantReference() {
    return sale.merchantReferenceInfo() != null;
  }

  private boolean needMerchref() {
    return need.merchref && !gotMerchantReference();
  }

  private boolean allowSigCap() {//@ivitrio@
    return termInfo.allowSigCap() && jTerm.haveSigCap();
  }

  /**
   * if we need a signature and have a sigcapture device and we don't have a signature then ask for one
   * else we are waiting for an auth.
   */
  private final int sigorwait() {
    if ( sigstate.required() && allowSigCap() && !sigstate.ready() ) {
      return POSForm.SignCard;
    } else {
      return POSForm.WaitCompletion;
    }
  }

  /**
   * @return what should we be asking / showing patron?
   */
  private final int /*POSForm*/ NeedFromPatron() {
    if ( amDying ) {
      return POSForm.NotInService;
    }
    if ( !terminalLoggedin ) {
      return POSForm.NotInService;
    }
    if ( need.login && !clerk.isLoggedIn() ) {
      return POSForm.ClerkLogin;
    }
    switch ( tranny.Value() ) {
      case FTstate.DoneBad:
        return POSForm.SeeClerk;
      case FTstate.NoInfo:
        return POSForm.ClerkLogin;
      case FTstate.WaitAuth:
        return sigorwait();
      case FTstate.WaitAdmin:
        return POSForm.WaitApproval;
        //waitsig only happens wehn we ahve an auth and still need a sig
      case FTstate.WaitSig:
        return POSForm.SignCard;
      case FTstate.DoneGood:
        return POSForm.WaitCompletion;
      case FTstate.Incomplete:
        {//prompt people for enugh input to start one
          if ( sale.typeIs( TransferType.Reversal ) ) {
            return POSForm.WaitApproval;//waitCompletion was confusing.
          }

          switch ( sale.type.payby.Value() ) {
            case PayType.Debit:
              {
                if ( !card.looksSwiped() ) {
                  return POSForm.SwipeAgain;
                }
                if ( gotSaleAmount() ) {
                  if ( PINData.NonTrivial( customerPin ) ) {
                    return POSForm.WaitApproval;//should be approving already +_+
                  } else {
                    return POSForm.WaitPatronDebit;
                  }
                } else {
                  return POSForm.WaitClerkDebit;
                }
              } //break;

            case PayType.GiftCard:
              {

              }
              break;

            case PayType.Credit:
              {
                int arf = evaluateCard( card );
                if ( gotSaleAmount() && !needAvsinfo() && !needMerchref() ) {
                  if ( arf != ClerkItem.NeedApproval ) {
                    //something is wrong with card
                    return POSForm.SwipeAgain;
                  }
                  if ( customerHasAccepted ) {
                    dbg.WARNING( "@sigorwait should be authing" );
                    return sigorwait();//expect to be requesting something soon
                  } else {
                    return POSForm.WaitPatronCredit;
                  }
                } else {
                  return POSForm.WaitClerkCredit;
                }
              }

//          case PayType.Check: {
//            if(sale.money.isValid()){
//              if(!id.isOk()){
//                return POSForm.NeedID;
//              }
//              if(need.customerOk){
//                return POSForm.WaitApproval;//should be approving already +_+
//              } else {
//                return POSForm.WaitPatronCheck;
//              }
//            } else {
//              return POSForm.WaitClerkCheck;
//            }
//          }
            default:
            case PayType.Unknown:
              {
                return POSForm.IdleAd;
              }
          } //end switch paytype
        } //break; //case Inompletye
    }//end switch FTstate
    return POSForm.FubarForm;//--- hopefully the equivalent of elevator music ... (eg girl from iponema on an organ (sp))
  }

  private boolean expectingIDcard = false;

  private String wap = "*-*-*";//4debug

  private final int wait( String why ) {
    clerkui.preLoad( ClerkItem.WaitApproval, wap = why );
    return ClerkItem.WaitApproval;//4inline return
  }

  private final int whyNotLoggedIn() {
    return ClerkItem.BootingUp;
  }

  private boolean gotSaleAmount() {
    return sale.haveAmount();
  }

  ClerkItem preemptiveQuestion = new ClerkItem();

  private void exitMenu() {
    preemptiveQuestion.Clear();
  }

  private void enterMenu( int clerkitem ) {
    preemptiveQuestion.setto( clerkitem );
  }

  private static ErrorLogStream dbprompt;

  /**
   * @return what we should be prompting the clerk for when a signature is of interest.
   * @todo: maybe this can be a simple negation of one of the states...
   */
  private int clerkSigWait() {
    switch ( sigstate.Value() ) {
      case SignatureState.onwire:
      case SignatureState.acquired:
        return ClerkItem.ApprovalCode;
      default:
        return ClerkItem.NeedSig;
    }
  }

  private final int /*ClerkItem*/ NeedFromClerk() {//scan state and ask for most important item
    dbg.Enter( "NeedFromClerk" );
    try {

      if ( amDying ) {
        dbprompt.VERBOSE( "amDying" );
        return ClerkItem.Problem; //which should already have the reason for death
      }
      if ( preemptiveQuestion.isLegal() ) {
        dbprompt.VERBOSE( "preempted by:" + preemptiveQuestion.toSpam() );
        return preemptiveQuestion.Value();
      }

      if ( !terminalLoggedin ) {
        dbprompt.VERBOSE( "terminal not LoggedIn" );
        return whyNotLoggedIn();
      }
      //moved second copy ahead of clerkid for sake of autologout systems.
      //printing second copy is not priveleged so this is perfectly cool.
      if ( need.AnotherCopy ) {//have to use a keystroke to pace multiple copies as printer doesn't cut
        dbprompt.VERBOSE( "expediting 2nd receipt" );
        return ClerkItem.SecondCopy;
      }

      if ( need.login && !clerk.isLoggedIn() ) {
        dbprompt.VERBOSE( StringX.bracketed( "need clerk login ..{", clerk.idInfo.toSpam() ) );
        if ( clerk.idInfo.NonTrivial() ) {
          dbprompt.VERBOSE( "... have info awaiting response" );
          return wait( "Clerk Login" );
        }
        if ( StringX.NonTrivial( clerk.idInfo.Name() ) ) {
          dbprompt.VERBOSE( "have name need passcode" );
          return ClerkItem.ClerkPasscode;
        }
        dbprompt.VERBOSE( "know nothing, need name" );
        return ClerkItem.ClerkID;
      }

      dbprompt.VERBOSE( "FTState:" + tranny.Image() );
      switch ( tranny.Value() ) {
        case FTstate.DoneBad:
          return ClerkItem.Problem; //which will be "declined" or such
          //the following is the state when admin Functions are active
        case FTstate.WaitAuth:
          return wait( "sale/return/void" );
          //wiatsig only happens wehn we ahve an auth and still need a sig
        case FTstate.WaitAdmin:
          return ClerkItem.WaitAdmin;
        case FTstate.WaitSig:
          return clerkSigWait();
        case FTstate.DoneGood:
          return ClerkItem.ApprovalCode;
        case FTstate.NoInfo:
          return ClerkItem.SaleType;//aka "functions"
        case FTstate.Incomplete:
          {//prompt people for enugh input to start one
            dbg.ERROR( "FT incomplete:" + sale.type.toSpam() );
            if ( need.xfertype && sale.type.payby.is( PayType.GiftCard ) ) {//GiftCard operations menu
              dbprompt.VERBOSE( "SVC forced, awaiting operation for it" );
              return ClerkItem.SVOperation;
            }
            if ( need.xfertype ) {//should ne in NoInfo state
              return ClerkItem.SaleType;
            }
            if ( need.authcode && !sale.hasPreapproval() ) {
              return ClerkItem.PreApproval;
            }
            if ( need.origref ) {
              if ( !TxnReference.NonTrivial( txnBeingChanged ) ) {
                return ClerkItem.RefNumber;
              }
            }
            if ( need.amount && !gotSaleAmount() ) {
              return ClerkItem.SalePrice;
            }
            if ( needMerchref() ) {
              return ClerkItem.MerchRef;
            }
            if ( !sale.type.isComplete() ) {
              return ClerkItem.PaymentSelect; //+_+ crude
            }
            if ( needTrack2() ) {
              return ClerkItem.PaymentSelect;
            }
            if ( need.cardnum ) {
              if ( card.accountNumber.isTrivial() ) {//never entered
                return ClerkItem.CreditNumber;
              }
              if ( !card.okAccount() ) {//entered value fails sanity checks
                clerkui.preLoad( ClerkItem.BadCardNumber, card.accountNumber.Image() );
                return ClerkItem.BadCardNumber;
              }
            }
            if ( need.expiration ) {
              if ( card.expirationDate.isTrivial() ) {//never entered
                return ClerkItem.CreditExpiration;
              }
              if ( !card.okExpiry() ) {//failed validity checks
                clerkui.preLoad( ClerkItem.BadExpiration, card.expirationDate.Image() );
                return ClerkItem.BadExpiration;
              }
            }
            if ( need.PIN && !PINData.NonTrivial( customerPin ) ) {
              return ClerkItem.NeedPIN;
            }

            if ( needAvsStreet() ) {
              return ClerkItem.AVSstreet;
            }
            if ( needAvsZip() ) {
              return ClerkItem.AVSzip;
            }
            if ( need.usersok && !customerHasAccepted ) {
              return ClerkItem.NeedApproval;
            }
            return wait( "authorization" ); //+_+ shouldn't be able to get here if actor is idle

//          if(check.isPresent) {
//            if(!check.TransitOk())  return ClerkItem.CheckBank; //a misnomer
//            if(!check.AccountOk())  return ClerkItem.CheckAccount; //may insist on rescan if >1
//            if(!check.SerialOk())   return ClerkItem.CheckNumber; //may insist on rescan if >1
            //          urg.Message("Picking question idOk is:"+id.isOk());
//            if(!id.isOk())          return ClerkItem.License;//+_+ no state input yet
//            return wait("check approval"); //+_+ shouldn't be able to get here if actor is idle
//          }
//          dbg.ERROR("Unknown sale state");
          }// break;
      }
      dbg.ERROR( "NC: tranny=" + tranny.Image() + sale.spam() );
      showProblem( "Sorry,try again", tranny.Image() );//first text must be 16 chars..
      return ClerkItem.Problem;
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * @return whether the operation in progress is allowed with a debit card
   */
  private boolean debitPossible() {
    return store != null && store.termcap != null && store.termcap.debitAllowed //debit enabled for store
        && sale != null && sale.type.canBeDebit()  //transaction is of a type that can be done with a debit card
        && card != null && card.looksSwiped();    //card must be swiped for debit.
  }

  private final void showAndTell() {//just about always called these guys together:
    int quid = NeedFromClerk();
    ClerkItem clrkItem = new ClerkItem( quid );
    Inform( "Asking for: " + clrkItem.Image() + "[" + quid + "] / " + clerkui.QuestionFor( quid ).prompt );
    if ( autoclerk != null && autoclerk.ask( quid ) ) {//if autoclerk answers question then put up a "wait" on clerkpad
      quid = wait( "Auto" + Ascii.bracket( quid ) );
    }

/// apply dynamic info to question.
    switch ( quid ) {  //some questions get modified before being asked each time:
      case ClerkItem.MerchRef:
        {//per store name for this field
          clerkui.splicePrompt( quid, store.termcap.MerchRefPrompt, "" );
        }
        break;
      case ClerkItem.SalePrice:
        {//modify prompt for type of sale
          clerkui.splicePrompt( quid, sale.amountHint(), theSaleAmount().Image() );
        }
        break;
      case ClerkItem.ApprovalCode:
        {//put approval code on display in case printer has failed.
          if ( lastFrep != null ) {
            clerkui.splicePrompt( quid, lastFrep.Approval(), lastFrep.refNum() );
          } else {
            clerkui.splicePrompt( quid, "Ok", "not financial" );
          }
        }
        break;
      case ClerkItem.RefNumber:
        {
          if ( lastFrep != null ) {
            clerkui.preLoad( quid, lastFrep.refNum() );//dynaprompt didn't seem to work.
          }
        }
        break;
    }

    int /*POSForm*/ formIndex;
//below we are using "ClerkLogin" to lock patron out when menu is showing, should create a form for that.
    formIndex = clrkItem.is( ClerkItem.SaleType ) ? POSForm.ClerkLogin : NeedFromPatron();//4debug
    OurForm currentForm = OurForms.Find( formIndex );
    if ( currentForm == null ) {
      dbg.ERROR( "no form for " + formIndex );      //single fault. Presume forms system is broken.
      currentForm = OurForms.Find( POSForm.FubarForm );//and we will blow if that doesn't exist
      if ( currentForm == null ) {
        dbg.ERROR( "Forms are really screwed up" );      //double fault. Presume forms system is broken.
        return;
      }
    }

    dbg.WARNING( currentForm.toSpam() );

    Uinfo uinfo = new Uinfo();//#do NOT make this a member, we want to forget its contents after each use.
    uinfo.sale = sale;
    uinfo.card = card;
    uinfo.tref = txnBeingChanged;//+_+ needs review
    uinfo.auth = lastFrep != null ? lastFrep.auth() : null;
    uinfo.expectingIDcard = expectingIDcard = currentForm.isSwiper && currentForm.idSwipe;
    uinfo.debitAllowedHack = debitPossible();

    if ( !jTerm.updateInterfaces( clrkItem, currentForm, formIsStale, uinfo ) ) {//if no interference between forms and clerk interface
      clerkui.ask( quid );//then also update clerk interface.
    }

    formIsStale = false;
  }

  private final void manCheck( int cci, String micrfield ) {
    switch ( cci ) {
      case ClerkItem.CheckBank:
        check.Transit = micrfield;
        return;
      case ClerkItem.CheckAccount:
        check.Account = micrfield;
        return;
      case ClerkItem.CheckNumber:
        check.Serial = micrfield;
        return;
    }
  }

  //////////////////////////////////////////////////////
  /**
   * slight misnomer... when invoked this prints the customer copy so that the
   * store can keep the first printout.
   */
  private final int printSecondCopy() {
    reception.secondCopy();
    need.AnotherCopy = false;
    return cleanAll();//and return to base state.
  }

  /**
   * process a clerk login attempt
   */
  private final int OnClerkId( ClerkIdInfo cid ) {//
    dbg.Enter( "onClerkId" );
    try {
      if ( ClerkIdInfo.NonTrivial( cid ) ) {
        clerk = new Clerk();
        clerk.idInfo = cid;
        dbg.VERBOSE( cid.toSpam() );
        //idiot check entry here

        if ( !sendRequest( "Clerk Id", new LoginRequest() ) ) {
          clerk.idInfo.killPassword(); //stops infinite approvla at DrawerClosing.
          return show.All; //gotta get a fresh prompt or it looks like we have frozen
        }
      }
      return show.Change;
    }
    catch ( Exception ex ) {
      dbg.Caught( ex );
      clerk.idInfo.killPassword();//stops infinite approvla at DrawerClosing.
      return show.All;//gotta get a fresh prompt or it looks like we have frozen
    }
    finally {
      dbg.Exit();
    }
  }

  private final void lostReceipt() {
    showProblem( "Rcpt not saved", "print one 2 keep" );
    need.AnotherCopy = true;
  }

  private final int lateCancel() {//cancel after tranny might have been sent
    //we agreed to merge receipt with the following void --   finishReceipt(false);//push paper so far
    if ( lastFrep != null && !lastFrep.transferType.is( TransferType.Reversal ) ) {
      //      issueVoid(lastFrep.tref(),false);  //we are trusting that this question is only asked at appropriate times
      //haven't done signand shove yet.
      finishReceipt( false ); //only happens when hypercom faults during signing and someone hits CLEAR
    } else {
      //should somehow inform clerk that we ignored their request.
    }
    return show.All;
  }

  /**
   * erase any data related to patron.
   */
  private final void clearCustomerData() {
    customerHasAccepted = false;
    card.Clear();
    check.Clear();
//    id.Clear();
    customerPin.Clear();
    sale.Clear();//ancinet bug! absense caused swipe before refund wierdnesses
// can't do this yet, need for receipt and this function is called when auth is issued   clearSigstuff(false);
  }

  /**
   * forget all about signing
   */
  private void clearSigstuff() {
    sigdata = null;
    sigstate.setto( false );
  }

  private final int cleanUp() {
    dbg.VERBOSE( "onCancel" );
    exitMenu(); //in cleanUp:  to be sure. too many pathways to analyze to trust removing this.
    clerkui.Clear();
    sale.Clear();
    txnBeingChanged = null;
    clearCustomerData();
    clearSigstuff();
    if ( tranny.is( FTstate.WaitSig ) ) {
      reception.printer.formfeed(); //NOT a clean eject.
    }
    need.Nothing();
    if ( autoSale ) {
      normalSale();
    } else {
      NoSale();//functions menu.
    }
    return show.All;
  }

  private final int cleanAll() {
    if ( store != null && store.termcap != null && store.termcap.enAutoLogout() ) {//null checks needed for first call, before connection completes
      clerk.Clear( true );//we could add option to clear just password
    }
    return cleanUp();
  }

  private final int onAmountOk() {// customer sez ok
    dbg.VERBOSE( "Amount Ok'd" );
    customerHasAccepted = true; //customer approves
    return attemptTransaction(); //send it off to have it approved
  }

  private final int onAmountOk( String passcode ) {//clerk is stating that customer ok's
    dbg.VERBOSE( "onAmountOk:" + passcode );
    if ( Appliance.StoreInfo().termcap.freePass() || clerk.Passes( passcode ) ) {
      return onAmountOk();
    } else {
      dbg.VERBOSE( "clerk ok'ing amount ignored." );
      return show.All;//clerk is impatient with patron
    }
  }

  private final RealMoney theSaleAmount() {//fue
    return sale.Amount();
  }

  private final int OnSaleAmount( RealMoney cents ) {//from clerkui,
    sale.setMoney( cents );
    if ( sale.typeIs( TransferType.Modify ) ) {
      if ( TxnReference.NonTrivial( txnBeingChanged ) ) {
        return issueModify( txnBeingChanged, cents );
      } else {
        return show.Change;
      }
    } else {
      if ( cents.Value() == 0 ) {
        clerkui.gotoFunction();
        return show.Change;
      } else {
        if ( store.termcap.autoApprove() && !store.termcap.doesDebit() && sale.isSimpleSale() ) { // only exclude credit from customer acknowledge
          return onAmountOk(); //which will also attemptTranscaction
        } else {
          return attemptTransaction();
        }
      }
    }
  }

  /**
   * does this transfer type need a login?
   * @return true when there must be a clerk of some kind "N_eed C_lerk L_ogin
   */
  private boolean ncl( int tft ) {
    switch ( tft ) {
      case TransferType.Authonly:
        return true;
      case TransferType.Sale:
        return true;
      case TransferType.Return:
        return true;
      case TransferType.Reversal:
        return true;
      case TransferType.Modify:
        return true;
      case TransferType.Force:
        return true;

      case TransferType.Unknown:
        return false;
      case TransferType.Query:
        return false;
      default:
        return false;
    }
  }

  private final int Handle( TransferType todo ) {
    return setTransferType( todo.Value() );
  }

  private final int setTransferType( int /*TransferType*/ tft ) {
    //we now erase all sale data when changing the mode of the sale.
    clearCustomerData();//fixes "card number used on next transaction" bug.
    clearSigstuff();
    if ( tft == TransferType.Unknown ) {
      GOTO( FTstate.NoInfo );
      //preemptiveQuestion.setto(preemptiveQuestion.Invalid());//+++ who needed this?
    } else {
      GOTO( FTstate.Incomplete );
    }
    sale.type.op.setto( tft );//this needs to be the only instance of this in this module!
    need.xfertype = !sale.typeIsKnown();//neither trash nor unknown
    need.login = ncl( tft );
    need.paytype = sale.type.cardReqd(); //credit, debit
    need.origref = !sale.type.cardReqd();
    need.merchref = store.termcap.enMerchRef; //can cross reference on voids and modifies.
    need.amount = sale.type.amountReqd();
    need.authcode = sale.typeIs( TransferType.Force );
    need.cardnum = sale.type.cardReqd();
    need.usersok = sale.type.cardholderReqd();//under some terminal configurations we will ignore this via forcing the related status bit.
    need.expiration = need.cardnum;//may change when we find out the paytype
    need.track2 = false;//may change when we find out the paytype
    need.AVSstreet = need.AVSzip = false;//may change when we are given a manually entered card #
    need.PIN = false; //may change when we find out the paytype
    need.sig = false; //may change when we find out the paytype
    return show.Change;
  }

  private final void startPinEntry() {//@ivitrio@
    dbg.ERROR( "in start pin pad" );
    jTerm.startPinEntry( card.accountNumber, theSaleAmount(), sale.typeIs( TransferType.Return ) );
  }

  private final void freshReceipt( PaymentRequest request ) {
    reception.start( request, printer, termInfo, clerk.idInfo );
    sigdata = null;
    sigstate.setto( store.signFor( request.Amount() ) && request.getsSignature() );
    dbg.VERBOSE( "freshreceipt, sigstate:" + sigstate );
  }

  private int issueChangeRequest( PaymentRequest changer, boolean fresh ) {
    if ( !sendRequest( "sending change", changer ) ) {
      Inform( "Reversal/Modify not sent!" );
    } else {
      if ( fresh ) {
        freshReceipt( changer );
      } else {
        //adding to partial receipt. (lateCancel)
        sigstate.setto( SigState.moot );//per 20011012 discussions
        reception.modifyReceiptInProgress( changer );
      }
    }
    return show.Change;
  }

  private final int issueVoid( TxnReference original, boolean fresh ) {
    PaymentRequest marf = PaymentRequest.Void( original );
    return issueChangeRequest( marf, fresh );
  }

  private final int issueModify( TxnReference original, RealMoney amount ) {
    //disallow modifies when in standin.
    if ( Standin().online() ) {//expedite, could leave this up to a lower layer to deal with.
      return issueChangeRequest( PaymentRequest.Modify( original, amount ), true );
    } else {
      showProblem( "OFFLINE", "try again later" );
      return show.All;
    }
  }

  private final int startForce( String authcode ) {
    sale.preapproval = authcode;
    if ( StringX.NonTrivial( sale.preapproval ) ) {
      //is it a reasonable code?
      return show.Change;
    } else {
      return show.All; //assume human is confused
    }
  }

  /**
   * originally called when doing voids, now called with any retrospective transaction
   */
  private final int onRefNumber( String stan ) {
    dbg.Enter( "onVoid" );
    TxnReference original = TxnReference.New( termInfo.id(), stan );
    try {
      switch ( sale.type.op.Value() ) {
        case TransferType.Reversal:
          {
            issueVoid( original, true );
          }
          break;
        case TransferType.Modify:
          {
            if ( cando( TransferType.Modify ) ) {//+_ should check earlier
              txnBeingChanged = original;
            } else {
              showProblem( "NOT PERMITTED", "see manager" );
            }
          }
          break;
      }//end switch
      return show.Change;
    }
    catch ( Exception caught ) {
      dbg.Caught( caught );
      return show.All;
    }
    finally {
      dbg.Exit();
    }
  }

  private final ActionRequest VoidForReply( PaymentReply reply ) {
    return PaymentRequest.Void( reply );
  }

  private final int interpretReply( PaymentReply reply ) {
    dbg.Enter( "interpretReply" );
    try {
      reception.onReply( reply );
      if ( reply.auth().isApproved() ) {
        lastFrep = reply; //only save successful ones.
        if ( sigstate.required() ) {
          dbg.VERBOSE( "signing is req'd" );
          //@todo .. if capturing on papaer then printStoreCopy() else doing electronic capture.
          if ( sigstate.ready() ) {//signed before approavl received.
            dbg.VERBOSE( "storing signature, presigned" );
            return finishReceipt( true );
          } else {
            dbg.VERBOSE( "and we will wait for it." );
            if ( allowSigCap() ) {
              GOTO( FTstate.WaitSig );
            } else {
              skipSignature();//skip WAITING FOR ELECTRONIC Signature.(includes a finishReceipt)
            }
          }
        } else {
          return finishReceipt( false );//receipts that don't get signed due to nature of operation.
        }
      } else {
        clearSigstuff();//make 'em sign again in case we skip approval screen
        dbg.ERROR( "REJECTED:" + reply.Approval() );
        clerkui.onRejected( reply.Approval() );
        GOTO( FTstate.DoneBad ); //this controls the human views
        reception.firstCopy( true );//simple receipt
        clearCustomerData();
      }
      dbg.VERBOSE( "nothing more to do" );
      return show.Change;
    }
    catch ( Exception arf ) {
      dbg.Caught( arf );
      //+++ change state to break infinite loops!
      return show.All;
    }
    finally {
      dbg.Exit();
    }
  }

/* whenever we are uncertain of the state, and think we might be getting an "infinite wait approval"
   we insert a call to this.
 */
  private final void quitWaiting() {
    if ( tranny.is( FTstate.WaitAuth ) ) {
      GOTO( FTstate.DoneBad );//to break possible lockup.
    }
  }

  /**
   * was broken out so that configurations without receipt generation can still do what need
   */
  private final int txnDone() {
    if ( store.termcap.beDoneIfApproved() ) {
      cleanAll(); //on receipt SENT @2c@
    } else {
      GOTO( FTstate.DoneGood );//wait until receipt acknowledged.
    }
    return show.All;
  }

  private void issueSecondCopy() {
    if ( jTerm.printerCuts() ) {
      printSecondCopy(); //can print immediately
    } else {
      need.AnotherCopy = true;
    }
  }

  private final int finishReceipt( boolean signed ) {//store to server.
    try {
      if ( signed ) {
        reception.firstCopy( sigdata );
      } else {
        reception.firstCopy( sigstate.is( sigstate.moot ) );
      }
      GOTO( FTstate.DoneGood ); //no longer watiing for signatre
      if ( termInfo.force2ndcopy() ) {//force 2nd printing regardless of all of our intelligent invocation of that.
        issueSecondCopy();
      }
      //either way we are done with the current signature:
      clearSigstuff(); //keeps signcard screen from cycling back into view.
      if ( reception.shouldbeSaved() ) {
        dbg.WARNING( "storing receipt for:" + lastFrep.tref().toSpam() );
        ReceiptStoreRequest ar = ReceiptStoreRequest.New( reception.Receipt(), lastFrep.tref() );
        if ( !sendRequest( "store receipt", ar ) ) {
          dbg.ERROR( "failed to store receipt:" + lastFrep.refNum() );
          lostReceipt();//i.e. the receipt was not sent to the server.
        }
      }
    }
    finally {
      dbg.VERBOSE( "finishReceipt: FTState = " + tranny.Image() + ", beDoneIfApproved = " + store.termcap.beDoneIfApproved() );
      return txnDone();
    }
  }

  /**
   * only call if trivial signatures don't upset you
   */
  private final int onSignature( SigData siggy ) {
    sigdata = siggy;
    sigstate.setto( SigState.gotten );
    dbg.VERBOSE( "sig received while:" + tranny.Image() );
    if ( tranny.is( FTstate.WaitSig ) ) {
      return finishReceipt( true );
    }
    return show.Change;
  }

  private boolean sigFormShowing() {//used by 'signature expected' and onBadSignature() to determine how much to refresh
    return jTerm.gettingSignature();
  }

  /**
   * we should only get here if signature was desired, then possibly acquired then deemed defective
   */
  private final int onBadSignature() {
    try {
      return sigFormShowing() ? show.All : show.Change;
    }
    finally {
      sigdata = null; //erase the trash
      sigstate.setto( SigState.need );
    }
  }

  private final int skipSignature() {//clerk is skipping sig cap step//@ivitrio@
    try {
//      if(tranny.is(FTstate.WaitSig)){
      finishReceipt( false );
      issueSecondCopy(); //when we have a customer-requested manual signing slot then we ask for a second copy
//      }
    }
    finally {
      return show.All;
    }
  }

  private final void saleIfUnknown() {
    if ( sale.typeIs( TransferType.Unknown ) ) {
      setTransferType( TransferType.Sale );//can't afford to bypass shared setting routine.
    }
  }

  /**
   * @param paytype     is our best guess as to what it is, server may override us later.
   * @param entrysource is source type when notManual. It would be nice to reduce entrysource to a boolean
   *                    its present enumeration is legacy of someone else's interface.
   */
  private final int onPaymentPresented( PayType paytype, boolean notManual ) {
    saleIfUnknown();//set transfer type to sale, must now preceed setting paytype

    //dbg.ERROR("have card at on paymentpresenet:"+MSRData.beenPresented(card));
    sale.type.setto( paytype ).setto( new EntrySource( notManual ? EntrySource.Machine : EntrySource.Manual ) );

    switch ( sale.type.payby.Value() ) {//special demonic behavior depending upon card type
      case PayType.GiftCard:
        need.track2 = true;//no manual gift cards yet. This really should be a store config boolean
        need.expiration = false;
        need.PIN = false;
        need.sig = false;
        if ( store.termcap.autoQuery() && !gotSaleAmount() ) {//needs tighter restriction!+++
          return startBalanceQuery();//gets "unknwon sale state"
        }
        break;
      case PayType.Debit:
        if ( !store.termcap.doesDebit() ) {//---  too severe
          return showProblem( "DEBIT CARD!", "not enabled" );//let it be severe, used to be that way
        } else {
          need.track2 = true;
          need.expiration = false;
          need.PIN = true;
          need.sig = false;
        }
        break;
      case PayType.Credit:
        {
          need.track2 = false;
          need.expiration = true;
          need.PIN = false;
          need.sig = true;
          need.AVSzip = termInfo.askforAvs() && !notManual;
          need.AVSstreet = need.AVSzip;
          if ( store.termcap.autoApprove() ) {//if they swipe we take their money...
            customerHasAccepted = true;//on credit when skipping approval question
          }
        }
        break;
      default:
        break;
    }
    return attemptTransaction();
  }

  private final int onPaymentPresented( int paytype, boolean notManual ) {
    return onPaymentPresented( new PayType( paytype ), notManual );
  }

  private final int onCheck( MICRData theScan, boolean reallyScanned ) {
    check = theScan;
    check.isPresent = true;//+_+ expected the scan to be marked already
    return onPaymentPresented( PayType.Check, reallyScanned );
  }

  private final int startSomeSale( int paytype, boolean manualling ) {
    try {
      sale.type.setto( new PayType( paytype ) );
      if ( manualling ) {
        manHandled();
      }
      saleIfUnknown();
    }
    catch ( NullPointerException npe ) {
      //we get these during startup, and don;t care if this function works anyway.
      dbg.WARNING( "NPE in startSomeSale" );
    }
    finally {
      return show.Change;
    }
  }

  private final int startBalanceQuery() {
    dbg.VERBOSE( "startBalanceQuery" );
    sale.type.setto( new PayType( PayType.GiftCard ) );
    setTransferType( TransferType.Query );
    customerHasAccepted = true;//balance query
    //but not our job to worry about whether card is already swiped
    return attemptTransaction();
  }

  private final int startCreditSale( boolean manly ) {
    if ( manly ) {
      card.Clear();
      card.beenPresented( true );
    }
    return startSomeSale( PayType.Credit, manly );
  }

  private final int startDebitSale() {//+++ missing switch from credit
    dbg.ERROR( "FINALLY trying debit" );
    //if clerk has already entered sale amount
    //or card has been swiped
    //just change the card mode and put up pinpad
//    EntrySource.Machine
    if ( sale.wasSwiped() ) {
      return startSomeSale( PayType.Debit, false ); // alwasy auto
    } else {
      return show.All;//shouldn't be able to get here.
    }
  }

  /**
   * forget everything except the transfertype and amount, should get us to "Swipe Card"
   */
  private final int cancelDebit() {
    card.Clear();//is this enough to let them swipe a credit card if they forgot a pin?
    sale.type.payby.setto( PayType.Unknown );
    sale.type.source.setto( EntrySource.Unknown );
    return attemptTransaction();
  }

  private final int startCashSale() {
    return startSomeSale( PayType.Cash, true ); //always manual (data entry)
  }

  private final int startCheckSale( boolean manly ) {
    if ( manly ) {
      check.Clear();
      check.isPresent = true; //back door to faking it.
    }
    need.id4check = store.termcap.AlwaysID();
    return startSomeSale( PayType.Check, manly );
  }

  private final int onCard( MSRData theSwipe, boolean reallySwiped ) {
    card.setto( theSwipe );
    if ( reallySwiped ) {
      card.ParseFinancial();
      card.accountNumber.mootSum();//don't check on swipes, at least not until bin table works better
      card.expirationDate.moot();//ditto, binEntry not correct yet for 581477
    } else {
      card.clearTracks();   //double safe: erase tracks
      card.ServiceCode = "101"; //fixup for ServiceCode check for amex in standin.
      card.beenPresented( true );
    }
    BinEntry guess = BinEntry.Guess( card );
    dbg.WARNING( "Card Guess:" + (guess != null ? guess.toSpam() : "null") );
    if ( CardIssuer.isIssuer( guess ) ) {
      PayType pt = new PayType( guess.act );
      if ( !guess.expires ) { //if card does not have expiration date
        dbg.WARNING( "mooting expiration date" );
        card.expirationDate.moot();
      }
      if ( !guess.enMod10ck ) {
        card.accountNumber.mootSum();//trusting bin table
      }
      return onPaymentPresented( pt, reallySwiped );
    } else {
      dbg.WARNING( "Not a bank-card" );
    }
    return show.All;
  }

  private final int onApproval( PaymentReply reply, PaymentRequest request ) {
    return interpretReply( reply );
  }

  /**
   * @return show.All
   */
  private final int showProblem( String one, String two ) {
    GOTO( FTstate.DoneBad );
    //16 is 'displayWidth' +_+
    clerkui.showProblem( Fstring.centered( one, 16, '*' ), two );
    return show.All;
  }

  /**
   * @note: all control paths MUST GOTO(FTstate.soemthing_not_waitadmin)
   * presently this is covered by cleanAll() and independently by all cases of the switch going through showProblem.
   */
  private final int onFailure( Action action ) {//return possible remedial request
    try {
      ActionReplyStatus why = action.reply.status; //fue
      switch ( why.Value() ) {
        default:
          {
            showProblem( "NETWORK ERROR", action.reply.status.Image() );//alh:even if the error is from an admin operation.
          }
          break;
        case ActionReplyStatus.InvalidLogin:
          {
            showProblem( "INVALID LOGIN", "Type more Slowly" );//gets overridden by login prompt
            clerk.Clear();//else locks with "APPROVING..."
          }
          break;
        case ActionReplyStatus.InvalidTerminal:
          {
            showProblem( "CALL FOR SERVICE", "Try Cycling Power" );
            //          return null;
          }
          break;
      }
      reception.fault( action.reply, printer );
      switch ( action.request.Type().Value() ) {//tiny failure demons go here
        case ActionType.clerkLogin:
          {//must clear else we get infinite "approving...Clerk Login loop"
            clerk.Clear();
          }
          break;
      }
      return show.All;
    }
    finally {
      cleanAll();//#GAWD not having this was a big bug! we were dependent upon a clerk pressing CLEAR to do this. @2c@
    }
  }

  /**
   * @param reply is the cfg from a ConnectionReply
   */
  private final int acceptStoreConfig( StoreConfig reply ) {
    dbg.Enter( "StoreConfig" );
    try {
      //4 configuration debug
//      clerkui.preLoad(ClerkItem.ClerkID,termInfo.getNickName());
//      clerkui.preLoad(ClerkItem.ClerkPasscode,""+termInfo.id());
      //end configuration debug
      connectionClient.setStoreInfo( reply.si );
      jTerm.setStoreInfo( reply.si );
//      id.setLocale(reply.si.State);
      store = reply;
      //rework some forms:
      if ( OurForms.applyOptions( reply ) ) {//return tells us forms changed
// need to write the code as if all forms stored before we cut the following line //let user do service function
        FormSetUp();
      }
      // set receipt info
      reception.setOptions( reply.receipt, reply.si.timeZoneName, reply.termcap.MerchRefPrompt() );
      // set the time format for the check screen:
      OurForms.ltf = LocalTimeFormat.New( TimeZone.getTimeZone( reply.si.timeZoneName ), reply.receipt.DefaultTimeFormat );
      if ( !terminalLoggedin ) {//show terminal stuff
        try {
          Receipt loggerin = reception.AdminReceipt();
          loggerin.setItem( reply, termInfo, connectionClient.online() );
          loggerin.print( printer, 0 );
        }
        catch ( Exception t ) {
          // don't die if this fails (the printout is just for testing, anyway)
        }
      }
      terminalLoggedin = true;
      {//because of this block setting store config while transacting may screw up a txn-in-progress, so don't do that!
        cleanUp();//+_+ can we afford to delete this block?.. No, //@2c@ok
        //but we can choose to delay processing of this data until the txn completes.
      }
      return show.All;
    }
    finally {
      dbg.VERBOSE( store.toSpam() );
      dbg.Exit();
    }
  }

  private final void showIdentity() {//@ivitrio@
    String ME = termInfo.getNickName();//+++ finish terminfo spam and use it here
    Inform( ME );
    jTerm.showIdentity( ME );
    ThreadX.sleepFor( 10.0 );//in case some other event is about to fire
  }

  private boolean cleanCard() {
    if ( !sale.wasSwiped() ) {//card but not swiped ...
      Inform( "Manual Card request" );
      //security: (make sure swipe data erased if clerk does any manual entry)
      card.clearTracks();
    } else {
      dbg.VERBOSE( "card age is:" + card.age() );
    }
    return MSRData.Cool( card, sale.wasSwiped() );
  }

  /**
   * @return true if check data can be accepted
   */
  private final boolean readyForCheck() {
    return tranny.is( FTstate.Incomplete );//very pessimistic for now.
  }

  ///////////////////////////////////////////////////////////////////
  /**
   * intended to lock out other transactions
   */
  boolean amTransacting() {
    switch ( tranny.Value() ) {
      case FTstate.WaitAuth:
      case FTstate.WaitSig:
      case FTstate.DoneGood:
      case FTstate.DoneBad:
        return true;
      case FTstate.NoInfo:
      case FTstate.Incomplete:
      default:
        return false;
    }
  }

  /**
   * ensure that there is a valid stan on this request
   */
  private void assertStan( PaymentRequest request ) {
    if ( STAN.isValid( request.sale.stan ) ) {
      //externally supplied stan
    } else {
      request.sale.stan = STAN.NewFrom( newClientStan() );
    }
  }

  /**
   * @return whether avs ifo is desired but not present
   */
  protected boolean needAvsinfo() {
    return needAvsZip() || needAvsStreet();
  }

  protected int attemptTransaction() {//a financial transaction, not an admin.
    try {
      dbg.Enter( "attemptTransaction" );
      if ( dbg.levelIs( LogSwitch.VERBOSE ) ) {//the following expression is expensive to create...
        dbg.VERBOSE( need.toString() );
      }
      if ( amTransacting() ) {
        Inform( "Already Transacting" );
        return show.Change;
      }
//      else
      if ( !clerkOk() ) {
        Inform( "Clerk Id Info missing?" );
        return show.Change;
      }
//      else
      if ( !sale.type.isComplete() ) {
        Inform( "What type of sale???" );
        return show.Change;
      }
//      else
      if ( !gotSaleAmount() ) {
        Inform( "Sale amount not set" );
        return show.Change;
      }
//      else
      if ( needMerchref() ) {
        Inform( "MerchRef not set" );
        return show.Change;
      }
      if ( needAvsinfo() ) {
        Inform( "need AVS" );
        return show.Change;
      }
//      else
      if ( need.usersok && !customerHasAccepted ) {
        Inform( "Waiting on customer approval" );
        return show.Change;
      }
//      else
      { //look deeper
        PaymentRequest request;

        switch ( sale.type.payby.Value() ) {
          default:
            {
              Inform( "Not Yet Implemented!" );
            }
            return show.Change;
          case PayType.Debit:
            {
              if ( !MSRData.Cool( card, sale.wasSwiped() ) ) {
                Inform( "Card data missing, invalid, or incomplete" );
                return show.Change;
              }
              if ( !sale.wasSwiped() ) {//card but not swiped ...
                Inform( "Debit Must Be Swiped" );
                //security: (make sure swipe data erased if clerk does any manual entry)
                card.Clear();
                return show.Change;
              }
              if ( !customerPin.NonTrivial() ) {
                Inform( "NeedPin" );
                return show.Change;
              }
              Inform( "Preparing Debit request" );
              request = PaymentRequest.DebitRequest( sale, card, customerPin );
            }
            break;
          case PayType.GiftCard:
            {
              if ( !cleanCard() ) {//+++ expiration date not required ---
                Inform( "Card data missing, invalid, or incomplete" );
                return show.Change;
              }
              Inform( "Preparing GiftCard request" );
              request = PaymentRequest.GiftCardRequest( sale, card );
            }
            break;

          case PayType.Credit:
            {
              if ( !cleanCard() ) {
                Inform( "Card data missing, invalid, or incomplete" );
                return show.Change;
              }
              Inform( "Preparing Credit request" );
              request = PaymentRequest.CreditRequest( sale, card );
            }
            break;
//          case PayType.Check: {
//            if(!MICRData.Cool(check)){
//              Inform("Check data missing, invalid, or incomplete");
//              return show.Change;
//            }
//            if(need.id4check && !id.isOk()){
//              Inform("ID required for Check");
//              return show.Change;
//            }
//            Inform("Preparing check request");
//            request=new CheckRequest(sale,check,id.Info());//even if id not ok.
//          } break;
        }//validation switch
        assertStan( request );
        if ( sendRequest( "attempting transaction", request ) ) {//will set tranny to either filled or sent.
          freshReceipt( request );
          //now we can clear the values, since request is already sent and handled.
          clearCustomerData();
        } else {
          Inform( "Request not sent!" );
        }
      }
      return show.Change;
    }
    catch ( Exception caught ) {
      dbg.Caught( caught );
      return show.All;
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * @returns whether clerk INFO is reasonable, does not check if actually logged in.
   */
  private boolean clerkOk() {
    if ( !clerk.idInfo.NonTrivial() ) {
      if ( autoclerk != null ) {//2nd chance
        clerk.idInfo = autoclerk.clerk();
      }
    }
    return clerk.idInfo.NonTrivial();
  }

  /**
   * insert fields common to all requests
   * all request made by PosTerminal must have a clerk logged in.
   * non-login operations are the exclusive property of the appliance.
   * @param request newly minted request
   * @return whether request is completed
   */
  private final boolean completeRequest( ActionRequest request ) {
    if ( request != null ) {
      request.setCallback( /*actor*/this );
      request.clerk = clerk.idInfo;
      return true;
    }
    return false;
  }

  /**
   * @return true if we spawn a transaction
   *         overloadable for SimPosTerminal
   */
  protected boolean sendRequest( String label, ActionRequest request ) {
    boolean success = false;
    dbg.Enter( "sendRequest:" + label );
    try {
      if ( completeRequest( request ) ) {//side effect: load common fields
        dbg.VERBOSE( "About to startaction on " + request.TypeInfo() );
        if ( connectionClient.StartAction( request ) ) {
          success = true;
          tip = request;
          if ( tip.isFinancial() ) {
            wait( "auth code" );
            GOTO( FTstate.WaitAuth );
          } else {
            wait( "reply (admin)" );
            GOTO( FTstate.WaitAdmin );
          }
        } else {
          dbg.ERROR( "failed to send:" + request.TypeInfo() );
        }
      } else {
        dbg.ERROR( "couldn't complete:" + request.TypeInfo() );
      }
    }
    catch ( Exception ex ) {
      dbg.Caught( ex );
      GOTO( FTstate.DoneBad );//COA
    }
    finally {
      Inform( label + (success ? " Started" : " Not Spawned") );
      dbg.VERBOSE( "success is:" + success );
      dbg.Exit();
      return success;
    }
  }

  private final int onBatchReply( BatchReply bratch ) {
    if ( bratch.tinfo == null ) {
      bratch.set( termInfo ); //cheating, copy from server didn't work +_+
    }
    BatchListingFormatter blif = BatchListingFormatter.Create( bratch, reception.Formatter() );
    Receipt receipt = new Receipt();
    receipt.setOptions( reception.recipe(), reception.timezone(), null );
    receipt.PrintBatchList( printer, blif );
    return show.All;
  }

  /**
   * respond to whatever type of reply we get, regardless of what we asked for
   */
  public void ActionReplyReceipt( Action action ) {//#implements ConnectionCallback
    Post( action );
  }

  public void extendTimeout( int millis ) {
  }//#ConnectionCallback interface

  private final int onReceiptStored() {
    if ( store.termcap.beDoneIfApproved() ) {//don't wait to see if receipt stored ok.
      return cleanAll(); //we are now finished, if we fail then clerk has to figure that out @2c@
    } else {
      GOTO( FTstate.DoneGood );//don't know what state we should go to.
      return show.Change;
    }
  }

  private final int onStoreReply( StoreReply reply ) {
    reception.AdminReceipt().PrintStoreReply( printer, reply );
    return show.All;
  }

  private final int Handle( Action action ) {
    dbg.Enter( "Handle Action:" + action.TypeInfo() );
    try {
      //made things worse rather than better GOTO(FTstate.DoneBad); //in case some control path forgets to change state.
      Inform( action.historyRec() );
      if ( action.request.isFinancial() ) {
        lastFrep = null;//forget last stored reply regardless of new one's success.
      }

      presentAuth( action );//gives autoclerk a chance to deal with it.

      if ( !action.reply.Succeeded() ) {
        return onFailure( action );
      } else {
        switch ( action.reply.Type().Value() ) {
          default:
            {
              String errmsg = "Ignoring a " + action.reply.Type().Image();
              dbg.ERROR( errmsg );
              Post( Problem.Noted( errmsg ) );
              GOTO( FTstate.DoneBad );
            }
            break;
          case ActionType.store:
            {
              return onStoreReply( (StoreReply) action.reply );
              // no state change
            }// break;
          case ActionType.batch:
            {
              return onBatchReply( (BatchReply) action.reply );
              // no state change
            } //break;
          case ActionType.receiptStore:
            {
              return onReceiptStored();//all paths do statechanges
            } //break;
          case ActionType.payment:
            {
              dbg.VERBOSE( "Processing financial reply" );
              return onApproval( (PaymentReply) action.reply, (PaymentRequest) action.request );
            } //break;
          case ActionType.clerkLogin:
            {
              Inform( "Login reply" );
              clerk.onLogIn( (LoginReply) action.reply );
              connectionClient.standin.startBacklog();
              cleanUp(); //#NOT cleanAll, you would have to log in again ad infinitum if enautologout is true
            }
            break;
        }//end successful action switch on type
      }
      return show.All;
    }
    catch ( Exception caught ) {
      dbg.Caught( caught );//+_+
      return show.All;
    }
    finally {
      dbg.Exit();
    }
  }

  private final int retryCheck() {//+++ this will need some work! down at messaging level!
    return attemptTransaction();
  }

  /**
   * @param butt a ButtonTag. value
   * @return display update intensity
   */
  private final int doButton( int butt ) {
    switch ( butt ) {
      default:
      case ButtonTag.NullButton:
        return show.All; /*will trigger reload form*/
      case ButtonTag.ClearForm:
        return show.Change; /*never get this, handled internally by entouch*/
//      case ButtonTag.CouponDesired:   return PrintCurrentCoupon();
      case ButtonTag.DoCheck:
        return startCheckSale( false );
      case ButtonTag.DoCredit:
        return startCreditSale( false );
      case ButtonTag.DoDebit:
        return startDebitSale();
      case ButtonTag.DoCash:
        return startCashSale();
      case ButtonTag.GetBalance:
        return startBalanceQuery();
      case ButtonTag.CustomerCancels:
        return doClerkCancel();   //conditionalized cancel
      case ButtonTag.CustomerAmountOk:
        return onAmountOk();
      case ButtonTag.Signed:
        return fetchSignature( "patron" );
      case ButtonTag.DriversLicense:
        return retryCheck();
      case ButtonTag.OtherCard:
        return retryCheck(); //other IDENTIFICATION card.
    }
  }

  private final int doButton( ButtonTag butt ) {
    return doButton( butt.Value() );
  }

  /**
   * our pinpadservice sends us PINData.Null() to indicate user cancelled
   */
  private final int onPin( PINData newpin ) {
    if ( PINData.NonTrivial( newpin ) ) {
      customerPin = newpin;
      customerHasAccepted = true;//on PIN received
      dbg.WARNING( "pin:" + EasyCursor.spamFrom( customerPin ) );
      return attemptTransaction();
    } else {//capture failed
      dbg.WARNING( newpin.errorMessage() );
      card.Clear();//make's user swipe again,
      return show.All;
    }
  }

//  private final void onIdPresented(){
//    if(id.isOk()){
//      customerHasAccepted=true; //for checks, on id entered
//      //caller had better attemptTransaction.
//    } else {    //need to bug them to try again...
//      customerHasAccepted=false;
//      Inform("ID was not Ok:"+id.Spam());
//    }
//  }

  private final int handle( PINData pinentry ) {
    onPin( pinentry );
    return show.All;
  }

  private final int handle( MICRData nucheck ) {//@ivitrio@
    if ( readyForCheck() ) {
      onCheck( nucheck, true /*reallyScanned*/ );
      clerkui.loadCheck( check ); //stores data in clerkUI in case it needs editing.
    }
//    jTerm.checkReader.Acquire(); //keep it alive for rescanning
    return show.All;
  }

  private final int handle( MSRData theCard ) {
    if ( TextList.NonTrivial( theCard.errors ) ) {
      dbg.ERROR( "CardErrors", theCard.errors.Vector() );
    }
    if ( jTerm.gettingSwipe() ) {
//      if(expectingIDcard){
//        id.onSwipe(theCard);
//        onIdPresented();
//        dbg.WARNING("ID swipe triggers transaction attempt");
//        return attemptTransaction();//+_+ qualify with "payby==check" or some not yet existent "need ID"
//      } else
      {
        dbg.VERBOSE( "presuming a financial swipe" );
        return onCard( theCard, true /*reallySwiped*/ );
      }
    } else {
      dbg.WARNING( "Unexpected swipe ignored!" );
    }
    return show.Change;
  }

  private final int handle( SigData siggy ) {
    dbg.VERBOSE( "sigdata from forminput" );
    if ( siggy.isTrivial() ) {
      return onBadSignature();
    } else {
      return onSignature( siggy );
    }
  }

  private final int handle( FormButtonData fbd ) {//relocate this inside iviWrapper. remove FormButtonData from PosTerminal scope.
    for ( int i = fbd.button.length; i-- > 0; ) {
      if ( fbd.button[i].wasPressed ) {//
        return doButton( fbd.button[i].ID ); //ignore remaining buttons as only one is ever pressed
      }//end if pressed
    }//end for buttons
    return show.All;
  }

  private final void NoSale() {
    setTransferType( TransferType.Unknown );
  }

  private final void normalSale() {
    setTransferType( TransferType.Sale );
//    jTerm.checkReader.Acquire();
  }

  private final int mkBatchRequest( boolean closer, boolean concise ) {
    dbg.Enter( "mkBatchRequest" );
    try {
      if ( closer ) {
        if ( !clerk.priv.canClose ) {
          mgrRequired( "to Close" );
          return show.All;
        }
      }
      String msg = (closer ? "Close " : "Print") + (concise ? "" : " +Itemize");
      if ( !sendRequest( msg, BatchRequest.fromMenu( closer, concise ) ) ) {
        return showProblem( "Try Again Later", weareoffline );
      }
      return show.Change;
    }
    finally {
      dbg.Exit();
    }
  }

  private void MenuDump( String menuname, TrueEnum menu ) {
    printer.startPage();
    try {
      printer.println( menuname + " Options:" );
      printer.print( TextList.enumAsMenu( menu ) );
      printer.formfeed();
    }
    finally {
      printer.endPage();
    }
  }

  private String GCAmountPrompt = "GiftCard op unknown!";

  private void setGiftCardoptions( String prompt, int transferType ) {
    GCAmountPrompt = prompt;
    setTransferType( TransferType.Unknown );
    sale.type.payby.setto( PayType.GiftCard );
    GOTO( FTstate.Incomplete );
  }

  private final int Handle( GiftCard_Op opcode ) {
    dbg.Enter( "GiftCard_Op" + opcode.Image() );
    try {
      switch ( opcode.Value() ) {
        case GiftCard_Op.CashOut:
          {
            return doCashOut();
          } //break;
        case GiftCard_Op.GetBalance:
          {
            setGiftCardoptions( "getting info", TransferType.Query );
            customerHasAccepted = true;//get balance
            attemptTransaction();
          }
          break;
        case GiftCard_Op.Instructions:
          {
            GCInstructionsDump();
          }
          break;
      }
      return show.All;
    }
    finally {
      dbg.Exit();
    }
  }

//@todo: convert these paragraphs into TextLists, and these values are default that get loaded from terminal Properties.
  private String GCinstructions[] = {//not final so that we can choose to load from disk.
    "Redemption is done just like a normal credit card sale except that no signature is asked for.", "Initializing and adding value are done via Refunding that amount as per credit card  operation.", "Balance Query is the only operation that requires using this menu, however each sale or refund operation reports a balance.", "If the card doesn't have enough value for the sale then the additional amount needed is printed on the receipt. The card will NOT have been partially debited.", "    GiftCard functions menu:", };

  private String CashOutInstructions[] = {"CashOut is not yet directly implemented.", "To cash out:", "\t1) do a balance query", "\t2) do a SALE for that amount", "\t3) give customer that amount of money", };

  private int doCashOut() {
    printer.printPage( CashOutInstructions );
    return show.Change;
  }

  private void GCInstructionsDump() {
    printer.startPage();
    try {
      printer.println( "GiftCard Operations:" );
      printer.print( GCinstructions );
      printer.print( TextList.enumAsMenu( GiftCard_Op.Prop ) );
      printer.formfeed();
    }
    finally {
      printer.endPage();
    }
  }

  private int mgrRequired( String why ) {
    return showProblem( "Manager Req'd", why );
  }

  private final int Handle( StoreMenu todo ) {
    switch ( todo.Value() ) {
      case DrawerMenu.PrintOptions:
        {
          MenuDump( "Store Administrative", todo );
          enterMenu( ClerkItem.StoreMenu );
          return show.Change;
        } //break;

      case StoreMenu.Deposit:
        {
          if ( clerk.priv.canClose ) {
            if ( !sendRequest( "Make Deposit", new StoreRequest() ) ) {
              return showProblem( "Deposit Failed", "sorry" );
              //and does NOT do normalSale() leaving them in the menu until lcear has been pressed plenty of times.
            }
          }
        }
    }
    exitMenu();//storemenu
    normalSale();
    return show.Change;
  }

  private final int Handle( DrawerMenu todo ) {
    boolean done = true;
    try {
      switch ( todo.Value() ) {         //                   (close, concise)
        case DrawerMenu.Detail:
          return mkBatchRequest( false, false );
        case DrawerMenu.Totals:
          return mkBatchRequest( false, true );
        case DrawerMenu.Close_w_Detail:
          return mkBatchRequest( true, false );
        case DrawerMenu.Close_w_Totals:
          return mkBatchRequest( true, true );
        case DrawerMenu.PrintOptions:
          {
            MenuDump( "Drawer Management", todo );
            enterMenu( ClerkItem.DrawerMenu );
            done = false;
            return show.Change;
          } //break;
      }
      return show.Change;
    }
    finally {
      if ( done ) {
        exitMenu();//drawer menu
        normalSale();
      }
    }
  }

  private boolean cando( int transfertype ) {
    switch ( transfertype ) {
      case TransferType.Return:
        return clerk.priv.canREFUND;
      case TransferType.Reversal:
        return clerk.priv.canVOID;
      case TransferType.Modify:
        return clerk.priv.canSALE;
      case TransferType.Force:
        return clerk.priv.canMOTO;
      case TransferType.Sale:
        return clerk.priv.canSALE;
      case TransferType.Query:
        return true;
      default:
        return false; //unknown type!
    }
  }

  private boolean setupTransfer( int transfertype, String gripe ) {
    if ( cando( transfertype ) ) {
      setTransferType( transfertype );
      return true;
    } else {
      mgrRequired( gripe );
      return false;
    }
  }

  private final int Handle( Functions funcode ) {
    exitMenu();//Functions exec
    boolean gotosale = false;
    try {
      switch ( funcode.Value() ) {
        default:
          {
            gotosale = true;
          }
          return show.All; //swallow it.
        case Functions.GiftCard:
          {//start svoperations menu.
            setTransferType( TransferType.Unknown );
            sale.type.payby.setto( PayType.GiftCard );
            GOTO( FTstate.Incomplete );
          }
          break;
        case Functions.Maintenance:
          {//invoke terminal command
            enterMenu( ClerkItem.TerminalOp );
          }
          break;
        case Functions.ChangeUser:
          {//
            Standin().setStandin( false );//doesn't seem to work +_+
            clerk.Clear(); //forget who the clerk is
            gotosale = true;
          }
          break;
        case Functions.LastReceipt:
          {
            reception.rePrint( printer );
            gotosale = true;
          }
          break;
        case Functions.Drawer:
          {
            enterMenu( ClerkItem.DrawerMenu );
            //          mkBatchRequest(false);
            //          gotosale=true;
          }
          break;
        case Functions.StoreAdmin:
          {
            enterMenu( ClerkItem.StoreMenu );
          }
          break;
//        case Functions.PrintCoupon: {
//          Handle(new ClerkCommand(ClerkEvent.PrintCoupon));
//          gotosale=true;
//        } break;
        case Functions.Sale:
          {
            setupTransfer( TransferType.Sale, "to do sales" );
          }
          break;
        case Functions.Return:
          {
            setupTransfer( TransferType.Return, "to do refund" );
          }
          break;
        case Functions.Void:
          {
            setupTransfer( TransferType.Reversal, "to do void" );
          }
          break;
        case Functions.OtherTransactn:
          {
            enterMenu( ClerkItem.SpecialOps );
          }
          break;
      }
    }
    finally {
      if ( gotosale ) {
        normalSale();
      }
      return show.Change;
    }
  }

  private final int Handle( PaySelect ps ) {
    switch ( ps.Value() ) {
      case PaySelect.Cancel:
        cleanAll();
        break; //@2c@
      case PaySelect.ManualCard:
        startCreditSale( true );
        break;
      case PaySelect.ManualCheck:
        startCheckSale( true );
        break;
    }
    return show.Change; //all of the above showantell
  }

  private final int Handle( SpecialOp op ) {
    switch ( op.Value() ) {
      default:
      case SpecialOp.List:
        //+++ add menudump here
        return show.All;//return so that we don't exit menu.
      case SpecialOp.Force:
        setTransferType( TransferType.Force );
        break;
      case SpecialOp.Adjust:
        setTransferType( TransferType.Modify );
        need.origref = true;//redundant
        break;
      case SpecialOp.PreAuth:
        setTransferType( TransferType.Authonly );
        break;
    }
    exitMenu();
    return show.Change;
  }

  /**
   * /**
   * call when manual data entry has occured
   */
  private final void manHandled() {
    sale.type.setto( new EntrySource( EntrySource.Manual ) );
  }

  /**
   * each clerkUi punt() must have a matching case here
   */
  private final int Handle( ItemEntry aiee ) {//clerkui.punt
    dbg.Enter( "Handle:" + aiee );
    //this didn't work as it should, it preempted next prompt    clerkui.flash("Checking ...");
    try {
      String manentry = aiee.image;
      switch ( aiee.item.Value() ) {
        case ClerkItem.AVSstreet:
          {
            need.AVSstreet = false; //do NOT check value, trivial input is allowed
            card.avsInfo().setAddress( manentry );// do NOT qualify data, let server do that
          }
          break;
        case ClerkItem.AVSzip:
          {
            need.AVSzip = false; //do NOT check value, trivial input is allowed
            card.avsInfo().setZip( manentry );// do NOT qualify data, let server do that
          }
          break;
        case ClerkItem.MerchRef:
          {
            sale.setMerchantReferenceInfo( manentry );
          }
          break;
        case ClerkItem.BadCardNumber:
        case ClerkItem.CreditNumber:
          {
            card.accountNumber.setto( manentry );
            return onCard( card, false ); //give mooting a chance to skip next question
          } //break;

        case ClerkItem.BadExpiration:
        case ClerkItem.CreditExpiration:
          {
            card.expirationDate.parsemmYY( manentry );
            return onCard( card, false );
          }// break;

        case ClerkItem.CreditName:
          {//+_+ we shove everything into the surname, good enough for visual displays.
            manHandled();
            card.person.setSurname( manentry );
          }
          break;

        case ClerkItem.CheckBank:
          {
            manHandled();
            check.Transit = manentry;
          }
          break;

        case ClerkItem.CheckAccount:
          {
            manHandled();
            check.Account = manentry;
          }
          break;

        case ClerkItem.CheckNumber:
          {
            manHandled();
            check.Serial = manentry;
          }
          break;

        case ClerkItem.SalePrice:
          {
            OnSaleAmount( new RealMoney( manentry ) );
          }
          break;

//        case ClerkItem.License:  {
//          id.setLocale("TX");  // --- testing (for now)
//          id.setNumber(manentry);//+_+ need to unpack state code.
//          if(!id.isOk()){//then see if we have a bypass code
//            id.Force(clerk.idInfo.Passes(manentry));//
//          }
//          onIdPresented();
//        } break;

        case ClerkItem.NeedApproval:
          {
            onAmountOk( manentry );
          }
          break;

        case ClerkItem.RefNumber:
          { //void or modify:
            onRefNumber( manentry );
          }
          break;

        case ClerkItem.PreApproval:
          {
            startForce( manentry );
          }
          break;
      }//end switch(clerkitem)
      return attemptTransaction();
    }
    finally {
      dbg.Exit();
    }
  }

  private final void reportOnConnection() {
    FormattedLineItem banner = FormattedLineItem.winger( "CONNECTION" );
    printer.print( banner );
    UTC systime = UTC.Now();
    //would like to access MD5 digest here instead of filesize.
    printer.print( "Crock:", Revision.Version() + " (C" + IOX.fileSize( "paymate.jar" ) + ")" );

    UTC fallback = IOX.fileModTime( "paymate.jar" );
    if ( systime.before( fallback ) ) {
      //then clock has failed!!
      printer.print( "Ctime:", "T" + fallback.getTime() );
      //      OS.setClock(fallback);
      //+++--- retry connection stuff.
    } else {
      printer.print( "Rtime:", "T" + systime.getTime() );
    }
    TextList netInfo = TheSinetSocketFactory.Dump( null );
    printer.print( netInfo );
    printer.print( banner );
    printer.formfeed();//in report on connection
  }

  /**
   * @param cq is the clerk item that the clerk has pressed cancel upon
   */
  private final int onCancel( ClerkItem cq ) {
    dbg.VERBOSE( "cancelling " + cq.Image() );
    switch ( cq.Value() ) {
      case ClerkItem.WaitApproval:
        {
          //@todo: change wait message to be seconds of waiting remaining.
        }
        break;
      case ClerkItem.SpecialOps:
        {
          cleanAll();//bail hard.
        }
        break;
      case ClerkItem.AVSstreet:
        {
          card.avsInfo().clear();
          need.AVSstreet = need.AVSzip = false;//cancel asking for avs.
        }
        break;
      case ClerkItem.AVSzip:
        {
          card.avsInfo().setAddress( "" );//which causes us to ask for street again
          need.AVSstreet = true;
        }
        break;
      case ClerkItem.MerchRef:
        {
          sale.setMerchantReferenceInfo( null );
          sale.Amount().setto( 0 );
        }
        break;
      case ClerkItem.TerminalOp:
      case ClerkItem.DrawerMenu:
      case ClerkItem.StoreMenu:
        { //presume they picked menu item in error.
          exitMenu();//cancel store menu
        }
        break;

      case ClerkItem.ClerkID:
        {//@@@
          if ( clerk.isLoggedIn() ) {
            exitMenu();//cancel clerkid: presume it was a functions menu oops
          } else {
            enterMenu( ClerkItem.TerminalOp );
          }
        }
        break;
      case ClerkItem.Problem:
        {
          cleanAll();// standalone autoclerk response to any problem. //@2c@
        }
        break;

      case ClerkItem.SVOperation:
        {
          normalSale();//must at least clear paytype else we are stuck in menu.
        }
        break;

      case ClerkItem.SecondCopy:
        {
          need.AnotherCopy = false;
          doClerkCancel();
        }
        break;

      case ClerkItem.NeedPIN:
        {
          cancelDebit();
        }
        break;

      case ClerkItem.NeedApproval:
        {//"reenter xyz amount"
          clearCustomerData(); //but even if autologout is set stay live.
        }
        break;

      case ClerkItem.NeedSig:
        {//do manual sig dance
          Handle( new SigningOption( SigningOption.SignPaper ) );
        }
        break;

      case ClerkItem.BootingUp:
        {
          //hook for letting the clerk know that we really are trying to connect.
          dbg.VERBOSE( "connection cleared" );
          reportOnConnection();
        }
        break;

      default:
        {
          //the following show and tell should restart the same question...
        }
        break;
    }
    return show.Change;
  }

  private final void whenConnecting( String backdoor ) {
    Handle( new ClerkCommand( new ClerkEvent( ClerkEvent.Reconnect ) ) );
  }

  /**
   * can't always cancel an operation
   */
  private final int doClerkCancel() {
//    exitMenu();//+_+ DrawerMenu and StoreMenu are getting here on cancel, really shouldn't
    switch ( tranny.Value() ) {
      case FTstate.WaitAuth:
        {
          //do nothing!
          /*we'd like to:
          set a flag to cancel next successful actionReply,
          but that is frought with ways to fail until we get reliable echo of
          actionRequest identity info in the actionReply.
          */
          return show.All;
        }
      case FTstate.WaitSig:
        {
          if ( false ) { //wasn't in specification
            return lateCancel(); //@voidlast@
          } else {
            return show.All;
          }
        }
      default:
        {
          return cleanAll(); //@2c@
        }
    }
  }

  private final int Handle( Remedies todo ) {
    switch ( todo.Value() ) {
      case Remedies.Void:
        return show.All;//ui screws up too much for people to know what is happening --- doClerkCancel();  //Void if at all possible!
      case Remedies.Done:
        return cleanUp();   //do NOT void if complete! @2c@
      case Remedies.Retry:
        return show.All;  //means attempt to retry transaction. Not yet allowed.
      case Remedies.Reprint:
        reception.rePrint();
        return show.All;
    }
    return show.All;
  }

  private final int Handle( ClerkCommand cmd ) {//Gui -> PosTerminal
    try {
      dbg.Enter( "Handle ClerkCommand " + cmd.kind.Image() );
      if ( cmd instanceof Cancellation ) {
        onCancel( ((Cancellation) cmd).ClerkItem() );
        return show.All;
      }
      if ( cmd instanceof ItemEntry ) {
        return Handle( (ItemEntry) cmd );
      }
      switch ( cmd.kind.Value() ) {
        case ClerkEvent.Enter:
          {
            dbg.ERROR( "Should be an ItemEntry, is:" + ReflectX.shortClassName( cmd ) );
          }
          break;
        case ClerkEvent.Cancel:
          {
            doClerkCancel();
          }
          break;

        case ClerkEvent.Send:
          {
            if ( !amTransacting() ) {
              attemptTransaction();
            } else {
              Inform( "Busy, CANCEL to try again" );
              return show.Change;
            }
          }
          break;

        case ClerkEvent.Login:
          {
            clerk = new Clerk(); //erase previous settings
            OnClerkId( new ClerkIdInfo( ((ClerkLoginCommand) cmd).cid ) );
          }
          break;

        case ClerkEvent.Reconnect:
          {
            //manually exit standin
            Standin().setStandin( false );
            //          myApple.notifyAll();
          }
          break;

        case ClerkEvent.Reprint:
          {
            if ( need.AnotherCopy ) {
              printSecondCopy();
            } else {
              reception.rePrint();
            }
          }
          break;

//        case ClerkEvent.PrintCoupon:{
//          PrintCurrentCoupon();
//        } break;

        case ClerkEvent.SendSignature:
          {
            onSignature( sigdata );
          }
          break;

        case ClerkEvent.Functions:
          {
            //IF NOT WAITING FOR AN AUTH!!!
            if ( !tranny.is( FTstate.WaitAuth ) ) {
              NoSale();
            } else {
              //transaction in progress!!!
            }
            return show.All;
          }//break;

        default:
          {
            dbg.ERROR( "Unhandled ClerkEvent: " + cmd.kind.Image() );
          }
          break;
      }
      return show.Change;
    }
    catch ( Exception caught ) {
      dbg.Caught( caught );
      return show.All;
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * unsolicited server actions
   * @todo: protect receipt for txn in progress!
   */
  private int Handle( ActionReply todo ) {
    if ( todo instanceof BatchReply ) {
      //@todo:auto close listing
    }
//    else if(todo instanceof ReceiptGetReply){
//      //@todo:server prints receipt on original printer
//    }
    else if ( todo instanceof AdminReply ) {
      //@todo:server prints arbitrary text on original printer
      //print Errors.
    }

    return show.Nothing;
  }

  private final void Inform( TextList msg ) {
    dbg.ERROR( "", msg.Vector() );
  }

  private final void Inform( String msg ) {
    dbg.WARNING( "info:" + msg );
  }

  private final void FormSetUp() {//@ivitrio@
    dbg.WARNING( "@Formsetup: doesdebit?" + store.termcap.doesDebit() + " really?" + OurForm.debitAllowed );
    for ( int fi = POSForm.Prop.numValues(); fi-- > 0; ) {
      OurForm form = OurForms.Find( fi );
      if ( form == null ) {
        if ( fi != POSForm.FubarForm ) {//not a real form ...yet
          dbg.ERROR( "didn't create form: " + POSForm.Prop.TextFor( fi ) );
        }
      } else {
        dbg.WARNING( "StoreForm:" + form.myName );
        dbg.VERBOSE( form.toSpam() );
        jTerm.cacheForm( form );
        dbg.VERBOSE( form.toSpam() );
      }
    }
  }

  public void Start( EasyCursor ezp ) {//run once
    //don't change the following order of initializations gratuitously!!
    //... there are dependencies
    adbg.Enter( "Start" );
    need = new InfoReqd();

    //create data stores, needed even if hardware doesn't init.
    clerk = new Clerk();
    sale = new SaleInfo();
    card = new MSRData();
    customerPin = PINData.Null();
    check = new MICRData();
//    checkId=new CheckIdInfo();

    sigdata = null;//-start
    sigstate = new SigState();

    store = StoreConfig.Null();
    super.Start( ezp );
    try {
      //construction deferred to here so that we would ahve a name for debugging:
      adbg.VERBOSE( "constructing " + termInfo.toSpam() );
      jTerm = PeripheralSet.fromDescription( this, termInfo.equipmentlist );
      clerkui = new ClerkUI( this, jTerm.getClerkPad() );

      autoclerk = AutoClerk.makeFrom( ezp );
      if ( autoclerk != null ) {
        autoclerk.setLink( this );
      }
      adbg.ERROR( "global debugger is on" );
      adbg.VERBOSE( "starting thread" );
      System.out.println( "Starting RUN agent" );
      incoming.Start();// starts it if not already started.
      OurForms.NotInService(); //to show while we are fetching our config
      adbg.VERBOSE( "finding printer" );
      printer = jTerm.getPrinter();//FUE
      adbg.VERBOSE( "starting clerk ui" );
      clerkui.Start(); //who has no autonomous events
      adbg.VERBOSE( "clear all state" );
      cleanAll(); //cancel ourselves to get into our base state @2c@
      shower( show.All );
    }
    catch ( Exception all ) {
      adbg.Caught( all );
    }
    finally {
//      hacks.pop();
      adbg.Exit();
    }
  }

  private final void unlink() {//for appliance ConnectionReply
    dbg.Enter( "Unlink" );
    try {
      if ( jTerm != null ) {
        dbg.WARNING( "jTerm stopping" );
        jTerm.detachAll();
      }
      //we trust that these will eventually stop:
      if ( connectionClient != null ) {
        dbg.WARNING( "CONNECTION stopping" );
        connectionClient.Stop();
      }
      //kill run queue!
      System.err.println( "KILLING RUN QUEUE" );
      incoming.Stop();//but can't stop until we return.
    }
    finally {
      dbg.Exit();
    }
  }

  public PosTerminal( TerminalInfo termInfo ) {//public so that terminalInfo can get to constructor via reflection
    // DO NOT put any function calls or real-world code in here ! i.e. don't trigger any behavior yet
    super( termInfo );
    if ( dbprompt == null ) {
      dbprompt = ErrorLogStream.getExtension( PosTerminal.class, "NFC" );
    }
    incoming = QAgent.New( id() + ".TERM", this, new PosActionSorter() );//+_+ add id() functions to QActor()
    incoming.config( adbg ).config( 30.0 );//wake up occasionally even if there is nothing to do.
  }

//  public PosTerminal() {//public so that terminalInfo can get to constructor via reflection
//  //for (formerly) reflective creation.
//  }

  /////////////////////////////////
  // Ip terminal subroutines

  /**
   * inform our peripherals,
   * disengage resources,
   * tell other terminals to do so.
   */
  private final void fancyExit( String why ) {
    if ( !amDying ) {//guard against reentrancy
      amDying = true;
      jTerm.updateInterfaces( new ClerkItem( clerkui.showProblem( Fstring.centered( why, 16, '#' ), "bye bye" ) ), OurForms.Find( POSForm.NotInService ), true, null );
//      clerkui.ask(ClerkItem.Problem);
      unlink();
      Stop();
    }
  }

  protected final int Handle( TerminalCommand tc ) {
    dbg.VERBOSE( "Handling TC:" + tc.Image() );
    switch ( tc.Value() ) {
      default:
        return super.Handle( tc );
      case TerminalCommand.Identify:
        {
          showIdentity();
        }
        break;
      case TerminalCommand.Shutdown:
        {
          fancyExit( "powering down" );
        }
        break;
      case TerminalCommand.StatusUp:
        {//unlikely to be useful.
          Handle( new ClerkCommand( new ClerkEvent( ClerkEvent.Reconnect ) ) );
        }
        break;
      case TerminalCommand.sendSignature:
        {
          onSignature( SigData.OnFile() );
        }
        break;
      case TerminalCommand.Clear:
        {
//--- prove that we need this first.        incoming.Clear(); //blast away everything pending
          FormSetUp();
          cleanAll(); //@2c@either way
        }
        break;
    }
    return show.Change;
  }

  public String toString() {
    return id();
  }

  /**
   * @todo add remaining members
   */
  private TextList toSpam() {
    TextList tl = new TextList();
    ErrorLogStream.objectDump( card, "card", tl );
//    ErrorLogStream.objectDump(check,   "check",   tl);
    ErrorLogStream.objectDump( sale, "sale", tl );
//    ErrorLogStream.objectDump(checkId, "checkId", tl);
    return tl;
  }

  /*package*/
  void dump( TextList responses ) {
    responses.appendMore( toSpam() );
  }

  private TextList superSpam( TextList spam ) {
    if ( spam == null ) {
      spam = new TextList();
    }
    spam.Add( id() );
    spam.Add( store.termcap.toSpam() );
    spam.Add( "" );//+_+ add more spam
    return spam;
  }

  ////////////////////////////////
  // localizable text
  private static final String weareoffline = "authorizer down";
  ////////////////
}

/**
 * some commands must precede others, as they may remove the others from the queue.
 */
class PosActionSorter implements Comparator {
  public int compare( Object enlisted, Object posting ) {
    if ( posting instanceof TerminalCommand ) {
      return 1; //newest terminal Command is higher than previous ones
    } else {
      return 0; //all other commands are equal.
    }
  }
}

//$Id: PosTerminal.java,v 1.437 2005/03/31 06:00:46 andyh Exp $
