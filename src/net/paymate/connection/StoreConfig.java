package net.paymate.connection;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Id: StoreConfig.java,v 1.12 2003/10/25 20:34:18 mattm Exp $
*/


import net.paymate.util.*;
import java.util.*;
import net.paymate.data.*;
import net.paymate.terminalClient.*;
import net.paymate.awtx.RealMoney;


public class StoreConfig implements isEasy {

  private final static String storeinfoKey="storeinfo";
  public StoreInfo si=new StoreInfo();//things like addresses

  private final static String receiptKey="receipt";
  public ReceiptFormat receipt=new ReceiptFormat();

  private final static String termcapKey="termcaps";
  public TerminalCapabilities termcap=new TerminalCapabilities();

  public RealMoney sigcapThreshold=RealMoney.Zero();//legacy value.

  final static String sigcapThresholdKey="sigcapThreshold";
  /**
 * @return whether to ask for a signature
 */
  public boolean signFor(RealMoney amount){
    if(sigcapThreshold.NonTrivial()){//--- legacy patch
      return sigcapThreshold.compareTo(amount)<0;
    } else {//--- legacy patch
       return termcap.debitPushThreshold().compareTo(amount)<0; //--- legacy patch
    }//--- legacy patch
  }

  public void save(EasyCursor ezc){
    ezc.setBlock(si,storeinfoKey);
    ezc.setBlock(receipt,receiptKey);
    ezc.setBlock(termcap,termcapKey);
    ezc.setBlock(sigcapThreshold,sigcapThresholdKey);
  }

  public void load(EasyCursor ezc){
    ezc.getBlock(si,storeinfoKey);
    ezc.getBlock(receipt,receiptKey);
    ezc.getBlock(termcap,termcapKey);
    ezc.getBlock(sigcapThreshold,sigcapThresholdKey);//default of zero is desirable here
  }

  private static StoreConfig nullone;

  public static StoreConfig Null(){
    if (nullone==null){
      nullone= new StoreConfig();
    }
    return nullone;
  }

  public StoreConfig() {
    //defaulted
  }
  //////////////////////
  public boolean equals(StoreConfig newone){
    return si.equals(newone.si)&&receipt.equals(newone.receipt)&&termcap.equals(newone.termcap);
  }
  ////////////////
  public String toSpam(){
    return EasyCursor.spamFrom(this);
  }

}
//$Id: StoreConfig.java,v 1.12 2003/10/25 20:34:18 mattm Exp $
