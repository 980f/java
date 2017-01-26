// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/MastercardAVSEnum.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class MastercardAVSEnum extends TrueEnum {
  public final static int Address_matches__ZIP_does_not                                          =0;
  public final static int Transaction_is_ineligible_for_address_verification                     =1;
  public final static int AVS_not_performed_because_the_international_issuer_does_not_support_AVS=2;
  public final static int Neither_the_ZIP_nor_the_address_matches                                =3;
  public final static int Retry__system_unable_to_process                                        =4;
  public final static int AVS_not_supported_at_this_time                                         =5;
  public final static int No_data_from_issuer_or_BankNet_switch                                  =6;
  public final static int ZIP_matches_9__address_does_not                                        =7;
  public final static int Both_Address_and_ZIP_match_9                                           =8;
  public final static int Both_Address_and_ZIP_match_5                                           =9;
  public final static int ZIP_matches__address_does_not_5                                        =10;

  public int numValues(){ return 11; }
  private static final String[ ] myText = TrueEnum.nameVector(MastercardAVSEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final MastercardAVSEnum Prop=new MastercardAVSEnum();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'A',
    'E',
    'G',
    'N',
    'R',
    'S',
    'U',
    'W',
    'X',
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
  public MastercardAVSEnum(char charValue){
    super();
    setto(charValue);
  }
  public MastercardAVSEnum(){
    super();
  }
  public MastercardAVSEnum(int rawValue){
    super(rawValue);
  }
  public MastercardAVSEnum(String textValue){
    super(textValue);
  }
  public MastercardAVSEnum(MastercardAVSEnum rhs){
    this(rhs.Value());
  }
  public MastercardAVSEnum setto(MastercardAVSEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static MastercardAVSEnum CopyOf(MastercardAVSEnum rhs){//null-safe cloner
    return (rhs!=null)? new MastercardAVSEnum(rhs) : new MastercardAVSEnum();
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

