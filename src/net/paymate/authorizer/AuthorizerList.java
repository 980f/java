package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthorizerList.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 * @todo define Authorizer.equals(Object) on  and replace teh iterations below with Vector.indexOf().
 */

// this class can probably be moved inside of AuthManager.java, and maybe even included in the class itself, or contain the functions and just use a vector.

import java.util.*;
import net.paymate.util.*;
import net.paymate.data.*; // Authid

public class AuthorizerList extends Vector {
  public AuthorizerList() {
  //
  }

  /* can synchronize since the superclass does everywhere */
  private synchronized Authorizer findBy(Object o) {
//doesn't work because stupid implementation of vector does 'equals()' in wrong order    int i=indexOf(o);
//and we rely upon that order elsewhere!!!! it doesn't matter that we rely upon it elsewhere,since we can't change that feature without rewriting the class.
    int i = size();
    while(i-->0) {
      if(((Authorizer)elementAt(i)).equals(o)) {
        return (Authorizer)elementAt(i);
      }
    }
    return null;
  }

  public Authorizer findByName(String name) {
    return findBy(name);
  }

  public Authorizer findById(Authid id) {
    return findBy(id);
  }

  public synchronized Authorizer [] getArray() {
    Authorizer [] auths = new Authorizer[size()];
    for(int i = size(); i-->0;) {//+_+ System.arrayCopy...
      auths[i] = (Authorizer)elementAt(i);
    }
    return auths;
  }

}
//$Id: AuthorizerList.java,v 1.10 2002/06/13 06:16:29 mattm Exp $