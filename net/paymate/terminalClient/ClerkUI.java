/**
* Title:        ClerkUI
* Description:  Q&A through a single line display
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ClerkUI.java,v 1.84 2001/11/16 01:34:31 mattm Exp $
*/
package net.paymate.terminalClient;

import net.paymate.data.*;

import net.paymate.awtx.*;//just about everything in here is used.
import net.paymate.util.*;//logger

import net.paymate.ISO8583.data.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.Terminal.*;
import net.paymate.connection.*;
import jpos.JposException;

public class ClerkUI implements AnswerListener{
  protected static final ErrorLogStream dbg=new ErrorLogStream(ClerkUI.class.getName());
  protected final static int fubar=Safe.INVALIDINDEX;
  PosTerminal posterm; //will segregate these out of ClerkUI when time permits
  protected int nextguid;//is now currentGuid, needs to be renamed

  Question fubard= new Question(fubar,"Software Error!" ,new TextValue("Call 4 Service"));
  //////////////////////////////////////////////////////////
  //the default order is in the enumeration file! not the array index/order!!!
  protected Question ClerkQuestion[]={      //"0123456789ABCDEF"
    new Question(ClerkItem.ClerkID           ,"Clerk ID?"       ,new TextValue()),
    new Question(ClerkItem.ClerkPasscode     ,"Clerk Password?" ,new TextValue()),//+++ Password is too broken!
    new Question(ClerkItem.SaleType          ,"Function #"      ,new EnumValue(new Functions(Functions.Sale))),
    new Question(ClerkItem.PreApproval       ,"Approval Code:"  ,new TextValue()),
    new Question(ClerkItem.PaymentSelect     ,"Swipe/Scan/Key..",new EnumValue(new PaySelect(PaySelect.ManualCard))),
    new Question(ClerkItem.CreditNumber      ,"enter card #"    ,new DecimalValue()),
    new Question(ClerkItem.BadCardNumber     ,"try card# again!",new DecimalValue()),

    new Question(ClerkItem.CreditExpiration  ,"exp date (MMYY)" ,new DecimalValue()),
    new Question(ClerkItem.BadExpiration     ,"try MMYY again!!",new DecimalValue()),

    new Question(ClerkItem.CreditName        ,"cardholder name?",new TextValue()),
    new Question(ClerkItem.CheckBank         ,"bad bank number" ,new MicrValue(9,MICRData.BadTransit)),
    new Question(ClerkItem.CheckAccount      ,"bad acct number" ,new MicrValue(12,MICRData.BadAccount)),
    new Question(ClerkItem.CheckNumber       ,"bad check numbr" ,new MicrValue(6,MICRData.BadSerial )),
    new Question(ClerkItem.License           ,"license number:" ,new TextValue()),
    new Question(ClerkItem.RefNumber         ,"Enter Txn #     ",new TextValue()),
    //!!!no colon in SalePrice prompt, so that posterminal can fully override it according to sale type
    new Question(ClerkItem.SalePrice         ,"Enter Amount"    ,new MoneyValue()),
    new Question(ClerkItem.NeedApproval      ,"Get Ok on amount",new TextValue()),
    new Question(ClerkItem.NeedSig           ,"Get Signature...",new EnumValue(new SigningOption(SigningOption.DoneSigning))),
    new Question(ClerkItem.WaitApproval      ,"Authorizing...  ",new TextValue()),
    new Question(ClerkItem.ApprovalCode      ,"Approved:       ",new EnumValue(new Remedies(Remedies.Done))),
    new Question(ClerkItem.Problem           ,"Look at Printer!",new TextValue()),
    new Question(ClerkItem.OverrideCode      ,"Enter override:" ,new TextValue()), //PasscodeValue()),
    new Question(ClerkItem.SecondCopy        ,"Another Copy?"   ,new DecimalValue(1)),
    new Question(ClerkItem.WaitAdmin         ,"Please wait..."  ,new TextValue("for listing")),

    new Question(ClerkItem.BootingUp         ,"CONNECTING..."   ,new TextValue()), //PasscodeValue()),
    //    new Question(ClerkItem.Blank             ,""             ,new TextValue()),
  };


  int onInvalidEnum(TrueEnum enum){
    if(enum==null)      return 0;
    if(enum instanceof Functions) return Functions.Sale;
    if(enum instanceof PaySelect) return PaySelect.ManualCard;
    if(enum instanceof Remedies) return Remedies.Done;
    if(enum instanceof SigningOption) return SigningOption.DoneSigning;
    return 0;
  }

  //////////////////////////////////////////////////////////
  protected int questionIndex(int guid){
    for(int qi=ClerkQuestion.length;qi-->0;){
      if(ClerkQuestion[qi].guid==guid){
        return qi;
      }
    }
    return fubar;
  }

  protected Question QuestionFor(int guid){
    int qi=questionIndex(guid);
    return (qi>=0)? ClerkQuestion[qi]: fubard;
  }

  protected long LongFrom  (int guid){
    Question q=QuestionFor(guid);
    try {
      return q.inandout.asLong();
    } finally {
      q.Clear();
    }
  }

  protected int EnumFrom  (int guid){
    Question q=QuestionFor(guid);
    try {
      return q.inandout.asInt();
    } finally {
      q.Clear();//makes entry invalid...
    }
  }

  protected String StringFrom  (int guid){
    Question q=QuestionFor(guid);
    try {
      return q.inandout.Image();
    } finally {
      q.Clear();
    }
  }

  protected String PassCodeFrom  (int guid){
    return StringFrom(guid); //QuestionFor(guid).inandout.Image();// really need Entry classes fixed! //toString();
  }

  //////////////////////////////////////////////////////////
  public CM3000UI peephole; //single line input with keypad.

  public ClerkUI(PosTerminal posterm) {
    this.posterm = posterm;
    peephole=new CM3000UI(""+posterm.termInfo.id());
  }

  //////////////////////////////////////////////////////////
  void doPing(){//ping the terminal logic
    dbg.VERBOSE("PING");
    posterm.Post(new DebugCommand(DebugOp.Refresh));
  }

  void doID(){
    ClerkIdInfo id=new ClerkIdInfo(StringFrom(ClerkItem.ClerkID),PassCodeFrom(ClerkItem.ClerkPasscode));
    posterm.Post(id);
  }

  void doSale(){//only change amount, card might already be swiped
    posterm.Post(new RealMoney(LongFrom(ClerkItem.SalePrice)));
  }

  void doClerkEvent(int clerkevent){
    posterm.postClerkEvent(clerkevent);
  }

  void doClerkCancel(){
    doClerkEvent(ClerkEvent.Cancel);
  }

  void cancelThis(int /*ClerkItem*/ ci){
    posterm.Post(new Cancellation(ci));
  }

  void doRemedy(Remedies funcode){
    switch(funcode.Value()){
      case Remedies.Retry:  return; //NYI
      case Remedies.Done:   posterm.Post(funcode); return;
      case Remedies.Reprint:doClerkEvent(ClerkEvent.Reprint); return;
      //+_+      case Remedies.Void:   doVoid(); return;
    }
  }

  void doRemedy(int funkey){
    doRemedy(new Remedies(funkey));
  }

  void gotoFunction(){
    doClerkEvent(ClerkEvent.Functions);
  }

  void nowAsk(int guid){
    peephole.ask(QuestionFor(guid),this); //we perpetually are asking for something.
  }

  void punt(Question q){//let host deal with sequencing etc.
    dbg.ERROR("Punting:"+ q.toSpam());
    posterm.Post(new ItemEntry(q.guid,StringFrom(q.guid)));
  }

  /**
  * @param beingAsked is accepted rather than using the local reference so that
  * virtual keyboards can answer questions unrealted to what is being asked. 4debug.
  */
  public void onReply (Question beingAsked, int opcode){
    //only a few items care about a change from the last time SUBMITTED
    //so we compress our 'opcode' range:
    boolean nochange= opcode==ACCEPTED;
    if(nochange){
      opcode=SUBMITTED;
    }
    //question dependent behaviors:
    switch(beingAsked.guid){
      default:
      case fubar: doClerkCancel(); return;

      case ClerkItem.PreApproval: doClerkCancel(); return;//+++ nyi

      case ClerkItem.SecondCopy:{
        switch(opcode){
          case SUBMITTED: {
            doClerkEvent(ClerkEvent.Reprint);
          } return;
          case CANCELLED:{
            cancelThis(ClerkItem.SecondCopy);
          } return;
        }
      } break;

      case ClerkItem.SaleType:{
        switch(opcode){
          case SUBMITTED: {
            int funcode=EnumFrom(beingAsked.guid);
            dbg.VERBOSE("Function code:"+funcode);
            posterm.Post(new Functions(funcode));
          } return;
          case CANCELLED: doClerkCancel(); return;
        }
      } break;

      case ClerkItem.SalePrice:{
        switch(opcode){
          case SUBMITTED: {
            if(nochange){
              gotoFunction();
            } else {
              doSale();
            }
          } return;
          case CANCELLED: {
            gotoFunction(); //under some conditions doesn't erase card #
          } return;
        }
      } break;

      case ClerkItem.PaymentSelect     :{
        try {
          switch(opcode){
            case SUBMITTED: posterm.Post(new PaySelect(EnumFrom(beingAsked.guid))); return;
            case CANCELLED: doClerkCancel();  return;      //want terminal to refresh us...
          }
        } finally {
          beingAsked.Clear();
        }
      } break;
      case ClerkItem.BadCardNumber:
      case ClerkItem.CreditNumber      :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked); return;
          case CANCELLED: doClerkCancel();//--- punitive, makes 'em start way over
          return;
        }
      } break;
      case ClerkItem.BadExpiration:
      case ClerkItem.CreditExpiration  :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked); return;
          case CANCELLED: nowAsk(ClerkItem.CreditNumber);  return;
        }
      } break;

      case ClerkItem.CreditName        :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked);         return;
          case CANCELLED: nowAsk(ClerkItem.CreditExpiration); return;
        }
      } break;

      case ClerkItem.CheckBank         :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked);    return;
          case CANCELLED: doClerkCancel();    return;//--- punitive, makes 'em start way over
        }
      } break;

      case ClerkItem.CheckAccount      :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked);        return;
          case CANCELLED: nowAsk(ClerkItem.CheckBank);  return;
        }
      } break;

      case ClerkItem.CheckNumber       :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked);                return;
          case CANCELLED: nowAsk(ClerkItem.CheckAccount);  return;
        }
      } break;

      case ClerkItem.License           :{
        switch(opcode){
          case SUBMITTED: punt(beingAsked); return;
          case CANCELLED: doClerkCancel();
        }
      } break;

      case ClerkItem.NeedApproval      :{
        switch(opcode){
          case SUBMITTED :
            dbg.ERROR("NeedApproval:"+beingAsked.guid+beingAsked.inandout.Image());
            punt(beingAsked);    return;
          case CANCELLED :   doClerkCancel();     return;
        } break;
      } //"not reachable"//break;

      case ClerkItem.NeedSig      :{
        switch(opcode){ //osterm can check passcode if it wishes
          case SUBMITTED :
          posterm.Post(new SigningOption(EnumFrom(beingAsked.guid)));
          return;
          case CANCELLED : {//manual signature
            cancelThis(ClerkItem.NeedSig);
            return;
          }
        } break;
      } //"not reachable"//break;

      case ClerkItem.WaitApproval      :{
        switch(opcode){
          case SUBMITTED :{
            // +_+             posterm.attemptTransaction();
            //the POSterminal must move us off of this, unless we cancell:
          } return;
          case CANCELLED :        doClerkCancel();     return;
        }
      } break;

      case ClerkItem.ApprovalCode      :{
        try {
          switch(opcode){
            case SUBMITTED:{
              //somwhow was sitting on reversal!!!
              doRemedy(true?Remedies.Done:EnumFrom(beingAsked.guid));
              return;
            }
            case CANCELLED: doRemedy(Remedies.Done);  return;
          }
        } finally {
          beingAsked.Clear();
        }

      } break;

      case ClerkItem.RefNumber: {//retrieval reference number
dbg.ERROR("made it to saking refnumber");
        switch(opcode){
          case SUBMITTED:  punt(beingAsked);         return;
          case CANCELLED:  doClerkCancel();  return;
        }
      } break;

      case ClerkItem.ClerkID           :{
        switch(opcode){
          case SUBMITTED:  nowAsk(ClerkItem.ClerkPasscode);   return;
          case CANCELLED:  posterm.Post(new Functions(Functions.ChangeUser));
        }
      } break;

      case ClerkItem.ClerkPasscode     :{
        switch(opcode){
          case SUBMITTED :          doID();                  return;
          case CANCELLED : nowAsk(ClerkItem.ClerkID);   return;
        }
      } break;

      case ClerkItem.Problem:{
        switch(opcode){
          case SUBMITTED : doClerkCancel(); return;
          //temporarily the same as enter, or vice versa:
          case CANCELLED : doClerkCancel(); return;
        }
      } break;

      case ClerkItem.OverrideCode:{                    //+++ placeholder
        switch(opcode){
          case SUBMITTED: nowAsk(ClerkItem.ClerkID); return;
          //tarbaby-once you get here you are stuck here
          case CANCELLED: nowAsk(ClerkItem.OverrideCode); return;
        }
      } break;

      case ClerkItem.BootingUp:{
        switch(opcode){
          case SUBMITTED: {
            //+_+              posterm.whenConnecting(StringFrom(beingAsked.guid));
          } return;
          case CANCELLED:{
            cancelThis(ClerkItem.BootingUp);
            //            nowAsk(ClerkItem.BootingUp);
          } return;
        }
      } break;

    }//switch question
    dbg.WARNING("Ignored:"+beingAsked.prompt);
    doPing(); //uncomment to test that this is still unreachable.
  }

  //    public Sequencer(){
    //      //formality
  //    }


  //  Sequencer sm=new Sequencer();
  /////////////////////////////////////////////
  public Question preLoad(int guid, String image){//safer than preset()
    Question q=QuestionFor(guid);//points to trashable q on invalid index.
    if(Safe.NonTrivial(image)){
      q.inandout.setto(image);
    }
    return q;
  }

  public Question splicePrompt(int guid, String addend, String image){
    Question q=preLoad(guid,image);
    int cut=q.prompt.indexOf(':')+1;
    q.prompt=q.prompt.substring(0,cut)+addend;
    return q;
  }

  public Question dynaPrompt(int guid, String addend, String image){
    dbg.VERBOSE("Dynaprompt "+guid+" prompt:"+addend+" value:"+image);
    Question q=preLoad(guid,image);
    q.prompt=addend;
    return q;
  }

  public void askInPrompt(int guid, String addend, String image){
    peephole.ask(splicePrompt(guid, addend, image),this);
  }

  public void loadCheck(MICRData scanned){ //in case there are bad fields
    preLoad(ClerkItem.CheckBank,    scanned.Transit);
    preLoad(ClerkItem.CheckAccount, scanned.Account);
    preLoad(ClerkItem.CheckNumber,  scanned.Serial);
  }
  //not done for cards, on error data is swallowed by jpos driver.

  //////////////////////////////////////////
  /**if the question indicated is being asked then mimic pressing ENTER
  * I say mimic because it will happen on a different thread than is normal.
  */
  public boolean autoEnterIf(int aClerkItem){//+_+getting lazy on the type checking
    return peephole.autoEnterIf(QuestionFor(aClerkItem));
  }

  public void ask(Question q){//ClerkItem
    dbg.VERBOSE("asking question:"+q.toSpam());
    if(q.inandout instanceof EnumValue){
      EnumValue ev= (EnumValue)q.inandout;
      if(ev.asInt()== TrueEnum.Invalid()){
        dbg.WARNING("Fixing up enumeration");
        ev.setto(onInvalidEnum(ev.Content()));
      }
    }
    peephole.ask(q,this);
  }

  public void ask(int guid){//ClerkItem
  dbg.VERBOSE("Asking for qid:"+guid);
    ask(QuestionFor(guid));
  }


  /**call only on negative approval
  *
  */
  public void onRejected(String reason){
    dbg.VERBOSE("rejecting coz:["+reason+"] "+Safe.hexImage(reason));
    //modify prompt, stuff refnum, let someone else do the asking
    reason=Safe.OnTrivial(reason,"unknown problem!");
    dynaPrompt(ClerkItem.Problem,reason,"Press CLEAR");
  }

  /**
  * @UNdeprecated ... +++ who deprecated this in the first place???
  * what do we use instead? ---
  */
  public Question showProblem(String problem,String detail){
    dbg.VERBOSE("Showing problem:"+problem+" "+detail);
    return dynaPrompt(ClerkItem.Problem,Safe.OnTrivial(problem,"unknown problem!"),detail);
  }

  public void Clear(){//erase internal data, start new sale
    for(int i=ClerkQuestion.length;i-->0;){
      ClerkQuestion[i].Clear();
    }
  }

  public void Start(){
    peephole.Start();
    ask(ClerkItem.ClerkID);
  }

  public void refresh(){
    peephole.refresh();
  }

  public String WhatsUp(){
    return peephole.WhatsUp();
  }

  public void flash(String blink){
    peephole.flash(blink);
  }

}
//$Id: ClerkUI.java,v 1.84 2001/11/16 01:34:31 mattm Exp $
