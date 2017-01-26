package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/StoreAuthid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class StoreAuthid extends UniqueId {
  public StoreAuthid() {
  }

  public StoreAuthid(int value) {
    super(value);
  }

  public StoreAuthid(String value) {
    super(value);
  }

}