package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ClerkRegistry.java,v $
 * Description:  cached encrypted list of clerk and priveleges.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Revision: 1.4 $
 */

import net.paymate.util.*;
import net.paymate.connection.*; //clerkPrivielege
import java.util.*;
import net.paymate.data.*;

public class ClerkRegistry extends Hashtable implements isEasy {

  public Clerk add(Clerk clerk){
    super.put(clerk.idInfo.fastHash(),clerk.priv);
    return clerk;
  }

  private ClerkPrivileges lookup(ClerkIdInfo clerk){
    return (ClerkPrivileges)super.get(clerk.fastHash());
  }

  public Clerk check(ClerkIdInfo clerk){
    Clerk okclerk=new Clerk();
    okclerk.set(clerk);
    okclerk.set(lookup(clerk));
    return okclerk;
  }
  ///////////////////////
  /**
   * @param ezp **must** be a root list.
   */
  public void save(EasyCursor ezp){
//    for(Enumeration list=super.keys();list.hasMoreElements();){
//      Integer hasher = (Integer)list.nextElement();
//      ezp.addBlock(logins.getSt(hasher),String.valueOf(hasher));
//    }
  }

  public void load(EasyCursor ezp){
//    for(Enumeration list=ezp.propertyNames();list.hasMoreElements();){
//      Integer hasher = new Integer((String)list.nextElement());
//      ezp.getBlock(new Clerk(),String.valueOf(hasher));
//    }
  }

  public ClerkRegistry() {
  //is empty
  }

}