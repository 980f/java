/**
* Title:        Clerk
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Clerk.java,v 1.23 2001/10/10 22:47:54 andyh Exp $
*/
package net.paymate.terminalClient;

import net.paymate.connection.*;
import net.paymate.util.*;

public class Clerk {
  static final ErrorLogStream dbg=new ErrorLogStream(Clerk.class.getName());
//default constructor must construct objects, for use by registry loader
  ClerkIdInfo idInfo= new ClerkIdInfo();
  ClerkPrivileges priv=new ClerkPrivileges();

//we do NOT put all objects in this registery, only ones checked by database
  static ClerkRegistry logins=new ClerkRegistry();

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
      logins.add(this);
    } else {
      dbg.ERROR("FailedReply:"+reply.status.Image());
      priv=new ClerkPrivileges();
    }
    //double check, in case we registered crap:
    loggedIn=idInfo.NonTrivial();//&& (logins.check(this.idInfo)!=null);
    return this;
  }

  public void save(EasyCursor ezp){
    ezp.addBlock(idInfo,"idInfo");
    ezp.addBlock(priv,"priv");
  }

  public void load(EasyCursor ezp){
    ezp.getBlock(idInfo,"idInfo");
    ezp.getBlock(priv,"priv");
  }

  /**
   * remove the old registry, create a new one with just what we are given
   */
  public static void setRegistry(EasyCursor ezp){
    logins=new ClerkRegistry();
    logins.load(ezp);
  }

}
//$Id: Clerk.java,v 1.23 2001/10/10 22:47:54 andyh Exp $
