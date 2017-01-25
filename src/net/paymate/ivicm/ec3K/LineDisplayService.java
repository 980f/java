/* $Id: LineDisplayService.java,v 1.5 2001/07/19 01:06:49 mattm Exp $ */
package net.paymate.ivicm.ec3K;
import net.paymate.util.*;

import net.paymate.ivicm.comm.*;
import java.io.PrintStream;

import javax.comm.CommPortIdentifier;

import jpos.*;
import jpos.events.*;
import jpos.services.EventCallbacks;
import jpos.services.LineDisplayService14;

public class LineDisplayService extends Service implements LineDisplayService14, LineDisplayConst, Constants, JposConst {
  static final ErrorLogStream dbg=new ErrorLogStream(LineDisplayService.class.getName());

  static final String VersionString="CM3000 display, (C) PayMate.net 2000 $Revision: 1.5 $";

  protected final static int iCapBlink=0;
  protected boolean bCapBrightness=false;
  protected int iCapCharacterSet=1;
  protected boolean bCapDescriptors=false;
  protected boolean bCapHMarquee;
  protected boolean bCapICharWait;
  protected boolean bCapVMarquee;
  protected int iDeviceWindows;
  protected int iDeviceRows;
  protected int iDeviceColumns;
  protected int iDeviceDescriptors;
  protected int iDeviceBrightness;
  protected int iCharacterSet;
  protected String sCharacterSetList;
  protected int iCurrentWindow;
  protected boolean bCursorUpdate;
  protected int iMarqueeType;
  protected int iMarqueeFormat;
  protected int iMarqueeUnitWait;
  protected int iMarqueeRepeatWait;
  protected int iInterCharacterWait;

  protected static final int MAX_ROWS = 10;//move to constants?
  protected static final int MAX_COLS = 40;//replace with one from constants

  protected String lastSent;
  protected int iLastBlink;
  protected int iLastStartCol;
  protected int iUnits;

  public LineDisplayService(String s,EC3K hw) {
    super(s,hw);
    identifiers(VersionString,Version1dot4,"EC3K LineDisplay");
    dbg.rawMessage(dbg.VERBOSE,"Creating LDS as "+s);
    iDeviceWindows = 0;
    iDeviceRows = 1;
    iDeviceColumns = 16;
    iDeviceDescriptors = 0;
    bCapHMarquee = true;
    bCapICharWait = true;
    bCapVMarquee = true;
    iCharacterSet = 998;
    sCharacterSetList = "850";
    //iColumns = iDeviceColumns;
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    iCurrentWindow = 0;
    //iCursorColumn = 0;
    //iCursorRow = 0;
    bCursorUpdate = false;
    iDeviceBrightness = 0;
    iInterCharacterWait = 0;
    iMarqueeFormat = 0;
    iMarqueeRepeatWait = 0;
    iMarqueeType = 0;
    iMarqueeUnitWait = 0;
    lastSent = "PayMate.net Display.";
    iLastBlink = -1;
    //iRows = iDeviceRows;
//      createWindow(0, 0, iDeviceRows, iDeviceColumns, 10, 40);
    super.open(s, eventcallbacks);
  }

  public void clearDescriptors() throws JposException {
    Illegal( "Device does not support descriptors");
  }

  public void clearText() throws JposException {
    hardware.Show("                ");
  }

  public void createWindow(int i, int j, int k, int l, int i1, int j1) throws JposException {
    Illegal( "May not create new windows");
  }

  public void destroyWindow() throws JposException {
    Illegal( "May not destroy windows");
  }

  public void displayText(String s, int i) throws JposException {
    displayTextAt(0,0,s,i);
  }

  public void refreshWindow(int i) throws JposException {
    assertWindow0(i);
    displayTextAt(iLastStartCol, 0,lastSent,0);
  }

  public void displayTextAt(int i, int j, String s, int k) throws JposException {
    lastSent=s;
    iLastStartCol=i;
    //why isn't row saved?
    hardware.Show(s);
  }

  protected int ifClaimed(int cap) throws JposException {
    assertClaimed();
    return cap;
  }

  protected boolean ifClaimed(boolean cap) throws JposException {
    assertClaimed();
    return cap;
  }

  protected String ifClaimed(String cap) throws JposException {
    assertClaimed();
    return cap;
  }

  public int ifEnabled(int field) throws JposException {
    assertEnabled();
    return field;
  }

  public boolean ifEnabled(boolean field) throws JposException {
    assertEnabled();
    return field;
  }

  public int getCapBlink() throws JposException {
    return ifClaimed(iCapBlink);
  }

  public boolean getCapBrightness() throws JposException {
    return ifClaimed(bCapBrightness);
  }

  public int getCapCharacterSet() throws JposException {
    return ifClaimed(iCapCharacterSet);
  }

  public boolean getCapDescriptors() throws JposException {
    return ifClaimed(bCapDescriptors);
  }

  public boolean getCapHMarquee() throws JposException {
    return ifClaimed(bCapHMarquee);
  }

  public boolean getCapICharWait() throws JposException {
    return ifClaimed(bCapICharWait);
  }

  public boolean getCapVMarquee() throws JposException {
    return ifClaimed(bCapVMarquee);
  }

  public int getCharacterSet() throws JposException {
    return ifClaimed(iCharacterSet);
  }

  public String getCharacterSetList()throws JposException {
    return ifClaimed(sCharacterSetList);
  }

  public int getColumns() throws JposException {
    return ifEnabled(16);
  }

  public int getCurrentWindow() throws JposException {
    return ifEnabled(iCurrentWindow);
  }

  public int getCursorColumn() throws JposException {
    return ifEnabled(0);
  }

  public int getCursorRow() throws JposException {
   return ifEnabled(0);
  }

  public boolean getCursorUpdate() throws JposException {
    return ifEnabled(bCursorUpdate);
  }

  public int getDeviceBrightness() throws JposException {
    return ifEnabled(iDeviceBrightness);
  }

  public int getDeviceColumns() throws JposException {
    return ifEnabled(iDeviceColumns);
  }

  public int getDeviceDescriptors() throws JposException {
    return ifEnabled(iDeviceDescriptors);
  }

  public int getDeviceRows() throws JposException {
    return ifEnabled(iDeviceRows);
  }

  public int getDeviceWindows() throws JposException {
    return ifEnabled(iDeviceWindows);
  }

  public int getInterCharacterWait() throws JposException {
    return ifEnabled(iInterCharacterWait);
  }

  public int getMarqueeFormat() throws JposException {
    return ifEnabled(iMarqueeFormat);
  }

  public int getMarqueeRepeatWait() throws JposException {
    return ifEnabled(iMarqueeRepeatWait);
  }

  public int getMarqueeType() throws JposException {
    return ifEnabled(iMarqueeType);
  }

  public int getMarqueeUnitWait() throws JposException {
    return ifEnabled(iMarqueeUnitWait);
  }

  public int getRows() throws JposException {
    return ifEnabled(iDeviceRows);//+_+
  }

  protected void assertWindow0(int windownumber)  throws JposException {
    if(windownumber != 0){
      Illegal( "Invalid window number");
    }
  }

  public void scrollText(int i, int j) throws JposException {
    Illegal( "Scrolling Not Implemented");
  }

  public void setCharacterSet(int i) throws JposException {
    iCharacterSet = ifClaimed(i);//cute way to hide test for exception
  }

  public void setCurrentWindow(int i) throws JposException {
    assertWindow0(i);
    iCurrentWindow = ifEnabled(i);
  }

  public void setCursorColumn(int i) throws JposException {
    Illegal("No cursor control");
  }

  public void setCursorRow(int i) throws JposException {
    Illegal("No cursor control");
  }

  public void setCursorUpdate(boolean i) throws JposException {
    bCursorUpdate = ifEnabled(i);
  }

  public void setDescriptor(int i, int j) throws JposException {
    Illegal( "Descriptors not supported");
  }

  public void setDeviceBrightness(int i) throws JposException {
    Illegal( "Brightness may not be modified");
  }

  public void setInterCharacterWait(int i) throws JposException {
    iInterCharacterWait = ifEnabled(i);
  }

  public void setMarqueeFormat(int i) throws JposException {
    iMarqueeFormat = ifEnabled(i);
  }

  public void setMarqueeRepeatWait(int i) throws JposException {
    iMarqueeRepeatWait = ifEnabled(i);
  }

  public void setMarqueeType(int i) throws JposException {
    iMarqueeType = ifEnabled(i);
  }

  public void setMarqueeUnitWait(int i) throws JposException {
    iMarqueeUnitWait = ifEnabled(i);
  }

}
//$Id: LineDisplayService.java,v 1.5 2001/07/19 01:06:49 mattm Exp $
