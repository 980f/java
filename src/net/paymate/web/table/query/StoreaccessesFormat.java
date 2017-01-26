package net.paymate.web.table.query;

/**
* Title:        StoreFormat<p>
* Description:  The canned query for the Stores screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: StoreaccessesFormat.java,v 1.3 2003/10/30 21:05:18 mattm Exp $
*/

import  net.paymate.database.ours.query.*; // StoreInfo
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //HeaderDef
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
// for the ennum:
import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;


public class StoreaccessesFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreaccessesFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new StoreAccessFormatEnum()).numValues()];
  static {
    theHeaders[StoreAccessFormatEnum.AssociateidCol]  =new HeaderDef(AlignType.RIGHT,"Associateid");
    theHeaders[StoreAccessFormatEnum.EnclosedrawerCol]=new HeaderDef(AlignType.LEFT ,"CanCloseDrawer");
    theHeaders[StoreAccessFormatEnum.EnreturnCol]     =new HeaderDef(AlignType.LEFT ,"CanReturn");
    theHeaders[StoreAccessFormatEnum.EnsaleCol]       =new HeaderDef(AlignType.LEFT ,"CanSale");
    theHeaders[StoreAccessFormatEnum.EnvoidCol]       =new HeaderDef(AlignType.LEFT ,"CanVoid");
    theHeaders[StoreAccessFormatEnum.StoreaccessidCol]=new HeaderDef(AlignType.RIGHT,"Storeaccessid");
    theHeaders[StoreAccessFormatEnum.StoreidCol]      =new HeaderDef(AlignType.RIGHT,"Storeid");
  }

  private StoreaccessRow storeInfo = null;
  public static final String STOREACCESSID="SAXID";

  public StoreaccessesFormat(LoginInfo linfo, StoreaccessRow storeInfo,
                             String title, String absoluteURL) {
    super(linfo.colors(), title, storeInfo, absoluteURL, linfo.ltf());
    this.storeInfo = storeInfo;
    headers = theHeaders;
  }

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        // do the real data
        String editurl = Acct.key() + "?adm=" +
            (new AdminOpCode(AdminOpCode.storeaccess)).Image() +
            "&"+STOREACCESSID+"=" + storeInfo.storeaccessid;
        setColumn(StoreAccessFormatEnum.AssociateidCol, storeInfo.associateid);
        setColumn(StoreAccessFormatEnum.EnclosedrawerCol, storeInfo.enclosedrawer);
        setColumn(StoreAccessFormatEnum.EnreturnCol, storeInfo.enreturn);
        setColumn(StoreAccessFormatEnum.EnsaleCol, storeInfo.ensale);
        setColumn(StoreAccessFormatEnum.EnvoidCol, storeInfo.envoid);
        setColumn(StoreAccessFormatEnum.StoreaccessidCol, new A(editurl, storeInfo.storeaccessid));
        setColumn(StoreAccessFormatEnum.StoreidCol, storeInfo.storeid);
      } else {
        dbg.WARNING("RecordFormat.next() returned null!");
      }
    } catch (Exception t2) {
      dbg.WARNING("Unknown and general exception generating next row content.");
    } finally {
      dbg.Exit();
      return (tgr == null) ? null : this;
    }
  }
}
