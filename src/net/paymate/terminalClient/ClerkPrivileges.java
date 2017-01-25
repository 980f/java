package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ClerkPrivileges.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ClerkPrivileges.java,v 1.4 2001/10/10 22:47:54 andyh Exp $
 */

import net.paymate.util.*;

public class ClerkPrivileges implements isEasy {
  //privileges set for common cashier.
  public boolean canSALE=   true;
  public boolean canMOTO=   false;
  public boolean canClose=  false;
  public boolean canVOID=   true;
  public boolean canREFUND= false;


  public EasyCursor saveas(String key, EasyCursor ezp){
    ezp.push(key);
    save(ezp);
    return ezp.pop();
  }

  public void save(EasyCursor ezp){
    ezp.setBoolean("canSALE",canSALE);
    ezp.setBoolean("canMOTO",canMOTO);
    ezp.setBoolean("canClose",canClose);
    ezp.setBoolean("canVOID",canVOID);
    ezp.setBoolean("canREFUND",canREFUND);
  }

  public EasyCursor loadfrom(String key, EasyCursor ezp){
    ezp.push(key);
    load(ezp);
    return ezp.pop();
  }

  public void load(EasyCursor ezp){
    canSALE=    ezp.getBoolean("canSALE",true);
    canMOTO=    ezp.getBoolean("canMOTO",false);
    canClose=   ezp.getBoolean("canClose",false);
    canVOID=    ezp.getBoolean("canVOID",true);
    canREFUND=  ezp.getBoolean("canREFUND",false);
  }

  public ClerkPrivileges() {
//see declarations for defaults.
  }

}
//$Id: ClerkPrivileges.java,v 1.4 2001/10/10 22:47:54 andyh Exp $