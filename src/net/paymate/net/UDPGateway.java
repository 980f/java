package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/UDPGateway.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;
import net.paymate.*;
import java.net.*;         // DatagramPacket

public class UDPGateway extends UDPServer {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(UDPGateway.class);

  public UDPGateway(String name, boolean isDaemon, IPSpec source, IPSpec destination) {
    super(source.port, 500, name, isDaemon, source.address);
    this.destination = destination;
    this.source = source;
  }

  private IPSpec destination;
  private IPSpec source;

  protected void handlePacket(DatagramPacket oldp) {
    dbg.WARNING("Actually received this (before conversion): " + Ascii.bracket(oldp.getData()));
    // first, see if it needs any conversion ...
    oldp = convert(oldp);
    dbg.WARNING("Converted: " + Ascii.bracket(oldp.getData()));
    byte [] data = oldp.getData();
    if(data != null) {
      DatagramSocket sockd = null;
      try {
        sockd = new DatagramSocket();
      } catch (Exception e) {
        dbg.Caught("Attempting to create datagram socket:", e);
      }
      if(sockd != null) {
        try {
          InetAddress net;
          sockd.send(new DatagramPacket(data, data.length, InetAddress.getByName(destination.address), destination.port));
          dbg.WARNING("Sent '" + Ascii.bracket(data) + "' to " + destination + ".");
        } catch (Exception e) {
          dbg.Caught("Attempting to send StatusPacket datagram:", e);
        } finally {
          sockd.close();
        }
      }
    }
  }

  protected DatagramPacket convert(DatagramPacket oldp) {
    return oldp;
  }

}
