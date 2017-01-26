package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/BatchListingFormatter.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.20 $
 */

import net.paymate.util.*;
import net.paymate.connection.*;

public class BatchListingFormatter /*implements isEasy*/ {
  static ErrorLogStream dbg;

  public FormattedLines header=new FormattedLines(); // print both before and after the body (ie: this is the footer, too)
  public FormattedLines body  =new FormattedLines();
  int rowCount = 0;
  public final static String batchmoney="$#0.00;$-#0.00";

  LocalTimeFormat ltf;

  private final static String bodyKey="body";
  private final static String headerKey="header";

    /**
   * @param newline one database record formatted as text
   * @return this
   */
  private void stuff(FormattedLineItem newline){//
    if(newline!=null){
      body.add(newline);
    }
  }

  public void stuffHeader(FormattedLineItem newline){//
    if(newline!=null){
      header.add(newline);
    }
  }

  public void error(String msg){
    stuffHeader(FormattedLineItem.winger(msg));
  }

  public void stuffErrors(TextList list){
    if(list.size()>0){
      stuffHeader(FormattedLineItem.blankline());
      FormattedLineItem section=FormattedLineItem.winger("ERRORS/WARNINGS");
      stuffHeader(section);
      for(int i=0;i<list.size();i++){//#maintain order
        error(list.itemAt(i));
      }
      stuffHeader(section);
      stuffHeader(FormattedLineItem.blankline());
    }
  }

  private LedgerValue moneyFormatter=new LedgerValue(batchmoney);//will reduce to moneyFormatter when we have one.

  private FormattedLineItem subtote(String cardtype, Accumulator acc) {
    return FormattedLineItem.pair(cardtype + " [" + acc.getCount() + "]: " , moneyFormatter.setto(acc.getTotal()).Image());
  }

  String localtime(UTC utc){
    return ltf.format(utc);
  }

  /**
   * @todo: [coded] deal with trivial and singular TimeRange.
   */
  protected FormattedLineItem timeline(TimeRange ranger){
    if(TimeRange.NonTrivial(ranger)){
      if(ranger.singular()){// +++ simplify using new ObjectRange.end() functionality
        String both=localtime(ranger.start());
        return FormattedLineItem.pair(both,both);
      } else {
        return FormattedLineItem.pair(localtime(ranger.start()), localtime(ranger.end()));
      }
    } else {
      return null;
    }
  }

  public void format(BatchReply reply){
    if(moneyFormatter==null){
      dbg.ERROR("moneyformatter was null");
      moneyFormatter=new LedgerValue(batchmoney);
    }

    if(ltf == null){
      dbg.ERROR("time formatter was null");
      ltf=LocalTimeFormat.Utc();
    }

    for(int i=0;i<reply.numItems();i++){//#puts most recent at top for when listing is requested for checking last txn
      BatchLineItem bli= reply.item(i);
      if(bli!=null){
        stuff(bli.formatted(ltf,moneyFormatter));
      } else {
        dbg.ERROR("item "+i+" of reply was null");
      }
    }

    int itemcount=reply.byInstitution.Count();
    stuffErrors(reply.Errors);
    stuffHeader(FormattedLineItem.pair(reply.isClosed?"CLOSING":"Open items",reply.tinfo.getNickName()));
    stuffHeader(timeline(reply.ranger()));

    if(itemcount>0){
      // add the subtotals for card type
      TextList totals=reply.byInstitution.subtotalNames();
      totals.sort();
      for(int i=totals.size();i-->0;) {
        String cardtype = totals.itemAt(i);
        Accumulator acc = reply.byInstitution.getAccumulator(cardtype);
        stuffHeader(subtote(cardtype,acc));
      }
      stuffHeader(subtote("TOTAL",reply.byInstitution.grand()));
    } else {
      stuff(FormattedLineItem.winger("There are no open items"));
    }
  }

  public static BatchListingFormatter Create(BatchReply reply,LocalTimeFormat ltf){
    BatchListingFormatter newone=New(ltf);
    newone.format(reply);
    return newone;
  }

  public static final BatchListingFormatter New(LocalTimeFormat ltf){
    BatchListingFormatter newone=new BatchListingFormatter();
    newone.ltf=ltf;//an ltf's format is not supposed to change after creation, so we can refer to the given one instead of copying.
    if(newone.ltf==null){
      // this next line will not work anymore +_+
      // newone.ltf=net.paymate.terminalClient.Receipt.Formatter();//only invoked if server config for terminal is bad. Don't realy care if this makes sense.
      newone.ltf=LocalTimeFormat.Utc(); // this will be UTC/GMT
    }
    return newone;
  }

  private BatchListingFormatter() {
    if(dbg==null) dbg=ErrorLogStream.getForClass(BatchListingFormatter.class);
  }

}
//$Id: BatchListingFormatter.java,v 1.20 2003/10/25 20:34:19 mattm Exp $