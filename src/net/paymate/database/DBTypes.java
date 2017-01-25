// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/DBTypes.Enum]
package net.paymate.database;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class DBTypes extends TrueEnum {
  public final static int DEC          =0;
  public final static int DECIMAL      =1;
  public final static int INT          =2;
  public final static int INTEGER      =3;
  public final static int SMALLINT     =4;
  public final static int BIGINT       =5;
  public final static int DOUBLE       =6;
  public final static int FLOAT        =7;
  public final static int TINYINT      =8;
  public final static int REAL         =9;
  public final static int NUMERIC      =10;
  public final static int TIME         =11;
  public final static int TIMESTAMP    =12;
  public final static int DATE         =13;
  public final static int DATETIME     =14;
  public final static int BYTE         =15;
  public final static int CHAR         =16;
  public final static int SERIAL       =17;
  public final static int TEXT         =18;
  public final static int VARCHAR      =19;
  public final static int ARRAY        =20;
  public final static int BINARY       =21;
  public final static int BIT          =22;
  public final static int BLOB         =23;
  public final static int CLOB         =24;
  public final static int DISTINCT     =25;
  public final static int JAVA         =26;
  public final static int LONGVARBINARY=27;
  public final static int LONGVARCHAR  =28;
  public final static int NULL         =29;
  public final static int OTHER        =30;
  public final static int REF          =31;
  public final static int STRUCT       =32;
  public final static int VARBINARY    =33;

  public int numValues(){ return 34; }
  private static final TextList myText = TrueEnum.nameVector(DBTypes.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final DBTypes Prop=new DBTypes();
  public DBTypes(){
    super();
  }
  public DBTypes(int rawValue){
    super(rawValue);
  }
  public DBTypes(String textValue){
    super(textValue);
  }
  public DBTypes(DBTypes rhs){
    this(rhs.Value());
  }
  public DBTypes setto(DBTypes rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
