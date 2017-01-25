package net.paymate.connection;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Id: StoreConfig.java,v 1.4 2001/07/18 22:00:16 andyh Exp $
*/


import net.paymate.util.*;
import java.util.*;
import net.paymate.ISO8583.data.*;
import net.paymate.terminalClient.*;

public class StoreConfig {

  private final static String storeinfoKey="storeinfo";
  public StoreInfo si=new StoreInfo();//things like addresses

  private final static String receiptKey="receipt";
  public ReceiptFormat receipt=new ReceiptFormat();

  private final static String termcapKey="termcaps";
  public TerminalCapabilities termcap=new TerminalCapabilities();



  public void saveas(String cfgKey,EasyCursor ezc){
    ezc.push(cfgKey);//{
      ezc.push(storeinfoKey);//{
        si.save(ezc);

        ezc.setKey(receiptKey);
        receipt.save(ezc);

        ezc.setKey(termcapKey);
        termcap.save(ezc);
      ezc.pop();//}
    ezc.pop();//}
  }

  public void loadfrom(String cfgKey,EasyCursor ezc){
    ezc.push(cfgKey);//{
      ezc.push(storeinfoKey);//{
        si.load(ezc);

        ezc.setKey(receiptKey);
        receipt.load(ezc);

        ezc.setKey(termcapKey);
        termcap.load(ezc);
      ezc.pop();//}
    ezc.pop();//}
  }

  public StoreConfig() {
    //defaulted
  }
  //////////////////////
  public boolean equals(StoreConfig newone){
    return si.equals(newone.si)&&receipt.equals(newone.receipt)&&termcap.equals(newone.termcap);
  }

}
//$Id: StoreConfig.java,v 1.4 2001/07/18 22:00:16 andyh Exp $
