// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/TerminalCommand.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class TerminalCommand extends TrueEnum {
  public final static int Identify     =0;
  public final static int Reconnect    =1;
  public final static int Shutdown     =2;
  public final static int Shout        =3;
  public final static int Quiet        =4;
  public final static int Reload       =5;
  public final static int Reinstall    =6;
  public final static int Pond         =7;
  public final static int Poff         =8;
  public final static int GoOnline     =9;
  public final static int GoOffline    =10;
  public final static int form         =11;
  public final static int Clear        =12;
  public final static int debug        =13;
  public final static int login        =14;
  public final static int logLevel     =15;
  public final static int function     =16;
  public final static int history      =17;
  public final static int stats        =18;
  public final static int sendSignature=19;
  public final static int sendTxn      =20;
  public final static int setAsk       =21;
  public final static int set          =22;
  public final static int Alarms       =23;

  public int numValues(){ return 24; }
  private static final TextList myText = TrueEnum.nameVector(TerminalCommand.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TerminalCommand Prop=new TerminalCommand();
  public TerminalCommand(){
    super();
  }
  public TerminalCommand(int rawValue){
    super(rawValue);
  }
  public TerminalCommand(String textValue){
    super(textValue);
  }
  public TerminalCommand(TerminalCommand rhs){
    this(rhs.Value());
  }
  public TerminalCommand setto(TerminalCommand rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
