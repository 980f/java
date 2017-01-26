// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/DiscoverAVSEnum.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class DiscoverAVSEnum extends TrueEnum {
  public final static int ZIP_and_address_both_match                               =0;
  public final static int Neither_the_ZIP_nor_the_address_matches                  =1;
  public final static int Unable_to_verify_address                                 =2;
  public final static int Cardholder_not_found_or_the_ZIPAddress_file_not_available=3;
  public final static int Only_Address_matches                                     =4;
  public final static int Only_ZIP_matches                                         =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(DiscoverAVSEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final DiscoverAVSEnum Prop=new DiscoverAVSEnum();//for accessing class info
  private static final char [ ] oneCharArray = new char[ ] {
    'A',
    'N',
    'U',
    'W',
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
  public DiscoverAVSEnum(char charValue){
    super();
    setto(charValue);
  }
  public DiscoverAVSEnum(){
    super();
  }
  public DiscoverAVSEnum(int rawValue){
    super(rawValue);
  }
  public DiscoverAVSEnum(String textValue){
    super(textValue);
  }
  public DiscoverAVSEnum(DiscoverAVSEnum rhs){
    this(rhs.Value());
  }
  public DiscoverAVSEnum setto(DiscoverAVSEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static DiscoverAVSEnum CopyOf(DiscoverAVSEnum rhs){//null-safe cloner
    return (rhs!=null)? new DiscoverAVSEnum(rhs) : new DiscoverAVSEnum();
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

