/**
* Title:        SigBox
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: SigBox.java,v 1.8 2003/12/08 22:45:43 mattm Exp $
*/
package net.paymate.terminalClient.IviForm;

import net.paymate.awtx.*;
import net.paymate.util.*;
import net.paymate.lang.Fstring;

public class SigBox extends FormItem {

  public static Legend signhere;//static is very ugly hack +++ fix

  public SigBox(XRectangle shape) {
    this.shape=new XRectangle(shape);
    String underline=Fstring.fill("X",16,'_');
    signhere=new Legend(shape.x+2,shape.y+shape.height-2,underline,"1");
  }

}
//$Id: SigBox.java,v 1.8 2003/12/08 22:45:43 mattm Exp $
