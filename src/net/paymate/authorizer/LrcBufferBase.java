/**
* Title:        $Source: /cvs/src/net/paymate/authorizer/LrcBufferBase.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LrcBufferBase.java,v 1.13 2001/11/03 13:16:37 mattm Exp $
*/
package net.paymate.authorizer;

import net.paymate.util.Safe;

public class LrcBufferBase extends Packet {
  protected byte lrc=0;
  protected byte lrcStart=0;

  protected boolean isReceiver=false;

  /**
   * @return true if no more bytes will be accepted
   */
  public boolean isComplete(){
    return hasEnded();
  }

  protected void updateLrc(byte b){
    lrc^=b;
  }

  void forceLrc(byte b){
    if(ended){
      buffer[nexti-1]=lrc=b;
    }
  }

  public boolean replace(int index, byte b){
    if(index<nexti){
      updateLrc(buffer[index]);
      buffer[index]=b;
      updateLrc(b);
      if(hasEnded()){
        buffer[nexti-1]=lrc;//rewrite lrc
      }
      return true;
    } else {
      return anError();
    }
  }

  /**
  * @return whether character successfully went into buffer
  */
  public boolean append(byte b){
    dbg.Enter("LrcBB.append");
    try {
      if(ended){
        dbg.VERBOSE("checking lrc:"+Safe.ox2(b));
//        if(!checked){ // +++ restore this code?  Who didn't like it ???
//          dbg.VERBOSE("completing");
//          checked=true;
//          updateLrc(b);//to make lrc zero.  @P@
//          return lrc==0;
//        } else { //definitely hopelessly screwed
//          dbg.ERROR("already checked");
//          return anError();
//        }
      }
      dbg.VERBOSE("appending:"+Safe.ox2(b));
      return super.append(b);
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * cpompute lrc of whole buffer
   */
  protected byte computeLRC(){
    dbg.VERBOSE("compute lrc");
    lrc=0;
    for(int i=nexti;i-->lrcStart;){
      updateLrc(buffer[i]);
    }
    return (byte)lrc;
  }

  /**
   * on entouch receiver this adds as extra byte that should be 0
   */
  protected boolean end(){
    dbg.Enter("LrcBB.end()");
    try {
      if(!isReceiver){//for transmission add LRC after end marker.
        anError(super.append(computeLRC()));
      }
      return super.end();
    }
    finally {
      dbg.Exit();
    }
  }

  /**
  * @return true if buffer is sane, says nothing about contents of it.
  */
  public boolean isOk(){
    return super.isOk() && lrc==0;
  }

  public byte showLRC(){
    return lrc;
  }

  protected LrcBufferBase(int maxsize,boolean receiver) {
    super(maxsize);
    this.isReceiver =receiver;
  }

//  LrcBufferBase() {
//    //no default
//  }

  /**
   * we have lost the ability to get partial lrc's
   */
  public String toSpam(int clip){
    return super.toSpam(clip)+ (ended?(" lrc:"+Safe.ox2(lrc)):" not ended");
  }

  public String toSpam(){
    return toSpam(nexti);
  }

}
//$Id: LrcBufferBase.java,v 1.13 2001/11/03 13:16:37 mattm Exp $
