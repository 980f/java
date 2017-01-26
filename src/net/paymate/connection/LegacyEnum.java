package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/LegacyEnum.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */
import net.paymate.lang.RawEnum;
import net.paymate.lang.StringX;

public class LegacyEnum extends RawEnum {
  public final static int CardRequest = 0;
  public final static int CreditRequest = 1;
  public final static int DebitRequest = 2;
  public final static int ReversalRequest = 3;
  public final static int GiftCardRequest = 4;
  public final static int FinancialRequest = 5;//for fubar'd requests
  private final static int numValues = 6;

  public final static int invalid = net.paymate.lang.ObjectX.INVALIDINDEX;

  public boolean isLegal(){
    return value != invalid;
  }

  public int setto(int any){
    if(any>=numValues || any <0){
      any=invalid;
    }
    return value=any;
  }

  public static String toString(int value){
    switch (value) { //these namesw MUST be the original class names
      case CardRequest:      return "net.paymate.connection.CardRequest";
      case CreditRequest:    return "net.paymate.connection.CreditRequest";
      case DebitRequest:     return "net.paymate.connection.DebitRequest";
      case ReversalRequest:  return "net.paymate.connection.ReversalRequest";
      case GiftCardRequest:  return "net.paymate.connection.GiftCardRequest";
      case FinancialRequest: return "net.paymate.connection.FinancialRequest";
      default: return "Illegal."+value;
    }
  }

  private static int ValueFrom(String name) {
    for(int i = numValues; i-->0;) {
      if(StringX.equalStrings(toString(i), name)) {
         return i;
      }
    }
    return invalid;
  }

  public static LegacyEnum ForClass(String clazname){
    LegacyEnum newone=new LegacyEnum ();
    newone.setto(ValueFrom(clazname));
    return newone;
  }

  public String toString(){
    return toString(value);
  }
}
//$Id: LegacyEnum.java,v 1.3 2003/07/29 22:43:02 andyh Exp $
