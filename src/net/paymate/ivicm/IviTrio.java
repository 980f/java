package net.paymate.ivicm;

/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/IviTrio.java,v $
* Description:  legacy terminal builder, the three ivi headaches
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.17 $
*/

import net.paymate.ivicm.et1K.*;
import net.paymate.ivicm.ec3K.*;
import net.paymate.ivicm.pinpad.*;
import net.paymate.ivicm.nc50.*;

import net.paymate.ivicm.comm.*;

import net.paymate.jpos.Terminal.*;
import net.paymate.util.*;
import net.paymate.*;

import net.paymate.serial.*;
import net.paymate.awtx.print.*;

public class IviTrio {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(IviTrio.class);

  ET1K enTouch;
  EC3K CM3000;
  encrypt100 pinpad;
  NC50 cashpad;
  Scribe612 printer;

  public final static String ET1Kkey="ET1K";
  public final static String EC3Kkey="EC3K";
  public final static String encrypt100Key="pinpad";
  public final static String NC50Key="cashpad";
  public final static String Scribe612key ="P612" ;

/**
 * @return whether a signature capture device installed.
 */
  public boolean haveSigCap(){
    return enTouch!=null;
  }

  public MICRService micrReader(){
    return CM3000!=null ? CM3000.MICR() : null;
  }

  public MSRService msrReader(){
    return enTouch!=null ? enTouch.MSRService(id) : null;
  }

  public PINPadService pinReader(){
    return enTouch!=null ? enTouch.PINPadService(id) : null;
  }

  public FormService cat(){
    return enTouch!=null ? enTouch.FormService(id) : null;
  }

  public DisplayPad DisplayPad(){
    if(cashpad!=null){
      return nc50DisplayPad.makePad(cashpad);
    }
    if(pinpad!=null){
      return EC100DisplayPad.makePad(pinpad);
    }
    if(CM3000!=null){
      return EC3KDisplayPad.makeFrom(CM3000);
    }
    return DisplayPad.Null();//a useless one, but averts NPE's
  }

  private EC3K CM3000(){
    return CM3000;
  }

  public PrinterModel Printer(){
    return printer!=null? printer : new Scribe612( LinePrinter.Null());
  }

  String id;//tedious legacy
  private IviTrio(String id) {
    this.id=id;
  }

  public static final IviTrio New(String id,EasyCursor ezc) {
    IviTrio legacy=new IviTrio(id);
    PortProvider.Config(Main.props());
    legacy.makeEC3K(id,ezc);
    legacy.makepinpad(id,ezc);
    legacy.makecashpad(id,ezc);
    legacy.makeET1K(id,ezc);
    legacy.makeP612(id,ezc);
    return legacy;
  }

  public void retire(){
    enTouch.disConnect();
    CM3000.disConnect();
    printer.disConnect();
    pinpad.disConnect();
  }

  private String classid(String classtoken,String id){
    return classtoken+"."+id;
  }

  private void  makeP612 (String id,EasyCursor ezc) {
    dbg.Enter("makeP612");
    ezc.push(Scribe612key);
    try {
      String uid=classid(Scribe612key,id);
      if(ezc.getBoolean("present")){
        LinePrinter urp;
        SerialConnection sc=SerialConnection.makeConnection(ezc,9600);
        if (sc!=null) {
          dbg.VERBOSE("printer on serial:"+sc.parms.getPortName());
          //to verify that padding is what makes it work...--          sc.parms.setFlow(SerialPort.FLOWCONTROL_RTSCTS_OUT);
          sc.parms.setFlow(Parameters.FLOWCONTROL_NONE);//+++ take from config file
          urp=new SerialPrinter(DeviceName.RawPrinter,sc);
          urp.configure(ezc);//to get null padding params
          //.can't do this as we don;t want to pad when RCB is in use.          urp.configure(main.props().push("Scribe612"))
        } else { //via RCB aux1:
          if(enTouch!=null){
            dbg.VERBOSE("hoping printer is on enTouch Aux");
            urp=enTouch.AuxPrinter(DeviceName.RawPrinter);
            RCBPrinter.Configure(Main.props("RCBPrinter"));//platform depednent variations not covered by server.
            urp.configure(ezc);
          } else {
            dbg.VERBOSE("printer is a bit bucket");
            urp=new LinePrinter("NullPrinter");
          }
        }
        printer = new Scribe612(urp);
        printer.configure(ezc);
      }
    }
    finally {
      ezc.pop();
      dbg.Exit();
    }
  }

  private void makeET1K(String id,EasyCursor ezc) {
    dbg.Enter("makeET1K");
    ezc.push(ET1Kkey);
    try {
      String uid = classid(ET1Kkey,id);
      if(ezc.getBoolean("present")){
        dbg.VERBOSE("Making "+uid);
        dbg.VERBOSE(ezc.asParagraph(OS.EOL));

        enTouch=new ET1K(uid);
        enTouch.Connect(SerialConnection.makeConnection(ezc,19200));
        enTouch.minlatency= ezc.getNumber("latency",enTouch.minlatency);
        enTouch.rawAccess().getVersionInfo();//"gets" it to debug stream
      } else {
        enTouch=null;
      }
    }
    finally {
      ezc.pop();
      dbg.Exit();
    }
  }

  private void makeEC3K(String id,EasyCursor ezc) {
    dbg.Enter("makeEC3K");
    ezc.push(EC3Kkey);
    try {
      String uniqueid=classid(EC3Kkey,id);
      if(ezc.getBoolean("present")){
        dbg.VERBOSE("Making "+uniqueid);
        dbg.VERBOSE(ezc.asParagraph(OS.EOL));
        CM3000= new EC3K(uniqueid);
        CM3000.Connect(SerialConnection.makeConnection(ezc,9600));
      } else {
        CM3000= null;
      }
    }
    finally {
      ezc.pop();
      dbg.Exit();
    }
  }

  private void makepinpad(String id,EasyCursor ezc) {
    dbg.Enter("makepinpad");
    ezc.push(encrypt100Key);
    try {
      String uniqueid=classid(encrypt100Key,id);
      if(ezc.getBoolean("present")){
        dbg.VERBOSE("Making "+uniqueid);
        ezc.setString(Parameters.protocolKey,"E71");
        dbg.VERBOSE(ezc.asParagraph(OS.EOL));
        pinpad= new encrypt100(uniqueid);
        pinpad.Connect(SerialConnection.makeConnection(ezc,9600));
      } else {
        pinpad= null;
      }
    }
    finally {
      ezc.pop();
      dbg.Exit();
    }
  }
  private void makecashpad(String id,EasyCursor ezc) {
    dbg.Enter("makecashpad");
    ezc.push(NC50Key);
    try {
      String uniqueid=classid(NC50Key,id);
      if(ezc.getBoolean("present")){
        dbg.VERBOSE("Making "+uniqueid);
        ezc.setString(Parameters.protocolKey,"E71");
        dbg.VERBOSE(ezc.asParagraph(OS.EOL));
        cashpad= new NC50(uniqueid);
        cashpad.Connect(SerialConnection.makeConnection(ezc,4800));
      } else {
        cashpad= null;
      }
    }
    finally {
      ezc.pop();
      dbg.Exit();
    }
  }

}
//$Id: IviTrio.java,v 1.17 2002/07/26 23:39:50 andyh Exp $
