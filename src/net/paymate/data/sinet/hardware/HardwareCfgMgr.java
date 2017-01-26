package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/HardwareCfgMgr.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.data.sinet.ConfigurationManager;
import net.paymate.data.sinet.EntityBase;
import net.paymate.data.UniqueId;
import net.paymate.data.sinet.SinetClass;

public interface HardwareCfgMgr  {

  public Applianceid getApplianceByName(String applname);
// no persistence of appliance log data 20040523 !
//  public ApplNetStatusid getLastApplNetStatus(Applianceid parent);
//  public ApplPgmStatusid getLastApplPgmStatus(Applianceid parent, boolean connection);

}