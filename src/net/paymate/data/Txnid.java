package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/Txnid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

public class Txnid extends UniqueId {

  public Txnid() {
  //default, which is invalid.
  }

  public Txnid(int value) {
    super(value);
  }

  public Txnid(String value) {
    super(value);
  }

}
//$Id: Txnid.java,v 1.6 2001/12/09 06:58:10 andyh Exp $