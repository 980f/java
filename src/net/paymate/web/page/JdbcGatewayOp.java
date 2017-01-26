// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/page/JdbcGatewayOp.Enum]
package net.paymate.web.page;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class JdbcGatewayOp extends TrueEnum {
  public final static int overview    =0;
  public final static int disptable   =1;
  public final static int capabilities=2;
  public final static int profile     =3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(JdbcGatewayOp.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final JdbcGatewayOp Prop=new JdbcGatewayOp();
  public JdbcGatewayOp(){
    super();
  }
  public JdbcGatewayOp(int rawValue){
    super(rawValue);
  }
  public JdbcGatewayOp(String textValue){
    super(textValue);
  }
  public JdbcGatewayOp(JdbcGatewayOp rhs){
    this(rhs.Value());
  }
  public JdbcGatewayOp setto(JdbcGatewayOp rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
