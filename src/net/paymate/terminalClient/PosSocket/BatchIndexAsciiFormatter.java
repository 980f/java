package net.paymate.terminalClient.PosSocket;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/BatchIndexAsciiFormatter.java,v $
 * Description:  BatchIndex variation on asciiformatter.
 *               The purpose of this class is to preserve the "batch"
 *               so that we can use the data from it
 *               (mostly for testing and certifying)
 * Copyright:    Copyright (c) 2004
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.*;
import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.lang.*;

public class BatchIndexAsciiFormatter extends AsciiFormatter {
  static ErrorLogStream dbg;
  private static String eol="\r\n";

  public String formatId() {
    return "I1." + super.formatId();
  }

  // ONLY store PaymentRequest/PaymentReply pairs in here!
  private ActionList paymentlist = new ActionList(500); // 500 should be PLENTY!

  // empty the list
  private void clearBatchIndex() {
    dbg.VERBOSE("Clearing batch index.");
    paymentlist.clear();
  }

  /**
   * @param fields incoming command
   * @return if next field starts with a plus then fetch that numbered Action from history
   *     ... else restore state of iterator and return null.
   */
  Action findAction(TextListIterator fields) {
    if(fields.stillHas(1)) {
      String t = fields.next();
      if(StringX.firstChar(t) == '+') { //we take advantage of this choice of character in the parseInt routine below.
        Action a = paymentlist.getAction(~StringX.parseInt(t)); //parseInt was modified to tolerate '+'
        if(a != null) {
          return a;
        } //else rewind so that field is available as per normal formatter
      } //else rewind so that field is available as per normal formatter
      fields.rewind(1);
    } //else iterator is defective, let caller figure that out.
    return null;
  }

  // overloaded to use our batch indexing to find an approval code
  //this is only called when doing forces, which can have arbirarty preapprovals,
  //why bother doing  this stuff? =>some cert script wanted it this way.
  protected String preapproval(TextListIterator fields) {
    Action a = findAction(fields);
    if(a != null) {
      try {
        return( (PaymentReply) a.reply).auth().authcode();
      } catch(Exception ex) {
        dbg.ERROR("failed to find a stored preapproval to use in a force:" + ex);
        return "FAILed";
      }
    } else {
      return super.preapproval(fields); //already rewound when field not found
    }
  }

  private ActionRequest testCommand(TextListIterator fields){
    fields.next();//ditch keyword
    String command=fields.next();
    if(command.equalsIgnoreCase("sleep")){
      ThreadX.sleepFor( StringX.parseDouble(fields.next()));
      return Ack("Done Waiting");
    }

    if(command.equalsIgnoreCase("history")){
      command=fields.next();
      if(command.equalsIgnoreCase("list")){
        return BypassRequest.New(paymentlist.ActionHistoryReport(null).asParagraph()+csvend);
      }
      if(command.equalsIgnoreCase("clear")){
        paymentlist.clear();
        return Ack("history cleared");
      }
    }
    fields.rewind();
    return BypassRequest.New("Unknown tester command:"+fields.tail(", ")+eol);
  }

  // overload so that we can make an Action!
  protected ActionRequest finReq(TextListIterator fields) {
    String simcommand=fields.lookAhead();
    if(simcommand.equalsIgnoreCase("tester")){
      return testCommand(fields);
    }
    ActionRequest ar = super.finReq(fields); // pretend nothing happened!  :)
    if( (ar != null) && (ar instanceof PaymentRequest)) {
      dbg.VERBOSE("Adding action to the list:" + ar);
      Action.New(ar, paymentlist); // automatically adds it to the list
    }
    return ar;
  }

  // overloaded this function to pull stan from one in batch
  protected TxnReference getReference(TextListIterator fields) {
    Action action = findAction(fields);
    if(action != null) {
      if(action.reply != null) {
        PaymentRequest pr = (PaymentRequest) action.request;
        String stanner = pr.sale.stan.toString();
        dbg.VERBOSE("getReference() stanner = [" + stanner + "]");
        return TxnReference.New(parent.termInfo.id(), STAN.NewFrom(stanner));
      } else {
        dbg.ERROR("getReference() invalid reference");
        return TxnReference.New(parent.termInfo.id(), STAN.OnError()); //defective one!
      }
    } else {
      return super.getReference(fields);
    }
  }

  // here, we match the reply with the request; only for PaymentReply's
  protected void convertAuthResponse(PaymentReply reply, TextList hresponse) {
    super.convertAuthResponse(reply, hresponse);
//    if(reply.Succeeded() && reply.isApproved()){
    // apply the reply to the most recent action
    if(!paymentlist.applyReply(reply)) {
      dbg.ERROR("paymentlist: no action! coding error!");
    }
//    }
  }

  // check to see if a storeissuedeposit request was successful, and if so, clear the actions
  public byte[] replyFrom(Action response, boolean timedout) {
    if(!timedout && (response != null) && (response.reply != null)) {
      switch(response.reply.Type().Value()) {
        case ActionType.store: {
          if(response.reply.Succeeded()) {
            clearBatchIndex();
          } else {
          // don't clear yet
          }
        }
        break;
      }
    }
    // act like nothing happened :)
    return super.replyFrom(response, timedout);
  }

  public BatchIndexAsciiFormatter() { //public for polymorphic instantiation
    super();
    dbg = ErrorLogStream.getForClass(BatchIndexAsciiFormatter.class);
    dbg.VERBOSE("Instantiated:" + ReflectX.shortClassName(this));
    //set AsciiFormatter options:
    showListing = true; //false;
    showTotals = true; //false;
    csvend+=">";
  }

}
//$Id: BatchIndexAsciiFormatter.java,v 1.4 2004/03/10 00:36:35 andyh Exp $