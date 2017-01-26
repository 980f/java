package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/TransactionTime.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 * @todo reduce legacy method names to something shorter.
 */

import net.paymate.util.*;

import java.util.Date;
/** @deprecated @see UTC (which was built from this but with a different interface)
 */
public class TransactionTime {
  public final static String transtarttimeFormat = "yyyyMMddHHmmss";
  private static final LocalTimeFormat trantime=LocalTimeFormat.Utc(transtarttimeFormat);

  private static final Monitor tranTimeMonitor = new Monitor("TransactionTime");
 /**
  * convert @param date into string for transtarttime field
  * @deprecated @see UTC
  */
  public static final String forTrantime(Date utc){
    String ret = "";
    try {
      tranTimeMonitor.getMonitor();
      ret = (utc==null)?null:trantime.format(utc);
    }
//    catch (Exception e) {
//      dbg.Caught(e);
//    }
    finally {
      tranTimeMonitor.freeMonitor();
      return ret;
    }
  }
//  /**
//  * convert @param utcTix into string for transtarttime field
//  *   * @deprecated @see UTC
//  */
//
//  public static final String forTrantime(long utcTix){
//    return forTrantime(new Date(utcTix));
//  }
///**
// *   * @deprecated @see UTC
// */
//  public static final Date tranUTC(String dbvalue){
//      Date date = Safe.Now();
//    try {
//      tranTimeMonitor.getMonitor();
//      date = trantime.parse(dbvalue);
//    }
  //    catch (Exception e) {
  //      dbg.Caught(e);
  //    }
//    finally {
//      tranTimeMonitor.freeMonitor();
//      return date;
//    }
//  }

}
//$Id: TransactionTime.java,v 1.5 2002/05/20 22:33:23 mattm Exp $