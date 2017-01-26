// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/DrawerMenu.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class DrawerMenu extends TrueEnum {
  public final static int PrintOptions  =0;
  public final static int Totals        =1;
  public final static int Detail        =2;
  public final static int Close_w_Totals=3;
  public final static int Close_w_Detail=4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(DrawerMenu.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final DrawerMenu Prop=new DrawerMenu();//for accessing class info
  public DrawerMenu(){
    super();
  }
  public DrawerMenu(int rawValue){
    super(rawValue);
  }
  public DrawerMenu(String textValue){
    super(textValue);
  }
  public DrawerMenu(DrawerMenu rhs){
    this(rhs.Value());
  }
  public DrawerMenu setto(DrawerMenu rhs){
    setto(rhs.Value());
    return this;
  }
  public static DrawerMenu CopyOf(DrawerMenu rhs){//null-safe cloner
    return (rhs!=null)? new DrawerMenu(rhs) : new DrawerMenu();
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

