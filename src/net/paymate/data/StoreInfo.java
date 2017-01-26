package net.paymate.data;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: StoreInfo.java,v 1.1 2003/10/25 20:34:21 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.awtx.RealMoney;
import  net.paymate.lang.StringX;

public class StoreInfo implements isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreInfo.class);
  ///////////////////////
  // standin parameters
  public StandinLimit slim=new StandinLimit();
  final static String slimKey="slim";
 //////////////////////////
  public String Name   ="";//"PayMate.NET Corporation";
  public String Address="";
  public String City   ="";//"Austin";
  public String State  ="";//"TX"; // should only be 2 chars!
  public String Country="";//"US"; // should only be 2 chars!
  public String timeZoneName           ="";// eg "America/Chicago" for Austin
  public MerchantType type=merchanttype(null);  // sets to retail by default
  public boolean enmodify = false;
  public boolean enauthonly = false;

//  protected static final String IdentificationCodeKey="IdentificationCode";
  private static final String NameKey="Name";
  private static final String AddressKey="Address";
  private static final String CityKey="City";
  private static final String StateKey="State";
  private static final String CountryKey="Country";
  private static final String TIME_ZONE_KEY="timeZone";
  private static final String TypeKey="merchantType";
  private static final String enAuthonlyKey="enauthonly";
  private static final String enModifyKey="enmodify";

  /**
   * sets fields enmasse rather than having separate setters. the legacy reason for this has been removed
   */
  public StoreInfo setNameLocation(String name, String address, String city, String state, String country) {
    Name = name;
    Address = address;
    City = city;
    State = state;
    Country = country;
    return this;
  }

////////////////////////////
// transport
  public void save(EasyCursor ezp){
    ezp.setString(NameKey ,Name );
    ezp.setString(AddressKey ,Address );
    ezp.setString(CityKey ,City );
    ezp.setString(StateKey ,State );
    ezp.setString(CountryKey ,Country );
    ezp.setString(TIME_ZONE_KEY, timeZoneName);
    ezp.setBoolean(enAuthonlyKey, enauthonly);
    ezp.setBoolean(enModifyKey, enmodify);
    ezp.saveEnum(TypeKey, type);
    ezp.push(slimKey);
    slim.save(ezp);
    ezp.pop();
  }

  public void load(EasyCursor ezp){
    Name =ezp.getString(NameKey );
    Address =ezp.getString(AddressKey );
    City =ezp.getString(CityKey );
    State =ezp.getString(StateKey );
    Country =ezp.getString(CountryKey );
    timeZoneName = ezp.getString(TIME_ZONE_KEY);
    enauthonly = ezp.getBoolean(enAuthonlyKey, false);
    enmodify = ezp.getBoolean(enModifyKey, false);

    ezp.loadEnum(TypeKey, type);
    ezp.push(slimKey);
    slim.load(ezp);
    ezp.pop();
  }

  public StoreInfo() {
//empty
  }

  public TextList superSpam(TextList spam){
    if(spam==null){
      spam=new TextList();
    }
    spam.add(this.Name);
    spam.add(this.Address);
    spam.add(this.City+" "+this.State+" "+Country+" "+timeZoneName);
    spam.add(this.type.toSpam());
    spam.add(slim.spam());
    return spam;
  }

/**
 * for detecting if change is enough to reboot the terminal during config.
 * a StoreInfo from cache gets compared to one periodicaly requested from server.
 * @todo check for other fields that affect terminal operations.
 * exclude slim, doesn't affect "real" operation
 */
  public boolean equals(StoreInfo newone){
    return newone!=null; //looks like nothing needs to be checked.
  }

  // mostly used by the database, but other places use it, like the web.
  // maybe put it in StoreInfoRow and call it from there.
  public static MerchantType merchanttype(String from1charstring) {
    MerchantType mt = StringX.NonTrivial(from1charstring) ?
        new MerchantType(from1charstring.charAt(0)) :
        new MerchantType(MerchantType.Retail); // default to retail if not set
    return mt.isLegal() ? mt : new MerchantType(MerchantType.Retail);
  }

}
//$Id: StoreInfo.java,v 1.1 2003/10/25 20:34:21 mattm Exp $