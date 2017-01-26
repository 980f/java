package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/Shaker.java,v $
 * Description:  hand shake output access, base class for virtual ports' use.
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class Shaker {
  private String tla;//three letter abbreviation
  private boolean lastKnownState;

  public String tla(){
    return tla;
  }

  public boolean lastSetting(){
    return lastKnownState;
  }

  protected Shaker(String tla,boolean lastKnownState) {
    this.tla=tla;
    this.lastKnownState=lastKnownState;
  }

  protected Shaker(String tla) {
    this(tla,false);
  }
  /**
   * argument for setto(boolean)
   */
  public final static boolean ON=true;
  public final static boolean OFF=false;
  public final static boolean ACTIVE=true;
  public final static boolean INACTIVE=false;

  /**
   * returns whether this is a change
   */
  public boolean setto(boolean on){
    if(lastKnownState!=on){
      lastKnownState=on;
      return true;
    } else {
      return false;
    }
  }

  public static Shaker Virtual(String tla,boolean lastKnownState){
    return new Shaker(tla,lastKnownState);
  }

}
//$Id: Shaker.java,v 1.1 2002/09/06 18:56:12 andyh Exp $