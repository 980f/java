// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/StoreMenu.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class StoreMenu extends TrueEnum {
  public final static int PrintOptions=0;
  public final static int Deposit     =1;
  public final static int CloseDrawers=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(StoreMenu.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final StoreMenu Prop=new StoreMenu();//for accessing class info
  public StoreMenu(){
    super();
  }
  public StoreMenu(int rawValue){
    super(rawValue);
  }
  public StoreMenu(String textValue){
    super(textValue);
  }
  public StoreMenu(StoreMenu rhs){
    this(rhs.Value());
  }
  public StoreMenu setto(StoreMenu rhs){
    setto(rhs.Value());
    return this;
  }
  public static StoreMenu CopyOf(StoreMenu rhs){//null-safe cloner
    return (rhs!=null)? new StoreMenu(rhs) : new StoreMenu();
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

