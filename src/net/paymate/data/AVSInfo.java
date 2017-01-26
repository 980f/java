package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/AVSInfo.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class AVSInfo implements isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AVSInfo.class);

  // this data not stored in the database
  private String address; // for PT 20
  private String zip; // for PT 9

  public boolean NonTrivial(){
    return StringX.NonTrivial(address)||StringX.NonTrivial(zip);
  }

  public static boolean NonTrivial(AVSInfo probate){
    return probate!=null && probate.NonTrivial();
  }

  public void clear(){
    address="";//using direct assignment to make it easy to grep for 'real' setting of values.
    zip="";
  }

  public AVSInfo() {  //needed for reflective load implied by being isEasy().
    clear();
  }

  private static final String addressKey="address";
  private static final String zipKey="zip";

  public String address(){
    return address;
  }
  public AVSInfo setAddress(String address){
    this.address=address;
    return this;
  }

  public String zip(){
    return zip;
  }

  public AVSInfo setZip(String zip){
    this.zip=zip;
    return this;
  }

  public void save(EasyCursor ezc){
    ezc.setString(addressKey,address);
    ezc.setString(zipKey,zip);
    dbg.VERBOSE("save():Saved AVS info");
  }

  public void load(EasyCursor ezc){
    address=ezc.getString(addressKey);
    zip=ezc.getString(zipKey);
    dbg.VERBOSE("load():Loaded AVS info");
  }

 public static AVSInfo New(String address, String zip){
   return new AVSInfo().setAddress(address).setZip(zip);
 }

 public String toSpam(){
   return EasyCursor.spamFrom(this);
 }

 public String toString(){
   return address+"@"+zip;
 }

} //$Id: AVSInfo.java,v 1.7 2004/03/08 17:19:09 andyh Exp $
