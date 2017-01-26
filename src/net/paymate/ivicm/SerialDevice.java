package net.paymate.ivicm;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/SerialDevice.java,v $
 * Description:  stuff common to ivicm devices
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: SerialDevice.java,v 1.27 2003/07/27 05:35:03 mattm Exp $
 */

//import javax.comm.*;
import net.paymate.ivicm.comm.*;
import net.paymate.util.timer.*;
import net.paymate.lang.ThreadX;
import net.paymate.serial.*;
import net.paymate.util.*;
import net.paymate.jpos.data.*;
import net.paymate.*; //tester
import net.paymate.lang.ReflectX;

import java.io.*;//reader for debug

abstract public class SerialDevice extends Receiver /*implements SerialPortEventListener*/ {
  protected String myName;
  protected SerialConnection myPort;


/**
 * process any bytes present until receiver has something complete
 * @synch lock out other responsible port users.
 */
  protected boolean suckInput(ErrorLogStream rcvr){
    synchronized (this) {//lock respectful others out of our onByte() routine.
      int nexttimeout= Receiver.TimeoutNever;
      while(myPort.available()>0){//got bytes, refetch available every pass of the loop.
        nexttimeout= onByte(myPort.ezRead());
        rcvr.VERBOSE("onByte returned:"+nexttimeout);
        if(nexttimeout==Receiver.TimeoutNever){
          return true;//packet done.
        }
        //@todo: should deal with other event codes.
      }
      return false;
    }
  }


  /**
   * @return class.instance
   */
  public String toString(){
    return ReflectX.shortClassName(this,myName);
  }

  public SerialDevice(String id) {
    myName=id;
    myPort=null;
  }

  boolean haveConnected(){
    return myPort!= null && myPort.isOpen();
  }

  abstract protected void onConnect();//called when serial connection opens

  /**
   * allow for demon activity on reconnect.
   */
  public void reConnect(){
    Connect(myPort);
  }

  public void disConnect(){
    myPort.closeConnection();
  }

  public void Connect(SerialConnection sc){
    Problem prob=null;
    if(haveConnected()){
      disConnect();//from previous connection
    }
    myPort=sc;
    if(myPort!=null){
      prob=myPort.openConnection(this);//'this' will be of an extended class.
      if(prob==null&&myPort.isOpen()){
        onConnect();
      }
    }
//    return prob;
  }

/**
 * for extended device module tests
 * defaults are set for CM3000, don't lean on them :)
 */

  abstract public void Display(String forDisplay);
//  {
//    System.err.println("SerialDeviceBase:"+forDisplay);
//  }

  public static void testerMain(Class extended){
    Main app=new Main(extended);
    LogSwitch.SetAll(LogSwitch.ERROR);  //@in a tester
    PrintFork.SetAll(LogSwitch.VERBOSE);//@in a tester
    app.stdStart(null); //starts logging etc. merges argv with system.properties and thisclass.properties
//LogSwitch.apply(fordebug);
  }

  public TextListIterator testerConnect(TextListIterator args,int defbaud,ErrorLogStream dbg){/* so that service users can make test devices*/
    testerMain(SerialDevice.class);//legacy
    Connect(SerialConnection.fortesters(args,defbaud,dbg));
    dbg.VERBOSE("sc:"+this.myPort.toSpam());
    return args;
  }

  public TextListIterator testerConnect(String []args,int defbaud,ErrorLogStream dbg){/* so that service users can make test devices*/
    return testerConnect(TextListIterator.New(args),defbaud,dbg);
  }


  public void testloop(ErrorLogStream dbg){/* so that service users can invoke test on devices*/
//    PeriodicGC showmetheproblem= PeriodicGC.Every(Ticks.forSeconds(5));
//    showmetheproblem.enabled(false); //true to detect memory leaks

    BufferedReader inline=new BufferedReader(new InputStreamReader(System.in));

    String typed="No input yet";
    while(true){
      try {
        typed= inline.readLine();
        if(typed.startsWith("do:")){
          typed=typed.substring("do:".length());
          for(int rep=1000000;rep-->0;){
            Display(typed+" "+rep+"    ");
            ThreadX.sleepFor(0.5);
          }
        } else if(typed.startsWith("burp")){
          Display("reconnecting");
//          disConnect();
          reConnect();
          Display("reconnected");
        }
        else {
          Display(typed);
        }
      }
      catch (Exception ex) {
        dbg.ERROR(ex.getMessage());
        continue;
      }
    }
  }

  public EasyCursor testConnect2(int defbaud,ErrorLogStream dbg){/* so that service users can make test devices*/
    testerMain(this.getClass());
    EasyCursor cfg=Main.props();
    Connect(SerialConnection.makeConnection(cfg,defbaud));
//    dbg.VERBOSE("sc:"+this.myPort.toSpam());
    return cfg;
  }


}
//$Id: SerialDevice.java,v 1.27 2003/07/27 05:35:03 mattm Exp $