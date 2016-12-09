package net.paymate.serial;

/**
* Title:        $Source: /cvs/src/net/paymate/serial/JavaxPort.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.3 $
*/

import javax.comm.*;
import java.io.*;
import net.paymate.util.*;
/**
* have to feed javax comm  events back into a blocking stream
* seems like a performance loss, and it is. But this is legacy stuff
* to help us verify operation of using teh fd's direct from teh system INSTEAD
* of javax.comm.
*/
class inputter extends InputStream implements SerialPortEventListener {

  InputStream javaxis;
  Thread reader;

  inputter(  InputStream javaxis){
    this.javaxis=javaxis;
  }

  public void serialEvent(SerialPortEvent siev){
    this.siev= siev;
    //get the read out of its wait state
    if(reader!=null){
      reader.interrupt();
    }
  }

  SerialPortEvent siev=null;

  public int read() throws IOException{
    //block until we have something on the javax stream...
    reader=Thread.currentThread();
    while(true){
      try {
        wait(); // on siev!=null
      } catch(InterruptedException go){
        Thread.interrupted(); // clears interrupted bits
        switch(siev.getEventType()){
          case SerialPortEvent.DATA_AVAILABLE:{
            return javaxis.read();
          }
          case SerialPortEvent.CD:
          case SerialPortEvent.DSR:
          case SerialPortEvent.RI:{
            //someday we will insert extra values into streams.
          }  break;
          default:{
            return Receiver.DataLost;
          }
        }
      }
    }
  }

}

public class JavaxPort extends Port {
  static final Tracer dbg=new Tracer(JavaxPort.class.getName());
  static final boolean hasBlocking=true;//true for pm version, false for rxtx,windows,...
  private static final int FOREVER = 0;

  SerialPort jxport;

  public JavaxPort(Parameters parms) {
    super(parms.getPortName());
    try {
      CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(parms.getPortName());
      jxport = (SerialPort) portId.open("Paymate_" +parms.getPortName(), 0);//better be available!
      try {
        jxport.setSerialPortParams(parms.getBaudRate(), parms.getDatabits(), parms.getStopbits(), parms.getParity());
        jxport.setFlowControlMode(parms.flowControl);//was using unknown default
        jxport.setOutputBufferSize(parms.obufsize);
      }
      catch(UnsupportedCommOperationException uce) {
        dbg.ERROR("Bad serial params:"+parms.toSpam());
      }
      if(hasBlocking){
        super.setStreams(jxport.getInputStream(),jxport.getOutputStream());
      } else {
        inputter legacy=new inputter(jxport.getInputStream());
        super.setStreams(legacy,jxport.getOutputStream());
        jxport.notifyOnDataAvailable(true);
        jxport.addEventListener(legacy);
      }
      jxport.enableReceiveTimeout(hasBlocking? FOREVER: 30);
    } catch (Exception ex){
      dbg.Caught(ex);
    }
  }

}
//$Id: JavaxPort.java,v 1.3 2001/08/14 23:25:29 andyh Exp $
