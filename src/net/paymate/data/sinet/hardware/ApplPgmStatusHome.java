package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplPgmStatusHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

// NO MORE PERSISTENCE!  20040523

import net.paymate.util.ErrorLogStream;
import net.paymate.data.sinet.*;

public class ApplPgmStatusHome extends LogEntityHome {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ApplPgmStatusHome.class);
  private static final SinetClass mySinetClass = new SinetClass(SinetClass.ApplPgmStatus);
  private static ApplPgmStatusHome THE = new ApplPgmStatusHome();

  ///////////////////////
  // STATICS
//  public static final ApplPgmStatus Get(ApplPgmStatusid id) {
//    return THE.get(id);
//  }

  public static final ApplPgmStatus New(Applianceid parent) {
    return new ApplPgmStatus();// THE.knew(parent);
  }

//  public static final ApplPgmStatus GetLastConnection(Applianceid parent) {
//    return GetLastUpdate(parent, true);
//  }
//
//  public static final ApplPgmStatus GetLastNonConnection(Applianceid parent) {
//    return GetLastUpdate(parent, false);
//  }
//
//  private static final ApplPgmStatus GetLastUpdate(Applianceid parent, boolean connection) {
//    ApplPgmStatusid id = ((HardwareCfgMgr)cfg()).getLastApplPgmStatus(parent, connection); // blows if we miscoded our system !
//    return Get(id);
//  }

  // END STATICS
  ///////////////////////

  public ApplPgmStatusHome() {
    super(mySinetClass);
  }

//  private final ApplPgmStatus get(ApplPgmStatusid id) {
//    ApplPgmStatus e = null;
//    if(ApplPgmStatusid.isValid(id)) {
//      e = (ApplPgmStatus) super.getEntity(id, ApplPgmStatus.class);
//    } else {
//      dbg.ERROR("Cannot Get ApplPgmStatus for invalid ApplPgmStatusid ["+id+"]!");
//    }
//    return e;
//  }

//  private synchronized final ApplPgmStatus knew(Applianceid creator) {
//    EntityBase base = New(creator, ApplPgmStatus.class);
//    if(base instanceof ApplPgmStatus) {
//      ApplPgmStatus knew = (ApplPgmStatus) base;
//      return knew;
//    } else {
//      return null;
//    }
//  }

}