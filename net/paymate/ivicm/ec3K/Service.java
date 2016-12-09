/* $Id: Service.java,v 1.6 2001/07/19 01:06:49 mattm Exp $ */
package net.paymate.ivicm.ec3K;
import net.paymate.ivicm.Base;

import net.paymate.jpos.common.*;
import net.paymate.jpos.data.MICRData;
import net.paymate.util.ErrorLogStream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.EventCallbacks;

public class Service extends Base implements InputServer, JposConst {
  static final ErrorLogStream dbg=new ErrorLogStream(Service.class.getName());

  static final String VersionInfo = "EC3K Service (C) PayMate.net 2000 $Revision: 1.6 $";

  protected EC3K hardware;

  public Service(String s, EC3K hw){
    super(s);
    hardware = hw;
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    if(hardware==null) {
      throw new JposException(JPOS_E_FAILURE, "EC3K Service "+s+" Can Not Be Opened",null);
    }
    super.open(this,s,eventcallbacks);
  }

//dummy handlers
  public void prepareForDataEvent(Object blob){
    dbg.WARNING("DataEvents Not Supported by "+myName);
  }

  public void Post(Command cmd) {
    dbg.WARNING("Command Response Ignored by "+myName);
  }

}
//$Id: Service.java,v 1.6 2001/07/19 01:06:49 mattm Exp $
