/**
 * Title:        Logout<p>
 * Description:  Logout Page; 'nuff said<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Logout.java,v 1.4 2001/07/19 01:06:56 mattm Exp $
 */

// This class only exists for the classname to be displayed as an option, etc (key(), name(), url()).
// It isn't acually used to DO anything.

package net.paymate.web.page;
import  net.paymate.util.*;

public class Logout extends PayMatePage {

  public Logout(String loginInfo) {
    super(name(), loginInfo);
    fillBody(contentFromStrings(contents));
  }
  protected static final String contents[]= {
    "",
    "Thank you for using PayMate.net!",
    ""
  };

  public static final String name() {
    return key();
  }
  public static final String key() {
    return PayMatePage.key(Logout.class);
  }
  public static final String url() {
    return key();
  }
}