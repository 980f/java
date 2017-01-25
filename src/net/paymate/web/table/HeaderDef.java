/**
 * Title:        HeaderDef<p>
 * Description:  Table header definition<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: HeaderDef.java,v 1.4 2000/09/06 17:49:25 mattm Exp $
 */

package net.paymate.web.table;
import  org.apache.ecs.*;

public class HeaderDef {
  public String colAlign; // for the rows under the header
  public Element title;
  public HeaderDef(String colAlign, String title) {
    this.colAlign = colAlign;
    this.title = new StringElement(title.trim());
  }
  public HeaderDef(String colAlign, Element title) {
    this.colAlign = colAlign;
    this.title = title;
  }
}
