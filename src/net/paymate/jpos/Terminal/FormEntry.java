package net.paymate.jpos.Terminal;
/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/FormEntry.java,v $
* Description:  wrap jpos form device
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: FormEntry.java,v 1.29 2002/07/09 17:51:28 mattm Exp $
*/
import net.paymate.util.*;

import net.paymate.terminalClient.IviForm.*; //--- need to move package here!
import net.paymate.jpos.data.*;

import net.paymate.ivicm.et1K.*;

public class FormEntry {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(FormEntry.class);
  boolean aFormIsActive=false;

  FormService device;
  public void joins(FormService device,QReceiver posterm){
    this.device=device;
    if(device!=null){
      device.setReceiver(posterm);
    }
  }

  public void clearInput(){
    if(device!=null){
      device.clearFormInput();
    }
  }

  boolean inAcquire=false;
  public void Acquire(Form form, boolean alreadyStored) {
    dbg.Enter("acquire:"+form.myName);//#gc
    try {
      if(device!=null) {
        if(inAcquire){
          dbg.ERROR("reentered:"+form.myName);
        }
        inAcquire=true;
        aFormIsActive=true;
        dbg.VERBOSE("startingform");
        device.startForm(form, alreadyStored);
      }
    }
    finally {
      inAcquire=false;
      dbg.Exit();//#gc
    }
  }

  public void StoreForm(Form form){
    if(device!=null) {
      device.storeForm(form);
    }
  }

  public void displayLegend(Legend dynamic) {
    if(device!=null) {
      device.setFont(dynamic.code(),dynamic.attr());
      device.displayTextAt(dynamic.y(),dynamic.x(),dynamic.getText());
    }
  }

  public FormEntry(FormService device,QReceiver posterm) {
    joins(device,posterm);
  }

  public void EndForm(){//kill form in prep for antoher to be loaded
    dbg.Enter("EndForm:"+aFormIsActive);
    try {
      if(aFormIsActive&&device!=null){
        dbg.ERROR("calling jpos end form");
        device.endForm();
      } else {
        dbg.ERROR("ending a form when already ended!");
      }
      aFormIsActive=false;
    }
    finally {
      dbg.Exit();
    }
  }

}
//$Id: FormEntry.java,v 1.29 2002/07/09 17:51:28 mattm Exp $