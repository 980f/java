package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/EnterprisePermissions.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public class EnterprisePermissions {

  public EnterprisePermissions() {
    clear();
  }
  public EnterprisePermissions(String legacy) {
    this();
    parseLegacy(legacy);
  }
  public void clear() {
    canWeb = false;
    canDB = false;
    canViewAuthMsgs = false;
  }
  public boolean canWeb;
  public boolean canDB;
  public boolean canViewAuthMsgs;

  public void parseLegacy(String perms) {
    if(StringX.NonTrivial(perms)) {
      canWeb = perms.indexOf("E") > ObjectX.INVALIDINDEX;
      canDB  = perms.indexOf("D") > ObjectX.INVALIDINDEX;
      canViewAuthMsgs = perms.indexOf("M") > ObjectX.INVALIDINDEX;
    }
  }

  public EnterprisePermissions from(EnterprisePermissions other) {
    this.canDB = other.canDB;
    this.canViewAuthMsgs = other.canViewAuthMsgs;
    this.canWeb = other.canWeb;

    return this;
  }

  public String toString() {
    return "canWeb="+canWeb+", canDB="+canDB+", canViewAuthMsgs"+canViewAuthMsgs;
  }
}