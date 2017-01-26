/* $Id: PINPadService.java,v 1.42 2003/07/27 05:35:04 mattm Exp $ */
package net.paymate.ivicm.et1K;

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.RealMoney;
import java.util.Vector;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;

public class PINPadService extends Service  {
  static final String VersionInfo = "PINPadService (C) PayMate.net 2000+ $Revision: 1.42 $";
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(PINPadService.class);

  //internal state
  boolean beenPrepped=false;
  boolean entryEnabled=false;
  int keyinuse;
  boolean WaitingOnKeyLoad=false;

//  static final String seecashier(int why){
//    return "see cashier("+why+")";
//  }

//  static final String prompt[]={
//    "EN_US",//since jpos ignored 0 I use it as the language name
//    "Enter PIN",//PPAD_MSG_ENTERPIN (1)        Enter pin number on the Pinpad.
//    "Please Wait",//PPAD_MSG_PLEASEWAIT (2)      The system is processing. Wait.
//    "Try Again",//PPAD_MSG_ENTERVALIDPIN (3)   The pin that was entered is not correct. Enter the correct pin number.
//    "Give up",//PPAD_MSG_RETRIESEXCEEDED (4) The user has failed to enter the correct pin number and the maximum number of attempts has been exceeded.
//    "Approved",//PPAD_MSG_APPROVED (5)        The request has been approved.
//    seecashier(6),//PPAD_MSG_DECLINED (6)        The EFT Transaction Host "as declined to perform the requested function.
//    "Cancelled",//PPAD_MSG_CANCELED (7)        The request is cancelled.
//    "Pay: ",//PPAD_MSG_AMOUNTOK (8)        Enter Yes/No to approve the amount.
//    "Not Ready",//PPAD_MSG_NOTREADY (9)        Pinpad is not ready for use.
//    seecashier(10),//PPAD_MSG_IDLE (10)           The System is Idle.
//    "Slide Card",//PPAD_MSG_SLIDE_CARD (11)     Slide card through the integrated MSR.
//    seecashier(12),//PPAD_MSG_INSERTCARD (12)     Insert (smart)card.
//    seecashier(13),//PPAD_MSG_SELECTCARDTYPE (13) Select the card type (typically credit or debit).
//  };
/*
with a colon didn't get text before the colon
with a space only got the bare cents.

 *
 */

final String promptSale=  "Pay:";
final String promptRefund="Get:";
  static final int DUKPT_PIN_INPUT = 0x62;//98;
  static final int GET_PIN_DATA = 0x63;//99;
//??  0x6A;//
  static final int UKPT_RESPONSE= 0x71;//response code indicating ukpt response.
//is followed by 00, 10-20 hexdigits, 16 hexdigits

  public PINPadService(String s,ET1K hw){
    super(s,hw);
    pinner= new PolledCommand(finish(new Command(GET_PIN_DATA, 0x6A,"Polling For Pin"),new PinResponse()),5.0,this,dbg);
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

  private PINData parsePinResponse(byte [] payload){
    dbg.Enter("parsePinresponse");
    try {
      dbg.ERROR(String.valueOf(Formatter.hexImage(payload)));
      int bufsize=payload.length;
      if(payload[0]!= UKPT_RESPONSE || bufsize < (8+5+2)){//8=16/2, 5=10/2, 2=71 and following 00
//        PostFailure("PIN read bad size");
        return PINData.Null();
      } else {
        byte [] pinhextext=ByteArray.subString(payload,bufsize-8,bufsize);//last 8 ignoring lrc
        dbg.VERBOSE("pin:"+Formatter.hexImage(pinhextext));
        byte [] countext=ByteArray.subString(payload,2,bufsize-8); //skip two, take whatever pin didn't
        dbg.VERBOSE("ksn:"+Formatter.hexImage(countext));
        return PINData.unpack(pinhextext,countext);
      }
    }
    finally {
      dbg.Exit();
    }
  }

  public Command onPinResponse (Command cmd){
    int response=cmd.response();
    switch(response){
      case ResponseCode.NO_DATA_READY:{
        pinner.Start();//poll again sometime soon.
      } return null;
      case ResponseCode.SUCCESS:{
        relinquish();
        dbg.ERROR("On response:"+cmd.incoming.toSpam());
        PostData(parsePinResponse(cmd.payload()));
      } return null;
      default:{
        PostFailure("PIN input error:"+Formatter.ox2(response));
      } //join +_+ would like to have distinct returns.
      case ResponseCode.KEYPAD_CANCELED:  {//customer prsses CANCEL
        relinquish();
        PostData(PINData.Null());//+_+ trivial PIN= cancel operation
      } return null;
    }
  }

  class PinResponse implements Callback {
    public Command Post(Command cmd){
      return onPinResponse(cmd);
    }
  }

  private byte [] promptBytes(RealMoney amount,boolean isRefund){
    String s=(isRefund ? promptRefund : promptSale) + amount.Image();
    dbg.VERBOSE("Prompt:"+s);
    return s.getBytes();
  }

  private String typeChar(boolean isRefund){
    return isRefund?  "D": "C";
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


  private BlockCommand showPinPad(String AccountNumber,RealMoney amount,boolean isRefund){
//  int TransactionType= PPAD_TRANS_CREDIT ;
/*transactionType is one of:
  PPAD_TRANS_DEBIT      Debit (decrease) the specified account
  PPAD_TRANS_CREDIT     Credit (increase) the specified account.
    others are irrelevent for entouch
  */
//    String dollars=amount.Image();
//    String dollars= Long.toString(amount.Value()/100);//so the manual states...
String dollars="123"; //since value is ignored and three digits is required
//but the thing fails if we give it three or more here... so trying just two.
    char padder=0xFF;
    LrcBuffer pinrequest= Command.Op(DUKcode);//this is just the LrcBuffer of the command we will send
    pinrequest.append(pinEntryRq);
      pinrequest.appendNibbles(AccountNumber,padder);
      pinrequest.append(typeChar(isRefund));
      pinrequest.appendNibbles(dollars,padder);
    pinrequest.append(padder);//"frame separator"
    pinrequest.append(promptBytes(amount,isRefund));
    pinrequest.end();
dbg.ERROR("PinReqPacket "+Formatter.hexImage(pinrequest.packet()));//debuggin prompt problems+

    entryEnabled=true;
    BlockCommand bc=new BlockCommand("Show PinPad");
    bc.addCommand(Command.JustOpcode(OpCode.ABORT));//the % of time we are NOT showing a form is trivial, so we do this unconditionally
    bc.addCommand(pinrequest);//the substance of this command.
    return bc;
  }


  /**
  */
  public void enablePINEntry(String AccountNumber,RealMoney amount,boolean isRefund){
    QueueCommand(showPinPad(AccountNumber,amount,isRefund),new PinStart("Showing PinPad"));
  }

  class PinStart extends WantZero {
    public PinStart(String s){
      super(s);
    }

    private void splatError(Command cmd,String msg){//added for when doing standalone debug
      dbg.ERROR(msg);
      cmd.service.PostFailure(msg);
    }

    public Command Post(Command cmd){
      switch(cmd.response()){
        case ResponseCode.SUCCESS:{
          try {
            return super.Post(cmd); //will throw if not perfect
          } finally {
            //+_+ if not error in above then ...
            startPolling();
          }
        }
        case 0xF8:{//text says "flash compression failed, but flash is irrelevent for this command
          splatError(cmd,"hardware is screwed");
        } break;
        case 0x03:{
          splatError(cmd,"device not keyed");
        } break;
        case 0xF4: {
          splatError(cmd,"data field incorrect format/length");
        } break;
        case 0xEC: {//look in visa ukpt dox for a syumbol for this
//seems to have occured when we enable card reader after enabling pinpad
          splatError(cmd,"CAN'T SWIPE AND ENTER PIN AT SAME TIME");
        } break;
      }
      return null;
    }
  }

  static final byte DUKcode= DUKPT_PIN_INPUT;
  static final byte pinEntryRq =0x70;
  static final byte PINEntryCancel=0x72;

  public void endEFTTransaction(int i)  {
    if(beenPrepped) {//
      QueueCommand(new Command(DUKcode,PINEntryCancel,"endEFT"),new WantZero("Device Termination Failure"));
      relinquish();
    }
  }
//////////////////////

 /**
  * to test bringing up PIN entry form
  */
  static String testcard="5454545454545454";
  static RealMoney testamt=RealMoney.Zero().setto(412);
  static boolean testrefund=false;
  static public void main(String[] args) {
    //need an entouch
    if(args.length>0){
    ET1K testunit=new ET1K("ET1K.tester");

    testunit.testerConnect(args,19200,dbg);//this guy turns dbg to verbose
    StringStack.setDebug(LogSwitch.OFF);
    testunit.dbg.setLevel(LogSwitch.VERBOSE);

    PINPadService pad=testunit.PINPadService("test");
    pad.setReceiver(new padtester());

    pad.enablePINEntry(testcard,testamt,testrefund);

    int key=0;
    while(true){
      try {
        key=System.in.read();
      }
      catch (Exception ex) {
        dbg.ERROR(ex.getMessage());
        continue;
      }

      switch(key){
      case Ascii.ETX: return;
      case Ascii.LF: continue; //need linefeeds to get keystrokes into this routine
      case '+': {
        testamt.setto(testamt.Value()*10);
      } break;

      case '-': {
        testamt.setto(testamt.Value()/10);
      } break;

      case 'S': case 's':{
        testrefund= false;
      } break;
      case 'R':case 'r':{
        testrefund= true;
      } break;

      default:
        dbg.ERROR("no action for keystroke:"+key);
      }
      pad.endEFTTransaction(0);
      pad.enablePINEntry(testcard,testamt,testrefund);
    }
    } else {
      PINPadService pps=new  PINPadService("just formatting",null);
      BlockCommand bc= pps.showPinPad("5123456789012346",new RealMoney(54321),false);
      for (Command cmd=bc.next();cmd!=null;cmd=bc.next()){
        System.out.println(cmd.outgoing().toSpam());
      }
    }

  }

}//end pinpadservice

class padtester implements QReceiver {
  public boolean Post(Object arf){
    if (arf instanceof PINData) {
      PINData pd=(PINData)arf;
      System.out.println("pin reply:"+EasyCursor.spamFrom(pd));
      return true;
    } else {
      System.out.println("PinResponse got inappropriate object:"+arf);
      return false;
    }
  }
}
//$Id: PINPadService.java,v 1.42 2003/07/27 05:35:04 mattm Exp $
