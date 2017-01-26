package net.paymate.authorizer.npc;

import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.data.*; // ActionCode
import net.paymate.lang.StringX;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCAuthSubmitResponse.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

public class NPCAuthSubmitResponse extends AuthSubmitResponse {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NPCAuthSubmitResponse.class, ErrorLogStream.VERBOSE);

  public Packet vb = null; // set before using

  public NPCAuthSubmitResponse() {
  }

  /**
   * Clears it for reuse

  /* package */ final void reset() {
    super.clear();
    action=ActionCode.Unknown;
    batchNumber=0;
    authmsg="";
    wasAckNaked = false;
    ack = false;
  }

  public boolean wasGood() {
    return wasAckNaked ? ack : isApproved();
  }

  int batchNumber = -1; // not used

  private NPCAuthSubmitResponse parse(VisaBuffer vb) {
    dbg.VERBOSE("parsing response");
    if(vb!=null){
      if(vb.isComplete()){
        vb.parserStart();
        int recordDescriptor = StringX.parseInt(vb.getFixed(1));
        switch(recordDescriptor) {
          case 1: {
            action = ActionCode.Approved;
          } break;
          case 2: {
            action = ActionCode.Unknown;
          } break;
          case 0:
          default: {
            action = ActionCode.Declined;
          } break;
        }
        // +++ NEED to implement setting the next batchnumber for this authorizer only !!! ?!?!
        batchNumber=StringX.parseInt(vb.getFixed(3));             // new Batch Number
        String trash = vb.getFixed(6+6); // skip date and time
        dbg.WARNING("Tossed date/time: " + trash);
        authmsg=vb.getFixed(32); // Response Message [note last char is the day of the week]
        // the rest is trash
        trash = vb.getFixed(2); // will always be 12
      } else {
        vbnotcomplete();
      }
    }
    return this;
  }

  public void simulateApproval() {
    //                DATE___CODED
    String ret = "1999061402083457"+"OK!                             "/*authmsg*/+"12";
    byte [] retbytes = ret.getBytes();
    vb.append(retbytes);
  }

  /* package */ boolean wasAckNaked = false;
  /* package */ boolean ack = false;

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

  public String packetImage() {
    return String.valueOf(Ascii.bracket(vb.packet()));
  }
}
