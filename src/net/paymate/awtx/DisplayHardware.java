package net.paymate.awtx;

/**
 * Title:        $Source: /cvs/src/net/paymate/awtx/DisplayHardware.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.data.*;
import net.paymate.lang.ContentType;

public interface DisplayHardware {
  public void Display(String forDisplay);//display and send keystrokes as they happen
  public boolean doesStringInput(ContentType ct);//does device process key input?
  public boolean hasTwoLines(); //has *at least* two lines
  public void Echo(String forDisplay);//for keystroke echoing on second line

/**
 * only called if doesStringInput() returns true
 */
  public void getString(String prompt,String preload,ContentType ct);

}
//$Id: DisplayHardware.java,v 1.3 2003/07/27 05:34:52 mattm Exp $
