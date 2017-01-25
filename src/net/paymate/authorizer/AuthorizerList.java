package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthorizerList.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

// this class can probably be moved inside of AuthManager.java, and maybe even included in the class itself, or contain the functions and just use a vector.

import java.util.*;
import net.paymate.util.*;

public class AuthorizerList extends Vector {
  public AuthorizerList() {
  }
  /* can synchronize since the superclass does everywhere */
  public synchronized Authorizer findByName(String name) {
    Authorizer auth = null;
    for(int i = size(); i-->0;) {
      Authorizer temp = (Authorizer)elementAt(i);
      if(temp.name.equals(name)) {
        auth=temp;
        break;
      }
    }
    return auth;
  }
  public synchronized Authorizer findById(int id) {
    Authorizer auth = null;
    for(int i = size(); i-->0;) {
      Authorizer temp = (Authorizer)elementAt(i);
      if(temp.id == id) {
        auth=temp;
        break;
      }
    }
    return auth;
  }
  public synchronized Authorizer [] getArray() {
    Authorizer [] auths = new Authorizer[size()];
    for(int i = size(); i-->0;) {
      auths[i] = (Authorizer)elementAt(i);
    }
    return auths;
  }
}

