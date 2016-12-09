package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/StandinStatus.java,v $
 * Description:  constant object for making a snapshot of backlog state
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;

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
    if(Safe.NonTrivial(prefix)){
      return prefix+txnBacklog()+"/"+rcpBacklog()+Fstring.matcher(prefix.charAt(prefix.length()-1));
    } else {
      return toSpam(" ");
    }
  }

  public String toSpam(){
    return toSpam(" ");
  }

}
//$Id: StandinStatus.java,v 1.3 2001/11/17 00:38:34 andyh Exp $