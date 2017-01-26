// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/VisaAVSEnum.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class VisaAVSEnum extends TrueEnum {
  public final static int Address_matches__ZIP_does_not                                                                             =0;
  public final static int Transaction_is_ineligible_for_address_verification                                                        =1;
  public final static int AVS_not_performed_because_the_international_issuer_does_not_support_AVS                                   =2;
  public final static int Neither_the_ZIP_nor_the_address_matches                                                                   =3;
  public final static int Issuers_system_is_unavailable__try_again_later                                                            =4;
  public final static int AVS_not_supported_at_this_time                                                                            =5;
  public final static int Unable_to_perform_AVS_because_either_address_information_is_unavailable_or_the_Issuer_does_not_support_AVS=6;
  public final static int Both_Address_and_ZIP_match                                                                                =7;
  public final static int ZIP_matches__address_does_not                                                                             =8;

  public int numValues(){ return 9; }
  private static final String[ ] myText = TrueEnum.nameVector(VisaAVSEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final VisaAVSEnum Prop=new VisaAVSEnum();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'A',
    'E',
    'G',
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
  public VisaAVSEnum(char charValue){
    super();
    setto(charValue);
  }
  public VisaAVSEnum(){
    super();
  }
  public VisaAVSEnum(int rawValue){
    super(rawValue);
  }
  public VisaAVSEnum(String textValue){
    super(textValue);
  }
  public VisaAVSEnum(VisaAVSEnum rhs){
    this(rhs.Value());
  }
  public VisaAVSEnum setto(VisaAVSEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static VisaAVSEnum CopyOf(VisaAVSEnum rhs){//null-safe cloner
    return (rhs!=null)? new VisaAVSEnum(rhs) : new VisaAVSEnum();
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

