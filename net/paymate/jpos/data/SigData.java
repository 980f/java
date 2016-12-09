package net.paymate.jpos.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/SigData.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: SigData.java,v 1.26 2001/10/18 05:33:03 andyh Exp $
 */

import net.paymate.ivicm.et1K.ncraSignature;
import java.awt.Point; // please don't use * here; I need to see which things are getting loaded
import net.paymate.util.*;

public class SigData extends Signature {
  static Tracer dbg=new Tracer(SigData.class.getName());

  ncraSignature es=null;

  public ncraSignature ncraData(){
    return es;
  }

  public boolean isTrivial(){
    dbg.mark("isTrivial");
    parseIfNeeded();//legacy, still making points
    return super.isTrivial();
  }

  private void parseIfNeeded(){
    if(!isPresent()){//Signature.isPresent()
      if(es!=null){
        dbg.VERBOSE("parsing points");
        setto(es.getPoints());
      }
    }
  }

  /**
   * return jpos style signature
   */
  public Point[] Signature(){
    dbg.mark("returning base Signature");
    parseIfNeeded();
    return signature;
  }

  /**
   * @return marginally nontrivial signature
   */
  public static final SigData MinimalFaked(){
    return new SigData(Signature.Faked());
  }

  public SigData(ncraSignature es) {
    dbg.VERBOSE("making sigdata from ncraSig");
    this.es=es;
  }

  public SigData(byte [] rawdata){
    this(ncraSignature.fromRawData(rawdata));
    dbg.VERBOSE("made sigdata from byte[]");
  }

  public SigData(Point [] strokes){
    super(strokes);
  }

  public SigData(){
    //makes a trivial one.
  }

  public static SigData Clone(SigData rhs){
    return new SigData(rhs.es);
  }

}
//$Id: SigData.java,v 1.26 2001/10/18 05:33:03 andyh Exp $
