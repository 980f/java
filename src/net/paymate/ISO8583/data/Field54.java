/* $Id: Field54.java,v 1.9 2001/10/15 22:39:44 andyh Exp $ */
package net.paymate.ISO8583.data;

import net.paymate.ISO8583.factory.SubDecimal;
import net.paymate.ISO8583.factory.SubString;

public class Field54 {
  SubDecimal first  = new SubDecimal(0,2);
  SubDecimal second = new SubDecimal(0+2,2);
  SubDecimal third  = new SubDecimal(2+2,3,840);  //fixed value field  5-7 Currency code // Must be 840 - US dollars
  SubString  fourth = new SubString(2+2+3,13);

  public int Type;
  //  1-2 Account type
  // Same codes as for digits 3-4 of bit 3 as defined by Transactive.
  // See "3 Processing Code."

  public boolean isCashBack  =false ; //else is just a balance
  // 3-4 Amount type:
  // 02 = Available balance
  // 40 = Cash back

  public LedgerValue cents= new LedgerValue(13);//---fourth.len

  public String toString(){
    StringBuffer packed= new StringBuffer();
    packed.setLength(20);

    first.setto(Type); //+_+ recode from some ennum
    second.setto(isCashBack?40:2);
    //third is set at construction and never modfied
    fourth.setto(cents.toString());

    //order doesn't matter but this looks nice:
    first.  insertInto(packed);
    second. insertInto(packed);
    third.  insertInto(packed);
    fourth. insertInto(packed);

    return packed.toString();
  }

}
//$Id: Field54.java,v 1.9 2001/10/15 22:39:44 andyh Exp $
