/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/Receipt.java,v $
 * Description:  Contains everything needed to print/store/retrieve a receipt (supposedly)
 * Copyright:    2000 PayMate.net
 * Company:      paymate
 * @author       paymate
 * @version      $Id: Receipt.java,v 1.160 2004/02/26 16:34:51 mattm Exp $
 */
package net.paymate.terminalClient;
import net.paymate.*;
import net.paymate.jpos.data.*;//ByteBlock prepared graphics dump info
import net.paymate.util.*;
import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.lang.StringX;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.jpos.awt.Hancock;
import net.paymate.awtx.RealMoney;
import java.util.*;
import net.paymate.lang.ObjectX;

/**
 * Any addition lines in content require mods to totalLines() !!!!
 */

public class Receipt implements isEasy {//ad hoc collection of pieces
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Receipt.class,ErrorLogStream.WARNING);
  ///////////////////
  LocalTimeFormat localTime=LocalTimeFormat.Utc(ReceiptFormat.DefaultTimeFormat);

  public final String localTime(UTC utc){
    return localTime.format(utc);
  }

  ///////////////////

  private FormattedLines sigSlot(){
    int slotHeight=3;//+_+ really should be derived from printer
    FormattedLines slot=new FormattedLines();
    //embedding a bunch of explicit newlines was a big mistake. overran printer!
    for(int i=slotHeight;i-->0;){//+_+ use inches and printer attributes to compute
      slot.add(new FormattedLineItem(" "," ",' ', FormattedLineItem.justified));
    }
    slot.add(new FormattedLineItem("X","",'_', FormattedLineItem.justified));
    slot.add(new TextList(abide,lp.textWidth(),TextList.SMARTWRAP_ON));
    return slot;
  }

  //these are translated versions of ReceiptFormat components
  private FormattedLines header    = null; // set on applianceLogin
  private FormattedLines trailer   = null; // set on applianceLogin
  private String abide="I agree to pay per my cardholder agreement";
  private String merchantReferenceTag=null;
  //the following controls whether electronic signatures get printed.
  private boolean showSignature =false;

//  private static final char stdFiller  = '.';

  int bodylinecursor=0;

  //components of a receipt
  private FormattedLines    body      = FormattedLines.Empty();//too hard to check null everywhere used
  private Hancock           signature = null;
  private FormattedLineItem section   = null;
  private FormattedLineItem dupline   = null;
  private FormattedLines    cardHolder = null;
  //individual receipt attributes, default for credit card viewed on web
  private boolean           isFancy=true; //when true print header and footer logoish stuff
  private boolean           isWorthy=true;//true for print at POS receipts
  private boolean           signable=false;//whether sig makes any sense.
  private boolean           hasreply=false;//whether some reply was included
  private boolean           showSlot =false; //whether this guy is manually signable
  /**
   * each receipt gets its own print destination, so that different printers can be in scope
   */
  private PrinterModel       lp=null;

  public Hancock getSignature() {
    return signature;
  }
  /**
   * if a receipt is print worthy then it is printed when the transaction occurs.
   */
  /*package*/ boolean printWorthy(){
    return lp!=null&&isWorthy;
  }

  public boolean shouldbeCaptured(){
    return Hancock.NonTrivial(signature);
    //old way: return signable || showSlot;
  }

  public boolean hasReply(){
    return hasreply;
  }

  /*package*/ boolean printWorthy(boolean printthis){
    isWorthy=printthis;
    return printWorthy();
  }

  ////////////////////////////////
  // transport
  private final static char HttpEol='\n';//http's new line, not java's localized one

  private final static String headerKey    = "header";
  private final static String bodyKey      = "body";
  private final static String signatureKey = "signature";
  private final static String trailerKey   = "trailer";
  private final static String cardHolderKey   = "cardHolder";

  /*
  ezformats:
  0: very fat 25k per receipt
  1: about 4 kay per receipt, still using urledstrings for nesting
  2: use EasyCursor modern vector and indentation
  */

  private static final int ezformat=2;
//starting with version 1.003
  public void save(EasyCursor ezc){
    ezc.setInt("format",ezformat);
    ezc.setObject(headerKey,header);
    ezc.setObject(bodyKey  ,body);
    ezc.setObject(signatureKey ,signature);
    ezc.setObject(trailerKey,trailer);
    ezc.setObject(cardHolderKey, cardHolder);
  }

  private Hancock getHancock(String key,EasyCursor ezc,int format){
//    System.out.println("Receipt.getHancock");
//    System.out.println(ezc.asParagraph(OS.EOL));
    String urld= ezc.getString(key);
    if(urld.startsWith("%23")){//signature block is still urlpacked
      Hancock newone= new Hancock();
      newone.load(EasyCursor.fromUrl(urld));
      return newone;
    }
    return (Hancock) ezc.getObject(key,Hancock.class);
  }

  private FormattedLines getblock(String key,EasyCursor ezc,int format){
    switch(format){
      case 0:
      case 1:{
        String streamed= ezc.getString(key);
        EasyProperties block=EasyProperties.FromString(streamed);
        int count=block.getInt("count");
        FormattedLines resurrected=new FormattedLines(count);
        for(int i=0;i<count;++i){//#preserve order
          String urlditem=block.getString(String.valueOf(i));
          FormattedLineItem fli= new FormattedLineItem(EasyCursor.fromUrl(urlditem));
          resurrected.add(fli);
        }
        return resurrected;
      }
    case 2: {
      return (FormattedLines) ezc.getObject(key,FormattedLines.class);
    }
    }
    return FormattedLines.Empty();
  }

  public void load(EasyCursor ezc){
    int loadformat=ezc.getInt("format",1);//before type 2 this field didn't exist
    header= getblock(headerKey,ezc,loadformat);
    body= getblock(bodyKey  ,ezc,loadformat);
    signature=   getHancock(signatureKey,ezc,loadformat);
    signable= Hancock.NonTrivial(signature);//legacy guess
    trailer=getblock(trailerKey,ezc,loadformat);
    cardHolder=getblock(cardHolderKey, ezc,loadformat);
  }

  ///////////////////////////////////////////
  //format controls, for all subsequent receipts
  public final void setShowSignature(boolean showSignature) {
    this.showSignature = showSignature;
  }

  public final Receipt setHeader(TextList newHeader){
    this.header = new FormattedLines(newHeader);
    return this;
  }

  public final Receipt setTrailer(TextList newTrailer){
    this.trailer = new FormattedLines(newTrailer);
    return this;
  }

  public final Receipt setHeader(String newHeader){
    setHeader(new TextList(newHeader,1000,true));//1000== "don't wrap"
    return this;
  }

  public final Receipt setTrailer(String newTrailer){
    setTrailer(new TextList(newTrailer,1000,true));
    return this;
  }

  public final void setTimeFormat(String tz, String newformat){
    try {
      localTime= LocalTimeFormat.New(tz,newformat);
    } catch(Exception ignored){
      //leave time format alone.
      dbg.ERROR("Incoming time format rejected:"+newformat);
    }
  }

  public final void setAbide(String newabide){
    if(StringX.NonTrivial(newabide)){
      abide=newabide;
    }
  }


  public final void setMerchantReferenceTag(String mrp){
    this.merchantReferenceTag=mrp;
  }

  public final void setOptions(ReceiptFormat recipe, String timezone, String merchRefPrompt){
    dbg.VERBOSE("Setting options:"+recipe.showSignature+" "+recipe.TimeFormat);
    setHeader(recipe.Header);
    setTrailer(recipe.Tagline);
    setTimeFormat(timezone, recipe.TimeFormat);
    setShowSignature(recipe.showSignature);
    setAbide(recipe.abide);
    setMerchantReferenceTag(merchRefPrompt);
  }
  //end format controls
  //////////////

  public Receipt setBody(FormattedLines body){
    this.body=(body != null)? body: FormattedLines.Empty();
    bodylinecursor=0;
    return this;
  }

  public Receipt setItem(Hancock signature){
    this.signature=signature; //reception manager did the essential copying for posterminl
    return this;
  }

  public Receipt setItem(ClerkIdInfo clerk){
    if(body!=null&&clerk.NonTrivial()){
      addLine("Clerk",clerk.Name());
    }
    return this;
  }

  public Receipt setSection(TransferType op){
    switch(op.Value()){
      case TransferType.Query:{
        section=new FormattedLineItem(" QUERY ", '*');
      } break;
      case TransferType.Return:{
        section=new FormattedLineItem(" CREDIT ", '*');
      } break;
      case TransferType.Reversal:{
        section=new FormattedLineItem(" VOID ", '*');
      } break;

      case TransferType.Modify:{
        section=new FormattedLineItem(" MODIFIED ", '-');
      } break;
      case TransferType.Force:{
        section=new FormattedLineItem(" FORCED ", '-');
      } break;
      case TransferType.Sale:{
        section=new FormattedLineItem("*", "*", '-');
      } break;
      case TransferType.Authonly:{
        section=new FormattedLineItem(" PRE-AUTH ", '-');
      } break;

      default:{
        section=FormattedLineItem.winger("ERROR");
      } break;
    }
    return this;
  }

  private Institution banknet=CardIssuer.Unknown;
  private Receipt setItem(MSRData card){
    banknet=card.binEntry().issuer;
    addLine("Account", card.accountNumber.Greeked());
// card truncation now incloudes omitting expiration date
//    if(! card.expirationDate.isMoot()){
//      addLine("Expires", card.expirationDate.Image());
//    }
    String name=card.person.isReasonable()?card.person.CompleteName():"name not available";
    cardHolder=new FormattedLines(new FormattedLineItem("Card Holder:",name ));
    cardHolder.add(section);
    return this;
  }

  public Receipt setItem(String hint,RealMoney amount){
    //we can try fancy ways of indicating negatives....
    addLine(hint, amount.Image());
    return this;
  }

  public Receipt setItem(PaymentRequest request){
    setSection(request.sale.type.op);
    Section();
    if(request.sale.Amount().absValue()!=0){
      setItem(request.sale.amountHint(), request.sale.Amount());
    }
    pickyAddLine(merchantReferenceTag,request.sale.merchantReferenceInfo());

    if( request.modifiesTxn()){//amount info not available on voids...
      addLine("Original Txn", request.refNum());
    } else {
      addLine("Txn #",String.valueOf(request.sale.stan));
    }
    signable= request.getsSignature();
    cardHolder=null;
//    if(request instanceof CheckRequest){//do we need to truncate these?
//      MICRData check= ((CheckRequest)request).check;
//      addLine("Bank", check.Transit);
//      addLine("Account", check.Account);
//      addLine("Number", check.Serial);
//      printWorthy(false);
//    }
//    else
      if(request.hasSomeCardInfo()){
        setItem(request.card);
      }
      return this;
  }

  public Receipt setItem(TerminalInfo timfo){
    if(body!=null && timfo!=null){
      addLine("Register",timfo.getNickName());
    }
    return this;
  }

  public Receipt() {
    //you'd better not use it...til you set some items
  }

  public Receipt(PaymentReply reply, PaymentRequest request,
                 String merchrefprompt, TerminalInfo ti, ClerkIdInfo cii,
                 String timezonename, String timeformat) {
    setTimeFormat(timezonename, timeformat);
    setMerchantReferenceTag(merchrefprompt); // must go before request is set
    setItem(request);
    if(cii != null) {
      setItem(cii);
    }
    if(ti != null) {
      setItem(ti);
    }
    setItem(reply);
  }

  /**
   * @param pretty if true then don't show signature even if it exists,
   * false means we want a slot, if not 2nd copy
   */
  public Receipt manSigning(boolean pretty){
    signature=null;
    if(pretty){
      signable=false;  //leave signrequired alone, to get three states.
    } else {
      showSlot=true;
    }
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
    addLine("code", reply.status.Image());
    insertDate("when",UTC.Now());
    if((reply instanceof PaymentReply) && (reply.Errors.size() == 0)) {
      reply.Errors.add(((PaymentReply)reply).authMessage());
    }
    setItem(reply.Errors);
    hasreply=true;
    Section();
    signable=false;//-don't sign failures
    return this;
  }

  /**
   * used by web server to presize graphics area
   */
  public int totalLines() {
    int count = 0;
    count += FormattedLines.Sizeof(header);
    count += FormattedLines.Sizeof(body);
    count += FormattedLines.Sizeof(trailer);
    count += FormattedLines.Sizeof(cardHolder);
    if(!Hancock.NonTrivial(signature)){
      count += 1;
    }
    return count;
  }

  /**
   * can't think of a good name for this, sets section separator to indicate that the transaction was NOT good.
   */
  private void Scream(){
    section=new FormattedLineItem(" ERROR ", "*", '*',FormattedLineItem.centered);
    isFancy=false;
    signable=false;//don't sign on faults
  }

  boolean didErrors=false;

  /**
   * @param Errors is a block of error text to append to receipt body.
   */
  private Receipt setItem(TextList Errors){
    if ( !didErrors&& Errors.size()>0) {
      didErrors=true;
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

  private void addLine(String prefix,String value){//FUE
    body.add(FormattedLineItem.pair(prefix+":",value));
  }

  /**
   * add to receipt only if operands are non trivial
   * i.e. must have a human readable prompt AND value.
   */
  private void pickyAddLine(String prefix,String value){//FUE
    if(StringX.NonTrivial(value)&&StringX.NonTrivial(prefix)){
      addLine(prefix,value);
    }
  }

  private void Section(){//FUE
    body.add(section);
  }

  private boolean showauth(PaymentReply reply){
    String authcode=reply.Approval();
    if(StringX.NonTrivial(authcode)){
      addLine("Approval", authcode);
      return true;
    }
    return false;
  }

  private void showOriginalDetail(PaymentReply reply,String type){
    //output original tranny info:
    setItem("Amount "+type,reply.originalAmount);
    showauth(reply);
    setItem(reply.card);
    //since cardholder part of card info doesn't naturally print on voids we have to...
    body.add(cardHolder);
  }

  private Receipt showavs(AuthResponse auth){
    if (auth!=null&&auth.hasAVSResponse()){
      String prefix=StringX.bracketed("AVS[",auth.getAVS());
      String descrip=AVSDecoder.AVSmessage(banknet,auth.getAvsCode());
      addLine(prefix,descrip);
    }
    return this;
  }

  /*package*/ Receipt setItem(PaymentReply reply) {
    FormattedLines fl = new FormattedLines();
    String tdate= localTime(reply.refTime());

    addLine("Date", tdate);
    setItem(reply.Errors);
    hasreply=true;

    String tracer=reply.auth().authrrn();
    if(StringX.NonTrivial(tracer)){
      addLine("Reference#",tracer);
    }

    if( Txnid.isValid(reply.tref().txnId)){
      addLine("Trace#",String.valueOf(reply.tref().txnId));
    }

    if(reply.auth().isApproved()){
      switch(reply.transferType.Value()){ //add type specific values
        default:{
          switch(reply.payType.Value()) {
            case PayType.GiftCard: { // used for sale operations
              showauth(reply); //operation usually does NOT get authed.
              Section();
              addLine("Balance Remaining",reply.sv.balance.Image());
            } break;
            case PayType.Credit:{//may have an avs response
              showavs(reply.auth());
            }//join:
            default: {
              if(!showauth(reply)){
                addLine("Operation","Approved");
              }
            }
          }
        } break;

        case TransferType.Modify:{
          showOriginalDetail(reply,"Modified");
        } break;

        case TransferType.Reversal:{
          showOriginalDetail(reply,"Voided");
        } break;
      }
    }
    else { //declined or failed
      Scream();
      Section();
      body.add(reply.authMessage());
      Section();
      //show balance on decline but not failures.
      if(reply.auth().isDeclined() && reply.payType.is(PayType.GiftCard)){//used for sale operations
        PaymentReply gcr= (PaymentReply)reply;
        RealMoney balance=gcr.sv.balance;
        addLine("Balance Remaining",balance.Image());
        if(RealMoney.NonTrivial(gcr.shortage)){
          addLine("Short by",gcr.shortage.Image());
        }
        Section();
      }
    }
    Section();
    return this;
  }

  private void insertDate(String prefix,UTC fig){
    addLine(prefix,localTime(fig));
  }

  /*package*/ Receipt setItem(StoreConfig storeInfo, TerminalInfo termInfo, boolean online) {
    section=FormattedLineItem.winger("TERMINAL INFO");
    Section();
    addLine("Host",TheSinetSocketFactory.PreferredHost().toString());
    addLine("TermIds",   termInfo.toSpam());
    addLine("StoreInfo",  storeInfo.si.Name);
    addLine("si",        storeInfo.si.slim.spam());
    //add current stand in amoutns/counts.
    addLine(storeInfo.si.timeZoneName,localTime(UTC.Now()));//to show off formatting
    addLine("TermCaps",  storeInfo.termcap.toSpam());
    addLine("Build",     Revision.Version());
    signable=false;//special info printout, not a txn receipt.
    Section();
    return this;
  }

  public void printHeader() {
    lp.startPage();
    try {
      if(printWorthy()){
        if(header != null) {
          lp.print(dupline);
          lp.print(header);
        } else {
          dbg.VERBOSE("header == null; not printing header");
        }
      }
    } finally {
      lp.endPage();
    }
  }

  public void printBody() {
    if(body!=null){
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
  }

  private void printSig() {
    lp.startPage();
    dbg.VERBOSE("printWorthy="+printWorthy()+", signable="+signable+", Hancock.NonTrivial(signature)="+Hancock.NonTrivial(signature)+", showSignature="+showSignature);
    try {
      if(signable && printWorthy()){//if we are even printing
        if(signature==null){//last chance to catch a trivial signature
          showSlot=true;
        }
        if(showSlot){
          lp.print(sigSlot()); //manual signature slot
        } else if(showSignature){
          lp.print(signature);
        }
      }
    } catch(Exception ex) {
      dbg.Caught(ex);
    } finally {
      lp.endPage();
    }
  }

  public void printTrailer() {
    lp.startPage();
    try {

      if(printWorthy()){
        lp.print(dupline);
        if(trailer != null) {
          lp.print(trailer);
        } else {
          dbg.VERBOSE("trailer == null; not printing trailer");
        }
      }
    } finally {
      lp.endPage();
    }
  }

  public void signAndShove() {
    lp.startPage();
    try {
      if(printWorthy()){
        printSig();
        if(FormattedLines.Sizeof(cardHolder)>0){
          lp.print(cardHolder);//especially if NOT printing signature
        }
        if(isFancy){
          printTrailer();
        }
        lp.formfeed(); // REQUIRED !!!! to force abstract generators to produce output
      } else {
        dbg.VERBOSE("signAndShove(): not printworthy!");
      }
    } finally {
      lp.endPage();
    }

  }

  public void startPrint(PrinterModel alp,int copy){
    if(!ObjectX.NonTrivial(alp)) {
      alp = PrinterModel.Null() ;
      dbg.ERROR("PrinterModel was null! Set to Null().");
    }
    lp= alp;
    if(body==null){
      body=new FormattedLines();
    }
    if(lp==null){
      dbg.ERROR("lp is null despite our best efforts!");
      return;
    }
    bodylinecursor=0;
    dupline= (copy>0)? new FormattedLineItem("Duplicate #"+copy,'!'):null;

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

  public void print(PrinterModel lp,int copy) {//used by web access, copy==0
    startPrint(lp,copy);
    printBody();
    signAndShove();
  }

  public static Receipt Prettify(Receipt existing){
    if(existing!=null){
      Receipt prettyone=new Receipt();
      //components of a receipt
      prettyone.body       = existing.body;
      prettyone.signature  = null;
      prettyone.section    = existing.section;
      prettyone.dupline    = null;
      prettyone.cardHolder = existing.cardHolder;
      //individual receipt attributes
      prettyone.isFancy = true; //when true print header and footer logoish stuff
      prettyone.isWorthy= existing.isWorthy;//true for print at POS receipts
      prettyone.signable= false;//whether sig makes any sense.
      prettyone.hasreply= existing.hasreply;//whether some reply was included
      prettyone.showSlot= false; //whether this guy is manually signable
      prettyone.lp=       existing.lp;
      return prettyone;
    } else {
      return null;
    }
  }

  //////////////////////////////////////////////////////////////
  // These are for debugging and for resolving issues with orphaned receipts ...
  public TextList getHeaders() {
    return formattedLinesToTextList(header, true);
  }
  public TextList getTrailers() {
    return formattedLinesToTextList(trailer, true);
  }
  public TextList getBodys() {
    return formattedLinesToTextList(body, false);
  }
  // if justName is false, the it is justValue
  private TextList formattedLinesToTextList(FormattedLines fl, boolean justName) {
    TextList tl = new TextList();
    for(int i = 0; i< fl.size(); i++) {
      FormattedLineItem fli = fl.itemAt(i);
      tl.add(justName? fli.name : fli.value);
    }
    return tl;
  }


  //////////////////////////////////////////////////////////////
  public final FormattedLineItem timeline(String prefix, UTC utc){
    return FormattedLineItem.pair(prefix,localTime(utc));
  }

  public final void PrintBatchList(PrinterModel lp, BatchListingFormatter blif){
    dbg.Enter("PrintBatchList");//#gc
    lp.startPage();
    try {
      if(lp!=null){
        lp.print(header);
        lp.print(blif.header);
        lp.print(blif.body);
        if(blif.body.size()>0){//suppress trivial repetition of totals-only.
          //for fun and to bracket items more obviously:
//all customers who were asked complained about this line:        blif.header.reverse();
          lp.print(blif.header);
          lp.print(header);
        }
        lp.formfeed();
      }
    }
    finally {
      lp.endPage();
      dbg.Exit();//#gc
    }
  }

  public final void PrintStoreReply(PrinterModel lp, StoreReply reply){
    lp.startPage();
    try {
      lp.print(header);
      lp.print(FormattedLineItem.winger("Issue Store deposit"));
      lp.print(timeline("Received:",reply.refTime()));
      lp.print(reply.Errors); //wihtout special decoration, so that simple notes can be printed by server
      lp.formfeed();
    }
    finally {
      lp.endPage();
    }
  }

}
//$Id: Receipt.java,v 1.160 2004/02/26 16:34:51 mattm Exp $
