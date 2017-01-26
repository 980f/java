package net.paymate.authorizer.paymentech;

import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.data.*; // ActionCode
import net.paymate.lang.StringX;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PTAuthSubmitResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.17 $
 */

public class PTAuthSubmitResponse extends AuthSubmitResponse {

  // +++ combine with PaymentechResponse +++

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PTAuthSubmitResponse.class, ErrorLogStream.VERBOSE);

  public Packet vb = null; // set before using

/*
  Use these from the AuthResponse
    actioncode
    authrespmsg
*/

  public PTAuthSubmitResponse() {
  }

  /**
   * Clears it for reuse
   */
  /* package */ final void reset() {
    super.clear();
//    completed = false;
    action=ActionCode.Unknown;
    addressVerificationResponseCode="";
    authcode="";
    batchNumber=0;
    retrievalReferenceNumber=0;
    seqnum= 0;
    authmsg="";
  }

  /* package */ static final boolean WASHEADER = true;
  /* package */ static final boolean WASNOTHEADER = false;

  public boolean wasGood(boolean wasHeader) {
    int authcodenum = StringX.parseInt(authcode());
    if(wasHeader) {
      if(isApproved()) {
        return (authcodenum == 901);
      } else if(isDeclined() /* should never happen */) {
        return false;
      } else { // assume it is an error; try to send the detail anyway
        return true;
      }
    } else { // it is a final message
      return (isApproved() && (authcodenum == 0));
    }
  }

  String addressVerificationResponseCode = "";
  int batchNumber = -1; // not used
  int retrievalReferenceNumber = -1; // for GiftCard voids
  int seqnum = -1;

  private PTAuthSubmitResponse parse(VisaBuffer vb) {
    dbg.VERBOSE("parsing response");
    if(vb!=null){
      if(vb.isComplete()){
        vb.parserStart();
        action=String.valueOf((char)vb.getByte()); // 2 Action Code
        addressVerificationResponseCode=vb.getFixed(1);        // 3 Address Verification Response Code
        authcode=vb.getFixed(6);                               // 4 Auth/Error Code
        batchNumber=StringX.parseInt(vb.getFixed(6));             // 5 Batch Number
        retrievalReferenceNumber=StringX.parseInt(vb.getFixed(8));// 6 Retrieval Reference Number  // +++ @@@ Needs to be recorded !!!
        seqnum= StringX.parseInt(vb.getFixed(6));                 // 8 Sequence Number
        authmsg=vb.getFixed(32);                               // 9 Response Message
        // the rest is trash
/*
10 FS Pic 9(1) REQ'D 1Ch
11 Download Flag Pic X(1) REQ'D 0 = None
12 Multi Message Flag Pic X(1) REQ'D N = Last Message
13 Batch Open Date/Time Pic 9(10) REQ'D MMDDYYHHMM
14 Batch Close Date/Time Pic 9(10) REQ'D MMDDYYHHMM
15 Batch Transaction Count Pic 9(6) REQ'D 000000 = The host will always return all zeros.
16 Batch Net Amount Pic X(10) REQ'D 00.00 = The host will always return 00.00.
*/
      } else {
        vbnotcomplete();
      }
    }
    return this;
  }


  public void process(Packet toFinish){
    // let the cast blow.  If it does, you coded things incorrectly
    VisaBuffer vb = (VisaBuffer)toFinish;
    parse(vb);
  }

  public PTAuthSubmitResponse From(byte [] body){
    PTAuthSubmitResponse newone= new PTAuthSubmitResponse();
    VisaBuffer vb = VisaBuffer.NewReceiver(200);
    vb.append(body);
    vb.end();
    newone.parse(vb);
    return newone;
  }


  public String packetImage() {
    return String.valueOf(Ascii.image(vb.packet()));
  }
}

/*
1 STX Pic X(1) REQ'D 02h

HEADER1  RESPONSE:
2 Action Code Pic X(1) REQ'D
  A 901 = Approved (continue with Deposit Header 2)
  A 000 = Approved (exact duplicate of previous batch found in the Upload control file, host will disconnect, do not send Deposit Header 2)
  E 9nn = Error (host might disconnect depending on severity of the error. Check Response message, if not EOT continue to send Deposit Header 2. If EOT is sent, Host has disconnected.

FINAL REPONSE:
2 Action Code Pic X(1) REQ'D
  A = Approved (requested trans was successful)
  E = Error

3 Address Verification Response Code Pic X(1) REQ'D This field will be return as a space. There is no address verification code sent back on a Batch Inquiry.

HEADER1 RESPONSE:
4 Authorization/Error Code Pic X(6) REQ'D If Batch Release Header 1 is accepted, the host will a
  "A 901 followed by three spaces" or an
  "A 000 with an OK message", please see comment in Action Code field.
  If this batch receives an error, it will return a six digit error code listed in the Error Messages section.

FINAL RESPONSE:
4 Authorization/Error Code Pic X(6) REQ'D If Batch Release Header 2, Detail, and Trailer is accepted, the host will ALWAYS return a
  "A 000 followed by three spaces".
  If this batch receives an error, it will return a six digit error code listed in Appendix A.

5 Batch Number Pic 9(6) REQ'D The host will return the batch number incremented by one.
6 Retrieval Reference Number Pic 9(8) REQ'D 00000000 = The will always return all zeros
7 Filler Pic 9(3) REQ'D 000 always
8 Sequence Number Pic 9(3) REQ'D This field is echoed from the Batch Release.
9 Response Message Pic X(32) REQ'D Approval/Decline/Error text message information
10 FS Pic 9(1) REQ'D 1Ch
11 Download Flag Pic X(1) REQ'D 0 = None
12 Multi Message Flag Pic X(1) REQ'D N = Last Message
13 Batch Open Date/Time Pic 9(10) REQ'D MMDDYYHHMM
14 Batch Close Date/Time Pic 9(10) REQ'D MMDDYYHHMM
15 Batch Transaction Count Pic 9(6) REQ'D 000000 = The host will always return all zeros.
16 Batch Net Amount Pic X(10) REQ'D 00.00 = The host will always return 00.00.
17 ETX Pic X(1) REQ'D 03h
18 LRC Pic X(1) REQ'D Calculated XOR "00-FF"
*/

