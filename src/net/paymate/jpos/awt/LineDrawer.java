/**
* Title:        LineDrawer
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LineDrawer.java,v 1.8 2003/12/08 22:45:42 mattm Exp $
*/
package net.paymate.jpos.awt;
import net.paymate.jpos.awt.Math;
import net.paymate.awtx.XPoint;
import  net.paymate.util.ErrorLogStream;

public class LineDrawer {//a line draw using only integers

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LineDrawer.class);

  XPoint adjuster=new XPoint();//+/- 1
  XPoint twiddle= new XPoint();//absolute deltas
  int   Fxy=0;
  int stepCount;
  XPoint cursor=new XPoint();

  public boolean moreSteps(){
    return stepCount>0;
  }

  public XPoint nextStep(){
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
/**
 * sets internal cursor for generating points from @param start
 * you really should call drawTo before drawing anything.
 * @return start
 */

  public XPoint moveTo(XPoint start){
    return cursor=start;
    //loophole: don't reset other parameters.
  }
/**
 * sets internal info for generating points from current location to @param end
 * @return number of steps til end.
 */
  public int drawTo(XPoint end){
    twiddle=Math.Direction(cursor,end);
    adjuster=Math.unit(twiddle);//takes absolute of twiddle!
//now we can use first quadrant reasoning, for the following and for the nextStep()
    Fxy=(twiddle.x-twiddle.y)/2; // /2 round off is trivial
    stepCount=twiddle.x+twiddle.y;
    return stepCount;
  }

}
//$Id: LineDrawer.java,v 1.8 2003/12/08 22:45:42 mattm Exp $
