// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/AdminOpCode.Enum]
package net.paymate.web;

import net.paymate.lang.TrueEnum;

public class AdminOpCode extends TrueEnum {
  public final static int appliance       =0;
  public final static int applianceDeath  =1;
  public final static int appliances      =2;
  public final static int associate       =3;
  public final static int associates      =4;
  public final static int authAttempts    =5;
  public final static int authBill        =6;
  public final static int batch           =7;
  public final static int batches         =8;
  public final static int changeEnterprise=9;
  public final static int defaultOp       =10;
  public final static int deposit         =11;
  public final static int deposits        =12;
  public final static int drawer          =13;
  public final static int drawers         =14;
  public final static int duptemp         =15;
  public final static int editRecord      =16;
  public final static int enterprise      =17;
  public final static int enterprises     =18;
  public final static int newAppliance    =19;
  public final static int newAssociate    =20;
  public final static int newEnterprise   =21;
  public final static int newStore        =22;
  public final static int newTerminal     =23;
  public final static int newStoreauth    =24;
  public final static int newTermauth     =25;
  public final static int news            =26;
  public final static int service         =27;
  public final static int services        =28;
  public final static int store           =29;
  public final static int storeaccess     =30;
  public final static int storeauth       =31;
  public final static int stores          =32;
  public final static int termauth        =33;
  public final static int terminal        =34;
  public final static int terminals       =35;
  public final static int TESTAPPLIANCE   =36;
  public final static int transaction     =37;
  public final static int txnSearch       =38;

  public int numValues(){ return 39; }
  private static final String[ ] myText = TrueEnum.nameVector(AdminOpCode.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AdminOpCode Prop=new AdminOpCode();//for accessing class info
  public AdminOpCode(){
    super();
  }
  public AdminOpCode(int rawValue){
    super(rawValue);
  }
  public AdminOpCode(String textValue){
    super(textValue);
  }
  public AdminOpCode(AdminOpCode rhs){
    this(rhs.Value());
  }
  public AdminOpCode setto(AdminOpCode rhs){
    setto(rhs.Value());
    return this;
  }
  public static AdminOpCode CopyOf(AdminOpCode rhs){//null-safe cloner
    return (rhs!=null)? new AdminOpCode(rhs) : new AdminOpCode();
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

