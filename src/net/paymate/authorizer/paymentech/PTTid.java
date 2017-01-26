package net.paymate.authorizer.paymentech;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/paymentech/PTTid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.Ascii;
import net.paymate.lang.StringX;

public class PTTid {
  public PTTid(String from) {
    setto(from);
  }
  public PTTid() {
    clear();
  }

  public void setto(String from) {
    clear();
    this.from = from;
    if(StringX.NonTrivial(from)) {
      int len = from.length();
      if(len < 4) { // most are of the form "002"
        // use it as it is (this must not be a NetConnect user)
        tid = from;
      } else {
        // break it down
        // always of the form: TIDUSERNAME,PASSWORD
        // where TID is always 3 numeric characters
        // and USERNAME has no commas in it
        // and PASSWORD is whatever is left
        int tidlen = 3;
        int commat = from.indexOf(",");
        // +++ test !!!  This may be broken!
        tid = StringX.left(from, tidlen);
        username = StringX.subString(from, tidlen, commat);
        password = StringX.right(from, len - commat - 1);
      }
    } else {
      // do nothing
    }
    tidint = StringX.parseInt(tid);
  }

  public void clear() {
    tid=username=password="";
    tidint = 0;
  }

  public String spam() {
    return
        "from="+Ascii.bracket(from)+"\n"+
        "tid="+Ascii.bracket(tid)+"\n"+
        "username="+Ascii.bracket(username)+"\n"+
        "password="+Ascii.bracket(password)+"\n"+
        "tidint="+tidint;
  }

  private String from = "";
  public int tidint;
  public String tid;
  public String username;
  public String password;

}