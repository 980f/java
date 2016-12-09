package net.paymate.data;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: mattm $
* @version $Id: CardIssuer.java,v 1.3 2001/10/02 17:06:37 mattm Exp $
*/

import java.util.Properties;
import net.paymate.util.Safe;
public class CardIssuer implements Institution {
  static final Properties NameList=new Properties();

  final static String nada="  ";//special 'nothing' to ease formatting issues
  /**
  * two character abbreviation
  */
  String key=nada;

  static {
    NameList.setProperty("VS","VISA");
    NameList.setProperty("MC","Mastercard");
    NameList.setProperty("AX","American Express");
    NameList.setProperty("DS","Discover");
    NameList.setProperty(nada,"Unknown");
  }

  public boolean is(String twochar){
    return key.equalsIgnoreCase(Safe.OnTrivial(twochar,nada));
  }

  public String Abbreviation(){
    return key;
  }

  public String FullName(){
    return NameList.getProperty(key,"Unknown Issuer");
  }

  public static final String firstDigitRule(int firstDigit){
    switch (firstDigit){
      case 6: return "DS";
      case 5: return "MC";
      case 4: return "VS";
      case 3: return "AX";
      default: return nada;
    }

  }

  public Institution setFromIIN(int sixdigitcode){
    key=firstDigitRule(sixdigitcode);
    return this;
  }

  public CardIssuer() {

  }

}
//$Id: CardIssuer.java,v 1.3 2001/10/02 17:06:37 mattm Exp $
