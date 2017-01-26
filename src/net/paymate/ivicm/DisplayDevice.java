package net.paymate.ivicm;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/DisplayDevice.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.lang.ReflectX;
import net.paymate.lang.ContentType;

public abstract class DisplayDevice extends SerialDevice {
  public boolean simulating;
  protected QReceiver keypadif; //on keystroke received pass it to this guy.

  public void setKeyListener(QReceiver keypadif){
    this.keypadif=keypadif;
  }

//////////////

  public DisplayDevice(String id) {
    super(id);
  }

  public void getString(String prompt,String preload){
    System.err.println(ReflectX.shortClassName(this)+ "Failed to overload getString");
  }

  public void getString(String prompt,String preload,ContentType ct){
    System.err.println(ReflectX.shortClassName(this)+ "Failed to overload getString(..type)");
  }

}
//$Id: DisplayDevice.java,v 1.4 2003/07/27 05:35:03 mattm Exp $