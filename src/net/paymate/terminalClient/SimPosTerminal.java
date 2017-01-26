package net.paymate.terminalClient;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/terminalClient/SimPosTerminal.java,v $
 * Description:  !!incomplete . simulate a posterminal. at present it is enough for interactive simulation.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.util.*;
import net.paymate.connection.*;
import net.paymate.lang.ReflectX;
import net.paymate.data.*;

public class SimPosTerminal extends PosTerminal {
  ErrorLogStream dbg;
  public final boolean Post(Object arf){//MAIN access point
    dbg.ERROR("post:"+ReflectX.shortClassName(arf));
    return super.Post(arf);
  }

  protected boolean sendRequest(String label,ActionRequest request){
    Action faked=Action.New(request);
    faked.setReply(ActionReply.For(request));

    switch (request.Type().Value()) {
      case ActionType.clerkLogin: {
        LoginReply ar= (LoginReply) faked.reply;
        ar.clerkCap.canClose=true;
        ar.clerkCap.canREFUND=true;
        ar.clerkCap.canSALE=true;
        ar.clerkCap.canVOID=true;
      } break;
      case ActionType.payment: {
        PaymentReply frep= (PaymentReply) faked.reply;
        frep.setAuth(AuthResponse.mkApproved("SimPos"));
      } break;
      default: {
        //won't happen
      } break;
    }
    super.runone(faked);
    return true;
  }

  public SimPosTerminal() {
    super(TerminalInfo.fake());
    dbg=ErrorLogStream.getForClass(this.getClass());
  }

  public static SimPosTerminal fake(){
    return new SimPosTerminal();
  }

}
//$Id: SimPosTerminal.java,v 1.10 2005/03/02 05:23:07 andyh Exp $