/**
* Title:        Button
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Button.java,v 1.10 2003/12/08 22:45:42 mattm Exp $
*/
package net.paymate.terminalClient.IviForm;
import net.paymate.awtx.*;

public class Button extends FormItem {
  public int guid;

  public Button(XRectangle outline,int guid){
    this.guid=guid;
    shape= new XRectangle(outline);//copy for comfort.
  }

  public Button(int x, int y, int w, int h, int guid){
    this.guid=guid;
    shape= new XRectangle(x,y,w,h);
  }

  public Button(XPoint topleft, XDimension bigness, int guid){
    this(topleft.x,topleft.y,bigness.width,bigness.height,guid);
  }

  public String xml(){
    return " <BUTTON> id="+guid+super.xml()+" </BUTTON>";
  }

}
//$Id: Button.java,v 1.10 2003/12/08 22:45:42 mattm Exp $
