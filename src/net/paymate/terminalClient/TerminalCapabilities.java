package net.paymate.terminalClient;
/**
 * Title:         $Source: /cvs/src/net/paymate/terminalClient/TerminalCapabilities.java,v $
 * Description:
 * Copyright:     Copyright (c) 2001
 * Company:       PayMate.net
 * @author        PayMate.net
 * @version       $Revision: 1.32 $
 */
import net.paymate.Main;
import net.paymate.awtx.RealMoney;
import net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;

public class TerminalCapabilities implements isEasy {

  public final static String autoApproveKey="autoApprove";
  public final static String autoQueryKey="autoQuery";
  public final static String checksAllowedKey="checksAllowed";
  public final static String creditAllowedKey="creditAllowed";
  public final static String debitAllowedKey="debitAllowed";
  public final static String freePassKey="freePass";
  public final static String enMerchRefKey="enMerchRef";
  public final static String enAutoLogoutKey="enAutoLogout";
  public final static String MerchRefPromptKey="MerchRefPrompt";

  boolean enMerchRef=false;
  public boolean enMerchRef() {
    return enMerchRef;
  }

  boolean enAutoLogout=false;
  public boolean enAutoLogout() {
    return enAutoLogout;
  }

  String MerchRefPrompt;
  public String MerchRefPrompt(){
    return StringX.NonTrivial(MerchRefPrompt) ? MerchRefPrompt : (enMerchRef?"MerchantRef#":"");
  }

  boolean autoQuery=false;
  public boolean autoQuery(){
    return autoQuery;
  }

  boolean checksAllowed=false;
  public boolean acceptsChecks(){
    return checksAllowed;
  }

  boolean debitAllowed=true; //%%%--- for debug only!!!
  public boolean doesDebit(){
    return debitAllowed;
  }

  boolean creditAllowed=false;
  public boolean doesCredit(){
    return creditAllowed;
  }

  RealMoney debitPushThreshold=RealMoney.Zero();
  boolean pushDebit=false;
  public final static String debitPushThresholdKey="debitPushThreshold";
  public final static String pushDebitKey="pushDebit";

  /**
   * exposed for legacy storeConfig sigcapThreshold hack
   * @todo: remove this.
   */
  public RealMoney debitPushThreshold(){
    return debitPushThreshold;
  }

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
  public boolean freePass(){
   return freePass;
  }

/**
 * if true then clerk's entry of sale amount == customer approves.
 * unless we have debit in which case we have to ask CREDIT vs DEBIT at this point
 */
  boolean autoApprove=false;
  public boolean autoApprove(){
    return !debitAllowed && autoApprove;
  }

  /////////////////////////////////////
/**
 * default values are set per legacy of when flag was created.
 */
  public void load(EasyCursor ezp){
    freePass=   ezp.getBoolean(freePassKey,false);
    autoApprove=ezp.getBoolean(autoApproveKey,false);
    checksAllowed =ezp.getBoolean(checksAllowedKey,checksAllowed);
    creditAllowed =ezp.getBoolean(creditAllowedKey,creditAllowed);
    debitAllowed  =ezp.getBoolean(debitAllowedKey,debitAllowed);
    pushDebit=ezp.getBoolean(pushDebitKey,pushDebit);
    ezp.getBlock(debitPushThreshold,debitPushThresholdKey);
    autoComplete=ezp.getBoolean(autoCompleteKey);
    alwaysID=ezp.getBoolean(alwaysIDKey);
    autoQuery=ezp.getBoolean(autoQueryKey,false);
    enMerchRef=ezp.getBoolean(enMerchRefKey,false);
    enAutoLogout=ezp.getBoolean(enAutoLogoutKey,false);
    MerchRefPrompt=ezp.getString(MerchRefPromptKey);//if blank won't appear
  }

  public void save(EasyCursor ezp){
    ezp.setBoolean(freePassKey,freePass);
    ezp.setBoolean(autoApproveKey,autoApprove);

    ezp.setBoolean(creditAllowedKey,creditAllowed);
    ezp.setBoolean(checksAllowedKey,checksAllowed);
    ezp.setBoolean(debitAllowedKey,debitAllowed);

    ezp.setBoolean(pushDebitKey,pushDebit);
    ezp.setBlock(debitPushThreshold,debitPushThresholdKey);
    ezp.setBoolean(autoCompleteKey,autoComplete);
    ezp.setBoolean(alwaysIDKey,alwaysID);
    ezp.setBoolean(autoQueryKey,autoQuery);
    ezp.setBoolean(enAutoLogoutKey,enAutoLogout);
    ezp.setBoolean(enMerchRefKey,enMerchRef);
    ezp.setString(MerchRefPromptKey,MerchRefPrompt);
  }

  public TerminalCapabilities(EasyCursor ezp) {
     load(ezp);
  }

  public TerminalCapabilities() {
    load(Main.props());
  }

  public String splat(String twochar,boolean control){
    return twochar+Bool.signChar(control);
  }

  public String toSpam(){
    return  splat("AA",autoApprove())
            + splat("FP",freePass())
            + splat("ID",AlwaysID())
            + splat("Cr",doesCredit())
//            + splat("Ck",acceptsChecks())
            + splat("Db",doesDebit())
            + splat("Mr",enMerchRef())
            + splat("AL",enAutoLogout())
//            + splat("Th",pushDebit)+debitPushThreshold
            ;
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
    alwaysID        == newone.alwaysID        &&
    enMerchRef      == newone.enMerchRef      &&
    enAutoLogout    == newone.enAutoLogout    &&
    MerchRefPrompt  == newone.MerchRefPrompt
    ;
  }

}
//$Id: TerminalCapabilities.java,v 1.32 2004/02/24 18:31:25 andyh Exp $