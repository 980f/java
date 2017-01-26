package net.paymate.servlet;

import net.paymate.util.*;
import javax.servlet.http.*; // HttpSessionContext
import java.util.*; // Enumeration

/**
 * Title:        $Source: /cvs/src/net/paymate/servlet/SessionedServletService.java,v $
 * Description:  Base class for Servlets Services; EXTEND!!!
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

public abstract class SessionedServletService extends Service {

  private SessionedServlet servlet = null;

  /* pcakage */ ServiceConfigurator configger() {
    return configger;
  }

  public SessionedServletService(String name, SessionedServlet servlet, ServiceConfigurator cfg) {
    super(name, cfg);
    this.servlet = servlet;
    initLog();
  }

  public String svcTxns() {
    return ""+servlet.httptimer.getCount();
  }
  public String svcPend() {
    return ""+servlet.pending.value();
  }
  public String svcAvgTime() {
    return DateX.millisToSecsPlus(servlet.httptimer.getAverage());
  }
  public String svcWrites() {
    return printByteStats(servlet.outgoing);
  }
  public String svcReads() {
    return "~ "+printByteStats(servlet.incoming);
  }

  // +++ make it do something !!!
  public boolean isUp() {
    return !servlet.down;
  }
  public void down() {
    servlet.down = true;
    markStateChange();
  }
  public void up() {
    servlet.down = false; // reload params? +++
    markStateChange();
  }

  public static final String MAXAGEMILLIS = "maxAgeMillis";
  public static final String TIMEOUTMILLIS = "timeoutMillis";

  public abstract long MAXAGEMILLIS();
  public abstract long TIMEOUTMILLIS();

  public SessionTermParams getTerminatums() {
    return (servlet != null && servlet.configger != null) ?
      new SessionTermParams(
        servlet.configger.getLongServiceParam(serviceName(), MAXAGEMILLIS, MAXAGEMILLIS()),
        servlet.configger.getLongServiceParam(serviceName(), TIMEOUTMILLIS, TIMEOUTMILLIS()))
      : new SessionTermParams(MAXAGEMILLIS(),TIMEOUTMILLIS());
  }

}
