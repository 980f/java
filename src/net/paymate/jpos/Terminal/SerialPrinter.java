package net.paymate.jpos.Terminal;

/**
 * Title:         $Source: /cvs/src/net/paymate/jpos/Terminal/SerialPrinter.java,v $
 * Description: line buffered data to serial device
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author        paymate.net
 * @version $Id: SerialPrinter.java,v 1.20 2002/09/06 18:56:13 andyh Exp $
 */

import net.paymate.ivicm.comm.*;
import net.paymate.util.*;

public class SerialPrinter extends LinePrinter {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(SerialPrinter.class);
  SerialConnection sc;

  boolean fullyBuffered=true;//at the moment we don't have handshake feedback so
  //we just pump the data at the device and hope it can handle it.

  public boolean isConnected(){
    return SerialConnection.Connected(sc);
  }

  public void disConnect(){
    //do our stuff then
    super.disConnect();
  }

  public SerialPrinter(String name,SerialConnection sc)  {
    super(name);
    try {
      this.sc=sc;
      if(sc!=null){
        dbg.VERBOSE("opening connection "+sc);
       //these guys aren't in config file (yet)
        sc.parms.hasrx=false;//constructor forced this true
        sc.parms.initialRTS=false;
        sc.openConnection(null);//no receiver
      }
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  //////////////////////
//LinePrinter overloads:
/**
 * @return an advisory value used to break up large binary blocks for open loop pacing.
 */
  public int maxByteBlock(){
    return 256;//rxtx port never did implement this function
  }

  /**
   * synched to deal with a race between RTS causing a cts event and our need to simulate one if CTS
   * is already on.
   */

  public /*synchronized*/ void setRTS(boolean on){
    if(sc.port.RTS(on)){
      //try to send something from parabuffer
      CTSEvent(on /*&& sc.Port().isCTS()*/);//leave CTS shaking up to driver
    }
  }

  protected synchronized void sendLine(byte [] rawline){
    if(isConnected()){
      try {
        dbg.VERBOSE("sendLine:"+Ascii.image(rawline));
        sc.os.write(rawline);
        if(fullyBuffered){//then we depend upon OS to keep us from overruning the device
          CTSEvent(true);
        }
      } catch(java.io.IOException caught){
        dbg.Caught(caught);
      }
    }
  }


//////////////////////////////////////////////////////////////
/**
 * test port and baud and such
 */
  static public void main(String[] args) {
    TextListIterator arglist= TextListIterator.New(args);
    SerialConnection sc= SerialConnection.fortesters(arglist,9600,dbg);
    SerialPrinter tp=new SerialPrinter("tester",sc);
    tp.sendLine("Printer tester\n".getBytes());
    byte []key=new byte[1];
    while(true){
      try {
        key[0]=(byte)System.in.read();
        tp.sendLine(key);
      }
      catch (Exception ex) {
        dbg.ERROR(ex.getMessage());
        continue;
      }
    }
  }

}
//$Id: SerialPrinter.java,v 1.20 2002/09/06 18:56:13 andyh Exp $