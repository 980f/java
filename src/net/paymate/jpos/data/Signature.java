package net.paymate.jpos.data;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/jpos/data/Signature.java,v $
 * Description:  the data for an iso bit field that has a variable content of sub fields
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.13 $
 * @todo: finish moving packing code into isocursor's domain.
 */

import  net.paymate.util.*;
import  net.paymate.awtx.XPoint;

public class Signature {
  protected XPoint[] signature;
  public static final XPoint MARK=new XPoint(-1, -1);//should be added to jpos spec.
  public static final XPoint MARK2=new XPoint(65535,65535);//from some guys terminals!

  public static boolean isJposMark(XPoint p) {
    return p.equals(Signature.MARK) || p.equals(MARK2);
  }

  public boolean isPresent(){
    return signature!=null && signature.length>0;
  }

  public static final int Threshold=10;

  public boolean isTrivial(){
    return !isPresent() || !(signature.length>=Threshold);//even a single dot should be 3 points
  } //zero points

  public static boolean isTrivial(Signature probate){
    return probate==null || probate.isTrivial();
  }

  public static boolean NonTrivial(Signature probate){
    return ! isTrivial(probate);
  }

  /**
   * @return marginally nontrivial signature
   */
  protected static final XPoint[] Faked(){
    return FAKEforFun;
  }

  public XPoint[] Signature(){
    return signature;
  }

  public void Clear(){
    signature= new XPoint[0]; //much nicer than null
  }

  public void setto(XPoint [] strokes){
    signature=strokes;//temporary assignment
    if(isPresent()){//replace reference with a copy of the thing
      signature=(XPoint [])strokes.clone();
    }
  }

  public Signature(XPoint [] strokes){
    setto(strokes);
  }

  public Signature(){
    signature=null;
  }

  public static XPoint [] ONFILE() {
    return ONFILE;
  }

  private static final XPoint [] ONFILE = {
    new XPoint(2,7),
    new XPoint(1,6),
    new XPoint(1,2),
    new XPoint(2,1),
    new XPoint(4,1),
    new XPoint(5,2),
    new XPoint(5,6),
    new XPoint(4,7),
    new XPoint(2,7),
    (XPoint)MARK.clone(),
    new XPoint(7,1),
    new XPoint(7,7),
    new XPoint(11,1),
    new XPoint(11,7),
    (XPoint)MARK.clone(),
    new XPoint(16,1),
    new XPoint(16,7),
    new XPoint(20,7),
    (XPoint)MARK.clone(),
    new XPoint(16,4),
    new XPoint(18,4),
    (XPoint)MARK.clone(),
    new XPoint(22,1),
    new XPoint(24,1),
    (XPoint)MARK.clone(),
    new XPoint(23,1),
    new XPoint(23,7),
    (XPoint)MARK.clone(),
    new XPoint(22,7),
    new XPoint(24,7),
    (XPoint)MARK.clone(),
    new XPoint(26,7),
    new XPoint(26,1),
    new XPoint(30,1),
    (XPoint)MARK.clone(),
    new XPoint(36,1),
    new XPoint(32,1),
    new XPoint(32,7),
    new XPoint(36,7),
    (XPoint)MARK.clone(),
    new XPoint(32,4),
    new XPoint(34,4),
    (XPoint)MARK.clone(),

  };

  private static final XPoint [] FAKEforFun = {
    new XPoint(4,7), // F
    new XPoint(1,7),
    new XPoint(1,2),
    new XPoint(1,5),
    new XPoint(3,5),
    (XPoint)MARK.clone(),
    new XPoint(7,5), // a
    new XPoint(7,3),
    new XPoint(6,2),
    new XPoint(5,2),
    new XPoint(4,3),
    new XPoint(4,4),
    new XPoint(5,5),
    new XPoint(6,5),
    new XPoint(7,4),
    new XPoint(7,2),
    (XPoint)MARK.clone(),
    new XPoint(8,7), // k
    new XPoint(8,2),
    new XPoint(8,4),
    new XPoint(11,5),
    new XPoint(8,4),
    new XPoint(11,2),
    (XPoint)MARK.clone(),
    new XPoint(12,4), // e
    new XPoint(15,4),
    new XPoint(14,5),
    new XPoint(13,5),
    new XPoint(12,4),
    new XPoint(12,3),
    new XPoint(13,2),
    new XPoint(14,2),
    new XPoint(15,3),
    (XPoint)MARK.clone(),
    new XPoint(19,7), // d
    new XPoint(19,2),
    new XPoint(17,2),
    new XPoint(16,3),
    new XPoint(16,4),
    new XPoint(17,5),
    new XPoint(19,5),
    (XPoint)MARK.clone(), //for good luck
  };

}
//$Id: Signature.java,v 1.13 2005/03/18 03:10:44 andyh Exp $
