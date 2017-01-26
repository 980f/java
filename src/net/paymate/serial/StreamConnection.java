package net.paymate.serial;

import net.paymate.ivicm.comm.SerialConnection;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/StreamConnection.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.ivicm.*;
import net.paymate.jpos.data.*;

import net.paymate.*;

import java.io.*;

public class StreamConnection extends SerialConnection {
  Stream stream;

  public void forceTimeout(int milliwait){
    if(stream!=null){
      stream.startTimeout(milliwait);
    }
  }

//  public boolean assertRts(boolean on){
//    return port!=null?port.assertRts(on):false;
//  }

  public synchronized boolean closeConnection() {
    if(super.closeConnection()){
      stream.Close();
      return true;
    } else {
      return false;
    }
  }

  public synchronized Problem openConnection(Receiver baseless) {
    try {
      if(super.openConnection()){
        stream= new Stream(baseless);
        //make port
        port=PortProvider.makePort(parms.getPortName());
        port.openas(parms);  //should check return value +_+
        stream.Attach(port); //get to the system streams
        setStreams(port.xmt(),port.rcv());
        stream.startReception(Receiver.TimeoutNever);//allow reception
      }
      return null;
    }
    catch(ClassCastException cce){
      return super.openConnection(baseless);
    }
    catch (Exception ex) {
      return Failure(ex);
    }
  }

  public void configure(String portname,Parameters parms) {
    super.configure(portname,parms);
    //c'est tout
  }

    /**
   * @undeprecated do not directly use, will soon merge into base class
   */
  public StreamConnection(){//for super maker
  }

}
//$Id: StreamConnection.java,v 1.5 2002/09/06 18:56:12 andyh Exp $