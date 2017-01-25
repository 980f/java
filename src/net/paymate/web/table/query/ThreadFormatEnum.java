// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/ThreadFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ThreadFormatEnum extends TrueEnum {
  public final static int nameCol    =0;
  public final static int priorityCol=1;
  public final static int daemonCol  =2;
  public final static int thisCol    =3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(ThreadFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ThreadFormatEnum Prop=new ThreadFormatEnum();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
