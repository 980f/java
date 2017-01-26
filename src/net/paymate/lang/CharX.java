package net.paymate.lang;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/lang/CharX.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class CharX {
  private CharX() {
    // I exist for static purposes only
  }

  public static final char INVALIDCHAR= '\uFFFF';

  /**
   * left - right pairs.
   * the &; pair is for html escapes.
   */
  private static final String fillerPairs="[]{}<>()&;";
  /**
   * @return complementary character, if none then returns given char
   */
  public static final char matchingBrace(char brace){
    int at=fillerPairs.indexOf(brace);
    return at>=0?fillerPairs.charAt(at^1):brace;
  }
}