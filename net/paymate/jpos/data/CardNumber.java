/**
* Title:        CardNumber
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CardNumber.java,v 1.14 2001/10/02 17:06:39 mattm Exp $
*/
package net.paymate.jpos.data;

import  net.paymate.util.Safe;

public class CardNumber {
  private static final net.paymate.util.ErrorLogStream dbg = new net.paymate.util.ErrorLogStream(CardNumber.class.getName());

  protected String cardImage="BYTEME"; //because so many folk want string format of it
  public final static int MinDigits=12;//really 13 ... if memory serves
  public final static int MaxDigits=16;//+++need industry spex
  protected boolean ckValid=false;
  public final static String greekfix="...";

  public boolean isTrivial(){
    return !Safe.NonTrivial(cardImage) || cardImage.equals("0");
  }

  public boolean equals (CardNumber rhs){
    return rhs!=null && rhs.cardImage.equals(cardImage);
  }

  public String Image(){
    return cardImage;
  }

  public String Greeked(String prefix){
    int realcard=cardImage.length()-4;
    return realcard>0 ? (prefix+cardImage.substring(realcard)) : "";
  }

  public String Greeked(){
    return Greeked(greekfix);
  }

  public boolean isValid(){
    return ckValid;
  }

/**
 *
 */
  public boolean setto(String incoming){
    if(Safe.NonTrivial(incoming)&&(incoming.length()>=6)){//6 is ISO number committee id number length
      // also remove internal spaces for when pulled from track1:
      cardImage=Safe.removeAll(" ",incoming);
      return ckValid=Mod10.zerosum(incoming);
    } else {
      cardImage="0";
      return ckValid=false;
    }
  }

  public void Clear(){
    setto("");
  }

  public boolean synthesize(String shortByOne){
    return setto(shortByOne+Character.forDigit(Mod10.sum(shortByOne),10));
  }

  public CardNumber(String image) {
    setto(image);
  }

  public CardNumber(CardNumber rhs) {
    setto(rhs.Image());
  }

  public CardNumber(long decimal) {
    this(Long.toString(decimal));
  }

  public CardNumber() {
    this("");
  }

}
//$Id: CardNumber.java,v 1.14 2001/10/02 17:06:39 mattm Exp $
