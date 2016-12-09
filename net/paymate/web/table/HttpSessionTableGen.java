/**
 * Title:        HttpSessionTableGen
 * Description:  Generates the content for a table of HttpSession info
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: HttpSessionTableGen.java,v 1.16 2001/11/17 06:16:59 mattm Exp $
 */

package net.paymate.web.table;

import  net.paymate.util.ErrorLogStream;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  net.paymate.util.Safe;
import  javax.servlet.http.*;
import  java.util.Enumeration;
import  net.paymate.web.UserSession;

public class HttpSessionTableGen extends TableGen {

  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(HttpSessionTableGen.class.getName());

  HttpSessionContext context = null;

  public HttpSessionTableGen(String title, ColorScheme colors, HttpSessionContext context, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(title, colors, headers, absoluteURL, howMany, sessionid);
    this.context = context;
  }

  public static final Element output(String title, ColorScheme colors, HttpSessionContext context, String sessionid) {
    return output(title, colors, context, null, null, -1, sessionid);
  }

  public static final Element output(String title, ColorScheme colors,  HttpSessionContext context, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    return new HttpSessionTableGen(title, colors, context, headers, absoluteURL, howMany, sessionid);
  }

  public RowEnumeration rows() {
    return new HttpSessionRowEnumeration(context);
  }

  public static final int numCols    = 8;
  // @EN@ looks like an enumeration, huh? ...
  public static final int ID         = 0;
  public static final int CREATED    = 1;
  public static final int ACCESSED   = 2;
  public static final int NEW        = 3;
  public static final int TERMINAL   = 4;
  public static final int USERID     = 5;
  public static final int STORE      = 6;
  public static final int ENTERPRISE = 7;

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = null;
    if(context != null) {
      headers = new HeaderDef[numCols];
      headers[ID]         = new HeaderDef(AlignType.LEFT, "ID");
      headers[CREATED]    = new HeaderDef(AlignType.LEFT, "Creation Time");
      headers[ACCESSED]   = new HeaderDef(AlignType.LEFT, "Last Accessed");
      headers[NEW]        = new HeaderDef(AlignType.LEFT, "New");
      headers[TERMINAL]   = new HeaderDef(AlignType.LEFT, "Terminal");
      headers[USERID]     = new HeaderDef(AlignType.LEFT, "UserID");
      headers[STORE]      = new HeaderDef(AlignType.LEFT, "Store");
      headers[ENTERPRISE] = new HeaderDef(AlignType.LEFT, "Enterprise");
    }
    return headers;
  }

  public void close() {
    super.close();
  }
}


class HttpSessionRowEnumeration implements RowEnumeration {

  HttpSessionContext context = null;
  Enumeration enum = null;
  private int curRow = -1;

  public HttpSessionRowEnumeration(HttpSessionContext context) {
    this.context = context;
    enum = context.getIds();
  }

  public boolean hasMoreRows() {
    return enum.hasMoreElements();
  }

  public TableGenRow nextRow() {
    TableGenRow row = null;
    if(enum.hasMoreElements()) {
      String id = (String) enum.nextElement();
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
          reply = Safe.timeStamp(session.getCreationTime());
        } break;
        case HttpSessionTableGen.ACCESSED: {
          reply = Safe.timeStamp(session.getLastAccessedTime());
        } break;
        case HttpSessionTableGen.NEW: {
          reply = (session.isNew() ? "T" : "F");
        } break;
        case HttpSessionTableGen.TERMINAL: {
          if(user != null) {
            reply = user.linfo.terminalID + ": " + user.linfo.terminalName;
          }
        } break;
        case HttpSessionTableGen.USERID: {
          if(user != null) {
            reply = user.linfo.clerk.Name() + ": " + user.linfo.longName;
          }
        } break;
        case HttpSessionTableGen.STORE: {
          if(user != null) {
            reply = user.linfo.storeid + ": " + user.linfo.companyName;
          }
        } break;
        case HttpSessionTableGen.ENTERPRISE: {
          if(user != null) {
            reply = ""+user.linfo.enterpriseID;
          }
        } break;
      }
    }
    return new StringElement(Safe.TrivialDefault(reply, " "));
  }
}
