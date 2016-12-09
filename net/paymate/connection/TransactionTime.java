package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/TransactionTime.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 * @todo reduce legacy method names to something shorter.
 */

import net.paymate.util.*;

import java.util.Date;

public class TransactionTime {
  public final static String transtarttimeFormat = "yyyyMMddHHmmss";//maimsail's choice
  private static final LocalTimeFormat trantime=LocalTimeFormat.Utc(transtarttimeFormat);

//  public static final TimeRange ForAllTime(){
//    return TimeRange.Create(transtarttimeFormat,TRANSTARTTIME).
//    setStart(LocalTimeFormat.genesis).setEnd(Safe.Now());
//  }

  private static final Monitor tranTimeMonitor = new Monitor("TransactionTime");
 /**
  * convert @param date into string for transtarttime field
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
  /**
  * convert @param utcTix into string for transtarttime field
  */

  public static final String forTrantime(long utcTix){
    return forTrantime(new Date(utcTix));
  }

  public static final Date tranUTC(String dbvalue){
      Date date = Safe.Now();
    try {
      tranTimeMonitor.getMonitor();
      date = trantime.parse(dbvalue);
    }
//    catch (Exception e) {
//      dbg.Caught(e);
//    }
    finally {
      tranTimeMonitor.freeMonitor();
      return date;
    }
  }


}