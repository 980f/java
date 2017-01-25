/**
* Title:        SigBox
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: SigBox.java,v 1.6 2001/07/19 01:06:53 mattm Exp $
*/
package net.paymate.terminalClient.IviForm;

import java.awt.Rectangle;
import net.paymate.util.*;

public class SigBox extends FormItem {

  public static Legend signhere;//static is very ugly hack +++ fix

  public SigBox(Rectangle shape) {
    this.shape=new Rectangle(shape);
    String underline=Fstring.fill("X",16,'_');
    signhere=new Legend(shape.x+2,shape.y+shape.height-2,underline,"1");
  }

}
//$Id: SigBox.java,v 1.6 2001/07/19 01:06:53 mattm Exp $
