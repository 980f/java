package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplianceHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

import net.paymate.util.ErrorLogStream;
import net.paymate.data.sinet.*;
import net.paymate.data.sinet.business.*;

public class ApplianceHome extends CfgEntityHome {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ApplianceHome.class);
  private static ApplianceHome myhome = new ApplianceHome(); // singleton

  ///////////////////////
  // STATICS
  public static final Appliance Get(Applianceid id) {
    return myhome.get(id);
  }

  public static final Appliance New(Storeid parent) {
    return myhome.knew(parent);
  }

  public static final Appliance GetByName(String applname) {
    Applianceid a = ((HardwareCfgMgr)cfg()).getApplianceByName(applname); // blows if we miscoded our system !
    Appliance ret = Get(a);
    Applianceid b = ret.applianceid();
    // GetByName(FORCETIPS11)=83, and 83!=17977, and FORCETIPS11==FORCETIPS11
    dbg.ERROR("GetByName("+applname+")="+a+", and "+a+(a.equals(b) ? "==" : "!=")+b+", and " + applname+(net.paymate.lang.StringX.equalStrings(applname, ret.applname) ? "==" : "!=")+ret.applname);
    return ret;
  }

  public static final int Size() {
    return myhome.size();
  }

  public static final Applianceid [ ] GetAllIds() {
    return (Applianceid [ ])myhome.preload(Applianceid.class);
  }

  // END STATICS
  ///////////////////////

  public final int size() {
    return cache.size(myclass); // +++ rewrite with getall().length ?
  }

  public ApplianceHome() {
    super(new SinetClass(SinetClass.Appliance), Appliance.class);
  }

  private final Appliance get(Applianceid id) {
    EntityBase base = super.get(id);
    return ((base != null) && (base instanceof Appliance)) ? (Appliance)base : (Appliance)null;
  }

  private synchronized final Appliance knew(Storeid creator) {
    EntityBase base = New(creator, Appliance.class);
    if(base instanceof Appliance) {
      Appliance newEnterprise = (Appliance) base;
      return newEnterprise;
    } else {
      return null;
    }
  }

}