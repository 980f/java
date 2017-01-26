package net.paymate.terminalClient.PosSocket;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/JumpwareFormatter.java,v $
 * Description:  fiona server (for jumpware).
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.66 $
 */
import net.paymate.terminalClient.*;
import net.paymate.lang.ReflectX;
import net.paymate.net.*;
import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.lang.Fstring;


public class JumpwareFormatter extends AsciiFormatter {
  static ErrorLogStream dbg;
  /**
   * @return protocol content revision
   * supposedly distinct across all formatters!
   */
  public String formatId() {
    return "J11." + super.formatId(); //manually updated for changes in protocol content
  }

  /** @ J7 use non-printable marker for eol instead of trusting \r or \n, which appear inside some content */
  static final String EOM = "\r\n\u0003";  //CR LF ETX
  /** per agreement with J-L always use 62 for communications errors. */
  static final String ErrorNumber ="62";
  static final String ErrorPrefix = "1,"+ErrorNumber+",";

  /**
   * (eventually) overloadable error messages
   */
  static String NotSupported= "Not supported by paymate [";
  static String NotUnderstood ="Not understood by paymate";
  static String ReportSuppressed="No report available, some transactions are incomplete. See paymate web site";
  static String CantSubmit=  "Cannot submit - can't get to paymate host" ;
  static String CantList= "No report available - can't get to paymate host";
  static String TimeoutErrorMessage=  "Paymate host not responding";
  static String DebitNotAllowed="Debit Cards Not allowed";
  static String UnknownCardIssuer="Card Not Recognized";
  static String UnknownResponse ="Paymate host gave unknown response:";
  static String EmptyReply="Paymate host gave empty reply";
  static String EmptyResponse="Paymate host gave empty response";
  static String HostError="Paymate host error,";

  /**
   * listing formatters
   */
  protected LocalTimeFormat mmddyy;
  protected LocalTimeFormat hhmmss;
  private LocalTimeFormat batchTime;
  private LedgerValue batchtotals;
  /**
       * the format strings in this function are Jumpware Defined, don't change them.
   */
  public Formatter setTimeFormat(String tz, String newformat) {
    dbg.VERBOSE("setTimeFormat:" + tz);
    mmddyy = LocalTimeFormat.New(tz, "MMddyy");
    hhmmss = LocalTimeFormat.New(tz, "HHmmss");
    //+_+ should have separate configurability for this, or add param to this function.
    batchtotals = LedgerValue.New(0);
    batchtotals.setFormat(" $####0.00;($####0.00)");
    batchTime = LocalTimeFormat.New(tz, newformat);
    return this;
  }

  /**
    */
   static String requestError(String reason) {
     return ErrorPrefix + reason + EOM;
   }

  private TextList startNak() {
    TextList oneline = new TextList();
    oneline.add(ErrorPrefix);
    return oneline;
  }

  private byte[] endNak(TextList oneline) {
    oneline.add(EOM);
    return oneline.asParagraph(" - ").getBytes();
  }

  /**
   * ackMessages are only used by non-jumpware operations.
   */
  protected byte[] ackMessage(String body) {
    return (ACK + "," + body + EOM).getBytes();
  }

  protected byte[] nakMessage(String body) {
    TextList oneline = startNak();
    oneline.add(body);
    oneline.add(formatId());
    return endNak(oneline);
  }

  protected byte[] nakAction(ActionRequest request, String reason) {
    TextList oneline = startNak();
    oneline.add(request.TypeInfo());
    oneline.add(reason);
    return endNak(oneline);
  }


  /**
   * @return actionRequest formatted from incoming bytes
   * returns null if no action required.
   */
  public ActionRequest requestFrom(byte[] line) {
    JumpwareRequest rq=new JumpwareRequest (parse(line));
    String firstword=rq.Field(1);//jumpware puts truncated chekc reference number here
    //if firstword is a known word of asciiFormatter .. write no wif it is text versus ascii number
    if( StringX.NonTrivial(firstword) && StringX.parseInt(firstword)==0 && ! firstword.equals("0")){//then AsciiFormatter protocol
      return super.requestFrom(TextListIterator.New(rq.fields));//signatures and such.
    } else {
      return rq.getRequest();//jumpware stuff
    }
  }

  private String filteredApproval(PaymentReply reply) {
    String pmerror = reply.Approval().trim(); //added .trim() to get rid of quotes on short authcodes.
    if (pmerror.equalsIgnoreCase(PaymentReply.ApprovalErrorText)) { //jumpware doesn't like text
      return "0";
    }
    else if (pmerror.equalsIgnoreCase("N/A")) { //jumpware doesn't like text
      return "0";
    }
    else {
      return pmerror;
    }
  }

  private String extractAuthMessage(AuthResponse auth) {
    if(auth != null) {
      if(auth.hasAVSResponse()) {
        return auth.message() + " AVS=" + auth.getAVS();
      } else {
        return auth.message();
      }
    } else {
      return "";
    }
  }

  private TextList beginReply(PaymentReply reply) {
    TextList forfiona = new TextList(13); //preallocate maximum fields
    dbg.VERBOSE("making a fiona approval");
    if (reply.Succeeded()) {//approved
      if (reply.isApproved()) {
        forfiona.add("0"); //approved=0, failed=1, declined=2.
        forfiona.add(filteredApproval(reply));
        forfiona.add(extractAuthMessage(reply.auth()));
      }
      else {//declined
        forfiona.add("2"); //approved=0, failed=1, declined=2.
        forfiona.add("05"); //generic declined for now.
        forfiona.add(reply.auth().message());
      }
    }
    else {//failed
      forfiona.add("1"); //approved=0, failed=1, declined=2.
      forfiona.add(ErrorNumber); //J6: majic number assigned to us by jumpware
      //+_+ Nick wants us to customize offline error messages for each transaction type.
//      if (reply.ComFailed()) {
        //if request is tips give a tips specific message.
//        forfiona.add("tips specific offline error message");
//      }
//      else {
        forfiona.add(reply.auth().message());
//      }
    }
    return forfiona;
  }

  /**
   * make a csv line from the reply fields and any cached local info.
   * jumpware fields 10,11,13 are set to zero rather than reflected
   */
  private String formatAuthResponse(PaymentRequest request, PaymentReply reply) {
    dbg.VERBOSE("authresponse:" + reply.auth());
    TextList forfiona = beginReply(reply);
    UTC timesplitter = request.requestInitiationTime;
    forfiona.add(datefrom(timesplitter));
    forfiona.add(timeofday(timesplitter));
    forfiona.add(trantype(request.sale.TransferType()));
    forfiona.add(entrymode(request.sale.wasSwiped()));
    forfiona.add(request.card.accountNumber.Image()); //we do NOT retain track data
    forfiona.add(request.Amount().Value());
    forfiona.add("0"); //second amount
    forfiona.add(0); //receipt number not supported
    forfiona.add(reply.refNum()); //stan fed from txnId in initial request
    forfiona.add("0"); //server number not supported

    return csvLine(forfiona);
  }

  /**
   * @return comma separated string from @param hresponse array of items-as-text,
   * deleting the text from @param hresponse so that it might be reused for another message
   */
  public static String csvLine(TextList hresponse) {
    try {
      return (hresponse.asParagraph(",") + CRLF);
    }
    finally {
      hresponse.clear();
    }
  }

  /**
   * translate from paymate type info to jumpware digit
   */
  private char trantype(TransferType type) {
    switch (type.Value()) {
      case TransferType.Sale:
        return '5';//+++ or '8'
      case TransferType.Return:
        return '0';//+++ or '9'
      case TransferType.Reversal:
        return '1';
      case TransferType.Force:
        return '2';
      case TransferType.Modify:
        return 'T';
      case TransferType.Authonly:

      default:
        return 'X';//can't happen
    }
  }

  /**
   * either manual or track 2, t1's are never sent to us.
   */
  private int entrymode(boolean manual) {
    return manual ? 0 : 2;
  }

  private String datefrom(UTC date) {
    return mmddyy.format(date);
  }

  private String timeofday(UTC date) {
    return hhmmss.format(date);
  }

  private boolean includeItem(BatchLineItem bli) {
    return bli.reallyApproved();//especially omit standin losses
  }
  /**
   * until the server gets an update, si-loss and si-pending are indistinguishable
   * by bli load(), so we make them both declines and reject declines here as
   * server is not supposed to send them.
   * this may need an update once server sends more info.
   */
  private boolean killWholeListing(BatchLineItem bli){
    return bli.isPending()||bli.isDeclined();//+++ there is now a 'stoodin' attribute to the bli, we should see if it works.
  }
  /**
   * example closing response, only the first line is sacred, the rest is only seen by humans.
   Settlement Response 7/15/98 11:50
   Sales     150  $ 10904.77
   Returns     1  $     7.95
   Total     151  $ 10895.82
   */
  private String subtote(String comment, Accumulator acc) {
    String name = Fstring.fill(comment, 10, ' ');
    String count = Fstring.righted(String.valueOf(acc.getCount()), 5, ' ');
    batchtotals.setto(acc.getTotal());
    String amount = batchtotals.Image();
    return name + " " + count + " " + amount;
  }

  private String formatBatchResponse(BatchRequest request, BatchReply reply) {
    if (reply.isOffline()) {
      return requestError( reply.isClosed ? CantSubmit : CantList);
    } else {
      if (reply.isClosed) {
        TextList response = new TextList(7);
        response.add("Settlement " + (reply.Succeeded() ? "R" : "r") + "esponse " + batchTime.format(reply.refTime()));
        response.add(reply.Succeeded() ? "Successfully Submitted" :  "Submittal Failed!");
        //no batch number or control number is available so we leave that line out.
        //we could consider passing back the drawerid.
        Accumulator total = reply.byInstitution.grand();
        if (total.getCount() > 0) {
          response.add(subtote("Total", total));
        }
        return response.asParagraph(CRLF);
      } else {
        //add overall approval here:
        StringBuffer listing = new StringBuffer();
        TextList hresponse = new TextList(20);
        for (int i = reply.numItems(); i-- > 0; ) {
          BatchLineItem bli = reply.item(i);
          if(killWholeListing(bli)){
            return requestError(ReportSuppressed);
          }
          if (includeItem(bli)) {//omit items that jumpware logic wants to void.
            hresponse.clear();
            hresponse.add(i);
            hresponse.add(""); //receipt # +++ merchref
            hresponse.add(bli.card.Image());
            hresponse.add(""); //expiration date unavailable
            hresponse.add(entrymode(bli.wasManual()));
            hresponse.add(bli.finalAmount().Value()); // settle amount instead of auth amount
            hresponse.add(trantype(bli.TransferType()));
            hresponse.add(datefrom(bli.date));
            hresponse.add(timeofday(bli.date));
            hresponse.add(bli.approval());
            hresponse.add(bli.stan());
            hresponse.add(""); //authorizer message
            hresponse.add(""); //shift
            hresponse.add(""); //server
            hresponse.add(""); //tip amount
            hresponse.add(""); //tip subtotoal
            hresponse.add(""); //tran subtotal
            listing.append(csvLine(hresponse));
          }
        }
        return String.valueOf(listing);
      }
    }
  }

  private String formatVoidReply(PaymentReply reply) {
    dbg.VERBOSE("voidresponse:" + reply.auth());
    TextList forfiona = beginReply(reply);
    UTC timesplitter = reply.refTime();
    forfiona.add(datefrom(timesplitter));
    forfiona.add(timeofday(timesplitter));
    forfiona.add(1); //original transfer type//wrong value here caused jumpware to orphan voids.
    forfiona.add(0); //orignal swipe vs manual
    forfiona.add(reply.card.accountNumber.Image()); //we do NOT retain track data
    forfiona.add(reply.originalAmount.Value()); //# cents required, not image
    forfiona.add("0"); //second amount
    forfiona.add("0"); //receipt number not supported
    forfiona.add(reply.refNum()); //stan fed from txnId in initial request
    forfiona.add("0"); //server number not supported
    return csvLine(forfiona);
  }

  public byte[] replyFrom(Action response, boolean timedout) {
    dbg.VERBOSE("got to replyFrom");
    String body = null;
    try {
      if (timedout) {
        body = requestError(TimeoutErrorMessage);
      } else {
        if (response != null) {
          dbg.VERBOSE("Responding to:" + response.TypeInfo());
          if (response.reply != null) {
            switch (response.reply.Type().Value()) {
              case ActionType.payment: {
                PaymentReply pr = (PaymentReply)response.reply;
                if(pr.transferType.is(TransferType.Reversal)) {
                  body = formatVoidReply( (PaymentReply) response.reply);
                } else {
                  body = formatAuthResponse((PaymentRequest) response.request,pr);
                }
              } break;
              case ActionType.batch: {
                body = formatBatchResponse( (BatchRequest) response.request,(BatchReply) response.reply);
              } break;
              case ActionType.receiptStore: {//non JW format response
                return ackMessage("Receipt Stored");
              } //break;
            }
            if(body == null) {
              body = requestError(UnknownResponse + response.TypeInfo());
            }
          } else {
            body = requestError(EmptyReply);
          }
        } else { //request that didn't need to go to paymate server
          body = requestError(EmptyResponse);
        }
      }
      if (body == null) {
        body = requestError(UnknownResponse + response.TypeInfo());
      }
      dbg.VERBOSE("returning:" + body);
      return body.concat(EOM).getBytes();
    }
    catch (Exception ex) {
      return nakMessage("Exception"+ex);
    }
  }

  public boolean onePerConnect() {
    return true; //close socket after each completed interaction
  }

  ////////////////////
  // construction.
  /*package*/ JumpwareFormatter() {
    if (dbg == null) {
      dbg = ErrorLogStream.getForClass(JumpwareFormatter.class);
    }
    dbg.VERBOSE("Just made a " + ReflectX.shortClassName(this));
  }

}
//$Id: JumpwareFormatter.java,v 1.66 2004/02/10 01:10:41 andyh Exp $
