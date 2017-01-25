package net.paymate.jpos.Terminal;

/**
 * Title:         $Source: /cvs/src/net/paymate/jpos/Terminal/SerialPrinter.java,v $
 * Description: line buffered data to serial device
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author        paymate.net
 * @version $Id: SerialPrinter.java,v 1.10 2001/09/27 00:02:59 andyh Exp $
 */

import net.paymate.ivicm.comm.*;
import jpos.JposException; //+_+ try to remove this via cleaning up SerialConnection
import javax.comm.*;
import net.paymate.util.ErrorLogStream;

public class SerialPrinter extends LinePrinter implements SerialPortEventListener {
  static final ErrorLogStream dbg=new ErrorLogStream(SerialPrinter.class.getName());
  SerialConnection sc;

  boolean fullyBuffered=true;//at the moment we don't have handshake feedback so
  //we just pump the data at the device and hope it can handle it.

  public boolean isConnected(){
    return SerialConnection.Connected(sc);
  }

  public SerialPrinter(String name,SerialConnection sc) throws JposException {
    super(name);
    this.sc=sc;
    if(sc!=null){
      dbg.VERBOSE("opening connection "+sc.toString());
      sc.openConnection(this);
      if(isConnected()){
        sc.Port().setRTS(false); //energize just in case it is needed
        sc.Port().notifyOnOutputEmpty(true);
        sc.Port().notifyOnDataAvailable(false);//constructor forced this true
        sc.Port().notifyOnCTS(false); //leave it up to driver to deal with this
      }
    }
  }

  //////////////////////
//LinePrinter overloads:
  public int maxByteBlock(){
    int fordebug=-1;
    try {
      return fordebug=sc.Port().getOutputBufferSize();
    } finally {
//      dbg.Message("BufferSize:"+fordebug);
    }
  }

  /**
   * synched to deal with a race between RTS causing a cts event and our need to simulate one if CTS
   * is already on.
   */
  public /*synchronized*/ void setRTS(boolean on){
    if(isConnected()&& sc.Port().isRTS()!=on){
      //set actual port bit
      dbg.VERBOSE("RTS:"+on);
      sc.Port().setRTS(on);
      //try to send something from parabuffer
      CTSEvent(on /*&& sc.Port().isCTS()*/);//leave CTS shaking up to driver
    }
  }

  protected synchronized void sendLine(byte [] rawline){
    if(isConnected()){
      try {
        dbg.VERBOSE("sendLine:"+rawline);
        sc.os.write(rawline);
        if(fullyBuffered){//then we depend upon OS to keep us from overruning the device
          CTSEvent(true);
        }
      } catch(java.io.IOException caught){
        dbg.Caught(caught);
      }
    }
  }


  public static String evtText(int evtCode){
      switch (evtCode) {
        case SerialPortEvent.DATA_AVAILABLE: return "DataAvailable";
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY: return "OutputBufferEmpty";
        case SerialPortEvent.CTS: return "ClearToSend changed to:";
        case SerialPortEvent.DSR: return "DSR changed to:";
        case SerialPortEvent.CD: return "CarrierDetect changed to:";
      }
    return "Illegal";
  }

  ///////////////////////
  public void serialEvent(SerialPortEvent serialportevent){
    dbg.Enter("events");
    try {
      int evt=serialportevent.getEventType();
      boolean estate=serialportevent.getNewValue();
      dbg.VERBOSE("Event:"+evtText(evt)+" "+estate);

      switch(evt){
        case SerialPortEvent.OUTPUT_BUFFER_EMPTY:{
          if(estate){
//driver is producing these 1 per second ---            CTSEvent(true);//CTSevent may have been a poor name
          }
        } break;
        case SerialPortEvent.DATA_AVAILABLE: {
          int commchar;
          while( (commchar = sc.is.read()) !=-1){
          //process received data;
          }
        } break;
        case SerialPortEvent.CTS: {//except we don't register for this, it is handled at lower level
//          CTSEvent(estate);
        } break;
      }
    }
    catch(Exception caught){
      dbg.Caught(caught);
    }
    finally {
      dbg.Exit();
    }
  }

}
//$Id: SerialPrinter.java,v 1.10 2001/09/27 00:02:59 andyh Exp $