package net.paymate.data.sinet.financialTxn;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/financialTxn/Processorid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Processorid extends UniqueId {

  public Processorid() {
  }

  public Processorid(int value) {
    super(value);
  }

  public Processorid(String value) {
    super(value);
  }
}
