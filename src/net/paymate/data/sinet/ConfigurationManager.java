package net.paymate.data.sinet;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/ConfigurationManager.java,v $</p>
 * <p>Description: Allows objects to load and save themselves from storage</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

import net.paymate.data.*;

public interface ConfigurationManager {

  public boolean loadEntity(EntityBase entity);
  public boolean storeEntity(EntityBase entity);

  public UniqueId [ ] getIds(SinetClass sclass, Class uniqueidExtentClass); // gets all ids from the database for this class

  public UniqueId New(SinetClass sclass, UniqueId parentid);
}
