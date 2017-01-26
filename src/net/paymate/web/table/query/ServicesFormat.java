/**
* Title:        ServicesFormat<p>
* Description:  The canned format for the ServicesFormat screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: ServicesFormat.java,v 1.12 2003/10/30 21:05:18 mattm Exp $
*/

package net.paymate.web.table.query;
import net.paymate.authorizer.Authorizer;
import  net.paymate.web.table.*; // TableGen, TableGenRow, RowEnumeration
import  net.paymate.util.*; // ErrorlogStream, Service
import  net.paymate.web.color.*; // ColorScheme
import  org.apache.ecs.*; // element, AlignType
import  org.apache.ecs.html.*; // A
import net.paymate.lang.StringX;

public class ServicesFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ServicesFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new ServicesFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[ServicesFormatEnum.nameCol]        = new HeaderDef(AlignType.LEFT   , "Name");
    theHeaders[ServicesFormatEnum.statusCol]      = new HeaderDef(AlignType.LEFT   , "^?");
    theHeaders[ServicesFormatEnum.connectionsCol] = new HeaderDef(AlignType.RIGHT  , "Cnxs");
    theHeaders[ServicesFormatEnum.txnsCol]        = new HeaderDef(AlignType.RIGHT  , "Txns");
    theHeaders[ServicesFormatEnum.pendCol]        = new HeaderDef(AlignType.RIGHT  , "Pend");
    theHeaders[ServicesFormatEnum.avgTimeCol]     = new HeaderDef(AlignType.RIGHT  , "Avg Txn");
    theHeaders[ServicesFormatEnum.timeOutsCol]    = new HeaderDef(AlignType.RIGHT  , "TimeX");
    theHeaders[ServicesFormatEnum.writeCol]       = new HeaderDef(AlignType.RIGHT  , "Wrote");
    theHeaders[ServicesFormatEnum.readCol]        = new HeaderDef(AlignType.RIGHT  , "Read");
    theHeaders[ServicesFormatEnum.logFileCol]     = new HeaderDef(AlignType.LEFT   , "LogFile");
    theHeaders[ServicesFormatEnum.notesCol]       = new HeaderDef(AlignType.LEFT   , "Notes");
  }

  Service [] service = null;
  String baseurl = null; // for an individual service
  public ServicesFormat(Service [] service, ColorScheme colors, String title,
                        String baseurl) {
    super(title, colors, theHeaders, null);
    this.service = service;
    this.baseurl = baseurl;
  }

  protected RowEnumeration rows() {
    return this;
  }
  protected HeaderDef[] fabricateHeaders() {
    return headers;
  }
  public int numColumns() {
    return headers.length;
  }
  public boolean hasMoreRows() {
    return currentRow < (service.length - 1);
  }
  private int currentRow = -1;

  public TableGenRow nextRow() {
    currentRow++;
    return this;
  }

  public Element column(int col) {
    String str = "";
    Service svc = service[currentRow];
    switch(col) {
      case ServicesFormatEnum.nameCol: {
        str = svc.serviceName();
        if(StringX.NonTrivial(baseurl)) {
          return new A(baseurl + "&s="+str, str + (svc.isAuthService() ? " Proc["+((Authorizer)svc).id.toString()+"]" : ""));
        }
      } break;
      case ServicesFormatEnum.statusCol: {
        str = svc.upText();
      } break;
      case ServicesFormatEnum.txnsCol: {
        str = svc.svcTxns();
      } break;
      case ServicesFormatEnum.pendCol: {
        str = svc.svcPend();
      } break;
      case ServicesFormatEnum.timeOutsCol: {
        str = svc.svcTimeouts();
      } break;
      case ServicesFormatEnum.connectionsCol: {
        str = svc.svcCnxns();
      } break;
      case ServicesFormatEnum.avgTimeCol: {
        str = svc.svcAvgTime();
      } break;
      case ServicesFormatEnum.writeCol: {
        str = svc.svcWrites();
      } break;
      case ServicesFormatEnum.readCol: {
        str = svc.svcReads();
      } break;
      case ServicesFormatEnum.logFileCol: {
        str = svc.svcLogFile();
      } break;
      case ServicesFormatEnum.notesCol: {
        str = svc.svcNotes();
      } break;
    }
    return new StringElement(str);
  }

}

//$Id: ServicesFormat.java,v 1.12 2003/10/30 21:05:18 mattm Exp $
