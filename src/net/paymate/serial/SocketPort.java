package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/SocketPort.java,v $
 * Description:  a port implemented with a socket.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 * @todo: add remote port protocol!
 */

import net.paymate.net.*;
import java.net.*;
import net.paymate.util.*;

public class SocketPort extends Port {
  static final ErrorLogStream dbg= ErrorLogStream.getForClass(SocketPort.class);
  Socket sock;
  boolean serveit;
  IPSpec ip;

//  /**
//   * use given socket, make local ip match as well as we can
//   */
//  public Port attachTo(Socket sock){
//    SocketX.Close(sock);//politely close current one
//    this.sock=sock;
//    if(ip==null){
//      ip=new IPSpec();
//    }
//    ip.set(sock.getInetAddress().getHostAddress(),sock.getPort());
//    return this;
//  }

  /**
   * open a socket using port name as hostname:port.
   */
  public boolean openas(Parameters parms){

    SocketX.Close(sock);//politely close current one
    try {
      if(ip.isService()){
//        sock = new ServerSocket(ip.port);
        //someday will accept() and take in remote serial params open up a serial port and attach to its streams
      } else {
        sock = new Socket(ip.address,ip.port);
        //socket constructor actually connects and gives us open streams
       //someday will send parms down the socket when we get a connect
      }
      return true;
    } catch( java.io.IOException ioex){
      dbg.Caught("openas"+Ascii.bracket(ip.toString()),ioex);
      return false;
    }
  }

  public SocketPort(IPSpec ip) {
    super(ip!=null? ip.toString(): "SocketPort");
    this.ip=ip;
    //defer socket creation 'til openas is called.
  }

}
//$Id: SocketPort.java,v 1.5 2003/07/27 05:35:14 mattm Exp $