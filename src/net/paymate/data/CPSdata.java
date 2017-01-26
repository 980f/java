package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/CPSdata.java,v $</p>
 * <p>Description: Compliance data (usually either MC or VS)</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public abstract class CPSdata {

  protected CPSdata() {
  }

  public abstract boolean isValid();
  public abstract String toString();

  public static final boolean isValid(CPSdata cpsd) {
    return (cpsd != null) && cpsd.isValid();
  }

}