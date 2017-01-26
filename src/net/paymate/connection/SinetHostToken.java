package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/SinetHostToken.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.net.*;

public interface SinetHostToken extends HostToken {
  String applianceKey="applName";
  String UrlpathKey  ="path";
  String timeoutKey  ="timeout";
  String interfaceKey="interface";
}
//$Id: SinetHostToken.java,v 1.4 2004/02/12 18:00:35 andyh Exp $
