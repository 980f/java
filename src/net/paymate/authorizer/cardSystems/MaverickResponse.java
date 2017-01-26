package net.paymate.authorizer.cardSystems;

//import net.paymate.authorizer.*;
import net.paymate.data.*; // ActionCode

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/MaverickResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.24 $
 */

import net.paymate.util.*;
import net.paymate.authorizer.*;
import net.paymate.lang.StringX;

public class MaverickResponse extends AuthResponse {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(MaverickResponse.class);

  private Authorizer auth = null;

  public MaverickResponse(Authorizer auth) {
    this.auth = auth;
  }

  // values that we are not storing (and which are likely not needed) until we get a table just for these (some day)
  protected int TerminalNumber;
  protected String AuthSourceCode;
  protected int TranSequenceNumber;
  protected String LocalTxnDateTime; // date in unknown timezone
  protected byte AddressVerificationResultCode;
  protected byte MarketSpecificDataID;
  protected String freeFormMessageData;

  /**
   * Clear the data back to its default values
   */
  protected void clear() {
    super.clear();
    TerminalNumber                = -1;
    AuthSourceCode                = "";
    TranSequenceNumber            = -1;
    LocalTxnDateTime              = "";
    AddressVerificationResultCode = Ascii.SP;
    MarketSpecificDataID          = Ascii.SP;
    freeFormMessageData           = "";
    authrrn                       = "";
  }

  VisaCPSdata vscpsdata         = null;
 /**
 * @param vb must already be parsed up to and including the "L0."
 */
  // shouldn't actually be used by real classes; overload!
  protected MaverickResponse parse(VisaBuffer vb){
//auth.println("NOW IN PARSE! vb="+vb);
    clear();
    vscpsdata         = new VisaCPSdata(); // make this no matter what kind of txn this was.  other fields used for other things
    if(vb!=null){
      vscpsdata.cpsaci              = vb.getFixed(1); // 4 - Returned ACI
      TerminalNumber                = StringX.parseInt(vb.getFixed(8)); // 5 - first 8 of authtermid, which we know.
      AuthSourceCode                = vb.getFixed(1); // 6
      TranSequenceNumber            = StringX.parseInt(vb.getFixed(4)); // 7
// YOU MUST DO THIS NEXT LINE!  it sets the response code for the system, regardless if it was for VS or MC!
      vscpsdata.cpsrespcode         = vb.getFixed(2); // 8
      authcode                      = vb.getFixed(6); // 9 - ApprovalCode
      LocalTxnDateTime              = vb.getFixed(12);  //date in unknown timezone 10 & 11
      authmsg                       = vb.getFixed(16); // 12 - conveniently the size of our clerk display
      AddressVerificationResultCode = vb.getByte(); // 13
      authrrn                       = vb.getFixed(12); // 14
      MarketSpecificDataID          = vb.getByte(); // 15
      vscpsdata.cpstxnid            = vb.getROF(); // 16 [either 0 or 15 bytes] + 17 // MC is 9 bytes! (addendum)
      vscpsdata.cpsvalcode          = vb.getROF(); // 18 [0 or 4 bytes] + 18
      freeFormMessageData           = vb.getROB(); // 19
      boolean trymc = false;
      if(vscpsdata.isValid()) {
        if(vscpsdata.cpstxnid.length() == 15) {
          cpsdata = vscpsdata;
        } else {
          trymc = true;
        }
      } else {
        trymc = true;
      }
      if(trymc) {
        // try to see if it is MC ...
        MastercardCPSdata mccpsdata = new MastercardCPSdata();
        mccpsdata.setto(vscpsdata.cpstxnid, vscpsdata.cpsvalcode);
        if(mccpsdata.isValid()) {
          cpsdata = mccpsdata;
        }
      }
    }
    // interpret the response ...
    action = StringX.equalStrings("00", vscpsdata.cpsrespcode) ? ActionCode.Approved : ActionCode.Declined;
    if(isM4()){
//      action=ActionCode.Failed;
      action=ActionCode.Unknown; // this is the only way it will get stooding!
//      auth.PANIC("Just got a CardSystems M4 Try Again!");
    }
    //@todo: detect Debit card tried as Credit and put into list of potential BIN upgrades.
    authmsg = vscpsdata.cpsrespcode + " " + authmsg ;
    return this;
  }

  // overloads the base class default of false
  public boolean isM4() {
    return (vscpsdata != null) &&
        StringX.NonTrivial(authmsg) &&
        StringX.equalStrings("M4", vscpsdata.cpsrespcode) &&
        authmsg.startsWith("TRY AGAIN");
  }

  public TextList toSpam(TextList tl){
    tl=super.toSpam(tl);
    tl.add("TerminalNumber",TerminalNumber);
    tl.add("AuthSourceCode",AuthSourceCode);
    tl.add("TranSequenceNumber",TranSequenceNumber);
    tl.add("LocalTxnDateTime",LocalTxnDateTime);
    tl.add("AddressVerificationResultCode",AddressVerificationResultCode);
    tl.add("MarketSpecificDataID",MarketSpecificDataID);
    tl.add("freeFormMessageData",freeFormMessageData);
    return tl;
  }

  /**
   * @return either new visabuffer made from what we have here, or append what we have to buffer given and return that
   *
   * Needs fixin
   *
   */
//  public VisaBuffer format(){//used by simulator
//    VisaBuffer vb=VisaBuffer.NewSender(80);//format is always for sending
//    vb.append("L0.");
//    vb.append('E');//=vb.getByte();       //'E' CPS bullshit is entirely not defined.
//    vb.appendAlpha(8,authstoreid);//=vb.getFixed(8); // first 8 of authtermid, which we know.
//    vb.append(authsource);//= vb.getByte(); //'5' we don't care...
//    vb.appendInt(4,seqnum);//= StringX.parseInt(vb.getFixed(4)); //short sequence number
//    vb.appendAlpha(2,respcode);//= vb.getFixed(2); //The response code!
//    vb.appendAlpha(6,authcode);//vb.getFixed(6); //The authorization blurb
//    vb.appendAlpha(12,yymmddhhmmss);//=vb.getFixed(12);  //date in unknown timezone
//    vb.appendAlpha(16,authmsg);//=  vb.getFixed(16); //conveniently the size of our clerk display
//    vb.append(avrcode);// = vb.getByte();
//    vb.appendNumeric(12,authrrn);
//    vb.append(msdataid);//= vb.getByte();
//    vb.appendFrame(moreCPSbs);
//    return vb;
//  }

  public void process(Packet toFinish){
    // let the cast blow.  If it does, you coded thing incorrectly
//auth.println("NOW IN PROCESS!");
    VisaBuffer vb = (VisaBuffer)toFinish;
    if(!vb.isOk()) {
      dbg.ERROR("VisaBuffer is NOT okay, and is" + (vb.isComplete() ? "" : " NOT ") + "Complete!");
    }
    String msgtype= vb.getMsgType();
    if(msgtype == null) {
      dbg.ERROR("getMsgType() for ["+vb.packet()+"] returned null!");
    } else {
      if(msgtype.indexOf("L0") >=0) {
        parse(vb);
      } else {
        dbg.ERROR("MessageType ["+msgtype+"] not L0! : " + Ascii.bracket(vb.packet()));
      }
    }
  }

}

//  Response Format (Visa "L")
//  ## FieldName               DataFormat DataLength Section
//   1 Response Format                  A 1          5.1
//   2 Application Type                 A 1          5.2
//   3 Message Delimiter                A 1          5.3
//   4 Returned ACI                     A 1          5.4
//   5 Terminal Number                  N 8          5.5
//   6 Auth. Source Code                A 1          5.6
//   7 Tran. Sequence Number            N 4          5.7
//   8 Response Code                    A 2          5.8
//   9 Approval Code                    A 6          5.9
//  10 Local Transaction Date           N 6          5.10
//  11 Local Transaction Time           N 6          5.11
//  12 Auth Response Message            A 16         5.12
//  13 Address Verification Result Code A 1          5.13
//  14 Retrieval Reference Number       N 12         5.14
//  15 Market Specific Data ID          A 1          5.15
//  16 Transaction Identifier           A 0 or 15    5.16
//  17 Field Separator                  A 1          5.17
//  18 Validation Code                  X 0 or 4     5.18
//  19 Field Separator                  A 1          5.17
//  20 Free-Form Message Data           A 0-120      5.19

// $Id: MaverickResponse.java,v 1.24 2003/12/10 02:16:46 mattm Exp $
