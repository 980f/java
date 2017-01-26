// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/ServicesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class ServicesFormatEnum extends TrueEnum {
  public final static int nameCol       =0;
  public final static int statusCol     =1;
  public final static int connectionsCol=2;
  public final static int txnsCol       =3;
  public final static int pendCol       =4;
  public final static int timeOutsCol   =5;
  public final static int avgTimeCol    =6;
  public final static int writeCol      =7;
  public final static int readCol       =8;
  public final static int logFileCol    =9;
  public final static int notesCol      =10;

  public int numValues(){ return 11; }
  private static final String[ ] myText = TrueEnum.nameVector(ServicesFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ServicesFormatEnum Prop=new ServicesFormatEnum();//for accessing class info
  public ServicesFormatEnum(){
    super();
  }
  public ServicesFormatEnum(int rawValue){
    super(rawValue);
  }
  public ServicesFormatEnum(String textValue){
    super(textValue);
  }
  public ServicesFormatEnum(ServicesFormatEnum rhs){
    this(rhs.Value());
  }
  public ServicesFormatEnum setto(ServicesFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static ServicesFormatEnum CopyOf(ServicesFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new ServicesFormatEnum(rhs) : new ServicesFormatEnum();
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

