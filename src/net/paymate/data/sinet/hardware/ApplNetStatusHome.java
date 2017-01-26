package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplNetStatusHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

// NO MORE PERSISTENCE!  20040523

import net.paymate.util.ErrorLogStream;
import net.paymate.data.sinet.*;

public class ApplNetStatusHome extends LogEntityHome {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ApplNetStatusHome.class);
  private static final SinetClass mySinetClass = new SinetClass(SinetClass.ApplNetStatus);
  private static ApplNetStatusHome myhome = new ApplNetStatusHome();

  ///////////////////////
  // STATICS
//  public static final ApplNetStatus Get(ApplNetStatusid id) {
//    return myhome.get(id);
//  }
//
  public static final ApplNetStatus New(Applianceid parent) {
    return new ApplNetStatus();//myhome.knew(parent);
  }


//  public static final ApplNetStatus GetLastUpdate(Applianceid parent) {
//    ApplNetStatusid id = ((HardwareCfgMgr)cfg()).getLastApplNetStatus(parent); // blows if we miscoded our system !
//    return Get(id);
//  }
  // END STATICS
  ///////////////////////

  public ApplNetStatusHome() {
    super(mySinetClass);
  }

//  private final ApplNetStatus get(ApplNetStatusid id) {
//    ApplNetStatus e = null;
//    if(ApplNetStatusid.isValid(id)) {
//      e = (ApplNetStatus) super.getEntity(id, ApplNetStatus.class);
//    } else {
//      dbg.ERROR("Cannot Get ApplNetStatus for invalid ApplNetStatusid ["+id+"]!");
//    }
//    return e;
//  }
//
//  private synchronized final ApplNetStatus knew(Applianceid creator) {
//    EntityBase base = New(creator, ApplNetStatus.class);
//    if(base instanceof ApplNetStatus) {
//      ApplNetStatus knew = (ApplNetStatus) base;
//      return knew;
//    } else {
//      return null;
//    }
//  }

}