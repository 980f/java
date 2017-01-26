package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/ClerkPrivileges.java,v $
 * Description:  collation of permission flags for a clerk.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ClerkPrivileges.java,v 1.2 2003/09/05 13:41:21 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public class ClerkPrivileges implements isEasy {
  //privileges set for common cashier WITHIN a store (not across the enterprise).
  public boolean canSALE=   true;
  public boolean canMOTO=   false;
  public boolean canClose=  false;
  public boolean canVOID=   true;
  public boolean canREFUND= false;

  public void save(EasyCursor ezp){
    ezp.setBoolean("canSALE",canSALE);
    ezp.setBoolean("canMOTO",canMOTO);
    ezp.setBoolean("canClose",canClose);
    ezp.setBoolean("canVOID",canVOID);
    ezp.setBoolean("canREFUND",canREFUND);
  }

  public void load(EasyCursor ezp){
    canSALE=    ezp.getBoolean("canSALE",true);
    canMOTO=    ezp.getBoolean("canMOTO",canSALE);//same as sale for now.
    canClose=   ezp.getBoolean("canClose",false);
    canVOID=    ezp.getBoolean("canVOID",true);
    canREFUND=  ezp.getBoolean("canREFUND",false);
  }

  public ClerkPrivileges grantAllForStore() {
    canSALE=   canMOTO=   canClose=  canVOID=   canREFUND= true;
    return this;
  }

  public void clear(){
    canSALE=   canMOTO=   canClose=  canVOID=   canREFUND= false;
  }

  public void parseLegacy(String perms) {
    if(StringX.NonTrivial(perms)) {
      canClose = perms.indexOf("C") > ObjectX.INVALIDINDEX;
      canSALE  = perms.indexOf("S") > ObjectX.INVALIDINDEX;
      canVOID  = perms.indexOf("V") > ObjectX.INVALIDINDEX;
      canREFUND= perms.indexOf("R") > ObjectX.INVALIDINDEX;
    }
  }

  public ClerkPrivileges(String perms) {
    parseLegacy(perms);
  }


  public ClerkPrivileges() {
//see declarations for defaults.
  }

  public String toSpam(){
    return
    "S"+Bool.signChar(canSALE)
    +"V"+Bool.signChar(canVOID)
    +"R"+Bool.signChar(canREFUND)
    +"M"+Bool.signChar(canMOTO)
    +"C"+Bool.signChar(canClose);
  }

}
//$Id: ClerkPrivileges.java,v 1.2 2003/09/05 13:41:21 mattm Exp $