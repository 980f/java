/* $Id: DebitRequest.java,v 1.7 2001/11/17 00:38:34 andyh Exp $ */

package net.paymate.connection;
import  net.paymate.jpos.data.*;
import  net.paymate.ISO8583.data.*;
import net.paymate.util.*;

public class DebitRequest extends CardRequest implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.debit);
  }

  public PINData pin;

  public void save(EasyCursor ezp){
    pin.save(ezp);
  }

  public void load(EasyCursor ezp){
    pin.load(ezp);
  }

  public DebitRequest(SaleInfo sale, MSRData card, PINData pin){
    super(sale,card);
    this.pin=new PINData(pin);
  }

}

//$Id: DebitRequest.java,v 1.7 2001/11/17 00:38:34 andyh Exp $
