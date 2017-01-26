package net.paymate.authorizer.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PaymentechConstants.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

public interface PaymentechConstants {
  /* package */
  static final int MAXREQUESTSIZE = 500; // calculated at 476 for auths, but I want to make room for error:
  static final int HEADERLENGTH   = 6;  //IP header length
  static final String MONEYIMAGE  = "#0.00";
  // content constants
  static final String TERMINALCAPTUREINDICATOR = "K";
  static final String HOSTCAPTUREINDICATOR     = "L";
  static final String SYSTEMSEPARATOR          = ".";
  static final String ROUTINGINDICATOR         = "A02000";
  static final String TRANSACTIONCLASS         = "F";
  // +++ continue this ...
}

// $Id: PaymentechConstants.java,v 1.7 2004/04/15 04:31:13 mattm Exp $

