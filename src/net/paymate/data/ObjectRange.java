package net.paymate.data;

/**
* Title:        $Source: /cvs/src/net/paymate/data/ObjectRange.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.6 $
*/

import net.paymate.util.*;

public class ObjectRange implements isEasy {
  private static final ErrorLogStream dbg = new ErrorLogStream(ObjectRange.class.getName(), ErrorLogStream.WARNING);

  protected Comparable one;
  protected Comparable two;
  private boolean singular;
  private boolean broad;
  private boolean sorted=false;

  private boolean isDirty=true; //defer analyze

  public boolean singular(){
    if(isDirty){
      analyze();
    }
    return singular;
  }

  public boolean broad(){
    if(isDirty){
      analyze();
    }
    return broad;
  }

  public String one(){
    if(isDirty){
      analyze();
    }
    return Safe.TrivialDefault(one);
  }

  public String two(){
    if(isDirty){
      analyze();
    }
    return Safe.TrivialDefault(two);
  }

  public Comparable filter(String input){
    return input;
  }

  public ObjectRange setOne(Comparable input) {
    dbg.VERBOSE("setOne():"+input);
    one=input;
    isDirty=true;
    return this;
  }

  public ObjectRange setTwo(Comparable input) {
    dbg.VERBOSE("setTwo():"+input);
    two=input;
    isDirty=true;
    return this;
  }

  public ObjectRange setOne(String input) {
    return setOne(filter(input));
  }

  public ObjectRange setTwo(String input) {
    return setTwo(filter(input));
  }

  public ObjectRange setBoth(Comparable oner,Comparable twoer){
    return setOne(oner).setTwo(twoer);
  }

  public ObjectRange setBoth(String oner,String twoer){
    return setOne(oner).setTwo(twoer);
  }

  public boolean NonTrivial(){
    if(isDirty){
      analyze();
    }
    return singular || broad;
  }

  public static final boolean NonTrivial(ObjectRange pair){
    return pair!=null && pair.NonTrivial();
  }

  protected ObjectRange swap(){
    dbg.VERBOSE("swapping");
    Comparable exchange=one;
    one = two;
    two = exchange;
    return this;
  }

  protected ObjectRange sort(){
    if(broad){
      if(two.compareTo(one)<0){
        swap();
      }
    }
    return this;
  }

  /**
  * must be called anytime either string has changed
  */
  protected void analyze(){
    dbg.Enter("analyze");
    try {
      //temporarily make assignments, then make sense of them
      broad=Safe.NonTrivial(two);
      singular=Safe.NonTrivial(one);
      dbg.VERBOSE("NonTrivials: "+singular+" "+broad);
      //if two is nonTrivial
      if(broad){
        if(!singular || one.compareTo(two)==0 ){//and either one is trivial or the same as two
          dbg.VERBOSE("is actually singular");
          one=two;
          two=null;
          singular=true;
          broad=false;
        } else {
          singular=false;
        }
        if(sorted){
          sort();
        }
      }
      isDirty=false;
    }
    finally {
      dbg.Exit();
    }
  }

  public void copyMembers(ObjectRange rhs){
    dbg.VERBOSE("copyMembers from "+rhs.getClass().getName()+" to this "+this.getClass().getName());
    //can this work? if(rhs.getClass()==this.getClass())
    {
      sorted = rhs.sorted;
      setOne(rhs.one);
      setTwo(rhs.two);
    }
  }

  ////////////////////////
  protected final static String oneKey="one";
  protected final static String twoKey="two";
  protected final static String sortedKey="sorted";

  public void save(EasyCursor ezp){
    ezp.setString(oneKey,one());
    ezp.setString(twoKey,two());
    ezp.setBoolean(sortedKey,sorted);
  }

  public EasyCursor saveas(String key,EasyCursor ezp){
    ezp.push(key);
    save(ezp);
    return ezp.pop();
  }

  public void load(EasyCursor ezp){
    sorted = ezp.getBoolean(sortedKey,false); // do this one first (affects the rest) !!!
    setBoth(ezp.getString(oneKey,null),ezp.getString(twoKey,null));
  }

  public EasyCursor loadfrom(String key,EasyCursor ezp){
    ezp.push(key);
    load(ezp);
    return ezp.pop();
  }

  protected ObjectRange(boolean sorted){
    isDirty=true;
    this.sorted=sorted;
  }

  public ObjectRange() {//default is to be sorted.
    this(true);
  }

  protected ObjectRange(String one, String two,boolean sorted){
    this(sorted);
    setBoth(one,two);
  }

  public String toString() {
    return (sorted?"":"un")+"sorted: [" + one() + ", " + two() + "]";
  }

  /* The rules for ObjectRange:
  <li> ObjectRange expects the objects to be comparable if sorted is true.
  You will get exceptions if they are not.

  <li> Safe has NonTrivial(Object o) which does an if-else on instanceof
  those classes for which we have a Safe.NonTrivial function.
  It returns !=null for unknown classes. ObjectRange relies upon this.
  Any object.NonTrivial functions should also have a Safe.NonTrivial()
  partner made for them, which checks null and then calls the object's NonTrivial.

  <li> Finally the objects used in a range should have a meaningful toString().

  */


}
//$Id: ObjectRange.java,v 1.6 2001/07/20 04:16:27 mattm Exp $
