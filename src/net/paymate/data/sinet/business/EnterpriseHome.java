package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/EnterpriseHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.ErrorLogStream;
import net.paymate.data.sinet.*;
import net.paymate.data.*;

public class EnterpriseHome extends CfgEntityHome {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EnterpriseHome.class);
  private static EnterpriseHome myhome = new EnterpriseHome(); // singleton

  ///////////////////////
  // STATICS
  public static final Enterprise Get(Enterpriseid id) {
    return myhome.get(id);
  }

  public static final Enterprise New(Associateid creator) {
    return myhome.knew(creator);
  }

  public static final Enterpriseid [ ] GetAllIds() {
    return (Enterpriseid [ ])myhome.preload(Enterpriseid.class);
  }

  // slower than the above query, most likely
  public static final Enterpriseid [ ] GetAllEnabledNameOrder() {
    return myhome.getAllEnabledNameOrder();
  }

//  // sometimes something needs to be handed around ... maybe
//  public static final EnterpriseHome Home() {
//    return myhome;
//  }
  // END STATICS
  ///////////////////////

  private EnterpriseHome() {
    super(new SinetClass(SinetClass.Enterprise), Enterprise.class);
  }

  private final Enterprise get(Enterpriseid id) {
    EntityBase base = super.get(id);
    return ((base != null) && (base instanceof Enterprise)) ? (Enterprise)base : (Enterprise)null;
  }

  public final Enterpriseid [ ] getAllEnabledNameOrder() {
    return ((BusinessCfgMgr)cfg()).getAllEnterprisesEnabledNameOrder();
  }

  private synchronized final Enterprise knew(Associateid creator) {
    EntityBase base = New(creator, Enterprise.class);
    if(base instanceof Enterprise) {
      Enterprise newEnterprise = (Enterprise) base;
      // create 1 store
      newEnterprise.newStore();
      return newEnterprise;
    } else {
      return null;
    }
  }

}