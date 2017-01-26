// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/ThreadFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class ThreadFormatEnum extends TrueEnum {
  public final static int nameCol    =0;
  public final static int priorityCol=1;
  public final static int daemonCol  =2;
  public final static int thisCol    =3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(ThreadFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ThreadFormatEnum Prop=new ThreadFormatEnum();//for accessing class info
  public ThreadFormatEnum(){
    super();
  }
  public ThreadFormatEnum(int rawValue){
    super(rawValue);
  }
  public ThreadFormatEnum(String textValue){
    super(textValue);
  }
  public ThreadFormatEnum(ThreadFormatEnum rhs){
    this(rhs.Value());
  }
  public ThreadFormatEnum setto(ThreadFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static ThreadFormatEnum CopyOf(ThreadFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new ThreadFormatEnum(rhs) : new ThreadFormatEnum();
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

