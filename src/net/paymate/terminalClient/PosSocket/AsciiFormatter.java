package net.paymate.terminalClient.PosSocket;

/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/AsciiFormatter.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.73 $
 * @todo: make signature quadrant configurable
*/

import net.paymate.connection.*;
import net.paymate.util.*;
import net.paymate.terminalClient.*;
import net.paymate.lang.StringX;
import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.awt.*;
import net.paymate.lang.ReflectX;
import net.paymate.awtx.RealMoney;
import net.paymate.awtx.Quadrant;

import net.paymate.ivicm.et1K.SignatureType;

import java.util.*;


public class AsciiFormatter extends Formatter implements AsciiFormatterToken {
  static ErrorLogStream dbg;
  protected LocalTimeFormat localTime=LocalTimeFormat.Utc(ReceiptFormat.DefaultTimeFormat);

  public String formatId(){
    return "A7";
  }
  protected static final String CRLF=Ascii.CRLF;
  /**
   * suffix applied to all response strings.
   */

  protected static String csvend= ","+END+CRLF;//removed 'final' so that extensions can tweak line end

  public Formatter setTimeFormat(String tz, String newformat){
    try {
      localTime= LocalTimeFormat.New(tz,newformat);
    } catch(Exception ignored){
      //leave time format alone.
      dbg.ERROR("Incoming time format rejected:"+newformat);
    } finally {
      return this;
    }
  }

  /////////////
  // utilities that will move into an intermediate class "TextListFormatter"

  public static TextList parse(byte [] raw){//+_+ move to textList.
    return TextList.CreateFrom(Ascii.cooked(raw));
  }

  /**
   * left as string here so that we can use a sstringBuffer to collect things.
   * +_+ all would work cleaner if we streamed.
   * @return string with commas added and a special line termintator
   * @param hresponse is packed into a line THEN CLEARED!
   */
  public static String csvLine(TextList hresponse){
    try {
      return (hresponse.asParagraph(",")+csvend);
    }
    finally {
      hresponse.clear();
    }
  }
/**
 * useful for when incoming message is too borked to deal with
 */
  protected byte[] nakMessage(String body){
    TextList oneline=new TextList();
    oneline.add(NAK);
    oneline.add(body);
    oneline.add(formatId());
    return csvLine(oneline).getBytes();
  }

  /**
   *
   */
  protected BypassRequest Nak(String detail){
    return BypassRequest.New(nakMessage(detail));
  }

  protected byte[] ackMessage(String body){
    return (ACK+","+body+csvend).getBytes();
  }

  /**
   * ack should reflect incoming command type
   */
  protected BypassRequest Ack(String token){
    return BypassRequest.New(ackMessage(token));
  }
/**
 * useful for servere errros with a request.
 */
  protected byte[] nakAction(ActionRequest request, String reason){
    TextList oneline=new TextList();
    oneline.add(NAK);
    oneline.add(request.TypeInfo());
    oneline.add(reason);
    return csvLine(oneline).getBytes();
  }

  ///////////////////////

  protected ClerkIdInfo storeId(String name,String password){
    return parent.clerk.set(new ClerkIdInfo(name,password)).Value();
  }

  protected ActionRequest asciiLogin(String name,String password){
    if(parent == null){
      return Nak("clerkLogin,no terminal!");
    }
    storeId(name,password);
    return new LoginRequest();//fyi: id gets applied to all messages sent, we don't have to stick it into request here.
  }

  protected static MSRData getCard(TextListIterator tli){
    MSRData newone=new MSRData();
    if(tli!=null&&!tli.eof()){
      newone.Clear();
      switch(StringX.parseInt(tli.next())){
        case 0:{//manual
          if(tli.stillHas(2)){
            newone.accountNumber.setto(tli.next());
            dbg.ERROR("expiration as text:"+tli.lookAhead());
            newone.expirationDate.parsemmYY(tli.next());//same order as card imprint
            dbg.ERROR("expiration as yymm:"+newone.expirationDate.YYmm("moot"));//#diagnostic
          }
        } break;//parseFinancial() is innocuous if there is no track data.
        case 1:{
          newone.setTrack(MSRData.T1,tli.next());
        } break;
        case 2:{
          newone.setTrack(MSRData.T2,tli.next());
        } break;
        case 3:{
          newone.setTrack(MSRData.T1,tli.next());
          newone.setTrack(MSRData.T2,tli.next());
        } break;
      }
      newone.ParseFinancial();
      if(tli.lookAhead().equals(AVSpresent)){//we shall NOT check manuals swipe at this point, only at authorizer interface
        if(tli.stillHas(3)){
          tli.next();//skip over "AVS" token. deferred utnil now to make error message prettier.
          newone.setAVSAddress(tli.next()).setZip(tli.next());
          dbg.VERBOSE("set avs="+newone.avsInfo());
        } else {
          newone.addError("Incomplete AVS info following AVS token:"+tli.tail());
        }
      } else { //no AVS option
        dbg.VERBOSE("no avs info included");
      }
    }
    return newone;
  }

  public static PINData getPindata(TextListIterator fields){
    if(fields!=null&&fields.stillHas(2)){
      return PINData.Dukpt(fields.next(),fields.next());
    }
    return null;
  }

  protected STAN getStan(TextListIterator fields){
    return STAN.NewFrom(fields.next());
  }

  /**
  * "Reversal",stan
  */
  protected TxnReference getReference(TextListIterator fields){
    if(fields.stillHas(1)){
      return TxnReference.New(parent.termInfo.id(),getStan(fields));
    } else {
      return TxnReference.New();//a defective one
    }
  }

  protected ActionRequest batcher(TextListIterator fields){
    String rqtoken=fields.next();//list or close
    if(rqtoken.equalsIgnoreCase(DrawerListing)){
      return BatchRequest.Listing();
    }
    else if(rqtoken.equalsIgnoreCase(DrawerClosing)){
      return BatchRequest.JustClose(BatchRequest.WITHDETAIL);
    }
    return Nak("Unknown Batch Option:"+rqtoken);
  }

  protected ActionRequest storeFunction(TextListIterator  fields){//see StoreMenu, but don't lock these tokens to the menu directly (yet)
    String rqtoken=fields.next();
    if(StringX.equalStrings(rqtoken,StoreDeposit,true)){
      return new StoreRequest(); //the only extant request is to do a deposit.
    } else {
      return Nak("Unknown Store Option:"+rqtoken);
    }
  }

  protected ActionRequest SocketCommand(TextListIterator fields){
    String rqtoken=fields.next();
    //whoami : return our terminal's identifiers
    if(rqtoken.equalsIgnoreCase(SocketPause)){
      net.paymate.lang.ThreadX.sleepFor(fields.nextInt()*1000L);
      return Ack(SocketPause+",Finished");
    }
    if(rqtoken.equalsIgnoreCase(SocketEcho)){//returns canonically formatter version of input
      return Ack(fields.TextList().asParagraph(","));//complete list
    }
    if(rqtoken.equalsIgnoreCase(SocketSystem)){//returns canonically formatter version of input
      if(fields.hasMoreElements()){
        TerminalCommand tc= new TerminalCommand(fields.next());
        Appliance.BroadCast(tc);
        // and we have a race to get the response back to caller!!!
        return Ack(SocketSystem+","+tc.Image());
      }
    }
    return Nak(SocketSystem+",UnknownCommand,"+rqtoken);
  }

  // separated out since we will want to overload it in extents
  protected String preapproval(TextListIterator fields) {
    return fields.next();//approval code, sposed to be 6 chars-usually all digits
  }

  protected SaleInfo saleInfo(TextListIterator fields){
    SaleInfo sale=new SaleInfo();
    sale.type.payby.setto(fields.next());//e.g. credit,debit,giftcard
    sale.type.op.setto(fields.next());//e.g. sale,return.void,force ...
    if (sale.type.op.is(TransferType.Force)) {
      sale.preapproval=preapproval(fields);
    }
    if(sale.type.amountReqd()){
      sale.setMoney(new RealMoney(fields.next()));
    }
    //#hyperformatter reads stan at this point.
    return sale;
  }

  protected ActionRequest finReq(TextListIterator fields){
    SaleInfo sale=saleInfo(fields);//gets up to stan (--- is this up to and including, or not including !?!?!?)

    dbg.VERBOSE("next: "+fields.lookAhead()+" finReq for:"+sale.toSpam());
    switch (sale.type.payby.Value()) {
      case PayType.GiftCard:{
        return PaymentRequest.GiftCardRequest(sale,getCard(fields));
      }// break;
      case PayType.Credit:{
        return PaymentRequest.CreditRequest(sale,getCard(fields));
      } //break;
      case PayType.Debit:{
        return PaymentRequest.DebitRequest(sale,getCard(fields),getPindata(fields));
      }// break;
    }
    return Nak("Unknown,Request");
  }
  ////////////////////////////
  // receipt
//transaction reference number
//printer model code
//#of text lines of this receipt
//text line style:
//"raw" or "formatted"

//insert signature after this line of text
//signature width
//signature height

  ReceiptAggregator rag;
  private void ragme(){
    if(rag==null){
      rag=new ReceiptAggregator();
    }
  }

  protected ActionRequest startReceiptStore(TextListIterator fields){
    rag.setReference(getReference(fields));
    rag.printerModel= fields.next();//printer model code
    int linestofollow=fields.nextInt();
    rag.isPreformatted= fields.next().equalsIgnoreCase("raw");//yes, raw is preformatted
    if(rag.isPreformatted){
      rag.preformatted=new TextList(linestofollow);
      rag.body=null;
    } else {
      rag.preformatted=null;
      rag.body=new FormattedLines(linestofollow);
    }
    if(fields.stillHas(3)){
      rag.signatureLine=fields.nextInt();
//      rag.setSigbox(fields.nextInt(),fields.nextInt());
    } else {
      rag.setNoSignature();
//      rag.sigbox= null;
    }
    rag.Hancock().setQuadrant(Quadrant.First());//make this configurable!
    return rag.expectingSig()? (ActionRequest) Ack(ReceiptStoreToken) : (ActionRequest) ReceiptStoreRequest.New(rag.rcp,rag.TxnReference());
  }

  protected ActionRequest ReceiptStoreRequest(){
    return ReceiptStoreRequest.New(rag.rcp,rag.TxnReference());
  }


  private ActionRequest sigorack(){
    ragme();
    if(rag.sigComplete()){//which it is for the simple types!
      rag.rcp.setItem(rag.Hancock());
      //anything else?
      //implied rule: don't send sig to this guy until auth has been checked
      return ReceiptStoreRequest();
    } else {
      return Ack(SignatureToken);
    }
  }

  protected ActionRequest startSignature(TextListIterator fields){
    ragme();
    rag.setReference(getReference(fields));
    //second word is format type
    String formatcode=fields.next();
    SignatureType sigType=new SignatureType(formatcode);

    switch(sigType.Value()){
      case SignatureType.Hancock:{
        rag.startHancock(fields.nextInt());
      } break;
      case SignatureType.NCRA:
      case SignatureType.Hypercom:
        //remaining are simple
        rag.simpleSignature(sigType,fields.next());
        break;
      default:
        return Nak(SignatureToken+",Unknown Format");
    }
    return sigorack();
  }

  //////////////
  public ActionRequest requestFrom(byte[] line){
    if(ByteArray.NonTrivial(line)){
      return requestFrom(TextListIterator.New (parse(line)));
    } else {//stifle response to extraneous line terminators.
      return new BypassRequest(NullResponse);
    }
  }
  /**
   * easier to override here:
   */
  public ActionRequest requestFrom(TextListIterator fields){
    String rqtoken=fields.next();

    if( ! StringX.NonTrivial(rqtoken)){
      return Nak("trivial request");
    }
//    if(rqtoken.equalsIgnoreCase(PaymentOperation)){//optional token
//      return finReq(fields);
//    }
    if(rqtoken.equalsIgnoreCase(LogOperation)){
      int msglevel= StringX.parseInt(fields.next());
      dbg.rawMessage(msglevel,fields.tail(","));//restore commas
      return BypassRequest.New("");//+_+ a problem for jumpwareFormatter
    }
    if(rqtoken.equalsIgnoreCase(SocketOperation)){
      return SocketCommand(fields);
    }
    if(rqtoken.equalsIgnoreCase("clerkLogin")||rqtoken.equalsIgnoreCase("login")){//deprecated
      return asciiLogin(fields.next(),fields.next());
    }
    if(rqtoken.equalsIgnoreCase(VoidOperation)){
      return PaymentRequest.Void(getReference(fields));
    }
    if(rqtoken.equalsIgnoreCase(ModifyOperation)){
      return PaymentRequest.Modify(getReference(fields),new RealMoney(fields.next()));
    }
    if(rqtoken.equalsIgnoreCase(DrawerOperation)){//approved transactions
      return batcher(fields);
    }
    if(rqtoken.equalsIgnoreCase(StoreOperation)){
      return storeFunction(fields);
    }
    if(rqtoken.equalsIgnoreCase(ReceiptStoreToken)){
      return startReceiptStore(fields);
    }
    if(rqtoken.equalsIgnoreCase(SignatureToken)){
      return startSignature(fields);
    }
    if(rqtoken.equalsIgnoreCase(StrokeToken)){
      ragme();
      rag.moreSignature(fields);
      return sigorack();
    }

    //else TRUST that it is financial:
    fields.rewind(1);  //to get back payment mechanism type
    return finReq(fields);
  }

  protected static String acker(ActionReply reply){
    return reply!=null&& reply.Succeeded()?ACK:NAK;
  }

  protected void convertAuthResponse(PaymentReply reply,TextList hresponse){
    if(reply.Succeeded()){
      hresponse.add(reply.tref().refNum());//had to add this info for voids.
      if(reply.isApproved()){
        hresponse.add("Approved");
        hresponse.add(reply.Approval());
      } else {
        hresponse.add("Declined");
      }
      hresponse.add(reply.authMessage().trim());//---move into where first read from database.
    }
    hresponse.appendMore(reply.Errors);//even approved txns can have some notices
  }

  protected static TextList showTotal(Accumulator ttl,String name,TextList hresponse){
    if(hresponse==null){
      hresponse=new TextList();
    }
    hresponse.add(name);
    hresponse.add(ttl.getCount());
    hresponse.add(ttl.getTotal());
    return hresponse;
  }

  protected static TextList showTotals(SubTotaller totals,String gname,TextList hresponse){
    if(hresponse==null){
      hresponse=new TextList();
    }
    showTotal(totals.grand(),gname,hresponse);
    TextList sums=totals.subtotalNames();
    for(int i=sums.size();i-->0;){
      String name=sums.itemAt(i);
      Accumulator one=totals.getAccumulator(name);
      showTotal(one,name,hresponse);
    }
    return hresponse;
  }

  protected boolean showListing=true; //added for hypercomFormatter H1
  protected boolean showTotals=true; //added for hypercomFormatter H1

  private void addTimeRange(TextList hresponse,TimeRange ranger){
    ranger=TimeRange.makeSafe((ranger));
    hresponse.add(ranger.start().toString());
    hresponse.add(ranger.end().toString());
  }

  /**
   * a message header is already fed into hresponse
   * add batch details
   */
  private byte[] batchListing(BatchReply reply,TextList hresponse){
    StringBuffer listing=new StringBuffer();
    hresponse.add(reply.numItems());
    hresponse.add(reply.isClosed?"Closed":"Open");
    addTimeRange(hresponse, reply.ranger());
    //@AVS@ put errors on to end of first line!
    listing.append(csvLine(hresponse));
    if(showListing){
      for(int i=reply.numItems();i-->0;){
        BatchLineItem bli= reply.item(i);
        hresponse.clear();
        hresponse.add(i);
        hresponse.add(bli.stan());
        hresponse.add(localTime.format(bli.date));
        hresponse.add(bli.TransferType().Image());
        hresponse.add(bli.finalAmount().Value());
        hresponse.add(bli.TypeColData);
        hresponse.add(bli.card.Greeked("")); //TypeColDate is MC or VS etc.
        listing.append(csvLine(hresponse));
      }
    }
    if(showTotals){
      listing.append(csvLine(showTotals(reply.byInstitution,"Total",hresponse)));
    }
    return String.valueOf(listing).getBytes();
  }

  public byte[] replyFrom(Action response,boolean timedout){
    TextList hresponse=new TextList();
    if(response==null){
      return nakMessage("Unknown,request not understood");
    }
    if(timedout){
      return nakAction(response.request,"timedout");
    }
    if(response.reply==null){
      return nakAction(response.request,"null reply");
    }
    dbg.VERBOSE("rcvd from server:"+response.reply.Type().Image());
    dbg.VERBOSE("reply:"+response.reply.toEasyCursorString());
    hresponse.add(acker(response.reply));
    hresponse.add(response.reply.Type().Image());
    switch(response.reply.Type().Value()){
      case ActionType.batch:{
        return batchListing((BatchReply)response.reply,hresponse);
      } //break;
      case ActionType.unknown:{
        return nakMessage("Unknown,InvalidReplytype");
      } //break;
      case ActionType.clerkLogin:{
        LoginReply reply=(LoginReply) response.reply;
        hresponse.add(reply.clerkCap.toSpam());
        //whatever happened to clerk nickname?
      } break;
      case ActionType.payment: {
        PaymentReply pr = (PaymentReply) response.reply;
        PayType pt = pr.payType;
        switch (pt.Value()) {
          case PayType.GiftCard: {
            convertAuthResponse(pr, hresponse);
            hresponse.add(pr.sv.balance.Value());//we get a balance even with a decline.
          }
          break;
          case PayType.Debit: //join credit
            //@todo: see if debit response has an "you were charged an extra $" field
          case PayType.Credit: { //mismatched request and reply should have a nice declined message already generated on server.
            convertAuthResponse(pr, hresponse);
            //@AVS@ attach avs character and decode it.
          }
          break;
          default: {

          }
          break;
        }
      }
    }
    hresponse.add(response.reply.Errors);
    return csvLine(hresponse).getBytes();
  }

  public boolean onePerConnect(){
    return false;
  }

  public byte[] onException(Exception any){
    dbg.Caught(any);
    return nakMessage("Exception,"+any.getMessage());
  }

  /*public*/ AsciiFormatter(){ //for polymorphic instantiation
    if(dbg==null){
      dbg = ErrorLogStream.getForClass(AsciiFormatter.class);
    }
    dbg.VERBOSE("Instantiated:"+  ReflectX.shortClassName(this));
  }

}
//$Id: AsciiFormatter.java,v 1.73 2004/03/10 00:36:35 andyh Exp $
