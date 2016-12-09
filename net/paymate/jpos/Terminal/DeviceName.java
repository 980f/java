/**
* Title:        DeviceName
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DeviceName.java,v 1.7 2001/07/19 01:06:50 mattm Exp $
*/
package net.paymate.jpos.Terminal;

public class DeviceName {
  public final static String CardReader = "MSR"   ;
  public final static String PinEntry   = "PINPad";
  public final static String FormEntry  = "Form"  ;
  public final static String RawPrinter = "LinePrinter"  ;
  public final static String ReceiptPrinter = "P612"  ;

  public final static String CheckReader= "MICR"  ;
  public final static String Keyboard   = "POSKeyboard";
  public final static String LineDisplay= "LineDisplay";

  public static final String fullname(String id,String deviceName){
    return id+'.'+deviceName;
  }

}
//$Id: DeviceName.java,v 1.7 2001/07/19 01:06:50 mattm Exp $
