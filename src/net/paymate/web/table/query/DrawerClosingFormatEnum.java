// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/DrawerClosingFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class DrawerClosingFormatEnum extends TrueEnum {
  public final static int TimeCol     =0;
  public final static int StoreCol    =1;
  public final static int TermCol     =2;
  public final static int AssociateCol=3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(DrawerClosingFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final DrawerClosingFormatEnum Prop=new DrawerClosingFormatEnum();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
