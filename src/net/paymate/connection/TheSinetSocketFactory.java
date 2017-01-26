package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/TheSinetSocketFactory.java,v $</p>
 * <p>Description: contains a list of hosts and logic for picking one and making a socket to it</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.14 $
 */

// singleton; only appropriate for client, not server

import net.paymate.util.*;
import net.paymate.net.*;
import net.paymate.lang.StringX;

public class TheSinetSocketFactory extends MultiHomedSocketFactory {

  private static TheSinetSocketFactory the;

  public static SinetHost PreferredHost(){
    return (SinetHost)the.preferredHost();
  }

  public static SinetHost CurrentHost(){
    return (SinetHost)the.currentHost();
  }

  public static SinetHost GetHost(boolean primary){
    return (SinetHost)the.getHost(primary);
  }

  public static boolean ThisFailed(SinetHost host){
    return the.thisFailed(host);
  }

  public static boolean ThisWorked(SinetHost host){
    return the.thisWorked(host);
  }

  public static TextList Dump(TextList dump){
    return the.dumpStatus(dump);
  }

  ////////////////////
  // construction

  public TheSinetSocketFactory(){//must be public due to call to newInstance() higher in this class hierarchy
    super();
  }

  public static TheSinetSocketFactory Initialize(EasyCursor ezc) {
    if(the == null){
      the=new TheSinetSocketFactory();
    }
    the.Initialize(ezc, SinetHost.class); // +++ check return value?
    return the;
  }
}
//$Id: TheSinetSocketFactory.java,v 1.14 2004/03/22 21:46:13 andyh Exp $