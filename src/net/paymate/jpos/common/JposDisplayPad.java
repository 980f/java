package net.paymate.jpos.common;

import net.paymate.lang.ContentType;
import net.paymate.awtx.DisplayHardware;
import net.paymate.util.QReceiver;
import jpos.JposException;
import jpos.LineDisplay;
import jpos.POSKeyboard;

/** misnamed, should be JposDisplayHardware
 * Created by: andyh
 * Date: Mar 1, 2005   10:30:28 PM
 * (C) 2005 hal42
 */
public class JposDisplayPad  implements DisplayHardware {

  LineDisplay display = null;
  POSKeyboard keypad = null;
  QReceiver parent = null; //for negative events.
  protected void post(Object obj) {
    if(parent != null) {
      parent.Post(obj);
    } else {
      System.err.println(String.valueOf(obj));
      if(obj instanceof Throwable) {
        Throwable error = (Throwable) obj;
        error.printStackTrace(System.err);
      }
    }
  }

  public JposDisplayPad(LineDisplay display, POSKeyboard keypad, QReceiver parent) {
    this.display = display;
    this.keypad = keypad;
    this.parent = parent;
  }

  ///////////////////////
  public void Display(String forDisplay) { //display and send keystrokes as they happen
    if(display != null) {
      try {
        display.displayTextAt(1, 1, forDisplay, 0);
      } catch(JposException e) {
        post(e);
      }
    }
  }

  public boolean doesStringInput(ContentType ct) { //does device process key input?
    return false;
  }

  public boolean hasTwoLines() { //has *at least* two lines
    try {
      return display != null && display.getDeviceRows() > 1;
    } catch(JposException e) {
      post(e);
      return false;
    }
  }

  public void Echo(String forDisplay) { //for keystroke echoing on second line
    if(display != null) {
      try {
        display.displayTextAt(2, 1, forDisplay, 0);
      } catch(JposException e) {
        post(e);
      }
    }
  }

  public void getString(String prompt, String preload, ContentType ct) {
    //null since we say false to doesStringInput();
  }

}
