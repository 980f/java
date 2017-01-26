/**
 * Title:        HTTPMessage<p>
 * Description:  RFC-2616 Http Message format<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: HTTPMessage.java,v 1.8 2003/11/24 04:52:30 mattm Exp $
 */
package net.paymate.net;
import net.paymate.util.*;

//find the HTTP/1.0 or 1.1 spec on the web and use it to create the objects that represent HTTP/1.0 or 1.1
//then, rewrite this with it
//then do the same for the HttpReply class (which doesn't exist yet)
//OR find a set of classes that already do that
//specs are on vesta

public class HTTPMessage { // this is an HttpRequest
  // no time to deal with issues now, so everything is public

  // universal formatting pieces:
  public static final String SP = " ";
  public static final String CRLF = "\r\n";

  // used class:
  // +++ This is like a media type (MIME) or something; generalize this!
  public class HTTPVersion {
    public static final String prefix = "HTTP/";
    public int majorNumber = 1;
    public static final String dotSeparator = ".";
    public int minorNumber = 0;

    public String toString() {
      return prefix + majorNumber + dotSeparator + minorNumber;
    }

  }

  // Generic message stuff
  public    HTTPVersion    version = new HTTPVersion();
  public    EasyCursor headers = new EasyCursor(); /* strings of name: value*/
  protected String         body    = "";

  // NOTE! Is you put anything in the body, then you must include a string of:
  //   Content-Length: %d
  // where %d is body.length();
  // you can do that automatically here:
  public void setBody(String body) {
    this.body = new String(body);
    if(this.body == null) {
      this.body = "";
    }
    headers.setString("Content-Length", String.valueOf(body.length()));
  }
  public String getBody() {
    return new String(body);
  }

  // +++ Eventually separate the Request and Response stuff into
  // +++ two separate extended classes from this HttpMessage class

  // Request message stuff
  public HttpURI    url    = new HttpURI();
  public HTTPMethod method = new HTTPMethod(HTTPMethod.GET);

  // Response message stuff (not used if you are doing a request
  // and we don't usually do responses since the HttpServlet stuff handles it)
  public boolean isResponse   = false; // most will be requests
  // +++ eventually could get this from the RFC and put in a TrueEnum
  public int     statusCode   = 200;   // most are okay?
  public String  reasonPhrase = "";


  private HTTPMessage(HTTPMethod method, String host, int port, String path, URIQuery query, String body) {
    url = new HttpURI(host, port, path, query);
    this.method = method;
    setBody(body);
  }

  public HTTPMessage(HTTPMethod method, IPSpec ipspec, String path, URIQuery query, String body) {
    this(method, ipspec.address, ipspec.port, path, query, body);
  }

  // HTTP-message   = Request | Response     ; HTTP/1.1 messages
  //
  //        Request       = Request-Line              ; Section 5.1
  //                        *(( general-header        ; Section 4.5
  //                         | request-header         ; Section 5.3
  //                         | entity-header ) CRLF)  ; Section 7.1
  //                        CRLF
  //                        [ message-body ]          ; Section 4.3
  //
  // Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
  //
  //     Response      = Status-Line               ; Section 6.1
  //                     *(( general-header        ; Section 4.5
  //                      | response-header        ; Section 6.2
  //                      | entity-header ) CRLF)  ; Section 7.1
  //                     CRLF
  //                     [ message-body ]          ; Section 7.2
  //
  //      Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
  //
  // +++ make class stuff for these later (see RFC 2616) ...
  // Request and Response messages MAY transfer an entity if not otherwise
  // restricted by the request method or response status code. An entity
  // consists of entity-header fields and an entity-body, although some
  // responses will only include the entity-headers.
  //
  //     entity-header  = Allow                    ; Section 14.7  --
  //                    | Content-Encoding         ; Section 14.11 --
  //                    | Content-Language         ; Section 14.12 --
  //                    | Content-Length           ; Section 14.13 (check 4.4)
  //                    | Content-Location         ; Section 14.14 --
  //                    | Content-MD5              ; Section 14.15 --
  //                    | Content-Range            ; Section 14.16 --
  //                    | Content-Type             ; Section 14.17 --
  //                    | Expires                  ; Section 14.21 --
  //                    | Last-Modified            ; Section 14.29 --
  //
  public String toString() {
    String startLine = !isResponse
      ? (method.Image() + SP + url + SP + version + CRLF)
      : (version + SP + statusCode + SP + reasonPhrase + CRLF);
    return
      /* startline: */
        startLine +
      /* headers: */
        headers.asParagraph(CRLF, ": ") + /* these should each end in CRLF! */
                CRLF +  //supressed trailing separator for when they are commas
      /* a blank line to indicate body is starting */
        CRLF +
      /* body */
        body +
      /* is this necessary?  will it screw things up? */
        CRLF;
  }

}