package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/AVSDecoder.java,v $</p>
 * <p>Description: where did this come from?  PTauth spec?</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 * @todo: replace cumbersome AVS enumerations with functions on the various CardIssuers.
 */

import net.paymate.util.Ascii;
import net.paymate.lang.StringX;
import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class AVSDecoder {

  public static TextList getAVSCodeSet() {
    TextList tl = new TextList();
    String header = "";
    for(int inst = 4; inst-->0;) {
      switch(inst) {
        case 0: { // VS
          header = CardIssuer.Visa.Abbreviation();
          VisaAVSEnum e = new VisaAVSEnum();
          for(int i = 0; i < e.numValues(); i++) {
            tl.add(header + e.CharFor(i));
          }
        } break;
        case 1: { // MC
          header = CardIssuer.MasterCard.Abbreviation();
          MastercardAVSEnum e = new MastercardAVSEnum();
          for(int i = 0; i < e.numValues(); i++) {
            tl.add(header + e.CharFor(i));
          }
        } break;
        case 2: { // AE
          header = CardIssuer.AmericanExpress.Abbreviation();
          AmericanExpressAVSEnum e = new AmericanExpressAVSEnum();
          for(int i = 0; i < e.numValues(); i++) {
            tl.add(header + e.CharFor(i));
          }
        } break;
        case 3: { // DS
          header = CardIssuer.Discover.Abbreviation();
          DiscoverAVSEnum e = new DiscoverAVSEnum();
          for(int i = 0; i < e.numValues(); i++) {
            tl.add(header + e.CharFor(i));
          }
        } break;
      }
      // --- WARNING!  These next two lines only help with the website!  Possibly move there!
      tl.add(header+""); // no code; this is for a header row only
    }
    return tl;
  }


  private static TrueEnum enumForAvsCode(Institution issuer,char code){
    if(issuer!=null){
      if(issuer.equals(CardIssuer.Visa)){
        return new VisaAVSEnum(code);
      }
      if(issuer.equals(CardIssuer.MasterCard)) {
        return new MastercardAVSEnum(code);
      }
      if(issuer.equals(CardIssuer.Discover)) {
        return new DiscoverAVSEnum(code);
      }
      if(issuer.equals(CardIssuer.AmericanExpress)) {
        return new AmericanExpressAVSEnum(code);
      }
    }
    return null; //take care.
  }

  /**
   * @param issuer  such as CardIssuer.MasterCard
   * @param avscode the byte from the authrorizer
   * @return human readable rendition of avs return code
   */

  public static String AVSmessage(Institution issuer,int avscode) {
    TrueEnum ennum = enumForAvsCode(issuer,(char)avscode);
    if(TrueEnum.IsLegal(ennum)) {
      return StringX.replace(StringX.replace(ennum.Image(), "__", ", "), "_", " ");
    }
    return "Unknown code or issuer";
  }
  /**
   * @param twochar  card issuer such as MC for MasterCard
   * @param response the byte from the authrorizer
   * @return human readable rendition of avs return code
   */
  public static String AVSDecode(String twochar,String response) {
    Institution issuer=CardIssuer.getFrom2(twochar);
    char avscode = StringX.firstChar(response);
    TrueEnum ennum = enumForAvsCode(issuer,avscode);
    if(TrueEnum.IsLegal(ennum)) {
      return " " + StringX.replace(StringX.replace(ennum.Image(), "__", ", "), "_", " ");
    } else {
      return twochar+StringX.bracketed(" AVS response is [",response);
    }
  }
}

//$Id: AVSDecoder.java,v 1.3 2004/02/24 18:31:15 andyh Exp $
