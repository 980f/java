package net.paymate.data;

import net.paymate.util.*; // ErrorLogStream

/**
 * Title:        $Source: /cvs/src/net/paymate/data/AuthTraceData.java,v $
 * Description:  Base class that just uses the whole string as the authtracedata
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

public class AuthTraceData {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthTraceData.class);

  protected String fullAuthTraceData = ""; // to prevent NPE's

  public AuthTraceData() {
  }

  public AuthTraceData(String fullAuthTraceData) {
    dbg.VERBOSE("About to call setto with '"+fullAuthTraceData+"'.");
    setto(fullAuthTraceData);
  }
  public AuthTraceData(AuthTraceData authTraceData) {
    setto(authTraceData.fullImage());
  }

  // overload these to do auth-specific parsing and building
  public String fullImage() { // for storage
    return fullAuthTraceData;
  }
  public AuthTraceData setto(String authTraceData) { // from storage
    fullAuthTraceData = authTraceData;
    return this;
  }
}
