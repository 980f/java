package net.paymate.ivicm;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/SerialDevice.java,v $
 * Description:  stuff common to ivicm devices
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: SerialDevice.java,v 1.6 2001/09/27 00:02:22 andyh Exp $
 */

import javax.comm.*;
import net.paymate.ivicm.comm.*;
import jpos.JposException;
import net.paymate.serial.*;

abstract public class SerialDevice extends Receiver implements SerialPortEventListener {
  protected String myName;
  protected SerialConnection myPort;

  public String toString(){
    return this.getClass().getName()+"."+myName;
  }

  public SerialDevice(String id) {
    myName=id;
    myPort=null;
  }

  boolean haveConnected(){
    return myPort!= null && myPort.isOpen();
  }

  abstract protected void onConnect();//called when serial connection opens

  public JposException reConnect(){
    return Connect(myPort);
  }

  public void disConnect(){
    myPort.closeConnection();
  }

  public JposException Connect(SerialConnection sc){
    JposException jape=null;
    if(haveConnected()){
      disConnect();
    }
    myPort=sc;
    if(myPort!=null){
      jape=myPort.openConnection(this);
      if(myPort.isOpen()){
        onConnect();
      }
    }
    return jape;
  }

}
//$Id: SerialDevice.java,v 1.6 2001/09/27 00:02:22 andyh Exp $