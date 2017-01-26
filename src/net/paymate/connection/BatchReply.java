/**
 * Title:        reply with batch list content
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: BatchReply.java,v 1.25 2001/10/24 04:14:18 mattm Exp $
 */
package net.paymate.connection;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.ISO8583.data.*;
import java.util.*; // date and enumeration
import net.paymate.terminalClient.Receipt;

public class BatchReply extends AdminReply implements isEasy {

  public FormattedLines header=new FormattedLines(); // print both before and after the body (ie: this is the footer, too)
  public FormattedLines body  =new FormattedLines();
  int rowCount = 0;
  public final static String batchmoney="$#0.00;$-#0.00";
  LedgerValue saleTotal = new LedgerValue(batchmoney);
  TimeRange ranger;

  protected final static String bodyKey="body";
  protected final static String headerKey="header";
  protected final static String rangerKey="ranger";


  /**
   * @param newline one database record formatted as text
   * @return this
   */
  public BatchReply stuff(FormattedLineItem newline){//
    body.add(newline);
    return this;
  }

  public BatchReply stuffHeader(FormattedLineItem newline){//
    header.add(newline);
    return this;
  }

  public BatchReply error(String msg){
    stuffHeader(new FormattedLineItem(msg,"",'*',FormattedLineItem.justified));
    return this;
  }

  public ActionType Type(){
    return new ActionType(ActionType.batch);
  }

  public void save(EasyCursor ezp){
    ezp.setEasyCursor(bodyKey,body.asProperties());
    ezp.setEasyCursor(headerKey,header.asProperties());
    ranger.saveas(rangerKey,ezp);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    body.load(ezp.getEasyCursor(bodyKey));
    header.load(ezp.getEasyCursor(headerKey));
    ranger= TimeRange.NewFrom(rangerKey,ezp);
    super.load(ezp);
  }

  public boolean addItem(BatchLineItem bli) {
    rowCount++;
    saleTotal.add(bli.saleamount);
    stuff(bli.formatted(ranger.Formatter()));
    ranger.include(bli.date);
    insertCardSubtotals(bli); // add the card subtotals in ...
    return true;
  }

  EasyCursor cardTotals = new EasyCursor();
  private void insertCardSubtotals(BatchLineItem bli) {
    Accumulator acc = (Accumulator) cardTotals.get(bli.TypeColData);
    if(acc == null) {
      acc = new Accumulator();
      cardTotals.put(bli.TypeColData, acc);
    }
    acc.add(bli.saleamount.Value()); // cents
  }

  public void close(String terminalName, boolean isClosing, String clerkName) {
    stuffHeader(new FormattedLineItem(isClosing?"CLOSING ":"Printing ",terminalName));
    stuffHeader(new FormattedLineItem("Cashier:",clerkName));
    if(rowCount>0){//+_+ take from saleTotal
      stuffHeader(new FormattedLineItem("Starting:",ranger.one()));
      stuffHeader(new FormattedLineItem("Ending:",ranger.two()));
      // add the subtotals for card type
      for(Enumeration ennum = cardTotals.sorted(); ennum.hasMoreElements(); ) {
        String cardtype = (String)ennum.nextElement();
        Accumulator acc = (Accumulator) cardTotals.get(cardtype);
        stuffHeader(new FormattedLineItem(cardtype + " [" + acc.getCount() + "]: " , (new LedgerValue(batchmoney)).setto(acc.getTotal()).Image()));
      }
      stuffHeader(new FormattedLineItem("TOTAL [" + rowCount + "]: " ,saleTotal.Image()));
    } else {
      stuff(FormattedLineItem.winger("No items found for time range"));
      stuff(new FormattedLineItem(ranger.one(),ranger.two()));
    }
  }

  public static final BatchReply New(TimeRange given){
    BatchReply newone=new BatchReply();
    newone.ranger=TimeRange.copy(given);//copied so that our format is private
    return newone;
  }

  public static final BatchReply New(){
    BatchReply newone=new BatchReply();
    newone.ranger=TimeRange.Create(Receipt.Formatter());
    return newone;
  }
/**
 * @deprecated only ActionReply.fromProperties may use this!
 */
  public BatchReply(){
  //use New()
  }

}
//$Id: BatchReply.java,v 1.25 2001/10/24 04:14:18 mattm Exp $
