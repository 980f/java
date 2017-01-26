package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/AssociateHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.ErrorLogStream;
import net.paymate.data.sinet.*;
import net.paymate.data.*;

public class AssociateHome extends CfgEntityHome {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AssociateHome.class);
  private static AssociateHome myhome = new AssociateHome(); // singleton

  ///////////////////////
  // STATICS
  public static final Associate Get(Associateid id) {
    return myhome.get(id);
  }

  /* package - really meant for Enterprise only */
  static final Associate New(Enterpriseid creator) {
    return myhome.knew(creator);
  }

  public static final Associateid [ ] GetAllIds() {
    return (Associateid [ ])myhome.preload(Associateid.class);
  }

  public static final Associate [ ] GetAllByName(String loginname) {
    Associateid [ ] a = ((BusinessCfgMgr)cfg()).getAssociatesByLoginname(loginname);
    return ASSOCIATEARRAYFROMIDARRAY(a);
  }

  public static final Associate [ ] GetAllByNameEnterprise(String loginname, Enterpriseid entid) {
    Associateid [ ] a = ((BusinessCfgMgr)cfg()).getAssociatesByLoginnameEnt(loginname, entid);
    return ASSOCIATEARRAYFROMIDARRAY(a);
  }

  public static final Associate [ ] GetAllByNameEnterprisePW(String loginname, Enterpriseid entid, String password) {
    Associateid [ ] a = ((BusinessCfgMgr)cfg()).getAssociatesByLoginnameEntPW(loginname, entid, password);
    return ASSOCIATEARRAYFROMIDARRAY(a);
  }

  private static final Associate [ ] EMPTYASSOCLIST = new Associate [0];
  private static final Associate [ ] ASSOCIATEARRAYFROMIDARRAY(Associateid [ ] ids) {
    Associate[] ret = EMPTYASSOCLIST;
    if(ids != null) {
      ret = new Associate[ids.length];
      for (int i = ids.length; i-- > 0; ) {
        ret[i] = Get(ids[i]);
        Associateid b = ret[i].associateid();
      }
    }
    return ret;
  }
  // END STATICS
  ///////////////////////

  private AssociateHome() {
    super(new SinetClass(SinetClass.Associate), Associate.class);
  }

  private final Associate get(Associateid id) {
    EntityBase base = super.get(id);
    return ((base != null) && (base instanceof Associate)) ? (Associate)base : (Associate)null;
  }

  private synchronized final Associate knew(Enterpriseid creator) {
    EntityBase base = New(creator, Associate.class);
    if(base instanceof Associate) {
      Associate newAssoc = (Associate) base;
      // create storeaccess! +++
      return newAssoc;
    } else {
      return null;
    }
  }

}