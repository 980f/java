/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/PinEntry.java,v $
* Description:  jpos.PINPad
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PinEntry.java,v 1.13 2001/07/19 01:06:50 mattm Exp $
*/
package net.paymate.jpos.Terminal;

import net.paymate.jpos.data.PINData;
import net.paymate.util.*;

import net.paymate.jpos.data.CardNumber;

import jpos.JposException;
import jpos.events.DataEvent;


public class PinEntry extends jpos.PINPad implements DEService  {
  static final ErrorLogStream dbg=new ErrorLogStream(PinEntry.class.getName());
  boolean fakeit;
  protected Listener thePOSapp;

  String id;
  public String toString(){
    return id;
  }

  public void dataOccurred (DataEvent e){
    try {
      //+_+ presume it is the only one we have registered for
      thePOSapp.Handle(new PinCaptured(new PINData(getEncryptedPIN())));
      setDataEventEnabled(true); //fire off the next event
    } catch (JposException jape){
      thePOSapp.Handle(jape);
    } catch (Exception trash) {
      dbg.Caught(trash);
    }
  }

  public void errorOccurred(jpos.events.ErrorEvent jape){
    thePOSapp.Handle(new PinCaptured(/*default is a bad one*/)) ;
    try {
      setDataEventEnabled(true); //fire off the next event
    } catch (Exception trash) {
      dbg.ERROR("ErrorEvent occurred: " + jape);
      dbg.Caught(trash);
    }
  }

  public void Attach(String id) {
    fakeit=(Base.Attach(this,this.id=id,DeviceName.PinEntry)!=null);
  }

  public void Release() {
    Base.Release(this);
  }

  public void Flush() throws JposException {
    Base.Flush(this);
  }

  /**
  @param accountNumber is used to seed encryption
  @param amt ditto.
  */
  public void Acquire(int msgcode, CardNumber accountNumber, long amt) { //start DUKPT acquisition
    if(fakeit){
      return;
    }
    try {
      /* from jpos spec:
      <li> Set the EFT transaction properties (AccountNumber, Amount,
      MerchantID, TerminalID, Track1Data, Track2Data, Track3Data and
      TransactionType) and then call the beginEFTTransaction method. This
      will initialize the Device to perform the encryption functions for the EFT
      transaction.
      <li> If PIN Entry is required, call the enablePINEntry method. Then set the
      DataEventEnabled property and wait for the DataEvent.
      <li> If Message Authentication Codes are required, call the computeMAC and
      verifyMAC methods as needed.
      <li> Call the endEFTTransaction method to notify the Device that all operations
      for the EFT transaction have been completed.
      */
      setAccountNumber(accountNumber.Image());
      setAmount(amt*100);//$1.23 => 12300
      setPrompt(msgcode);
      beginEFTTransaction("DUKPT", 1);//1 is try, 3 is try again
      enablePINEntry();
      Base.Acquire(this);
    } catch (JposException jape){
      thePOSapp.Handle(jape);
    }
  }

  public PinEntry(Listener thePOSapp) {
    this.thePOSapp=thePOSapp;
  }

}

//$Id: PinEntry.java,v 1.13 2001/07/19 01:06:50 mattm Exp $
