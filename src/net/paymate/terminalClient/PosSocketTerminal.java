package net.paymate.terminalClient;

/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocketTerminal.java,v $
* Description:  Socket interface for an external terminal.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.43 $
*/

import net.paymate.*;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.terminalClient.PosSocket.*;

public class PosSocketTerminal extends ExternalTerminal  {
  SocketLineServer listen2fiona=null;  //the socket listener

  int port;
  int lineIdleTimeoutMs;
  public PosSocketTerminal(TerminalInfo termInfo) {////public for load by reflection, uses config from server
    super(termInfo);
    port=termInfo.equipmentlist.getInt("port2serve");
    lineIdleTimeoutMs = termInfo.equipmentlist.getInt("idleTimeoutMs",LineServer.NOLINEIDLETIMEOUT);
  }
//  public PosSocketTerminal() {////public for load by reflection, uses config from server
//  //
//  }
  public void Start(EasyCursor ezp){//called once, local config tweaks
    super.Start(ezp);
    Start("PosSocket."+port);
    listen2fiona=SocketLineServer.Serve(port,(LineServerUser) this, true, lineIdleTimeoutMs);
  }

  protected void fancyExit(String why){
    listen2fiona.Stop();
    super.fancyExit(why);
  }

}
/*
for multiple terminals we need either multiple PosSocketTerminals and some external rule
for correlating them to ports, or make an array of all PosSocketTerminals and match an
incoming terminalId.
Until we get a terminal id in the incoming message we will stick with one
PosSocketTerminal per terminal and with fiona figuring out the terminalid<->ip port relationship.

When we do have terminalid info available we will make a PosSocket that contains
multiple PosSocketTerminals, parses the message just enough to figure out which terminal
to send it to. Each PosSocketTerminal will register itself with the PosSOcket upon creation.

*/
//$Id: PosSocketTerminal.java,v 1.43 2003/12/12 18:16:13 mattm Exp $
