/**
 * Title:        ConnectionCallback<p>
 * Description:  Class to receive a callback from the connection
 *               when a thread is completed (socket closed).<p>
 *               This one is used for testing and pinging on the ConnectionClient<p>
 *               Extend it to make a more complex one.
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: ConnectionCallback.java,v 1.12 2004/02/23 17:47:23 andyh Exp $
 */

package net.paymate.connection;

public interface ConnectionCallback {
  public void ActionReplyReceipt(Action action);
  public void extendTimeout(int millis);//ConnectionCallback interface
}
//$Id: ConnectionCallback.java,v 1.12 2004/02/23 17:47:23 andyh Exp $