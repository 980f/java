// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/MerchantType.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class MerchantType extends TrueEnum {
  public final static int Restaurant        =0;
  public final static int Lodging           =1;
  public final static int AutoRental        =2;
  public final static int Moto              =3;
  public final static int ElectronicCommerce=4;
  public final static int Retail            =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(MerchantType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final MerchantType Prop=new MerchantType();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'F',
    'L',
    'A',
    'M',
    'E',
    'R',
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
  public MerchantType(char charValue){
    super();
    setto(charValue);
  }
  public MerchantType(){
    super();
  }
  public MerchantType(int rawValue){
    super(rawValue);
  }
  public MerchantType(String textValue){
    super(textValue);
  }
  public MerchantType(MerchantType rhs){
    this(rhs.Value());
  }
  public MerchantType setto(MerchantType rhs){
    setto(rhs.Value());
    return this;
  }
  public static MerchantType CopyOf(MerchantType rhs){//null-safe cloner
    return (rhs!=null)? new MerchantType(rhs) : new MerchantType();
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

