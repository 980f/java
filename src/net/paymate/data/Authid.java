package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/Authid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class Authid extends UniqueId {

  public Authid() {
  }

  public Authid(int value) {
    super(value);
  }

  public Authid(String value) {
    super(value);
  }
}