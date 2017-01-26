package net.paymate.terminalClient.PosSocket.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/GatewayUTFormatter.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.connection.*;
import net.paymate.util.*;
import net.paymate.data.VisaBuffer;

public class GatewayUTFormatter extends PaymentechUTFormatter {

  private int errorSeqnum=999;

  public GatewayUTFormatter() {//must exist for instantiator
    super();
    gateway=true;
    hostcapture=true;
  }
  /**
   * as of 17Oct02 we don't decode incoming requests until after a connection response failure.
   */
  public ActionRequest requestFrom(byte[] line){
    dbg.VERBOSE("request from terminal:"+Ascii.bracket(line));
    GatewayRequest greq= new GatewayRequest();
    VisaBuffer vb= VisaBuffer.FromBytes(line);//expects framing on input.
    greq.setOrigMessage(vb.body());
    ++ lastrrn;//guess at value, used by standin.
dbg.VERBOSE("gatewayformatter"+Ascii.bracket(greq.origMessage()));
    return greq;
  }

  public ActionRequest openGatewayRequest(byte[] line){
    return super.requestFrom(line);
  }

  public byte[] replyFrom(Action response,boolean timedout){
    dbg.VERBOSE("gw reply from action, complete? "+Action.isComplete(response));
    if(Action.isComplete(response)){//then nothing is null but that is all we know
      dbg.VERBOSE("gw reply from action, RESPONSE TYPE: "+response.TypeInfo());
      if( response.Type().is(ActionType.gateway)){//picking out gateway
        dbg.VERBOSE("gw reply from action, reply TYPE: "+response.reply.getClass());
        if( response.reply instanceof GatewayReply){
          GatewayReply grep= (GatewayReply)response.reply;
          //@todo: check success and generate error on incomplete reply
          if(grep.Succeeded()){//approved or declined
            dbg.VERBOSE("gw reply succeeded");
            captureSequenceInfo(response.request.origMessage(),grep.origMessage());
            return rawReplyFrom(grep);
          } else {//probably can't get here ....
            dbg.WARNING("gw reply failed");
            return errorReply(0,218,"Gateway Failure 1");
          }
        } else {//NON GATEWAY reply to a gateway request
          dbg.WARNING("non-gw reply");
          return errorReply(0,218,"Gateway Failure 2");
        }
      } else {//non gateway operation (such as occurs during standin)
        return super.replyFrom(response,timedout);//must do this to support standin
      }
    } else {//null in, null out.
      dbg.WARNING("returning null response");
      return NullResponse;
    }
  }

}
//$Id: GatewayUTFormatter.java,v 1.1 2003/12/10 02:16:53 mattm Exp $