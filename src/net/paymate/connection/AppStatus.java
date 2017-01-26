package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/AppStatus.java,v $
 * Description: get runtime info for display at server.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.lang.ThreadX;

public class AppStatus implements isEasy {
//  public static int threadThrashLimit=12;

  public long freeMemory;
  final static String freeMemoryKey="freeMemory";
  public long totalMemory;
  final static String totalMemoryKey="totalMemory";
  public int activeCount;
  final static String activeCountKey="activeCount";
  public int activeAlarmsCount;
  final static String activeAlarmsCountKey="activeAlarmsCount";
  public String revision;
  final static String revisionKey="revision";
  public String threadDump;
  final static String threadDumpKey = "threadDump";


  /**
   * @todo add averager for freeMemory
   * @todo add velocity for freeMemory
   * @todo ditto for activeCount.
   */

  boolean snapped=false;
  public AppStatus snapshot(){
    freeMemory=Runtime.getRuntime().freeMemory();
    totalMemory=Runtime.getRuntime().totalMemory();//can this ever change while running?
    activeCount=Thread.currentThread().getThreadGroup().activeCount();
    activeAlarmsCount=Alarmer.alarmCount();
    revision = net.paymate.Revision.Version();
    threadDump = ThreadX.ThreadDump(ThreadX.RootThread()).asParagraph("|");
    snapped=true;
    return this;
  }
  /////////////////////
  // transport
  public void save(EasyCursor ezp){
    if(!snapped){
      snapshot();
    }
    ezp.setLong(freeMemoryKey,freeMemory);
    ezp.setLong(totalMemoryKey,totalMemory);
    ezp.setInt(activeCountKey,activeCount);
    ezp.setInt(activeAlarmsCountKey,activeAlarmsCount);
    ezp.setString(revisionKey,revision);
    ezp.setString(threadDumpKey,threadDump);
  }

  public void load(EasyCursor ezp){
    freeMemory=ezp.getLong(freeMemoryKey);
    totalMemory=ezp.getLong(totalMemoryKey);
    activeCount=ezp.getInt(activeCountKey);
    activeAlarmsCount=ezp.getInt(activeAlarmsCountKey);
    revision=ezp.getString(revisionKey);
    threadDump=ezp.getString(threadDumpKey);
    snapped=true;
  }

  public AppStatus() {
//    snapshot();
  }

  public String toString(){
    return EasyCursor.spamFrom(this);
  }

}
//$Id: AppStatus.java,v 1.16 2003/07/27 05:34:54 mattm Exp $