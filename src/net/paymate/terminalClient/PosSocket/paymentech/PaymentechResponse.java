package net.paymate.terminalClient.PosSocket.paymentech;


/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/PaymentechResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.lang.StringX;

public class PaymentechResponse extends AuthResponse {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PaymentechResponse.class);

  public PaymentechResponse(boolean fullHC) {
    this.fullHC = fullHC;
  }

  /**
   * convenience for Techulator
   */
  public static PaymentechResponse From(VisaBuffer vb, boolean fullHC) {
    PaymentechResponse newone=new PaymentechResponse (fullHC);
    newone.parse(vb);
    return newone;
  }

  /**
   * convenience for Techulator
   */
  public static PaymentechResponse From(byte []body, boolean fullHC) {
    return From(VisaBuffer.FrameThis(body),fullHC);
  }

  /**
   * Clear the data back to its default values
   */
  protected void clear() {
    super.clear();
    actionCode = ""; // A - approved, E - error
    batchNumber = -1; // not used
    ptrrn = -1; // see spec
    seqnum = -1;
    cardType = "";
    interchangeCompliance = "";
//    workingKey = "";
    optionalData = "";
  }

  boolean fullHC = false;
  // stuff we do nothing with ... public below are hack for PaymentechUTFormatter. We should probably relocate that class into this package....
  public String actionCode; // A - approved, E - error
  int batchNumber;
  int ptrrn;
  public int seqnum;
  String tracenumber;// This is what goes into the authtracedata, regardless of where it comes from
  String cardType;
  String interchangeCompliance;
  String workingKey; //alh: this is master session stuff and will always be null. However its frame should always be present
  String optionalData;

  protected PaymentechResponse parse(VisaBuffer vb){//onluy suitable for authorization requests
    dbg.VERBOSE("parsing response");
    if(vb!=null){
//      vb.dump("PT response");
      if(vb.isComplete()){
        rawresponse.setrawto(vb.body());
        vb.parserStart();
// +++ can this code parse HC as well as TC???
        actionCode=vb.getFixed(1);                             // 2 Action Code
        addressVerificationResponseCode=vb.getByte();        // 3 Address Verification Response Code
        authcode=vb.getFixed(6);                               // 4 Auth/Error Code
        batchNumber=StringX.parseInt(vb.getFixed(6));             // 5 Batch Number
        ptrrn=StringX.parseInt(vb.getFixed(8));// 6 Retrieval Reference Number  // Needs to be recorded !!!
        PaymentechLastRRN.LASTretrievalReferenceNumber = ptrrn; // +++ @@@ Issues of chicken and egg here
  //was actually part of following      vb.getFixed(3); // trash                               // 7 Filler
        seqnum= StringX.parseInt(vb.getFixed(6));                 // 8 Sequence Number
        authmsg=vb.getFixed(32);                               // 9 Response Message
        cardType=vb.getROF();                                  // 10 Card Type
        boolean issvc=cardType.equals("SV");
        boolean isdb=cardType.equals("DB");
        if(issvc || isdb){
          tracenumber=vb.getROF(); //supposedly X(8).
        } else {
          String interchangeCompliance=vb.getROF();                     // 12 Interchange Compliance
          dbg.VERBOSE("interchangeCompliance="+interchangeCompliance);
          parseInterchangeComplianceData();
        }
        if(fullHC) { // for full HC
          authrrn = String.valueOf(ptrrn); // gcrrn goes into the authrrn
          tracenumber = authrrn;           // gcRRN goes into the authtracedata
        } else {
          authrrn = tracenumber; // for TC (including GC and DB), put the trace in the authrrn
// +++ @@@@ %%% was this never finished?  Where is the rest of this?  Does it work?
          if(issvc) { // for TCGC, put the ptrrn into the

          } else {

          }
        }
        //@todo: on debit extract the surcharge fee and get that into the reply and out to the client for the receipt
        authTraceData = new PTAuthTraceData(cardType, vb.getROF(), tracenumber);// 10 Card Type + 14 Authorizing Network ID  & 15 Authorization Source + tracenumber
dbg.WARNING("Setting authTraceData to [" + authTraceData.fullImage() + "].");
        workingKey=vb.getROF();                                // 17 Working Key
        if(issvc){
          vb.getROF();
          vb.getROF();
          String bindicator=vb.getFixed(2);
dbg.WARNING("bindicator was ["+bindicator+"].");
          String bal = vb.getROF();
dbg.WARNING("bal was [" + bal + "].");
          long ball = (long)(100.0*StringX.parseDouble(bal));
dbg.WARNING("ball was [" + ball + "].");
          acctBalance.setto(ball);
          //B4 is actual amount deducted. B3 seems to be amount transferred.
        } else {
          optionalData=vb.getROF();                              // 19 Optional Data
        }
        // toss the rest aside (although it would be nice to pull it all out into one large field with EOFrames marked with ^, and print it).
        //now interpret for parent class:
        if(actionCode.startsWith("A")) {
          action= ActionCode.Approved;
        } else {//--- some instances should be failures rather than declines
          // Everything is declined since we don't trust the auth's numbers to stay stable.
          // we only return failed if we are going to retry later and want to stand it in.
          action = ActionCode.Declined;
        }
      } else {
        rawresponse.setrawto(new String(vb.packet())); // +++ packet() or body()???
        vbnotcomplete();
      }
    } else {
      action= ActionCode.Failed;
      authmsg="timeout/null response";
    }
    return this;
  }

/*
// +++ @@@ %%%
VISA CPS stuff - PT [put into txn!]
ATxnid----------vc--a-mcc-p-c*amount---a
C               ao  r eao e c          r
I               ld  c rtd m t          c
E0821444476154273XJV00581290  000002.58
                            ^^         ^
*/
  protected void parseInterchangeComplianceData() {
    if(StringX.NonTrivial(interchangeCompliance)) {
      if(interchangeCompliance.length() > 39) {
        dbg.VERBOSE("This is likely Visa CPS.");
        VisaCPSdata vscpsdata = new VisaCPSdata();
        vscpsdata.cpsaci = ""+interchangeCompliance.charAt(0);
        vscpsdata.cpstxnid    = StringX.subString(interchangeCompliance,  1, 15); // +++ @@@ %%% check it !!!
        vscpsdata.cpsvalcode  = StringX.subString(interchangeCompliance, 16,  4); // +++ @@@ %%% check it !!!
        vscpsdata.cpsrespcode = StringX.subString(interchangeCompliance, 20,  2); // +++ @@@ %%% check it !!!
        cpsdata = vscpsdata;
        // +++ what to do with the rest of it?
        interchangeCompliance = StringX.subString(interchangeCompliance, 22);
      } else {
        dbg.VERBOSE("This is likely MCIC.");
        MastercardCPSdata mccpsdata = new MastercardCPSdata();
        // toss -> ACI                      PIC X (1) See Appendix A
        //  BankNet Reference Number PIC X (9) Reference number assigned by MasterCard to each authorization message.
        mccpsdata.referenceNumber = StringX.subString(interchangeCompliance,  1, 9); // +++ @@@ %%% check it !!!
        //  BankNet Date             PIC 9 (4) MMDD
        mccpsdata.date            = StringX.subString(interchangeCompliance, 10, 4); // +++ @@@ %%% check it !!!
        cpsdata = mccpsdata;
        // toss -> the rest
        //  CVC Error Indicator      PIC X (1) Y = CVC is incorrect, N = CVC is okay
        //  CVC Status Change        PIC X (1) Y = MasterCard changed POS only mode to a ‘02’ from an ‘09’. Member is in monitoring mode. N = POS entry mode was not changed.
        //  Magnetic Stripe Quality  PIC X (1) Code indicating an error existed in the original authorization data by MasterCard.
        //  Merchant Category Code   PIC 9 (4) SIC code
        // +++ what to do with the rest of it?
        interchangeCompliance = StringX.subString(interchangeCompliance, 22);
      }
    } else {
      dbg.VERBOSE("No interchangeCompliance data!");
    }
  }

  /**
   * parses packet into internal fields.
   */
  public void process(Packet toFinish){
    // let the cast blow.  If it does, you coded things incorrectly
    parse((VisaBuffer)toFinish);
  }

  /**
   * added for Techulator what follows is from a live trace, spaces added for calrity
   *  A [sp] 199096 318001 00000003 000 000
   *  APPROVED[sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp][sp]
   *  MC[FS] A00019909611140[sp]05969[FS]
   *  02[sp][FS]
   *  0E646F2366DF8DBD[FS]
   */
  public VisaBuffer pack(){
    VisaBuffer vb = VisaBuffer.NewSender(257);//+_+ who is the owner/source of this size???
    vb.append(isApproved()?'A':'E');//  2 Action Code                        Pic X (1)       X A Approved (requested transaction was successful) E Error
    vb.append(addressVerificationResponseCode); //  3 Address Verification Response Code Pic X (1)
    vb.appendAlpha(6,authcode());//  4 Auth/Error Code                    Pic X (6)       X Authorization Code. If an error transaction,
    vb.appendInt(6,batchNumber);//  5 Batch Number                       Pic 9 (6)       X The Host sends back all zeros.
    vb.appendInt(8,ptrrn);//  6 Retrieval Reference Number         Pic 9 (8)       X 00000000 This is for future use
    vb.appendInt(3,0);        //  7 Filler                             Pic 9 (3)       X 000 always
    vb.appendInt(3,seqnum);//  8 Sequence Number                    Pic 9 (3)       X This field is echoed from the Sale or Authorization transaction.
    vb.appendAlpha(32,authmsg);  //  9 Response Message                   Pic X (32)      X Approval/Decline/Error text message information.
    vb.appendAlpha(2,cardType);//  10 Card Type                         Pic X (2)       X See Card Type (BIN) Table for valid mnemonics.
    vb.endFrame();//  11 FS                                Pic X (1)       X 1Ch
    vb.appendFrame(interchangeCompliance); //  12 Interchange Compliance            Pic X (40)        Applies ONLY to Visa, MasterCard, and Visa Commercial Card transactions.,  13 FS
    try {
      vb.appendFrame(((PTAuthTraceData)authTraceData).networkAndSource);//  14 Authorizing Network ID            Pic 9 (2)       X See Appendix A for applicable codes.//  15 Authorization Source              Pic X (1)       X See Appendix A for applicable codes.//  16 FS                                Pic X (1)       X 1Ch
    }
    catch (Exception ex) {
      dbg.WARNING("while packing net and source ignored:"+ex);
      vb.endFrame();
    }
    vb.appendFrame(workingKey);//17 (useless) working key  18 FS                                Pic X (1)       X 1Ch
    vb.appendFrame(optionalData);//  19 Optional Data                     Pic X (120)       User defined data, optional and variable see Note 1
    vb.end();
    return vb;
  }


  // called by the super.toString()
  public TextList toSpam(TextList tl){
    tl=super.toSpam(tl);
    tl.add("actioncode",actionCode);
    tl.add("addressVerificationResponseCode",(char)addressVerificationResponseCode);
    tl.add("batchNumber",batchNumber);
    tl.add("ptrrn",ptrrn);
    tl.add("authrrn",authrrn);
    tl.add("seqnum",seqnum);
    tl.add("cardType",cardType);
    tl.add("tracenumber",tracenumber);
    tl.add("acctBalance", acctBalance);
    tl.add("interchangeCompliance",interchangeCompliance);
    tl.add("useless workingKey",workingKey);
    tl.add("optionalData",optionalData);
    tl.add("authtracedata", authTraceData);
    return tl;
  }

  static public void main(String[] args) {
    try {
      PaymentechResponse pr = new PaymentechResponse(false);
      VisaBuffer vb = VisaBuffer.NewReceiver(255);
      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(args[0]));
      byte [] bytes = reader.readLine().getBytes();
      for(int thebyte = 0; thebyte < bytes.length; thebyte++) {
        vb.append(bytes[thebyte]);
      }
      pr.parse(vb);
      System.out.println("SPAM:\n"+pr);
    } catch (Exception t) {
      System.out.println("ex: " + t);
    }
  }


  /* suggested error codes
210 Invalid Term No The merchant ID is not valid or active.
214 Call Voice Oper Authorization center cannot be reached.
217 Over Credit Flr Amount requested exceeds credit limit. (server standin)
218 Request Denied Transaction is not valid for this authorizer.
220 Not Online to XX Fatal communications error.
292 Auth Down - Retry Authorizer is not responding
293 Auth Busy - Retry Authorizer not available at this time.
294 Auth Busy - Retry Authorizer not available at this time
297 Auth Error - Retry Authorizer not available at this time

420 Amount Too Large Maximum sale amount exceeded.
421 Amount Too Large Maximum return amount exceeded.
429 Rev Not Allowed The batch containing the transaction to void has been released.
602 Call Voice Op Auth center cannot be reached.

  */

}

//  # Field Description                  Format      Req'd Comments
//  1 STX                                Pic X (1)       X 02h
//  2 Action Code                        Pic X (1)       X A Approved (requested transaction was successful) E Error
//  3 Address Verification Response Code Pic X (1)       X If a space is returned, then the AVS service was not performed.
//                                                         Visa, MasterCard, American Express and Discover have their own listing
//                                                         of responses. See Appendix A for all applicable codes.
//  4 Auth/Error Code                    Pic X (6)       X Authorization Code. If an error transaction,
//                                                         see the ERROR MESSAGE section for further description.
//  5 Batch Number                       Pic 9 (6)       X The Host sends back all zeros.
//                                                         The Host does not store or maintain the batch number.
//                                                         The terminal must maintain and send the current batch number
//                                                         at time of deposit.
//  6 Retrieval Reference Number         Pic 9 (8)       X 00000000 This is for future use
//  7 Filler                             Pic 9 (3)       X 000 always
//  8 Sequence Number                    Pic 9 (3)       X This field is echoed from the Sale or Authorization transaction.
//  9 Response Message                   Pic X (32)      X Approval/Decline/Error text message information.
//  10 Card Type                         Pic X (2)       X See Card Type (BIN) Table for valid mnemonics.
//  11 FS                                Pic X (1)       X 1Ch
//  12 Interchange Compliance            Pic X (40)        Applies ONLY to Visa, MasterCard, and Visa Commercial Card transactions.
//                                                         This field is for reference purposes only.
//                                                         (See Chapter 2, section 2.3.1 and 2.3.2 for more details.).
//  13 FS                                Pic X (1)       X 1Ch
//  14 Authorizing Network ID            Pic 9 (2)       X See Appendix A for applicable codes.
//  15 Authorization Source              Pic X (1)       X See Appendix A for applicable codes.
//  16 FS                                Pic X (1)       X 1Ch
//  17 Working Key                       Pic X (16)        Optional: The field, Working Key, is present only when Debit or EBT
//                                                         is supported by the merchant and Master Session PIN Processing is used.
//  18 FS                                Pic X (1)       X 1Ch
//  19 Optional Data                     Pic X (120)       User defined data, optional and variable see Note 1

