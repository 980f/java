package net.paymate.ivicm.comm;
/* $Id: SerialConnection.java,v 1.17 2001/10/30 18:54:42 andyh Exp $ */

import net.paymate.util.*;
import net.paymate.ivicm.*;
import net.paymate.*;

import java.io.*;
import java.util.*;
import javax.comm.*;
import jpos.*;

public class SerialConnection implements JposConst {
  static final ErrorLogStream dbg=new ErrorLogStream(SerialConnection.class.getName());

  final static String VersionInfo= "$Revision: 1.17 $";
//this has to be tested with every driver we try to use.
//when we find an OS based difference we will push this into OS.java
  final static int WAITFOREVER=0;

  public SerialParameters parms;

  public OutputStream os;
  public InputStream is;

  protected CommPortIdentifier portId;
  protected SerialPort spPort;

  public SerialPort Port(){
    return spPort;
  }

  protected String myName;

  boolean amOpen;

  protected JposException Failure(String comment) {
    return new JposException(JPOS_E_FAILURE, comment);
  }

  protected JposException Failure(Exception ex) {
    return new JposException(JPOS_E_FAILURE,"Serial Connection Fault" ,ex);
  }

  public SerialConnection( String s, SerialParameters serialparameters) {
    myName = s;
    parms = serialparameters;
    amOpen = false;
    dbg.WARNING("On: "+myName+" ="+parms.toSpam());
  }

  public double CharTime(int numchars){
    return parms.CharTime()*(double)numchars;
  }

  public JposException setConnectionParameters() throws JposException {
    try {
      spPort.setSerialPortParams(parms.getBaudRate(), parms.getDatabits(), parms.getStopbits(), parms.getParity());
      spPort.setFlowControlMode(parms.flowControl);//was using unknown default
      spPort.setOutputBufferSize(parms.obufsize);
    }
    catch(UnsupportedCommOperationException uce) {
      dbg.ERROR("Bad serial params:"+parms.toSpam());
      return Failure(uce);
    }
    return null;
  }

  final static String  rxtxlistkey="gnu.io.rxtx.SerialPorts";

  public synchronized JposException openConnection(SerialPortEventListener receiver) {
    try {
      if(!amOpen){
        portId = CommPortIdentifier.getPortIdentifier(parms.getPortName());
        spPort = (SerialPort)portId.open("Paymate/" +parms.getPortName(), 0);//better be available!

        setConnectionParameters();

        os = spPort.getOutputStream();
        is = spPort.getInputStream();

        spPort.addEventListener(receiver);
        spPort.notifyOnDataAvailable(true);
        spPort.enableReceiveTimeout(WAITFOREVER);

        spPort.setRTS(true);//energize just in case it is needed
        spPort.setDTR(true);//ditto, although we will leave it set.

        amOpen = true;
      }
      return null;
    }
    catch(NoSuchPortException nosuchportexception){
      dbg.ERROR("No such port:"+parms.toSpam());
      dbg.ERROR(rxtxlistkey+":"+Main.props().getString(rxtxlistkey));
      return Failure(nosuchportexception);
    }
    catch(PortInUseException piu)  {
      dbg.ERROR("port in use:"+piu.currentOwner +" our settings"+parms.toSpam());
      return Failure(piu);
    }
    catch(JposException jposexception){
      spPort.close();
      return jposexception;
    }
    catch(IOException _ex){
      spPort.close();
      return Failure("Error opening i/o streams");
    }
    catch(TooManyListenersException _ex){
      spPort.close();
      return Failure("Too many listners for port");
    }
    catch(UnsupportedCommOperationException _ex){
      return Failure("Can not set port timeout");//presumably because of value
    }
  }
//////////////////////
// thread priority management
  protected boolean boosted=false; //have boosted my serial thread's priority
  public void boostCheck(){
    if(!boosted) {
      Thread.currentThread().setPriority(parms.priority);
      Thread.currentThread().setName(parms.getPortName());
      boosted = true;//do just once per program run.
    }
  }

  public synchronized void closeConnection() {
    if(amOpen){
      if(spPort != null){
        spPort.close();
        amOpen = false;
      }
    }
  }

  public boolean isOpen() {
    return amOpen;
  }

  public String toString() {
    return myName;
  }

  public static final boolean Connected(SerialConnection sc){
    return sc!=null && sc.isOpen();
  }

  public static final boolean canWrite(SerialConnection sc){
    return sc!=null && sc.isOpen() && sc.os!=null;
  }

  /**
   * start and end logic per String.substring();
   */
  public Exception lazyWrite(byte [] buffer,int off, int len){
    try {
      os.write(buffer,off,len);
      return null;
    } catch(Exception whocares){
      return whocares;
    }
  }

  public int ezRead(){
    try {
      return is.read();
    }
    catch (Exception ex) {
      return -1; //will add error codes later. Serial ports just don't do this.
    }
  }

  public int available(){
    try {
      return is.available();
    }
    catch (Exception ex) {
      return 0; //will add error codes later. Serial ports just don't do this.
    }
  }

  public int reallyFlushInput(int limit){
    int flushed=0;
    while(available()>0&&++flushed<limit){
      ezRead();
    }
    return flushed;
  }

}
//$Id: SerialConnection.java,v 1.17 2001/10/30 18:54:42 andyh Exp $
