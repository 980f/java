/**
* Title:        ReScaler
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ReScaler.java,v 1.11 2001/09/07 03:55:11 andyh Exp $
*/
package net.paymate.jpos.awt;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.lang.Math;

/**
 * class to map from one integer 2-D coordinate space to another.
 */
public class ReScaler {
  public final static float ONE=1.0F;
//these will be 'final'-ized after some more experiments
  public static final boolean preserveAspectRatio=true;
  public static final boolean noZoom=false;
//computed at construction
  protected Point2D.Float scaling= new Point2D.Float(ONE,ONE);//init 4debug
//--- move reflectingY functionality into raster dump...
  protected int reflectY; //Y coordinate polarity inverted awt vs. sigcap
  protected int border=0; //to keep from clipping pixels at extremes of display area
  //separate x and y borders is not worth implementing at this time.
  /**
   * @param p gets modified from present coords to new ones
   */
  public Point remap(Point p){
    p.x *= scaling.x;
    p.x += border;
    p.y *= scaling.y;
    p.y += border;
    if(reflectY>0){
      p.y=reflectY-p.y;
    }
    return p;
  }

  /**
   * @return new target space point computed from
   * @param p const source space point
   */
  public Point Remap(Point p){
    return remap(new Point(p));
  }

  /**
   * @param extreme bounds of the source data
   * @param desired bounds of the target space equivalent
   * @param border squeeze a little extra to leave some whitespace ...
   * @param aspect
   */
  public ReScaler(Point extreme, Dimension desired, int border, Dimension aspect){//mapp one to the other..
    scaling= new Point2D.Float(ONE,ONE); //reset
    if(border<0){
      border=0;
    }

    // NOTE! Extreme of (0,0) will NOT work!
    if(extreme.x == 0) {
      extreme.x = 1;
    }
    if(extreme.y == 0) {
      extreme.y = 1;
    }

    scaling.y = (float)(desired.height-border)/(float)extreme.y;
    scaling.x = (float)(desired.width-border) /(float)extreme.x;

    if(noZoom){
      scaling.x=Math.max(scaling.x,ONE);
      scaling.y=Math.max(scaling.y,ONE);
    }

    if(preserveAspectRatio){//--- should implement float aspectRatio
      float aspectRatio=(float)aspect.getHeight()/(float)aspect.getWidth();
      if (scaling.y>scaling.x*aspectRatio){//keep the one less magnifying.
        scaling.y = scaling.x*aspectRatio;
      } else {
        scaling.x = scaling.y/aspectRatio;
      }
    }
    //reflectY does NOT get affected by border!
    reflectY=desired.height-1;//1st quadrant to 4th quadrant conversion
  }

  public ReScaler(Point extreme, Dimension desired, int border){
    this(extreme,desired,border,new Dimension(1,1));
  }

  public String toString(){
    return "Factors:"+scaling+" reflectY:"+reflectY;
  }

}

//$Id: ReScaler.java,v 1.11 2001/09/07 03:55:11 andyh Exp $
