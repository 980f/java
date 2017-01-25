package net.paymate.authorizer.cardSystems;

import net.paymate.authorizer.ResponseNotificationEvent;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CSResponseNotificationEvent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class CSResponseNotificationEvent extends ResponseNotificationEvent {

  InBlockedSocket socketeer = null;

  public CSResponseNotificationEvent(InBlockedSocket socketeer) {
    this.socketeer = socketeer;
  }
}