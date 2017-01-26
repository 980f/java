package net.paymate.authorizer.linkpoint;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LinkpointSettlementRequest.java,v $
 * Description:  Settlement messaging spec for NPC per "NPC Point of Sale Batch Upload Specs" doc dated July, 2001
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * +++ TODO:     Finish the debit parts -- LATER!
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.authorizer.*;
import net.paymate.data.*;
import net.paymate.util.*; // Safe

public class LinkpointSettlementRequest extends AuthSubmitRequest implements LPConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LinkpointSettlementRequest.class);

  public LinkpointSettlementRequest(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    super(authid, terminalid, merch);
  }

  /* package */ void detail() { // this is just used to add up the amounts
    amounts.add( records.netSettleAmountCents());
  }

  // filler to make concrete
  protected int maxRequestSize() {
    return 0;
  }
}
