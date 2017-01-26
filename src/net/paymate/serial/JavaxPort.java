package net.paymate.serial;

/**
* Title:        $Source: /cvs/src/net/paymate/serial/JavaxPort.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.34 $
*/

import javax.comm.*;
import java.io.*;
import net.paymate.util.*;
import net.paymate.lang.ReflectX;

/**
* have to feed javax comm  events back into a blocking stream
* seems like a performance loss, and it is. But this is legacy stuff
* to help us verify operation of using teh fd's direct from teh system INSTEAD
* of javax.comm.
*/
class inputter extends InputStream implements SerialPortEventListener {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(inputter.class);

  InputStream javaxis;
  int rxtimeout;//wake up this often to see if input is present but failed to notify.

  public inputter setNiceness(int rxtimeout){
    this.rxtimeout=rxtimeout;
    dbg.VERBOSE("rxtimeout set to:"+rxtimeout);
    return this;
  }

  inputter(InputStream javaxis,int rxtimeout){
    this.javaxis=javaxis;
    setNiceness(rxtimeout);
  }

  public int available() throws IOException {
    return javaxis!=null? javaxis.available(): 0;
  }

  public void serialEvent(SerialPortEvent siev){
    if(siev == null) {
      dbg.VERBOSE("siev is null!");
    } else {
      dbg.VERBOSE("siev = " +siev.getSource()+","+siev.getEventType());
    }
    //get the read out of its wait state
    wakeupread.Stop();
  }

  private Waiter wakeupread = new Waiter();//prepare()s on construction.

  /**
   * blocking read, timeout set on this object.
   */
  public int read() throws IOException{
    //block until we have something on the javax stream...
    int avaible=javaxis.available();
    dbg.VERBOSE("read() called:"+avaible);
    if(avaible<=0){//stifle sending endOfInput when line is idle.
      wakeupread.Start(rxtimeout);//periodically poll in case event gets lost
    //ignore timeout indication by ignoring return from above.
      dbg.VERBOSE(wakeupread.toSpam());
      avaible=javaxis.available();
      wakeupread.prepare(); //be ready for another event.
      dbg.VERBOSE("waiton: available= "+javaxis.available());
    }
    return avaible>0? javaxis.read() : Receiver.TimedOut; //"line idle timeout"
  }

  public int read(byte b[]) throws IOException {
    int blen=Math.min(javaxis.available(),b.length);
    int i;
    for(i=0; i<blen; i++){
       b[i]=(byte)javaxis.read();
    }
    return i;//formerly returned blen, this should be the same but is safer for maintenance.
  }

}//end class inputter

public class JavaxPort extends Port {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(JavaxPort.class);
  static final boolean hasBlocking=false;//+_+ seems to be only true for paymate fileports.
  private static final int FOREVER = 0;
/*javax.comm*/  SerialPort jxport;
  inputter legacy;

/**
 * a nice port doesn't spew eof's when the source is idle.
 * a few now and then are allowed even when nice.
 */
  public boolean isNice(){
    return hasBlocking || niceness>0; //very few variations are nice
  }

  public int setNiceness(int millis){
    try {
      return super.setNiceness(millis);
    }
    finally {
      if(legacy!=null){
        legacy.setNiceness(millis);
      }
    }
  }

  public Port changeSettings(Parameters parms){
    try {
      if (jxport != null) {
        jxport.setSerialPortParams(parms.getBaudRate(), parms.getDatabits(), parms.getStopbits(), jxcparity(parms.parityCode()));
        jxport.setFlowControlMode(parms.flowControl);//was using unknown default
        jxport.setOutputBufferSize(parms.obufsize);
      }
    }
    catch (UnsupportedCommOperationException ug){
        dbg.ERROR("Bad serial params:"+parms.toSpam());      //don't frigging care
    }
    finally {
      return super.changeSettings(parms);
    }
  }

  /**
   * @--deprecated legacy.
   */
  public boolean assertRts(boolean on){//legacy
    return RTS().setto(on);
  }

  private int jxcparity(char traditional){
    switch (traditional) {
    case 'N': return SerialPort.PARITY_NONE;
    case 'E': return SerialPort.PARITY_EVEN;
    case 'O': return SerialPort.PARITY_ODD;
    }
    return SerialPort.PARITY_MARK; //will never happen but appeases compiler
  }

/**
 * open port getting port and port settings from @param parms
 * @return whether open succeeded. If so then we also have streams ready for use.
 */
  public boolean openas(Parameters parms){
    this.parms =parms;
    CommPortIdentifier portId = null;//retained for debug purposes.
    try {
      portId = CommPortIdentifier.getPortIdentifier(parms.getPortName());
      jxport = (SerialPort) portId.open("Paymate_" +parms.getPortName(), 0);//better be available!
      changeSettings(parms);
      if(hasBlocking){
        super.setStreams(jxport.getInputStream(),jxport.getOutputStream());
      } else {
        niceness=parms.niceness(/*minimum:*/37);//30 is legacy for entouch
        legacy=new inputter(jxport.getInputStream(),niceness);
        jxport.addEventListener(legacy);
        super.setStreams(legacy,jxport.getOutputStream());
        jxport.notifyOnDataAvailable(parms.hasrx);
        jxport.notifyOnOutputEmpty(false);//printer once had this true

        super.rts=JavaxShaker.makeRTS(jxport);
        if( ! parms.FlowIs(parms.FLOWCONTROL_RTSCTS_OUT)){
          RTS().setto(parms.initialRTS);
        }
        //we are limiting ourselves here to either using the jni driver to deal
        //with CTS, or ignoring it totally. +_+
        jxport.notifyOnCTS(false); //leave it up to driver to deal with this
        super.dtr=JavaxShaker.makeDTR(jxport);
        DTR().setto(parms.initialDTR);
      }
      jxport.enableReceiveTimeout(hasBlocking? FOREVER: niceness);
      return true;
    } catch (javax.comm.NoSuchPortException nsp){
      dbg.ERROR(ReflectX.justClassName(nsp)+" "+parms.toSpam());
      return false;
    } catch (javax.comm.PortInUseException piu){
      dbg.ERROR(ReflectX.justClassName(piu)+" "+parms.toSpam());
      dbg.ERROR("port in use by:"+portId.getCurrentOwner());
      return false;
    }
    catch (Exception ex){
      dbg.Caught(ex);
      return false;
    }
  }

/**
 * after a close one must call openas to use this Port again.
 */
  public Port close(){
    super.close();
    if(jxport!=null){
      jxport.close();
      jxport=null;
    }
    return this;
  }

  public JavaxPort(String portname) {
    super(portname);
  }



}
//$Id: JavaxPort.java,v 1.34 2003/07/27 05:35:13 mattm Exp $