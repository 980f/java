/**
* Title:        $Source: /cvs/src/net/paymate/connection/BatchRequest.java,v $
* Description:
* Copyright:    2000,2001 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.19 $
*/
package net.paymate.connection;
import net.paymate.util.*;
import net.paymate.data.*;

public class BatchRequest extends AdminRequest implements isEasy  {
  public ActionType Type(){
    return new ActionType(ActionType.batch);
  }

  public static final boolean WITHDETAIL = true;
  public static final boolean WITHOUTDETAIL = false;

  public boolean isClosing;//legacy: false
  /**
   * if true then details are
   */
  public boolean justSummary ;//legacy: false, always detailed
  public boolean justClose;//legacy: false, close and settle
  public boolean noReport;//legacy: false, no reporting

  public BatchFilter filter;
  // the next parameter is only used on the website, so isn't in the transport
  public Batchid redoBatchid=null; // the batchid to redo (null if it is a new batch, otherwise redo an old one)

  public BatchRequest close(boolean isClosing) {
    this.isClosing = isClosing;
    return this;
  }
  public BatchRequest printDetails(boolean notJustSummary) {
    this.justSummary = !notJustSummary;
    return this;
  }
  public BatchRequest submit(boolean notJustClose) {
    this.justClose = !notJustClose;
    return this;
  }
  public BatchRequest filter(BatchFilter filter) {
    this.filter = filter;
    return this;
  }

  public static BatchRequest JustClose(boolean withdetail){
    return Close(false, withdetail);
  }

  public static BatchRequest CloseAndSubmit(boolean withdetail) {
    BatchRequest br = Close(true, withdetail);
    if(!withdetail) {
      br.noReport = true;
    }
    return br;
  }

  private static BatchRequest Close(boolean andsubmit, boolean withdetail) {
    return new BatchRequest().close(true).submit(andsubmit).printDetails(withdetail).filter(withdetail?BatchFilter.Approved():BatchFilter.None());
  }

  // can't close or submit, but print details with a filter
  public static BatchRequest Listing(){
    return new BatchRequest().close(false).printDetails(true).submit(false).filter(BatchFilter.Approved());
  }

  // can't submit from the menu; use DepositRequest() instead
  public static BatchRequest fromMenu(boolean closer, boolean concise){
    return new BatchRequest().close(closer).printDetails(!concise).submit(false);
  }

  /**
   * @unwise direct use is unlikely to be correct. see static methods
   */
  public BatchRequest(){//required by fromProperties
    //used by fromProperties
  }

  static final String isClosingKey="isClosing";
  static final String justSummaryKey="justSummary" ;//legacy: always detailed
//  static final String allTerminalsKey="allTerminals";//legacy: just one terminal
  static final String justCloseKey="justClose";//legacy: false
  static final String noReportKey="noReport";//legacy: false

  static final String filterKey="filter";


  public void save(EasyCursor ezp){
    ezp.setBoolean(isClosingKey,isClosing);
    ezp.setBoolean(justSummaryKey,justSummary) ;//legacy: always detailed
    if(isClosing){//only relevent if closing.
      ezp.setBoolean(justCloseKey,justClose);//legacy: false
    }
    ezp.setBlock(filter,filterKey);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    isClosing=ezp.getBoolean(isClosingKey);
    justSummary=ezp.getBoolean(justSummaryKey) ;//legacy: always detailed
    justClose=isClosing && ezp.getBoolean(justCloseKey);//legacy: false
    ezp.getBlock(filter,filterKey);
    super.load(ezp);
  }

}
//$Id: BatchRequest.java,v 1.19 2003/05/02 18:15:13 mattm Exp $
