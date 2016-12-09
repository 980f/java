package net.paymate.jpos.Terminal;
/**
* Title:        CardReader
* Description:  jpos.MSR
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CardReader.java,v 1.18 2001/11/15 03:15:45 andyh Exp $
*/

import net.paymate.jpos.data.*;
import net.paymate.util.*;

import jpos.*;
import jpos.events.*;

public class CardReader extends jpos.MSR implements DEService  {
  static final ErrorLogStream dbg=new ErrorLogStream(CardReader.class.getName());
  boolean fakeit=true;
  protected Listener thePOSapp;

  String id;
  public String toString(){
    return id;
  }

  public void dataOccurred (DataEvent e) {//no throws allowed
    //+_+ presume it is the only one we have registered for
    try {
      dbg.Enter("CardReader.dataOccurred");
      thePOSapp.Handle(new CardSwiped(Value()));
    } catch (JposException jape) {//must be caught
      thePOSapp.Handle(jape);
    } finally {
      try {
        setDataEventEnabled(true); //fire off the next event
      } catch (Exception trash) {
        dbg.Caught(trash);
      } finally {
        dbg.Exit();
      }
    }
  }

  public void errorOccurred(jpos.events.ErrorEvent jape){
    try {
  //    thePOSapp.Handle(jape);
      setDataEventEnabled(true); //fire off the next event
    } catch (Exception trash) {
      dbg.ERROR("ErrorEvent occurred: " + jape);
      dbg.Caught(trash);
    }
  }

/**
 *  @return collated fields from card
 */
  public MSRData Value() throws JposException {
    MSRData blob = new MSRData();

//    blob.accountNumber.setto(getAccountNumber());
//    blob.expirationDate.parseYYmm(getExpirationDate());
//    blob.ServiceCode=getServiceCode();
//    blob.person=Person.jposParsed(getTitle(),getFirstName(),getMiddleInitial(),getSurname(),getSuffix());

    blob.setTrack(blob.T1,TrackData.asString(getTrack1Data()));
    blob.setTrack(blob.T2,TrackData.asString(getTrack2Data()));
    //track 3 hijacked for swipetime
    blob.setTrack(blob.T3,TrackData.asString(getTrack3Data()));
    blob.beenPresented(true);
    return blob;
  }

  public JposException Attach(String id) {
    try {
      fakeit=(Base.Attach(this,this.id=id,DeviceName.CardReader)!=null);
    } catch (Exception e) {
      dbg.ERROR("Exception occurred attempting to attach to the card reader!");
      dbg.Caught(e);
    }
    if(!fakeit){
      try {
        //we will never let the driver parse. we wish to discover type of card, THEN parse
        setDecodeData(false);
        setParseDecodeData(false);
        setTracksToRead(MSRConst.MSR_TR_1_2);
      } catch (JposException jape){
        dbg.ERROR("WhileAttaching:"+jape);
        return jape;
      }
    }
    return null;
  }

  public void Release() {
    Base.Release(this);
  }

  public JposException Flush() {
    return Base.Flush(this);
  }

  /**
   */
  public void Acquire() {
    if(fakeit){
      return;
    }
    Base.Acquire(this);
  }

  public CardReader(Listener thePOSapp) {
    this.thePOSapp=thePOSapp;
  }

}

//$Id: CardReader.java,v 1.18 2001/11/15 03:15:45 andyh Exp $
