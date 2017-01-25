package net.paymate.jpos.awt;
/**
* Title:        Hancock
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Hancock.java,v 1.26 2001/10/18 05:33:03 andyh Exp $
*/

import net.paymate.jpos.data.*;
import net.paymate.util.*;

import java.awt.geom.Point2D;
import java.awt.Point;  // please don't use * here; I need to see which things are getting loaded
import java.awt.Dimension;
import java.lang.Math;
import java.util.*;
import java.io.*;

import net.paymate.ivicm.et1K.ncrOstream;//tyring to push bakc into jpos.data package
import net.paymate.ivicm.et1K.ncraSignature;//tyring to push bakc into jpos.data package

public class Hancock implements isEasy {
  static Tracer dbg=new Tracer(Hancock.class.getName());

  Vector strokes;
  Point extreme;

  SigData jpossig;
  Hancock setSig(SigData siggy){
    this.jpossig=SigData.Clone(siggy);
    return this;
  }

  private boolean haveStrokes(){
    return strokes!=null && strokes.size()>0;
  }

  private void parseIfNeeded(){
//    dbg.VERBOSE("parse if needed");
    if(!haveStrokes()){
      if(!SigData.isTrivial(jpossig)){
//        dbg.VERBOSE("parsing jpos signature");
        parse(jpossig.Signature());
      }
    }
  }

  public Hancock(){
    extreme=new Point(0,0);
    strokes=new Vector();
  }

  public Hancock(Hancock rhs){
    extreme=new Point(rhs.extreme);
    //inline copy, cloning didn't work out as expected
    strokes=new Vector();
    int size=rhs.strokes.size();
    strokes.ensureCapacity(size);
    for(int si=size;si-->0;){
      strokes.add(new Stroke(rhs.stroke(si)));
    }
  }

  public boolean NonTrivial(){
//    dbg.mark("at NonTrivial test");
    if(!SigData.isTrivial(jpossig)){
      return true;
    }
    parseIfNeeded();
    return haveStrokes();
  }

  public static final boolean NonTrivial(Hancock probate){
//    dbg.VERBOSE("probate is "+probate);
    return probate!=null && probate.NonTrivial();
  }

  public boolean legalIndex(int i){
    return i>=0 && i<strokes.size();
  }

  public void addStroke(Stroke stroke){
    if(stroke!=null && stroke.vertices!=null && stroke.vertices.size()>0){
      strokes.add(stroke);
    }
  }

  public int numStrokes(){
//    dbg.mark("at num strokes test");
    parseIfNeeded();
    return strokes.size();
  }

  public Stroke stroke(int i){
    return legalIndex(i)? (Stroke)strokes.elementAt(i): new Stroke();
  }

  //////////////////////////////
  // parse jpos nonsense
  /**
  * bottom left corner is relocated to origin.
  * is this required ??? YES, expected by our rescalig code.
  * @return the maximum
  */
  public static final Point MARK=new Point(-1, -1);
//  public
  static final Point makePositive(Point [] Points){
    Point extreme = new Point(0,0);

    if(Points.length>1){//else it is trivial and doesn't need offsetting
      Point origin= new Point(Points[0]);//a point known to NOT be a mark, so long as WE source the data.

      for(int i = Points.length;i-->1;) {//0 is used to prime the search
        Point p=Points[i];
        if(!p.equals(MARK)){
          extreme.x=Math.max(extreme.x,p.x);
          extreme.y=Math.max(extreme.y,p.y);
          origin.x= Math.min(origin.x,p.x);
          origin.y= Math.min(origin.y,p.y);
        }
      }
//      dbg.VERBOSE("origin:"+origin+" extreme:"+extreme);

      origin.x= -origin.x;
      origin.y= -origin.y;

      for(int i = Points.length; i-->0;){
        Point p=Points[i];
        if(!p.equals(MARK)){
          p.translate(origin.x,origin.y);
        }
      }
      extreme.translate(origin.x,origin.y);
//      dbg.VERBOSE("translated extreme:"+extreme);
    }
    return extreme;
  }


  /** convert string of points some of which are special marker values into
  a vector of vectors of points */
  private Hancock parse(Point [] sig){
//    dbg.mark("parse(Point[])");
    extreme = makePositive(sig);
//    dbg.VERBOSE("parsed extreme:"+extreme);
    if(sig != null) {
      Stroke stroke=new Stroke();
      for(int i=0;i<sig.length;i++){
        Point vertex=sig[i];
        if(vertex.equals(Signature.MARK)){
          addStroke(stroke);
          stroke=new Stroke();//do NOT reuse a stroke, unless addStroke is changed to make copies
        } else {
          stroke.addVertex(vertex);
        }
      }
      addStroke(stroke);//for when sig does NOT have a trailing MARK!
    }
    return this;
  }

  /** modify stored items so that their coords are compatible with some rectangle  */
  public Hancock remapped(Dimension preferred,Dimension aspect){
    dbg.Enter("remapped");
    try {
      parseIfNeeded();
//      dbg.VERBOSE("extreme:"+extreme+ " preferred:"+preferred);
      ReScaler scaler=new ReScaler(extreme,preferred,1/*pixels of border*/,aspect);
//      dbg.VERBOSE("scaling thingy:"+scaler);
      Hancock morphed=new Hancock(this); //copies objects, not references

      for(int si=strokes.size();si-->0;){
        morphed.stroke(si).remapto(scaler);
      }
      scaler.remap(morphed.extreme);
//      dbg.VERBOSE("remapped extreme:"+morphed.extreme);
      return morphed;
    }
    finally {
      dbg.Exit();
    }
  }

  public Raster rasterTo(Dimension preferred){//already cooked
    dbg.mark("at rasterTo");
    parseIfNeeded();
    Raster raster=new Raster(preferred);//during debug this is the same as 'this' was scaled with...
//    dbg.VERBOSE("raster into:"+preferred);
    for(int si=strokes.size();si-->0;){
      stroke(si).drawinto(raster);
    }
    return raster;
  }

  /////////////////////////////////////////////
  // transport

  private static final String pointsKey    = "pes";
  private static final String strokesKey   = "ses";
  private static final String pointsLength = "p";
  private static final String strokesMark  = "s";
  private static final String pointX = "x";
  private static final String pointY = "y";
  //packed:
  private static final String ncraDataKey = "ncraData";

  private static final int extremePointDefault = 32000; // +_+ or whatever

  protected EasyCursor savePoints(Stroke stroke) {
    EasyCursor ezp = new EasyCursor();
    if(Stroke.NonTrivial(stroke)) {
      int i=stroke.numVertices();
      ezp.setInt(pointsLength, i);
      while(i-->0) {
        ezp.setInt(pointX+i, stroke.vertex(i).x);
        ezp.setInt(pointY+i, stroke.vertex(i).y);
      }
    }
    return ezp;
  }

  protected Stroke loadPoints(EasyCursor ezp) {
    Stroke stroke= new Stroke();//make an empty one if properties is defective
    if(ezp != null) {
      int i = ezp.getInt(pointsLength);
      while(i-->0) {//as long as it is sequentail order does not matter
        Point p = new Point(ezp.getInt(pointX+i, extremePointDefault), ezp.getInt(pointY+i, extremePointDefault));
        extreme.x=Math.max(extreme.x,p.x);
        extreme.y=Math.max(extreme.y,p.y);
        stroke.addVertex(p);
      }
    }
    return stroke;
  }

  public void load(EasyCursor ezp) {//legacy, original receipt format
    String mightbepacked=ezp.getString(ncraDataKey);
    if (Safe.hasSubstance(mightbepacked)){
      //it is an ncra format byte[]
      dbg.VERBOSE("loading from ncra format");
      this.jpossig= new SigData(Base64Codec.fromString(mightbepacked));
    }
    else { //legacy
      EasyCursor pezp = ezp.getEasyCursor(pointsKey);
      extreme = new Point(0,0);
      int count = ezp.getInt(strokesKey);
      strokes = new Vector(count);
      while(count-->0) {
        EasyCursor sezp = ezp.getEasyCursor(strokesMark + count);
        Stroke stroke = loadPoints(sezp);
        strokes.add(stroke);
      }
    }
//    dbg.VERBOSE("load:extreme:"+extreme);
  }

  public void save(EasyCursor ezp) {
    if(!SigData.isTrivial(jpossig)){
      dbg.VERBOSE("saving in ncra format");
      ezp.setString(ncraDataKey, Base64Codec.toString(jpossig.ncraData().getRawData()));
    }
    else if(strokes != null) {//legacy, original receipt format
      int count = numStrokes();
      ezp.setInt(strokesKey, count);
      while(count-->0) {
        EasyCursor sezp = savePoints(stroke(count));
        ezp.setEasyCursor(strokesMark + count, sezp);
      }
    }
  }

  // +_+ fixup later if it isn't fast enough
  public String toTransport(){
    EasyCursor ezp = new EasyCursor();
    save(ezp);
    return ezp.toURLdString("");
  }

  public Hancock fromTransport(String s){
    EasyCursor ezp = new EasyCursor();
    ezp.fromURLdString(s, true);
    load(ezp);
    return this;
  }

  /**
  * convert into NCR A format
  * @param compression is an int 0,1,2,3 for number of lsbs to clip.
  * @param os is stream to feed.
  */


  public void toNCRA(ncrOstream packer){
    packer.writeHeader();
    //for each stroke
    for(int i=strokes.size();i-->0;){
      Stroke s=stroke(i);
      s.toNCRA(packer);
    }
  }

  public void toNCRA(OutputStream os, int compression){
    int missinglsbs = 3 - (compression & 0xf);
    //will discard (shift right by) that many bits to improve the run length
    ncrOstream packer=new ncrOstream(os,missinglsbs);
    toNCRA(packer);
  }

  //  public InputStream toNCRA(int compression){
    //    ByteArrayOutputStream os=new ByteArrayOutputStream();
    //    toNCRA(os,compression);
    //    return new ByteArrayInputStream(os.toByteArray());
  //  }

  //should copy over ElectronicSignature.parseNCR and restructure to directly create
  //a Hancock from it. WOuld need to deal wiht negative coordsinates in remapper.
  ////////////////////////////
  public static Hancock Create(SigData siggy){
    Hancock newone= new Hancock();
    newone.setSig(siggy);
    return newone;
  }


}
//$Id: Hancock.java,v 1.26 2001/10/18 05:33:03 andyh Exp $
