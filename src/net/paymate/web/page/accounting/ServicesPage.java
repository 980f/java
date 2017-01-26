/**
 * Title:        ServicesPage<p>
 * Description:  Displays the highest level view of the services of the system<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ServicesPage.java,v 1.5 2004/04/08 09:09:54 mattm Exp $
 */

package net.paymate.web.page.accounting;
import  net.paymate.web.*;
import  net.paymate.web.page.*;
import  net.paymate.web.table.query.*;
import  net.paymate.util.*; // Service
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import net.paymate.lang.StringX;

public class ServicesPage extends Acct {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ServicesPage.class);

  public ServicesPage(LoginInfo linfo, AdminOpCode opcodeused) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    String rev = net.paymate.Revision.Version();
    this.fillBody(new ElementContainer()
      .addElement(new Center(new H2(title()).addElement(" " + (isProduction ? "" : "NOT ") + "PRODUCTION ["+"Rev: " + rev + ((rev.indexOf(net.paymate.Revision.LongVersion()) == -1) ? (" - " + net.paymate.Revision.jarSize()) : "")+"]")))
      .addElement(PayMatePage.BRLF)
      .addElement(new ServicesFormat(
        Service.getList(), linfo.colors(), "Services", serviceUrl()))
      );
  }

  private static final String title() {
    return StringX.TrivialDefault(Service.hostname(), "Status");
  }
}
