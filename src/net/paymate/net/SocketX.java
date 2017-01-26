package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/SocketX.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.net.*;
import net.paymate.util.ErrorLogStream;

public class SocketX {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SocketX.class);

  private SocketX() {
    // I exist for static purposes only
  }
  /**
   * @return true if socket closes Ok, or didn't need to.
   */
  public static final boolean Close(Socket sock){
    if(sock != null) {
      try {
        sock.getOutputStream().flush(); //to make this like C
        //how about flushing input also???
        sock.close();
      }
      catch(java.net.SocketException ignore){
        return ignore.getMessage().endsWith("Socket is closed");//--- could fail with any jre upgrade.
      }
      catch (Exception tfos) {
        dbg.Caught("Close(Socket):",tfos);
        return false;
      }
    }
    return true;
  }
  /**
   * @return true if socket closes Ok, or didn't need to.
   */
  public static final boolean Close(ServerSocket sock){
    if(sock != null) {
      try {
        sock.close();
      }
      catch(java.net.SocketException ignore){
        return ignore.getMessage().endsWith("Socket is closed");//--- could fail with any jre upgrade.
      }
      catch (Exception tfos) {
        dbg.Caught("Close(ServerSocket):", tfos);
        return false;
      }
    }
    return true;
  }


}