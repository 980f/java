/**
* Title:        CreditRequest
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CreditRequest.java,v 1.9 2001/11/17 00:38:34 andyh Exp $
*/
package net.paymate.connection;
import net.paymate.jpos.data.MSRData;
import net.paymate.ISO8583.data.SaleInfo;

public class CreditRequest extends CardRequest {
  public ActionType Type(){
    return new ActionType(ActionType.credit);
  }

  public CreditRequest(SaleInfo sale, MSRData card){
    super(sale,card);
  }

  public CreditRequest(CreditRequest old){
    super(old);
  }

  // required for object instantiation on the server
  public CreditRequest() {
    super();
  }

  public boolean getsSignature(){
    return true;
  }

/**
 * so far this is all we have to filter out bogus cards with
 * it might exclude some ok cards but... wihtout comprehenisve BIN we can't tell.
 */
  public boolean canStandin() {
    return card.ServiceCode.equals("101");
  }

}
//$Id: CreditRequest.java,v 1.9 2001/11/17 00:38:34 andyh Exp $
