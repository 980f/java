/* $Id: POSConditionCode.java,v 1.8 2001/10/02 17:06:34 mattm Exp $ */
package net.paymate.ISO8583.data;

public class POSConditionCode { //for field 25
  public static final int Normal=0;
  public static final int CustomerNotPresent=1; //will xor into other patterns
  public static final int ManualReversal=20; //???at odds with other patterns
  public static final int ManagerOverride=50;
  public static final int VoiceAuthorized=60;

  protected int basevalue             =Normal;
  protected boolean CustomerIsPresent =true;

  public POSConditionCode setType(int base){
    basevalue=base;
    return this;
  }

  public POSConditionCode setCustomer(boolean ispresent){
    CustomerIsPresent=ispresent;
    return this;
  }

  public int Value(){
    return CustomerIsPresent? basevalue : (basevalue|CustomerNotPresent);
  }

  public POSConditionCode(int base, boolean ispresent){
    setType(base);
    setCustomer(ispresent);
  }

}
//$Id: POSConditionCode.java,v 1.8 2001/10/02 17:06:34 mattm Exp $
