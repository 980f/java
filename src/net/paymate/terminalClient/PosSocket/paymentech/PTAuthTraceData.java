package net.paymate.terminalClient.PosSocket.paymentech;

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/PTAuthTraceData.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class PTAuthTraceData extends AuthTraceData {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PTAuthTraceData.class);

  public String ptcardtype = "";
  public String networkAndSource = "";
  public String ptrrn = ""; // PT retreival reference number (not the "pt trace")

  private static final int MAXSIZE = 2 + 3 + 8;
  // 2 chars     3 chars                                       8 chars    NO MORE:   8 chars
  // Card Type + Authorizing Network ID/Authorization Source + tracenumberNO MORE: + RRN
  public PTAuthTraceData(String ptcardtype, String networkAndSource, String ptrrn) {
    this.ptcardtype = ptcardtype;
    this.networkAndSource = networkAndSource;
    this.ptrrn = ptrrn;
    fullImage(); // go ahead and build fullAuthTraceData now
  }
  public PTAuthTraceData(String fullAuthTraceData) {
    super();
    setto(fullAuthTraceData);
  }

  public String fullImage() { // for storage
    if(!StringX.NonTrivial(fullAuthTraceData)) {
      // try to build fullAuthTraceData from parts
      Packet pacman = new Packet(MAXSIZE);
      pacman.appendAlpha(2, ptcardtype);
      pacman.appendAlpha(3, networkAndSource);
      pacman.appendAlpha(8, ptrrn);
      fullAuthTraceData = new String(pacman.packet());
    }
    return fullAuthTraceData;
  }
  public AuthTraceData setto(String authTraceData) { // from storage
    fullAuthTraceData = authTraceData;
    if(!StringX.NonTrivial(fullAuthTraceData)) {
      dbg.ERROR("PTAuthTraceData.setto() receiving corrupt packet [null or empty]!");
    } else {
      if(fullAuthTraceData.length() < MAXSIZE) {
        dbg.WARNING("PTAuthTraceData.setto() receiving possibly corrupt packet!");
      }
      // parse fullAuthTraceData into its independent components
      ptcardtype = StringX.subString(fullAuthTraceData, 0, 2);
      networkAndSource = StringX.subString(fullAuthTraceData, 2, 5);
//      tracenumber = StringX.subString(fullAuthTraceData, 5, 13);
      ptrrn = StringX.subString(fullAuthTraceData, 13, 21);
      dbg.VERBOSE("Just setto: " + this);
    }
    return this;
  }

  public String toString() {
    return "ptcardtype="+ptcardtype+", networkAndSource="+networkAndSource+", ptrrn="+ptrrn/*+", retrievalReferenceNumber="+retrievalReferenceNumber*/+", fullAuthTraceData="+fullAuthTraceData;
  }
}
