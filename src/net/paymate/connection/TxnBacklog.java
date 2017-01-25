package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/TxnBacklog.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TxnBacklog.java,v 1.17 2001/10/02 17:06:37 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.ISO8583.data.*;
import net.paymate.awtx.*;

import java.util.Vector;
import java.io.*;

public class TxnBacklog extends Backlog {
  private static final Tracer dbg=new Tracer(TxnBacklog.class.getName());
  /**
  * running total of stoodin transactions
  */
  private LedgerValue outstanding;

  public boolean totalOk(RealMoney limit){
    return outstanding.compareTo(limit)<0;
  }

  public String CentsOutstanding(){
    return outstanding.Image();
  }

  final static char fs='.';

/**
 * @return filename linked to embedded tid
 * voided flag goes lat to hide value from limit tester.
 */
  private String filename(StoodinRequest srek){
    return srek.reply.tid.image(fs) /*other stuff can go in here*/  +fs+srek.request.Amount().Value()+ (srek.voidme!=null?".VOIDED":"");
  }

  private RealMoney amtFromName(File f){
    String s=f.getPath();
    return new RealMoney(Safe.parseLong(Safe.subString(s,1+s.lastIndexOf(fs),s.length())));
  }

  Monitor deque=new Monitor("a TxnBacklog");
  public ActionRequest next(){
    try {
      deque.getMonitor();
      StoodinRequest srek=(StoodinRequest)super.next();
      outstanding.subtract(srek.request.Amount());
//      outstanding.subtract(srek.request.LedgerAmount());
      return srek;
    }
    catch(NullPointerException npe){
      return null; //this is normal, for our convenience
    }
    catch(ClassCastException cce){
      dbg.ERROR("Corrupt Txn file removed (no more info at this level...)");
      return null; //or a AlertRequest to go to server, when we have those.
    } finally {
      deque.freeMonitor();
    }
  }

  public boolean register(StoodinRequest srek){
    dbg.VERBOSE("storing request ");
    deque.getMonitor();
    try {
      ++srek.request.retry;
      if(super.register(srek,filename(srek))){
        dbg.VERBOSE("adding amount to total "+outstanding.Image());
        outstanding.add(srek.request.Amount());//refunds count against the total
//      outstanding.add(srek.request.LedgerAmount());//refunds reduce the total
        return true;
      }
      return false;
    }
    finally {
      deque.freeMonitor();
    }
  }

 /**
  * we can't (yet) void the transaction in progress, which often will be exactly the one we want to
  */
  void doLocalVoid(ReversalRequest frek,ReversalReply fry){
    int stan=frek.toBeReversed.stanValue();
    dbg.VERBOSE("stan to void:"+stan);
    deque.getMonitor(); //this will lock up reply thread
    try {
      StoodinRequest arq = null;
      while ((arq = (StoodinRequest) super.next()) != null) {
        dbg.VERBOSE("item stan:"+arq.reply.tid.stan());
        if(arq.reply.tid.stanValue()==stan){
          markDone(arq);//remove from disk
          fry.setApproval(arq.reply.Approval());
          fry.originalAmount=arq.request.Amount();
          if(arq.request instanceof CardRequest){
            fry.card=((CardRequest)arq.request).card;
          }
          Standin.succeed(fry);
          arq.voidme=fry; //don't need the assoicated request.
//don't try to record txn+void yet.          super.register(arq,filename(arq)); //not essential, can ignore failure here.
          return; //but we must remember to NOT put the reversal into storage.
        }
      }
      Standin.completeDecline(fry,"transaction not found offline","25");
    }
    catch (Exception ex) {
      dbg.ERROR("bad request in backlog");
      Standin.tryAgainLater(fry,"Backlog must be emptied first");
    }
    finally {
      deque.freeMonitor();
      init();
    }
  }



  public int init(){
    int size=super.init(); //load em up
    outstanding=new LedgerValue(BatchReply.batchmoney);
    for(int i=size;i-->0;){
      File f= (File) files.elementAt(i);
      outstanding.add(amtFromName(f));
    }
    return size;
  }

  public TxnBacklog(File root) {
    this(root, dbg);
  }

  public TxnBacklog(File root, ErrorLogStream newDbg) {
    super(new File(root,"txn"),newDbg);
  }

/**
 *  pseudoclone
 *  creates a new one with the same path, but because of different threads, contents may differ
 */

  public TxnBacklog clone(ErrorLogStream newDbg) {
    try {
      deque.getMonitor();
      return new TxnBacklog(root.getParentFile(), newDbg);
    }
    finally {
      deque.freeMonitor();
    }
  }

}
//$Id: TxnBacklog.java,v 1.17 2001/10/02 17:06:37 mattm Exp $
