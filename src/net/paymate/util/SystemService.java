package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/SystemService.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.timer.*; // StopWatch
import net.paymate.lang.ThreadX;
import net.paymate.text.Formatter;
// +++ Separate the Service stuff into more "control" type structures and "interface" type structures, and put control in np.data or np.util or np.service, or distribute them, and put the "display" portions in a presentation package, like np.web!
//import org.apache.ecs.html.*;// A

public class SystemService extends Service {

  private final StopWatch uptime= new StopWatch();
  public long uptime() {
    return uptime.millis();
  }
  public long upsince() {
    return uptime.startedAt();
  }
  public static final String NAME = "System";
  public SystemService(ServiceConfigurator cfg) {
    super(NAME, cfg);
  }
  public void down() {
    // stub
  }
  public void up() {
    // stub
  }
  public boolean isUp() {
    return true; // always, since if this method will run, the system is up!
  }

  /**
   * Number of processes
   */
  public String svcCnxns() {
    return "1"; // for the number of processes running here
  }

  /**
   * Number of threads
   */
  public String svcTxns() {
    return ""+ThreadX.ThreadCount();
  }

  /**
   * Uptime
   */
  public String svcAvgTime() {
    return DateX.millisToTime(uptime());
  }

  /**
   * Revision info
   */
  public String svcNotes() {
    long freemem = Runtime.getRuntime().freeMemory();
    long ttlmem = Runtime.getRuntime().totalMemory();
    String linkText = "" + (freemem * 100 / ttlmem) + "% freemem [" + of(Formatter.sizeLong(freemem), Formatter.sizeLong(ttlmem)) + " B]";
    return linkText;
  }

}