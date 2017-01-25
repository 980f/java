/* $Id: DEService.java,v 1.3 2000/05/26 01:44:12 alien Exp $ */
package net.paymate.jpos.Terminal;

import jpos.*;
import jpos.events.*;

public interface DEService extends DEListener {//functions present in all interesting jpos services
  //the implements clauses above give us dataOccured() and errorOccured()

  //these could have been in a class, as it is each service reimplements them identically
  public void addDataListener(DataListener callback);
  public void removeDataListener(jpos.events.DataListener callback);
  public void addErrorListener(ErrorListener callback);
  public void removeErrorListener(jpos.events.ErrorListener callback);
  public void setDataEventEnabled(boolean enable) throws jpos.JposException;
  public void clearInput() throws jpos.JposException;

  //the next group usually come from BaseControl:
  public void open(String nameFromActFile) throws JposException;
  public void claim(int millseconds2wait) throws JposException;
  public void setDeviceEnabled(boolean enabled) throws JposException;
  public void release() throws JposException;
  public void close() throws JposException;

}
//$Id: DEService.java,v 1.3 2000/05/26 01:44:12 alien Exp $
