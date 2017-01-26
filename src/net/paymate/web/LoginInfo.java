package net.paymate.web;
/**
 * Title:        LoginInfo<p>
 * Description:  Info retreived from DB upon user login<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: LoginInfo.java,v 1.52 2004/01/31 01:07:56 mattm Exp $
 */

import  net.paymate.web.color.*;
import net.paymate.util.*;
import net.paymate.data.*; // ID's
import net.paymate.database.*; // until SS2
import net.paymate.lang.StringX;
import  net.paymate.connection.*;
import net.paymate.data.sinet.business.*;

public class LoginInfo {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LoginInfo.class);

  public Enterprise enterprise; // from the login
  public Store store; // the store where the terminal is located
  public Associate assoc;
  public AssociatePermissions permissions;
  // terminal stuff
  public TerminalInfo ti; //was already in the query!
  public Terminalid terminalID() { // passed in
    if(ti != null) {
      return ti.id();
    }
    return null;
  }
  public Associateid associateid() {
    if(assoc != null) {
      return assoc.associateid();
    }
    return null;
  }
  public String terminalName() { // from the terminal
    if(ti != null) {
      return ti.getNickName();
    }
    return "";
  }
  // status
  public boolean loggedIn = false;
  public String loginError;

  public static final LocalTimeFormat DEFAULTLTF = LocalTimeFormat.New("America/Chicago", LocalTimeFormat.DESCENDINGTIMEFORMAT);
  public LocalTimeFormat ltf() {
    return store == null ? DEFAULTLTF : LocalTimeFormat.New(store.timeZoneStr(),store.receipttimeformat);
  }

  public boolean isEnabled(){
    return !requiresAssociate() || (assoc != null && assoc.enabled);
  }

  public ColorScheme colors() {
    return ColorScheme.schemeForName(assoc.colorschemeid);
  }

  /**
   * @return whether a human is trying to login
   * if no terminal then must be web login and must have priveleges
   * else some terminals require a human to operate them.
   */
  public boolean requiresAssociate(){
    return ti==null || ti.requiresLogin();
  }

  public boolean permits(UserPermissions userlevel){
    return (permissions != null) && isEnabled() && permissions.permits(userlevel);
  }

  public LoginInfo() {
    clear();
  }

  public String toString() {
    return ""+enterprise+"/"+store+"/"+terminalID()+":"+terminalName()+"/"+assoc;
  }

  public String forDisplay() {
    // need to put enterprisename, storename, and username, or at least enterprisename and username, or at least username, depends on how we handle this!
    return loggedIn ? assoc.firstMiddleLast() + " of " + store.storename + " [" + ltf().getZone().getID() + "]" : "";
  }

  public void logout() {
    try {
      sessionMon.getMonitor();
      loggedIn = false;
      // now, make this user NOT a god
      if(permissions != null) {
        permissions.clear();
      }
    } catch (Throwable e) {
      dbg.Caught(e);
    } finally {
      sessionMon.freeMonitor();
    }
  }

  public final Monitor sessionMon = new Monitor("UserSession");

  public LoginInfo clear() {
    enterprise     = null;
    assoc          = null;
    store          = null;
    loginError     = "";
    loggedIn       = false;
    permissions    = new AssociatePermissions();
    ti             = null;
    return this;
  }

  public static final void main(String [] args) {
    if(args.length < 1) {
      System.out.println("Must specify filename.");
    } else {
      String filename = args[0];
      System.out.println("Running EC3Kalpha2numericConversion on file " + filename);
      try {
        java.io.BufferedReader bis = new java.io.BufferedReader(new java.io.FileReader(filename));
        if(bis != null) {
          String line = null;
          while((line = bis.readLine()) != null) {
            if(StringX.NonTrivial(line)) {
              System.out.println(line+"="+EC3Kalpha2numericConversion(line));
            }
          }
        }
      } catch (Exception e) {
        System.out.println("exception: " + e);
      }
    }
  }

  // alpha to numeric conversion strings
  private static final String alphas   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String numerics = "22233344455566671778889991";

  /**
   * For converting username and passwords deom alpha to numeric, for EC3K typing.
   */
  public static final String EC3Kalpha2numericConversion(String alpha) {
    StringBuffer sb = new StringBuffer(alpha.length());
    char [] chars = new char[alpha.length()];
    for(int i = alpha.length(); i-->0;) {
      char Char = alpha.charAt(i);
      int where = alphas.indexOf(Char);
      if(where > -1) {
        chars[i] = numerics.charAt(where);
      }
    }
    return new String(chars);
  }

  public final boolean isaGod() {
    return permits(AssociatePermissions.PermitdbAccess);
  }

  public boolean permissionsValid(ActionRequest request) {
    ActionType type = request.Type();
    switch(type.Value()) {
      //internal admin requests, never initiatied by a human
      case ActionType.gateway:
      case ActionType.update:
      case ActionType.receiptStore:
      case ActionType.receiptGet://+_+ should have some privelege
        return true; //{}
      case ActionType.batch:
      case ActionType.store: return permits(AssociatePermissions.PermitClose);
      case ActionType.clerkLogin: return true;//permissions are now checked deeper in system, the only thing we might check here is the 'Associate.enable'
      // these next few are all extensions of the financial request, more or less
      case ActionType.payment: {
        PaymentRequest pr = (PaymentRequest)request;
        TransferType tt = pr.OperationType();
        switch(tt.Value()) {
          case TransferType.Reversal: return permits(AssociatePermissions.PermitVoid);
          case TransferType.Return: return permits(AssociatePermissions.PermitReturn);
          case TransferType.Authonly:
          case TransferType.Force:
          case TransferType.Sale:
          case TransferType.Modify: {
            return permits(AssociatePermissions.PermitSale);
          }
        }
        return false;
      }
      case ActionType.adminWeb: return permits(AssociatePermissions.PermitWebAdmin);
      case ActionType.admin:
      case ActionType.unknown:
      default: return false; //{}// never let these pass (should never get here ???)
    }
  }

  // the permissions lists for the following commands ...
  private static final UserPermissions [] UPWEB = {AssociatePermissions.PermitWebAdmin};
  private static final UserPermissions [] UPWEBDB = {AssociatePermissions.PermitWebAdmin, AssociatePermissions.PermitdbAccess};

  private static UserPermissions[][] requiredPermissions;
  static {
    // set the permissions
    requiredPermissions = new UserPermissions[(new AdminOpCode()).numValues()][];
    // first of all, assume that EVERY ONE of them is UPWEBDB unless otherwise noted
    // this will prevent new items from accidentally leaking to the public
    for(int i=requiredPermissions.length; i-->0;) {
      requiredPermissions[i] = UPWEBDB;
    }
    // now, set the exceptions (basically grant priveleges to peons)
    requiredPermissions[AdminOpCode.appliances] = UPWEB;
    requiredPermissions[AdminOpCode.appliance] = UPWEB;
    requiredPermissions[AdminOpCode.associates] = UPWEB;
    requiredPermissions[AdminOpCode.batch] = UPWEB;
    requiredPermissions[AdminOpCode.defaultOp] = UPWEB; // anybody, but never gets checked anyway
    requiredPermissions[AdminOpCode.deposit] = UPWEB;
    requiredPermissions[AdminOpCode.deposits] = UPWEB;
    requiredPermissions[AdminOpCode.drawer] = UPWEB;
    requiredPermissions[AdminOpCode.news] = UPWEB;
    requiredPermissions[AdminOpCode.batches] = UPWEB;
    requiredPermissions[AdminOpCode.drawers] = UPWEB;
    requiredPermissions[AdminOpCode.stores] = UPWEB;
    requiredPermissions[AdminOpCode.terminals] = UPWEB;
    requiredPermissions[AdminOpCode.transaction] = UPWEB;
    requiredPermissions[AdminOpCode.txnSearch] = UPWEB;
  }

  public final boolean permissionsValid(AdminOpCode op) {
    dbg.VERBOSE("Checking permissions for op = " + op + ", for logininfo = " + this);
    if(isaGod()) {
      return true; // for now, gods can do ANYTHING!
    }
    if((op == null) || !op.isLegal()) {
      return false;
    }
    UserPermissions [] reqs = requiredPermissions[op.Value()];
    if(reqs == null) {
      return false; // bug !!! +++ PANIC!!! coding error
    }
    boolean ret = true;
    for(int i = reqs.length; i-->0;) {
      ret &= permits(reqs[i]); // if any of these are false, the result will be false
    }
    return ret;
  }

  ///
  // from usersession
  /**
   * web logins recheck all session info
   * @param request
   * @param clerk
   * @param enterpriseID
   * @param terminalID
   * @param checkPerms
   * @return
   */
  public ActionReplyStatus web_login(ActionRequest request, ClerkIdInfo clerk, Enterpriseid enterpriseID, boolean checkPerms) { // maybe shouldn't be synchronized (monitored)?
    // start with generic 'error' in case something blows
    ActionReplyStatus stat = new ActionReplyStatus(ActionReplyStatus.ServerError);
    try {
      sessionMon.getMonitor();
      sessionMon.name = "UserSession." + enterpriseID + "." + clerk.Name();
      loggedIn = false;
      int count = getWebLoginInfo(clerk, enterpriseID);
      switch(count) {
        case 0:{//if web login then likely to be bad name or password
          stat.setto(ActionReplyStatus.InvalidLogin);
          loginError = "FAILED LOGIN ATTEMPT!  Please try again. \n"+loginError;
        } break;
        case 1:{//we have a user , enterprise, and store.
          if(!isEnabled()) {
            dbg.WARNING("User " + clerk.Name() + " NOT enabled!");
            stat.setto(ActionReplyStatus.InvalidLogin);
            loginError = "FAILED LOGIN ATTEMPT!  Tell your Manager";
          } else {
//we do check user permission early on the web, so that terminal type is not factored in.
            if(checkPerms && ! permissionsValid(request)) {
              loginError = "Action type " + request.Type().Image() + " is not allowed for user " + clerk.Name();
              dbg.WARNING(loginError);
              stat.setto(ActionReplyStatus.InsufficientPriveleges);
            } else {// EVERYTHING IS GOOD!  YOU ARE LOGGED IN!
              dbg.VERBOSE("logged in userid=" + assoc);
              loginError = "logged in ";
              loggedIn = true;
              stat.setto(ActionReplyStatus.Success); // default for the case where nothing goes wrong
            }
          }
        } break;
        default:{//not enough detail to get a unique login
          stat.setto(ActionReplyStatus.InsufficientDetail);
          loginError = "Please enter your enterpriseID and try again.  Contact Paymate.net for your EnterpriseID";
        } break;
      }
      dbg.VERBOSE(toString() + ", ar.status=" + stat.Image());
      // returning an ActionReplyStatus makes it easy, but there is probably a better way.
      // like what about the loginError?
    } catch (Throwable e) {
      dbg.Caught(e);
      loginError +="\n Exception in web login";
    } finally {
      sessionMon.freeMonitor();
    }
    return stat;
  }

  /**
    * return 0=NOLOGIN, 1=GOODLOGIN, 2+ =MOREINFONEEDED (failed due to too many matches)
    */
  public int getWebLoginInfo(ClerkIdInfo clerk, Enterpriseid enterpriseID) {
    clear(); //erase all memory of stuff before this login attempt
    try {
      // first, find the enterprise
      if (!Enterpriseid.isValid(enterpriseID)) { //enterprise not provided
        //so try to get it from associate info alone (only web logins should get here)
        Associate [ ] assocs = AssociateHome.GetAllByName(clerk.Name());
        if (assocs.length != 1) {
          return assocs.length;
        } else {
          enterprise = assocs[0].enterprise;
        }
      } else {
        enterprise = EnterpriseHome.Get(enterpriseID);
      }
      // then the store
      store = null;
      if(enterprise != null) {
        Store[ ] stores = enterprise.stores.getAll();
        if (stores.length > 0) {
          store = stores[0];
        }
      }
      if (store != null) {
        // now, try to login the user
        if (getPermissionsInfo(clerk)) {
          return 1;
        }
      } else { //probably can't happen, databsae is very corrupt to get here
        dbg.ERROR("Store::Enterprise incoherence in login attempt.");
      }
      return 0;
    } catch (Exception caught) {
      dbg.ERROR("Exception retreiving login info for web user=" + clerk.toSpam() +
                ", entid=" + enterpriseID + ":");
      //+++ log to events table. as loginfailure
      dbg.Caught(caught);
      loginError = "Exception in getWebLoginInfo";
      return 0;
    }
  }

  public boolean getTerminalLoginInfo(String loginname, Terminalid terminalID, String password) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    try {
      clear(); // so that legacy stuff doesn't hang around ... just in case
      if (Terminalid.isValid(terminalID)) {
        // get the terminal info:
        ti = db.getTerminalInfo(terminalID);
        // look up the associate!
        Store store = StoreHome.Get(db.getStoreForTerminal(terminalID));
        if(store != null) {
          this.store = store;
          this.enterprise = store.enterprise;
          return getPermissionsInfo(new ClerkIdInfo(loginname, password));
        }
      }
      return false;
    } catch (Exception caught) {
      dbg.ERROR("Exception retreiving login info for user=" + loginname +
                ", termguid=" + terminalID + ":");
      //+++ log to events table. as loginfailure
      dbg.Caught(caught);
      loginError="Exception in getTerminalLoginInfo";
      return false;
    }
  }

  private boolean getPermissionsInfo(ClerkIdInfo clerk){
    boolean requiresAssociate = requiresAssociate();
    dbg.ERROR("Does " + (requiresAssociate ? "" : "NOT ") + "require associate!");
    if (requiresAssociate) { // login not required for this terminal, so set the permissions accordingly
      return getAssociateInfo(clerk);
    } else {
      permissions = AssociatePermissions.grantAllForStore(permissions);
      return true;
    }
  }

  /**
   * return 0=NOLOGIN, 1=GOODLOGIN
   * --- this entire function is a cheat to keep PayMateDB from compiling LoginInfo!
   * --- when we go to SS2, this will be resolved and moved out of here
   * +++ go to SS2 so we can get this resolved and moved out of here
   */
  public boolean getAssociateInfo(ClerkIdInfo clerk) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    Associate [ ] assocs =
        AssociateHome.GetAllByNameEnterprisePW(clerk.Name(),
                                               enterprise.enterpriseid(),
                                               clerk.Password());
    net.paymate.database.ours.table.StoreaccessTable storeaccess = db.storeaccess;
    switch(assocs.length) {
      case 1: {
        assoc = assocs[0]; // just use the first one, I guess
        StoreAccessid said = db.getStoreaccess(assoc.associateid(), store.storeId());
        EasyProperties saidz = db.getRecordProperties(storeaccess, said);
        permissions.store.canSALE = saidz.getBoolean(storeaccess.ensale.name());
        permissions.store.canREFUND = saidz.getBoolean(storeaccess.enreturn.name());
        permissions.store.canClose= saidz.getBoolean(storeaccess.enclosedrawer.name());
        permissions.store.canVOID = saidz.getBoolean(storeaccess.envoid.name());
        permissions.enterprise.canWeb = assoc.eperms.canWeb;
        permissions.enterprise.canDB = assoc.eperms.canDB;
        permissions.enterprise.canViewAuthMsgs = assoc.eperms.canViewAuthMsgs;
        // for now, force piggybacks sale
        permissions.store.canMOTO = permissions.store.canSALE;
        // for debugging, log what we have here
        dbg.WARNING("Logged in: assoc="+assoc+", said="+said+", saidz="+saidz+", logininfo="+this+".");
        return true; //success
      } // break;
      default:
      case 0: {
        loginError="password error";
        return false; //password failure
      } // break;
    }
  }


  // does return that the terminal is logged in or that the terminal needs login?
  public boolean terminalLogin(ActionRequest request) {
    boolean get = getTerminalLoginInfo(request.clerk.Name(), request.terminalid, request.clerk.Password());
    if(request.isAutomatedRequest()){
      permissions=null;//appliance can't do nuthin.
      return true;
    } else {
      return get;
    }
  }


}
//$Id: LoginInfo.java,v 1.52 2004/01/31 01:07:56 mattm Exp $
