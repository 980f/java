/**
* Title:        ReScaler
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ReScaler.java,v 1.15 2003/12/08 22:45:42 mattm Exp $
*/
package net.paymate.jpos.awt;

import net.paymate.awtx.*;

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
  private XPoint2D.Float scaling= new XPoint2D.Float(ONE,ONE);//init 4debug
//--- move reflectingY functionality into raster dump...
  private XPoint reflect = new XPoint(); //Y coordinate polarity inverted awt vs. sigcap

  private int border=0; //to keep from clipping pixels at extremes of display area
  //separate x and y borders is not worth implementing at this time.
  /**
   * @param p gets modified from present coords to new ones
   */
  public XPoint remap(XPoint p){
    p.x *= scaling.x;
    p.x += border;
    p.y *= scaling.y;
    p.y += border;
    if(reflect.y>0){
      p.y=reflect.y-p.y;
    }
    if(reflect.x>0){
      p.x=reflect.x-p.x;
    }
    return p;
  }

  /**
   * @return new target space point computed from
   * @param p const source space point
   */
  public XPoint Remap(XPoint p){
    return remap(new XPoint(p));
  }

  /**
   * @param extreme bounds of the source data
   * @param desired bounds of the target space equivalent
   * @param border squeeze a little extra to leave some whitespace ...
   * @param aspect
   */
  public ReScaler(XPoint extreme, XDimension desired, int border, XDimension aspect,Quadrant quad){//mapp one to the other..
    scaling= new XPoint2D.Float(ONE,ONE); //reset
    if(border<0){
      border=0;
    }
    reflect.y= quad.YisNegative()? desired.height-1:0;//top-bottom reversal
    reflect.x= quad.XisNegative()? desired.width-1 :0;//potential top-bottom reversal

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
  }

  /**
   * default scaling is fourth quadrant 1:1
   * @param extreme incoming coordinates
   * @param desired pixel coordinates
   * @param border white space around image
   */
  public ReScaler(XPoint extreme, XDimension desired, int border){
    this(extreme,desired,border,new XDimension(1,1),Quadrant.Fourth());
  }

  public String toString(){
    return "Factors:"+scaling+" reflect (x:y):"+reflect.x+':'+reflect.y;
  }

}

//$Id: ReScaler.java,v 1.15 2003/12/08 22:45:42 mattm Exp $
