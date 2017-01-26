package net.paymate.jpos.awt;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/jpos/awt/Hancock.java,v $
 * Description:  signature and some renderign code for it.
 * Copyright:    2000 PayMate.net
 * Company:      paymate
 * @author       paymate
 * @version      $Id: Hancock.java,v 1.55 2005/03/18 03:10:44 andyh Exp $
 */

import net.paymate.jpos.data.*;
import net.paymate.util.*;
import net.paymate.util.codec.Base64Codec;
import net.paymate.awtx.*;
import net.paymate.ivicm.et1K.SignatureType;
import net.paymate.ivicm.et1K.ncraSignature;
import net.paymate.ivicm.et1K.ncrOstream;
import net.paymate.lang.StringX;
import net.paymate.awtx.XPoint;
import net.paymate.lang.TrueEnum;
import java.lang.Math;
import java.util.*;
import java.io.*;

public class Hancock implements isEasy {
  static Tracer dbg = new Tracer(Hancock.class);
  static final SignatureType HANCOCK = new SignatureType(SignatureType.Hancock);

  Vector strokes;
  XPoint extreme;
  Quadrant quad;
  public Hancock setQuadrant(Quadrant quad) {
    quad = Quadrant.Clone(quad);
    return this;
  }

  public Quadrant getQuadrant() {
    return quad;
  }

  SigData jpossig;
  Hancock setSig(SigData siggy) {
    this.jpossig = SigData.Clone(siggy);
    return this;
  }

  private boolean haveStrokes() {
    dbg.VERBOSE("strokes=" + (strokes == null ? "null" : "notnull") + " so size=" + (strokes == null ? "0" : "" + strokes.size()));
    return strokes != null && strokes.size() > 0;
  }

  private void parseIfNeeded() {
    dbg.VERBOSE("parse if needed");
    if(!haveStrokes()) {
      dbg.VERBOSE("parse if needed");
      if(!SigData.isTrivial(jpossig)) {
        dbg.VERBOSE("parsing jpos signature");
        parse(jpossig.Signature());
      }
    }
  }

  public Hancock() {
    extreme = new XPoint(0, 0);
    strokes = new Vector();
    quad = Quadrant.First(); //legacy convention.
  }

  private Hancock(Hancock rhs) {
    setSig(rhs.jpossig);
    quad = Quadrant.Clone(rhs.quad);
    extreme = new XPoint(rhs.extreme);
    //inline copy, cloning didn't work out as expected as we need to clone deep
    strokes = new Vector();
    int size = rhs.strokes.size();
    strokes.ensureCapacity(size);
    for(int si = size; si-- > 0; ) {
      strokes.add(new Stroke(rhs.stroke(si)));
    }
  }

  public boolean NonTrivial() {
//    dbg.mark("at NonTrivial test");
    if(!SigData.isTrivial(jpossig)) {
      dbg.VERBOSE("jpossig is not trivial");
      return true;
    }
    parseIfNeeded();
    return haveStrokes();
  }

  public static final boolean NonTrivial(Hancock probate) {
    dbg.VERBOSE("probate is " + (probate == null ? "" : "NOT ") + "NULL");
    return probate != null && probate.NonTrivial();
  }

  public boolean legalIndex(int i) {
    return i >= 0 && i < strokes.size();
  }

  public void addStroke(Stroke stroke) {
    if(stroke != null && stroke.vertices != null && stroke.vertices.size() > 0) {
      strokes.add(stroke);
    }
  }

  public int numStrokes() {
//    dbg.mark("at num strokes test");
    parseIfNeeded();
    return strokes.size();
  }

  public Stroke stroke(int i) {
    return legalIndex(i) ? (Stroke) strokes.elementAt(i) : new Stroke();
  }

  //////////////////////////////
  // parse jpos nonsense
  /**
   * bottom left corner is relocated to origin.
   * is this required ??? YES, expected by our rescalig code.
   * @return the maximum
   */

  public
  static final XPoint makePositive(XPoint[] Points) {
    XPoint extreme = new XPoint(0, 0);

    dbg.VERBOSE("Mark is " + Signature.MARK);

    if(Points.length > 1) { //else it is trivial and doesn't need offsetting
      XPoint origin = new XPoint(Points[0]); //a point known to NOT be a mark, so long as WE source the data.

      for(int i = Points.length; i-- > 1; ) { //0 is used to prime the search
        XPoint p = Points[i];
        if(!Signature.isJposMark(p)) {
          dbg.VERBOSE("makepositive adjusting extreme and origin by:" + p);
          extreme.x = Math.max(extreme.x, p.x);
          extreme.y = Math.max(extreme.y, p.y);
          origin.x = Math.min(origin.x, p.x);
          origin.y = Math.min(origin.y, p.y);
        } else {
          dbg.VERBOSE("makepositive found a mark");
        }
      }
      dbg.VERBOSE("origin:" + origin + " extreme:" + extreme);
//pervert value for use by XPoint::translate()
      origin.x = -origin.x;
      origin.y = -origin.y;

      for(int i = Points.length; i-- > 0; ) {
        XPoint p = Points[i];
        if(!Signature.isJposMark(p)) {
          p.translate(origin.x, origin.y);
        }
      }
      extreme.translate(origin.x, origin.y);
      dbg.VERBOSE("translated extreme:" + extreme);
    }
    dbg.VERBOSE("Mark is " + Signature.MARK);
    return extreme;
  }

  public
 static final XPoint flipY (XPoint[] Points) {
   XPoint extreme = new XPoint(0, 0);

   dbg.VERBOSE("Mark is " + Signature.MARK);

   if(Points.length > 1) { //else it is trivial and doesn't need offsetting
     XPoint origin = new XPoint(Points[0]); //a point known to NOT be a mark, so long as WE source the data.

     for(int i = Points.length; i-- > 1; ) { //0 is used to prime the search
       XPoint p = Points[i];
       if(! Signature.isJposMark( p)) {
         dbg.VERBOSE("makepositive adjusting extreme and origin by:" + p);
         if (p.y>500){
           dbg.ERROR("invalid incoming data");
         }
         extreme.y = Math.max(extreme.y, p.y);
         origin.y = Math.min(origin.y, p.y);
       } else {
         dbg.VERBOSE("makepositive found a mark");
       }
     }
     dbg.VERBOSE("origin:" + origin + " extreme:" + extreme);

     for(int i = Points.length; i-- > 0; ) {
       XPoint p = Points[i];
       if(!Signature.isJposMark(p)) {
         p.y=origin.y+1-p.y;
       }
     }
     extreme.translate(origin.x, origin.y);
     dbg.VERBOSE("translated extreme:" + extreme);
   }
   dbg.VERBOSE("Mark is " + Signature.MARK);
   return extreme;
 }




  /** convert string of points some of which are special marker values into
     a vector of vectors of points */
  private Hancock parse(XPoint[] sig) {
    dbg.Enter("parse(XPoint[])");
    dbg.VERBOSE("Mark is " + Signature.MARK);
    try {
      extreme = makePositive(sig);
//    dbg.VERBOSE("parsed extreme:"+extreme);
      if(sig != null) {
        Stroke stroke = new Stroke();
        for(int i = 0; i < sig.length; i++) {
          XPoint vertex = sig[i];
          if(Signature.isJposMark(vertex)) {
            addStroke(stroke);
            stroke = new Stroke(); //do NOT reuse a stroke, unless addStroke is changed to make copies
            dbg.VERBOSE("Adding stroke due to mark.");
          } else {
            stroke.addVertex(vertex);
            dbg.VERBOSE("adding vertex: " + vertex);
          }
        }
        addStroke(stroke); //for when sig does NOT have a trailing MARK!
      }
    } catch(Exception ex) {
      dbg.Caught(ex);
    } finally {
      dbg.Exit();
      dbg.VERBOSE("Mark is " + Signature.MARK);
      return this;
    }
  }

  /** modify stored items so that their coords are compatible with some rectangle  */
  public Hancock remapped(XDimension preferred, XDimension aspect, Quadrant printQuad) {
    dbg.Enter("remapped");
    try {
      parseIfNeeded();
      ReScaler scaler = new ReScaler(extreme, preferred, 1 /*pixels of border*/, aspect, Quadrant.Combine(quad, printQuad));
      //ReScaler(...) has a side effect of converting preferred to absolute values
      Hancock morphed = new Hancock(this); //copies objects, not references

      for(int si = strokes.size(); si-- > 0; ) {
        morphed.stroke(si).remapto(scaler);
      }
      scaler.remap(morphed.extreme);
      return morphed;
    } finally {
      dbg.Exit();
    }
  }

  /**
   * @param preferred is dimensions that our stroke data is supposed to already be scaled to.
   * @return a new raster of those dimensions upon which the vector data has been drawn.
   */
  public Raster rasterTo(XDimension preferred) {
    return drawOnto(new Raster(preferred)); //during debug this is the same as 'this' was scaled with...
  }

  public Raster drawOnto(Raster raster) {
    dbg.mark("drawOnto");
    parseIfNeeded();
    for(int si = strokes.size(); si-- > 0; ) {
      stroke(si).drawinto(raster);
    }
    return raster;
  }

  /////////////////////////////////////////////
  // transport

  private static final String pointsKey = "pes";
  private static final String strokesKey = "ses";
  private static final String pointsLength = "p";
  private static final String strokesMark = "s";
  private static final String pointX = "x";
  private static final String pointY = "y";
  //packed:
  private static final String ncraDataKey = "ncraData";

  private static final int extremePointDefault = 32000; // +_+ or whatever

  protected EasyCursor savePoints(Stroke stroke) {
    EasyCursor ezp = new EasyCursor();
    if(Stroke.NonTrivial(stroke)) {
      int i = stroke.numVertices();
      ezp.setInt(pointsLength, i);
      while(i-- > 0) {
        ezp.setInt(pointX + i, stroke.vertex(i).x);
        ezp.setInt(pointY + i, stroke.vertex(i).y);
      }
    }
    return ezp;
  }

  protected Stroke loadPoints(EasyCursor ezp) {
    Stroke stroke = new Stroke(); //make an empty one if properties is defective
    if(ezp != null) {
      int i = ezp.getInt(pointsLength);
      while(i-- > 0) { //as long as it is sequentail order does not matter
        XPoint p = new XPoint(ezp.getInt(pointX + i, extremePointDefault), ezp.getInt(pointY + i, extremePointDefault));
        extreme.x = Math.max(extreme.x, p.x);
        extreme.y = Math.max(extreme.y, p.y);
        stroke.addVertex(p);
      }
    }
    return stroke;
  }

  public EasyCursor getEasyCursor(EasyCursor ezp, String key) {
    return new EasyCursor(ezp.getURLedString(key, ""));
  }

  private static final String SIGTYPEKEY = "SIGTYPE";
  private static final String SIGDATAKEY = "SIGDATA";

//  private static final SignatureType BADSIGTYPE() { // can't make this static enough to pevent setto(), :(
//    return new SignatureType(SignatureType.Invalid());//+++ this should never even be convenient, much less valuable
//  }

  private void loadLegacy(EasyCursor ezp) {
    dbg.VERBOSE("loading from format 0");
    EasyCursor pezp = getEasyCursor(ezp, pointsKey);
    extreme = new XPoint(0, 0);
    int count = ezp.getInt(strokesKey);
    strokes = new Vector(count);
    while(count-- > 0) {
      EasyCursor sezp = getEasyCursor(ezp, strokesMark + count);
      Stroke stroke = loadPoints(sezp);
      strokes.add(stroke);
    }
  }

  /**
   * push all coordinates into one quadrant,
   * set 'extreme' to size of signature polyline.
   */
  private void normalize() {
    extreme = new XPoint(0, 0); //reset size
    XPoint origin = new XPoint(0, 0); //look for negatives
    XPoint p;
    for(int stroker = numStrokes(); stroker-- > 0; ) {
      Stroke stroke = stroke(stroker);
      for(int i = stroke.numVertices(); i-- > 0; ) {
        p = stroke.vertex(i);
        extreme.x = Math.max(extreme.x, p.x);
        extreme.y = Math.max(extreme.y, p.y);
        origin.x = Math.min(origin.x, p.x);
        origin.y = Math.min(origin.y, p.y);
      }
    }

    origin.x = -origin.x;
    origin.y = -origin.y;

    for(int stroker = numStrokes(); stroker-- > 0; ) {
      Stroke stroke = stroke(stroker);
      for(int i = stroke.numVertices(); i-- > 0; ) {
        p = stroke.vertex(i);
        p.translate(origin.x, origin.y);
      }
    }
    extreme.translate(origin.x, origin.y);
  }

  private void loadStrokes(EasyCursor ezp) {
    strokes = ezp.loadVector("stroke", Stroke.class);
    //jumpware/ross signatures were vacuous unti we updated 'extreme'
    normalize();
  }

  /**
   * @return whether we found something LIKELY to be real
   */
  private boolean loadPacked(String packed, SignatureType sigtype) {
    if(StringX.NonTrivial(packed)) {
      dbg.VERBOSE("packed = " + packed);
      byte[] base64ed = Base64Codec.fromString(packed);
      dbg.VERBOSE("unpacked = " + Ascii.bracket(base64ed));
      jpossig = SigData.CreateFrom(base64ed, sigtype);
      dbg.VERBOSE("sigdata = " + jpossig.toString());
      return true;
    } else {
      dbg.WARNING("sigtype is " + (sigtype.isLegal() ? "" : "NOT ") + "legal");
      dbg.WARNING("packed is  " + (StringX.NonTrivial(packed) ? "NOT " : "") + "trivial");
      return false;
    }
  }

  public void load(EasyCursor ezp) { //legacy, original receipt format
    // load the sig type
    SignatureType sigtype = new SignatureType(ezp.getEnumValue(SIGTYPEKEY, SignatureType.Prop));
    // if there is one (not unknown type), load the sig data
    if(sigtype.isLegal()) {
      dbg.VERBOSE("loading from " + sigtype.Image() + " format");
      if(sigtype.is(sigtype.Hancock)) {
        loadStrokes(ezp);
      } else {
        loadPacked(ezp.getString(SIGDATAKEY), sigtype);
      }
      quad.load(ezp);
    } else {
      dbg.VERBOSE("trying legacy formats");
      // if there isn't one, try to load the legacy format
      if(loadPacked(ezp.getString(ncraDataKey), new SignatureType(SignatureType.NCRA))) {
        dbg.VERBOSE("loaded from legacy ncra format");
      } else { //format 0
        dbg.VERBOSE("trying original verbose format");
        loadLegacy(ezp);
      }
    }
  }

  public void save(EasyCursor ezp) {
    parseIfNeeded(); //added for hyperjpos
    quad.save(ezp);
    if(!SigData.isTrivial(jpossig)) { //if true then we have a compressed version of sig
      ncraSignature ncraData = jpossig.ncraData();
      if(ncraData != null) {//we really do have a compressed sig
        // save the type
        ezp.saveEnum(SIGTYPEKEY, (ncraData != null) ? ncraData.getType() : new SignatureType());
        // save the content
        SignatureType type = ncraData.getType();
        if(TrueEnum.IsLegal(type)) {
          dbg.VERBOSE("saving in " + type.Image() + " format");
          ezp.setString(SIGDATAKEY, Base64Codec.toString(jpossig.ncraData().getRawData()));
        }
        return;
      }
      //haven't managed to decode hyperjpos rawdata yet so we wil have to fall through to using strokes.
      dbg.WARNING("get and decode hjpos sigs!");
    }
    if(strokes != null) {
      ezp.saveEnum(SIGTYPEKEY, HANCOCK);
      ezp.saveVector("stroke", this.strokes);
    }
  }

  /**
   * convert into NCR A format
   * @param compression is an int 0,1,2,3 for number of lsbs to clip.
   * @param os is stream to feed.
   */

  public void toNCRA(ncrOstream packer) {
    packer.writeHeader();
    //for each stroke
    for(int i = strokes.size(); i-- > 0; ) {
      Stroke s = stroke(i);
      s.toNCRA(packer);
    }
  }

  public void toNCRA(OutputStream os, int compression) {
    int missinglsbs = 3 - (compression & 0xf);
    //will discard (shift right by) that many bits to improve the run length
    ncrOstream packer = new ncrOstream(os, missinglsbs);
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
  public static Hancock Create(SigData siggy) {
    Hancock newone = new Hancock();
    newone.setSig(siggy);
    return newone;
  }

  public static Hancock Copy(Hancock rhs) {
    return(rhs != null) ? (new Hancock(rhs)) : (new Hancock());
  }

  /////////////////////////////
  /**
   *
   */
  static public void main(String[] args) {
    Hancock testme = new Hancock();
    testme.parse(Signature.ONFILE());
    EasyCursor ezc = EasyCursor.makeFrom(testme);
    Hancock cf = new Hancock();
    cf.load(ezc);
    System.out.println("did it");

  }

}
//$Id: Hancock.java,v 1.55 2005/03/18 03:10:44 andyh Exp $
