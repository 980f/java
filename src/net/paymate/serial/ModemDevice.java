package net.paymate.serial;
/**
 * Title:        $Source: /cvs/src/net/paymate/serial/ModemDevice.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

import net.paymate.ivicm.*;
import net.paymate.util.*;

public abstract class ModemDevice extends SerialDevice {
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(ModemDevice.class, ErrorLogStream.WARNING);

  // need ModemConfig class
  // need function for pulling those from properties
  // need to convert that stuff to AT commands.

  // +++ Need a class (enumeration?) that defines all of the standard AT commands!

  public ModemDevice(String name4debug){
    super(name4debug);//legacy instance name goes here.
  }

  /**
   * No need to put AT in front of it or CRLF on the end!
   */
  public void ATcommand(String cmd) {
    myPort.lazyWrite(AT);
    myPort.lazyWrite(cmd.getBytes());
    myPort.lazyWrite(CRLF);
  }
  // CR or LF alone are not sufficient; must send CRLF!
  public static final byte [] CRLF = { Ascii.CR /*0x0D*/, Ascii.LF /*0x0A*/, };
  private static final byte [] AT = {Ascii.A /*'A'*/, Ascii.T /*'T'*/,};

  ///////////////////////////
  // For testing only ...

  // NOTE: Modem commands require a CRLF on the end.  HOWEVER, once you have connected and entered data mode, this rule may not apply!
  public void Display(String forDisplay){
    myPort.lazyWrite(forDisplay.getBytes());
    myPort.lazyWrite(CRLF);
    dbg.VERBOSE("ModemDevice sent:"+forDisplay);
  }

}
//$Id: ModemDevice.java,v 1.11 2003/03/19 06:13:13 mattm Exp $
