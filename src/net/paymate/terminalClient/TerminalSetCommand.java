// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/TerminalSetCommand.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

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
  private static final TextList myText = TrueEnum.nameVector(TerminalSetCommand.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TerminalSetCommand Prop=new TerminalSetCommand();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
