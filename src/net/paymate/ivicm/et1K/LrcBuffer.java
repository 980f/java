package net.paymate.ivicm.et1K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/LrcBuffer.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LrcBuffer.java,v 1.16 2001/10/05 17:28:52 andyh Exp $
*/


import net.paymate.authorizer.*;
import net.paymate.util.Safe;

public class LrcBuffer extends LrcBufferBase {

  protected LrcBuffer(int maxsize,boolean rcv){
    super(maxsize,rcv);
  }

  public static LrcBuffer NewSender(int maxsize){
    return new LrcBuffer(maxsize,false);
    //dbg= .... add a shared command debugger.
  }

  ////////////////////
  // interpretive functions


  public int sizeCode(){//size of variable component
    return bight(1);
  }

  public int opCode(){
    return bight(2);
  }


  /////////////
  //

  public boolean end(){
    dbg.Enter("LrcBuffer.end()");
    try {
      if(!isReceiver&& sizeCode()==0){//for building commands whose length is not known in advance.
        dbg.VERBOSE("autosizing");
        replace(1,(byte)(nexti+1));//include lrc in count
      }
      return super.end();
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * @deprecated ended flag is not controlled properly yet.
   * remove first bytes and adjust everything
   */

  public void shift(int start){
    if(start>0 && start<nexti){
      int len=start;
//      while(len-->0){//remove these bytes from lrc calculation
//        lrc^=buffer[len];
//      }
      nexti-=start;
      System.arraycopy(buffer,start,buffer,0,nexti);
      computeLRC();
    }
  }

  /**
   * @return true if buffer is sane for sending, says nothing about contents of it.
   */
  public boolean isOk(){
    //4 is minimum packet length
    return super.isOk() && nexti>=4 && nexti==sizeCode();
  }

}
//$Id: LrcBuffer.java,v 1.16 2001/10/05 17:28:52 andyh Exp $
