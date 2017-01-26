package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/StandinStatus.java,v $
 * Description:  constant object for making a snapshot of backlog state
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;

public class StandinStatus {

  int txnCount;
  int rcpCount;

  public boolean haveTxnBacklog(){
    return txnCount>0;
  }

  public boolean haveBacklog(){
    return haveTxnBacklog()||rcpCount>0;
  }

  public int txnBacklog(){
    return txnCount;
  }

  public int rcpBacklog(){
    return rcpCount;
  }

  public StandinStatus addTxnCount(int size) {
    txnCount += size;
    return this;
  }

  public StandinStatus addRcpCount(int size) {
    rcpCount += size;
    return this;
  }

  /**
   * snapshot the backlog counters
   */
  public StandinStatus(int txnCount,int rcpCount) {
    this.txnCount=txnCount;
    this.rcpCount=rcpCount;
  }

  /**
   * @return txns/receipts
   * @param prefix goes at fron of string, its last char's complementary char goes at last
   */
  public String toSpam(String prefix){
    if(StringX.NonTrivial(prefix)){
      return Formatter.ratioText(prefix,txnBacklog(),rcpBacklog());
    } else {
      return toSpam(" ");
    }
  }

  public String toSpam(){
    return toSpam(" ");
  }

}
//$Id: StandinStatus.java,v 1.5 2003/07/27 05:34:55 mattm Exp $