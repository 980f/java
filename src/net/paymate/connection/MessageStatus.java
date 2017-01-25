// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/MessageStatus.Enum]
package net.paymate.connection;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class MessageStatus extends TrueEnum {
  public final static int New     =0;
  public final static int Sending =1;
  public final static int Sent    =2;
  public final static int Received=3;
  public final static int Done    =4;

  public int numValues(){ return 5; }
  private static final TextList myText = TrueEnum.nameVector(MessageStatus.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final MessageStatus Prop=new MessageStatus();
  public MessageStatus(){
    super();
  }
  public MessageStatus(int rawValue){
    super(rawValue);
  }
  public MessageStatus(String textValue){
    super(textValue);
  }
  public MessageStatus(MessageStatus rhs){
    this(rhs.Value());
  }
  public MessageStatus setto(MessageStatus rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
