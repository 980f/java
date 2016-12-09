// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AuthorizersFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class AuthorizersFormatEnum extends TrueEnum {
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
  private static final TextList myText = TrueEnum.nameVector(AuthorizersFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final AuthorizersFormatEnum Prop=new AuthorizersFormatEnum();
  public AuthorizersFormatEnum(){
    super();
  }
  public AuthorizersFormatEnum(int rawValue){
    super(rawValue);
  }
  public AuthorizersFormatEnum(String textValue){
    super(textValue);
  }
  public AuthorizersFormatEnum(AuthorizersFormatEnum rhs){
    this(rhs.Value());
  }
  public AuthorizersFormatEnum setto(AuthorizersFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
