/**
 * Title:        SystemRevision<p>
 * Description:  Handles the system versioning issues <p>(checking that server and appliance match) <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SystemRevision.java,v 1.8 2003/07/29 21:22:00 andyh Exp $
 */

package net.paymate.connection;

import net.paymate.util.*;

public class SystemRevision implements isEasy , Comparable {
  // see the list at the bottom of the file
  private static final String BASECODE = "2";//protocol format

  public static final boolean match(String testCode) {
    String rcTheirs = new String(testCode);
    rcTheirs.trim();
    String rcOurs = new String(BASECODE); // shouldn't need it, but JIC
    rcOurs.trim();
    boolean pass = rcTheirs.equalsIgnoreCase(rcOurs);
    return pass;
  }

  public boolean isCurrent(){
    return BASECODE.endsWith(revision);
  }

  String revision=BASECODE; //default current
//  public static SystemRevision from(String image){
//    SystemRevision newone=new SystemRevision();
//    newone.revision=StringX.OnTrivial(image,"0.0").trim();
//    return newone;
//  }

  private String canonical(Object o){
    if(o instanceof String){
      return ((String) o).trim();
    } else if(o instanceof SystemRevision){
      return ((SystemRevision)o).revision;
    }
    return "";
  }

  public int compareTo(Object o){//implements Comparable
    return revision.compareTo(canonical(o));
  }

  public boolean equals(Object o){
   return revision.equals(canonical(o));
  }

  private static final String versionKey = "version";

  public void save(EasyCursor ezp){
    ezp.setString(versionKey, revision);
  }
  public void load(EasyCursor ezp){
    revision = ezp.getString(versionKey);
  }

}


// revision codes:
// 2     - unified PaymentRequest instead of FinancialRequest hierarchy
// 1.003 - prepare for removal of legacy server quirks
// 1.002 - caused by moving the "object" (serialized Properties object) to the
//         body of the message from the URL, where it can make the URL
//         too large for apache to deal with, especially for sending jars.
// 1.001 - the original
