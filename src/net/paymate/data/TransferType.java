// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/TransferType.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class TransferType extends TrueEnum {
  public final static int Unknown =0;
  public final static int Sale    =1;
  public final static int Return  =2;
  public final static int Reversal=3;
  public final static int Query   =4;
  public final static int Authonly=5;
  public final static int Modify  =6;
  public final static int Force   =7;

  public int numValues(){ return 8; }
  private static final String[ ] myText = TrueEnum.nameVector(TransferType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TransferType Prop=new TransferType();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'U',
    'S',
    'R',
    'V',
    'Q',
    'A',
    'M',
    'F',
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
  public TransferType(char charValue){
    super();
    setto(charValue);
  }
  public TransferType(){
    super();
  }
  public TransferType(int rawValue){
    super(rawValue);
  }
  public TransferType(String textValue){
    super(textValue);
  }
  public TransferType(TransferType rhs){
    this(rhs.Value());
  }
  public TransferType setto(TransferType rhs){
    setto(rhs.Value());
    return this;
  }
  public static TransferType CopyOf(TransferType rhs){//null-safe cloner
    return (rhs!=null)? new TransferType(rhs) : new TransferType();
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

