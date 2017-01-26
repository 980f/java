package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/MastercardCPSdata.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.TextList;
import net.paymate.lang.StringX;

public class MastercardCPSdata extends CPSdata {

  public MastercardCPSdata() {
  }

  public MastercardCPSdata(String referenceNumber, String date) {
    setto(referenceNumber, date);
  }

  // +++ Enforce the lengths!
  public String referenceNumber = ""; // 9 characters
  public String date            = ""; //  4 characters

  public void setto(String referenceNumber, String date) {
    this.referenceNumber = referenceNumber;
    this.date            = date;
  }

  public boolean isValid() {
    // really need to trim the values first, or on setting them!
    return StringX.NonTrivial(referenceNumber) && StringX.NonTrivial(date);
  }

  public String toString() {
    TextList tl = new TextList();
    tl.add("referenceNumber", referenceNumber);
    tl.add("date"           , date);
    return tl.toString();
  }
}

// $Id: MastercardCPSdata.java,v 1.2 2003/07/27 05:34:57 mattm Exp $
