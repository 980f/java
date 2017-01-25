package net.paymate.ivicm.et1K;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/Reply.java,v $
 * Description:  adds et1k specific interpretation to LrcBuffer.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Reply.java,v 1.8 2001/10/05 17:28:52 andyh Exp $
 */

import net.paymate.util.*;
import net.paymate.authorizer.LrcBufferBase;

public class Reply extends LrcBuffer {
  static ErrorLogStream rdbg=new ErrorLogStream(Reply.class.getName());
  final static int RcvSize   =256;//actually 230 is probably the max.

  private Reply(int maxsize){
    super(maxsize,true);
    dbg=rdbg;
  }

  public static Reply New(){
    return new Reply(RcvSize);
  }

  /**
   * rather than an 'end of frame' this protocol counts bytes.
   */
  public int EndExpectedAt(){
  //if got enough bytes and first one is SOF
    return ( ptr()>1 && bight(0)==0 ) ? sizeCode(): RcvSize;
  }

  /**
   * @return true if the last byte appended completed reception.
   */
  private boolean endReceived(){
    return ptr()==EndExpectedAt();
  }

   public int response(){
    return isOk()? bight(3):Safe.INVALIDINTEGER;
  }

/**
 * where the variable part of the reply begins:
 */
  final static int inCargo=4;//stx,len,code,status
  /**
   * @param offset bytes to ignore from front of payload.
   */
  public byte [] payload(int offset){
    //inCargo is the bytes on every communication, the -1 is for the lrc:
    int length=sizeCode()-inCargo-1-offset;
    return extract(inCargo+offset,length);
  }

  public boolean append(byte b){
    dbg.Enter("append:"+Safe.ox2(b));
    if(super.append(b)){
      if(endReceived()){//looks at sizes rather than an etx marker.
        dbg.VERBOSE("end received");
        return end();
      } else {
        return true;
      }
    }
    return false;
  }

//  /**
//   * removes at least one nominal packet, at a minimum teh whole incoming thing is removed
//   */
//  Reply peel(){
//    Reply atfront= Reply.New();
//    for(int i=0;i<nexti;i++){
//      atfront.append(buffer[i]);
//      if(atfront.hasEnded()){//ignore if CS is ok.
//        //remove its bytes from incoming
//        shift(atfront.ptr());
//        break;
//      }
//    }
//    return atfront;
//  }
//
// /**
// * for when we suspect we missed the end of a reply...
// * peel an apparent reply off of the front
// */
//  static Reply peel(Reply rcvbuf){
//    return rcvbuf!=null? rcvbuf.peel():null;
//  }
//
//  /**
//   * discard leading data while trying to find a start of frame
//   */
//  boolean peelSof(){
//    int i=0;
//    for(;i<nexti;i++){
//      if(buffer[i]==0){//SOF for incoming data is 0
//        break;
//      }
//    }
//    shift(i); //obliterate all if no SOF found at all. Modifies nexti...
//    return nexti>0;
//  }




}
//$Id: Reply.java,v 1.8 2001/10/05 17:28:52 andyh Exp $
