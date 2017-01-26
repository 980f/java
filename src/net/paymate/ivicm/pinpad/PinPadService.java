/* $Id: PinPadService.java,v 1.8 2003/09/29 20:08:58 andyh Exp $ */
package net.paymate.ivicm.pinpad;

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.RealMoney;
import java.util.Vector;
import net.paymate.lang.ReflectX;

public class PinPadService extends Service  {
  static final String VersionInfo = ReflectX.shortClassName(PinPadService.class)+" (C) PayMate.net 2000+ $Revision: 1.8 $";
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(PinPadService.class);

  //visa UKPT command codes:
  static final String pinEntryRq ="70";
  static final String PINEntryCancel="72";

  //internal state
  boolean beenPrepped=false;
  boolean entryEnabled=false;
  int keyinuse;
  boolean WaitingOnKeyLoad=false;


  final String promptSale=  "Pay:";
  final String promptRefund="Get:";
  static final byte DUKPT_PIN_INPUT = 0x62;//98;
  static final byte GET_PIN_DATA = 0x63;//99;
//??  0x6A;//
  static final byte UKPT_RESPONSE= 0x71;//response code indicating ukpt response.
//is followed by 00, 10-20 hexdigits, 16 hexdigits

  public PinPadService(String s,encrypt100 hw){
    super(s,hw);
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


 /**
  *
  */
  private PINData parse71(){
    //begins wtih "710".
    //ksn variable followed by
    //16 pin. bastards.
    return PINData.Null();
  }
  /**
  */
  public void enablePINEntry(String AccountNumber,RealMoney amount,boolean isRefund){
    //first packet: display something (Z2 or Z3
    //startpacket
    //append("60");
    //append(AccountNumber);
    //end packet
    //expect packet "71"

    //OR
    //start().append("70.").appendFrame(AccountNumber).append(isRefund?'C':'D').append(amount.image()).end()
    //expect("71");

  }

  static final byte DUKcode= DUKPT_PIN_INPUT;

  public void endEFTTransaction(int i){
    //stxwrap "72"
  }

  //////////////////////

 /**
  * to test bringing up PIN entry form
  */
  static String testcard="5454545454545454";
  static RealMoney testamt=RealMoney.Zero().setto(412);
  static boolean testrefund=false;
  static public void main(String[] args) {
    //need an device

//    testunit.testerConnect(args,9600,dbg);//this guy turns dbg to verbose
//    StringStack.setDebug(LogSwitch.OFF);
//    testunit.dbg.myLevel.setLevel(LogLevelEnum.VERBOSE);
//
//    PinPadService pad=testunit.PINPadService("test");
//    pad.setReceiver(new padtester());
//
//    pad.enablePINEntry(testcard,testamt,testrefund);
//
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
//      pad.endEFTTransaction(0);
//      pad.enablePINEntry(testcard,testamt,testrefund);
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
//$Id: PinPadService.java,v 1.8 2003/09/29 20:08:58 andyh Exp $
