package net.paymate.authorizer.linkpoint;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LinkpointAuthResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @@author PayMate.net
 * @@version $Revision: 1.17 $
 */

import net.paymate.util.*;
import net.paymate.authorizer.*;
import net.paymate.data.*;
import clearcommerce.ssl.*;
import java.util.Enumeration;
import net.paymate.lang.StringX;

public class LinkpointAuthResponse extends AuthResponse {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LinkpointAuthResponse.class, ErrorLogStream.VERBOSE);

  public LinkpointAuthResponse() {
    //see initializers.
  }

  public JCharge charge = null;

  protected boolean parse(JCharge charge){
    try {
      dbg.Enter("parse");
      this.charge = charge;
      if(charge != null) {
        boolean approved = StringX.equalStrings("APPROVED", charge.getApproved().toUpperCase());
        action=approved ? ActionCode.Approved : ActionCode.Declined;
        authcode = charge.getCode();
        // so that the authmsg will go into the database, cook it to remove crlf's
        authmsg = StringX.TrivialDefault(Ascii.cooked(charge.getError()),charge.getApproved()+":"+authcode); // might get truncated by database
        authrrn = charge.getRef();
        authTraceData.setto(""+charge.getTDate());
      } else {
        action= ActionCode.Failed;
        authmsg="Transmission exception";
      }
      dbg.VERBOSE("parsed:\n"+this);
      return true;
    } catch (Exception e) {
      dbg.Caught(e);
      action= ActionCode.Failed;
      authmsg="Parser exception";
      return false;
    } finally {
      dbg.Exit();
    }
  }

  public void process(Packet toFinish){
    // stub
  }

  public String toString() {
    return ""+super.toString()+"\n"+String.valueOf(toSpam(charge));
  }

  private static final void addLine(String name, String value, StringBuffer buffer) {
    buffer.append("\ncharge."+name+"=["+value+"]");
  }

  public static final String TOSPAM(JCharge charge) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("\nCHARGE:");
    if(charge != null) {
      addLine("getAVSCode", charge.getAVSCode(), buffer);
      addLine("getApproved", charge.getApproved(), buffer);
      addLine("getCode", charge.getCode(), buffer);
      addLine("getError", charge.getError(), buffer);
      addLine("getOrdernum", charge.getOrdernum(), buffer);
      addLine("getPayServ", charge.getPayServ(), buffer);
      addLine("getRef", charge.getRef(), buffer);
      addLine("getTDate", String.valueOf(charge.getTDate()), buffer);
      addLine("getTime", charge.getTime(), buffer);
      StringBuffer names = new StringBuffer();
      Enumeration e = charge.getESDNames();
      int esdcount = 0;
      while(e.hasMoreElements()) {
        String esdname = (String) e.nextElement();
        if(esdname != null) {
          names.append("ESD" + esdcount + ":" + esdname+",");
        }
        esdcount++;
      }
      addLine("getESDNames", names.toString(), buffer);
    } else {
      buffer.append("NULL");
    }
    return buffer.toString();
  }

  public String toSpam(JCharge charge) {
    return TOSPAM(charge);
  }

}
