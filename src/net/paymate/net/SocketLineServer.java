package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/SocketLineServer.java,v $
 * Description:  pace lines from a socket
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import net.paymate.util.*;
import java.net.*;
import java.io.*;
import net.paymate.lang.StringX;

public class SocketLineServer extends LineServer {
 static final ErrorLogStream dbg= ErrorLogStream.getForClass(SocketLineServer.class);
  int port; //for deferred socket creation.
  ServerSocket mySocket;
  private LineServer config(int port,LineServerUser myUser){
    this.myUser=myUser;
    this.port=port;
    return this;
  }

  public boolean bind(int port,LineServerUser myUser){
    config(port,myUser);
    return Start();
  }

  public boolean Start(){
    try {
      mySocket= new ServerSocket(port);
      return super.Start();
    } catch(Exception ex){
      dbg.Caught("While binding "+port,ex);
      return false;
    }
  }

  public void run(){
    dbg.WARNING("Ready for Connections");
    while(!killed){
      try{
        Socket client=mySocket.accept();//blocks until a connection is made
        dbg.WARNING("Accepted Connection from:"+client);
        OutputStream replyTo= client.getOutputStream();
        InputStream incoming= client.getInputStream();
        byte [] onconnect=myUser.onConnect();
        if(ByteArray.NonTrivial(onconnect)){
          replyTo.write(onconnect);//+++ add a flush()
        }
        try {
          core(incoming,replyTo);
        }
        catch(Exception ioe){
          dbg.Caught("Core loop got:",ioe);
        }
        client.close();
      } catch(Throwable ex){
        dbg.Caught("while running got ",ex);
      }
    }//wait for another connect.
  }

  protected SocketLineServer(String name, int bindto, LineServerUser agent, boolean isDaemon, int lineIdleTimeoutMs) {
    super(name, isDaemon, lineIdleTimeoutMs);
    config(bindto,agent);
  }

//  public static SocketLineServer NewNoTimeout(int bindto, LineServerUser agent, boolean isDaemon){
//    return New(bindto, agent, isDaemon, NOLINEIDLETIMEOUT);
//  }
//
  public static SocketLineServer New(int bindto, LineServerUser agent, boolean isDaemon, int lineIdleTimeoutMs){
    return new SocketLineServer("SocketLineServer."+bindto, bindto, agent, isDaemon, lineIdleTimeoutMs);
  }

  public static SocketLineServer Serve(int bindto, LineServerUser agent, boolean isDaemon, int lineIdleTimeoutMs){
    SocketLineServer newone=SocketLineServer.New(bindto, agent, isDaemon, lineIdleTimeoutMs);
    newone.Start();
    return newone;
  }

}
//$Id: SocketLineServer.java,v 1.12 2003/12/03 03:32:11 andyh Exp $
