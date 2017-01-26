package net.paymate.ivicm.et1K;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/Reply.java,v $
 * Description:  adds et1k specific interpretation to LrcBuffer.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Reply.java,v 1.20 2003/12/10 02:16:53 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.lang.MathX;

public class Reply extends LrcBuffer {
  static ErrorLogStream rdbg;
  final static int RcvSize   =256;//actually ~230 is probably the max.
  private final static int responseCodeIndex=3;

  private Reply(int maxsize){
    super(maxsize,true);
    if(rdbg==null) rdbg=ErrorLogStream.getForClass(Reply.class);
    isReceiver=true;
  }

  public static Reply New(){
    return new Reply(RcvSize);
  }

  /**
   * rather than an 'end of frame' this protocol counts bytes.
   */
  public int EndExpectedAt(){
  //if got enough bytes and first one is SOF
    return ( ptr()>1 && bight(0)==0 ) ? sizeCode() : RcvSize;
  }

  /**
   * @return true if the last byte appended completed reception.
   */
  private boolean endReceived(){
    return ptr()==EndExpectedAt()-1;//lrc acquisition now handled by LrcBufferBase
  }

  public int response(){
    return isOk()? bight(responseCodeIndex):MathX.INVALIDINTEGER;
  }

  boolean forceResponse(int respcode){
    return replace(responseCodeIndex,(byte)respcode);
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
    if(super.append(b)){
      if(!hasEnded()&& endReceived()){//looks at sizes rather than an etx marker.
        rdbg.VERBOSE("end received");
        return end();
      } else {
        return true;
      }
    }
    return false;
  }

}
//$Id: Reply.java,v 1.20 2003/12/10 02:16:53 mattm Exp $
