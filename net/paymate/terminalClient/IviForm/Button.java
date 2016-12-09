/**
* Title:        Button
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Button.java,v 1.8 2001/06/17 00:14:28 andyh Exp $
*/
package net.paymate.terminalClient.IviForm;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;

public class Button extends FormItem {
  public int guid;

  public Button(Rectangle outline,int guid){
    this.guid=guid;
    shape= new Rectangle(outline);//copy for comfort.
  }

  public Button(int x, int y, int w, int h, int guid){
    this.guid=guid;
    shape= new Rectangle(x,y,w,h);
  }

  public Button(Point topleft, Dimension bigness, int guid){
    this(topleft.x,topleft.y,bigness.width,bigness.height,guid);
  }

  public String xml(){
    return " <BUTTON> id="+guid+super.xml()+" </BUTTON>";
  }

}
//$Id: Button.java,v 1.8 2001/06/17 00:14:28 andyh Exp $
