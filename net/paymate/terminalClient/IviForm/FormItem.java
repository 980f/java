/**
* Title:        FormItem
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: FormItem.java,v 1.6 2001/06/17 00:14:28 andyh Exp $
*/
package net.paymate.terminalClient.IviForm;
import  net.paymate.util.Xml;

import java.awt.Rectangle;

public class FormItem {
  protected Rectangle shape=new Rectangle();

  public int x(){
    return shape.x;
  }

  public int y(){
    return shape.y;
  }

  public int Width(){
    return shape.width;
  }

  public int Height(){
    return shape.height;
  }

  public FormItem moveX(int signed){
    shape.x+=signed;
    return this;
  }

  public int nextY(){
    return shape.y+shape.height;
  }

  public int nextX(){
    return shape.x+shape.width;
  }

  public String xml(){
    return Xml.wrap("SHAPE", Xml.xypair("location",x(),y())+Xml.xypair("dimension",Width(),Height()));
  }

}
//$Id: FormItem.java,v 1.6 2001/06/17 00:14:28 andyh Exp $
