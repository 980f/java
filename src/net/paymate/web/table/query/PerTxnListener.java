package net.paymate.web.table.query;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/table/query/PerTxnListener.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.database.ours.query.TxnRow;

public interface PerTxnListener {
  public void loadedTxn(TxnRow rec);
}