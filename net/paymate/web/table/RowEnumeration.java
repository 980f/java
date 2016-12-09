/**
 * Title:        <p>
 * Description:  <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: RowEnumeration.java,v 1.3 2001/05/30 02:43:26 mattm Exp $
 */

package net.paymate.web.table;

public interface RowEnumeration {
  public abstract TableGenRow nextRow();
  public abstract boolean hasMoreRows();
}
