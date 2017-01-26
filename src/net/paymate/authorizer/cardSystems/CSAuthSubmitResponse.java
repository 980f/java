package net.paymate.authorizer.cardSystems;

import net.paymate.authorizer.*; // AuthSubmitResponse
import net.paymate.util.*; // ErrorLogStream
import net.paymate.data.*; // ActionCode
import net.paymate.lang.StringX;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CSAuthSubmitResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.15 $
 */

public class CSAuthSubmitResponse extends AuthSubmitResponse {

  // Merge similarities with MaverickResponse and PTAuthSubmitResponse +++

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CSAuthSubmitResponse.class, ErrorLogStream.VERBOSE);

  public Packet reply = null; // make one of appropriate type before each use.

  /* package */ boolean wasAckNaked = false;
  /* package */ boolean ack = false;

/*
  Use these from the AuthResponse
    actioncode
    authrespmsg
*/

  public CSAuthSubmitResponse() {
  }

  /**
   * Clears it for reuse,
   * must leave reply packet alone!!!
   */
  /* package */ final void reset() {
    super.clear();
//    completed = false;
    wasAckNaked = false;
    ack = false;
    action=ActionCode.Unknown;
//    addressVerificationResponseCode="";
    authcode="";
    batchNumber=0;
    retrievalReferenceNumber=0;
    authmsg="";
  }

/*
Batch Settlement Response Record
Field                    Data
# Name              Format Len Section Example
1 Record Format          A   1 6.41    "U" = CardSystems' Proprietary Batch Upload Record Format.
2 Application Type       A   1 6.42    2 = Multi-tran.
3 Message Delimiter      A   1 6.43    "." or HEX 2E
4 Acquirer Bin           N   6 6.44    Visa assigned Bank ID number. eg:"123456"
5 Unused Field           A  12 6.45    This field is currently not in use.
6 Terminal Number        N  10 6.46    Unique Terminal ID assigned by CardSystems. eg:"9999999901"
7 Field Separator        A   1  6.6    <FS> or HEX 1C
8 Batch Number           N   4 6.47    Returns the same number sent in the batch upload record. eg:"1234"
9 Batch Response Data
   or
  Autoload Response Data A var 6.48    Batch Response Data is sent after all transactions have been uploaded. Autoload Response Data is sent to an Autoload Request Message.
*/

/*
Batch Response Data
Field                         Data
### Name                 Format Len Section Example
R1  Approval Display Message  A  16 6.48    "APP 10.00"
    “APP ZZZZZZZZZZ9.99” for net amount if approved.
    “OUT OF BALANCE” if the batch is out of balance.
    “FORMAT ERROR” if one of the transactions fails to pass the edits.
R2  Field Separator           A   1  6.6    <FS> or HEX 1C
R3  Reference Display Message A  16 6.49    “REF 123456789012” if batch was approved return a batch reference nbr message, otherwise return a second error message or spaces.
R4  Field Separator           A   1  6.6    <FS> or HEX 1C
R5  Response Type             A   1 6.50    "A"
    A = Host Approval
    N = Balance Error
    E = Edit Error
R6  Field Separator           A   1  6.6    <FS> or HEX 1C
R7  Autoload Indicator        A   1 6.51    "Y" if autoload is needed, "N" if no autoload needed.
R8  Field Separator           A   1  6.6    <FS> or HEX 1C
R9  Batch Reference Number    A  12 6.52    Same number as in field R3. To be printed on the batch settlement report. eg:"123456789012"
R10 Field Separator           A   1  6.6    <FS> or HEX 1C
R11 Local Timestamp           N  12 6.53    Return local date & time in format YYMMDDHHMMSS. eg:"9906010800"
*/


//@@@ what was the intent of the shared part of wasGood??
//we can replace the following three by a switch on packet type.
  private boolean generallyGood() {
    int authcodenum = StringX.parseInt(authcode());//parsed but never used...
    return true;
  }

  public boolean headerGood() {
    return generallyGood() && reply.isComplete(); // should be an acknak packet
  }

  public boolean detailGood(boolean isLast) {
    return generallyGood() && (isLast ? isApproved() : reply.isComplete());
  }

//  String addressVerificationResponseCode = "";
  int batchNumber = -1;
  int retrievalReferenceNumber = -1;

  private CSAuthSubmitResponse parse(VisaBuffer vb) {
    dbg.VERBOSE("parsing response");
    if(vb!=null){
      if(vb.isComplete()){
        vb.parserStart();

/*
1 Record Format          A   1 6.41    "U" = CardSystems' Proprietary Batch Upload Record Format.
2 Application Type       A   1 6.42    2 = Multi-tran.
3 Message Delimiter      A   1 6.43    "." or HEX 2E
4 Acquirer Bin           N   6 6.44    Visa assigned Bank ID number. eg:"123456"
5 Unused Field           A  12 6.45    This field is currently not in use.
6 Terminal Number        N  10 6.46    Unique Terminal ID assigned by CardSystems. eg:"9999999901"
7 Field Separator        A   1  6.6    <FS> or HEX 1C
8 Batch Number           N   4 6.47    Returns the same number sent in the batch upload record. eg:"1234"
9 Batch Response Data    A var 6.48    Batch Response Data is sent after all transactions have been uploaded. Autoload Response Data is sent to an Autoload Request Message.

Batch Response Data
Field                         Data
### Name                 Format Len Section Example
R1  Approval Display Message  A  16 6.48    "APP 10.00"
    "APP ZZZZZZZZZZ9.99" for net amount if approved.
    "OUT OF BALANCE" if the batch is out of balance.
    "FORMAT ERROR" if one of the transactions fails to pass the edits.
R2  Field Separator           A   1  6.6    <FS> or HEX 1C
R3  Reference Display Message A  16 6.49    "REF 123456789012" if batch was approved return a batch reference nbr message, otherwise return a second error message or spaces.
R4  Field Separator           A   1  6.6    <FS> or HEX 1C
R5  Response Type             A   1 6.50    "A"
    A = Host Approval
    N = Balance Error
    E = Edit Error
R6  Field Separator           A   1  6.6    <FS> or HEX 1C
R7  Autoload Indicator        A   1 6.51    "Y" if autoload is needed, "N" if no autoload needed.
R8  Field Separator           A   1  6.6    <FS> or HEX 1C
R9  Batch Reference Number    A  12 6.52    Same number as in field R3. To be printed on the batch settlement report. eg:"123456789012"
R10 Field Separator           A   1  6.6    <FS> or HEX 1C
R11 Local Timestamp           N  12 6.53    Return local date & time in format YYMMDDHHMMSS. eg:"9906010800"
*/
        vb.getROF(); // skip the header
        vb.getFixed(4); // throw away the next 4
        // +++ @@@ the rest might or might not be there !!!
        try {
          authmsg=vb.getFixed(16);
          vb.getFixed(18); // throw away up until the actioncode
          action = String.valueOf((char)vb.getByte());
          if(StringX.equalStrings(action, "N")) {
            action = ActionCode.Declined;
          }
          vb.getFixed(3); // throw away up until refnumber
          retrievalReferenceNumber=StringX.parseInt(vb.getFixed(12)); // +++ @@@ Needs to be recorded !!!
          // the rest is trash
        } catch (Exception e) {
          dbg.Caught(e);
          action = ActionCode.Failed;
        }
      } else {
        vbnotcomplete();
      }
    }
    return this;
  }

  public void process(Packet toFinish){
  //what about U2 packets???
    wasAckNaked = false;
    if (toFinish instanceof VisaBuffer) {
      VisaBuffer vb = (VisaBuffer)toFinish;
      if(vb.wasAckNaked()) {
        wasAckNaked = true;
        ack = toFinish.isOk();
        if(ack){
          markApproved("ACKED");
        } else {
          markDeclined("NAKED");
        }
      } else {
        parse(vb);
      }
    } else {
      dbg.ERROR("Might have been a nullpacket receiver.");
    }
  }

  public String packetImage() {//diags use only
    return new String(Ascii.image(reply.packet()));
  }

}
//$Id: CSAuthSubmitResponse.java,v 1.15 2003/07/27 05:34:50 mattm Exp $