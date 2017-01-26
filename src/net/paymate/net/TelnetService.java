package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/TelnetService.java,v $
 * Description:  A single-socket telnet service (only one person can connect at a time)
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;
import java.io.*;
import java.net.*;
import net.paymate.lang.StringX;

public class TelnetService extends Server implements Runnable {
  ErrorLogStream dbg;

  String shellname;

  int port;
  ServerSocket mySocket;

  /**
   * @returns whether Started fault free.
   */
  public boolean Start(){
    dbg.WARNING("Starting shell at:"+port);
    try {
      dbg=ErrorLogStream.getForClass(this.getClass());
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
        //start a child process
        int exitcode=-1;// a valu enever returned by a real process
        try {
          Process process = Runtime.getRuntime().exec(StringX.OnTrivial(shellname,"bash"));
          //@@@needs to connect streams here @@@
//    in  = new BufferedReader(new InputStreamReader(process.getInputStream()));
//    err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//    out = new PrintStream(process.getOutputStream());
          boolean done=false;
          while(!killed&&process!=null){
            try {
              process.exitValue();  // throws if process is not complete
              done = true;
            } catch (IllegalThreadStateException itse) {
//              not done yet
            }
          }
          if(!done){
            process.destroy();
          }

        }
        catch (Exception ex) {
          dbg.Caught(ex);
        }
        finally {
          dbg.WARNING("process terminated:"+exitcode);
        }

      }
      catch (Exception ex) {

      }
      finally {

      }
    }
  }

  public TelnetService(int port) {
    super(StringX.bracketed("telnet(",String.valueOf(port)),true);
    this.port=port;
    dbg=ErrorLogStream.getForClass(this.getClass());
  }

}
//$Id: TelnetService.java,v 1.3 2003/07/27 05:35:12 mattm Exp $