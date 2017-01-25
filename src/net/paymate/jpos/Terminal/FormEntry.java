package net.paymate.jpos.Terminal;
/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/FormEntry.java,v $
* Description:  wrap jpos form device
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: FormEntry.java,v 1.23 2001/11/14 01:47:50 andyh Exp $
*/


import net.paymate.util.*;

import net.paymate.terminalClient.IviForm.*; //--- need to move package here!
import net.paymate.jpos.data.*;

import jpos.JposException;
import jpos.events.DataEvent;
import java.awt.Point;

public class FormEntry extends jpos.Form implements DEService  {
  static final ErrorLogStream dbg=new ErrorLogStream(FormEntry.class.getName());
  boolean fakeit=true; //until we are attached successfully
  protected Listener thePOSapp;
  boolean aFormIsActive=false;

  String id;
  public String toString(){
    return id;
  }

  public void dataOccurred (DataEvent e){
    try {
      dbg.Enter("FormEntry.dataOccurred:"+e.getClass().getName());
      byte [] humm=getButtonData();
      if(Safe.NonTrivial(humm)){
        thePOSapp.Handle(new FormButtonData(humm));
      } else {
        humm=getRawSigData();
        if(Safe.NonTrivial(humm)){
          thePOSapp.Handle(new SigCaptured(new SigData(humm)));
        }
      }
      setDataEventEnabled(true); //fire off the next event
    } catch (JposException jape){
      thePOSapp.Handle(jape);
    } finally {
      dbg.Exit();
    }
  }

  public void errorOccurred(jpos.events.ErrorEvent jape){
    try {
      thePOSapp.Handle(jape);
      setDataEventEnabled(true); //fire off the next event
    } catch (Exception trash) {
      dbg.ERROR("ErrorEvent occurred: " + jape);
      dbg.Caught(trash);
    }
  }

  public void clearInput() throws jpos.JposException {
    if(fakeit) return;
    clearFormInput(); //gratuitous naming difference bites.
  }

  public void Attach(String id) {
    dbg.VERBOSE("attach:"+id);
    fakeit=(Base.Attach(this,this.id=id,DeviceName.FormEntry)!=null);
    //tried to load forms here but that was pointless syntax
    //the person who creates the terminal will have to send forms
  }

  public void Release() {
    Base.Release(this);
  }

  boolean inAcquire=false;
  public JposException Acquire(String formName, boolean b) {
    dbg.Enter("acquire:"+formName);
    try {
      if(fakeit) {
        dbg.VERBOSE("fakeit");
        return null;
      }
      if(true==inAcquire){
        dbg.ERROR("reentered:"+formName);
        return null; //reentered!
      }
      inAcquire=true;
      try {
        aFormIsActive=true;
        dbg.VERBOSE("startingform");
        startForm(formName, b);
        Base.Acquire(this);
      } catch (JposException jape){
        dbg.Caught(jape);
        return jape;
      } finally {
        inAcquire=false;
      }
      return null;
    }
    finally {
      dbg.Exit();
    }
  }

  public JposException StoreForm(String formname){
    if(fakeit) {
      return null;
    }
    try {
      if(Safe.NonTrivial(formname)){
        storeForm(formname);
      }
      return null;
    } catch (JposException jape){
      return jape;
    }
  }

  public JposException displayLegend(Legend dynamic) {
    if(fakeit) {
      return null;
    }
    try {
      setFont(dynamic.code(),dynamic.attr());
      displayTextAt(dynamic.y(),dynamic.x(),dynamic.getText());
      return null;
    } catch (JposException jape){
      return jape;
    }
  }

  public FormEntry(Listener thePOSapp) {
    this.thePOSapp=thePOSapp;
  }

  public JposException EndForm(){//kill form in prep for antoher to be loaded
    dbg.Enter("EndForm:"+aFormIsActive);
    try {
      if(aFormIsActive){
        ErrorLogStream.Debug.ERROR("calling jpos end form");
        super.endForm();
      } else {
        ErrorLogStream.Debug.ERROR("ending a form when already ended!");
      }
      aFormIsActive=false;
      return null;
    } catch (JposException jape){
      return jape;
    } finally {
      dbg.Exit();
    }
  }

}
//$Id: FormEntry.java,v 1.23 2001/11/14 01:47:50 andyh Exp $
