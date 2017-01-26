package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IceDisplayHardware.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */
import net.paymate.serial.*;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.awtx.DisplayHardware;
import net.paymate.lang.StringX;
import net.paymate.lang.ContentType;

public class IceDisplayHardware implements DisplayHardware{
  IceTerminal sterm; //at present ports implement blocking write. they are responsible for queing output
//DisplayHardware
  public void Display(String forDisplay){//display and send keystrokes as they happen
    if(StringX.NonTrivial(forDisplay)){
      IceCommand cmd=IceCommand.Create(2+forDisplay.length());
      cmd.append(IceCommand.KeyInput); cmd.append(IceCommand.Prompt);
      cmd.frame(forDisplay);
      sterm.sendCommand(cmd);
    }
  }
  /**
   * @return command code for setting softkey selection
//from hypercom keypad.c:
//case 'N':  keybd.inmode=numeric;
//case 'A': keybd.inmode=alpha;
//case 'M': keybd.inmode=money;
   */

//  static byte typecode(ContentType ct){
//    switch (ct.Value()) {
//      case ContentType.arbitrary:
//      case ContentType.purealpha:
//      case ContentType.alphanum:
//      case ContentType.password:
//      case ContentType.hex:
//      default:
//        return 'A';
//
//      case ContentType.money:
//      case ContentType.ledger:
//        return 'M';
//
//      case ContentType.decimal:
//      case ContentType.cardnumber:
//      case ContentType.expirdate:
//      case ContentType.micrdata:
//      case ContentType.select:
//        return 'N';
//    }
//  }


  /**
   * while we return false to indicate the hypercom doesn't collect strings,
   * we first tell hypercom the type expected so that it can customize the soft keys
   *
   */
  public boolean doesStringInput(ContentType ct){//does device process key input?
    IceCommand cmd=IceCommand.Create(50);
    cmd.append(IceCommand.KeyInput); cmd.append(Ascii.I);
    cmd.appendFrame("  MENU   Alpha  Erase ");
    cmd.appendNumericFrame(Ascii.ESC,1);
    cmd.appendNumericFrame(Ascii.SI,1);//
    cmd.appendNumericFrame(Ascii.BS,1);//
    cmd.endFrame();//
    sterm.sendCommand(cmd);
    return false;
  }

/**
 * only called if doesStringInput() returns true
 */
  public void getString(String prompt,String preload,ContentType ct){
    ;//empty function
  }
  public boolean hasTwoLines(){ //has *at least* two lines
    return true;
  }
  public void Echo(String forDisplay){//for keystroke echoing on second line
    IceCommand cmd = IceCommand.Create(2 + StringX.lengthOf(forDisplay));
    cmd.append(IceCommand.KeyInput); cmd.append(IceCommand.Echo);
    cmd.frame(forDisplay);
    sterm.sendCommand(cmd);
  }

  public IceDisplayHardware(IceTerminal sterm) {
    this.sterm=sterm;
  }
}
//$Id: IceDisplayHardware.java,v 1.11 2003/07/27 05:35:02 mattm Exp $