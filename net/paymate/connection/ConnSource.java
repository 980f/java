// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/ConnSource.Enum]
package net.paymate.connection;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ConnSource extends TrueEnum {
  public final static int terminalObjects=0;
  public final static int browserHttp    =1;

  public int numValues(){ return 2; }
  private static final TextList myText = TrueEnum.nameVector(ConnSource.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ConnSource Prop=new ConnSource();
  public ConnSource(){
    super();
  }
  public ConnSource(int rawValue){
    super(rawValue);
  }
  public ConnSource(String textValue){
    super(textValue);
  }
  public ConnSource(ConnSource rhs){
    this(rhs.Value());
  }
  public ConnSource setto(ConnSource rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
