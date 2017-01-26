/**
 * Title:        DBPage<p>
 * Description:  Page for db maintenance <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: DBPage.java,v 1.6 2004/04/08 09:09:53 mattm Exp $
 */
package net.paymate.web.page.accounting;
import net.paymate.web.page.Acct;
import net.paymate.web.page.PayMatePage;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.web.*;
import  net.paymate.web.color.*;
import  java.sql.*;
import  net.paymate.web.table.*;
import  net.paymate.database.*;
import  java.util.Vector;
import  net.paymate.util.*;
import  net.paymate.data.*; // UniqueId (possibly move into np.database? +++)
import net.paymate.lang.StringX;
import net.paymate.lang.TrueEnum;

public class DBPage extends Acct {

  // logging facilities
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(DBPage.class);

  private static final JdbcGatewayOp dfltOp = new JdbcGatewayOp(JdbcGatewayOp.overview);

  public DBPage(Element e, LoginInfo linfo, AdminOpCode opcodeused) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(e);
  }

  public static final Element generatePage(
      EasyProperties ezp, String pathAdd, ColorScheme cs,
      Element [] otherElements) {
    return generatePage(pathAdd, cs, otherElements, ezp.getString("op"),
                        ezp.getString("table"), ezp);
  }

  public static final Element generatePage(
      String pathAdd, ColorScheme cs, Element [] otherElements,
      String opTmp, String table, EasyProperties ezp) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    ResultSet rs = null;
    String base = "";//req.getServletPath(); // put this back if you ever remove this from the DB service's page
    dbg.VERBOSE("Query parameters: " + ezp);
    base += (StringX.NonTrivial(pathAdd)) ? pathAdd : "?";
    String opStr = StringX.TrivialDefault(opTmp, dfltOp.Image());
    JdbcGatewayOp op = new JdbcGatewayOp();
    op.setto(opStr);
    // check to see if we can just continue a previous query ...
    DatabaseMetaData pmdb = db.getDatabaseMetadata();
    ec.addElement(printHeader(base, pmdb));
    switch(op.Value()) {
      case JdbcGatewayOp.profile: {
        // profile does not need pagination (finite length)
        ec.addElement(profileTable(db, cs, table));
        if(StringX.NonTrivial(table)) {
          ec.addElement(printCapabilities(pmdb, cs, table));
        }
      } break;
      case JdbcGatewayOp.capabilities: {
        // capabilities do not need pagination (finite length) ??? ---
        ec.addElement(printCapabilities(pmdb, cs, null));
      } break;
      default:
      case JdbcGatewayOp.overview: {
        list(ec, db, base, cs);
        ec.addElement(BRLF);
      } break;
    }
    return ec;
  }

  private static final void list(ElementContainer ec, PayMateDB db, String base,
                                     ColorScheme cs) {
    // overview does not need pagination (finite length)
    // Write list of tables (and other stuff)
    TableInfoList til = db.getTableList();
    ec.addElement(ProfileDBTableGen.output(til, cs, base)).addElement(BRLF);
  }

  protected static final Element printHeader(String base, DatabaseMetaData dbmd) {
    ElementContainer ec = new ElementContainer();
    try {
      ec.addElement(new B("Connected to:"))
        .addElement(dbmd.getURL())
        .addElement(BRLF)
        .addElement(new B("Database product:"))
        .addElement(dbmd.getDatabaseProductName() + " " + dbmd.getDatabaseProductVersion())
        .addElement(BRLF)
        .addElement(new B("Username:"))
        .addElement(dbmd.getUserName())
        .addElement(BRLF)
        .addElement(new A(base + "&op=overview", "Tables"))
        .addElement(BRLF)
        .addElement(new A(base + "&op=capabilities", "Metadata info"))
        .addElement(BRLF)
        .addElement(new A(base + "&op=profile", "Profile All Tables"))
        .addElement(BRLF);
    } catch (SQLException e) {
      ec.addElement(new StringElement("Internal error: " + e.getMessage()));
    }
    return ec;
  }

  protected static final void addElems2Form(Form f, Element [] elems) {
    if(elems != null) {
      for(int i = 0; i < elems.length; i++) {
        f.addElement(elems[i])
         .addElement(LF);
      }
    }
  }

  protected static final Element printSQLException(SQLException e) {
    ElementContainer ec = new ElementContainer();
    if(e != null) {
      ec.addElement(new B("SQLException"))
        .addElement(BRLF);
      while (e != null) {
        ec.addElement(new B("SQLState:")).addElement(e.getSQLState()).addElement(BRLF)
          .addElement(new B("Message:")).addElement(e.getMessage()).addElement(BRLF)
          .addElement(new B("Vendor:")).addElement(String.valueOf(e.getErrorCode())).addElement(BRLF)
          .addElement(BRLF);
        e = e.getNextException();
      }
    }
    return ec;
  }

  protected static final String o(Object val) {
    return String.valueOf(val).replace(',',' ');
  }

  protected static final String o(boolean val) {
    return val ? "true" : "false";
  }

  protected static final String o(int val) {
    return Integer.toString(val);
  }

  public static final Element profileTable(PayMateDB db, ColorScheme cs, String tablename) {
    ElementContainer ec = new ElementContainer();
    ec.addElement(ProfileTableGen.output(db, cs, tablename));
    return ec;
  }

  public static final Element printCapabilities(DatabaseMetaData dbmd, ColorScheme cs, String tablename) {
    ElementContainer ec = new ElementContainer();
    if((tablename != null) && !StringX.NonTrivial(tablename)) {
      tablename = null;
    }
    boolean tabled = StringX.NonTrivial(tablename);
    if(!tabled) {
      ec.addElement(settings(dbmd, cs)).addElement(BRLF);
    }
    Capabilities cap = new Capabilities();
    for(int i = 0; i < cap.numValues(); i++) {
      cap.setto(i);
      try {
        ResultSet rs = null;
        switch(i) {
          case Capabilities.Schemas: {
            if(!tabled) {
              rs = dbmd.getSchemas();
            }
          } break;
          case Capabilities.Catalogs: {
            if(!tabled) {
              rs = dbmd.getCatalogs();
            }
          } break;
          case Capabilities.TableTypes: {
            if(!tabled) {
              rs = dbmd.getTableTypes();
            }
          } break;
          case Capabilities.BestRowIdentifier: {
            if(tabled) {
              rs = dbmd.getBestRowIdentifier(null, null, tablename, DatabaseMetaData.bestRowSession, true);
            }
          } break;
          case Capabilities.CrossReferences: {
            rs = dbmd.getCrossReference(null, null, tablename, null, null, null);
          } break;
          case Capabilities.ColumnPrivileges: {
            rs = dbmd.getColumnPrivileges(null, null, tablename, null);
          } break;
          case Capabilities.ExportedKeys: {
            rs = dbmd.getExportedKeys(null, null, tablename);
          } break;
          case Capabilities.ImportedKeys: {
            rs = dbmd.getImportedKeys(null, null, tablename);
          } break;
          case Capabilities.IndexInfo: {
            rs = dbmd.getIndexInfo(null, null, tablename, false, false/*true -- testing !!!*/);
          } break;
          case Capabilities.PrimaryKeys: {
            rs = dbmd.getPrimaryKeys(null, null, tablename);
          } break;
          case Capabilities.ProcedureColumns: {
            if(!tabled) {
              rs = dbmd.getProcedureColumns(null, null, null, null);
            }
          } break;
          case Capabilities.Procedures: {
            if(!tabled) {
              rs = dbmd.getProcedures(null, null, null);
            }
          } break;
          case Capabilities.TablePrivileges: {
            rs = dbmd.getTablePrivileges(null, null, tablename);
          } break;
          case Capabilities.UserDefinedTypes: {
            if(!tabled) {
              rs = dbmd.getUDTs(null, null, null, null);
            }
          } break;
          case Capabilities.VersionColumns: {
            rs = dbmd.getVersionColumns(null, null, tablename);
          } break;
        }
        if(rs == null) {
          ec.addElement("Unable to output " + cap.Image()).addElement(BRLF);
        } else {
          ec.addElement(AnyDBTableGen.output(cap.Image(), cs, rs, null)); // show them all
        }
        ec.addElement(BRLF);
      } catch (SQLException e2) {
        ec.addElement("Exception performing: " + cap);
        ec.addElement(printSQLException(e2));
        dbg.Caught(e2);
      }
    }
    return ec;
  }

  private static final Element settings(DatabaseMetaData dbmd, ColorScheme cs) {
    ElementContainer ec = new ElementContainer();
    try {
    String tdata[][] = {
        // this is sorted alphabetically.  please keep it that way.
      {"allProceduresAreCallable"                 , o(dbmd.allProceduresAreCallable())},
      {"allTablesAreSelectable"                   , o(dbmd.allTablesAreSelectable())},
      {"dataDefinitionCausesTransactionCommit"    , o(dbmd.dataDefinitionCausesTransactionCommit())},
      {"dataDefinitionIgnoredInTransactions"      , o(dbmd.dataDefinitionIgnoredInTransactions())},
      {"doesMaxRowSizeIncludeBlobs"               , o(dbmd.doesMaxRowSizeIncludeBlobs())},
      {"getCatalogSeparator"                      , o(dbmd.getCatalogSeparator())},
      {"getCatalogTerm"                           , o(dbmd.getCatalogTerm())},
      {"getDatabaseProductName"                   , o(dbmd.getDatabaseProductName())},
      {"getDatabaseProductVersion"                , o(dbmd.getDatabaseProductVersion())},
      {"getDefaultTransactionIsolation"           , o(dbmd.getDefaultTransactionIsolation())},
      {"getDriverMajorVersion"                    , o(dbmd.getDriverMajorVersion())},
      {"getDriverMinorVersion"                    , o(dbmd.getDriverMinorVersion())},
      {"getDriverName"                            , o(dbmd.getDriverName())},
      {"getDriverVersion"                         , o(dbmd.getDriverVersion())},
      {"getExtraNameCharacters"                   , o(dbmd.getExtraNameCharacters())},
      {"getIdentifierQuoteString"                 , o(dbmd.getIdentifierQuoteString())},
      {"getMaxBinaryLiteralLength"                , o(dbmd.getMaxBinaryLiteralLength())},
      {"getMaxCatalogNameLength"                  , o(dbmd.getMaxCatalogNameLength())},
      {"getMaxCharLiteralLength"                  , o(dbmd.getMaxCharLiteralLength())},
      {"getMaxColumnNameLength"                   , o(dbmd.getMaxColumnNameLength())},
      {"getMaxColumnsInGroupBy"                   , o(dbmd.getMaxColumnsInGroupBy())},
      {"getMaxColumnsInIndex"                     , o(dbmd.getMaxColumnsInIndex())},
      {"getMaxColumnsInOrderBy"                   , o(dbmd.getMaxColumnsInOrderBy())},
      {"getMaxColumnsInSelect"                    , o(dbmd.getMaxColumnsInSelect())},
      {"getMaxColumnsInTable"                     , o(dbmd.getMaxColumnsInTable())},
      {"getMaxConnections"                        , o(dbmd.getMaxConnections())},
      {"getMaxCursorNameLength"                   , o(dbmd.getMaxCursorNameLength())},
      {"getMaxIndexLength"                        , o(dbmd.getMaxIndexLength())},
      {"getMaxProcedureNameLength"                , o(dbmd.getMaxProcedureNameLength())},
      {"getMaxRowSize"                            , o(dbmd.getMaxRowSize())},
      {"getMaxSchemaNameLength"                   , o(dbmd.getMaxSchemaNameLength())},
      {"getMaxStatementLength"                    , o(dbmd.getMaxStatementLength())},
      {"getMaxStatements"                         , o(dbmd.getMaxStatements())},
      {"getMaxTableNameLength"                    , o(dbmd.getMaxTableNameLength())},
      {"getMaxTablesInSelect"                     , o(dbmd.getMaxTablesInSelect())},
      {"getMaxUserNameLength"                     , o(dbmd.getMaxUserNameLength())},
      {"getNumericFunctions"                      , o(dbmd.getNumericFunctions())},
      {"getProcedureTerm"                         , o(dbmd.getProcedureTerm())},
      {"getSQLKeywords"                           , o(dbmd.getSQLKeywords())},
      {"getSchemaTerm"                            , o(dbmd.getSchemaTerm())},
      {"getSearchStringEscape"                    , o(dbmd.getSearchStringEscape())},
      {"getStringFunctions"                       , o(dbmd.getStringFunctions())},
      {"getSystemFunctions"                       , o(dbmd.getSystemFunctions())},
      {"getTimeDateFunctions"                     , o(dbmd.getTimeDateFunctions())},
      {"getURL"                                   , o(dbmd.getURL())},
      {"getUserName"                              , o(dbmd.getUserName())},
      {"isCatalogAtStart"                         , o(dbmd.isCatalogAtStart())},
      {"isReadOnly"                               , o(dbmd.isReadOnly())},
      {"nullPlusNonNullIsNull"                    , o(dbmd.nullPlusNonNullIsNull())},
      {"nullsAreSortedAtEnd"                      , o(dbmd.nullsAreSortedAtEnd())},
      {"nullsAreSortedAtStart"                    , o(dbmd.nullsAreSortedAtStart())},
      {"nullsAreSortedHigh"                       , o(dbmd.nullsAreSortedHigh())},
      {"nullsAreSortedLow"                        , o(dbmd.nullsAreSortedLow())},
      {"storesLowerCaseIdentifiers"               , o(dbmd.storesLowerCaseIdentifiers())},
      {"storesLowerCaseQuotedIdentifiers"         , o(dbmd.storesLowerCaseQuotedIdentifiers())},
      {"storesMixedCaseIdentifiers"               , o(dbmd.storesMixedCaseIdentifiers())},
      {"storesMixedCaseQuotedIdentifiers"         , o(dbmd.storesMixedCaseQuotedIdentifiers())},
      {"storesUpperCaseIdentifiers"               , o(dbmd.storesUpperCaseIdentifiers())},
      {"storesUpperCaseQuotedIdentifiers"         , o(dbmd.storesUpperCaseQuotedIdentifiers())},
      {"supportsANSI92EntryLevelSQL"              , o(dbmd.supportsANSI92EntryLevelSQL())},
      {"supportsANSI92FullSQL"                    , o(dbmd.supportsANSI92FullSQL())},
      {"supportsANSI92IntermediateSQL"            , o(dbmd.supportsANSI92IntermediateSQL())},
      {"supportsAlterTableWithAddColumn"          , o(dbmd.supportsAlterTableWithAddColumn())},
      {"supportsAlterTableWithDropColumn"         , o(dbmd.supportsAlterTableWithDropColumn())},
      {"supportsBatchUpdates"                     , o(dbmd.supportsBatchUpdates())},
      {"supportsCatalogsInDataManipulation"       , o(dbmd.supportsCatalogsInDataManipulation())},
      {"supportsCatalogsInIndexDefinitions"       , o(dbmd.supportsCatalogsInIndexDefinitions())},
      {"supportsCatalogsInPrivilegeDefinitions"   , o(dbmd.supportsCatalogsInPrivilegeDefinitions())},
      {"supportsCatalogsInProcedureCalls"         , o(dbmd.supportsCatalogsInProcedureCalls())},
      {"supportsCatalogsInTableDefinitions"       , o(dbmd.supportsCatalogsInTableDefinitions())},
      {"supportsColumnAliasing"                   , o(dbmd.supportsColumnAliasing())},
      {"supportsConvert"                          , o(dbmd.supportsConvert())},
      {"supportsCoreSQLGrammar"                   , o(dbmd.supportsCoreSQLGrammar())},
      {"supportsCorrelatedSubqueries"             , o(dbmd.supportsCorrelatedSubqueries())},
      {"supportsDataDefinitionAndDataManipulationTransactions", o(dbmd.supportsDataDefinitionAndDataManipulationTransactions())},
      {"supportsDataManipulationTransactionsOnly" , o(dbmd.supportsDataManipulationTransactionsOnly())},
      {"supportsDifferentTableCorrelationNames"   , o(dbmd.supportsDifferentTableCorrelationNames())},
      {"supportsExpressionsInOrderBy"             , o(dbmd.supportsExpressionsInOrderBy())},
      {"supportsExtendedSQLGrammar"               , o(dbmd.supportsExtendedSQLGrammar())},
      {"supportsFullOuterJoins"                   , o(dbmd.supportsFullOuterJoins())},
      {"supportsGroupBy"                          , o(dbmd.supportsGroupBy())},
      {"supportsGroupByBeyondSelect"              , o(dbmd.supportsGroupByBeyondSelect())},
      {"supportsGroupByUnrelated"                 , o(dbmd.supportsGroupByUnrelated())},
      {"supportsIntegrityEnhancementFacility"     , o(dbmd.supportsIntegrityEnhancementFacility())},
      {"supportsLikeEscapeClause"                 , o(dbmd.supportsLikeEscapeClause())},
      {"supportsLimitedOuterJoins"                , o(dbmd.supportsLimitedOuterJoins())},
      {"supportsMinimumSQLGrammar"                , o(dbmd.supportsMinimumSQLGrammar())},
      {"supportsMixedCaseIdentifiers"             , o(dbmd.supportsMixedCaseIdentifiers())},
      {"supportsMixedCaseQuotedIdentifiers"       , o(dbmd.supportsMixedCaseQuotedIdentifiers())},
      {"supportsMultipleResultSets"               , o(dbmd.supportsMultipleResultSets())},
      {"supportsMultipleTransactions"             , o(dbmd.supportsMultipleTransactions())},
      {"supportsNonNullableColumns"               , o(dbmd.supportsNonNullableColumns())},
      {"supportsOpenCursorsAcrossCommit"          , o(dbmd.supportsOpenCursorsAcrossCommit())},
      {"supportsOpenCursorsAcrossRollback"        , o(dbmd.supportsOpenCursorsAcrossRollback())},
      {"supportsOpenStatementsAcrossCommit"       , o(dbmd.supportsOpenStatementsAcrossCommit())},
      {"supportsOpenStatementsAcrossRollback"     , o(dbmd.supportsOpenStatementsAcrossRollback())},
      {"supportsOrderByUnrelated"                 , o(dbmd.supportsOrderByUnrelated())},
      {"supportsOuterJoins"                       , o(dbmd.supportsOuterJoins())},
      {"supportsPositionedDelete"                 , o(dbmd.supportsPositionedDelete())},
      {"supportsPositionedUpdate"                 , o(dbmd.supportsPositionedUpdate())},
      {"supportsSchemasInDataManipulation"        , o(dbmd.supportsSchemasInDataManipulation())},
      {"supportsSchemasInIndexDefinitions"        , o(dbmd.supportsSchemasInIndexDefinitions())},
      {"supportsSchemasInPrivilegeDefinitions"    , o(dbmd.supportsSchemasInPrivilegeDefinitions())},
      {"supportsSchemasInProcedureCalls"          , o(dbmd.supportsSchemasInProcedureCalls())},
      {"supportsSchemasInTableDefinitions"        , o(dbmd.supportsSchemasInTableDefinitions())},
      {"supportsSelectForUpdate"                  , o(dbmd.supportsSelectForUpdate())},
      {"supportsStoredProcedures"                 , o(dbmd.supportsStoredProcedures())},
      {"supportsSubqueriesInComparisons"          , o(dbmd.supportsSubqueriesInComparisons())},
      {"supportsSubqueriesInExists"               , o(dbmd.supportsSubqueriesInExists())},
      {"supportsSubqueriesInIns"                  , o(dbmd.supportsSubqueriesInIns())},
      {"supportsSubqueriesInQuantifieds"          , o(dbmd.supportsSubqueriesInQuantifieds())},
      {"supportsTableCorrelationNames"            , o(dbmd.supportsTableCorrelationNames())},
      {"supportsTransactions"                     , o(dbmd.supportsTransactions())},
      {"supportsUnion"                            , o(dbmd.supportsUnion())},
      {"supportsUnionAll"                         , o(dbmd.supportsUnionAll())},
      {"usesLocalFilePerTable"                    , o(dbmd.usesLocalFilePerTable())},
      {"usesLocalFiles"                           , o(dbmd.usesLocalFiles())},
    };
    HeaderDef headers[] = {new HeaderDef(AlignType.LEFT, "Capability"),
                             new HeaderDef(AlignType.LEFT, "Setting")};
/*
// +++ add these (or as many as you can) !!!
deletesAreDetected(type)
insertsAreDetected(type)
othersDeletesAreVisible(type)
othersInsertsAreVisible(type)
othersUpdatesAreVisible(type)
ownDeletesAreVisible(type)
ownInsertsAreVisible(type)
ownUpdatesAreVisible(type)
supportsResultSetConcurrency(type, concurrency)
supportsResultSetType(type)
supportsTransactionIsolationLevel(level)
updatesAreDetected(type)
*/
      ec.addElement(ArrayTableGen.output("Capabilities", cs, tdata, headers));
    } catch (SQLException e) {
      ec.addElement(printSQLException(e));
      dbg.Caught(e);
    } finally {
      return ec;
    }
  }

}


class Capabilities extends TrueEnum {
  public final static int Schemas = 0;
  public final static int Catalogs = 1;
  public final static int TableTypes = 2;
  public final static int BestRowIdentifier = 3;
  public final static int CrossReferences = 4;
  public final static int ColumnPrivileges = 5;
  public final static int ExportedKeys = 6;
  public final static int ImportedKeys = 7;
  public final static int IndexInfo = 8;
  public final static int PrimaryKeys = 9;
  public final static int ProcedureColumns = 10;
  public final static int Procedures = 11;
  public final static int TablePrivileges = 12;
  public final static int UserDefinedTypes = 13;
  public final static int VersionColumns = 14;

  private static final String [] texts = {
    "Schemas",
    "Catalogs",
    "TableTypes",
    "BestRowIdentifier",
    "CrossReferences",
    "ColumnPrivileges",
    "ExportedKeys",
    "ImportedKeys",
    "IndexInfo",
    "PrimaryKeys",
    "ProcedureColumns",
    "Procedures",
    "TablePrivileges",
    "UserDefinedTypes",
    "VersionColumns",
  };

  public int numValues(){ return 15; }


  protected final String [ ] getMyText() {
    return texts;
  }
  public static final Capabilities Prop=new Capabilities();
  public Capabilities(){
    super();
  }
  public Capabilities(int rawValue){
    super(rawValue);
  }
  public Capabilities(String textValue){
    super(textValue);
  }
  public Capabilities(Capabilities rhs){
    this(rhs.Value());
  }

}
