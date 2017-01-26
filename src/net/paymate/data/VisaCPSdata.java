package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/VisaCPSdata.java,v $</p>
 * <p>Description: The CardPresentSignature data as spccified by VISA [spec location unknown, but to date, 3 different authorizers have all done it the same way].</p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class VisaCPSdata extends CPSdata {

  public VisaCPSdata() {
  }

  public VisaCPSdata(String cpsaci, String cpsrespcode, String cpstxnid, String cpsvalcode) {
    setto(cpsaci, cpsrespcode, cpstxnid, cpsvalcode);
  }

  public void setto(String cpsaci, String cpsrespcode, String cpstxnid, String cpsvalcode) {
    this.cpsaci      = cpsaci;
    this.cpsrespcode = cpsrespcode;
    this.cpstxnid    = cpstxnid;
    this.cpsvalcode  = cpsvalcode;
  }

  // +++ Enforce the lengths!
  public String cpsaci = "";      //  1 character +++ develop a class for this
  public String cpsrespcode = ""; //  2 characters
  public String cpstxnid = "";    // 15 characters
  public String cpsvalcode = "";  //  4 characters

  public boolean isValid() {
    // really need to trim the values first, or on setting them!
    return StringX.NonTrivial(cpsaci) && StringX.NonTrivial(cpsrespcode) && StringX.NonTrivial(cpstxnid) && StringX.NonTrivial(cpsvalcode);
  }

  public String toString() {
    TextList tl = new TextList();
    tl.add("cpsaci"     , cpsaci);
    tl.add("cpsrespcode", cpsrespcode);
    tl.add("cpstxnid"   , cpstxnid);
    tl.add("cpsvalcode" , cpsvalcode);
    return tl.toString();
  }

}