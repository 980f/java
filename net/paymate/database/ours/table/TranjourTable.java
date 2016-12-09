package net.paymate.database.ours.table;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TranjourTable.java,v 1.13 2001/11/16 13:17:27 mattm Exp $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class TranjourTable extends TableProfile implements DBConstants {

  public static final String TRANJOUR = "TRANJOUR";
  public static final String TXNID = "TXNID";
  public static final String AUTHSEQ = "AUTHSEQ";

  //public static final ColumnProfile txnid=ColumnProfile.create(TRANJOUR, TXNID, DBTypesFiltered.INTEGER, 8, ColumnProfile.NOTNULL, "TxnID", ColumnProfile.AUTO);
  public static final ColumnProfile authtermid=ColumnProfile.create(TRANJOUR, AUTHTERMID, DBTypesFiltered.CHAR, 10, ColumnProfile.ALLOWNULL, "AuthTermId", ColumnProfile.NOAUTO);
  public static final ColumnProfile authseq  =ColumnProfile.create(TRANJOUR, AUTHSEQ, DBTypesFiltered.INTEGER, 8,ColumnProfile.ALLOWNULL,"AuthSeq", ColumnProfile.NOAUTO);

  private static final ColumnProfile [] staticColumns = {
    //txnid,
    authtermid,
    authseq,
    // +++ do me !!!  and all other columns & tables, too !!!
  };

  public TranjourTable() {
    super(new TableInfo(TRANJOUR), staticColumns);
  }
}

// $Id: TranjourTable.java,v 1.13 2001/11/16 13:17:27 mattm Exp $
