package net.paymate.data.sinet.financialTxn;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/financialTxn/MerchantPermid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class MerchantPermid extends UniqueId {

  public MerchantPermid() {
  }

  public MerchantPermid(int value) {
    super(value);
  }

  public MerchantPermid(String value) {
    super(value);
  }
}
