package net.paymate.terminalClient.PosSocket;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/HyperFormatter.java,v $
 * Description:  hypercom variation on asciiformatter
 *      will probably merge back into asciiFormatter once
 *      format version stuff is explicitly managed.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.14 $
 * @legacy: H2A4,HYPER6500,Z80POS,Credit,Sale,258,000141,
 */

import net.paymate.util.*;
import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.lang.ReflectX;

public class HyperFormatter extends AsciiFormatter {
  static ErrorLogStream dbg;

  public String formatId(){
    return "H3."+super.formatId();
  }

//find txn number after amount but before payment info
//if txn number is <=zero then paymate host will supply one and what goes in here does NOT match what comes back.
  protected SaleInfo saleInfo(TextListIterator fields){
    SaleInfo sale=super.saleInfo(fields);
    sale.stan=getStan(fields);
    return sale;
  }

  /**
   * added to deal with legacy format hypercom terminal H2A4
   */
  public ActionRequest requestFrom(TextListIterator fields){
    if(fields.lookAhead()=="H2A4"){
      fields.next();//discard format indicator
      fields.next();//discard login
      fields.next();//discard password
    }
    return super.requestFrom(fields);
  }

  public HyperFormatter() {//for polymorphic instantiation
    if(dbg==null) dbg=ErrorLogStream.getForClass(HyperFormatter.class);
    dbg.VERBOSE("Instantiated:"+ReflectX.shortClassName(this));
    //set AsciiFormatter options:
    showListing=false;
    showTotals=false;
  }

}
//$Id: HyperFormatter.java,v 1.14 2003/10/25 20:34:26 mattm Exp $