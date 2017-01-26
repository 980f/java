package net.paymate.authorizer;

import java.util.Vector;
import net.paymate.data.*; // Terminalid
import net.paymate.awtx.*; // Realmoney

import net.paymate.util.*; // Accumulator

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthTermAgentList.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

public class AuthTermAgentList extends Vector {

  public AuthTermAgentList() {
  }

  public synchronized AuthTerminalAgent agentForTerminal(Terminalid term) {
    if(Terminalid.isValid(term)) {
      for(int i = size(); i-->0;) {
        AuthTerminalAgent agent = itemAt(i);
        if(term.equals(agent.myTerminalid)) {
          return agent;
        }
      }
    }
    return null;
  }

  private synchronized AuthTerminalAgent itemAt(int index) {
    AuthTerminalAgent agent = null;
    Object o = null;
    try {
      o = get(index);
    } catch (Exception e) {
      // swallow
    }
    if((o != null) && /* unnecessary-> */ (o instanceof AuthTerminalAgent)) {
      agent = (AuthTerminalAgent)o;
    }
    return agent;
  }

  public synchronized int removeAny(Terminalid termid, Txnid txnid, RealMoney cents) {
    AuthTerminalAgent agent = agentForTerminal(termid);
    if(agent != null) {
      return agent.removeAny(txnid, cents);
    }
    return 0;
  }

  public synchronized Accumulator queuedTxns() {
    Accumulator acc = new Accumulator();
    // this guy never shrinks
    for(int i = 0; i < size(); i++) {
      AuthTerminalAgent agent = itemAt(i);
      if(agent!=null) {
        acc.add(agent.centsQueued());
      }
    }
    return acc;
  }

  public synchronized void Stop() {
    for(int i = 0; i < size(); i++) {
      AuthTerminalAgent agent = itemAt(i);
      if(agent!=null) {
        agent.stop();
      }
    }
    // empty the list!  Since you can't ever restart them, must dispose of them.
    setSize(0);
  }

}
