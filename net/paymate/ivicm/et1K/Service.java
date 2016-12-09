package net.paymate.ivicm.et1K;

/* $Id: Service.java,v 1.23 2001/10/12 04:11:37 andyh Exp $ */

import net.paymate.ivicm.Base;
import net.paymate.jpos.common.*;
import net.paymate.util.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.EventCallbacks;
import jpos.events.*;

/**
* manage response to ping command
*/

/**
* common parts of enTouch based jpos devices.
*/
public class Service extends Base implements InputServer, JposConst {
  private static final ErrorLogStream dbg=new ErrorLogStream(Service.class.getName());
  static final String VersionInfo = "ET1K Service, (C) Paymate 2000 $Revision: 1.23 $";
  protected ET1K hardware;

  public Service(String s,ET1K hw){
    super(s);
    hardware = hw;
    setState(JPOS_S_CLOSED);
  }

  public void prepareForDataEvent(Object blob){
    //post error!! should be overridden in derived classes if input is actually expected
  }

  public void PostFailure(String s){
    hardware.ClearError();
    super.PostFailure(s);
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    if(hardware==null) {
      Failure("Port Can Not Be Opened");
    }
    super.open(this,s,eventcallbacks);
  }

  protected Command finish(Command cmd, Callback cb){
    if(cmd!=null){//pathc for setMainPoll to null +_+
      cmd.service=this;
      cmd.onReception=cb;
    }
    return cmd;
  }

    /**
   * gotData is only relevant for commands that poll for data
   */
  public boolean gotData(Command cmd){
    int rsp=cmd.response();
    if(cmd.nothingThere(rsp)){//no data present
      return false;
    }
    if(rsp!=0){
      PostFailure(cmd.errorNote+ "PollFailed:"+rsp);
      return false;
    }
    return true;
  }

  protected Command QueueCommand(Command cmd){
    hardware.QueueCommand(cmd);
    return cmd;
  }

  protected Command QueueCommand(Command cmd, Callback cb){
    return QueueCommand(finish(cmd,cb));
  }

  /**
   * sends command now but also saves it for reconnect
   */
  protected Command ConfigurationCommand(Command cmd, Callback cb){
    hardware.setStartup(finish(cmd,cb));
    return cmd;
  }

  protected void Get(int pollcommand,String locus,Callback cb){//fue
    QueueCommand(new Command(pollcommand,locus),cb);
  }

  protected void JustSend(LrcBuffer rawcmd,String forError){//fue
    JustSend(new Command(rawcmd,forError));
  }

  protected void JustSend(Command cmd){//fue
    QueueCommand(cmd,new WantZero(cmd.errorNote));
  }

  protected void Issue(int pollcommand,String locus){//fue
    QueueCommand(new Command(pollcommand,locus),new WantZero(locus));
  }

  public boolean getCapRealTimeData() throws JposException {
    assertOpened();
    return false;
  }

  public boolean getRealTimeDataEnabled() throws JposException {
    assertOpened();
    Illegal("Real time not supported");
    return false;
  }

  public void setRealTimeDataEnabled(boolean flag) throws JposException {
    assertOpened();
    Illegal("Real time not supported");
  }

  protected boolean bCapUserTerminated;

  public boolean getCapUserTerminated() throws JposException {
    assertOpened();
    return bCapUserTerminated;
  }

  public void getVersionInfo(){
    Command gs=new Command(8,"gettingVersionInfo");
    QueueCommand(gs,new VersionInfo());
  }

}
//$Id: Service.java,v 1.23 2001/10/12 04:11:37 andyh Exp $
