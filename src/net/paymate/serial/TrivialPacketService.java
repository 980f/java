package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/TrivialPacketService.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.data.*;

public class TrivialPacketService implements PacketService {
  protected ErrorLogStream dbg;
  final static int bufsize=100;
  protected Port        port;
  protected Packetizer  response= Packetizer.Ascii(bufsize);
  protected PacketReceiver receiver;
  protected PacketServer server;

  public void load(EasyCursor ezc){
    dbg.VERBOSE(ezc.asParagraph(Ascii.CRLF));
    port = PortProvider.openPort("trivial",ezc);
    server = PacketServer.ServeVisaBasic(this, bufsize, port);
  }

  public boolean onControlEvent(int controlevent){
    dbg.VERBOSE("controlevent:"+Receiver.imageOf(controlevent));
    return false;
  }

  public void onPacket(Buffer packet){
    dbg.VERBOSE("body:"+Ascii.image(packet.packet()));
  }

  public void onConnect(){
    dbg.VERBOSE("onConnect() not overloaded");
  }

  public TrivialPacketService() {
    dbg = ErrorLogStream.getForClass(this.getClass());
  }

}
//$Id: TrivialPacketService.java,v 1.5 2003/07/24 16:47:35 andyh Exp $