/**
* Title:        CheckReader
* Description:  jpos.MICR
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CheckReader.java,v 1.11 2001/07/19 01:06:50 mattm Exp $
*/
package net.paymate.jpos.Terminal;
import  net.paymate.jpos.data.MICRData;
import net.paymate.util.*;

import jpos.JposException;
import jpos.events.DataEvent;

public class CheckReader extends jpos.MICR implements DEService {
  static final ErrorLogStream dbg=new ErrorLogStream(CheckReader.class.getName());
  boolean fakeit;
  protected Listener thePOSapp;
  protected MICRData blob;//4debug

  String id;
  public String toString(){
    return id;
  }


  protected MICRData Value() throws JposException {
    // was the blob supposed to be static?  it wasn't getting created at all
    blob = new MICRData();
    if(this!=null){
      blob.RawTrack=getRawData();
      blob.Transit= getTransitNumber();
//      blob.Bank= getBankNumber() ;
      blob.Account= getAccountNumber();
      blob.Serial= getSerialNumber();
      blob.Amount= getAmount();
      blob.EPC= getEPC();
      //.getCheckType()) //need to coordinate with iso checktype
      //.getCountryCode())
    }
    return blob;
  }

  public void dataOccurred (DataEvent e){
    try {
      //+_+ presume it is the only one we have registered for
      thePOSapp.Handle(new CheckScanned(Value()));
    } catch (JposException jape){
      thePOSapp.Handle(jape);
    } finally {
      try {
//cm3000 bug makes do one-shot acquisition        setDataEventEnabled(true); //fire off the next event
      } catch (Exception trash) {
        dbg.Caught(trash);
      }
    }
  }

  public void errorOccurred(jpos.events.ErrorEvent jape){
    //thePOSapp.swallow(jape)
    try {
      setDataEventEnabled(true); //fire off the next event
    } catch (Exception trash) {
      dbg.ERROR("ErrorEvent occurred: " + jape);
      dbg.Caught(trash);
    }
  }

  public void Attach(String id) {
    fakeit=(Base.Attach(this,this.id=id,DeviceName.CheckReader)!=null);
  }

  public void Release() {
    Base.Release(this);
  }

  public void Acquire() {
    if(!fakeit){
      try {
        Base.Acquire(this);
        beginInsertion(-1);
      } catch (JposException jape){
        thePOSapp.Handle(jape);
      }
    }
  }

  public void Pause() {
    if(!fakeit){
      try {
        endInsertion();
      } catch (JposException jape){
        thePOSapp.Handle(jape);
      }
    }
  }

  public CheckReader(Listener thePOSapp) {
    this.thePOSapp=thePOSapp;
  }

}
//$Id: CheckReader.java,v 1.11 2001/07/19 01:06:50 mattm Exp $
