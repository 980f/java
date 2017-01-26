package net.paymate.authorizer.cardSystems;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CardSystemConstants.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface CardSystemConstants {

  public static final int MAXREQUEST = 313; // <-calculated, not including wrapper // old: 192;
  public static final String BATCHUPLOADMORERECORDS = "U2.";
  public static final String BATCHUPLOADLASTRECORD = "U0.";
}