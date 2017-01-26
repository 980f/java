package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/EmailAddressList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.Enumeration;
import net.paymate.data.sinet.EntityList;
import net.paymate.util.TextList;

public class EmailAddressList extends EntityList {

  public EmailAddressList() {
  }

  public EmailAddress getById(EmailAddressid id) {
    return (EmailAddress)getEntityById(id);
  }

  public EmailAddressList addEmailAddress(EmailAddress a) {
    addEntity(a);
    return this;
  }

  public EmailAddressList removeEmailAddress(EmailAddress a) {
    removeEntity(a);
    return this;
  }

  public EmailAddressList addEmailAddressList(EmailAddressList toadd) {
    Enumeration enum = toadd.entities();
    while(enum.hasMoreElements()) {
      addEmailAddress((EmailAddress)enum.nextElement());
    }
    return this;
  }

}
