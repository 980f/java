/**
 * Title:        URIQuery<p>
 * Description:  Used to convert Properties to/from URIQueries<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: URIQuery.java,v 1.4 2001/07/06 18:59:02 andyh Exp $
 */

package net.paymate.net;
import  net.paymate.util.*;

public class URIQuery {
  // for outputting to http:
  protected static final String PARAMSEP = "&";
  protected static final String VALUESEP = "=";
  // the data
  EasyCursor props;

  public URIQuery(EasyCursor props) {
    this.props = props;
  }

  public String toString() {
    // this converts to a "URI-encoded" string (actually already partially URI)
    return Safe.clipEOL(props.asRawParagraph(PARAMSEP, VALUESEP), PARAMSEP);
  }

}

