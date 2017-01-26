package net.paymate.hypercom;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/hypercom/IceForm.java,v $</p>
 * <p>Description: Singleton form generator</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.connection.StoreConfig;

import net.paymate.Main;
import net.paymate.util.*;

import net.paymate.terminalClient.*;


public class IceForm {
  static StoreConfig cfg;

  public static void setStoreInfo(StoreConfig cfg){
    IceForm.cfg=cfg;
  }
  final static String genericCreditAgreement=  "\"I agree to the amount per my cardholder agreement\"";

  static IcePick FubarForm(){
    IcePick thisform= IcePick.New(1);
    thisform.addText("Program Error!!");
    thisform.addItem("Cancel",ButtonTag.CustomerCancels);
    return thisform;
  }

  static IcePick WaitPatronCredit(Uinfo dynamic){//got sale amount, waiting on customer to tell us type/credit ok
    IcePick thisform= IcePick.New(6);
    thisform.addText(dynamic.sale.youPay());
    thisform.addText("Select Card Type ...");
    thisform.addText("to Indicate Approval.");
    thisform.addItem("CREDIT",ButtonTag.CustomerAmountOk);
    thisform.addText("");
    thisform.addItem("DEBIT",ButtonTag.DoDebit);
    thisform.addText("");
    thisform.addItem("Press here to Cancel",ButtonTag.CustomerCancels);
    thisform.addText("touch one of the above");
    return thisform;
  }

  /**
   * I don't think this guy should ever be called, not until the clerk interface is done via an attached keypad...
   */
  static IcePick WaitClerkCredit(Uinfo dynamic){//still waiting on clerk to enter sale amount
    if(cfg.termcap.doesDebit()){
      IcePick thisform= IcePick.New(2);
      thisform.addText("Card type?");
      thisform.addItem("Credit",ButtonTag.DoCredit);
      thisform.addItem("Debit",ButtonTag.DoDebit);
//    thisform.addItem("Cancel",ButtonTag.CustomerCancels);
      return thisform;
    } else {
      return FubarForm();//display is controled by clerk interface.
    }
  }

}
//$Id: IceForm.java,v 1.2 2003/05/28 00:31:58 andyh Exp $