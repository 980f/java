package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/ClockClient.java,v $
* Description:
* Copyright:    Copyright (c) 2000,2001
* Company:      PayMate.net
* @author $Author: mattm $
* @version $Revision: 1.10 $
*/

import java.net.Socket;
import java.io.InputStream;
import net.paymate.util.*;
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;

public class ClockClient implements Runnable {
  Thread periodic=null;
  //+_+ use IpSpec.
  int port=3942;//2nd generation server
  String hostname="clkserver";

  Socket server;
  InputStream is;
  long forAwhile=Ticks.forDays(1);

  boolean killed=false;
  public void Stop(){
    killed=true;
    periodic.interrupt();
  }

  /**
  * @return true if a sincere attempt was made.
  */
  public boolean UpdateNow(){
    if(periodic!=null){
      periodic.interrupt(); //break it out of sleep
      ThreadX.sleepFor(Ticks.forSeconds(30));//+++ do wait and notify with demon.
      return true;
    } else {
      return UpdateOurClock();
    }
  }


  /**
  * @return true if a sincere attempt was made.
  */
  private boolean UpdateOurClock(){
    try {

      server=new Socket(hostname,port);
      is=server.getInputStream();
      byte [] image=new byte[20];
      if(is.read(image)>0){//+_+ trusts integral reception of packet, malformed packets will do wierd things here.
        long time=StringX.parseLong(new String(image));
        if(time>0){
          //          DateX.setSystemClock(time);
          return true;
        }
      }

      return false;

    }
    catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
  * read time then hang for a while
  */
  public void run(){

    try {
      while(!killed){
        UpdateOurClock();
        ThreadX.sleepFor(forAwhile);
      }
    } catch(Exception any){
      //dbg.Caught(any);
    }
    //only get here on serious fault, or explcit kill request
  }

  public ClockClient(int port, boolean asDemon) {
    if(port!=0){
      this.port = port;
    }
    if(asDemon){
      periodic=new Thread(this, "ClockClientDaemon:"+port);
      periodic.start();
    }
  }

}
//$Id: ClockClient.java,v 1.10 2003/09/25 02:06:10 mattm Exp $
