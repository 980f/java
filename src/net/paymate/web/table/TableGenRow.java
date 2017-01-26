/**
 * Title:        TableGenRow<p>
 * Description:  Interface for html table row query implementation<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: TableGenRow.java,v 1.1 2000/09/07 08:55:04 mattm Exp $
 */

package net.paymate.web.table;
import  org.apache.ecs.*;

public interface TableGenRow {

  public abstract Element column(int col);
  public abstract int numColumns();
}