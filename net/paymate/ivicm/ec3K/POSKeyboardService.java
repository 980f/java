/* $Id: POSKeyboardService.java,v 1.6 2001/10/22 23:33:38 andyh Exp $ */

package net.paymate.ivicm.ec3K;
import net.paymate.util.ErrorLogStream;
import net.paymate.jpos.common.*;

import jpos.*;
import jpos.events.*;
import jpos.services.EventCallbacks;
import jpos.services.POSKeyboardService14;


public class POSKeyboardService extends Service implements InputServer, POSKeyboardService14, POSKeyboardConst, Constants, JposConst
{
  static final ErrorLogStream dbg=new ErrorLogStream(POSKeyboardService.class.getName());

  static final String VersionInfo= "CM3000 POSKeybd (C) PayMate.net 2000 $Revision: 1.6 $";

  int KeyStroke;

  public POSKeyboardService(String s, EC3K hw){
    super(s,hw);
    identifiers(VersionInfo,Version1dot4,"EC3K Keyboard");
//    hw.setKeypad(this);
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    KeyStroke = 0;
    super.open(s,eventcallbacks);
  }

  public void prepareForDataEvent(Object blob){
    KeyStroke=((Integer)blob).intValue();
  }

  public void Post(boolean failed, RcvPacket incoming){
    if(!failed){
      if(incoming.response()==STATUS_SUCCESS){
        PostData( new Integer(incoming.bight(4)));
      }
    }
    else {
      PostFailure("ec3k.posky.post.command failed");
    }
  }

  public boolean getCapKeyUp() throws JposException {
    assertClaimed();//we need this for linkage even though it is stupid
    return false;
  }
  public int getEventTypes() throws JposException {
    assertEnabled();
    return KBD_ET_DOWN;
  }

  public int getPOSEventType() throws JposException {
    assertEnabled();
    return KBD_ET_DOWN;
  }

  public int getPOSKeyData() throws JposException {
    assertEnabled();
    return KeyStroke;
  }

  public int getPOSKeyEventType() throws JposException {
    return KBD_KET_KEYDOWN;
  }

  public void setEventTypes(int i) throws JposException {
    assertEnabled();
    Illegal(i != KBD_KET_KEYDOWN,"Only KEY_DOWN available");
  }

}
//$Id: POSKeyboardService.java,v 1.6 2001/10/22 23:33:38 andyh Exp $
