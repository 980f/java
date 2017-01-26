package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/BusinessCfgMgr.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.data.sinet.*;
import net.paymate.data.UniqueId;

public interface BusinessCfgMgr  {
  // associateids
  public Associateid [ ] getAssociatesByLoginname(String loginname);
  public Associateid [ ] getAssociatesByLoginnameEnt(String loginname, Enterpriseid entid);
  public Associateid [ ] getAssociatesByLoginnameEntPW(String loginname, Enterpriseid entid, String password);
  // enterpriseids
  public Enterpriseid [ ] getAllEnterprisesEnabledNameOrder();
  // storeids
  public Storeid [ ] getAllStoresTxnCountOrder();

  // get +++ ???
}