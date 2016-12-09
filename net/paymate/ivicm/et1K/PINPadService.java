/* $Id: PINPadService.java,v 1.21 2001/11/15 03:15:45 andyh Exp $ */
package net.paymate.ivicm.et1K;

import net.paymate.authorizer.Packet;//this guy was prematurely moved into this domain..
import net.paymate.data.Value;
import net.paymate.util.*;
import net.paymate.jpos.common.InputServer;
import net.paymate.awtx.RealMoney;
import java.util.Vector;

import jpos.*;
import jpos.events.*;
import jpos.services.EventCallbacks;
import jpos.services.PINPadService14;


public class PINPadService extends MSRService implements InputServer,PINPadService14, PINPadConst, JposConst{
  static final String VersionInfo = "PIN Pad Service (C) PayMate.net 2000 $Revision: 1.21 $";
  static final ErrorLogStream dbg=new ErrorLogStream(PINPadService.class.getName());
  ///////////////////
  //command parameters:
  protected int PromptCode;
  protected String AccountNumber;
  protected long amount;

  /////////////////
  //unused attirbutes, but host can read and write them
  protected String MerchantID;
  protected String TerminalID;
  protected int PINLengthMin;
  protected int PINLengthMax;

  /** transactionType is one of:
  PPAD_TRANS_DEBIT      Debit (decrease) the specified account
  PPAD_TRANS_CREDIT     Credit (increase) the specified account.
  PPAD_TRANS_INQ        (Balance) Inquiry
  PPAD_TRANS_RECONCILE  Reconciliation/Settlement
  PPAD_TRANS_ADMIN      Administrative Transaction
  */

  protected int TransactionType;
  //pinpad response
  protected String EncryptedPIN;
  protected String ASI;

  //internal state
  protected boolean beenPrepped=false;
  protected boolean entryEnabled=false;
  protected int keyinuse;
  boolean WaitingOnKeyLoad=false;

  static final String seecashier(int why){
    return "see cashier("+why+")";
  }

  static final String prompt[]={
    "EN_US",//since jpos ignored 0 I use it as the language name
    "Enter PIN",//PPAD_MSG_ENTERPIN (1)        Enter pin number on the Pinpad.
    "Please Wait",//PPAD_MSG_PLEASEWAIT (2)      The system is processing. Wait.
    "Try Again",//PPAD_MSG_ENTERVALIDPIN (3)   The pin that was entered is not correct. Enter the correct pin number.
    "Give up",//PPAD_MSG_RETRIESEXCEEDED (4) The user has failed to enter the correct pin number and the maximum number of attempts has been exceeded.
    "Approved",//PPAD_MSG_APPROVED (5)        The request has been approved.
    seecashier(6),//PPAD_MSG_DECLINED (6)        The EFT Transaction Host "as declined to perform the requested function.
    "Cancelled",//PPAD_MSG_CANCELED (7)        The request is cancelled.
    "Pay: ",//PPAD_MSG_AMOUNTOK (8)        Enter Yes/No to approve the amount.
    "Not Ready",//PPAD_MSG_NOTREADY (9)        Pinpad is not ready for use.
    seecashier(10),//PPAD_MSG_IDLE (10)           The System is Idle.
    "Slide Card",//PPAD_MSG_SLIDE_CARD (11)     Slide card through the integrated MSR.
    seecashier(12),//PPAD_MSG_INSERTCARD (12)     Insert (smart)card.
    seecashier(13),//PPAD_MSG_SELECTCARDTYPE (13) Select the card type (typically credit or debit).
  };

  public PINPadService(String s,ET1K hw){
    super(s,hw);
    identifiers(VersionInfo,Version1dot4,"PINPad");
    pinner= new PolledCommand(finish(new Command(Codes.GET_PIN_DATA, 0x6A,"Polling For Pin"),new PinResponse()),5.0,this,dbg);
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    beenPrepped = false;
    entryEnabled=false;

    PromptCode = 0;//an invalid value
    AccountNumber = null;
    amount = 0L;
    MerchantID = null;
    TerminalID = null;
    TransactionType = PPAD_TRANS_CREDIT ;
    PINLengthMin = 4;
    PINLengthMax = 12;

    EncryptedPIN = null;
    ASI = null;
    super.open(s,eventcallbacks);
  }

  PolledCommand pinner;

  protected void startPolling(){
    pinner.Start();
  }

  protected void relinquish(){//the et1k +++ needs to be added to ET1K itself!!
    pinner.Stop();
    entryEnabled=false;
    keyinuse = -1;
  }

  public void prepareForDataEvent(Object blob){//about to post DataEvent
    if(blob==null){
      EncryptedPIN = null;
      ASI = null;
    }
    else
    if(blob instanceof byte[]){
      parsePinResponse((byte[]) blob);
    }
  }

  /**
   * DATA
71 - UKPT response code, 1 byte
funct key - function key indicator (not used, set to 00), 1 Numeric.
serial # - key serial number, 10-20 Hexadecimal
PIN block - encrypted PIN data, 16 Hexadecimal
function key- one digit or one byte, ambiguous.

5..10 bytes for 'key serial number'

8 bytes for pin
   */

  private void parsePinResponse(byte [] payload){
    dbg.Enter("parsePinresponse");
    try {
      dbg.ERROR(Safe.hexImage(payload).toString());
      int bufsize=payload.length;
      if(payload[0]!= 0x71 || bufsize < (8+5+2)){//
        PostFailure("PIN error");
      }
      //@debit@ parsing is all wrong. these need to be unpackBCD's
      long pinin=Safe.bigEndian(payload,1,8);
      ASI = Long.toHexString(pinin);
      //where is additionSecurity Info defined??? how big might it be??
      pinin= Safe.bigEndian(payload,bufsize-8,8);//last 8 ignoring lrc
    }
    finally {
      dbg.Exit();
    }
  }

  public Command onPinResponse (Command cmd){
    int response=cmd.response();
    long pinin=0;
    switch(response){
      case Codes.NO_DATA_READY:{
        //nothing yet
      } return null;
      case Codes.KEYPAD_CANCELED:  {//cancelled????
        EncryptedPIN = null;
        ASI = null;
        relinquish();
        //Post() something!+++ +_+ post formbutton customerCancels.
      } return null;//+_+
      case 0:{
        relinquish();
        dbg.ERROR("On response:"+cmd.incoming.toSpam());
        PostData(cmd.payload());
      } return null;//+_+
      default:{
        PostFailure("PIN input error");
      } return null;//+_+
    }
  }

  class PinResponse implements Callback {
    public Command Post(Command cmd){
      return onPinResponse(cmd);
    }
  }

  boolean  doingDUKPT(String s){
    return s.equals("DUKPT");
  }

  public boolean getPINEntryEnabled() throws JposException {
    assertOpened();
    return entryEnabled;
  }

  public void beginEFTTransaction(String s, int i) throws JposException {
    assertEnabled();
    Illegal(!doingDUKPT(s),"Only DUKPT supported");
    Illegal(i < 1 || i > 8,"Invalid Transaction Host");
    keyinuse = i;
  }

  protected void assertStarted() throws JposException {
    if(keyinuse == -1) {
      throw new JposException(105, "Transaction not started");
    }
  }

  protected byte [] promptBytes(){
    String s=prompt[PromptCode]+((PromptCode == PPAD_MSG_AMOUNTOK )?(new RealMoney(amount)).Image():":");
    return s.getBytes();
  }

  char typeChar(){
    return ( TransactionType== PPAD_TRANS_CREDIT?  'C': 'D');
  }

 /* PAN - the customer's Primary Account Number (obtained from the MSR
debit card), the PIN data is exclusive-or'ed (XOR'ed) with this number
before it is encrypted, 19 (Max) Numeric
FILL - fill character, 1 Special
D/C - debit/credit indicator ("C" or "D"), this byte is ignored, 1
Hexadecimal
amount - transaction amount, this field is ignored, 3-12 Numeric
FS - field separator, must = FF hex, 1 Alphanumeric
PIN pad text - the ASCII text to be displayed above the PIN pad (0 - 17
characters)
*/

  public void enablePINEntry() throws JposException {
    char padder=0xFF;
    LrcBuffer pinrequest= Command.Op(DUKcode);
    pinrequest.append(pinEntryRq);
//    pinrequest.appendNibbles(AccountNumber,padder);
//    pinrequest.append(typeChar());//must be here but is ignored...
//    pinrequest.append(Long.toString(amount / 100L));//money becomes cents again.
    pinrequest.appendNibbles(AccountNumber+padder+typeChar()+Long.toString(amount/100L),padder);
    pinrequest.append(padder);//"frame separator"
    pinrequest.append(promptBytes());

    entryEnabled=true;
    QueueCommand(new Command(pinrequest,"Seeding PIN"),new PinStart("Seeding PIN"));
  }


  class PinStart extends WantZero {
    public PinStart(String s){
      super(s);
    }
    public Command Post(Command cmd){
      switch(cmd.response()){
        case Codes.SUCCESS:{
          try {
            return super.Post(cmd); //will throw if not perfect
          } finally {
            //+_+ if not error in above then ...
            startPolling();
          }
        }
        case 3:{
          cmd.service.PostFailure("device not keyed");
        } break;
        case 0xF4: {
          cmd.service.PostFailure("data field incorrect format/length");
        } break;
        case 0xEC: {//look in visa ukpt dox for a syumbol for this
//seems to have occured when we enable card reader after enabling pinpad
          cmd.service.PostFailure("CAN'T SWIPE AND ENTER PIN AT SAME TIME");
        } break;
      }
      return null;
    }
  }

  static final byte DUKcode= Codes.DUKPT_PIN_INPUT;
  static final byte pinEntryRq =0x70;
  static final byte PINEntryCancel=0x72;


  public void endEFTTransaction(int i) throws JposException {
    //why!!! just makes life hard on user.    assertStarted();
    if(beenPrepped) {//
      QueueCommand(new Command(DUKcode,PINEntryCancel,"endEFT"),new WantZero("Device Termination Failure"));
      relinquish();
    }
  }
//////////////////////////////////////////////////////////////////////
/// jpos bs
  public String getEncryptedPIN() throws JposException {
    assertOpened();
    return EncryptedPIN;
  }

  public String getAdditionalSecurityInformation() throws JposException {
    assertOpened();
    return ASI;
  }

  /////////////////////
  // display attributes
  public String getAvailableLanguagesList() throws JposException {
    assertOpened();
    return "EN,US";
  }

  public String getAvailablePromptsList() throws JposException {
    assertOpened();
    TextList packer=new TextList(prompt);
    return packer.csv(false,false);
  }

  public int getCapDisplay() throws JposException {
    assertOpened();
    return PPAD_DISP_RESTRICTED_LIST;
  }

  public boolean getCapKeyboard() throws JposException {
    assertOpened();
    return false;
  }

  public int getCapLanguage() throws JposException {
    assertOpened();
    return PPAD_LANG_ONE;
  }

  public boolean getCapTone() throws JposException {
    assertOpened();
    return false;
  }


  ///////////////////////
  //"Properties"

  public String getAccountNumber() throws JposException {
    assertClaimed();
    return AccountNumber;
  }
  public void setAccountNumber(String s) throws JposException {
    assertClaimed();
    Illegal(AccountNumber.length() < 1 || AccountNumber.length() > 19, "Account Number invalid");
    AccountNumber = s;
  }

  public long getAmount() throws JposException {
    assertClaimed();
    return amount;
  }
  public void setAmount(long ell) throws JposException {
    assertClaimed();
    amount = ell;
  }

  public String getMerchantID() throws JposException {
    assertClaimed();
    return MerchantID;
  }
  public void setMerchantID(String s) throws JposException {
    assertClaimed();
    MerchantID = s;
  }

  public int getMinimumPINLength() throws JposException {
    assertClaimed();
    return PINLengthMin;
  }
  public int getMaximumPINLength() throws JposException {
    assertClaimed();
    return PINLengthMax;
  }
  public void setMinimumPINLength(int i) throws JposException {
    assertClaimed();
    PINLengthMin = i;
  }
  public void setMaximumPINLength(int i) throws JposException {
    assertClaimed();
    PINLengthMax = i;
  }

  public int getPrompt() throws JposException {
    assertClaimed();
    return PromptCode;
  }
  public void setPrompt(int i) throws JposException {
    assertClaimed();
    Illegal(i < 1 || i > prompt.length,"Invalid Prompt Selected");
    PromptCode = i;
  }

  public String getPromptLanguage() throws JposException {
    assertClaimed();
    return prompt[0];
  }
  public void setPromptLanguage(String s) throws JposException {
    assertClaimed();
    Illegal(s!=prompt[0],"Language Not Supported:"+s+" Only "+prompt[0]+"is implemented");
  }

  public String getTerminalID() throws JposException {
    assertClaimed();
    return TerminalID;
  }
  public void setTerminalID(String s) throws JposException {
    assertClaimed();
    TerminalID = s;
  }

  public int getTransactionType() throws JposException {
    assertClaimed();
    return TransactionType;
  }
  public void setTransactionType(int i) throws JposException {
    assertClaimed();
    TransactionType = i;
  }

  public void updateKey(int i, String s) throws JposException {
    Illegal("No longer supported");
  }
  //////////////////////
  // whose MAC is this supposed to be anyway?
  public boolean getCapMACCalculation() throws JposException {
    assertOpened();
    return false;
  }

  public void verifyMAC(String s) throws JposException {
    Illegal("MAC not supported by device");
  }

  public void computeMAC(String s, String as[]) throws JposException {
    Illegal("MAC not supported by device");
  }

}
//$Id: PINPadService.java,v 1.21 2001/11/15 03:15:45 andyh Exp $
