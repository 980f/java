package net.paymate.web;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/AdminOp.java,v $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

//import net.paymate.data.UserPermissions;

public class AdminOp {
  private String name;
  private AdminOpCode code;

  public AdminOp(String name, AdminOpCode code) {
    this.name=name;
    this.code=code;
  }
  public static final String adminPrefix = "adm";
  public String name() {
    return name;
  }
  public String url() {
    return (code != null) ? (adminPrefix + "=" + code.Image()) : "";
  }
  public String codeImage() {
    return code.Image();
  }
  public AdminOpCode code() {
    return new AdminOpCode(code.Value());
  }

  // the menu items ...
  public static final AdminOp NewsAdminOp      = new AdminOp("News"       ,new AdminOpCode(AdminOpCode.news));
  public static final AdminOp DrawersAdminOp   = new AdminOp("Drawers"    ,new AdminOpCode(AdminOpCode.drawers));
  public static final AdminOp BatchesAdminOp   = new AdminOp("Batches"    ,new AdminOpCode(AdminOpCode.batches));
  public static final AdminOp DepositsAdminOp  = new AdminOp("Deposit"    ,new AdminOpCode(AdminOpCode.deposits));
  public static final AdminOp SearchAdminOp    = new AdminOp("Search"     ,new AdminOpCode(AdminOpCode.txnSearch));
  public static final AdminOp AppliancesAdminOp= new AdminOp("Appliances" ,new AdminOpCode(AdminOpCode.appliances));
  public static final AdminOp StoresAdminOp    = new AdminOp("Stores"     ,new AdminOpCode(AdminOpCode.stores));
  public static final AdminOp EnterprisesAdminOp= new AdminOp("Enterprises" ,new AdminOpCode(AdminOpCode.enterprises));
  public static final AdminOp AssociatesAdminOp= new AdminOp("Associates" ,new AdminOpCode(AdminOpCode.associates));
  public static final AdminOp TerminalsAdminOp = new AdminOp("Terminals"  ,new AdminOpCode(AdminOpCode.terminals));
  public static final AdminOp debugpg          = new AdminOp("Services"   ,new AdminOpCode(AdminOpCode.services));
  public static final AdminOp authMsgsOp       = new AdminOp("Authorizer Msgs" ,new AdminOpCode(AdminOpCode.authAttempts));
  // these are not in the menu, but are used as general commands ...
  public static final AdminOp DetailsAdminOp   = new AdminOp(""           ,new AdminOpCode(AdminOpCode.transaction));
  public static final AdminOp servicepg        = new AdminOp("SERVICE"    ,new AdminOpCode(AdminOpCode.service));
  public static final AdminOp b1pg             = new AdminOp("one batch"  ,new AdminOpCode(AdminOpCode.batch));
  public static final AdminOp t1pg             = new AdminOp("one terml"  ,new AdminOpCode(AdminOpCode.terminal));
  public static final AdminOp changeEnterprise = new AdminOp("changeEnterprise",new AdminOpCode(AdminOpCode.changeEnterprise));
  public static final AdminOp newEnterprise    = new AdminOp("newEnterprise",new AdminOpCode(AdminOpCode.newEnterprise));
  public static final AdminOp newAssociate     = new AdminOp("newAssociate",new AdminOpCode(AdminOpCode.newAssociate));
  public static final AdminOp newAppliance     = new AdminOp("newAppliance",new AdminOpCode(AdminOpCode.newAppliance));
  public static final AdminOp newTerminal      = new AdminOp("newTerminal",new AdminOpCode(AdminOpCode.newTerminal));
  public static final AdminOp newStoreauth     = new AdminOp("newStoreauth",new AdminOpCode(AdminOpCode.newStoreauth));
  public static final AdminOp newTermauth      = new AdminOp("newTermauth",new AdminOpCode(AdminOpCode.newTermauth));
  public static final AdminOp enterprise       = new AdminOp("enterprise" ,new AdminOpCode(AdminOpCode.enterprise));
  public static final AdminOp editRecordAdminOp= new AdminOp("editRecord" ,new AdminOpCode(AdminOpCode.editRecord));
  public static final AdminOp drawerAdminOp    = new AdminOp("drawer"     ,new AdminOpCode(AdminOpCode.drawer));
  public static final AdminOp associateAdminOp = new AdminOp("associate"  ,new AdminOpCode(AdminOpCode.associate));
  public static final AdminOp applianceAdminOp = new AdminOp("Appliance"  ,new AdminOpCode(AdminOpCode.appliance));

}
//$Id: AdminOp.java,v 1.4 2003/09/05 13:41:23 mattm Exp $
