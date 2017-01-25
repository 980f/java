// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/POSForm.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

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
  private static final TextList myText = TrueEnum.nameVector(POSForm.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final POSForm Prop=new POSForm();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
