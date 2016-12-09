package net.paymate.connection;

/**
 * Title:
 * Description:  Formatter for a Batch (Drawer) Listing's Line
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: BatchLineItem.java,v 1.8 2001/07/07 03:31:49 andyh Exp $
 */

import net.paymate.util.*;
import net.paymate.ISO8583.data.*;
import net.paymate.jpos.data.*;
import java.util.*;
import net.paymate.util.*;

public class BatchLineItem {
  public final static char space=' ';

  public CardNumber card=new CardNumber(); //and Checks are made to look like cards.
  public Date date=new Date();
  public String stan="";

  public String TypeColData = "CC";//2+1=3

  public LedgerValue saleamount;//caller better make one...

  public String forSale(){
    return saleamount.Image();
  }

  /**
   * @return new FormattedLineItem from most recently set values
   */
  public FormattedLineItem formatted(LocalTimeFormat ltf) {
    StringBuffer fixedStuff=new StringBuffer(40);
    fixedStuff.append(ltf.format(date));
    fixedStuff.append(space);
    fixedStuff.append(Fstring.righted(stan,6,'0'));
    fixedStuff.append(space);
    fixedStuff.append(TypeColData);
    fixedStuff.append(card.Greeked(" "));
    return new FormattedLineItem(fixedStuff.toString(),forSale(),' ',FormattedLineItem.justified);
  }

}
//$Id: BatchLineItem.java,v 1.8 2001/07/07 03:31:49 andyh Exp $