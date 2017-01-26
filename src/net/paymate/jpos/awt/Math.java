package net.paymate.jpos.awt;

/**
* Title:        $Source: /cvs/src/net/paymate/jpos/awt/Math.java,v $
* Description: some math missing from the Point class
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.8 $
*/


import net.paymate.awtx.XPoint;
import net.paymate.lang.MathX;

public class Math {//will integrate with net.paymate.Math when we have that
  public static final XPoint unit(XPoint delta){
    XPoint retval=new XPoint();
    retval.x= MathX.signum(delta.x);
    retval.y= MathX.signum(delta.y);
    if(retval.x<0){
      delta.x=-delta.x;
    }
    if(retval.y<0){
      delta.y=-delta.y;
    }
    return retval;
  }

  public static final XPoint Direction(XPoint start,XPoint end){
    XPoint retval=new XPoint();
    retval.x= end.x-start.x;
    retval.y= end.y-start.y;
    return retval;
  }

}
//$Id: Math.java,v 1.8 2003/12/08 22:45:42 mattm Exp $
