/**
 * Title:        URI<p>
 * Description:  Universal Resource Indentifier <p>
 *               based on the "generic URI" syntax, described in RFC 2396, section 3<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: URI.java,v 1.2 2000/10/12 19:08:08 mattm Exp $
 */
package net.paymate.net;
import  net.paymate.util.Safe;

public class URI {

  public String scheme = "";
  public static final String schemePostfix = "://";
  // start authority pieces ...
  public String host = "";
  public static final String portPrefix = ":";
  public int port = 0;
  // ... end authority pieces
  public String path = "";
  public static final String queryPrefix = "?";
  public URIQuery query = null;
  public int defaultPort = 0; // set this to NOT have it print!

/**
 * @param scheme      usually just means the protocol
 * @param host        just the servername, eg: www.spaceship.com or 208.58.21.60
 * @param port        the port number
 * @param abs_path    the path to place beyond the servername (eg: /servlets/txnservet)
 * @param query       what normally appears after the '?' (eg: name=mmmello&test=3&size=20)
 * @param portDefault do not output the port number if it matches this (the default)
 */
  public URI(String scheme, String host, int port, String path, URIQuery query, int defaultPort) {
    this.scheme = scheme;
    this.host     = host;
    this.port     = port;
    this.path     = path;
    this.query    = query;
    this.defaultPort = defaultPort;
  }

  public URI(String scheme, String host, int port, String path, URIQuery query) {
    this(scheme, host, port, path, query, 0);
  }

  public String toString() {
    String authority = Safe.unNull(host) + ((port == defaultPort) ? "" : (portPrefix + port));
    String queryStr  = query.toString();
    return Safe.unNull(scheme) +
           schemePostfix +
           authority +
           Safe.unNull(path) +
           ((Safe.NonTrivial(queryStr)) ? (queryPrefix + queryStr) : "");
  }
  // +++ NEED A fromString() !!!!!
}