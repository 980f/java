package net.paymate.authorizer.cardSystems;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/MAuthRec.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.18 $
 */

import net.paymate.awtx.*;
import net.paymate.connection.*;
import net.paymate.authorizer.*;
import net.paymate.data.VisaBuffer;
import net.paymate.util.*;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.web.LoginInfo;
import net.paymate.ISO8583.data.TerminalInfo;
import net.paymate.connection.*;
import net.paymate.jpos.data.*;
import net.paymate.ISO8583.data.*;

public class MAuthRec {
  private static final ErrorLogStream dbg = ErrorLogStream.get(MAuthRec.class.getName());

/*
// according to SteveAdcock, they don't use this
-----Original Message-----
From: Steve Adcock [mailto:Steve.Adcock@Cardsystems.com]
Sent: Thursday, October 04, 2001 10:53 AM
To: 'alien@spaceship.com'; Steve Adcock
Cc: Bill Thompson; Andy Heilveil; Allan Jones; Robb Shannon
Subject: RE: AcquirerBin

Matt
In checking with our programmers, it would be preferable to zero-fill this field. (six zero's) as opposed to six nines.
Thanks
*/
  private static final int dummyAcquirerBin = 0;

  // we will continue to use legacy settings for now (it works, after all)
  private static final String transactionProcessingIndicators = "Y C";
  //Y means that we are CPS capable
  //the space means that this is NOT a MOTO
  //the 'C' means ???


// samples ['^' could be a 0x02, 0x03, or 0x1C character]:
/*
sale:       M0.9999992000450001^469354@T5123456789012346^0202^^^Y C^0000100^^^^^^^
return:     M0.9999992000450001^469405@T4123456789012349^1212^^^Y C^0000300^^^^^^^
voidsale:   M0.9999992000450001^4695V5@T4123456789012349^1212^^      ^Y C^0000300^^^^04031829535 ^^^
voidreturn: M0.9999992000450001^4696V1@T5123456789012346^0202^^203997^Y C^0000100^^^^04031829168 ^^^

^W0.02037146000000001004001200^M0.9999991600270001^714654@D4352720000109743=04051012672440710529^^Y C^0001158^^^^^^^^
^W0.02037147000000001004001200^M0.9999991600270001^714754@D4323736553679904=020810100000454^^Y C^0000734^^^^^^^^
^W0.02037148000000001004001200^M0.9999991600270001^714854@D4610461226414967=01101010855922100000^^Y C^0001349^^^^^^^^
^W0.02037149000000001004001200^M0.9999991600270001^714954@D4356300009564119=04021015860559020152^^Y C^0000712^^^^^^^^
^W0.02037150000000001004001200^M0.9999991600270001^715054@D4323740238674454=03061017633688974^^Y C^0000679^^^^^^^^
^W0.02037151000000001004001200^M0.9999991600270001^715154@D4160320010343770=03011010000056303374^^Y C^0000626^^^^^^^^
^W0.02037152000000001004001200^M0.9999991600270001^715254@D4356030010482742=02021012093336050287^^Y C^0001458^^^^^^^^
^W0.02037153000000001004001200^M0.9999991600270001^715354@D4356030029264628=04071012978680210780^^Y C^0001696^^^^^^^^
^W0.02037154000000001004001200^M0.9999991600270001^715454@D4631588214921259=030410176881430^^Y C^0000950^^^^^^^^
^W0.02037155000000001004001200^M0.9999991600270001^715554@D4323736585462014=020910100000345^^Y C^0000600^^^^^^^^
*/

/**
 * 4.8 Field 8 - Transaction Code
Transaction codes currently supported are listed below:
Transaction Code Transaction Description
54 Auth : Purchase
55 Auth : Cash Advance
56 Auth : Mail/Phone Order
58 Auth : Card Authentication
01 Purch: No Auth
02 Cash Adv: No Auth
03 Mail: No Auth
04 Force : Offline Voice Auth
05 Credit : Credit Transaction
V1 Void : Voided Purchase
V2 Void : Voided Cash Advance
V3 Void : Voided Mail/Phone Order
V4 Void : Voided Force
V5 Void : Voided Credit
 */
  static String TransactionCode(TxnRow tr, TxnRow trold){
    if(tr.isReversal()){
      if((trold != null) && trold.isReturn()){//was original operation a refund==credit?
        return "V5";
      } else {
        return "V1";//other options not encoded yet.
      }
    }
    if(tr.isReturn()){
      return "05";
    } else {
      return "54";//other options not encoded yet.
    }
  }

  static void ManCardInfo(VisaBuffer req, MSRData card){
    req.append('T'); //4.10 manual, track 2 capable
    req.appendFrame(card.accountNumber.Image());
    req.appendFrame(card.expirationDate.mmYY());
    req.endFrame();//manual claims this is an N3 field, perhaps ServiceCode???
  }

  static void AmountInfo(VisaBuffer req,RealMoney amount){
    req.appendFrame(/*Fstring.righted(*/Long.toString(amount.Value())/*, 7, '0')*/); // --- extra spaces were screwing up CS dupcheck; === remove when code is tested 20011031 MMM
  }

  private static final String WRAPPERHEADER = "W0.";

  private static final boolean beWrapping=false; //haven't tried asynch protocol yet.
  private static void appendWrapper(VisaBuffer req, TxnRow tjr){
    String wrapperTxnId = tjr.tid().image();
//    int wrapperEst = WRAPPERHEADER.length()+wrapperTxnId.length()+1; // 1 is for frame separator
//    req.start(MaxRequest+wrapperEst);
    //wrapper with uniqueId
    req.appendAlpha(3,WRAPPERHEADER);
    req.appendFrame(wrapperTxnId);//variable anything
  }

  public static final int MaxRequest=313;// <-calculated, not including wrapper // old: 192;

  static VisaBuffer fromRequest(TxnRow tjr, TxnRow original){
    VisaBuffer req=VisaBuffer.NewSender(MaxRequest);

    req.start(MaxRequest);
    if(beWrapping){
      appendWrapper(req,tjr);
    }
    //format
    req.append("M0");
    req.append(".");
    //4.4 bin
    req.appendInt(6,dummyAcquirerBin);// see above
    //terminal id for auth
dbg.ERROR("authtermid = " + tjr.authtermid);

    req.appendNumeric(10,tjr.authtermid);//mainsail used char where an integer is defined by CS
    req.endFrame();
    req.appendNumeric(4,tjr.authseq);//### new 4 digit sequence#/terminal
    req.appendAlpha(2,TransactionCode(tjr, original));//54=pruchase, V1=void,04 force, 56=moto
    req.append('@');//4.9 ==signature, ### other options apply!!!
    if(tjr.isReversal()){
      //card number is always manually entered regardless of whether track data is available
      ManCardInfo(req,tjr.card());
      req.appendFrame(original.authidresponse);// tjr.authidresponse);//response to original txn!!!
      req.appendFrame(transactionProcessingIndicators);//CPS capable  , not MOTO, normal auth
      AmountInfo(req,original.rawamount());//+_+ should chase down and demote this guy to int.
      req.emptyFrames(3);
      req.appendFrame(original.hosttracedata);//original reference number
      req.emptyFrames(2);
    } else if(tjr.isDebit()){//very restrictive about track usage
      //CardRequest creq=(CardRequest) freq;
      String trackData = tjr.track2data;//creq.card.track(1).Data();
      if(TrackData.isProper(1, trackData)){//is track2 present and a valid track 2?
        req.append('D'); //4.10 swipe, track 2
        req.appendFrame(trackData);
      } else {
        return null;//can't do debit without track 2
      }
      //+++ much is still missing for debit
      return null;
    } else if(/*freq instanceof CardRequest*/tjr.isCredit()){ // is "card" specific enough?
      //CardRequest creq=(CardRequest) freq;
      String trackData = tjr.track2data;//creq.card.track(1).Data()
      if(TrackData.isProper(1, trackData)){//is track2 present and a valid track 2?
        req.append('D'); //4.10 swipe, track 2
        req.appendFrame(trackData);
      } else {
        trackData = tjr.track1data;//creq.card.track(0).Data()
        if(TrackData.isProper(0, trackData)){//see about T1
          req.append('H'); //4.10 swipe, track 1
          req.appendFrame(trackData);
        } else {
          ManCardInfo(req,tjr.card());
        }
      }
      req.endFrame();//no address verification info
      req.appendFrame(transactionProcessingIndicators);
      AmountInfo(req,tjr.rawamount());
      req.emptyFrames(6);
    } else {
      dbg.ERROR("Unknown request variant!  Returning null!");
      return null;
    }
    req.end();
    dbg.ERROR("isOk() returned " + req.isOk());
    return req;
  }

  public static VisaBuffer forReply(){
    return VisaBuffer.NewReceiver(MaxRequest).setClipLRC();
  }


/*
  String authexample="^BW0.02042260000000001004001200^\\M0.999999,1600270001^\\2260,54@D4388642046171305=030910152868759^\\^\\Y C^\\0000802^\\^\\^\\^\\^\\^\\^\\^C";
  String authresponse="^BW0.02042260000000001004001200^\\L0.E160027005226000217350011002070911APPROVAL 217350 004033920751  001275473135129^\\6S58^\\^C";

  String voidexample="^BW0.03010005000000001006001400^\\M0.9999996750415001^\\0005V1@T5430690133084712^\\0102^\\^\\028294^\\Y C^\\0000001^\\^\\^\\^\\04033645697 ^\\^\\^\\^C";
  String voidresponse="^BW0.03010005000000001006001400^\\L0. 67504150 000500028294010928201158APPROVAL  028294004033645697  ^\\^\\^C";
*/

  /**
   * @return wrapper content, which has also been removed.
   * @param vb visabuffer, raw off the 'net.
   */
  static String TxnIdentifier(VisaBuffer vb){
    String msgtype= vb.getMsgType();
    if(msgtype=="W0"){
      return vb.getROF();
    }
    return null; //or some guaranteed trash might be nice.
  }

  /**
   * @param vb already has had wrapper removed.
   * @return the crucial info from the response
   * @see TxnIdentifier
   */
  static VisaL0Response responseFrom(VisaBuffer vb){
    String msgtype= vb.getMsgType();
    if(msgtype == null) {
      dbg.ERROR("getMsgType() for ["+vb.packet()+"] returned null!");
    } else {
      if(msgtype.indexOf("L0") > -1) {
        return VisaL0Response.newFrom(vb);
      } else {
        dbg.ERROR("MessageType ["+msgtype+"] not L0! : " + vb.packet());
      }
    }
    return null;
  }

  /**
   * @deprecated
   * @return a stripped down txn record so that we can simulate card systems
   */
  static TxnRow parseRequest(VisaBuffer vb){
    String msgtype= vb.getMsgType();
    if(msgtype == null) {
      dbg.ERROR("getMsgType() for ["+vb.packet()+"] returned null!");
      return null;
    }
    switch(msgtype.charAt(0)){
      default:
      case 'L': return null;
      case 'W':
        dbg.VERBOSE("Wrapper"+vb.getROF());
        msgtype= vb.getMsgType();
      case 'M':
        TxnRow tjr=null; //no constructor available! TxnRow.forTesting();

        //+++ NYI
        return tjr;
    }
  }

}

/*
Request Format [Maverick M]:
## FieldName                          DataFormat DataLength Section Example
1  Record Format                      A          1          4.1     M
2  Application Type                   A          1          4.2     0
3  Message Delimiter                  A          1          4.3     "."
4  Acquirer Bin or Terminal Batch Nbr N or A     6          4.4     "123456" or "1234"
5  Terminal Number                    N          10         4.5     "9999999901"
6  Field Separator                    A          1          4.6     <FS> or HEX 1C
7  Transaction Sequence Number        N          4          4.7     0001
8  Transaction Code                   A          2          4.8     "54"
9  Cardholder ID Code                 A          1          4.9     "@"
10 Account Data Source Code           A          1          4.10    D
11 Customer Data Field                A          1-80       4.11    Track1, track 2, or manually entered customer data
12 Field Separator                    A          1          4.6     <FS> or HEX 1C
13 Address Verification OR Auth Code  See 4.12   See 4.12   4.12    1234MAINSTREET123456789 OR 123456 (Auth Code)
14 Field Separator                    A          1          4.6     <FS> or HEX 1C
15 Transaction Processing Indicators  A          0 to 4     4.13
16 Field Separator                    A          1          4.6     <FS> or HEX 1C
17 Transaction Amount                 N          0 to 12    4.14
18 Field Separator                    A          1          4.6     <FS> or HEX 1C
19 Secondary Amount                   N          0 to 12    4.15
20 Field Separator                    A          1          4.6     <FS> or HEX 1C
21 Market Specific Data               A          0 or 4     4.16
22 Field Separator                    A          1          4.6     <FS> or HEX 1C
23 Informational Data                 A          0-60       4.17
24 Field Separator                    A          1          4.6     <FS> or HEX 1C
25 Original Reference Number          N          0 or 12    4.18
26 Field Separator                    A          1          4.6     <FS> or HEX 1C
27 Purchasing Card Data               A          0-17       4.19
28 Field Separator                    A          1          4.6     <FS> or HEX 1C
28 Free Form Data                     A          0-45       4.20
30 Field Separator                    A          1          4.6     <FS> or HEX 1C
*/

/*
... this seems to be the tail endof discussing batch settlenment:
After checking the LRC on the packet returned by CardSystems, the terminal should look for the packet to
begin with "APP" or "D." If the packet returned begins with an "APP," the next transaction should be sent.
Upon receiving a packet beginning with "D", the terminal should display the message received and hang up.
After sending the last transaction, the terminal should check for the final "APP" message. If the batch
transmission is successful, the terminal should clear the batch or mark it as sent so that the transactions are
not settled again.
*/

// $Id: MAuthRec.java,v 1.18 2001/11/17 06:16:57 mattm Exp $
