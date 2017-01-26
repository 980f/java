package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplianceList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import java.util.Enumeration;
import net.paymate.data.sinet.EntityList;

public class ApplianceList extends EntityList {

  public ApplianceList() {

  }

  public Appliance getById(Applianceid id) {
    return (Appliance)getEntityById(id);
  }

  public Applianceid [ ] getAllIds() {
    Applianceid [ ] ret = new Applianceid [size()];
    int index = -1;
    for(Enumeration enums = entities(); enums.hasMoreElements();) {
      Appliance appliance = (Appliance)enums.nextElement();
      ret[++index] = appliance.applianceid();
    }
    return ret;
  }

  public ApplianceList addAppliance(Appliance a) {
    addEntity(a);
    return this;
  }

  public ApplianceList removeAppliance(Appliance a) {
    removeEntity(a);
    return this;
  }

}
