package net.paymate.jpos.data;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/jpos/data/SigData.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: SigData.java,v 1.37 2005/03/17 06:45:04 andyh Exp $
 */

import net.paymate.ivicm.et1K.ncraSignature;
import net.paymate.ivicm.et1K.SignatureType;
import net.paymate.awtx.XPoint;
import net.paymate.util.*;

public class SigData extends Signature {
  static Tracer dbg = new Tracer(SigData.class);

  ncraSignature es = null;

  public ncraSignature ncraData() {
    return es;
  }

  public boolean isTrivial() {
    dbg.mark("isTrivial");
    parseIfNeeded(); //legacy, still making points
    return super.isTrivial();
  }

  private void parseIfNeeded() {
    if(!isPresent()) { //Signature.isPresent()
      if(es != null) {
        dbg.VERBOSE("parsing points");
        setto(es.getPoints());
      }
    }
  }

  /**
   * return jpos style signature
   */
  public XPoint[] Signature() {
    dbg.mark("returning base Signature");
    parseIfNeeded();
    return super.Signature();
  }

  /**
   * @return marginally nontrivial signature
   */
  public static final SigData MinimalFaked() {
    return new SigData(Signature.Faked());
  }

  public static final SigData OnFile() {
    return new SigData(Signature.ONFILE());
  }

  public SigData(ncraSignature es) {
    if(es != null) {
      dbg.VERBOSE("making SigData by assigning ncraSig of type " + es.getType().Image());
    }
    this.es = es;
  }

  public static SigData CreateFrom(byte[] rawdata, SignatureType sigtype) {
    return new SigData(ncraSignature.fromRawData(rawdata, sigtype));
  }

  public SigData(XPoint[] strokes) {
    super(strokes);
  }

  public SigData() {
    //makes a trivial one.
  }

  public static SigData Clone(SigData rhs) {
    if(rhs == null) {
      return new SigData();
    }
    if(rhs.es == null) {
      if(rhs.signature != null) {
        return new SigData(rhs.signature);
      } else {
        return new SigData();
      }
    }
    return SigData.CreateFrom(rhs.es.getRawData(), rhs.es.getType());
  }

}
//$Id: SigData.java,v 1.37 2005/03/17 06:45:04 andyh Exp $
