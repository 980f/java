package net.paymate.jpos.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/CardNumber.java,v $
 * Description:  bank card account number
 * Copyright:    2000 -2003 PayMate.net
 * Company:      paymate.net
 * @author       paymate.net
 * @version      $Revision: 1.26 $
 */

import net.paymate.util.*;
import net.paymate.lang.*;

public class CardNumber {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CardNumber.class);
  protected String cardImage = "BYTEME"; //internally a string because so many folk want string format of it
  public final static int MinDigits = 12;
  public final static int MaxDigits = 19;
  protected boolean ckValid = false;
  public final static String greekfix = "...";

  public boolean isTrivial() {
    return!StringX.NonTrivial(cardImage) || cardImage.equals("0");
  }

  public static boolean NonTrivial(CardNumber probate) {
    return probate != null && !probate.isTrivial();
  }

  public boolean equals(CardNumber rhs) {
    return rhs != null && rhs.cardImage.equals(cardImage);
  }

  public String Image() {
    return cardImage;
  }

  /**
   * for messaging that wants a zero filled fixed length field
   */
  public long asLong() {
    return StringX.parseLong(cardImage);
  }

  public String toString() {
    return cardImage;
  }

  public int cardHash() {
    return cardHash(cardImage);
  }
  // use this to hash a string that a user types in for a search!
  public static int cardHash(String cardImage) {
    return StringX.NonTrivial(cardImage) ? cardImage.hashCode() : 0;
  }

  public int BinNumber() {
    return StringX.parseInt(StringX.subString(cardImage, 0, 6));
  }

  public String Greeked(String prefix) {
    if (prefix == null) { //trivial is ok.
      prefix = "";
    }
    int realcard = cardImage.length() - 4;
    String hellenic = realcard > 0 ? (prefix + cardImage.substring(realcard)) :
        "";
    hellenic = StringX.right(hellenic, StringX.lengthOf(cardImage));
    return hellenic;
  }

  public String last4() {
    String last4 = StringX.right(cardImage, 4);
    return Fstring.righted(last4, 4, '0');
  }

  public int last4int() {
    return Integer.valueOf(last4()).intValue();
  }

  public String Greeked() {
    return Greeked(greekfix);
  }

  public boolean isValid() {
    return ckValid;
  }

  /**
   * call this when mod10 checksum is irrelevent, in case someone later forgets to check the relevency.
   */
  public void mootSum(){
    ckValid=true;
  }

  /**
   *
   */
  public boolean setto(String incoming) {
    if (StringX.NonTrivial(incoming) && (incoming.length() >= 6)) { //6 is ISO number committee id number length
      // also remove internal spaces for when pulled from track1:
      cardImage = StringX.removeAll(" ", incoming);
      return ckValid = Mod10.zerosum(incoming);//cached in case it isn't relevent.
    } else {
      cardImage = "0";
      return ckValid = false;
    }
  }

  public void Clear() {
    setto("");
  }

  public boolean synthesize(String shortByOne) {
    return setto(shortByOne + Character.forDigit(Mod10.sum(shortByOne), 10));
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
//$Id: CardNumber.java,v 1.26 2003/11/02 07:59:10 mattm Exp $
