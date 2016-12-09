/**
* Title:        Button
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TextButton.java,v 1.7 2001/06/17 00:14:28 andyh Exp $
*/
package net.paymate.terminalClient.IviForm;
import  net.paymate.util.Xml;
import java.awt.Point;
import java.awt.Rectangle;

public class TextButton extends Button {

  protected Legend legend;

  public Legend Legend(){
    return legend;
  }

  public TextButton(Legend lege, int guid,boolean leftAlign){
    super( //{
      lege.x() - (leftAlign ? 1 : lege.Width()+1),
      lege.y()-1,
      lege.Width()+2,
      lege.Height()+2,
      guid);
      //}
    if(!leftAlign){
      lege.moveX(-lege.Width());
    }
    this.legend= lege;
  }

  public TextButton(Legend lege, int guid){
  //this(lege,guid,true);
    super(lege.x()-1,lege.y()-1,lege.Width()+2,lege.Height()+2,guid);
    this.legend= lege;
  }


  public TextButton(int x, int y, String legend, int guid){//---+_+ probably broken
    this(new Legend(x+1,y+1,legend),guid);
  }

  public TextButton(Point p, String legend, int guid){
    this(p.x,p.y,legend,guid);
  }

  public String xml(){
    return Xml.wrap("TextButton", legend.xml()+super.xml());
  }

}

//$Id: TextButton.java,v 1.7 2001/06/17 00:14:28 andyh Exp $
