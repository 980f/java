/**
 * Title:        Unknown<p>
 * Description:  Used to return 404 Unknown messages<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Unknown.java,v 1.3 2001/07/19 01:06:56 mattm Exp $
 */

package net.paymate.web.page;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;

public class Unknown extends PayMatePage {

  public Unknown(String requestURL, String loginInfo) {
    super(name(), loginInfo);
    fillBody(content(requestURL));
  }

  private Element content(String requestURL) {
    ElementContainer ec = new ElementContainer();
    ec
      .addElement(br)
      .addElement(new B("Not Found"))
      .addElement(br)
      .addElement(br)
      .addElement(new StringElement("The requested URL "))
      .addElement(new I(requestURL))
      .addElement(new StringElement(" was not found on this server."))
      .addElement(br);
    return ec;
  }

  public static final String name() {
    return key();
  }
  public static final String key() {
    return PayMatePage.key(Unknown.class);
  }
}
