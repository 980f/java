package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/StoreCronCallback.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

// this is temporary until we have an SS2-style Terminal object
// that can perform the deposits and drawer closures that the
// connection server currently performs.
// since we don't want the Store class to have its autoDrawerAndDeposit function
// compile in the np.web.connectionserver, we do this instead ...

public interface StoreCronCallback {
  public /*MultiReply mr = */ void autoCloseAllDrawers(Storeid storeid);
  public boolean autoIssueDeposit(Storeid storeid);
}