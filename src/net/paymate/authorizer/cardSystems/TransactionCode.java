// NOT generated.
package net.paymate.authorizer.cardSystems;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

/*
// np.Authorizer.CardSystems.TransactionCode.Enum
TC54 // 54 Auth : Purchase
TC55 // 55 Auth : Cash Advance
TC56 // 56 Auth : Mail/Phone Order
TC58 // 58 Auth : Card Authentication
TC01 // 01 Purch: No Auth
TC02 // 02 Cash Adv: No Auth
TC03 // 03 Mail: No Auth
TC04 // 04 Force : Offline Voice Auth
TC05 // 05 Credit : Credit Transaction
TCV1 // V1 Void : Voided Purchase
TCV2 // V2 Void : Voided Cash Advance
TCV3 // V3 Void : Voided Mail/Phone Order
TCV4 // V4 Void : Voided Force
TCV5 // V5 Void : Voided Credit
*/

public class TransactionCode extends TrueEnum {
  public final static int TC54=0;
  public final static int TC55=1;
  public final static int TC56=2;
  public final static int TC58=3;
  public final static int TC01=4;
  public final static int TC02=5;
  public final static int TC03=6;
  public final static int TC04=7;
  public final static int TC05=8;
  public final static int TCV1=9;
  public final static int TCV2=10;
  public final static int TCV3=11;
  public final static int TCV4=12;
  public final static int TCV5=13;

  public int numValues(){ return 14; }
  private static final TextList myText = TrueEnum.nameVector(TransactionCode.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TransactionCode Prop=new TransactionCode();
  public TransactionCode(){
    super();
  }
  public TransactionCode(int rawValue){
    super(rawValue);
  }
  public TransactionCode(String textValue){
    super(textValue);
  }
  public TransactionCode(TransactionCode rhs){
    this(rhs.Value());
  }

}
//$Id: TransactionCode.java,v 1.1 2001/10/02 17:06:35 mattm Exp $