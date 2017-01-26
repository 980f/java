package net.paymate.terminalClient.IviForm;

/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/IviForm/FormItem.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: FormItem.java,v 1.8 2003/12/08 22:45:43 mattm Exp $
*/

import net.paymate.util.Xml;
import net.paymate.awtx.*;

public class FormItem {
  protected XRectangle shape=new XRectangle();

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
//$Id: FormItem.java,v 1.8 2003/12/08 22:45:43 mattm Exp $
