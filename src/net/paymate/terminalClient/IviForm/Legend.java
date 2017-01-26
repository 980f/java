package net.paymate.terminalClient.IviForm;
/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/IviForm/Legend.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.15 $
*/

import  net.paymate.util.Xml;
import net.paymate.awtx.*;

public class Legend extends FormItem {
/** square braces in legends get killed by the net.paymate.ivicm forms parser! */
  protected String legend;
  protected Font font;

  public static final Font DefaultFont=new Font();

  public String getText(){
    return legend;
  }

  public int et1kcode(){
    return font.et1k();
  }

  public int code(){
    return font.code();
  }

  public int attr(){
    return font.attr();
  }

  protected void reshape(){
    shape.width=font.Width()*legend.length();
    shape.height=font.Height();
  }

  public void setText(String legend){
    this.legend=legend;
    reshape();
  }

  public void setFont(String fontspec){
    font.parse(fontspec);
    reshape();
  }

  public void setFont(Font afont){
    font=afont;
    reshape();
  }

  public Legend(int x, int y, String legend,Font framis){
    shape.x=x;
    shape.y=y;

    font=framis.Clone();//must copy as source might get modified
    setText(legend);//which computes rest of shape
  }

  public Legend(int x, int y, String legend,String fontspec){
    this(x,y,legend,Font.Create(fontspec));
  }

  public Legend(int x, int y, String legend){
    this(x,y,legend,DefaultFont.Clone());
  }

  public Legend(XPoint p, String legend,String fontspec){
    this(p.x,p.y,legend,fontspec);
  }

  public Legend(XPoint p, String legend){
    this(p.x,p.y,legend,DefaultFont.Clone());
  }

  public Legend(Legend duplicatus){//copy constructor
    shape=duplicatus.shape;
    font =duplicatus.font;
    setText(duplicatus.legend);
  }

  public String xml(){
    return Xml.wrap("LEGEND",Xml.quoted("text",legend)+font.xml()+super.xml() );
  }

}
//$Id: Legend.java,v 1.15 2003/12/08 22:45:43 mattm Exp $
