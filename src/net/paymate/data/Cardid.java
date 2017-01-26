package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/Cardid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class Cardid extends UniqueId {
  public Cardid() {
  }

  public Cardid(int value) {
    super(value);
  }

  public Cardid(String value) {
    super(value);
  }

}