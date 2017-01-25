package net.paymate.connection;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: mattm $
* @version $Id: ClockClient.java,v 1.6 2001/10/30 19:37:20 mattm Exp $
*/

import java.net.Socket;
import java.io.InputStream;
import net.paymate.util.*;

public class ClockClient implements Runnable {
  Thread periodic;
  int port=3942;//2nd generation server
  String hostname="clkserver";
  Socket server;
  InputStream is;
  // +_+ this is too long!  make it shorter so that incremental time fixes can happen
  //... maybe once per hour. So as not to ever rollback very much
  long forAwhile=86400000L;//one-day in milliseconds.

  /**
  * read time then hang for a while
  */
  public void run(){
    byte [] image=new byte[20];
    try {
      server=new Socket(hostname,port);
      is=server.getInputStream();

      while(true){
        if(is.read(image)>0){
          long time=Safe.parseLong(new String(image));
          if(time>0){
            String pureclock=Long.toString(time);
            //          Executor.runProcess("hwclock -? "+pureclock,"Setting Hardware Clock");
          }
        }
        ThreadX.sleepFor(forAwhile);
      }
    } catch(Exception any){
      //dbg.Caught(any);
    }
    //only get here on serious fault
  }

  public ClockClient(int port) {
    if(port!=0){
      this.port = port;
    }
    periodic=new Thread(this);
    periodic.start();
  }

}
//$Id: ClockClient.java,v 1.6 2001/10/30 19:37:20 mattm Exp $
