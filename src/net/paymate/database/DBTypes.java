// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/DBTypes.Enum]
package net.paymate.database;

import net.paymate.lang.TrueEnum;

public class DBTypes extends TrueEnum {
  public final static int DECIMAL      =0;
  public final static int INT4         =1;
  public final static int SMALLINT     =2;
  public final static int BIGINT       =3;
  public final static int DOUBLE       =4;
  public final static int FLOAT        =5;
  public final static int TINYINT      =6;
  public final static int REAL         =7;
  public final static int NUMERIC      =8;
  public final static int BOOL         =9;
  public final static int TIME         =10;
  public final static int TIMESTAMP    =11;
  public final static int DATE         =12;
  public final static int DATETIME     =13;
  public final static int BYTE         =14;
  public final static int CHAR         =15;
  public final static int SERIAL       =16;
  public final static int TEXT         =17;
  public final static int VARCHAR      =18;
  public final static int ARRAY        =19;
  public final static int BINARY       =20;
  public final static int BIT          =21;
  public final static int BLOB         =22;
  public final static int CLOB         =23;
  public final static int DISTINCT     =24;
  public final static int JAVA         =25;
  public final static int LONGVARBINARY=26;
  public final static int LONGVARCHAR  =27;
  public final static int NULL         =28;
  public final static int OTHER        =29;
  public final static int REF          =30;
  public final static int STRUCT       =31;
  public final static int VARBINARY    =32;

  public int numValues(){ return 33; }
  private static final String[ ] myText = TrueEnum.nameVector(DBTypes.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final DBTypes Prop=new DBTypes();//for accessing class info
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
  public static DBTypes CopyOf(DBTypes rhs){//null-safe cloner
    return (rhs!=null)? new DBTypes(rhs) : new DBTypes();
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

