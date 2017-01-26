package net.paymate.web.table.query;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DrawerClosingFormat.java,v 1.36 2004/04/08 09:09:55 mattm Exp $
 */

import net.paymate.util.*;  // ErrorLogStream
import net.paymate.web.*; // logininfo
import net.paymate.web.table.*; //DBTableGen
import java.sql.*; // resultset
import org.apache.ecs.*; // element
import org.apache.ecs.html.*; // various html elements
import net.paymate.database.*; // db
import net.paymate.database.ours.query.*; // Drawer
import net.paymate.web.page.*; // Acct
import net.paymate.web.AdminOp;
import net.paymate.data.*; // LedgerValue
import net.paymate.lang.StringX;
import net.paymate.data.sinet.business.*;

public class DrawerClosingFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DrawerClosingFormat.class);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new DrawerClosingFormatEnum()).numValues()];
  static { // keeping these separate makes it easier to keep their order straight // order is set in the enum, nowhere else
    theHeaders[DrawerClosingFormatEnum.TimeCol]      = new HeaderDef(AlignType.LEFT ,"Time");
    theHeaders[DrawerClosingFormatEnum.StoreCol]     = new HeaderDef(AlignType.LEFT ,"Store");
    theHeaders[DrawerClosingFormatEnum.TermCol]      = new HeaderDef(AlignType.LEFT ,"Terminal");
    theHeaders[DrawerClosingFormatEnum.AssociateCol] = new HeaderDef(AlignType.LEFT ,"Associate");
    theHeaders[DrawerClosingFormatEnum.CountCol]     = new HeaderDef(AlignType.RIGHT,"Count");
    theHeaders[DrawerClosingFormatEnum.AmountCol]    = new HeaderDef(AlignType.RIGHT,"Total");
    theHeaders[DrawerClosingFormatEnum.CSVCol]       = new HeaderDef(AlignType.RIGHT,"Export");
  }

  private DrawerRow drawer = null;
  private boolean archive = false;
  public DrawerClosingFormat(LoginInfo linfo, DrawerRow drawer, String title, SubTotaller totaller, boolean archive) {
    super(linfo.colors(), title, drawer, null, linfo.ltf());
    this.drawer = drawer;
    headers = theHeaders;
    // new stuff
    this.linfo = linfo;
    this.totaller = totaller;
    this.archive = archive;
  }

  // new stuff
  private SubTotaller totaller = null;
  private LoginInfo linfo = null;

  public static final String BMID = "bmid";

  private LedgerValue amount = new LedgerValue(UnsettledTransactionFormat.moneyformat); //+_+ to keep the drawer report and this report looking the same

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        // do the real data
        drawercnt++;
        long amt = StringX.parseLong(drawer.txntotal);
        long cnt = StringX.parseLong(drawer.txncount);
        apprCount+=cnt;
        total+=amt;
        amount.setto(amt);
        String pather = null;
        if(archive) {
          pather = ReceiptArchiver.drawerForDrawers(drawer.drawerid);
        } else {
          pather = Acct.key() + "?" + AdminOp.drawerAdminOp.url() + "&" + BMID + "=" + drawer.drawerid;
        }
        setColumn(DrawerClosingFormatEnum.AmountCol, new A(pather , new StringElement(amount.Image()))); //utc#
        Store store = StoreHome.Get(new Storeid(drawer.storeid));
        setColumn(DrawerClosingFormatEnum.StoreCol    , new StringElement(store.storename));
        setColumn(DrawerClosingFormatEnum.TermCol     , new StringElement(drawer.terminalName));
        setColumn(DrawerClosingFormatEnum.CountCol    , new StringElement(drawer.txncount));
        setColumn(DrawerClosingFormatEnum.TimeCol     , ltf.format(PayMateDBQueryString.tranUTC(drawer.transtarttime))); // link via drawer.drawerid
        if(archive) {
          // don't put anything in this column
        } else {
          setColumn(DrawerClosingFormatEnum.CSVCol, Acct.TSVDrawerLink(drawer.drawerid));
        }
        Associate assoc = AssociateHome.Get(new Associateid(drawer.associateid));
        if(assoc != null) {
          setColumn(DrawerClosingFormatEnum.AssociateCol,
                    new StringElement(assoc.firstMiddleLast()));
        }
        if(linfo.store.enlistsummary && ! archive) {
          UnsettledTransactionFormat.getSubsNoDetail(
              PayMateDBDispenser.getPayMateDB().getDrawerClosingCursor(
              new Drawerid(drawer.drawerid), linfo.ltf()),
              true /*countLosses*/, linfo, totaller, false /*we will never create this page from here in archive mode*/); // new stuff
        }
      }
    } catch (Exception t2) {
      dbg.Enter("nextRow");
      dbg.WARNING("Unknown and general exception generating next row content.");
      dbg.Exit();
    } finally {
      return (tgr == null) ? null : this;
    }
  }

  protected int footerRows() {
    return 1;
  }

  private long apprCount = 0;
  private long total = 0;
  private int drawercnt = 0;

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case DrawerClosingFormatEnum.TimeCol: {
          ret = "TOTALS:";
        } break;
        case DrawerClosingFormatEnum.TermCol: {
          ret = String.valueOf(drawercnt);
        } break;
        case DrawerClosingFormatEnum.CountCol: {
          ret = Long.toString(apprCount);
        } break;
        case DrawerClosingFormatEnum.AmountCol: {
          amount.setto(total);
          ret = amount.Image();
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }
}
//$Id: DrawerClosingFormat.java,v 1.36 2004/04/08 09:09:55 mattm Exp $
