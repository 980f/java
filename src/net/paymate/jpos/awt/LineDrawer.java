/**
* Title:        LineDrawer
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LineDrawer.java,v 1.4 2000/10/14 04:20:30 mattm Exp $
*/
package net.paymate.jpos.awt;
import net.paymate.jpos.awt.Math;
import java.awt.Point;
import  net.paymate.util.ErrorLogStream;

public class LineDrawer {//a line draw using only integers

  private static final ErrorLogStream dbg = new ErrorLogStream(LineDrawer.class.getName());

  Point adjuster=new Point();//+/- 1
  Point twiddle= new Point();//absolute deltas
  int   Fxy=0;
  int stepCount;
  Point cursor=new Point();

  public boolean moreSteps(){
    return stepCount>0;
  }

  public Point nextStep(){
    if(Fxy>=0){//step x
      cursor.x+=adjuster.x;
      Fxy-=twiddle.y; //yes , y not x
    } else {//step y
      cursor.y+=adjuster.y;
      Fxy+=twiddle.x; //yes , x not y
    }
    --stepCount; //not our problem if this goes negative...
    return cursor; //and the user can screw with our cursor, user beware!!
  }

  public Point moveTo(Point start){
    return cursor=start;
    //loophole: don't reset other parameters.
  }

  public int drawTo(Point end){
    twiddle=Math.Direction(cursor,end);
    adjuster=Math.unit(twiddle);//takes absolute of twiddle!
//now we can use first quadrant reasoning, for the following and for the nextStep()
    Fxy=(twiddle.x-twiddle.y)/2; // /2 round off is trivial
    stepCount=twiddle.x+twiddle.y;
    return stepCount;
  }

}
//$Id: LineDrawer.java,v 1.4 2000/10/14 04:20:30 mattm Exp $
