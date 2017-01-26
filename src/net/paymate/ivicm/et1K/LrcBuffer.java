package net.paymate.ivicm.et1K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/LrcBuffer.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LrcBuffer.java,v 1.22 2003/12/10 02:16:53 mattm Exp $
*/


import net.paymate.util.*; // Safe, ErrorLogStream
import net.paymate.data.LrcBufferBase;

public class LrcBuffer extends LrcBufferBase {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LrcBuffer.class);

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
    dbg.Enter("LrcBuffer.end()");//#gc
    try {
      if(!isReceiver && sizeCode()==0){//for building commands whose length is not known in advance.
        dbg.VERBOSE("autosizing");
        replace(1,(byte)(nexti+1));//include lrc in count
      }
      return super.end();
    }
    finally {
      dbg.Exit();//#gc
    }
  }

  /**
   * @return true if buffer is sane for sending, says nothing about contents of it.
   */
  public boolean isOk(){
//    System.out.println("at LrcBuffer.isOk() super is "+super.isOk()+" nexti is "+ nexti+" sizecode is "+sizeCode());
    //4 is minimum packet length
    return super.isOk() && nexti>=4 && nexti==sizeCode()-1;
  }

}
//$Id: LrcBuffer.java,v 1.22 2003/12/10 02:16:53 mattm Exp $
