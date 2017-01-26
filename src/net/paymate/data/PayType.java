// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/PayType.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class PayType extends TrueEnum {
  public final static int Unknown =0;
  public final static int Credit  =1;
  public final static int GiftCard=2;
  public final static int Debit   =3;
  public final static int Check   =4;
  public final static int Cash    =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(PayType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final PayType Prop=new PayType();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'U',
    'C',
    'G',
    'D',
    'K',
    'S',
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
  public PayType(char charValue){
    super();
    setto(charValue);
  }
  public PayType(){
    super();
  }
  public PayType(int rawValue){
    super(rawValue);
  }
  public PayType(String textValue){
    super(textValue);
  }
  public PayType(PayType rhs){
    this(rhs.Value());
  }
  public PayType setto(PayType rhs){
    setto(rhs.Value());
    return this;
  }
  public static PayType CopyOf(PayType rhs){//null-safe cloner
    return (rhs!=null)? new PayType(rhs) : new PayType();
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

