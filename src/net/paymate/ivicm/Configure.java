package net.paymate.ivicm;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/Configure.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Configure.java,v 1.35 2001/11/03 02:53:35 andyh Exp $
*/

import jpos.ServiceTracker;
import net.paymate.util.*;
import net.paymate.awtx.print.*;
import net.paymate.jpos.common.NullForm;
import net.paymate.ivicm.ec3K.*;
import net.paymate.ivicm.et1K.*;
import net.paymate.ivicm.comm.*;
import net.paymate.*;

import jpos.*;
import jpos.services.*;
import javax.comm.*;//for parameters
import net.paymate.jpos.Terminal.*;
import java.util.Vector;

public class Configure {
  static final ErrorLogStream dbg=new ErrorLogStream(Configure.class.getName());
//the following is a shortcut through jpos layers to get at real hardware
//the legal way is to make device physical descriptions yield up a classname
//with which a devicename can be used to create a link to a real device.
//we would then have to reduce the set of all devices to unique devices and so on
//the result of which is to build the folowing vector:
  public static final Vector realThings=new Vector(3);

  public static final void purge(){
    for(int i=realThings.size();i-->0;){
      SerialDevice realThing=(SerialDevice)realThings.elementAt(i);//fue
        //gotta free the objects port
        realThing.disConnect(); //free up real resources
        //and remove it from our list
        realThings.remove(i);
    }
    ServiceTracker.purge();//erase objects.
  }

  /**
   * calls "Connect()" on each real thing
   */
  public static final void reConnect(){
    for(int i=realThings.size();i-->0;){
      SerialDevice realThing=(SerialDevice)realThings.elementAt(i);//fue
      realThing.reConnect();
    }
  }

  protected static final SerialConnection makeConnection(EasyCursor ezp,int defbaud){
    try {
//      dbg.VERBOSE(ezp.toString("MakeConnection"));
      String portname=ezp.getString("portName");
      if(Safe.NonTrivial(portname)){
        ezp.assure("baud",Integer.toString(defbaud));
        return new SerialConnection("paymate_"+portname,new SerialParameters(portname,ezp));
      } else {
        dbg.VERBOSE("hopelessly bad portName:"+portname);
        return null;
      }
    } catch(Exception ignored){
      dbg.Caught("Caught while makingConnection:",ignored);
      return null;
    }
  }

  public static final void Load(String id,EasyCursor ezc) {
    dbg.Enter("Load");
    String uid="Loading";
    SerialConnection port;
    try {
      dbg.VERBOSE("Look for a EC3K");
      ezc.push("EC3K");
      dbg.VERBOSE(ezc.toSpam());
      if(ezc.getBoolean("present",true)){
        //check for an existing one, in case this is a reload
        dbg.VERBOSE("Making a CM3000 for "+id);
        uid="EC3K."+id; //unique across equipment pieces.
        port=makeConnection(ezc,9600);

        EC3K CM3000= new EC3K(uid);
        CM3000.Connect(port);
        realThings.add(CM3000);
        //+_+ iterate over a "provides" CSV list:
        //the names here are what must be used in the jpos open's
        ServiceTracker.storeService(CM3000.LineDisplay(DeviceName.fullname(id,DeviceName.LineDisplay)));
        ServiceTracker.storeService(CM3000.Keypad(DeviceName.fullname(id,DeviceName.Keyboard)));
        ServiceTracker.storeService(CM3000.MICR(DeviceName.fullname(id,DeviceName.CheckReader)));
      }
//      else {
//        dbg.Message("CM3000 not configured");
//      }
      dbg.VERBOSE("Look for a ET1K");
      ezc.setKey("ET1K");
      uid = "ET1K."+id;
      ET1K enTouch=null;
      if(ezc.getBoolean("present",true)){        //then we will do the same for et1k
        dbg.VERBOSE("Making an ET1K named "+uid);
        ServiceTracker.storeService(new ServiceObject("ET1K."+"SignatureCompression",ezc.getString("SignatureCompression","2byte")));
        enTouch=new ET1K(uid);
        dbg.VERBOSE(ezc.toString("Make ET1k"));
        enTouch.Connect(makeConnection(ezc,19200));
        realThings.add(enTouch);
        enTouch.minlatency= ezc.getNumber("latency",enTouch.minlatency);

        ServiceTracker.storeService(enTouch.MSRService(DeviceName.fullname(id,DeviceName.CardReader)));
        ServiceTracker.storeService(enTouch.FormService(DeviceName.fullname(id,DeviceName.FormEntry)));
        ServiceTracker.storeService(enTouch.PINPadService(DeviceName.fullname(id,DeviceName.PinEntry)));

        enTouch.rawAccess().getVersionInfo();//"gets" it to debug stream
      }

      dbg.VERBOSE("Look for a P612");
      ezc.setKey("P612");
      uid="P612."+id;
      if(ezc.getBoolean("present",true)){
        LinePrinter urp;
        SerialConnection sc=makeConnection(ezc,9600);
        if (sc!=null) {
          dbg.VERBOSE("printer on serial:"+sc.parms.getPortName());
//to verify that padding is what makes it work...--          sc.parms.setFlow(SerialPort.FLOWCONTROL_RTSCTS_OUT);
          sc.parms.setFlow(SerialPort.FLOWCONTROL_NONE);//+++ take from config file
          urp=new SerialPrinter(DeviceName.RawPrinter,sc);
          ServiceTracker.storeService(new ServiceObject(DeviceName.RawPrinter,urp));
          urp.configure(ezc);//to get null padding params
//can't do this as we don;t want to pad when RCB is in use.          urp.configure(main.props().push("Scribe612"))
        } else { //via RCB aux1:
          if(enTouch!=null){
            dbg.VERBOSE("hoping printer is on enTouch Aux");
            urp=enTouch.AuxPrinter(DeviceName.RawPrinter);
            RCBPrinter.Configure(Main.props().push("RCBPrinter"));//platform depednent variations not covered by server.
            urp.configure(ezc);
            ServiceTracker.storeService(urp);
          } else {
            dbg.VERBOSE("printer is a bit bucket");
            urp=new LinePrinter("NullPrinter");
          }
        }
        PrinterModel printer = new Scribe612(urp);
        printer.configure(ezc);
        ServiceTracker.storeService(new ServiceObject(DeviceName.ReceiptPrinter,printer));
      }
    }
    catch (JposException jape){
      dbg.Caught(jape);
    }
    catch (Exception caught) {
      dbg.Caught(caught);
    }
    finally {
      ezc.pop();
      dbg.Exit();
    }
  }

}
//$Id: Configure.java,v 1.35 2001/11/03 02:53:35 andyh Exp $
