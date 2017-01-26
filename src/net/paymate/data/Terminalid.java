package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/Terminalid.java,v $
 * Description:  perpetually unique identifier for a logical terminal, not for specific hardware.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class Terminalid extends UniqueId {

  public Terminalid() {
  }

  public Terminalid(int value) {
    super(value);
  }

  public Terminalid(String value) {
    super(value);
  }

}
//$Id: Terminalid.java,v 1.3 2001/12/09 06:58:09 andyh Exp $