/* $Id: CardRequest.java,v 1.13 2001/07/06 18:56:36 andyh Exp $ */

package net.paymate.connection;
import  net.paymate.jpos.data.*;
import  net.paymate.ISO8583.data.*;
import net.paymate.util.*;

public class CardRequest extends FinancialRequest implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.card);
  }

  public MSRData card;

  public void save(EasyCursor ezp){
    card.save(ezp);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    card.load(ezp);
    super.load(ezp);
  }

  public CardRequest (SaleInfo sale, MSRData card){
    super(sale);
    this.card= new MSRData(card);
  }

  // required for object instantiation on the server
  public CardRequest() {
    card = new MSRData(); // an empty one
  }

  public CardRequest (CardRequest old){
    this(old.sale, old.card);
  }

}

//$Id: CardRequest.java,v 1.13 2001/07/06 18:56:36 andyh Exp $
