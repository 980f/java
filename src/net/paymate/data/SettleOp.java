// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/SettleOp.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class SettleOp extends TrueEnum {
  public final static int Unknown=0;
  public final static int Sale   =1;
  public final static int Return =2;
  public final static int Void   =3;
  public final static int Query  =4;
  public final static int Modify =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(SettleOp.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SettleOp Prop=new SettleOp();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'U',
    'S',
    'R',
    'V',
    'Q',
    'M',
  };
  protected final static char invalidChar = '_'; // ???
  public char CharFor(int rawValue){
    int newVal = Coerced(rawValue);
    return (newVal == invalid) ? invalidChar : oneCharArray[newVal];
  }
  public char Char(){
    return CharFor(value);
  }
  public int setto(char charValue){
    int tempvalue=invalid;
    for(int i = oneCharArray.length; i-->0;) {
      char mychar = oneCharArray[i];
      if(mychar == charValue) {
        tempvalue = i;
        break;
      }
    }
    return value=tempvalue;
  }
  public SettleOp(char charValue){
    super();
    setto(charValue);
  }
  public SettleOp(){
    super();
  }
  public SettleOp(int rawValue){
    super(rawValue);
  }
  public SettleOp(String textValue){
    super(textValue);
  }
  public SettleOp(SettleOp rhs){
    this(rhs.Value());
  }
  public SettleOp setto(SettleOp rhs){
    setto(rhs.Value());
    return this;
  }
  public static SettleOp CopyOf(SettleOp rhs){//null-safe cloner
    return (rhs!=null)? new SettleOp(rhs) : new SettleOp();
  }
/** @return whether it was invalid */
  public boolean AssureValid(int defaultValue){//setto only if invalid
    if( ! isLegal() ){
       setto(defaultValue);
       return true;
    } else {
       return false;
    }
  }

}

