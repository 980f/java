package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/tester.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.18 $
 */

import net.paymate.util.*;
import net.paymate.*;
import net.paymate.lang.StringX;

public class tester extends Receiver{
  static final Tracer dbg=new Tracer(tester.class);
  Stream stream; //what we are testing...

  boolean emitCR=true;

  static final void pout(String s){
    dbg.VERBOSE(s);
  }

  /**
   * implements Receiver:
   */
  public synchronized int onByte(int b){
    System.out.println(Receiver.imageOf(b));
    if(b==Ascii.CR || b == Ascii.LF){
      System.out.print((char)b);
    }
    return Receiver.TimeoutNever;
  }

//////////////////////////////////////////
// the below may have been broken, don't trust it.
  void runtest(Parameters port, boolean localecho){
    stream=new Stream(this);//set reception pathway
    Port notrxtx;

    notrxtx=PortProvider.makePort(port.getPortName());
    if(!notrxtx.openas(port)){
      pout("port didn't open, bailing");
      return;
    }
    pout("Constructed:"+notrxtx.nickName());

    stream.Attach(notrxtx); //get to the system streams
    pout("Attached...");
    stream.startReception(TimeoutNever);//allow reception

    try {
      int c;
      while((c=System.in.read())!=Receiver.EndOfInput){//blocking read from console
        if(localecho){
          System.out.write(c);
        }
        if(emitCR && c == Ascii.LF){
          stream.write(Ascii.CR);
        }
        stream.write(c);
      }
    } catch(Exception t){
      System.out.println("Exception on keybd->commport:"+t);
    }
  }

  private static void tty(String[] args){
    TextListIterator arg=TextListIterator.New(args);
    Parameters sp= Parameters.CommandLine(arg,9600,"N81");
    boolean localecho= StringX.OnTrivial(arg.next(),true);
    pout("port:"+sp.portName+" local echo:"+localecho);
//      boolean emitCR=true;
//      sp.setStopbits(1);
    Main app=new Main(tester.class);

    LogSwitch.SetAll(LogSwitch.ERROR);
    PrintFork.SetAll(LogSwitch.VERBOSE);
    app.stdStart(args);//includes processing logcontrol.properties

    tester javaisapain=new tester();
    javaisapain.emitCR=true;
    javaisapain.runtest(sp,localecho);
  }

  public static final void main(String[] args) {
    try {
      tty(args);
    } catch(Throwable t) {
      System.out.println(t);
    }

  }
}
//$Id: tester.java,v 1.18 2003/07/27 05:35:14 mattm Exp $