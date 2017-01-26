package net.paymate.terminalClient;
/**
 * Title:         $Source: /cvs/src/net/paymate/terminalClient/ClerkUI.java,v $
 * Description:   questions that the posterminal wants answered by a human.
 * Copyright:     Copyright (c) 2001
 * Company:       PayMate.net
 * @author        PayMate.net
 * @version       $Revision: 1.125 $
 * @todo: synch the **From() functions (guessing at why menu items remember the last entry.)
 */
import net.paymate.data.*;
import net.paymate.lang.ObjectX;
import net.paymate.awtx.*;//just about everything in here is used.
import net.paymate.util.*;//logger
import net.paymate.lang.StringX;
import net.paymate.jpos.data.*;
import net.paymate.jpos.Terminal.*;
import net.paymate.connection.*;
import net.paymate.lang.ContentType;
import net.paymate.lang.TrueEnum;

public class ClerkUI implements AnswerListener {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(ClerkUI.class);
  final static int fubar=ObjectX.INVALIDINDEX;
  PosTerminal posterm; //will segregate these out of ClerkUI when time permits
/**
 * clear at SalePrice question gets us to function menu. else it just resets
 * internal sale info and prepares for fresh sale.
 */
  boolean clearToTop=true; //legacy setting
  private int nextguid;//is now currentGuid, needs to be renamed

  Question fubard= new Question(fubar,"Software Error!" ,new TextValue("Call 4 Service"));
  //////////////////////////////////////////////////////////
  //the default order is in the enumeration file! not the array index/order!!!
  private Question ClerkQuestion[]={      //"0123456789ABCDEF"
    new Question(ClerkItem.ClerkID           ,"Clerk ID?"       ,new TextValue()),
    new Question(ClerkItem.ClerkPasscode     ,"Clerk Password?" ,new PasscodeValue()),
    new Question(ClerkItem.SaleType          ,"Function #"      ,new EnumValue(new Functions())),
    new Question(ClerkItem.SpecialOps        ,"OperationType:"  ,new EnumValue(new SpecialOp())),
    new Question(ClerkItem.DrawerMenu        ,"Drawer Functions",new EnumValue(new DrawerMenu())),
    new Question(ClerkItem.StoreMenu         ,"Store Functions" ,new EnumValue(new StoreMenu())),

    new Question(ClerkItem.PreApproval       ,"Approval Code:"  ,new TextValue()),
    new Question(ClerkItem.PaymentSelect     ,"Swipe Card ...  ",new EnumValue(new PaySelect())),
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
    new Question(ClerkItem.AVSstreet         ,"Street #"        ,new TextValue()),//only some auths limit this to numerical
    new Question(ClerkItem.AVSzip            ,"Zipcode #"       ,new DecimalValue()),//USA.

    new Question(ClerkItem.MerchRef          ,"Enter Ref #     ",new TextValue()), //expect this to get overlayed with system sepcific text.

    //!!!no colon in SalePrice prompt, so that posterminal can fully override it according to sale type
    new Question(ClerkItem.SalePrice         ,"Enter Amount"    ,new MoneyValue()),
    new Question(ClerkItem.NeedApproval      ,"Get Ok on amount",new TextValue()),
    new Question(ClerkItem.NeedPIN           ,"Waiting on PIN"  ,new TextValue()),
    new Question(ClerkItem.NeedSig           ,"Get Signature...",new EnumValue(new SigningOption())),
    new Question(ClerkItem.WaitApproval      ,"Authorizing...  ",new TextValue()),
    new Question(ClerkItem.ApprovalCode      ,"Approved:       ",new EnumValue(new Remedies(Remedies.Done))),
    new Question(ClerkItem.Problem           ,"Look at Printer!",new TextValue()),//use of plain 'value' left "BaseValueError" on hypercom during declines
    new Question(ClerkItem.OverrideCode      ,"Enter override:" ,new PasscodeValue()), //PasscodeValue()),
    new Question(ClerkItem.SecondCopy        ,"Another Copy?"   ,new EnumValue(new YesnoEnum())),
    new Question(ClerkItem.WaitAdmin         ,"Please wait..."  ,new TextValue("for listing")),
    new Question(ClerkItem.SVOperation       ,"GiftCard fn# "   ,new EnumValue(new GiftCard_Op())),
    new Question(ClerkItem.TerminalOp        ,"ServiceFunction" ,new EnumValue(new TerminalCommand())),

    new Question(ClerkItem.BootingUp         ,"CONNECTING..."   ,new TextValue()), //PasscodeValue()),
  };

  /**
  * "menu" defaults
  */
  int onInvalidEnum(TrueEnum ennum){
    if(ennum==null)                    return 0;
    if(ennum instanceof Functions)     return Functions.Sale;
    if(ennum instanceof PaySelect)     return PaySelect.ManualCard;
    if(ennum instanceof Remedies)      return Remedies.Done;
    if(ennum instanceof SigningOption) return SigningOption.DoneSigning;//lubys "getsignature" problem was here.
    if(ennum instanceof GiftCard_Op)   return GiftCard_Op.GetBalance;
    if(ennum instanceof YesnoEnum)     return YesnoEnum.Yes;
    if(ennum instanceof TerminalCommand) return TerminalCommand.Normal;
    return 0;
  }

  //////////////////////////////////////////////////////////
  private int questionIndex(int guid){
    for(int qi=ClerkQuestion.length;qi-->0;){
      if(ClerkQuestion[qi].guid==guid){
        return qi;
      }
    }
    return fubar;
  }

  public Question QuestionFor(int guid){
    int qi=questionIndex(guid);
    return (qi>=0)? ClerkQuestion[qi]: fubard;
  }

  private long LongFrom  (int guid){
    Question q=QuestionFor(guid);
    try {
      return q.inandout.asLong();
    } finally {
      q.Clear();
    }
  }

  private TrueEnum EnumFrom(Question q){
    try {
      return ((EnumValue) q.inandout).Value();
    } finally {
      q.Clear();
    }
  }

  private String StringFrom  (int guid){
    Question q=QuestionFor(guid);
    try {
      return q.inandout.Image();
    } finally {
      q.Clear();
    }
  }

  private String PassCodeFrom  (int guid){
    Question q=QuestionFor(guid);
    try {
      PasscodeValue p=(PasscodeValue)q.inandout;
      return p.Value();
    } finally {
      q.Clear();
    }

//    return StringFrom(guid); //QuestionFor(guid).inandout.Image();// really need Entry classes fixed! //toString();
  }

  //////////////////////////////////////////////////////////
  public DisplayInterface peephole; //single line input with keypad.
/**
 * establish dataflow links, coming and going
 * @param posterm gets answers
 * @param peephole is one-qustion-at-a-time interface.
 */
  public ClerkUI(PosTerminal posterm,DisplayInterface peephole) {
    this.posterm = posterm;
    this.peephole= peephole;
    peephole.attachTo(this);
  }

  boolean doID(){
    ClerkIdInfo id=new ClerkIdInfo(StringFrom(ClerkItem.ClerkID),PassCodeFrom(ClerkItem.ClerkPasscode));
    return posterm.Post(id);
  }

  boolean doSale(){//only change amount, card might already be swiped
    return posterm.Post(new RealMoney(LongFrom(ClerkItem.SalePrice)));
  }

  boolean doClerkEvent(int clerkevent){
    return posterm.postClerkEvent(clerkevent);
  }

  boolean doClerkCancel(){
    return doClerkEvent(ClerkEvent.Cancel);
  }

  boolean cancelThis(int /*ClerkItem*/ ci){
    return posterm.Post(new Cancellation(ci));
  }

  boolean doRemedy(Remedies funcode){
    switch(funcode.Value()){
      case Remedies.Retry:  return false; //NYI
      case Remedies.Done:   return posterm.Post(funcode);
      case Remedies.Reprint:return doClerkEvent(ClerkEvent.Reprint);
      //+_+      case Remedies.Void:   return doVoid();
    }
    return false;
  }

  boolean doRemedy(int funkey){
    return doRemedy(new Remedies(funkey));
  }

  boolean gotoFunction(){
    return doClerkEvent(ClerkEvent.Functions);
  }

  boolean nowAsk(int guid){
    if(peephole!=null){
      peephole.ask(QuestionFor(guid)); //we perpetually are asking for something.
    }
    return true;
  }

  boolean punt(Question q){//let host deal with sequencing etc.
    dbg.WARNING("Punting:"+ q.toSpam());
    try {
      if(q.charType().is(ContentType.select)){//then the value is some type of enumeraiton
        TrueEnum te=EnumFrom(q);
        if(te!=null){
          if(!te.isLegal()){//the cm3000 interface occasionally rapes the enumerations.
            te.setto(onInvalidEnum(te));
          }
          return posterm.Post(te);
        } else {
          //post some sort of error so that user interface unlocks +++
          return false;
        }
      } else {
        return posterm.Post(new ItemEntry(q.guid,StringFrom(q.guid)));
      }
    }
    catch (Exception ex) {
      q.Clear(); //often prevents infintie loops due to wildly unexpected input.
      return false;
    }
  }
////////////////////////////////
// common responses
  /**
   * for picks with simple CLEAR response.
   */
  private boolean punt(Question beingAsked,int opcode){
    return (opcode==CANCELLED) ? cancelThis(beingAsked.guid) : punt(beingAsked);
  }

  /** +_+  @todo: remove this function per following changes.
   * the users of this method should use pun() instead and choose to 'doClerkCancell'
   * inside of posterminal.onCancel(clerkitem).
   */
  private boolean puntOrCancel(Question beingAsked,int opcode){
    return (opcode==CANCELLED) ? doClerkCancel() :  punt(beingAsked);
  }

  private boolean puntOrAsk(Question beingAsked,int opcode,int guid){
    return (opcode==CANCELLED) ? nowAsk(guid) :  punt(beingAsked);
  }

  /**
   * @return true if question honored.
  * @param beingAsked is accepted rather than using the local reference so that
  * virtual keyboards can answer questions unrealted to what is being asked. 4debug.
  */
  public boolean onReply (Question beingAsked, int opcode){
    boolean cancel= opcode==AnswerListener.CANCELLED;
    boolean functioned= opcode==AnswerListener.FUNCTIONED;
    if(functioned){
      gotoFunction();
    }
    //question dependent behaviors:
    switch(beingAsked.guid){
      default: //join
      case fubar:                   return doClerkCancel();
      case ClerkItem.SpecialOps:    return puntOrCancel(beingAsked,opcode);
      case ClerkItem.MerchRef:      return punt(beingAsked,opcode);
      case ClerkItem.AVSstreet:     return punt(beingAsked,opcode);
      case ClerkItem.AVSzip:        return punt(beingAsked,opcode);
      case ClerkItem.PreApproval:   return punt(beingAsked,opcode);
      case ClerkItem.SecondCopy:    {
        return cancel?cancelThis(ClerkItem.SecondCopy):doClerkEvent(ClerkEvent.Reprint);
      }

      case ClerkItem.TerminalOp:    {
        if(cancel){
          return cancelThis(ClerkItem.TerminalOp);
        } else {
          //make a majic event and ask for key and check key.
          //until then "just do it"
          Appliance.doCommand((TerminalCommand) EnumFrom(beingAsked));
          return true;//gotta presume the appliance takes care of any problems.
        }
      }
      case ClerkItem.DrawerMenu:    return punt(beingAsked,opcode);
      case ClerkItem.StoreMenu:     return punt(beingAsked,opcode);
      //join
      case ClerkItem.SaleType:      return puntOrCancel(beingAsked,opcode);

      case ClerkItem.SalePrice:{
        switch(opcode){
          case SUBMITTED: return doSale();
          case ACCEPTED:  return gotoFunction();
          case CANCELLED: return clearToTop ? gotoFunction(): doClerkCancel();
          default:        return false;
        }
      }

      case ClerkItem.PaymentSelect: return puntOrCancel(beingAsked,opcode);

      case ClerkItem.BadCardNumber://join
      case ClerkItem.CreditNumber:  return puntOrCancel(beingAsked,opcode);

      case ClerkItem.BadExpiration: //join
      case ClerkItem.CreditExpiration:  return puntOrAsk(beingAsked,opcode,ClerkItem.CreditNumber);
      case ClerkItem.CreditName:    return puntOrAsk(beingAsked,opcode,ClerkItem.CreditExpiration);
      case ClerkItem.CheckBank:     return puntOrCancel(beingAsked,opcode);
      case ClerkItem.CheckAccount:  return puntOrAsk(beingAsked,opcode,ClerkItem.CheckBank);
      case ClerkItem.CheckNumber:   return puntOrAsk(beingAsked,opcode,ClerkItem.CheckAccount);
      case ClerkItem.License:       return puntOrCancel(beingAsked,opcode);
      case ClerkItem.NeedApproval:  return puntOrCancel(beingAsked,opcode);
      case ClerkItem.NeedSig:       return punt(beingAsked,opcode);
      case ClerkItem.SVOperation:   return punt(beingAsked,opcode);
      case ClerkItem.WaitApproval:  return cancel && doClerkCancel();
      case ClerkItem.ApprovalCode:  return doRemedy(Remedies.Done);//ignore user input!! //should be : punt(beingAsked)
      case ClerkItem.RefNumber:     return puntOrCancel(beingAsked,opcode);
      case ClerkItem.ClerkID:       return cancel ? cancelThis(ClerkItem.ClerkID) : nowAsk(ClerkItem.ClerkPasscode);
      case ClerkItem.ClerkPasscode: return cancel ? nowAsk(ClerkItem.ClerkID): doID();
      case ClerkItem.Problem:       return doClerkCancel();
      case ClerkItem.OverrideCode:  return cancel ? nowAsk(ClerkItem.OverrideCode) : nowAsk(ClerkItem.ClerkID); //tarbaby-once you get here you are stuck here
      case ClerkItem.BootingUp:     return cancel && cancelThis(ClerkItem.BootingUp);

    }//switch question
  }

  /////////////////////////////////////////////
  public Question preLoad(int guid, String image){//safer than preset()
    Question q=QuestionFor(guid);//points to trashable q on invalid index.
    if(StringX.NonTrivial(image)){
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
    if(peephole!=null){
      peephole.ask(splicePrompt(guid, addend, image));
    }
  }

  public void loadCheck(MICRData scanned){ //in case there are bad fields
    preLoad(ClerkItem.CheckBank,    scanned.Transit);
    preLoad(ClerkItem.CheckAccount, scanned.Account);
    preLoad(ClerkItem.CheckNumber,  scanned.Serial);
  }
  //not done for cards, on error data is swallowed by jpos driver.

//  //////////////////////////////////////////
//  /**if the question indicated is being asked then mimic pressing ENTER
//  * I say mimic because it will happen on a different thread than is normal.
//  * @return true if an enter was generated
//  */

  private Question ask(Question q){//ClerkItem
    dbg.VERBOSE("asking question:"+q.toSpam());
    if(q.inandout instanceof EnumValue){
      EnumValue ev= (EnumValue)q.inandout;
      if(ev.asInt()== TrueEnum.Invalid()){
        dbg.WARNING("Fixing up enumeration");
        ev.setto(onInvalidEnum(ev.Content()));
      }
    }
    if(peephole!=null){
      peephole.ask(q);
    }
    return q;
  }

  public Question ask(int guid){//ClerkItem
    dbg.VERBOSE("Asking for qid:"+guid);
    return ask(QuestionFor(guid));
  }

  /**call only on negative approval
   *   modify prompt, let someone else do the actual asking
  */
  public void onRejected(String reason){
    reason=StringX.OnTrivial(reason,"unknown problem!");
    dynaPrompt(ClerkItem.Problem,reason,"Press CLEAR");
  }

  /**
  * @UNdeprecated ... +++ who deprecated this in the first place???
  * what do we use instead? ---
  */
  public int showProblem(String problem,String detail){
    dbg.VERBOSE("Showing problem:"+problem+" "+detail);
    dynaPrompt(ClerkItem.Problem,StringX.OnTrivial(problem,"unknown problem!"),detail);
    return ClerkItem.Problem;
  }

  public void Clear(){//erase internal data, start new sale
    for(int i=ClerkQuestion.length;i-->0;){
      ClerkQuestion[i].Clear();
    }
  }

  public void Start(){//no longer required..
    if(peephole!=null) {
      ask(ClerkItem.BootingUp);
    }
  }

  public String WhatsUp(){
    if(peephole!=null){
      return peephole.WhatsUp();
    } else {
      return "NO DISPLAY DEVICE";
    }
  }

  public void flash(String blink){
    if(peephole!=null){
      peephole.flash(blink);
    }
  }

}
//$Id: ClerkUI.java,v 1.125 2004/02/24 18:31:24 andyh Exp $
