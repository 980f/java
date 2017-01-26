package net.paymate.terminalClient.PosSocket.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/PaymentechUTFormatter.java,v $
 * Description:  mate paymentech messaging to paymate terminal devices
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 * @todo: retest batch reply in standing for gateway, and online when non-gateway.
 */

import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.lang.StringX;
import net.paymate.util.*;
import net.paymate.connection.*;
import net.paymate.terminalClient.PosSocket.*;
import net.paymate.terminalClient.DrawerMenu;
import net.paymate.lang.ReflectX;
import java.util.Enumeration;
import net.paymate.lang.Fstring;

public class PaymentechUTFormatter extends Formatter {
  protected ErrorLogStream dbg;

  public String formatId(){
    return "P0";
  }
/**
 * these guys are set at construction/configuration time,
 * it is irrational to think that they would change once usage has begun.
 */
//  protected boolean gateway;    //true if running in gateway mode
  protected boolean hostcapture;//true if terminal is using hostcapture protocol
  protected final String batchTimeFormat="MMddyyHHmm";
  LocalTimeFormat barf;

  /**
 * these guys are needed for standin.
 * if TC then we record them from the terminal's requests
 * if HC then we yank them out of our ass. No really, we make a vain attempt to put something reasonable in them.
 */
  protected int seqnum; //purely reflected from request to reply
  protected int lastrrn; //hopefully the correct reference number for a reply
  protected int currentBatch; //6 digits, typically 3 julian date + 3 counter

  ////////////////////////////////
  // request processing components
  ////////////////////////////////


  /**
   * @return paymate request from info from paymentech request
   */
  private PaymentRequest gcFrom(SaleInfo wad,MSRData card){
    return PaymentRequest.GiftCardRequest (wad,card);
  }

  /**
   * @return paymate request from info from paymentech request
   */
  private PaymentRequest ccFrom(SaleInfo wad,MSRData card){
    return PaymentRequest.CreditRequest(wad,card);
  }

  /**
   * @return paymate request from info from paymentech request
   */
  private PaymentRequest dbFrom(SaleInfo wad,MSRData card,PINData pin){
    return PaymentRequest.DebitRequest (wad,card,pin);
  }

  /**
   * @return paymate request from info from paymentech request
   */
  private  ActionRequest batchRequestFrom(UTFrequest utf){
    return BatchRequest.JustClose(false);
  }

  /**
   * @return paymate request from info from paymentech request
   */
  protected ActionRequest requestFrom(UTFrequest utf){
    ActionRequest newone;
    SaleInfo wad= utf.SaleInfo();
    if(wad.type.op.is(TransferType.Reversal)){
      TxnReference tref=TxnReference.New(/* +++ */ new Terminalid(), STAN.NewFrom(utf.origRefNumber));
      newone= PaymentRequest.Void(tref);//pm doesn't distinguish payment type when reversing.
    } else {
      switch (wad.type.payby.Value()) {
        case PayType.GiftCard:{
          newone= gcFrom(wad,utf.card);
        } break;
        case PayType.Credit:{
          newone= ccFrom(wad,utf.card);
        } break;
        case PayType.Debit:{
         newone= dbFrom(wad,utf.card,utf.pin);
        } break;
        default:{
         newone= BypassRequest.New(errorReply(utf.stan,218,"NYI "+wad.type.payby.Image()));
        } break;
      }
    }
    newone.requestInitiationTime= UTC.Now();//fixes ptgwsi "null" txn filenames.
    return newone;
  }


  /**
   * @return paymate request from info from paymentech request
   * @param line is a full request with stx etc.
   */
  public ActionRequest requestFrom(byte[] line){//Formatter interface
    ActionRequest ar=null;
    UTFrequest utf= UTFrequest.From(VisaBuffer.FromBytes(line));
    ++lastrrn;
    seqnum= hostcapture? lastrrn: utf.stan; //always 0 on HC so make a guess
    switch(utf.trancode.code()){
      case PTTransactionCodes.QueryBatchUpload:
      case PTTransactionCodes.BatchUploadHeader:
      case PTTransactionCodes.BatchUploadTrailer: {
        ar= BypassRequest.New(errorReply(utf.stan,666,"NYI trancode "+utf.trancode));
      } break;
      case PTTransactionCodes.BatchRelease:{
        ar=batchRequestFrom(utf);
      } break;
      default:{//better be financial
        ar= requestFrom(utf);
      } break;
    }
    ar.setOrigMessage(line);
    return ar;
  }

  ////////////////////////////////
  // reply processing components
  ////////////////////////////////
  protected void nextBatch(){
    lastrrn=0; //preinc'd before each use
    ++currentBatch;//+_+ can get fancy here and try to detect date rollover.
  }
  /**
   * need to look at request to know how to parse response
   * store host sourced information to simulate the host in standin.
   */
  protected void captureSequenceInfo(byte [] ptrequest,byte [] ptresponse){
    boolean isAuth;
    UTFrequest rq= UTFrequest.From(ptrequest);
    if(rq.trancode.isBatchy()){
      //parse response as batch response
      dbg.WARNING("CSI:fool with batch counters on batch responses");
      nextBatch();
    } else {
      //parse response as auth response
      PaymentechResponse resp=PaymentechResponse.From(ptresponse, hostcapture);
      lastrrn=resp.ptrrn; //NOT sequence number, from watching the live authorizer log.
      if(currentBatch != resp.batchNumber){
        if(hostcapture){
          //consider closing the drawer +++ un fortunately this is not a place where such can be invoked.
          //we can probably post an object onto the terminal that owns us...
          parent.Post(new DrawerMenu(DrawerMenu.Close_w_Totals));
        }
        currentBatch=resp.batchNumber;
      }

      dbg.WARNING("CSI:rrn  and batch:"+lastrrn+", "+currentBatch);
    }
  }

  /**
   * @return framed message given body of message from host
   */
  protected byte[] rawReplyFrom(ActionReply ar){
    dbg.ERROR("returning gateway reply");
    return VisaBuffer.FrameThis(ar.origMessage()).packet();
  }

  /**
   * @return framed paymentech response given logical equivalent
   */
  private byte [] cvt2byte(PaymentechResponse synth){
    return synth.pack().packet();
  }

  private String twolinemessage(String detail){
    TextList wrapper= new TextList(2);
    wrapper.split("PM "+detail,16,wrapper.SMARTWRAP_ON);
    dbg.VERBOSE( wrapper.asParagraph(OS.EOL));
    return Fstring.fill(wrapper.itemAt(0),16,' ')+ Fstring.righted(wrapper.itemAt(1),16,' ');
  }

  /**
   * @param seqnum usually comes from the request for which we are generating an error
   * @ercode is a 3 digit number, fit into a 6 alpha field.
   */
  public byte [] errorReply(int refnum,int ercode,String detail){
    PaymentechResponse synth=new PaymentechResponse(true);
    synth.setTrio("E",String.valueOf(ercode),twolinemessage(detail));
// +_+ not sure what we should be doing here
    synth.seqnum=seqnum;
    synth.ptrrn=refnum;
    synth.batchNumber=currentBatch;
    return cvt2byte(synth);
  }

  /**
   * @param seqnum usually comes from the request for which we are generating an error
   * @ercode is a 3 digit number, fit into a 6 alpha field.
   */
  protected byte [] authedReply(boolean approved,int refnum,int seqnum,String authcode,String detail){
    dbg.WARNING("authing: (arscd)"+approved+", "+refnum+", "+seqnum+", "+authcode+", "+detail);
    PaymentechResponse synth=new PaymentechResponse(true);
    //on server standin inject APPRVD
    synth.setTrio(approved?"A":"E",StringX.OnTrivial(authcode,AuthResponse.DEFAULTAUTHCODE),twolinemessage(detail));
    synth.seqnum=seqnum;
    synth.ptrrn=refnum;
    synth.batchNumber=currentBatch;
    return cvt2byte(synth);
  }

  protected byte [] authedReply(PaymentRequest  freq,PaymentReply frep){
    return authedReply(
    frep.isApproved(),
    lastrrn,  //sometimes this is a guess from standin. most often it is from host
    seqnum,  //in TC mode this counts up with auth attempts, in HC Mode it is zero.
    frep.Approval(),
    frep.authMessage()
    );
  }

  /**
   * @return wrapped message given paymate generated response
   */
  public byte[] replyFrom(Action response,boolean timedout){
    dbg.Enter("ReplyFrom");
    boolean isFinancial=Action.isFinancial(response);
    try {
      if(timedout || response == null){
        dbg.WARNING("host timedout");
        return isFinancial? errorReply(0,292,"Host Down"): Formatter.NullResponse;
      }
      if(isFinancial){
        PaymentReply frep= (PaymentReply) response.reply;
        PaymentRequest  freq= (PaymentRequest ) response.request;
        if(frep.Succeeded()){
          dbg.WARNING("auth completed");
          if(gateway && frep.isGatewayMessage()){//gateway
            return rawReplyFrom(response.reply);
          } else {
            dbg.ERROR("making synthetic reply");
            return authedReply(freq,frep);
          }
        } else {
          dbg.WARNING("auth failed");
          String msg=StringX.OnTrivial(frep.authMessage(),frep.status.Image());
          return errorReply(0,297,msg);
        }
      }
      else if (response.reply instanceof BatchReply) {
        dbg.WARNING("synthetic batch response");
        if(true){
          nextBatch();
          return batchresponse((BatchReply) (response.reply)).packet();
        } else {//this was a secondary attempt to get a hangup for gateway mode
          return Formatter.NullResponse;//should trigger a hangup.
        }
      }
      else {
        dbg.WARNING("unknown response type");
        if(response!=null){
          if(response.request!=null){
            dbg.ERROR("the request:"+response.request.toEasyCursorString());
          } else {
            dbg.ERROR("null request");
          }
          if (response.reply!=null) {
            dbg.ERROR("the reply:"+response.reply.toEasyCursorString());
          } else {
            dbg.ERROR("null reply");
          }
        } else {
          dbg.ERROR("null action");
        }
        return errorReply(0,218,"Unknown Host Response");
      }
    }
    finally {
      dbg.Exit();
    }
  }

  public boolean onePerConnect(){
    return false; //legacy setting, how would we know to disconnect between each interaction?
  }


  /**
   * response when replyFrom allows an exception to throw
   */
  public byte[] onException(Exception any){
    dbg.Caught(any);
    return errorReply(0,666,ReflectX.justClassName(any));
  }

  private void insertRange(VisaBuffer vb,TimeRange trange){
    if(barf==null){ //+_+ find where parent is set and move the line below there.
      barf= LocalTimeFormat.New(parent.termInfo.si.timeZoneName,batchTimeFormat);
    }
    int btflen=batchTimeFormat.length();
   //using the fixed length below in case we blow the time formatting, will preserve fixed spacing
    if(trange!=null){
      if(trange.broad()){// +++ simplify using new ObjectRange.end() functionality
        vb.appendAlpha(btflen,barf.format(trange.start()));   //12 Batch Open Date/Time Pic 9(10) Ö MMDDYYHHMM
        vb.appendAlpha(btflen,barf.format(trange.end()));   //13 Batch Close Date/Time Pic 9(10) Ö MMDDYYHHMM
      } else if(trange.singular()) {// +++ simplify using new ObjectRange.end() functionality
        vb.appendAlpha(btflen,barf.format(trange.start()));   //12 Batch Open Date/Time Pic 9(10) Ö MMDDYYHHMM
        vb.appendAlpha(btflen,barf.format(trange.start()));   //13 Batch Close Date/Time Pic 9(10) Ö MMDDYYHHMM
      } else {
        vb.appendAlpha(btflen,barf.format(UTC.Now()));   //12 Batch Open Date/Time Pic 9(10) Ö MMDDYYHHMM
        vb.appendAlpha(btflen,barf.format(UTC.Now()));   //13 Batch Close Date/Time Pic 9(10) Ö MMDDYYHHMM
      }
    } else {
      vb.appendAlpha(btflen,barf.format(UTC.Now()));   //12 Batch Open Date/Time Pic 9(10) Ö MMDDYYHHMM
      vb.appendAlpha(btflen,barf.format(UTC.Now()));   //13 Batch Close Date/Time Pic 9(10) Ö MMDDYYHHMM
    }
  }


  /**
   * @return paymentech batch response given paymate batch response
   */
  private VisaBuffer batchresponse(BatchReply brep){
    dbg.VERBOSE("synthing batch reply");
    VisaBuffer vb = VisaBuffer.NewSender(666);
    vb.append("A"); //only approved ones call this function. //2 Action Code Pic X(1) Ö A = Approved (requested trains was successful)
    vb.append(" ");   //3 Pic X(1) Ö This field will be return as a space.
    vb.append("      ");//4 Authorization/Error Code Pic X(6) Ö This field will be returned as all spaces.
    vb.appendInt(6,currentBatch);//5 Batch Number Pic 9(6) Ö Current Open Batch number.
    vb.appendInt(8,0);//6 Retrieval Reference Number Pic 9(8) Ö The number returned in this field represents how manytimes a batch inquiry has been requested from the host.
    vb.appendInt(6,seqnum); //7 Sequence Number Pic 9(6) Ö This field is echoed from the transaction.
  // below: terminal looks for 32 chars before it checks for the endofframe!
    vb.appendAlpha(32,"Submitted"); //8 Response Message Pic X(32) Ö Approval/Decline/Error text message information //9 FS Pic X(1) Ö 1Ch
    vb.endFrame();
    vb.append("0"); //10 Download Flag Pic X(1) Ö 0 = None
    vb.append("N");//11 Multi Message Flag Pic X(1) Ö N = Last Message
    insertRange(vb,brep.ranger());
    vb.appendInt(6,(int)brep.byInstitution.grand().getCount()); //14 Batch Transaction Count Pic 9(6) Ö Total number of transactions in batch
//following may need to be byTtype's total
    vb.appendFrameWithDecimalPoint(brep.byInstitution.grand().getTotal(),4,2);//15 Batch Net Amount Pic X(11) Ö Variable, “-9999999.99” to “9999999.99”//16 FS Pic X(1) Ö 1Ch
    vb.endFrame();  //17 Working Key Pic X(16) Ö M/S Working Key //18 FS Pic X(1) Ö 1Ch

    TextList totals=brep.byInstitution.subtotalNames();
    totals.sort();
    for(int i=totals.size();i-->0;) {
      String cardtype = totals.itemAt(i);
      Accumulator acc = brep.byInstitution.getAccumulator(cardtype);
      vb.appendAlpha(3,cardtype);
      vb.appendInt(6,(int)acc.getCount());
      vb.appendFrameWithDecimalPoint(acc.getTotal(),4,2);//need to check that refunds are negatives here.
    }
    vb.end();
    return vb;
  }

  public PaymentechUTFormatter() {
    dbg=ErrorLogStream.getForClass(this.getClass());
    gateway=false;
    hostcapture=true;
    //if we decide to go non-volatile this is the place to load last values.
    lastrrn=0;
    currentBatch=999001; //6 digits, typically 3 julian date + 3 counter
    //we will never send off the above batch number. It will cause the terminal to
    //autoclose.
  }
}

//$Id: PaymentechUTFormatter.java,v 1.1 2003/12/10 02:16:54 mattm Exp $