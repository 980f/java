package net.paymate.authorizer.linkpoint;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LPResponsePacket.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.Packet;
import net.paymate.util.*; // Safe
import net.paymate.lang.ObjectX;

public class LPResponsePacket extends Packet {

  public LPResponsePacket(int maxsize) {
    super(maxsize);
  }

  public boolean isComplete() {
    return (new String(this.buffer)).indexOf("</response>") > ObjectX.INVALIDINDEX;
  }

  public boolean isOk() {
    return isComplete();
  }

  public byte [] packet(){
    return buffer;
  }
}