package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/MerchantInfo.java,v $
 * Description:  a particular authorizer's defined info for identifying a terminal
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

import net.paymate.util.*;
import java.util.*; // timezone
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public class MerchantInfo {

  public String authmerchid;
  public String authtermid;
  private TimeZone tz;
  public MerchantType type = new MerchantType();

  public TimeZone tz(){return tz;}

  public String toString(){
    String tzpart= (tz !=null) ? tz.getID() : "noTimeZone";
    return String.valueOf(authmerchid)+"/"+String.valueOf(authtermid)+"/"+tzpart+"/"+type.Image();
  }

  /**
   * @return true if content is not trivial. THis implies that if a particular
   * authorizer doesn't need a field that we set that field to "N/A" or
   * something equivalent to that in our database.
   */
  public boolean isValid(){
    return StringX.NonTrivial(authmerchid)&&StringX.NonTrivial(authtermid)&&ObjectX.NonTrivial(tz);
  }

  public void Clear(){
    authtermid = "";
    authmerchid = "";
    tz = null;
  }

  public MerchantInfo set(String authmerchid,String authtermid,TimeZone tz,MerchantType type){
    this.authmerchid=authmerchid;
    this.authtermid=authtermid;
    this.tz=tz;
    this.type.setto(type);
    return this;
  }

  public static MerchantInfo New(String authmerchid,String authtermid,TimeZone tz,MerchantType type){
    return (new MerchantInfo()).set(authmerchid,authtermid,tz,type);
  }

  public static MerchantInfo fakeOne(){
  //present settings are for linkpoint debug:
    return New("800001","notermid",TimeZone.getDefault(),new MerchantType(MerchantType.Retail));
  }

}
//$Id: MerchantInfo.java,v 1.11 2003/08/27 02:20:09 mattm Exp $
