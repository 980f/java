// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/ClerkItem.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ClerkItem extends TrueEnum {
  public final static int BootingUp       =0;
  public final static int ClerkID         =1;
  public final static int ClerkPasscode   =2;
  public final static int SaleType        =3;
  public final static int TerminalOp      =4;
  public final static int PreApproval     =5;
  public final static int PaymentSelect   =6;
  public final static int CreditNumber    =7;
  public final static int BadCardNumber   =8;
  public final static int CreditExpiration=9;
  public final static int BadExpiration   =10;
  public final static int CreditName      =11;
  public final static int CheckBank       =12;
  public final static int CheckAccount    =13;
  public final static int CheckNumber     =14;
  public final static int License         =15;
  public final static int MTA             =16;
  public final static int RefNumber       =17;
  public final static int SalePrice       =18;
  public final static int NeedApproval    =19;
  public final static int NeedSig         =20;
  public final static int WaitApproval    =21;
  public final static int ApprovalCode    =22;
  public final static int Problem         =23;
  public final static int OverrideCode    =24;
  public final static int SecondCopy      =25;
  public final static int WaitAdmin       =26;

  public int numValues(){ return 27; }
  private static final TextList myText = TrueEnum.nameVector(ClerkItem.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ClerkItem Prop=new ClerkItem();
  public ClerkItem(){
    super();
  }
  public ClerkItem(int rawValue){
    super(rawValue);
  }
  public ClerkItem(String textValue){
    super(textValue);
  }
  public ClerkItem(ClerkItem rhs){
    this(rhs.Value());
  }
  public ClerkItem setto(ClerkItem rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
