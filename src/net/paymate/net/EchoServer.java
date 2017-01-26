package net.paymate.net;

/**
* Title:        $Source: /cvs/src/net/paymate/net/EchoServer.java,v $
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author       PayMate.net
* @version $Id: EchoServer.java,v 1.3 2004/01/09 11:46:06 mattm Exp $
*/
import java.net.*;
import java.io.*;

import net.paymate.util.*;
import net.paymate.net.*;
public class EchoServer extends SocketLineServer implements LineServerUser {
  static final ErrorLogStream dbg= ErrorLogStream.getForClass(EchoServer.class);

  private EchoServer(int port) {
    super("EchoServer"+port, port, null, false, NOLINEIDLETIMEOUT);
    super.bind(port, this);
  }

  public byte[] onReception(byte[] line){
    return line;
  }

  public byte [] onConnect(){
    return "UR Connected to EchoServer $Rev$".getBytes();
  }
  public boolean onePerConnect(){
    return false;
  }


  public static  void main(String argv[]){
    try {
      EchoServer echoer=new EchoServer(Integer.parseInt(argv[0]));
      //and its runnable should keep itself alive.
    } catch(Exception shit){
      //appease the compiler
    }
  }

}
//$Id: EchoServer.java,v 1.3 2004/01/09 11:46:06 mattm Exp $
