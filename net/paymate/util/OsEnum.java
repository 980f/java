package net.paymate.util;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class OsEnum extends TrueEnum {
  public final static int Linux      =0;
  public final static int NT         =1;
  public final static int Windows    =2;
  public final static int Windows2000=3;
  public final static int SunOS      =4;

  public int numValues(){ return 5; }
  static final TextList myText = TrueEnum.nameVector(OsEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final OsEnum Prop=new OsEnum();
  public OsEnum(){
    super();
  }
  public OsEnum(int rawValue){
    super(rawValue);
  }
  public OsEnum(String textValue){
    super(textValue);
  }
  public OsEnum(OsEnum rhs){
    this(rhs.Value());
  }

}
//$Id: OsEnum.java,v 1.17 2001/07/19 01:06:55 mattm Exp $
