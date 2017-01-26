// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/POSForm.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class POSForm extends TrueEnum {
  public final static int FubarForm       =0;
  public final static int NotInService    =1;
  public final static int ClerkLogin      =2;
  public final static int IdleAd          =3;
  public final static int GetPayment      =4;
  public final static int SwipeAgain      =5;
  public final static int WaitClerkCredit =6;
  public final static int WaitClerkDebit  =7;
  public final static int WaitClerkCheck  =8;
  public final static int WaitPatronCredit=9;
  public final static int WaitPatronDebit =10;
  public final static int WaitPatronCheck =11;
  public final static int WaitApproval    =12;
  public final static int WaitCompletion  =13;
  public final static int SeeClerk        =14;
  public final static int SignCard        =15;
  public final static int NeedID          =16;

  public int numValues(){ return 17; }
  private static final String[ ] myText = TrueEnum.nameVector(POSForm.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final POSForm Prop=new POSForm();//for accessing class info
  public POSForm(){
    super();
  }
  public POSForm(int rawValue){
    super(rawValue);
  }
  public POSForm(String textValue){
    super(textValue);
  }
  public POSForm(POSForm rhs){
    this(rhs.Value());
  }
  public POSForm setto(POSForm rhs){
    setto(rhs.Value());
    return this;
  }
  public static POSForm CopyOf(POSForm rhs){//null-safe cloner
    return (rhs!=null)? new POSForm(rhs) : new POSForm();
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

