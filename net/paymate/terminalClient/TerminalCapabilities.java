package net.paymate.terminalClient;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: TerminalCapabilities.java,v 1.12 2001/11/03 13:16:42 mattm Exp $
 */

import net.paymate.Main;
import net.paymate.awtx.RealMoney;
import net.paymate.util.*;

public class TerminalCapabilities implements isEasy {

  boolean checksAllowed=false;
  public final static String checksAllowedKey="doChecks";
  public boolean acceptsChecks(){
    return checksAllowed;
  }

  boolean debitAllowed=false;
  public final static String debitAllowedKey="doDebit";
  public boolean doesDebit(){
    return debitAllowed;
  }

  boolean creditAllowed=false;
  public final static String creditAllowedKey="doCredit";
  public boolean doesCredit(){
    return creditAllowed;
  }

  RealMoney debitPushThreshold=new RealMoney();
  boolean pushDebit=false;
  public final static String debitPushThresholdKey="debitPushThreshold";
  public final static String pushDebitKey="pushDebit";
/**
 * @return true if sale is expensive enough to payback debit flat fee over credit percentage
 */
  public boolean pushDebit(RealMoney salevalue){
    return pushDebit&&debitPushThreshold.compareTo(salevalue)<0;
  }

  boolean autoComplete=false;//legacy setting
  public final static String autoCompleteKey="autoComplete";

  /**
   * @return true if we are going to not wait around for acknowledgement of receipt saved on server.
   */
  public boolean beDoneIfApproved(){
    return autoComplete;
  }

  boolean alwaysID=false;
  public final static String alwaysIDKey="alwaysID";
  public boolean AlwaysID(){
    return alwaysID;
  }

    //the following are misplaced. Should be terminal or store options!
/**
 * if true don't need passcode for clerk forced buttons.
 */
  boolean freePass=false;
  public final static String freePassKey="freePass";
  public boolean freePass(){
   return freePass;
  }

/**
 * if true then clerk's entry of sale amount == customer approves.
 */
  boolean autoApprove=false;
  public final static String autoApproveKey="autoApprove";
  public boolean autoApprove(){
    return autoApprove;
  }

  /////////////////////////////////////
  public void load(EasyCursor ezp){
    freePass=   ezp.getBoolean(freePassKey,false);
    autoApprove=ezp.getBoolean(autoApproveKey,false);

    checksAllowed =ezp.getBoolean(checksAllowedKey,checksAllowed);
    creditAllowed =ezp.getBoolean(creditAllowedKey,creditAllowed);
    debitAllowed  =ezp.getBoolean(debitAllowedKey,debitAllowed);
    pushDebit=ezp.getBoolean(pushDebitKey,pushDebit);
    //+_+ create an ezp.update(key,Object);
    String newthresh=ezp.getString(debitPushThresholdKey);
    if(Safe.NonTrivial(newthresh)){
      debitPushThreshold= new RealMoney(newthresh);
    }
    autoComplete=ezp.getBoolean(autoCompleteKey);
    alwaysID=ezp.getBoolean(alwaysIDKey);
//    return ezp;
  }

  public void save(EasyCursor ezp){
    ezp.setBoolean(freePassKey,freePass);
    ezp.setBoolean(autoApproveKey,autoApprove);

    ezp.setBoolean(creditAllowedKey,creditAllowed);
    ezp.setBoolean(checksAllowedKey,checksAllowed);
    ezp.setBoolean(debitAllowedKey,debitAllowed);

    ezp.setBoolean(pushDebitKey,pushDebit);
    ezp.setString(debitPushThresholdKey,debitPushThreshold.Image());
    ezp.setBoolean(autoCompleteKey,autoComplete);
    ezp.setBoolean(alwaysIDKey,alwaysID);
  }

  public TerminalCapabilities(EasyCursor ezp) {
     load(ezp);
  }

  public TerminalCapabilities() {
    load(Main.props());
  }

  public String toSpam(){
    return "AA"+Bool.signChar(autoApprove())+"FP"+Bool.signChar(freePass())+"ID"+Bool.signChar(AlwaysID())+"Cr"+Bool.signChar(doesCredit()) +"Ck"+Bool.signChar(acceptsChecks()) +"Db"+Bool.signChar(doesDebit())+"Th"+Bool.signChar(pushDebit)+debitPushThreshold;
  }

    public boolean equals(TerminalCapabilities newone){
      return
      autoApprove()   == newone.autoApprove()   &&
      freePass()      == newone.freePass()      &&
      checksAllowed   == newone.checksAllowed   &&
      debitAllowed    == newone.debitAllowed    &&
      debitPushThreshold.compareTo(newone.debitPushThreshold)==0 &&
      pushDebit       == newone.pushDebit       &&
      autoComplete    == newone.autoComplete    &&
      alwaysID        == newone.alwaysID
      ;
    }

}
//$Id: TerminalCapabilities.java,v 1.12 2001/11/03 13:16:42 mattm Exp $