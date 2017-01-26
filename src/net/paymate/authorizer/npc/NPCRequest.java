package net.paymate.authorizer.npc;

import net.paymate.authorizer.*; // AuthRequest
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.*; // ErrorLogStream
import net.paymate.data.*; // MerchantInfo

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

public class NPCRequest extends AuthRequest implements NPCConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NPCRequest.class);

  private final VisaBuffer makeRequest() {
    return null;
  }

  public NPCRequest() {
    // empty
  }

  public byte [] toBytes() {
    return null;
  }

  protected int maxRequestSize() {
    return 0;
  }

  public int compareTo(Object o) {
    // nothing special to compare to. let the super handle it
    return 0;
  }

  public AuthRequest fromRequest(TxnRow tjr, TxnRow original, MerchantInfo merch) {
    return this;
  }

  public TextList toSpam(){
    return null;
  }

}
//$Id: NPCRequest.java,v 1.4 2003/10/25 20:34:16 mattm Exp $
