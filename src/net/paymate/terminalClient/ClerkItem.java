// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/ClerkItem.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class ClerkItem extends TrueEnum {
  public final static int BootingUp       =0;
  public final static int ClerkID         =1;
  public final static int ClerkPasscode   =2;
  public final static int SaleType        =3;
  public final static int SpecialOps      =4;
  public final static int DrawerMenu      =5;
  public final static int StoreMenu       =6;
  public final static int PreApproval     =7;
  public final static int PaymentSelect   =8;
  public final static int CreditNumber    =9;
  public final static int BadCardNumber   =10;
  public final static int CreditExpiration=11;
  public final static int BadExpiration   =12;
  public final static int CreditName      =13;
  public final static int CheckBank       =14;
  public final static int CheckAccount    =15;
  public final static int CheckNumber     =16;
  public final static int License         =17;
  public final static int MTA             =18;
  public final static int RefNumber       =19;
  public final static int AVSstreet       =20;
  public final static int AVSzip          =21;
  public final static int MerchRef        =22;
  public final static int SalePrice       =23;
  public final static int NeedApproval    =24;
  public final static int NeedPIN         =25;
  public final static int NeedSig         =26;
  public final static int WaitApproval    =27;
  public final static int ApprovalCode    =28;
  public final static int Problem         =29;
  public final static int OverrideCode    =30;
  public final static int SecondCopy      =31;
  public final static int WaitAdmin       =32;
  public final static int SVOperation     =33;
  public final static int ShowForm        =34;
  public final static int TerminalOp      =35;

  public int numValues(){ return 36; }
  private static final String[ ] myText = TrueEnum.nameVector(ClerkItem.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ClerkItem Prop=new ClerkItem();//for accessing class info
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
  public static ClerkItem CopyOf(ClerkItem rhs){//null-safe cloner
    return (rhs!=null)? new ClerkItem(rhs) : new ClerkItem();
  }
/** @return whether it was invalid */
  public boolean AssureValid(int defaultValue){//setto only if invalid
    if( ! isLegal() ){
       setto(defaultValue);
       return true;
    } else {
       return false;
    }
  }

}

