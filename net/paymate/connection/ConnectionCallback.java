/**
 * Title:        ConnectionCallback<p>
 * Description:  Class to receive a callback from the connection
 *               when a thread is completed (socket closed).<p>
 *               This one is used for testing and pinging on the ConnectionClient<p>
 *               Extend it to make a more complex one.
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: ConnectionCallback.java,v 1.9 2001/10/17 22:07:22 andyh Exp $
 */

package net.paymate.connection;
import  net.paymate.util.ErrorLogStream;

public interface ConnectionCallback {
  void ActionReplyReceipt(Action action);
}
//$Id: ConnectionCallback.java,v 1.9 2001/10/17 22:07:22 andyh Exp $