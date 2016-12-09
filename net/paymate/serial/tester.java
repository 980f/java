package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/tester.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import net.paymate.*;

public class tester extends Receiver{
  static final Tracer dbg=new Tracer("serialtester");

  static final void pout(String s){
    dbg.VERBOSE(s);
  }

  Stream stream; //what we are testing...

  /**
   * implements Receiver:
   */
  public synchronized int onByte(int b){
    if(b>0){
      System.out.write(b);//echo comm
    } else {
      System.out.println("[eventcode "+b+"]");
    }
    return Receiver.TimeoutNever;
  }


  void runtest(String portname, boolean localecho){
      stream=new Stream(this);//set reception pathway
      Port notrxtx;

      if(portname.startsWith("/dev")){
        Parameters sp=new Parameters(portname);
        sp.setBaudRate(9600);
        sp.setParity("None");
        sp.setDatabits(8);
        sp.setStopbits(1);
        notrxtx=PortProvider.makeJavaxPort(sp);
      } else {
        notrxtx=PortProvider.makeFilePort(portname);
      }
      //if the above fails we have a virtual LoopBack
      pout("Constructed a "+notrxtx.getClass().getName()+" named:"+notrxtx.nickName());

      stream.Attach(notrxtx); //get to the system streams
      pout("Attached...");
      stream.startReception(TimeoutNever);//allow reception

      try {
        int c;
        while((c=System.in.read())!=-1){//blocking read from console
          if(localecho){
            System.out.write(c);
          }
          stream.write(c);
        }
      } catch(Exception t){
        System.out.println("Exception on keybd->commport:"+t);
      }
  }

  public static final void main(String[] args) {
    try {
      pout("args:"+TextList.CreateFrom(args).asParagraph());
      String portname=   (args.length>0)?args[0]:"/dev/ttyS0";
      boolean localecho= (args.length>1)?Boolean.valueOf(args[1]).booleanValue():true;
      pout("port:"+portname+" local echo:"+localecho);

      Main app=new Main(tester.class);
      app.stdStart(args);//includes processing logcontrol.properties

      LogSwitch.SetAll(LogSwitch.VERBOSE);
      PrintFork.SetAll(LogSwitch.VERBOSE);

      pout("port:"+portname+" local echo:"+localecho);

      tester javaisapain=new tester();
      javaisapain.runtest(portname,localecho);
    } catch(Throwable t) {
      System.out.println(t);
    }

  }
}
//$Id: tester.java,v 1.6 2001/09/14 21:10:39 andyh Exp $