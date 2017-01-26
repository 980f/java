/**
* Title:        BigButton
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: BigButton.java,v 1.5 2003/12/08 22:45:42 mattm Exp $
*/
package net.paymate.terminalClient.IviForm;

import net.paymate.awtx.*;

public class BigButton extends FormItem {
 protected final static String topline="Touch";
 protected final static String botline="Here!";
 public final static int buttwidth=2+topline.length();
 public final static int butthigh =4;


 protected Button butt;

 protected Legend legend;

 public BigButton(Legend lege, int guid,boolean leftAlign){
    shape=new XRectangle(lege.x(),lege.y()-1,buttwidth + lege.Width(),butthigh);
    butt=new Button ( //{
      lege.x() - (leftAlign ? 0 : buttwidth -1 ),
      lege.y()-1,
      buttwidth,
      butthigh,
      guid);
      //}
    legend= lege;
    legend.moveX(leftAlign? buttwidth +1 :-(lege.Width()+ buttwidth));
  }

  public BigButton(Legend lege, int guid){
    this(lege,guid,true);
  }

  public BigButton(int x, int y, String legend, int guid){
    this(new Legend(x,y,legend),guid);
  }

  public BigButton(XPoint p, String legend, int guid){
    this(p.x,p.y,legend,guid);
  }

  public String xml(){
    return " <BigButton> button="+butt.xml()+" text="+legend.xml()+" </BigButton>";
  }

}
//$Id: BigButton.java,v 1.5 2003/12/08 22:45:42 mattm Exp $
