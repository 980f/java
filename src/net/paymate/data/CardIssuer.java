package net.paymate.data;

/**
* Title:        $Source: /cvs/src/net/paymate/data/CardIssuer.java,v $
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author paymate.net
* @version $Revision: 1.18 $
*/

import java.util.*;
import net.paymate.util.*;

public class CardIssuer implements Institution {

  static InstitutionClass myclass;
  public InstitutionClass Class(){
    if(myclass==null){
      myclass= new InstitutionClass (InstitutionClass.CardIssuer);
    }
    return myclass;
  }

  /**
  * two character abbreviation
  */
  String key;
  String longname;

  private static Hashtable lookup=new Hashtable(20);//must textually precede the well known instances


  //well known instances:
  public static final Institution MasterCard=new CardIssuer("MC","MasterCard");
  public static final Institution DinersClub=new CardIssuer("DC","Diners Club");
  public static final Institution JCB=new CardIssuer("JC","JCB");
  public static final Institution CarteBlanche=new CardIssuer("CB","CarteBlance");

  public static final Institution Discover=new CardIssuer("DS","Discover");
  public static final Institution Visa=new CardIssuer("VS","VISA");
  public static final Institution AmericanExpress=new CardIssuer("AE","American Express");

  public static final Institution CardSystems=new CardIssuer("CS","Maverick");
  public static final Institution Paymentech =new CardIssuer("PT","Paymentech");
  public static final Institution Debit =new CardIssuer("DB","Debit");//ptdebit3


  public static final Institution Unknown =new CardIssuer("UK","Unknown");

//////////////////////
//
  private boolean is(String twochar){
    return key.equalsIgnoreCase(twochar);
  }

  /**
   * all Institution objects are created uniquely, therefore we can
   * compare objects neatly taking care of nulls while we are at it.
   */
  public static boolean AreSame(Institution issuer,Institution bank){
    return issuer!=null && issuer == bank;
  }

  public boolean is(Institution bank){
    return bank!=null && bank instanceof CardIssuer && is(((CardIssuer)bank).key);
  }

  public boolean supportsAVS(){ // +++ please reference the authority on this matter (where we found out who supports AVS)
    return this == MasterCard || this == Visa ||  this == Discover ||  this == AmericanExpress;
  }

  public String Abbreviation(){
    return key;
  }

  public String FullName(){
    return longname;
  }

  public Institution setFromIIN(int sixdigitcode){
    return getFromIIN(sixdigitcode);
  }

  public static boolean isIssuer(BinEntry guess){
    return guess != null && guess.issuer instanceof CardIssuer;
    //dregs && ((CardIssuer)guess).is(InstitutionClass.CardIssuer);
  }

  public static Institution getFromIIN(int sixdigitcode){
    BinEntry be=BinEntry.Guess(sixdigitcode);
    return be!=null ? be.issuer : null;
  }

  public static Institution getFrom2(String twochar){
    try {
      return (Institution) lookup.get(twochar);
    }
    catch (Exception ex) {
      return null;
    }
  }

  private CardIssuer(String twochar,String longname) {
    key=twochar;
    this.longname=longname;
    lookup.put(key,this);
  }

}
//$Id: CardIssuer.java,v 1.18 2004/01/29 16:39:23 mattm Exp $
