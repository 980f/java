package net.paymate.connection;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: StoodinReply.java,v 1.7 2002/07/09 17:51:22 mattm Exp $
 */

import  net.paymate.util.*;

public class StoodinReply extends AdminReply implements isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoodinReply.class);

  public ActionType Type(){
    return new ActionType(ActionType.stoodin);
  }
  /**
   * the local record key is sent by the client and returned by the server to
   * allow the request and reply to run open-loop at the socket level. I.e.
   * requests are made without regard to whether replies have been received.
   */
  String recordID;

  public StoodinReply(String passback) {
    recordID=passback;
    status.setto(ActionReplyStatus.ServerError) ;
  }

  public StoodinReply() {
    recordID="";
// for ActionReply.fromProperties
  }

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setString(StoodinRequest.recordIDKey, recordID);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    recordID = ezp.getString(StoodinRequest.recordIDKey);
  }
}
//$Id: StoodinReply.java,v 1.7 2002/07/09 17:51:22 mattm Exp $