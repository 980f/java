/**
* Title:        ServicesFormat<p>
* Description:  The canned format for the ServicesFormat screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: AuthorizerTerminalAgentsFormat.java,v 1.5 2004/03/17 20:29:38 mattm Exp $
*/

package net.paymate.web.table.query;
import net.paymate.authorizer.*;
import net.paymate.data.*;
import  net.paymate.web.table.*; // TableGen, TableGenRow, RowEnumeration
import  net.paymate.util.*; // ErrorlogStream, Service
import  net.paymate.web.color.*; // ColorScheme
import  org.apache.ecs.*; // element, AlignType
import  org.apache.ecs.html.*; // A
import net.paymate.lang.StringX;

public class AuthorizerTerminalAgentsFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthorizerTerminalAgentsFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AuthorizerTerminalAgentsFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[AuthorizerTerminalAgentsFormatEnum.terminalidCol]  = new HeaderDef(AlignType.LEFT , "Terminal");
    theHeaders[AuthorizerTerminalAgentsFormatEnum.termBatchNumCol]= new HeaderDef(AlignType.LEFT , "Batch Num");
    theHeaders[AuthorizerTerminalAgentsFormatEnum.centsCountCol]  = new HeaderDef(AlignType.RIGHT, "Count");
    theHeaders[AuthorizerTerminalAgentsFormatEnum.centsTotalCol]  = new HeaderDef(AlignType.RIGHT, "Total");
    theHeaders[AuthorizerTerminalAgentsFormatEnum.agentStatus]    = new HeaderDef(AlignType.LEFT,  "Agent");
    theHeaders[AuthorizerTerminalAgentsFormatEnum.standinStatus]  = new HeaderDef(AlignType.LEFT,  "Standin");
  }

  Authorizer auth = null;
  AuthTermAgentList agents = null;
  Object [ ] array = null;
  public static final String moneyformat = "#0.00";

  public AuthorizerTerminalAgentsFormat(Authorizer auth, ColorScheme colors) {
    super("TerminalAgents for auth " + auth.id, colors, theHeaders, null);
    this.auth = auth;
    agents = auth.termAgents;
    array = agents.toArray();
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
    return currentRow < ( array.length - 1);
  }
  private int currentRow = -1;

  Terminalid termid = null;
  AuthTerminalAgent agent = null;
  public TableGenRow nextRow() {
    currentRow++;
    agent = (AuthTerminalAgent) array[currentRow];
    termid = agent.myTerminalid;
    return this;
  }

  private LedgerValue lv = new LedgerValue(moneyformat);
  public Element column(int col) {
    String str = "";
    try {
      switch(col) {
        case AuthorizerTerminalAgentsFormatEnum.terminalidCol: {
          str = agent.myTerminalid.toString();
        } break;
        case AuthorizerTerminalAgentsFormatEnum.termBatchNumCol: {
          str = String.valueOf(agent.termbatchnumer.value());
        } break;
        case AuthorizerTerminalAgentsFormatEnum.centsCountCol: {
          str = String.valueOf(agent.centsQueued().getCount());
        } break;
        case AuthorizerTerminalAgentsFormatEnum.centsTotalCol: {
          str = lv.setto(agent.centsQueued().getTotal()).Image();
        } break;
        case AuthorizerTerminalAgentsFormatEnum.agentStatus: {
          str = agent.foregroundStatus();
        } break;
        case AuthorizerTerminalAgentsFormatEnum.standinStatus: {
          str = agent.standinStatus();
        } break;
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
    return new StringElement(str);
  }

}

//$Id: AuthorizerTerminalAgentsFormat.java,v 1.5 2004/03/17 20:29:38 mattm Exp $
