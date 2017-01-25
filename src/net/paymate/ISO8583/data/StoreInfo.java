package net.paymate.ISO8583.data;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: StoreInfo.java,v 1.13 2001/11/16 13:17:26 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.data.StandinLimit;

public class StoreInfo implements isEasy {
  private static final ErrorLogStream dbg = new ErrorLogStream(StoreInfo.class.getName());
  ///////////////////////
  // standin parameters
  public StandinLimit slim=new StandinLimit();
  final static String slimKey="slim";
  //////////////////////////
  // remove some of this now? @MS@
  public String IdentificationCode="";//"000000000076001";
  private String NameLocation="";//"Paymate.NET CorporationAustin       TXUS";
  public String Name   ="";//"PayMate.NET Corporation";
  public String Address="";
  public String City   ="";//"Austin";
  public String State  ="";//"TX"; // should only be 2 chars!
  public String Country="";//"US"; // should only be 2 chars!
  public String timeZoneName           ="";// eg "America/Chicago" for Austin

  protected static final String IdentificationCodeKey="IdentificationCode";
  protected static final String NameKey="Name";
  protected static final String AddressKey="Address";
  protected static final String CityKey="City";
  protected static final String StateKey="State";
  protected static final String CountryKey="Country";
  protected static final String TIME_ZONE_KEY="timeZone";

  /**
   * State is used for guessing at driver's license info
   * --- replace with DL associated classes
   */

  /* member set and gets: */
  public String getNameLocation(){
    return NameLocation;
  }

  // this function is probably only used at the server to construct field 43 from the data in the tables
  public StoreInfo setNameLocation(String name, String address, String city, String state, String country) {
    try {
      dbg.Enter("setNameLocation");
      if(name == null) {
        dbg.ERROR("name is null!");
      }
      Name    = name;
      if(address == null) {
        dbg.ERROR("address is null!");
      }
      Address = address;
      if(city == null) {
        dbg.ERROR("city is null!");
      }
      City    = city;
      if(state == null) {
        dbg.ERROR("state is null!");
      }
      State   = state;
      if(country == null) {
        dbg.ERROR("country is null!");
      }
      Country = country;
      // --- iso messaging has a class for most of this
      // storename 23 left just, padded
      // city      13 left just, padded
      // state      2
      // country    2
      char nameLocation [] = new String("                                        ").toCharArray();
      // stuff it
      System.arraycopy(Name.toCharArray(),    0, nameLocation, 0,  /* necessary? */Math.min(23, Name.length()));
      System.arraycopy(City.toCharArray(),    0, nameLocation, 23, /* necessary? */Math.min(13, City.length()));
      System.arraycopy(State.toCharArray(),   0, nameLocation, 36, /* necessary? */Math.min(2,  State.length()));
      System.arraycopy(Country.toCharArray(), 0, nameLocation, 38, /* necessary? */Math.min(2,  Country.length()));
      NameLocation = new String(nameLocation);
    } catch (Exception t) {
      dbg.Caught(t);
    } finally {
      dbg.Exit();
      return this;
    }
  }

  public String getIdentificationCode(){
    return IdentificationCode;
  }

  public void setIdentificationCode(String id){
    IdentificationCode = id;
  }


////////////////////////////
// transport
  public void save(EasyCursor ezp){
    ezp.setString(IdentificationCodeKey ,IdentificationCode );
    ezp.setString(NameKey ,Name );
    ezp.setString(AddressKey ,Address );
    ezp.setString(CityKey ,City );
    ezp.setString(StateKey ,State );
    ezp.setString(CountryKey ,Country );
    ezp.setString(TIME_ZONE_KEY, timeZoneName);
    ezp.push(slimKey);
    slim.save(ezp);
    ezp.pop();
  }

  public void load(EasyCursor ezp){
    IdentificationCode =ezp.getString(IdentificationCodeKey );
    Name =ezp.getString(NameKey );
    Address =ezp.getString(AddressKey );
    City =ezp.getString(CityKey );
    State =ezp.getString(StateKey );
    Country =ezp.getString(CountryKey );
    timeZoneName = ezp.getString(TIME_ZONE_KEY);
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

}
//$Id: StoreInfo.java,v 1.13 2001/11/16 13:17:26 mattm Exp $