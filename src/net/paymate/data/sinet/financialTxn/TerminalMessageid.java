package net.paymate.data.sinet.financialTxn;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/financialTxn/TerminalMessageid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class TerminalMessageid extends UniqueId {

  public TerminalMessageid() {
  }

  public TerminalMessageid(int value) {
    super(value);
  }

  public TerminalMessageid(String value) {
    super(value);
  }
}
