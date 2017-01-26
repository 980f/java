package net.paymate.authorizer.linkpoint;

import net.paymate.data.*;
import net.paymate.net.*;
import net.paymate.authorizer.*;
import net.paymate.util.Counter;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LPAuthTermAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

public class LPAuthTermAgent extends AuthTerminalAgent {

  int bufferSize = 0;

  public LPAuthTermAgent(Authorizer authorizer, Terminalid term, int bufferSize,
                         Counter sequencer, Counter termbatchnumer,
                         int fgtxnpriority, int bgtxnpriority) {
    super(authorizer, term, sequencer, termbatchnumer, fgtxnpriority,
          bgtxnpriority);
    this.bufferSize = bufferSize;
  }

  public AuthSocketAgent makeSocketAgent() {
    return new LPAuthSocketAgent(new LPResponsePacket(bufferSize), authorizer);
  }

}
