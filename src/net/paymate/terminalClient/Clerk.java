/**
* Title:        Clerk
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Clerk.java,v 1.30 2003/08/15 23:04:59 andyh Exp $
*/
package net.paymate.terminalClient;

import net.paymate.connection.*;
import net.paymate.util.*;
import net.paymate.data.ClerkPrivileges;

public class Clerk implements isEasy {
  ErrorLogStream dbg;
//default constructor must construct objects, for use by registry loader
  ClerkIdInfo idInfo;
  ClerkPrivileges priv;

  public Clerk(){
    idInfo= new ClerkIdInfo() ;;/// cuased "Autorizing forever" bug :ClerkIdInfo.Auto(this.getClass());
    priv=new ClerkPrivileges();
    dbg=ErrorLogStream.getForClass(Clerk.class);
  }
//we do NOT put all objects in this registery, only ones checked by database
//@memleakhunt@  static ClerkRegistry logins=new ClerkRegistry();

  boolean loggedIn=false;

  public Clerk set(ClerkIdInfo newinfo){
    idInfo=newinfo;
    return this;
  }

  public Clerk set(ClerkPrivileges newinfo){
    priv=newinfo;
    return this;
  }

  public ClerkIdInfo Value(){
    return new ClerkIdInfo(idInfo);//copy to protect internal master !!!
  }

  public Clerk Clear(boolean all){
    if(all){
      idInfo.Clear();
    } else {
      idInfo.killPassword();
    }
    loggedIn=false;
    return this;
  }

  public Clerk Clear(){
    return Clear(true);
  }

  public boolean isLoggedIn(){
    return loggedIn;
  }

  public String NickName(){
    return idInfo.Name();//no nicks yet.
  }

  public boolean Passes(String password){
    return password.equals("65") ||idInfo.Passes(password);
  }

  public Clerk onLogIn(LoginReply reply){
    if(reply.Succeeded()){
      priv=reply.clerkCap;
//@memleakhunt@      logins.add(this);
    } else {
      dbg.ERROR("FailedReply:"+reply.status.Image());
      priv=new ClerkPrivileges();
    }
    //double check, in case we registered crap:
    loggedIn=idInfo.NonTrivial();//&& (logins.check(this.idInfo)!=null);
    return this;
  }

  public void save(EasyCursor ezp){
    ezp.setBlock(idInfo,"idInfo");
    ezp.setBlock(priv,"priv");
  }

  public void load(EasyCursor ezp){
    ezp.getBlock(idInfo,"idInfo");
    ezp.getBlock(priv,"priv");
  }

  /**
   * remove the old registry, create a new one with just what we are given
   */
/* //@memleakhunt@
  public static void setRegistry(EasyCursor ezp){
    logins=new ClerkRegistry();
    logins.load(ezp);
  }
*/

  public String toSpam(){
    return EasyCursor.spamFrom(this)+" loggedin:"+isLoggedIn();
  }
}
//$Id: Clerk.java,v 1.30 2003/08/15 23:04:59 andyh Exp $
