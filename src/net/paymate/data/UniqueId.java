package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/UniqueId.java,v $
 * Description:  most database engines's idea of a serial number, uniqueness not enforced within this class
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.19 $
 */

import net.paymate.util.*; // Safe
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;

public class UniqueId implements isEasy, Comparable {

  protected static final int INVALID = -1;//database engines's idea of an invalid serial number
//  private /*---*/ int value = INVALID;
  protected static final Integer INVALIDINTEGER = new Integer(INVALID);
  private Integer ivalue = INVALIDINTEGER;

  public boolean isValid() {
    return ivalue.intValue() > 0; // anything less than 1 is invalid!
  }

  public static final boolean isValid(UniqueId id){
    return id!=null&& id.isValid();
  }

  public final String makeMutexName() { // handy tool for making names for mutexes.
    return KEY+"."+ivalue.intValue();
  }

  public String toString() {//+_+ rename Image()
    return ivalue.toString();
  }

  // These HAVE to be private so that people don't screw with them!  Causes problems otherwise!
  private UniqueId setValue(int value) {
    this.ivalue = new Integer(value);
    if(!isValid()){
      ivalue=INVALIDINTEGER;//only a single invalid valu allowed, at least until we have compare funcitons
    }
    return this;
  }
  private UniqueId setValue(String value) {
    return setValue(StringX.parseInt(value));
  }

  public int value() {
    return ivalue.intValue();
  }

  public static boolean equals(UniqueId id1, UniqueId id2) {
    if(id1 == null && id2 == null) {
      return true;
    } else {
      if(id1 == null) {
        return id2.equals(id1);
      } else {
        return id1.equals(id2);
      }
    }
  }

  public boolean equals(UniqueId id2) {
    return compareTo(id2) == 0;
  }

  public boolean equals(Object o) {
    if(o instanceof UniqueId) {
      return this.equals((UniqueId)o);
    }
    return false;
  }

  // for use as a key in a hashtable
  public Integer hashKey() {
    return ivalue;
  }

   public int compareTo(Object obj){//implements Comparable
    if(obj !=null) {
      if(this.getClass()==obj.getClass()) { // +++ does this do the trick?
        return this.ivalue.compareTo(((UniqueId)obj).ivalue); //this.value- ((UniqueId)obj).value;
      } else {
        throw new ClassCastException("Comparing a "+ReflectX.shortClassName(this)+" to: "+obj);
      }
    } else {
      return 1; // this one is greater than null
    }
   }

  private String KEY = ReflectX.justClassName(this);//can't be static! each class has to overload this

  public void save(EasyCursor ezp) {
    ezp.setInt(KEY, value());
  }

  /**
   * it is often nice to not have the field present if value is not known.
   * @return pass through of "field valid" check.
   */
  public boolean saveIfValid(EasyCursor ezp) {
    if(isValid()){
      save(ezp);
      return true;
    } else {
      return false;
    }
  }

  public void load(EasyCursor ezp) {
    setValue(ezp.getInt(KEY));
  }

//  /////////////////
//  // used to bridge the major server upgrade
//  public void legacysave(EasyCursor ezp,String keyword) {
//    ezp.setInt(keyword, value());
//  }
//
//  public void legacyload(EasyCursor ezp,String keyword) {
//    setValue(ezp.getInt(keyword));
//  }
//  // end legacy transporter.
//  ////////////////////////

  public UniqueId() {
    this(INVALID);
  }

  public UniqueId(int value) {
    setValue(value);
  }

  public UniqueId(String value) {
    setValue(value);
  }

  public UniqueId Clear(){
    return setValue(INVALID);
  }

}
//$Id: UniqueId.java,v 1.19 2003/10/19 20:07:11 mattm Exp $