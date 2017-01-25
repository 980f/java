/**
 * Title:        HttpURI<p>
 * Description:  an HTTP URL<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: HttpURI.java,v 1.1 2000/07/14 11:25:37 mattm Exp $
 */
package net.paymate.net;

public class HttpURI extends URI {
  public static final String HTTP_SCHEME = "http";
  public static final int    HTTP_DEFAULT_PORT = 80;

/**
 * @param host     just the servername, eg: www.spaceship.com or 208.58.21.60
 * @param port     the port number
 * @param abs_path the path to place beyond the servername (eg: /servlets/txnservet)
 * @param query    what normally appears after the '?' (eg: name=mmmello&test=3&size=20)
 */
  public HttpURI(String host, int port, String path, URIQuery query) {
    super(HTTP_SCHEME, host, port, path, query, HTTP_DEFAULT_PORT);
  }

  public HttpURI(String host, String path, URIQuery query) {
    this(host, HTTP_DEFAULT_PORT, path, query);
  }

  public HttpURI(String host, String path) {
    this(host, path, null);
  }

  public HttpURI(String host) {
    this(host, "");
  }

  public HttpURI() {
    this("");
    port = HTTP_DEFAULT_PORT;
  }

}

