/**
* Title:        Extract
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Extract.java,v 1.33 2001/11/17 06:16:57 mattm Exp $
*/
package net.paymate.ISO8583.factory;

import  net.paymate.ISO8583.data.*;
import  net.paymate.connection.*;
import  net.paymate.util.TextList;

import net.paymate.jpos.data.MSRData;    //pieces of requests

public class Extract {

  protected /*static*/ final void parseResponse(ActionReply reply, Message msg){
    //interpret 39 and 153 into error list
    //if response code is not present then the transaction is OK, such as for checks.
    reply.setResponse(msg.ValueFor(Field.ResponseCode,"00"));

    if(!reply.Response.equals("00")){
      reply.Errors=new TextList(1);//rarely more than one item
      reply.Errors.add(reply.Response.ExtendedDescription());
      if(reply.Response.equals("MD")){//then we have mulitple errors
        String md=msg.ValueFor(Field.MessageMap);
        for(int i=0;i<md.length();i+=3){//for each set of three characters
          //two chars are another response code,the third is supposed to be a space...
          ResponseCode err=new ResponseCode(md.substring(i,i+2));
          reply.Errors.add(err.ExtendedDescription());
        }
      }
    } else {
      reply.Errors=new TextList(0);
    }
  }

  /**
   * extract the main fields that are common to financial replies
   */
  protected /*static*/ final void parseApproval(FinancialReply reply, Message msg){
// +++ next three lines
//    reply.tid     = TransactionID.New(msg.ValueFor(Field.TransStartTime),
//                                      msg.ValueFor(Field.SystemTraceAuditNumber),
//                                      msg.ValueFor(Field.CardAcceptorIdentificationCode));
    reply.CATermID   =msg.ValueFor(Field.CardAcceptorTerminalIdentification);
    parseResponse(reply,msg);
    reply.setApproval(msg.ValueFor(Field.AuthorizationIdentificationResponse,"REJECTED"));
  }

  public /*static*/ final CardReply CardResponseFrom(Message msg){
    CardReply reply= new CardReply();
    parseApproval(reply,msg);
    return reply;
  }

  public /*static*/ final CheckReply CheckResponseFrom(Message msg){
    CheckReply reply=new CheckReply();//gawd java gets verbose at times.
    parseApproval(reply,msg);
    return reply;
  }

  /**
   * @return a card object from the message
   * @param msg is presumed to be a reversal reply, other variations are untested but likely to work just fine
   * the track data is parsed first, then the mandatory fields are extracted which will often
   * write over what was parsed from the track. This is done to deal with bad or faked track1 info.
   */
  protected /*static*/ final MSRData CardInfoFrom(Message msg){
    MSRData card=new MSRData();
  //these extractions are carefully ordered to deal with partial track data.
    String trackImage=msg.ValueFor(Field.Track1Data);
    if(trackImage!=null){
      card.track(0).setto(trackImage,null);
      //to extract person's name, and in case track2 not kosher.
      card.ParseTrack1();
    }
    trackImage=msg.ValueFor(Field.Track2Data);
    if(trackImage!=null){
      card.track(1).setto(trackImage,null);
      //don't bother parsing, trust the following fields.
    }
    card.accountNumber.setto(msg.ValueFor(Field.PrimaryAccountNumber));
    card.expirationDate.parseYYmm(msg.ValueFor(Field.ExpirationDate));

    //track 3 is not comprehended by mainsail.
    return card;
  }

  protected /*static*/ final ReversalReply VoidResponseFrom(Message msg){
    ReversalReply reply= new ReversalReply();
    parseApproval(reply,msg); //this be all that we need
    //we have a feature request for card info and amount as well...for the receipt
    TransactionType ttype=new TransactionType(msg.ValueFor(Field.TransactionType));
    if(ttype.is(TransactionType.CR) || ttype.is(TransactionType.DB) ){
      reply.card=CardInfoFrom(msg);
    }
    reply.originalAmount.parse(msg.ValueFor(Field.TransactionAmount));
    return reply;
  }

  public /*static*/ final ActionReply ResponseFrom(Message msg){
    switch(msg.Type){
      case 110: return CheckResponseFrom(msg);
      case 210: return  CardResponseFrom(msg);
      case 410: return  VoidResponseFrom(msg);
//      case 810: return LoginResponseFrom(msg);
    }
    return new ActionReply(ActionReplyStatus.UndefinedISOResponse);
  }

}
//$Id: Extract.java,v 1.33 2001/11/17 06:16:57 mattm Exp $
