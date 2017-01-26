package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/TxnBacklog.java,v $
 * Description:  client's standin logic.
 * Copyright:    Copyright (c) 2001-2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TxnBacklog.java,v 1.43 2004/02/03 09:00:47 mattm Exp $
 * @todo: on server, deal with voided transactions by first seeing if the original attempt succeeded, THEN void or refund as required.
 */

import net.paymate.util.*;
import net.paymate.data.*; // id's, stan
import net.paymate.awtx.*;
import net.paymate.lang.StringX;
import java.util.Vector;
import java.io.*;

public class TxnBacklog extends Backlog {
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(TxnBacklog.class);
  private static final boolean serverHandlesVoids=false;

  /// split txn backlog into two, super is one modlog is the other.
  private Backlog modlog;//for ensuring all auths precede modifies

  public boolean isEmpty(){
     return super.isEmpty() && modlog.isEmpty();
   }

   public int size(){
     return super.size() + modlog.size();
   }

  ///

  /**
   * running total of stoodin transactions
   */
  private LedgerValue outstanding;

  public boolean totalOk(RealMoney limit){
    return outstanding.compareTo(limit)<0;
  }
  public LedgerValue total(){
    return outstanding;
  }

  public String CentsOutstanding(){
    return outstanding.Image();
  }

  final static char fs='.';

  /**
   * @return filename linked to embedded tid
   * voided flag goes last to hide value from limit tester.
   */
  private String filename(StoodinRequest srek){
    return srek.filename()+ (srek.voidme!=null?".VOIDED":"");
  }

  private RealMoney amtFromName(File f){
    String s=f.getPath();
    return new RealMoney(StringX.parseLong(StringX.subString(s,1+s.lastIndexOf(fs),s.length())));
  }

  Monitor deque=new Monitor("a TxnBacklog");
  public ActionRequest next(){
    try {
      deque.getMonitor();
      return (StoodinRequest) (super.isEmpty()? modlog.next():super.next());
//      StoodinRequest srek=(StoodinRequest)super.next();
//      return srek;
    }
    catch(NullPointerException npe){
      return null; //this is normal
    }
    catch(ClassCastException cce){
      dbg.ERROR("Corrupt Txn entry removed (no more info at this level...)");
      return null; //UDP panic , when we have those.
    } finally {
      deque.freeMonitor();
    }
  }

  public void onDone(StoodinRequest request) {
    decTotal(request);
  }

  private boolean countsTowardsTotal(StoodinRequest srek){
    return srek.reply.isApproved()&&srek.voidme==null;
  }

  private void incTotal(StoodinRequest srek){
    if(countsTowardsTotal(srek)){
      dbg.VERBOSE("adding amount to total "+outstanding.Image());
      outstanding.add(srek.amount());//refunds count against the total
    } else {
      dbg.VERBOSE("not including item in total");
    }
  }

  private void decTotal(StoodinRequest srek){
    if(srek.reply.isApproved()){
      dbg.VERBOSE("removing amount from total "+outstanding.Image());
      outstanding.subtract(srek.amount());//refunds count against the total
    } else {
      dbg.VERBOSE("item not included in total");
    }
  }

  public boolean register(StoodinRequest srek){
    dbg.VERBOSE("storing request ");
    deque.getMonitor();
    try {
      ++srek.request.retry; //which usually sets it to one, unless was already on disk
      if(srek.isModifier()) {//modifies do not affect total.
        return modlog.register(srek, filename(srek));
      } else {
        if(super.register(srek, filename(srek))) {
          incTotal(srek);
          return true;
        }
      }
      return false;
    }
    finally {
      deque.freeMonitor();
    }
  }

  /**
   * we can't (yet) void the transaction in progress, which often will be exactly the one we want to
   * @param frek request for reversal
   * @param voider, to be filled in by this guy
   * @return whether completed ok, not to be confused with voiding something
   */
  boolean doLocalVoid(PaymentRequest frek,PaymentReply voider){
    TxnReference original=frek.original;//fue
    dbg.VERBOSE("stan to void:"+original.toSpam());
    deque.getMonitor(); //this will lock up standin processing
    try {
      StoodinRequest arq = null;
      //the following boolean expression only inspect txn's and does NOT remove any from disk
      while ((arq = (StoodinRequest) super.next()) != null) {
        dbg.VERBOSE("item stan:"+arq.reply.tref().toSpam());
        if(arq.reply.tref().sameStan(original.httn)){
          //we don't check voidability here as at present only voidable things get stood-in.+_+
          if(countsTowardsTotal(arq)){
            voider.setApproval(arq.reply.Approval());//approving a void of a standin
            voider.originalAmount=arq.amount();
            if(arq.request .hasSomeCardInfo()){
              voider.card=arq.request.card;
            }
            Standin.succeed(voider);
            arq.voidme=voider; //don't need the assoicated request.
            markDone(arq);//remove from disk.
            onDone(arq);  //remove from total
            if (serverHandlesVoids) {
              return register(arq);  //have to store the modified request
              //a failuire in the register() above is of little consequence as the transaction
              //lost is being voided. if the original got approved on server then
              //the void might get missed giving us an unvoided transaction.
            } else {
              return true;
            }
          }
          break;
        }
      }
      Standin.completeDecline(voider,"offline, try again later");
      return true;
    }
    catch (Exception ex) {
      dbg.ERROR("bad request in backlog");
      Standin.tryAgainLater(voider,"error, try again later");
      return false;
    }
    finally {
      deque.freeMonitor();
      init();//re read all from disk here, to simplify coding above.
    }
  }

  boolean isModifiable(StoodinRequest arq){
    return arq.request().isModifiable();
  }

  private void succeedModify(PaymentReply frep,String appcode,RealMoney origamount){
    frep.setApproval(appcode);//???gratuitous
    frep.originalAmount=origamount;//???gratuitous
    Standin.succeed(frep);//we have stoodin the modify
  }

  /**
   * @param frek modification request
   * @param frep reply to send if this function returns true
   * @return whether to standin the modify by itself.
   */
  boolean tryLocalModify(PaymentRequest frek,PaymentReply frep){
    TxnReference original=frek.original;//fue
    dbg.VERBOSE("stan to modify:"+original.toSpam());
    deque.getMonitor(); //this will lock up standin processing
    try {
      StoodinRequest arq = null;
      while ((arq = (StoodinRequest) super.next()) != null) {//note: removes from list but not disk OR total.
        dbg.VERBOSE("item stan:"+arq.reply.tref().toSpam());
        if(arq.reply.tref().sameStan(original.httn)){//then txn was found
          if(isModifiable(arq)){//type of transaction is modifiable
            if(arq.isVoided()){
              Standin.completeDecline(frep,"original voided");
              //return false;//we aren't (yet) sending declined modifies to server.
            } else {
              //caller is supposed to have checked all aspects of the modify that don't depend upon the original
              succeedModify(frep,arq.reply.Approval(),arq.request.Amount());//???gratuitous
              return true;//passes extra tests
//we decided to not couple them at file level. in case we change our minds again:
//              arq.modifier=frek;//we only need to hold onto the request long enough to (re)register the standin
//              markDone(arq);//remove original from disk.
//              onDone(arq);  //remove from total
//              register(arq); //now add modified one back into system
              //end of re-writing original record with modifier record attached.
            }
          } else {
            Standin.completeDecline(frep,"original not modifiable");
          }
          return false;//we have attached this into an existing record, we are done
        } else {
        //keep on looking for more
        }
      }
      //we get here if the original is not in local storage, but all other rules say it is ok to standin.
      succeedModify(frep,AuthResponse.DEFAULTAUTHCODE,RealMoney.Zero());//???gratuitous
      return true;//couldn't do extra tests, hope the user knows what they are doing.
    }
    catch (Exception ex) {
      dbg.ERROR("bad request in backlog");
      Standin.tryAgainLater(frep,"error, try again later");
      return false;
    }
    finally {
      deque.freeMonitor();
      init();//re read all from disk here, to simplify coding above.
    }
  }

  /**
   * this doesn't need to lock the list as it is only (supposed to be) called from the constructor
   */
  public int init(){
    int size=super.init(); //load em up
    outstanding=new LedgerValue();
    for(int i=size;i-->0;){
      ActionRequest srek=loadFile((File) files.elementAt(i));
      if(srek instanceof StoodinRequest ){
        incTotal((StoodinRequest )srek);
      } else {
        //UDPanic??? what else do we ever store on disk???
      }
    }
  //initialization loop   size += modlog.init();
  //return value isn't used for anything important, doesn't need to include the modifies.
    return size;
  }

  public TxnBacklog(File root) {
    this(root, dbg);
  }

  public TxnBacklog(File root, ErrorLogStream newDbg) {
    super(new File(root,"txn"),newDbg);
    modlog=new Backlog(new File(root,"mod"),newDbg);
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
//$Id: TxnBacklog.java,v 1.43 2004/02/03 09:00:47 mattm Exp $