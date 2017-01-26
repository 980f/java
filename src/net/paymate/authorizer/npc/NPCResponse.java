package net.paymate.authorizer.npc;

import net.paymate.util.*;
import net.paymate.authorizer.*;
import net.paymate.data.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */


public class NPCResponse extends AuthResponse {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NPCResponse.class);

  public NPCResponse() {
    //see initializers.
  }

  protected NPCResponse parse(VisaBuffer vb){
    dbg.VERBOSE("parsing response");
    action= ActionCode.Failed;
    authmsg="Auth does not auth";
    return this;
  }

  public void process(Packet toFinish){
    parse(null);
  }

}
