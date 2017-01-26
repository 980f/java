package net.paymate.util;

import net.paymate.lang.TrueEnum;

public class OsEnum extends TrueEnum {
  public final static int Linux      =0;
  public final static int NT         =1;
  public final static int Windows    =2;
  public final static int Windows2000=3;
  public final static int SunOS      =4;

  public int numValues(){ return 5; }
  static final String [ ] myText = TrueEnum.nameVector(OsEnum.class);
  protected final String [ ] getMyText() {
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
//$Id: OsEnum.java,v 1.18 2003/07/27 05:35:22 mattm Exp $
