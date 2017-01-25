/**
* Title:        Math
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Math.java,v 1.3 2001/07/19 01:06:50 mattm Exp $
*/


package net.paymate.jpos.awt;
import java.awt.Point;

public class Math {//will integrate with net.paymate.Math when we have that
  public static final int signum(int signed){
    return signed>0? 1 : signed<0 ? -1 : 0;
  }

  public static final int signum(long signed){
    return signed>0? 1 : signed<0 ? -1 : 0;
  }

  public static final Point unit(Point delta){
    Point retval=new Point();
    retval.x= signum(delta.x);
    retval.y= signum(delta.y);
    if(retval.x<0){
      delta.x=-delta.x;
    }
    if(retval.y<0){
      delta.y=-delta.y;
    }
    return retval;
  }

  public static final Point Direction(Point start,Point end){
    Point retval=new Point();
    retval.x= end.x-start.x;
    retval.y= end.y-start.y;
    return retval;
  }

}
//$Id: Math.java,v 1.3 2001/07/19 01:06:50 mattm Exp $
