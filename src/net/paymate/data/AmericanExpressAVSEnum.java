// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/AmericanExpressAVSEnum.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class AmericanExpressAVSEnum extends TrueEnum {
  public final static int Only_Address_matches                                         =0;
  public final static int Neither_the_ZIP_nor_the_address_matches                      =1;
  public final static int Issuers_system_is_unavailable__try_again_later               =2;
  public final static int AVS_not_supported_at_this_time                               =3;
  public final static int Information_is_not_available__account_neither_US_nor_Canadian=4;
  public final static int Both_Address_and_ZIP_match                                   =5;
  public final static int Only_ZIP_matches                                             =6;

  public int numValues(){ return 7; }
  private static final String[ ] myText = TrueEnum.nameVector(AmericanExpressAVSEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AmericanExpressAVSEnum Prop=new AmericanExpressAVSEnum();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'A',
    'N',
    'R',
    'S',
    'U',
    'Y',
    'Z',
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
  public AmericanExpressAVSEnum(char charValue){
    super();
    setto(charValue);
  }
  public AmericanExpressAVSEnum(){
    super();
  }
  public AmericanExpressAVSEnum(int rawValue){
    super(rawValue);
  }
  public AmericanExpressAVSEnum(String textValue){
    super(textValue);
  }
  public AmericanExpressAVSEnum(AmericanExpressAVSEnum rhs){
    this(rhs.Value());
  }
  public AmericanExpressAVSEnum setto(AmericanExpressAVSEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AmericanExpressAVSEnum CopyOf(AmericanExpressAVSEnum rhs){//null-safe cloner
    return (rhs!=null)? new AmericanExpressAVSEnum(rhs) : new AmericanExpressAVSEnum();
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

