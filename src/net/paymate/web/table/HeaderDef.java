/**
 * Title:        HeaderDef<p>
 * Description:  Table header definition<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: HeaderDef.java,v 1.6 2003/07/27 05:35:29 mattm Exp $
 */

package net.paymate.web.table;
import org.apache.ecs.*;
import net.paymate.lang.StringX;

public class HeaderDef {
  public String colAlign; // for the rows under the header
  public Element title;
  public HeaderDef(String colAlign, String title) {
    this.colAlign = colAlign;
    this.title = new StringElement(StringX.TrivialDefault(title, "").trim());
  }
  public HeaderDef(String colAlign, Element title) {
    this.colAlign = colAlign;
    this.title = title;
  }
}
