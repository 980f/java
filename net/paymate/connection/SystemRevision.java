/**
 * Title:        SystemRevision<p>
 * Description:  Handles the system versioning issues <p>(checking that server and appliance match) <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SystemRevision.java,v 1.4 2001/07/19 01:06:46 mattm Exp $
 */

package net.paymate.connection;

// +++ put this in a different package? no, just name it packageVersion

public class SystemRevision {
  // see the list at the bottom of the file
  public static final String BASECODE = "1.002";//protocol format

  public static final boolean match(String testCode) {
    String rcTheirs = new String(testCode);
    rcTheirs.trim();
    String rcOurs = new String(BASECODE); // shouldn't need it, but JIC
    rcOurs.trim();
    boolean pass = rcTheirs.equalsIgnoreCase(rcOurs);
    return pass;
  }
}

//$Log: SystemRevision.java,v $
//Revision 1.4  2001/07/19 01:06:46  mattm
//finals and minor bug fixes
//
//Revision 1.3  2001/05/24 05:08:42  andyh
//ipterm restored
//
//Revision 1.2  2001/05/18 12:42:29  andyh
//matts turn
//
// revision codes:
// 1.002 - caused by moving the "object" (serialized Properties object) to the
//         body of the message from the URL, where it can make the URL
//         too large for apache to deal with, especially for sending jars.
// 1.001 - the original
