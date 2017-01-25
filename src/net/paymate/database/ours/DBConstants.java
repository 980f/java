package net.paymate.database.ours;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DBConstants.java,v 1.21 2001/11/17 06:16:58 mattm Exp $
 */

import net.paymate.database.*;

public interface DBConstants {
  // shared fieldnames DO NOT *CHANGE* THESE CONSTANTS, EVER !!!
              static final String ACCOUNT            = "cardholderaccount";
  /*package*/ static final String ACTIONCODE         = "actioncode";
              static final String AMOUNT             = "transactionamount";
  /*package*/ static final String APPLIANCEID        = "applianceid";
  /*package*/ static final String ASSOCIATEID        = "associateID";
  /*package*/ static final String AUTHTERMID         = "AUTHTERMID";
  /*package*/ static final String DRAWERID           = "drawerid";
  /*package*/ static final String STOREID            = "STOREID";
  /*package*/ static final String CARDACCEPTORTERMID = "CardAcceptorTERMID";
              static final String CLIENTREFTIME      = "CLIENTREFTIME";
  /*package*/ static final String COLORSCHEMEID      = "COLORSCHEMEID";
  /*package*/ static final String ENTERPRISEID       = "ENTERPRISEID";
  /*package*/ static final String MESSAGETYPE        = "messagetype";
  /*package*/ static final String RECEIPTAUTH        = "receiptAuth";
  /*package*/ static final String RECEIPTSTAN        = "receiptStan";
  /*package*/ static final String RECEIPTTIME        = "receiptTime";
  /*package*/ static final String STATE              = "state";
              static final String STOODINSTAN        = "STOODINSTAN";
  /*package*/ static final String STAN               = "stan";
  /*package*/ static final String TERMINALID         = "terminalid";
  /*package*/ static final String TERMINALNAME       = "terminalname";
  /*package*/ static final String TRANSTARTTIME      = "transtarttime";
}
//$Id: DBConstants.java,v 1.21 2001/11/17 06:16:58 mattm Exp $
