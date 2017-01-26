package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/PacketService.java,v $
 * Description:  abstract packetized communications service, needed by a PacketServer
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.data.*;

public interface PacketService {
  /**
   * called when a good packet is received
   * @--return a Byte, byte [], or Buffer to be sent to client, null to send nothing
   * @ changed at some point to send-response-yourself requiring that the packetService retain the sending object.
   */
  public void onPacket(Buffer packet);

  /**
   * @return whether to restart receiver state machine
   */
  public boolean onControlEvent(int controlevent);

  /**
   * called when line is established, usefull for init of internal logic
   * @return same as onPacket()
   */
  public void onConnect();

}
//$Id: PacketService.java,v 1.4 2003/07/24 16:47:34 andyh Exp $