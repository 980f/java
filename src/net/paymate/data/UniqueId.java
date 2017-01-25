package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/UniqueId.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class UniqueId {

  public static final int INVALID = -1;
  private /*---*/ int value = INVALID;

  public boolean isValid() {
    return value > 0; // anything less than 1 is invalid!
  }

  public String toString() {
    return String.valueOf(value);
  }

  // +++ check for valid and standardize to -1 if not?
  public UniqueId setValue(int value) {
    this.value = value;
    return this;
  }

  public int value() {
    return value;
  }

  public UniqueId() {
    this(INVALID);
  }

  public UniqueId(int value) {
    setValue(value);
  }
}
