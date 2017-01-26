package net.paymate.web.table.query;

/**
* Title:        StoreFormat<p>
* Description:  The canned query for the Stores screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: StoreAuthsFormat.java,v 1.3 2003/10/30 21:05:18 mattm Exp $
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


public class StoreAuthsFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreAuthsFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new StoreAuthsFormatEnum()).numValues()];
  static {
    theHeaders[StoreAuthsFormatEnum.AuthidCol]       = new HeaderDef(AlignType.RIGHT, "Authid");
    theHeaders[StoreAuthsFormatEnum.AuthMerchidCol]  = new HeaderDef(AlignType.LEFT , "AuthMerchid");
    theHeaders[StoreAuthsFormatEnum.InstCol]         = new HeaderDef(AlignType.LEFT , "Inst");
    theHeaders[StoreAuthsFormatEnum.MaxTxnLimitCol]  = new HeaderDef(AlignType.RIGHT, "MaxTxnLimit");
    theHeaders[StoreAuthsFormatEnum.PaytypeCol]      = new HeaderDef(AlignType.LEFT , "Paytype");
    theHeaders[StoreAuthsFormatEnum.SettleidCol]     = new HeaderDef(AlignType.RIGHT, "Settleid");
    theHeaders[StoreAuthsFormatEnum.SettleMerchidCol]= new HeaderDef(AlignType.LEFT , "SettleMerchid");
    theHeaders[StoreAuthsFormatEnum.StoreAuthidCol]  = new HeaderDef(AlignType.RIGHT, "StoreAuthid");
    theHeaders[StoreAuthsFormatEnum.StoreidCol]      = new HeaderDef(AlignType.RIGHT,"Storeid");
  }

  private StoreauthRow storeInfo = null;
  public static final String STOREAUTHID="SAUID";

  public StoreAuthsFormat(LoginInfo linfo, StoreauthRow storeInfo, String title,
                          String absoluteURL) {
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
            (new AdminOpCode(AdminOpCode.storeauth)).Image() +
            "&"+STOREAUTHID+"=" + storeInfo.storeauthid;
        setColumn(StoreAuthsFormatEnum.AuthidCol, storeInfo.authid);
        setColumn(StoreAuthsFormatEnum.AuthMerchidCol, storeInfo.authmerchid);
        setColumn(StoreAuthsFormatEnum.InstCol, storeInfo.institution);
        setColumn(StoreAuthsFormatEnum.MaxTxnLimitCol, storeInfo.maxtxnlimit);
        setColumn(StoreAuthsFormatEnum.PaytypeCol, storeInfo.paytype);
        setColumn(StoreAuthsFormatEnum.SettleidCol, storeInfo.settleid);
        setColumn(StoreAuthsFormatEnum.SettleMerchidCol, storeInfo.settlemerchid);
        setColumn(StoreAuthsFormatEnum.StoreAuthidCol, new A(editurl, storeInfo.storeauthid));
        setColumn(StoreAuthsFormatEnum.StoreidCol, storeInfo.storeid);
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
