// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/MessageStatus.Enum]
package net.paymate.connection;

import net.paymate.lang.TrueEnum;

public class MessageStatus extends TrueEnum {
  public final static int New     =0;
  public final static int Sending =1;
  public final static int Sent    =2;
  public final static int Received=3;
  public final static int Done    =4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(MessageStatus.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final MessageStatus Prop=new MessageStatus();//for accessing class info
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
  public static MessageStatus CopyOf(MessageStatus rhs){//null-safe cloner
    return (rhs!=null)? new MessageStatus(rhs) : new MessageStatus();
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

