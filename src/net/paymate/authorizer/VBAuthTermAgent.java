package net.paymate.authorizer;

import net.paymate.data.*;
import net.paymate.net.*;
import net.paymate.util.Counter;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/VBAuthTermAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

public class VBAuthTermAgent extends AuthTerminalAgent {

  int bufferSize = 0;

  public VBAuthTermAgent(Authorizer authorizer, Terminalid term, int bufferSize,
                         Counter sequencer, Counter termbatchnumer,
                         int fgThreadPriority, int bgThreadPriority) {
    super(authorizer, term, sequencer, termbatchnumer, fgThreadPriority, bgThreadPriority);
    this.bufferSize = bufferSize;
  }

  public AuthSocketAgent makeSocketAgent() {
    return new AuthSocketAgent(VisaBuffer.NewReceiver(bufferSize).setClipLRC(), authorizer);
  }

}
