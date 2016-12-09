package net.paymate.database;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DateToken.java,v 1.3 2001/07/19 01:06:47 mattm Exp $
 */


import  net.paymate.util.TrueEnum;
import  net.paymate.util.TextList;

public class DateToken extends TrueEnum {
  public final static int YEAR      =0;
  public final static int MONTH     =1;
  public final static int DAY       =2;
  public final static int HOUR      =3;
  public final static int MINUTE    =4;
  public final static int SECOND    =5;
  public final static int FRACTION  =6;

  public int numValues(){ return 7; }
  static final TextList myText = TrueEnum.nameVector(DateToken.class);
  protected final TextList getMyText() {
    return myText;
  }
  static final DateToken Prop=new DateToken();
  public DateToken(){
    super();
  }
  public DateToken(int rawValue){
    super(rawValue);
  }
  public DateToken(String textValue){
    super(textValue);
  }
  public DateToken(DateToken rhs){
    this(rhs.Value());
  }
  public int setto(String textValue){
//    dbg.Enter("setto");
    try {
      String toTest = TextFor(FRACTION)/*"FRACTION"*/;
      textValue = textValue.toUpperCase(); // just in case
      if(textValue.indexOf(toTest) > -1) {
        textValue = toTest; // since it could have other stuff tacked on afterwards
      }
    } catch (Exception e) {
//      dbg.Caught(e);
    } finally {
//      dbg.Exit();
    }
    return super.setto(textValue);
  }
}
