/* $Id: LoginReply.java,v 1.21 2001/07/18 22:00:16 andyh Exp $ */
package net.paymate.connection;
import net.paymate.util.*;
import net.paymate.terminalClient.*;

public class LoginReply extends ActionReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.clerkLogin);
  }

  public ClerkPrivileges clerkCap=new ClerkPrivileges();

  public static final String clerkCapKey = "clerkCap";

  public void save(EasyCursor ezp){
    super.save(ezp);
    clerkCap.saveas(clerkCapKey, ezp);
  }

  public LoginReply setCaps(EasyCursor ezp){
    clerkCap.loadfrom(clerkCapKey,ezp);
    return this;
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    clerkCap.loadfrom(clerkCapKey,ezp);
  }

  public LoginReply(){
  //defaults
  }

  public LoginReply(EasyCursor ezp){
    setCaps(ezp);
  }

  // is this function necessary? FUE +_+ use enum
  public LoginReply(/*ActionReplyStatus*/ int newStatus) {
    super(newStatus);
  }

}
//$Id: LoginReply.java,v 1.21 2001/07/18 22:00:16 andyh Exp $
