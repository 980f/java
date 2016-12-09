/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/Receipt.java,v $
* Description:  Contains everything needed to print/store/retrieve a receipt (supposedly)
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Receipt.java,v 1.93 2001/10/22 23:33:39 andyh Exp $
*/
package net.paymate.terminalClient;
import net.paymate.*;
import net.paymate.jpos.data.*;//ByteBlock prepared graphics dump info
import net.paymate.util.*;
import net.paymate.connection.*;
import net.paymate.ISO8583.data.*; //TransferType;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.jpos.awt.Hancock;
import net.paymate.awtx.RealMoney;
import java.util.*;
//import java.util.TimeZone;


/**
* Any addition lines in content require mods to totalLines() !!!!
*/

public class Receipt {//ad hoc collection of pieces
  private static final ErrorLogStream dbg = new ErrorLogStream(Receipt.class.getName(),ErrorLogStream.WARNING);
  ///////////////////
  static LocalTimeFormat localTime=LocalTimeFormat.Utc(ReceiptFormat.DefaultTimeFormat);

  public static final String LocalTime(Date utc){
    return localTime.format(utc);
  }

  public static final LocalTimeFormat Formatter(){
    return localTime;
  }

  ///////////////////
  protected int slotHeight=3;
  protected FormattedLines sigSlot(){
    FormattedLines     slot=new FormattedLines();
    //embedding a bunch of explicit newlines was a big mistake. overran printer!
    for(int i=slotHeight;i-->0;){//+_+ use inches and printer attributes to compute
      slot.add(new FormattedLineItem(" "," ",' ', FormattedLineItem.justified));
    }
    slot.add(new FormattedLineItem("X","",'_', FormattedLineItem.justified));
    slot.add(new TextList(abide,lp.textWidth(),TextList.SMARTWRAP_ON));
    return slot;
  }

  //these are translated versions of ReceiptFormat components
  protected static FormattedLines header    = null; // set on applianceLogin
  protected static FormattedLines trailer   = null; // set on applianceLogin
  protected static String abide="I agree to pay per my cardholder agreement";
  //the following controls whether actual signatures get shown.
  protected static boolean showSignature =false;

  protected static final char stdFiller  = '.';

  int bodylinecursor=0;

  //components of a receipt
  protected FormattedLines     body      = new FormattedLines();
  protected Hancock            signature = new Hancock();
  protected FormattedLineItem  section   = null;
  protected FormattedLineItem  dupline   = null;
  protected FormattedLines     cardHolder = null;
  //individual receipt attributes, default for credit card viewed on web
  protected boolean            isFancy=true; //when true print header and footer logoish stuff
  protected boolean            isWorthy=true;//true for print at POS receipts
  protected boolean            signable=false;//whether sig makes any sense.
  protected boolean            hasreply=false;//whether some reply was included


  /**
  * each receipt gets its own print destination, so that different printers can be in scope
  */
  protected PrinterModel       lp=null;

  public Hancock getSignature() {
    return signature;
  }
  /**
  * if a receipt is print worthy then it is printed when the transaction occurs.
  */
  protected boolean printWorthy(){
    return lp!=null&&isWorthy;
  }

  protected boolean hasReply(){
    return hasreply;
  }

  protected boolean printWorthy(boolean printthis){
    isWorthy=printthis;
    return printWorthy();
  }

  ////////////////////////////////
  // transport
  protected final static char HttpEol='\n';//http's new line, not java's localized one

  protected final static String headerKey    = "header";
  protected final static String bodyKey      = "body";
  protected final static String signatureKey = "signature";
  protected final static String trailerKey   = "trailer";
  protected final static String cardHolderKey   = "cardHolder";

  public String toTransport(){
    EasyCursor ezp = new EasyCursor();
    ezp.setString(headerKey    , header.toString());
    ezp.setString(bodyKey      , body.toString());
    if(signature!=null){
      ezp.setString(signatureKey , signature.toTransport().toString());
    }
    ezp.setString(trailerKey   , trailer.toString());
    if(cardHolder!=null){
      ezp.setString(cardHolderKey, cardHolder.toString());
    }
    return ezp.toURLdString("");
  }

  protected FormattedLines getBlock(EasyCursor ezp,String blockkey){
    FormattedLines newblock=new FormattedLines();
    dbg.VERBOSE("BEFORE getBlock(" + blockkey + ")--> " + ezp.getString(blockkey));
    String inBetween = ezp.getString(blockkey);
    dbg.VERBOSE("BETWEEN getBlock(" + blockkey + ")--> " + inBetween);
    newblock.fromString(inBetween);
    dbg.VERBOSE("AFTER getBlock(" + blockkey + ")--> " + newblock.toString());
    return newblock;
  }

  /**
   * @todo transport signable attribute rather than guess at it here
   */
  public void fromTransport(String s){
    // only used by server
    EasyCursor ezp = new EasyCursor();
    ezp.fromURLdString(s, true);
    header =      getBlock(ezp,headerKey);
    body =        getBlock(ezp,bodyKey);
    cardHolder =  getBlock(ezp,cardHolderKey);
    trailer =     getBlock(ezp,trailerKey);
    signature = new Hancock().fromTransport(ezp.getString(signatureKey));
    signable=Hancock.NonTrivial(signature);
  }
  //end transport
  ///////////////////////////////////////

  ///////////////////////////////////////////
  //format controls, for all subsequent receipts
  public static final void setShowSignature(boolean showSignature) {
    Receipt.showSignature = showSignature;
  }

  public static final void setHeader(TextList newHeader){
    Receipt.header = new FormattedLines(newHeader);
  }

  public static final void setTrailer(TextList newTrailer){
    Receipt.trailer = new FormattedLines(newTrailer);
  }

  public static final void setHeader(String newHeader){
    setHeader(new TextList(newHeader,1000,true));
  }

  public static final void setTrailer(String newTrailer){
    setTrailer(new TextList(newTrailer,1000,true));
  }

  public static final void setTimeFormat(String tz, String newformat){
    try {
      localTime= LocalTimeFormat.New(tz,newformat);
    } catch(Exception ignored){
      //leave time format alone.
      dbg.ERROR("Incoming time format rejected:"+newformat);
    }
  }

  public static final void setAbide(String newabide){
    if(Safe.NonTrivial(newabide)){
      abide=newabide;
    }
  }

  public static final void setOptions(ReceiptFormat recipe, String timezone){
    dbg.VERBOSE("Setting options:"+recipe.showSignature+" "+recipe.TimeFormat);
    setHeader(recipe.Header);
    setTrailer(recipe.Tagline);
    setTimeFormat(timezone, recipe.TimeFormat);
    setShowSignature(recipe.showSignature);
    setAbide(recipe.abide);
  }
  //end format controls
  //////////////

  public Receipt setBody(FormattedLines body){
    this.body=(body != null)? body: FormattedLines.createFromString("");
    bodylinecursor=0;
    return this;
  }

  public Receipt setItem(Hancock signature){
    this.signature=signature;
    return this;
  }

  public Receipt setItem(ClerkIdInfo clerk){
    if(body!=null&&clerk.NonTrivial()){
      addLine("Clerk:",clerk.Name());
    }
    return this;
  }

  public Receipt setSection(TransferType op){
    switch(op.Value()){
      case TransferType.Return:{
        section=new FormattedLineItem(" CREDIT ", '*');
      } break;
      case TransferType.Reversal:{
        section=new FormattedLineItem(" VOID ", '*');
      } break;
      case TransferType.Sale:{
        section=new FormattedLineItem("*", "*", '-');
      } break;
      default:{
        section=FormattedLineItem.winger("ERROR");
      }
    }
    return this;
  }

  protected Receipt setItem(MSRData card){
    addLine("Account:", card.accountNumber.Greeked());
    addLine("Expires:", card.expirationDate.Image());
    String name=card.person.isReasonable()?card.person.CompleteName():"manually entered card";
    cardHolder=new FormattedLines(new FormattedLineItem("Card Holder:",name ));
    cardHolder.add(section);
    return this;
  }

  public Receipt setItem(String hint,RealMoney amount){
    //we can try fancy ways of indicating negatives....
    addLine(hint+":", amount.Image());
    return this;
  }

  public Receipt setItem(FinancialRequest request){
//    dbgFanciness("settingRequestInfo");
    setSection(request.sale.type.op);
    Section();
    if( request instanceof ReversalRequest){//amount info not available on voids...
      addLine("Original Txn:", ((ReversalRequest)request).toBeReversed.stan());
    } else {
      setItem(request.sale.amountHint(), request.sale.money.amount);
    }
    signable= request.getsSignature();
    cardHolder=null;
    if(request instanceof CheckRequest){
      MICRData check= ((CheckRequest)request).check;
      addLine("Bank:", check.Transit);
      addLine("Account:", check.Account);
      addLine("Number:", check.Serial);
      printWorthy(false);
    }
    else
    if(request instanceof CardRequest){
      setItem(((CardRequest) request).card);
    }
    return this;
  }

  public Receipt setItem(TerminalInfo timfo){
    if(body!=null && timfo!=null){
      addLine("Terminal:",timfo.getNickName());
    }
    return this;
  }

  public Receipt() {
    //you'd better not use it...til you set some items
//    dbgFanciness("built an empty one");
  }

  public Receipt(FinancialReply reply,FinancialRequest request) {
    setItem(request);
    setItem(reply);
  }

  public Receipt(String transport) {
    if(transport != null) {
      fromTransport(transport);
    } else {
      setBody(null);
    }
  }

  public Receipt(Receipt old) {//--- does NOT do anything worthwhile.
    this();
    this.slotHeight = old.slotHeight;
    this.body = old.body;
    this.signature = old.signature;//+++ copy
    this.section = old.section;
    this.dupline = old.dupline;
    this.cardHolder = old.cardHolder;
    this.isFancy = old.isFancy;
    this.isWorthy = old.isWorthy;
    this.signable = old.signable;
    this.hasreply = old.hasreply;
    this.lp = old.lp;
  }

  /**
   * call when you decide to blow off the signature part of the receipt content.
   */
  public Receipt dropSigning(){
    signable=false;
    signature=null;
    return this;
  }

  /**
  * @param reply from a FAILED transaction
  */
  public Receipt onFailure(ActionReply reply,PrinterModel printer){//for failures
    if(body==null){
      body = new FormattedLines();
    }
    if(printer!=null){
      lp=printer;//lp is NOT a static. This lets us have multiple printers, but only one per actual receipt rendition
    }
    section=FormattedLineItem.winger("Transaction failed");
    Section();
    addLine("code:", reply.status.Image());
    insertDate("when:",Safe.Now());
    setItem(reply.Errors);
    hasreply=true;
    Section();
    return this;
  }

  /**
  * used by web server to presize graphics area
  */
  public int totalLines() {
    int count = 0;
    if(header != null) {
      count += header.size();
    }
    if(body != null) {
      count += body.size();//sections are now added to body rather than printed separately// + 1;  // an extra one for the section we stuff in there
    }
    if(trailer != null) {
      count += trailer.size();
    }
    if(cardHolder != null) {
      count += cardHolder.size();
    }
    if(!Hancock.NonTrivial(signature)){
      count += 1; // for dupline
    }
    return count;
  }

  /**
  * can't think of a good name for this, sets section separator to indicate that the transaction was NOT good.
  */
  void Scream(){
    section=new FormattedLineItem(" ERROR ", "*", '*',FormattedLineItem.centered);
    isFancy=false;
    dropSigning();
  }

  /**
  * @param Errors is a block of error text to append to receipt body.
  */
  protected Receipt setItem(TextList Errors){
    if (Errors.size()>0) {
      FormattedLineItem highlighter=new FormattedLineItem(" ");
      Scream();
      body.add(highlighter);
      Section();
      body.add(Errors);
      Section();
      body.add(highlighter);
    }
    return this;
  }

  protected void addLine(String prefix,String value){//FUE
    body.add(new FormattedLineItem(prefix,value));
  }

  protected void Section(){//FUE
    body.add(section);
  }

  protected Receipt setItem(FinancialReply reply) {
    FormattedLines fl = new FormattedLines();
//    dbgFanciness("setting reply");
    String tdate= LocalTime(TransactionTime.tranUTC(reply.tid.time));
    addLine("Date:", tdate);
    addLine("Txn #:", reply.tid.Abbreviated());
    setItem(reply.Errors);
    hasreply=true;
    if(reply.Response.isApproved()){
      addLine("Approval:", reply.Approval());
      if(reply instanceof ReversalReply){
        //output original tranny info:
        ReversalReply voided= (ReversalReply)reply;
        setItem("Amount Voided",voided.originalAmount);
        setItem(voided.card);
        //since cardholder part of card info doesn't naturally print on voids we have to...
        body.add(cardHolder);
      }
    } else {
//      addLine("DECLINED:", reply.Response.ExtendedDescription()); // +++ until we start sending this info as a seaprate string and not as a Response.
      Scream();
    }
    Section();
    return this;
  }

  protected void insertDate(String prefix,Date fig){
    addLine(prefix,LocalTime(fig));
  }

  protected Receipt setItem(StoreConfig reply, TerminalInfo termInfo, boolean online) {
    //we could relocate the other termial reply processing to this function...
    section=FormattedLineItem.winger("TERMINAL LOGIN");
    Section();
    addLine("TermCaps:", termInfo.getNickName()+'/'+reply.termcap.toSpam()+"N"+(online?"+":"-"));
    addLine(LocalTime(Safe.Now()), reply.si.timeZoneName);//to show off formatting
    addLine("Build:",Revision.Version());

    signable=false;
    Section();
    return this;
  }

  public void printHeader() {
//    dbgFanciness("printingHeader");
    lp.startPage();
    try {
      if(printWorthy()){
        if( Receipt.header != null) {
          lp.print(dupline);
          lp.print(Receipt.header);
        } else {
          dbg.VERBOSE("header == null; not printing header");
        }
      }
    } finally {
      lp.endPage();
    }
  }

  public void printBody() {
    lp.startPage();
    try {
      if(printWorthy()){
        lp.print(dupline);
        while(bodylinecursor<body.size()){
          lp.print(body.itemAt(bodylinecursor++));
        }
        lp.print(dupline);
      }
    } finally {
      lp.endPage();
    }
  }

  public void printSig() {
//    dbgFanciness("printingSignature");
    lp.startPage();
dbg.VERBOSE("printWorthy="+printWorthy()+", signable="+signable+", Hancock.NonTrivial(signature)="+Hancock.NonTrivial(signature)+", showSignature="+showSignature);
    try {
      if(printWorthy()){
        if(signable){
          if(Hancock.NonTrivial(signature)){
            if(showSignature){
              lp.print(signature);
            }
            //else no signature of any sort appears on receipt
          } else {
            dbg.VERBOSE("trivsig:"+signature);
            if(dupline==null){
              dupline=new FormattedLineItem(" STORE COPY ",'#');
            }
            lp.print(sigSlot()); //manual signature slot
          }
          lp.print(cardHolder);//especially if NOT printing signature
        }
      }
    }
    finally {
      lp.endPage();
    }
  }

  public void printTrailer() {
//    dbgFanciness("printingTrailer");
    lp.startPage();
    try {

      if(printWorthy()){
        lp.print(dupline);
        if(Receipt.trailer != null) {
          lp.print(Receipt.trailer);
        } else {
          dbg.VERBOSE("trailer == null; not printing trailer");
        }
      }
    } finally {
      lp.endPage();
    }
  }

  public void signAndShove() {
//    dbgFanciness("signAndShove");
    lp.startPage();
    try {

      if(printWorthy()){
        printSig();
        if(isFancy){
          printTrailer();
        }
        lp.formfeed(); // REQUIRED !!!! to force abstract generators to produce output
      }
    } finally {
      lp.endPage();
    }

  }

  public void startPrint(PrinterModel lp,int copy){
    this.lp=lp;
    bodylinecursor=0;
    switch(copy){
      default: {
        dupline=new FormattedLineItem("Duplicate #"+copy,'!');
      } break;
      case 1:  {//???who changed this to 5? back to one for sigcapture failure use.
        dupline=new FormattedLineItem(" Customer Copy ",'*');
      } break;
      case 0:  {
        //if manually signed then mark this as store copy.
        dupline=null;
      } break;
    }

    lp.startPage();
    try {
      if(printWorthy()){
        lp.startText(); //to ensure printer is in a known mode
        if(isFancy){
          printHeader();
        }
      }
    } finally {
      lp.endPage();
    }

  }

  public void print(PrinterModel lp,int copy) {
    startPrint(lp,copy);
    printBody();
    signAndShove();
  }
  //////////////////////////////////////////////////////////////
  public static final FormattedLineItem timeline(String prefix, Date utc){
    return new FormattedLineItem(prefix,LocalTime(utc));
  }

  public static final FormattedLineItem timeline(String prefix, long utc){
    return timeline(prefix,new Date(utc));
  }

  public static final void PrintBatchList(PrinterModel lp,BatchReply reply){
    dbg.Enter("PrintBatchList");
    dbg.VERBOSE("line count:"+reply.body.size()+2*(reply.header.size()+Receipt.header.size()));
    if(lp!=null){
      lp.print(Receipt.header);
      lp.print(reply.header);
      lp.print(reply.body);
      lp.print(reply.header);
      lp.print(Receipt.header);
      lp.formfeed();
    }
  }

}
//$Id: Receipt.java,v 1.93 2001/10/22 23:33:39 andyh Exp $
