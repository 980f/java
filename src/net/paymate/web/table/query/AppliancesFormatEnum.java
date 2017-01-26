// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AppliancesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class AppliancesFormatEnum extends TrueEnum {
  public final static int storeCol            =0;
  public final static int applianceIdCol      =1;
  public final static int srvrCnxnTimeCol     =2;
  public final static int applClockDriftCol   =3;
  public final static int lastLclTimeCol      =4;
  public final static int diffTimeCol         =5;
  public final static int revisionCol         =6;
  public final static int backLog             =7;
  public final static int freeMemoryCol       =8;
  public final static int fmPercentCol        =9;
  public final static int totalMemoryCol      =10;
  public final static int activeCountCol      =11;
  public final static int activeAlarmsCountCol=12;
  public final static int terminalsCol        =13;
  public final static int ipLan               =14;
  public final static int ipWan               =15;
  public final static int ipAppTime           =16;
  public final static int ipSrvTime           =17;

  public int numValues(){ return 18; }
  private static final String[ ] myText = TrueEnum.nameVector(AppliancesFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AppliancesFormatEnum Prop=new AppliancesFormatEnum();//for accessing class info
  public AppliancesFormatEnum(){
    super();
  }
  public AppliancesFormatEnum(int rawValue){
    super(rawValue);
  }
  public AppliancesFormatEnum(String textValue){
    super(textValue);
  }
  public AppliancesFormatEnum(AppliancesFormatEnum rhs){
    this(rhs.Value());
  }
  public AppliancesFormatEnum setto(AppliancesFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AppliancesFormatEnum CopyOf(AppliancesFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new AppliancesFormatEnum(rhs) : new AppliancesFormatEnum();
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

