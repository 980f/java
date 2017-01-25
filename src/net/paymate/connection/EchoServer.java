package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/EchoServer.java,v $
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author       PayMate.net
* @version $Id: EchoServer.java,v 1.6 2001/10/11 11:36:52 andyh Exp $
*/
import java.net.*;
import java.io.*;

import net.paymate.util.ErrorLogStream;

public class EchoServer implements Runnable{
  static final ErrorLogStream dbg= new ErrorLogStream(EchoServer.class.getName());
  ServerSocket inSocket;

  void bind(int port){
    try {
      inSocket= new ServerSocket(port);
    } catch(Exception ex){
      dbg.Caught("While binding "+port,ex);
    }
  }

  protected void  core( InputStream incoming,OutputStream replyTo) throws IOException {
    int readPtr=0;
    StringBuffer oneline=new StringBuffer(2001);
    int ch=incoming.read();//blocks until data available or socket gets whacked
    if(ch<0){
      dbg.WARNING("Client Socket dropped");
      return;
    }
    oneline.append((char)ch);
    if (ch=='\n'){
      dbg.VERBOSE(oneline.toString());
      replyTo.write(oneline.toString().getBytes());
      oneline.setLength(0);//erase content
    }
  }

  public void run(){
    dbg.WARNING("Ready for Connections");
    while(true){
      try{
        Socket client=inSocket.accept();//blocks until a connection is made
        dbg.WARNING("Accepted Connection from:"+client.toString());
        OutputStream replyTo= client.getOutputStream();
        InputStream incoming= client.getInputStream();
        while(true){//breaks on exceptions
          try {
            core(incoming,replyTo);
          }
          catch(IOException ioe){
            dbg.Caught("While reading got:",ioe);
            break;
          }
        }
        client.close();
      } catch(Throwable ex){
        dbg.Caught("while running got ",ex);
      }
    }//wait for another connect.
  }

  Thread background;
  public EchoServer(int port) {
    bind(port);
    background=new Thread(this);
    background.run();
  }

  public void Destroy(){
    background.destroy();
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
//$Id: EchoServer.java,v 1.6 2001/10/11 11:36:52 andyh Exp $
