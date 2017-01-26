// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/DrawerClosingFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class DrawerClosingFormatEnum extends TrueEnum {
  public final static int TimeCol     =0;
  public final static int StoreCol    =1;
  public final static int TermCol     =2;
  public final static int AssociateCol=3;
  public final static int CountCol    =4;
  public final static int AmountCol   =5;
  public final static int CSVCol      =6;

  public int numValues(){ return 7; }
  private static final String[ ] myText = TrueEnum.nameVector(DrawerClosingFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final DrawerClosingFormatEnum Prop=new DrawerClosingFormatEnum();//for accessing class info
  public DrawerClosingFormatEnum(){
    super();
  }
  public DrawerClosingFormatEnum(int rawValue){
    super(rawValue);
  }
  public DrawerClosingFormatEnum(String textValue){
    super(textValue);
  }
  public DrawerClosingFormatEnum(DrawerClosingFormatEnum rhs){
    this(rhs.Value());
  }
  public DrawerClosingFormatEnum setto(DrawerClosingFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static DrawerClosingFormatEnum CopyOf(DrawerClosingFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new DrawerClosingFormatEnum(rhs) : new DrawerClosingFormatEnum();
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

