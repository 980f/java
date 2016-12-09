/* $Id: MICRService.java,v 1.7 2001/07/19 01:06:49 mattm Exp $ */
package net.paymate.ivicm.ec3K;
import net.paymate.jpos.data.MICRData;
import net.paymate.util.*;
import net.paymate.jpos.common.*;

import java.util.*;

import jpos.*;
import jpos.events.*;
import jpos.services.EventCallbacks;
import jpos.services.MICRService14;


public class MICRService extends Service implements MICRService14, MICRConst, Constants, JposConst
{
  static final ErrorLogStream dbg=new ErrorLogStream(MICRService.class.getName());

  static final String VersionInfo = "MICRService (C) PayMate.net 2000, $Revision: 1.7 $";

  MICRData get;  //set upon posting data event

  public MICRService(String s, EC3K hw) {
    super(s, hw);
    identifiers(VersionInfo,Version1dot4,"CM3000 Check Reader");
//    hw.setMiker(this);
    get =new MICRData();
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    get.Clear();
    super.open(s, eventcallbacks);
  }

  public void prepareForDataEvent(Object blob){
    get= (MICRData)blob;
  }

  public void onTimeout() {
    //placeholder.
  }

  public void Post(boolean failed, RcvPacket cmd) {
    MICRData newone=new MICRData();
    try {
      if(failed){
        dbg.ERROR("Bad Packet Received");
        //+++ Post Error.
      } else {
        if(cmd.response()==0){
          TextList micrErrors= newone.Parse(cmd.payload());
          if (micrErrors.size()>0) {
            dbg.WARNING("Micr Parser Errors:\n"+micrErrors.asParagraph());
          }
          PostData( newone);
        } else {
          dbg.ERROR("Device reports: " + cmd.response());
          //+++ Post Error.
        }
      }
    }
    catch (Exception  jape){
      dbg.Caught(jape);
      //+++ Post Error.
    }
  }

  public void beginInsertion(int i) throws JposException {
    assertEnabled();
    Illegal(i < -1, "Invalid Timeout Value");
    //our micr device is always on...
  }

  public void endInsertion() throws JposException {
    assertEnabled(); //to satisfy compiler re throwing something
  }

  public void beginRemoval(int i) throws JposException {
    assertEnabled(); //to satisfy compiler re throwing something
  }
  public void endRemoval() throws JposException {
    assertEnabled(); //to satisfy compiler re throwing something
  }

  public String getAccountNumber() throws JposException {
    assertEnabled();
    return get.Account;
  }

  public String getAmount() throws JposException {
    assertEnabled();
    return get.Amount;
  }

  public String getBankNumber() throws JposException {
    assertEnabled();
    return get.Bank();
  }

  public boolean getCapValidationDevice() throws JposException  {
    assertEnabled();
    return false;
  }

  public int getCheckType()  throws JposException  {
    assertEnabled();
    return get.checktype;
  }

  public int getCountryCode()  throws JposException  {
    assertEnabled();
    return get.country;
  }

  public String getEPC()  throws JposException  {
    assertEnabled();
    return get.EPC;
  }

  public String getRawData()  throws JposException  {
    assertEnabled();
    return get.RawTrack;
  }

  public String getSerialNumber()  throws JposException  {
    assertEnabled();
    return get.Serial;
  }

  public String getTransitNumber()  throws JposException  {
    assertEnabled();
    return get.Transit;
  }

  public void release()  throws JposException {
    endInsertion();
    super.release();
  }

}
//$Id: MICRService.java,v 1.7 2001/07/19 01:06:49 mattm Exp $
