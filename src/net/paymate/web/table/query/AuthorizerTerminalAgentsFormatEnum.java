// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AuthorizerTerminalAgentsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class AuthorizerTerminalAgentsFormatEnum extends TrueEnum {
  public final static int terminalidCol  =0;
  public final static int centsCountCol  =1;
  public final static int centsTotalCol  =2;
  public final static int termBatchNumCol=3;
  public final static int agentStatus    =4;
  public final static int standinStatus  =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(AuthorizerTerminalAgentsFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AuthorizerTerminalAgentsFormatEnum Prop=new AuthorizerTerminalAgentsFormatEnum();//for accessing class info
  public AuthorizerTerminalAgentsFormatEnum(){
    super();
  }
  public AuthorizerTerminalAgentsFormatEnum(int rawValue){
    super(rawValue);
  }
  public AuthorizerTerminalAgentsFormatEnum(String textValue){
    super(textValue);
  }
  public AuthorizerTerminalAgentsFormatEnum(AuthorizerTerminalAgentsFormatEnum rhs){
    this(rhs.Value());
  }
  public AuthorizerTerminalAgentsFormatEnum setto(AuthorizerTerminalAgentsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AuthorizerTerminalAgentsFormatEnum CopyOf(AuthorizerTerminalAgentsFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new AuthorizerTerminalAgentsFormatEnum(rhs) : new AuthorizerTerminalAgentsFormatEnum();
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

