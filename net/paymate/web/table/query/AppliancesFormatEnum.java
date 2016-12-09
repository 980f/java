// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AppliancesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class AppliancesFormatEnum extends TrueEnum {
  public final static int storeCol            =0;
  public final static int applianceIdCol      =1;
  public final static int lastLclTimeCol      =2;
  public final static int locallyUniqueIdCol  =3;
  public final static int diffTimeCol         =4;
  public final static int revisionCol         =5;
  public final static int backLog             =6;
  public final static int freeMemoryCol       =7;
  public final static int fmPercentCol        =8;
  public final static int totalMemoryCol      =9;
  public final static int activeCountCol      =10;
  public final static int activeAlarmsCountCol=11;
  public final static int terminalsCol        =12;

  public int numValues(){ return 13; }
  private static final TextList myText = TrueEnum.nameVector(AppliancesFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final AppliancesFormatEnum Prop=new AppliancesFormatEnum();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
