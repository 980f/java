/**
* Title:        $Source: /cvs/src/net/paymate/jpos/awt/Stroke.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Stroke.java,v 1.18 2003/12/08 22:45:42 mattm Exp $
*/
package net.paymate.jpos.awt;
import  net.paymate.util.*;
import java.util.Vector;
import net.paymate.awtx.XPoint;


import net.paymate.ivicm.et1K.ncrOstream;

public class Stroke implements isEasy,EasyHelper{ //awt.Polygon was too complex to mate to our input

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Stroke.class);

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

  public void addVertex(XPoint vertex){
    if(vertex!=null){
      vertices.add(vertex);
    }
  }

  public int numVertices(){
    return vertices.size();
  }

  public XPoint vertex(int i){
    return legalIndex(i)? (XPoint)vertices.elementAt(i): new XPoint();
  }

  public Stroke(){
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
      vertices.add(new XPoint(rhs.vertex(vi)));
    }
  }

  public void drawinto(Raster raster){
    boolean nottriv = NonTrivial(this); //at least one point in vector
    dbg.VERBOSE("stroke is " + (nottriv ? "not " : "") + "trivial");
    if(nottriv){
      LineDrawer Ld=new LineDrawer();
      int vi=vertices.size();
      dbg.VERBOSE("stroke has " + vi + " vertices");
      if(true /* +++ */){
        for(int i = vi; i-->0;) {
          dbg.VERBOSE("point is "+vertex(i));
        }
      }
      raster.set(Ld.moveTo(vertex(--vi)),true);
      while(vi-->0){
        Ld.drawTo(vertex(vi));
        while(Ld.moreSteps()){
          raster.set(Ld.nextStep(),true);
        }
      }
    }
  }

  public void helpsave(EasyCursor ezc,Object point){
    if(point instanceof XPoint){
      ezc.setInt ("X",((XPoint)point).x);
      ezc.setInt ("Y",((XPoint)point).y);
    } else {
      dbg.WARNING("helpsave(): NOT a Point object!");
    }
  }

  public Object helpload(EasyCursor ezc,Object point){
    if(point instanceof XPoint){
      ((XPoint)point).setLocation(ezc.getInt("X"),ezc.getInt("Y"));
      dbg.VERBOSE("got point " + (XPoint)point);
    } else {
      dbg.WARNING("helpload(): NOT a Point object!");
    }
    return point;
  }

  public void save(EasyCursor ezc){
    ezc.setVector(this.vertices,this);
  }

  public void load(EasyCursor ezc){
    vertices=ezc.getVector(XPoint.class,this);
  }

  /**
  * this version DEPENDS upon the data having been sourced from NCRA and not
  * optimized thereafter.
  */

  public void toNCRA(ncrOstream packer){
    if(NonTrivial()){
      int vi=numVertices();//vi==ncr.strokeLength
      packer.writeHilo(vi|0x8000);//set msbit to indicate format of next two fields

      XPoint prev=vertex(--vi);
      //coordinates in low high order
      packer.writeLohi(prev.y);
      packer.writeLohi(prev.x);

      while(vi-->0){
        XPoint current=vertex(vi);
        packer.writeDiff(current.y,prev.y);
        packer.writeDiff(current.x,prev.x);
        prev=current;
      }
    }
  }

}
//$Id: Stroke.java,v 1.18 2003/12/08 22:45:42 mattm Exp $
