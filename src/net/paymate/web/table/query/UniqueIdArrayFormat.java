package net.paymate.web.table.query;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/table/query/UniqueIdArrayFormat.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.web.table.*;
import net.paymate.web.*;
import net.paymate.data.UniqueId;

public abstract class UniqueIdArrayFormat extends TableGen implements RowEnumeration {

  protected UniqueId [ ] ids = null;

  public UniqueIdArrayFormat(UniqueId [ ] ids, String title, LoginInfo linfo,
                        String absoluteURL, HeaderDef[] headers) {
    super(title, linfo.colors(), headers, absoluteURL);
    this.ids = ids;
  }

  protected RowEnumeration rows() {
    return this;
  }
  protected HeaderDef[] fabricateHeaders() {
    return headers;
  }
  public int numColumns() {
    return headers.length;
  }
  public boolean hasMoreRows() {
    return currentRow < (ids.length - 1);
  }
  protected int currentRow = -1;


}