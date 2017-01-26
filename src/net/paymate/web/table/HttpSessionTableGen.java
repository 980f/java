/**
 * Title:        HttpSessionTableGen
 * Description:  Generates the content for a table of HttpSession info
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: HttpSessionTableGen.java,v 1.25 2003/10/30 23:06:12 mattm Exp $
 */

package net.paymate.web.table;

import  net.paymate.util.ErrorLogStream;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  javax.servlet.http.*;
import  java.util.Enumeration;
import  net.paymate.web.UserSession;
import net.paymate.util.DateX;
import net.paymate.lang.StringX;
import net.paymate.data.Terminalid;

public class HttpSessionTableGen extends TableGen {

  // logging facilities
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(HttpSessionTableGen.class);

  HttpSessionContext context = null;

  public HttpSessionTableGen(String title, ColorScheme colors, HttpSessionContext context, HeaderDef headers[], String absoluteURL) {
    super(title, colors, headers, absoluteURL);
    this.context = context;
  }

  public static final Element output(String title, ColorScheme colors, HttpSessionContext context) {
    return output(title, colors, context, null, null);
  }

  public static final Element output(String title, ColorScheme colors,  HttpSessionContext context, HeaderDef headers[], String absoluteURL) {
    return new HttpSessionTableGen(title, colors, context, headers, absoluteURL);
  }

  public RowEnumeration rows() {
    return new HttpSessionRowEnumeration(context);
  }

  public static final int numCols    = 10;
  // @EN@ looks like an enumeration, huh? ...
  public static final int ID         = 0;
  public static final int CREATED    = 1;
  public static final int DURATION   = 2;
  public static final int ACCESSED   = 3;
  public static final int IDLE       = 4;
  public static final int NEW        = 5;
  public static final int TERMINAL   = 6;
  public static final int USERID     = 7;
  public static final int STORE      = 8;
  public static final int ENTERPRISE = 9;

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = null;
    if(context != null) {
      headers = new HeaderDef[numCols];
      headers[ID]         = new HeaderDef(AlignType.LEFT, "ID");
      headers[CREATED]    = new HeaderDef(AlignType.LEFT, "Creation Time");
      headers[DURATION]   = new HeaderDef(AlignType.LEFT, "Lifetime");
      headers[ACCESSED]   = new HeaderDef(AlignType.LEFT, "Last Accessed");
      headers[IDLE]       = new HeaderDef(AlignType.LEFT, "Idled");
      headers[NEW]        = new HeaderDef(AlignType.LEFT, "New");
      headers[TERMINAL]   = new HeaderDef(AlignType.LEFT, "Terminal");
      headers[USERID]     = new HeaderDef(AlignType.LEFT, "UserID");
      headers[STORE]      = new HeaderDef(AlignType.LEFT, "Store");
      headers[ENTERPRISE] = new HeaderDef(AlignType.LEFT, "Enterprise");
    }
    return headers;
  }
}


class HttpSessionRowEnumeration implements RowEnumeration {

  HttpSessionContext context = null;
  Enumeration ennum = null;
  private int curRow = -1;

  public HttpSessionRowEnumeration(HttpSessionContext context) {
    this.context = context;
    if(context != null) {
      ennum = context.getIds();
    }
  }

  public boolean hasMoreRows() {
    return (ennum != null) ? ennum.hasMoreElements() : false;
  }

  public TableGenRow nextRow() {
    TableGenRow row = null;
    if((ennum != null) && ennum.hasMoreElements()) {
      String id = (String) ennum.nextElement();
      HttpSession temp = context.getSession(id);
      row = new HttpSessionTableGenRow(temp);
      curRow++;
    }
    return row;
  }
}

class HttpSessionTableGenRow implements TableGenRow {
  HttpSession session = null;
  UserSession user = null;

  public HttpSessionTableGenRow(HttpSession session) {
    this.session = session;
    if(session != null) {
      try {
        user = (UserSession)session.getValue(UserSession.userSessionKey);
      } catch (Exception e ) {
        // +++ and do what?
      }
    }
  }

  public int numColumns() {
    return HttpSessionTableGen.numCols;
  }

  public Element column(int col) {
    String reply = null;
    if(session != null) {
      switch(col) {
        case HttpSessionTableGen.ID: {
          reply = session.getId();
        } break;
        case HttpSessionTableGen.CREATED: {
          reply = DateX.timeStamp(session.getCreationTime());
        } break;
        case HttpSessionTableGen.DURATION: {
          reply = DateX.millisToTime(DateX.utcNow() - session.getCreationTime());
        } break;
        case HttpSessionTableGen.ACCESSED: {
          reply = DateX.timeStamp(session.getLastAccessedTime());
        } break;
        case HttpSessionTableGen.IDLE: {
          reply = DateX.millisToTime(DateX.utcNow() - session.getLastAccessedTime());
        } break;
        case HttpSessionTableGen.NEW: {
          reply = (session.isNew() ? "T" : "F");
        } break;
        case HttpSessionTableGen.TERMINAL: {
          reply = ((user != null) && (user.linfo!=null) &&
                   Terminalid.isValid(user.linfo.terminalID()) &&
                   user.linfo.terminalID().isValid())
              ? (user.linfo.terminalID() + ": " + user.linfo.terminalName())
              : "N/A";
        } break;
        case HttpSessionTableGen.USERID: {
          if(user != null) {
            reply = ""+user.linfo.assoc;
          }
        } break;
        case HttpSessionTableGen.STORE: {
          if(user != null) {
            reply = ""+user.linfo.store;
          }
        } break;
        case HttpSessionTableGen.ENTERPRISE: {
          if(user != null) {
            reply = ""+user.linfo.enterprise;
          }
        } break;
      }
    }
    return new StringElement(StringX.TrivialDefault(reply, " "));
  }
}
