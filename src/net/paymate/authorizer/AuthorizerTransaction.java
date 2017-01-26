package net.paymate.authorizer;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/AuthorizerTransaction.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.net.MultiHomedHost;
import net.paymate.util.Counter;

public class AuthorizerTransaction {
  public MultiHomedHost host = null;
  public Counter socketOpenAttempts = new Counter();
}