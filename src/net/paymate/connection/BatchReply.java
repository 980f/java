/**
 * Title:        $Source: /cvs/src/net/paymate/connection/BatchReply.java,v $
 * Description:  reply with batch list content
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @version      $Revision: 1.59 $
 * @todo make read only access for what are presently public members.
 */
package net.paymate.connection;
import net.paymate.util.*;
import net.paymate.data.*;
import java.util.*; // date and enumeration
import net.paymate.terminalClient.Receipt;

/// for tester
import net.paymate.awtx.print.PrinterModel;
///

public class BatchReply extends AdminReply implements isEasy {
  static ErrorLogStream dbg;
  public boolean isClosed;
  public boolean submitted;


  private TimeRange ranger=null;//constructor finds nontrivial one
  public TimeRange ranger(){
    if(ranger==null){
      UTC now=UTC.Now();
      ranger=TimeRange.Create();
      ranger.setBoth(now,now);
    }
    return ranger;
  }

  public SubTotaller byInstitution=new SubTotaller();
  private SubTotaller byTtype=new SubTotaller();

  public TerminalInfo tinfo;

  private Vector items=new Vector();
  public int numItems(){
    return VectorX.size(items);
  }
  public BatchLineItem item(int i){
    return (BatchLineItem)items.elementAt(i);
  }

  public void clearDetail() {
    items.clear();
  }

  public ActionType Type(){
    return new ActionType(ActionType.batch);
  }

  private final static String rangerKey="ranger";
  private final static String itemsKey= "item";
  private final static String totalsKey="totals";
  private final static String closedKey="closed";
  private final static String tinfoKey="terminal";

  public void save(EasyCursor ezp){
    ezp.setObject(rangerKey,ranger);
    ezp.saveVector(itemsKey,items);
    ezp.setBoolean(closedKey,isClosed);
    ezp.setObject(totalsKey,byInstitution);
    ezp.setObject(tinfoKey,tinfo);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    ranger= TimeRange.NewFrom(rangerKey,ezp);
    items=ezp.loadVector(itemsKey,BatchLineItem.class);
    isClosed=ezp.getBoolean(closedKey);
    byInstitution=(SubTotaller)ezp.getObject(totalsKey,SubTotaller.class);//java needs templates! or implicit casts!
    tinfo=(TerminalInfo)ezp.getObject(tinfoKey,TerminalInfo.class);
  }

  public boolean addItem(BatchLineItem bli) {
    dbg.Enter("addItem");
    try {
      items.add(bli);
      dbg.VERBOSE("subtote:"+bli.TypeColData);
      byInstitution.add(bli.TypeColData,bli.finalNetAmount().Value());//signed value for institution
      byTtype.add( bli.isReturn()?"RT":"SA", bli.finalAmount().Value());//absolute for transtype.
      ranger.include(bli.date);
      return true;
    }
    catch (Exception ex) {
      dbg.Caught(ex);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  public SubTotaller byTransactionType(){
    if(byTtype==null){
      byTtype=new SubTotaller();
    }
    if(byTtype.Count()<=0){//bridge old server to new client
      for(int i=numItems();i-->0;){
        BatchLineItem bli=item(i);
        byTtype.add( bli.isReturn()?"RT":"SA", bli.finalAmount().Value());//absolute amount
      }
    }
    return byTtype;
  }
////////////////
  public BatchReply set(TerminalInfo tinfo){
    this.tinfo=tinfo;
    return this;
  }

/////////////////
  public static final BatchReply New(TimeRange given){
    BatchReply newone=new BatchReply();
    newone.ranger=TimeRange.copy(given);//copied for good luck.
    return newone;
  }

  public static final BatchReply New(TerminalInfo tinfo){
    BatchReply newone=new BatchReply();
    newone.ranger=TimeRange.Create();
    return newone;
  }


  public static final BatchReply New(){
    BatchReply newone=new BatchReply();
    newone.ranger=TimeRange.Create();
    return newone;
  }

/**
 * @unwise only ActionReply.fromProperties may use this!
 */
  public BatchReply(){//public for instantiator.
  //use New()
    if(dbg==null) dbg=ErrorLogStream.getForClass(BatchReply.class);
  }

  /**
   *  storage tester
   */
  static public void main(String[] args) {
    BatchReply tosave= BatchReply.New();
    tosave.ranger=TimeRange.Create();
    tosave.ranger.setStart(UTC.Now()).setEnd(UTC.Now());
    tosave.set(TerminalInfo.fake());
    for(int i=2;i-->0;){
      tosave.addItem(BatchLineItem.FakeOne(i));
    }
    PrinterModel dump=PrinterModel.BugPrinter(100);

    EasyCursor image= EasyCursor.makeFrom(tosave);
    dump.println(image.asParagraph(OS.EOL));

    BatchReply toload= BatchReply.New();
    toload.load(image);

    EasyCursor image2= EasyCursor.makeFrom(toload);
    dump.println(image2.asParagraph(OS.EOL));

  }

}
//$Id: BatchReply.java,v 1.59 2003/10/25 20:34:17 mattm Exp $
