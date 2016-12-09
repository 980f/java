package net.paymate.connection;

/**
* Title:        null<p>
* Description:  null<p>
* Copyright:    null<p>
* Company:      paymate<p>
* @author paymate
* @version $Id: ClerkIdInfo.java,v 1.12 2001/07/18 22:00:15 andyh Exp $
*/

import net.paymate.util.*;

public class ClerkIdInfo implements isEasy  {
  protected String name      ="";
  protected String password  ="";
  public static final String userIDKey                = "userID";
  public static final String passwordKey              = "password";

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
    name= Safe.OnTrivial(s, "").trim();
    return this;
  }

  public ClerkIdInfo setPass(String s){
    password=Safe.OnTrivial(s, "").trim();
    return this;
  }

  public boolean NonTrivial(){
    return Safe.NonTrivial(name)&&Safe.NonTrivial(password);
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
    return Safe.NonTrivial(password)&& password.equals(entered);
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

}
//$Id: ClerkIdInfo.java,v 1.12 2001/07/18 22:00:15 andyh Exp $
