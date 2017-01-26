// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/page/accounting/JdbcGatewayOp.Enum]
package net.paymate.web.page.accounting;

import net.paymate.lang.TrueEnum;

public class JdbcGatewayOp extends TrueEnum {
  public final static int overview    =0;
  public final static int capabilities=1;
  public final static int profile     =2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(JdbcGatewayOp.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final JdbcGatewayOp Prop=new JdbcGatewayOp();//for accessing class info
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
  public static JdbcGatewayOp CopyOf(JdbcGatewayOp rhs){//null-safe cloner
    return (rhs!=null)? new JdbcGatewayOp(rhs) : new JdbcGatewayOp();
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

