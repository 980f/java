/* $Id: IPSpec.java,v 1.10 2001/03/15 02:00:00 mattm Exp $ */

package net.paymate.net;
import net.paymate.util.Safe;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;

// +_+ {I'm not really sure why we are separating all this.
//  We could probably just leave it as a string (ip:port)
//  since that is how it will be transmitted anyway.
//  As it is, we break it apart here and put it back together again later.
//answer: this was created to clean up some third party source, now abandoned.
//}
public class IPSpec {
  public String address= "localhost";
  public int port=0;

  public boolean isTrivial(){
    return port==0 || !Safe.NonTrivial(address) || address.equalsIgnoreCase("dummy");
  }

  public IPSpec(String ipname,int aport){
    set(ipname, aport);
  }

  public IPSpec(String ipname){//one that accepts "hostname:portnumber"
    this();
    fromString(ipname);
  }

  public IPSpec() {
    //
  }

  public void set(String ipname,int aport) {
    address= ipname;
    port=aport;
  }

  public void fromString(String ipname) {
    if(Safe.NonTrivial(ipname)){
      int divider = ipname.lastIndexOf(":");
      if(divider >=0) {
        String justip = ipname.substring(0, divider);
        int aport = Integer.parseInt(ipname.substring(divider+1));
        set(justip, aport);
      }
    } else {
      //+_+ un changed, should wipe???
    }
  }

  public String toString(){
    return address+":"+port;
  }

}
//$Id: IPSpec.java,v 1.10 2001/03/15 02:00:00 mattm Exp $
