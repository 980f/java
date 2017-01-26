/* $Id: LoginReply.java,v 1.24 2003/08/15 23:04:57 andyh Exp $ */
package net.paymate.connection;
import net.paymate.data.ClerkPrivileges;
import net.paymate.util.*;
import net.paymate.terminalClient.*;
import net.paymate.data.*;

public class LoginReply extends ActionReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.clerkLogin);
  }

  public ClerkPrivileges clerkCap=new ClerkPrivileges();

  public static final String clerkCapKey = "clerkCap";

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setBlock(clerkCap,clerkCapKey);
  }

  public LoginReply setCaps(EasyCursor ezp){
    ezp.getBlock(clerkCap,clerkCapKey);
    return this;
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    ezp.getBlock(clerkCap,clerkCapKey);
  }

  public LoginReply(){
  //defaults
  }

  public LoginReply(EasyCursor ezp){
    setCaps(ezp);
  }

//  // is this function necessary? FUE +_+ use ennum
//  public LoginReply(/*ActionReplyStatus*/ int newStatus) {
//    super(newStatus);
//  }

}
//$Id: LoginReply.java,v 1.24 2003/08/15 23:04:57 andyh Exp $
