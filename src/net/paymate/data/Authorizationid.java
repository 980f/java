package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/Authorizationid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class Authorizationid extends UniqueId {

  public Authorizationid() {
  }
  public Authorizationid(int value) {
    super(value);
  }
  public Authorizationid(String value) {
    super(value);
  }
}