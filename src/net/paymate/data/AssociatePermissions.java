package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/AssociatePermissions.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.util.ErrorLogStream;


public class AssociatePermissions {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AssociatePermissions.class);

  public static final UserPermissions PermitdbAccess = new UserPermissions(UserPermissions.candatabase);
  public static final UserPermissions PermitWebAdmin = new UserPermissions(UserPermissions.canweb);
  public static final UserPermissions PermitReturn   = new UserPermissions(UserPermissions.canreturn);
  public static final UserPermissions PermitVoid     = new UserPermissions(UserPermissions.canvoid);
  public static final UserPermissions PermitSale     = new UserPermissions(UserPermissions.cansale);
  public static final UserPermissions PermitClose    = new UserPermissions(UserPermissions.canclose);

  public EnterprisePermissions enterprise = new EnterprisePermissions();
  public ClerkPrivileges store = new ClerkPrivileges();
  public AssociatePermissions() {
  }
  public void clear() {
    enterprise.clear();
    store.clear();
  }

  public static final AssociatePermissions grantAllForStore(AssociatePermissions perms) {
    if(perms == null) {
      perms = new AssociatePermissions();
    }
    perms.store.grantAllForStore();
    return perms;
  }

  public boolean permits(UserPermissions userlevel){
    boolean ret = false;
    try {
      dbg.Enter("permits()");
      if(userlevel != null) {
        switch(userlevel.Value()) {
          case UserPermissions.candatabase: {
            ret = enterprise.canDB;
          } break;
          case UserPermissions.canreturn: {
            ret = store.canREFUND;
          } break;
          case UserPermissions.cansale: {
            ret = store.canSALE;
          } break;
          case UserPermissions.canclose: {
            ret = store.canClose;
          } break;
          case UserPermissions.canvoid: {
            ret = store.canVOID;
          } break;
          case UserPermissions.canweb: {
            ret = enterprise.canWeb;
          } break;
        }
      } else {
        dbg.WARNING("Userlevel is null!");
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      dbg.VERBOSE("Checking "+userlevel.Image()+" permissions for store:"+store+", enterprise:"+enterprise+", returning " + ret);
      dbg.Exit();
      return ret;
    }
  }
  public String toString() {
    return "EnterprisePermissions: "+enterprise+", StorePermissions: "+store;
  }
}

// $Id: AssociatePermissions.java,v 1.10 2003/10/30 23:06:09 mattm Exp $

