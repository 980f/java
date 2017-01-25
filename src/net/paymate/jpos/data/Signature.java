/* $Id: Signature.java,v 1.5 2001/09/07 03:55:11 andyh Exp $ */
package net.paymate.jpos.data;
import  net.paymate.util.*;
import  java.awt.Point;
import  java.util.StringTokenizer;

public class Signature {
  protected Point[] signature;
  public static final Point MARK=new Point(-1, -1);//should be added to jpos spec.

  public boolean isPresent(){
    return signature!=null;
  }

  public static final int Threshold=10;

  public boolean isTrivial(){
    return !isPresent() || !(signature.length>=Threshold);//even a single dot should be 3 points
  } //zero points

  public static boolean isTrivial(Signature probate){
    return probate==null || probate.isTrivial();
  }

  /**
   * @return marginally nontrivial signature
   */
  protected static final Point[] Faked(){
    return FAKEforFun;
  }

  public Point[] jpos(){
    return signature;
  }

  public void Clear(){
    signature= new Point[0]; //much nicer than null
  }

  public void setto(Point [] strokes){
    signature=strokes;//temporary assignment
    if(isPresent()){//replace reference with a copy of the thing
      signature=(Point [])strokes.clone();
    }
  }

  public Signature(Point [] strokes){
    setto(strokes);
  }

  public Signature(){
    signature=null;
  }

  private static final Point [] FAKEforFun = {
    new Point(4,7), // F
    new Point(1,7),
    new Point(1,2),
    new Point(1,5),
    new Point(3,5),
    MARK,
    new Point(7,5), // a
    new Point(7,3),
    new Point(6,2),
    new Point(5,2),
    new Point(4,3),
    new Point(4,4),
    new Point(5,5),
    new Point(6,5),
    new Point(7,4),
    new Point(7,2),
    MARK,
    new Point(8,7), // k
    new Point(8,2),
    new Point(8,4),
    new Point(11,5),
    new Point(8,4),
    new Point(11,2),
    MARK,
    new Point(12,4), // e
    new Point(15,4),
    new Point(14,5),
    new Point(13,5),
    new Point(12,4),
    new Point(12,3),
    new Point(13,2),
    new Point(14,2),
    new Point(15,3),
    MARK,
    new Point(19,7), // d
    new Point(19,2),
    new Point(17,2),
    new Point(16,3),
    new Point(16,4),
    new Point(17,5),
    new Point(19,5),
    MARK, //for good luck
  };

}
//$Id: Signature.java,v 1.5 2001/09/07 03:55:11 andyh Exp $
