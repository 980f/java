/**
* Title:        $Source: /cvs/src/net/paymate/jpos/common/NullForm.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: NullForm.java,v 1.2 2001/09/28 02:10:43 andyh Exp $
*/
package net.paymate.jpos.common;
import java.awt.Point;
import jpos.JposException;

public class NullForm {

  public  void clearFormInput() throws JposException{}
  public  void displayKeyboard() throws JposException{}
  public  void displayKeypad() throws JposException{ }
  public  void displayTextAt(int i, int j, String s) throws JposException{}
  public  void endForm()  throws JposException{ }
  public  boolean getAutoDisable()  throws JposException{  return true;  }
  public  byte[] getButtonData()  throws JposException{ return new byte[0]; }
  public  boolean getCapDisplay()  throws JposException{  return false; }
  public  int getCapPowerReporting()  throws JposException{  return 0; }
  public  boolean getCapRealTimeData() throws JposException{  return false; }
  public  boolean getCapUserTerminated()  throws JposException{ return false; }
  public  int getCols() throws JposException{   return 40; }
  public  int getDataCount()  throws JposException{  return 0; }
  public  boolean getDataEventEnabled()  throws JposException{  return false; }
  public  String getKeyedData()  throws JposException{  return ""; }
  public  int getMaximumX()  throws JposException{ return 0; }
  public  int getMaximumY()  throws JposException{    return 0; }
  public  Point[] getPointArray()  throws JposException{ return new Point[0]; }
  public  Point[] getPointArray(int i) throws JposException{ return new Point[0]; }
  public  int getPowerNotify() throws JposException{   return 0; }
  public  int getPowerState()  throws JposException{    return 0;  }
  public  byte[] getRawData()  throws JposException{    return new byte[0];  }
  public  byte[] getRawScriptData() throws JposException{  return new byte[0]; }
  public  byte[] getRawSigData()  throws JposException{   return new byte[0];  }
  public  boolean getRealTimeDataEnabled()  throws JposException{ return false;}
  public  int getRows()  throws JposException{    return 30;  }
  public  byte[] getSurveyData()  throws JposException{    return new byte[0];  }
  public  void setAutoDisable(boolean flag)  throws JposException{ }
  public  void setDataEventEnabled(boolean flag)  throws JposException{ }
  public  void setFont(int i, int j)  throws JposException{  }
  public  void setKeyboardPrompt(String s)  throws JposException{  }
  public  void setKeypadPrompts(String s, String s1)  throws JposException{  }
  public  void setPowerNotify(int i)  throws JposException{  }
  public  void setRealTimeDataEnabled(boolean flag)  throws JposException{  }
  public  void startForm(String s, boolean flag)  throws JposException{  }
  public  void storeForm(String s)  throws JposException{  }
}
//$Id: NullForm.java,v 1.2 2001/09/28 02:10:43 andyh Exp $
