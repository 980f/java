// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/TerminalSetCommand.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class TerminalSetCommand extends TrueEnum {
  public final static int cardaccountNumber =0;
  public final static int cardexpirationDate=1;
  public final static int saletypepayby     =2;
  public final static int saletypeop        =3;
  public final static int saletypesource    =4;
  public final static int salemoneyamount   =5;
  public final static int salepreapproval   =6;
  public final static int checkIdlicense    =7;
  public final static int voidstan          =8;

  public int numValues(){ return 9; }
  private static final String[ ] myText = TrueEnum.nameVector(TerminalSetCommand.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TerminalSetCommand Prop=new TerminalSetCommand();//for accessing class info
  public TerminalSetCommand(){
    super();
  }
  public TerminalSetCommand(int rawValue){
    super(rawValue);
  }
  public TerminalSetCommand(String textValue){
    super(textValue);
  }
  public TerminalSetCommand(TerminalSetCommand rhs){
    this(rhs.Value());
  }
  public TerminalSetCommand setto(TerminalSetCommand rhs){
    setto(rhs.Value());
    return this;
  }
  public static TerminalSetCommand CopyOf(TerminalSetCommand rhs){//null-safe cloner
    return (rhs!=null)? new TerminalSetCommand(rhs) : new TerminalSetCommand();
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

