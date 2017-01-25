/**
 * Title:        Acct<p>
 * Description:  Accounting page -- wraps all transactions / browsing <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Acct.java,v 1.3 2001/07/19 01:06:56 mattm Exp $
 */

package net.paymate.web.page;

public class Acct extends PayMatePage {

  public Acct(String loginInfo) {
    super(name(), loginInfo);
  }

  public static final String name() {
    return "Account Administration";
  }
  public static final String key() {
    return PayMatePage.key(Acct.class);
  }
}