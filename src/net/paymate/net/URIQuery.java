/**
 * Title:        URIQuery<p>
 * Description:  Used to convert Properties to/from URIQueries<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: URIQuery.java,v 1.5 2002/01/30 05:24:26 andyh Exp $
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

  public String toString() {// this converts to a "URI-encoded" string (actually already partially URI)
    return props.asRawParagraph(PARAMSEP, VALUESEP);//asRawPara fixed to not but a trailing EIL
  }

}

