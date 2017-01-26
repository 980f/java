package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/MuteServer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class MuteServer implements LineServerUser {
  LineServer ls;

  public byte[] onReception(byte[] line){//return response to "line"
    return NullResponse;    //never reply, but keep socket open
  }

  public byte [] onConnect(){//return something to send when a connection has been made
    System.out.println("got connection");
    return NullResponse;
  }
  public boolean onePerConnect(){
    return false;
  }

  public MuteServer(int bindto, boolean isDaemon) {
    ls=SocketLineServer.Serve(bindto, this, isDaemon, SocketLineServer.NOLINEIDLETIMEOUT);
  }

//////////////////////
  public static void main(String[] args) {
    if(args.length<1){
      System.exit(1);
    }
    try {
      LogSwitch.SetAll(LogSwitch.ERROR);
      MuteServer ms = new MuteServer(StringX.parseInt(args[0]), false);
    } catch (Exception ex) {
      System.exit(0);
    }
  }
}
// $Id: MuteServer.java,v 1.8 2004/01/09 11:46:06 mattm Exp $