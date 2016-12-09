/**
* Title:        Stroke
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Stroke.java,v 1.11 2001/07/19 01:06:51 mattm Exp $
*/
package net.paymate.jpos.awt;
import  net.paymate.util.ErrorLogStream;

import java.util.Vector;
import java.awt.Point;
import java.awt.geom.Point2D;


import net.paymate.ivicm.et1K.ncrOstream;

public class Stroke {//awt.Polygon was too complex to mate to our input

  private static final ErrorLogStream dbg = new ErrorLogStream(Stroke.class.getName());

  Vector vertices;

  public static final boolean NonTrivial(Stroke stroke){
    return stroke!=null && stroke.NonTrivial();
  }

  protected boolean NonTrivial(){
    return vertices!=null && vertices.size()>0;
  }

  public boolean legalIndex(int i){
    return i>=0 && i<vertices.size();
  }

  public void addVertex(Point vertex){
    if(vertex!=null){
      vertices.add(vertex);
    }
  }

  public int numVertices(){
    return vertices.size();
  }

  public Point vertex(int i){
    return legalIndex(i)? (Point)vertices.elementAt(i): new Point();
  }

  Stroke(){
    vertices=new Vector();
  }

  public void remapto(ReScaler scalar){
    for(int vi=vertices.size() ;vi-->0;){
      scalar.remap(vertex(vi));
    }
  }

  public Stroke(Stroke rhs){
    vertices=new Vector();
    int size=rhs.vertices.size();
    vertices.ensureCapacity(size);
    for(int vi=size;vi-->0;){
      vertices.add(new Point(rhs.vertex(vi)));
    }
  }

  public void drawinto(Raster raster){
    if(NonTrivial(this)){//at least one point in vector
      LineDrawer Ld=new LineDrawer();
      int vi=vertices.size();
      raster.set(Ld.moveTo(vertex(--vi)),true);

      while(vi-->0){
        Ld.drawTo(vertex(vi));
        while(Ld.moreSteps()){
          raster.set(Ld.nextStep(),true);
        }
      }
    }
  }

  /**
  * this version DEPENDS upon the data having been sourced from NCRA and not
  * optimized thereafter.
  */

  public void toNCRA(ncrOstream packer){
    if(NonTrivial()){
      int vi=numVertices();//vi==ncr.strokeLength
      packer.writeHilo(vi|0x8000);//set msbit to indicate format of next two fields

      Point prev=vertex(--vi);
      //coordinates in low high order
      packer.writeLohi(prev.y);
      packer.writeLohi(prev.x);

      while(vi-->0){
        Point current=vertex(vi);
        packer.writeDiff(current.y,prev.y);
        packer.writeDiff(current.x,prev.x);
        prev=current;
      }
    }
  }

}
//$Id: Stroke.java,v 1.11 2001/07/19 01:06:51 mattm Exp $
