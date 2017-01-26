package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/ClerkIdInfo.java,v $<p>
* Description:  clerk (associate) identifying info<p>
* Copyright:    Paymate.net 2000<p>
* Company:      paymate.net<p>
* @author paymate
* @version $Revision: 1.20 $
*/

import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;

public class ClerkIdInfo implements isEasy  {
  private String name      ="";
  private String password  ="";
  public static final String userIDKey                = "userID";
  public static final String passwordKey              = "password";

  public static ClerkIdInfo Auto(Object instance){
    ClerkIdInfo newone=new ClerkIdInfo();
    newone.name="AutoClerk";
    newone.password=ReflectX.justClassName(instance);
    return newone;
  }

  public Integer fastHash(){
    return new Integer(name.hashCode()+password.hashCode());
  }

  public void save(EasyCursor ezp){
    ezp.setString (userIDKey,    Name());
    ezp.setString (passwordKey,  Password());
  }

  public void load(EasyCursor ezp){
    setName(ezp.getString (userIDKey)).
    setPass(ezp.getString (passwordKey));
 }

  public ClerkIdInfo setName(String s){
    name= StringX.OnTrivial(s, "").trim();
    return this;
  }

  public ClerkIdInfo setPass(String s){
    password=StringX.OnTrivial(s, "").trim();
    return this;
  }
  /**
   * @return whether login info is realistic
   */
  public boolean NonTrivial(){
    //third term added for a one-time bug in the hypercom iceTerminal program
    return StringX.NonTrivial(name)&&StringX.NonTrivial(password) ;
  }

  public static boolean NonTrivial(ClerkIdInfo probate){
    return probate!=null && probate.NonTrivial();
  }

  public ClerkIdInfo Clear(){
    return setName("").setPass("");
  }

  public String Name(){
    return name;
  }

  public String Password(){
    return password;
  }

  public void killPassword(){
    password="";
  }

  public boolean Passes(String entered){
    return StringX.NonTrivial(password) && StringX.equalStrings(password, entered);
  }

  public ClerkIdInfo(String nomen, String passgas){
    setName(nomen);
    setPass(passgas);
  }

  public ClerkIdInfo(ClerkIdInfo rhs){
    this(new String(rhs.name),new String(rhs.password));
  }

  public ClerkIdInfo() {
    //live with static inits
  }

  public String toSpam(){
    return "userid="+Name()+", pass=" + Password()+";" ;
  }

  public String toString() {
    return "userid="+Name();
  }

}
//$Id: ClerkIdInfo.java,v 1.20 2003/10/29 08:33:13 mattm Exp $
