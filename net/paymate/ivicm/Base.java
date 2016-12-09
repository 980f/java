/* $Id: Base.java,v 1.11 2001/11/15 03:15:44 andyh Exp $ */
package net.paymate.ivicm;

import net.paymate.jpos.common.*;
import net.paymate.util.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import jpos.*;
import jpos.events.*;
import jpos.services.*;

public abstract class Base implements BaseService, JposConst {
  static final ErrorLogStream dbg=new ErrorLogStream(Base.class.getName());

  static final String VersionInfo="Jpos Base, (C) PayMate.net 2000 $Revision: 1.11 $";

  //set in constructors via 'identifiers'
  private String ServiceDescription="Unknown!";
  private int ServiceVersion=0;
  private String PhysicalDescription="Unknown!";
  //set in constructor
  //abusing physDeviceName, tying to logical one as the device does not have a serial number
  private String InstanceName="Unknown!";

  protected String myName;

  protected void identifiers(String DSD,int vernum,String PDD){
    ServiceDescription=DSD;
    ServiceVersion=vernum;
    PhysicalDescription=PDD;
  }
//jpos properties
  protected boolean AutoDisable;
  protected boolean Claimed;
  protected boolean DataEventEnabled;
  protected boolean DeviceEnabled;
  protected boolean FreezeEvents;

  protected String CheckHealthText;
  protected int PowerNotify;
  protected int PowerState;

  protected int State;

  protected int DataCount=0;

  ////////////////////////////////////////////////////////////////////
  protected EventQueuer evq;

//  public void PostExtended(int extcode){
//    evq.Post(new ErrorEvent(this,  //{
//      (extcode==0? JPOS_E_FAILURE:JPOS_E_EXTENDED),
//      extcode,
//      (DataCount <= 0 ? JPOS_EL_INPUT : JPOS_EL_INPUT_DATA),
//      0)
//    ); //}
//  }

  public void PostFailure(String s){
    evq.PostFailure(s);
  }

  public void PostData(Object obj){
    try {
      dbg.Enter("PostData");
      evq.Post(new DataEvent(this,0), obj);
      if(AutoDisable){
        dbg.VERBOSE("auto disabling ");
        setDataEventEnabled(false);
      }
    }
    catch(JposException jape){
      PostFailure("Posting Data for "+InstanceName+" got "+jape);
    }
    catch(Exception arful){
      dbg.ERROR(arful+" posting a "+obj.getClass().getName()+"=="+obj.toString());
      dbg.Caught(arful);
    } finally {
      dbg.Exit();
    }
  }

  ////////////////////////////////////////////////////////////////////
  /* std interface to all services */
  public synchronized void claim(int timeout) throws JposException {
    if(!Claimed && State != JPOS_S_CLOSED) {
      Claimed = true;
      State = JPOS_S_IDLE;
    }
    else {
    //we are not cooperative and so we do not implement timeout
      throw new JposException(JPOS_E_CLAIMED, "Device already claimed");
    }
  }

  public void doOpenInits() {
    DataCount = 0;
    Claimed = false;
    DeviceEnabled = false;
    FreezeEvents = false;
    DataEventEnabled = false;
    AutoDisable = false;
    setState(JPOS_S_IDLE);
  }

  public void clearInput() throws JposException {
    assertClaimed();
    evq.Clear();
  }

  public synchronized void close() throws JposException {
    if(getClaimed()) {
      release();
    }
    try {
      setState(JPOS_S_CLOSED);
    }
    catch(Exception exception) {
      Failure("Can not close port", exception);
    }
  }

  ////////////////////////////////////////////////////////////////////
  /*
  common error condition management
  */

  protected void assertInit() throws JposException {
    if(ServiceDescription == null) {//we always set this string when we construct a service object
      throw new JposException(JPOS_E_NOSERVICE, "Service Description not available");
    }
  }

  protected void assertOpened() throws JposException {
    if(State == JPOS_S_CLOSED){
      throw new JposException(JPOS_E_CLOSED, "Device is closed");
    }
  }

  protected void assertClaimed() throws JposException {
    if(!getClaimed()){
      throw new JposException(JPOS_E_NOTCLAIMED, "Device is not claimed");
    }
  }

  protected void assertEnabled() throws JposException {
    if(!getDeviceEnabled()) {
      throw new JposException(JPOS_E_DISABLED, "Device is disabled");
    }
  }

  public static final void Illegal(String comment) throws JposException {
    throw new JposException(JPOS_E_ILLEGAL, comment);
  }

  public static final void Illegal(boolean expression, String comment) throws JposException {
    if(expression){
      Illegal(comment);
    }
  }

  public static final void NotImplemented(String nyi) throws JposException {
    Illegal(nyi+" Not Implemented");
  }

  public static final void Failure(String comment) throws JposException {
    throw new JposException(JPOS_E_FAILURE, comment);
  }

  //  public static final void Failure(Exception exception) throws JposException {
    //   throw new JposException(JPOS_E_FAILURE, exception);
  // }

  public static final void Failure(String comment, Exception exception) throws JposException {
    throw new JposException(JPOS_E_FAILURE, comment, exception);
  }

  ////////////////////////////////////////////////////////////////////
  /*
  common Properties, defaults that can be overridden.
  */


  public boolean getAutoDisable() throws JposException {
    assertOpened();
    return AutoDisable;
  }

//    Illegal(iCapPowerReporting == 0, "Device does not support power reporting.");
  public int getCapPowerReporting() throws JposException {
    assertOpened();
    return JPOS_PR_NONE;
  }

  public String getCheckHealthText() throws JposException {
    assertOpened();
    return CheckHealthText;
  }

  public boolean getClaimed() throws JposException {
    assertOpened();
    return Claimed;
  }

  public int getDataCount() throws JposException {
    assertOpened();
    return DataCount;
  }

  public boolean getDataEventEnabled() throws JposException {
    assertOpened();
    return DataEventEnabled;
  }

  public boolean getDeviceEnabled() throws JposException {
    assertOpened();
    return DeviceEnabled;
  }

  public boolean getFreezeEvents() throws JposException {
    assertOpened();
    return FreezeEvents;
  }

  public int getPowerNotify() throws JposException {
    Illegal("Device does not support power reporting.");
    return JPOS_PN_DISABLED; //pro forma, compiler insists on a return
  }

  public int getPowerState() throws JposException {
    Illegal("Device does not support power reporting.");
    return JPOS_PS_UNKNOWN;
  }

  public int getState() {
    return State;
  }

  public void release() throws JposException {
    assertClaimed();
    if(DeviceEnabled) {
      setDeviceEnabled(false);
    }
    Claimed = false;
    setState(JPOS_S_IDLE);//available to be claimed
  }

  public void resetDataCount() {
    DataCount = 0;
  }

  public void setAutoDisable(boolean flag) throws JposException {
    assertOpened();
    AutoDisable = flag;
  }

  public void setDataEventEnabled(boolean flag) throws JposException {
    assertOpened();
    DataEventEnabled = flag;
    evq.TurnMe(DataEventEnabled);
  }

  public synchronized void setDeviceEnabled(boolean beEnabled) throws JposException {
    assertClaimed();
    DeviceEnabled = beEnabled;
    if(!DeviceEnabled){
      setDataEventEnabled(false);
    }
    setState(DeviceEnabled? JPOS_S_BUSY : JPOS_S_IDLE);
  }

  public synchronized void setFreezeEvents(boolean beEnabled) throws JposException {
    assertOpened();
    FreezeEvents = beEnabled;
    //evq.Freezing(FreezeEvents);
  }

  public void setPowerNotify(int i) throws JposException {
    Illegal("PowerNotification not supported");
  }

  protected void setState(int i){
    State = i;
  }

  public String toString(){
    return InstanceName; //defined for used by servicetracker!
  }

  public void vSetDataCount(int i) {
    DataCount += i;
  }

  public void SetHealthText(String s){//???protected
    CheckHealthText = s;
  }

  public void checkHealth(int i) throws JposException {//dummy implementation
    switch(i) {
      default:  Illegal("Invalid checkHealth value:"+i);// break;
      case JPOS_CH_INTERNAL: SetHealthText("Internal Health Check not implemented");  break;
      case JPOS_CH_EXTERNAL: SetHealthText("External Health Check not implemented");  break;
      case JPOS_CH_INTERACTIVE: SetHealthText("Interactive Health Check not implemented"); break;
    }
  }

  public String getPhysicalDeviceDescription() throws JposException {
    assertInit();
    return PhysicalDescription;
  }

  public String getPhysicalDeviceName() throws JposException {
    assertInit();
    return InstanceName;
  }

  public String getDeviceServiceDescription() throws JposException {
    assertInit();
    return ServiceDescription;
  }

  public int getDeviceServiceVersion() throws JposException {
    assertInit();
    return ServiceVersion;
  }

  public Base(String s){
    myName=InstanceName=s;
    evq=new EventQueuer();
    setState(JPOS_S_CLOSED);
  }
  ////////////////////////////////////////////////////////////////
  protected void open(InputServer service, String s, EventCallbacks eventcallbacks) throws JposException {
    myName=s;
    if(service==null){
      Illegal("null service error opening "+myName);
    } else {
      evq.Attach(service,eventcallbacks);
      doOpenInits();
    }
  }

  public void directIO(int i, int ai[], Object obj) throws JposException {
    Illegal("DirectIO not supported by this device");
  }

}
//$Id: Base.java,v 1.11 2001/11/15 03:15:44 andyh Exp $
