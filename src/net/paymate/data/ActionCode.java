package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/ActionCode.java,v $
 * Description:  the mutually exclusive results of an authorization
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

public interface ActionCode {
  public static final String Unknown ="U";
  public static final String Approved="A";
  public static final String Declined="D";
  public static final String Failed  ="F";
  //the next two were added for client manipulaiton of batch listings. They aren't yet ever put into database
  public static final String Pending ="P";//to distinguish defective records from one being processed
  public static final String Loss    ="L";//sometimes is approved, sometimes declined, depending upon who is looking at it.
}
//$Id: ActionCode.java,v 1.5 2003/04/25 00:24:02 andyh Exp $