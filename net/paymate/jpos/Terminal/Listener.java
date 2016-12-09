/**
* Title:        Listener
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Listener.java,v 1.3 2001/03/29 19:16:11 andyh Exp $
*/

package net.paymate.jpos.Terminal;

import jpos.JposException;

public interface Listener {
  public void Handle(Event jte);
  public void Handle(jpos.JposException jte);
  public void Handle(jpos.events.ErrorEvent jape);
}
//$Id: Listener.java,v 1.3 2001/03/29 19:16:11 andyh Exp $
