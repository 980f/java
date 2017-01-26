package net.paymate.authorizer.cardSystems;

import net.paymate.authorizer.*; // base class
import net.paymate.data.*; // Authid
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.*; // ErrorLogStream
import net.paymate.jpos.data.*; // TrackData
import net.paymate.lang.StringX;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CSAuthSubmitRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.27 $
 */

public class CSAuthSubmitRequest extends AuthSubmitRequest implements CardSystemConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CSAuthSubmitRequest.class);

  // +++ Merge similarities with PTAuthSubmitRequest and MaverickRequest

  public CSAuthSubmitRequest(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    super(authid, terminalid, merch);
  }
  protected int maxRequestSize() {
    return MAXREQUEST;
  }

  private VisaBuffer req=makeRequest();

  private final VisaBuffer makeRequest() {
    return VisaBuffer.NewSender(maxRequestSize()).setClipLRC(); // CS doesn't use LRC!
  }

  // set these before using
  /* package */ Accumulator salesOnly = new Accumulator();
  /* package */ Accumulator returnsOnly = new Accumulator();
  /* package */ Accumulator voidsOnly = new Accumulator();

/*
Batch Settlement Record

 Field                     Data
# Name                Format Length Section
1 Record Format            A   1        6.1 "U" = CardSystems' Proprietary Batch Upload Record Format
2 Application Type         A   1        6.2 2 (multi-tran)
3 Record Delimiter         A   1        6.3 "."
4 Acquirer Bin             N   6        6.4 Visa-assigned Bank ID Number, eg: "123456" (6 digit BIN)
5 Terminal Number          N  10        6.5 Unique Terminal ID assigned by CardSystems, eg: "9999999901"
6 Field Separator          A   1        6.6 Field Separator, eg: <FS> or HEX 1C
7 Batch Number             N   4        6.7 Incremented from 1 to 9999 with each settled batch, eg: "0001"
8 Batch Header Data
   or
  Batch Detail Data
   or
  Autoload Request Message A 210 (max.) 6.8
*/

  /* package */ void prefix(boolean lastone) {
    req=makeRequest();
//    req.start(maxRequestSize());
    req.append(lastone ? BATCHUPLOADLASTRECORD : BATCHUPLOADMORERECORDS); // 1-3: U#.
    req.appendNumeric(6, records.terminalid); // 4: acquirer bin, but we can use whatever we want, and we will use the terminalid
    req.appendNumeric(10, merch.authtermid); // 5: Terminal Number
    req.endFrame();
    req.appendInt(4, termbatchnum);
  }

/*
Batch Header Data
  Field                    Data
### Name               Format  Len Section
H1  Transaction Code        A    2 6.9     10-Settlement:Batch Header, EG: "10"
H2  Batch Changed Indicator A    1 6.10    We always do this: "N" so that we can send details
H3  Field Separator         A    1  6.6    <FS> or HEX 1C
H4  Application Type        A    7 6.11    "VT3RL01"
H5  Application Version     N    2 6.12    01
H6  Eprom Version           A 0-10 6.13    "3E2EU3.70"
H7  Field Separator         A    1  6.6    <FS> or HEX 1C
H8  Batch Sales Amount      N 0-12 6.14    Total amount of all sales transactions. Implied decimal. eg: "100000"
H9  Field Separator         A    1  6.6    <FS> or HEX 1C
H10 Batch Sales Count       N  0-7 6.15    Total number of all sales transactions. eg: "10"
H11 Field Separator         A    1  6.6    <FS> or HEX 1C
H12 Batch Returns Amount    N 0-12 6.16    Total amount of all returns transactions. Implied decimal. eg: "1000"
H13 Field Separator         A    1  6.6    <FS> or HEX 1C
H14 Batch Returns Count     N  0-7 6.17    Total number of all returns transactions. eg: "10"
H15 Field Separator         A    1  6.6    <FS> or HEX 1C
H16 Void Amount             N 0-12 6.18    Unsigned net total amount of all void transactions. Implied decimal. eg: "1000"
H17 Field Separator         A    1  6.6    <FS> or HEX 1C
H18 Void Count              N  0-7 6.19    Total number of all void transactions. eg: "10"
H19 Field Separator         A    1  6.6    <FS> or HEX 1C
H20 Free Form Data          A 0-45 6.20    Merchant Defined. Optional field.
H21 Record Separator        A    1 6.21    <RS> or HEX 1E // "The Record Separators are for sending in mulitple records per line which you are not doing." - BB
H22 Batch Detail Data       A  var 6.22    Required when field H21 is present. See Batch Detail Data Definition below.
*/

  /* package */ byte [] header() {
    prefix(false);
    req.appendFrame("10N"); // TransactionCode, Batch Changed Indicator: H1-H3
    //the N above is per CS rules, ignore the manual
    //following is spec'd as 7 alpha and 2 numeric
    req.appendFrame("       00"); // System info: H4 - H7
    // sales
    CardSystemsAuth.AmountInfo(req,salesOnly.getTotal());
//    req.endFrame(); // end of this chunk
    CardSystemsAuth.AmountInfo(req,salesOnly.getCount());
//    req.endFrame(); // end of this chunk
    // returns
    CardSystemsAuth.AmountInfo(req,returnsOnly.getTotal());
//    req.endFrame(); // end of this chunk
    CardSystemsAuth.AmountInfo(req,returnsOnly.getCount());
//    req.endFrame(); // end of this chunk
    // voids
    CardSystemsAuth.AmountInfo(req,voidsOnly.getTotal());
//    req.endFrame(); // end of this chunk
    CardSystemsAuth.AmountInfo(req,voidsOnly.getCount());
//    req.endFrame(); // end of this chunk
    // batchid info
    req.appendFrame(String.valueOf(batchid)); // Free Form Data
//    req.endRecord(); // "The Record Separators are for sending in mulitple records per line which you are not doing." - BB
    // add the details in a separate send ...
    req.end();
    dbg.ERROR("isOk() returned " + req.isOk());
    return req.packet();
  }

/*
Batch Detail Data
Field                        Data
### Name                  Format Len   Section
D1  Transaction Code           A     2 6.24
    01   Settlement : Purchase
    02   Settlement : Cash Advance
    03   Settlement : Mail Order
    04   Settlement : Force (Off-Line Auth)
    05   Settlement : Credit Return
    10   Settlement : Batch Header
    V1   Settlement : Voided Purchase
    V2   Settlement : Voided Cash Advance
    V3   Settlement : Voided Mail Order
    V4   Settlement : Voided Force
    V5   Settlement : Voided Credit
D2  Account Data Source Code   A     1 6.25
    D    Full Mag-stripe read and transmit, Track 2
    H    Full Mag-stripe read and transmit, Track 1
    T    Manually keyed, Track 2 capable
    X    Manually keyed, Track 1 capable
    @    Manually keyed, no card reading capability
D3  Account Number             A 13-19 6.26    Cardholder Account Number, variable length. eg:"4999999999999"
D4  Field Separator            A     1  6.6    <FS> or HEX 1C
D5  Expiration Date            N     4 6.27    Cardholder Account Number Expiration date in format MMYY. eg:"1099"
D6  Field Separator            A     1  6.6    <FS> or HEX 1C
D7  Transaction Date & Time    N    10 6.28    Transaction date and time in format YYMMDDHHMM. eg:"9906010800"
D8  Field Separator            A     1  6.6    <FS> or HEX 1C
D9  Approval Code              A     6 6.29    Approval code returned in the authorization response record. eg:"123456"
D10 Field Separator            A     1  6.6    <FS> or HEX 1C
D11 Transaction Amount         N  0-12 6.30    Total amount of transaction, including tip and sales tax. Implied decimal. eg:"10000"
D12 Field Separator            A     1  6.6    <FS> or HEX 1C
D13 Tax Amount                 A  0-12 6.31    Any sales tax amount, implied decimal. See note in field D21 (section 6.37). Optional field. eg:"700"
D14 Field Separator            A     1  6.6    <FS> or HEX 1C
D15 Retrieval Reference Number A 0or12 6.32    The Retrieval Reference Number returned in the authorization response record. eg:"123456789101"
D16 Field Separator            A     1  6.6    <FS> or HEX 1C
D17 Industry Type              A  0or1 6.33    "R"
    "A"  Auto Rental
    "H"  Hotel
    "R"  Retail/Restaurant
D18 Industry Specific Data     A  0-60 6.34    Sub-fields follow. See 6.33 (field D17)
    Tip Amount                 N    12 6.34    Tip Amount (implied decimal, right justified, zero filled). eg:"000000000500"
    Server Number              A     8 6.34    Employee Number (left justified, space filled). eg:"12345678"
    Ticket Number              A  0-12 6.34    Ticket Number. eg:"R47809T4"
D19 Field Separator            A     1  6.6    <FS> or HEX 1C
D20 Card Type                  A  0or1 6.35    Optional [V or M]. eg:"V"; per Barb, can leave off
D21 Card Specific Data         A  1-40 6.36    per Barb, can leave off
D22 Field Separator            A     1  6.6    <FS> or HEX 1C
D23 Free Form Data             A  0-45 6.37    Merchant Defined Data. Optional Field.
D24 Record Separator           A     1 6.38    <RS> or HEX 1E // "The Record Separators are for sending in mulitple records per line which you are not doing." - BB
*/

  /* package */ byte [] detail(boolean lastone) {
    // add up the count and total; Everything is positive except for returns and VOIDS, which are negative. add more?
    int rawamount = StringX.parseInt(records.amount);//@authorsettle?; auth.  we don't allow mods in this auther
    // start making it
    prefix(lastone);
    req.endFrame();
    // presume that we already filtered out the voids and voideds, etc.
    // 01 Settlement : Purchase
    // 05 Settlement : Credit Return
    req.appendInt(2, (records.isReturn()) ? 5 : 1); // return : sale
    MSRData card = records.card();
    CardSystemsAuth.source(req, card.track(MSRData.T1).Data(), card.track(MSRData.T2).Data(), true, records.card());
    // Per Barbara, the timezone used for the date/time stamp of the txn should be the timezone of the store
    LocalTimeFormat ltf = LocalTimeFormat.New(merch.tz(), "yyMMddHHmm");
    req.appendNumeric(10, ltf.format(UTC.New(records.transtarttime))); // Transaction date and time in format YYMMDDHHMM
    req.endFrame();
    req.appendAlpha(6, records.approvalcode);
    req.endFrame();
    // +++ if we add preauth, etc., we need to be able to change the settle amount.
    CardSystemsAuth.AmountInfo(req,records.rawSettleAmount()); // sale
    req.endFrame(); // tax (who knows?=> for Purchasing Cards)
    if(StringX.NonTrivial(records.authrrn)) {
      req.appendAlpha(12, records.authrrn); // RRN - left-justified
    }
    req.endFrame();
    req.endFrame(); // 18&19 (optional and not used)
    req.endFrame(); // 20-22 (optional and not used)
    // txnid info
    req.append(records.txnid); // Free Form Data
    req.end();
    dbg.ERROR("isOk() returned " + req.isOk());
    return req.packet();
  }

  // +++ DO NOT add amounts here.  instead, all authsubmitrequests (base object) contain
  // a sales and a returns accumulator.  they are added (or subtracted) together for a grand total
  // only in the code that needs them, including the reporting code
  public void addToTotals(TxnRow record) {
    int rawamount = StringX.parseInt(record.amount);//@authorsettle?; auth.  we don't allow mods in this auther
    if(record.isReturn()) {
      returnsOnly.add(rawamount);
      amounts.add(rawamount * -1); // can't do remove, as that decreases the COUNT!
    } else {
      salesOnly.add(rawamount);
      amounts.add(rawamount);
    }
  }
}
//$Id: CSAuthSubmitRequest.java,v 1.27 2003/11/02 07:59:08 mattm Exp $