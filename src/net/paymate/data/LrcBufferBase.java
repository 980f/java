/**
* Title:        $Source: /cvs/src/net/paymate/data/LrcBufferBase.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LrcBufferBase.java,v 1.2 2004/03/18 00:29:28 mattm Exp $
* @todo: tie debugger into extended class instance.
*/
package net.paymate.data;

import net.paymate.util.*; // Safe
import net.paymate.data.*; // Packet
import net.paymate.text.Formatter;

public class LrcBufferBase extends Packet {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LrcBufferBase.class);
//configuration flags
  protected byte lrcStart=0;//allows extension to omit stx's or such from lrc
  protected boolean isReceiver=false;
//state
  protected byte lrc=0;
  protected boolean expectinglrc=false;

  public void reset(){
    super.reset();
    lrc=0;
    //but receiver mode is left alone
    expectinglrc=false;
  }

//  public boolean start(int size){
// super.start() calls our reset()
//  }


/**
 * @param content becomes content of completed buffer
 */
  protected boolean stuff(byte []content){
    try {
      return super.stuff(content);
    }
    finally {
      lrc=buffer[nexti-1];
    }
  }

  public boolean setReceiveMode(boolean toOn){
    try {
      return isReceiver;
    }
    finally {
      isReceiver=toOn;
    }
  }

  /**
   * @return true if no more bytes will be accepted
   */
  public boolean isComplete(){
//    boolean retval=
    return hasEnded()&& !(isReceiver && expectinglrc);
//    dbg.WARNING("isComp()rel: "+isReceiver+ended+expectinglrc+" ->"+retval);
//    return retval;
  }

  protected void updateLrc(byte b){
    lrc^=b;
  }

  /**
   * this function is only valid for outgoing packets.
   */
  public boolean replace(int index, byte b){
  if(super.replace(index,b)){//if we change it
      if(hasEnded()){   //update cached lrc.
        buffer[nexti]= computeLRC();//rewrite lrc
        updateLrc(buffer[nexti]);
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
    dbg.Enter("LrcBB.append");//gc
    try {
      if(expectinglrc){
        expectinglrc=false;
        updateLrc(b);//if correct will make lrc zero.
        return lrc==0;
      } else {
        return super.append(b);
      }
    }
    finally {
      dbg.Exit();//gc
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
    dbg.Enter("LrcBB.end()");//#gc
    try {
      computeLRC();
      if(isReceiver){ //expect one byte of lrc.
        dbg.WARNING("next byte is incoming lrc");
        expectinglrc=true;
      } else { //for transmission add LRC after end marker.
        dbg.WARNING("appending lrc");
        anError(super.append(lrc));
      }
      return super.end();
    }
    finally {
      dbg.Exit();//#gc
    }
  }

  /**
  * @return true if buffer is sane, says nothing about contents of it.
  * only valid if isComplete() is true.
  */
  public boolean isOk(){
    dbg.VERBOSE("isOk():"+lrc);
    return super.isOk() && lrc==0;
  }

  public byte showLRC(){
    return lrc;
  }

  protected LrcBufferBase(int maxsize,boolean receiver) {
    super(maxsize); //unforuntately the super MUST precede setting the receiver mode flag.
    this.isReceiver =receiver;
  }

//  LrcBufferBase() {
//    //no default
//  }

  /**
   * we have lost the ability to get partial lrc's
   */
  public String toSpam(int clip){
    return super.toSpam(clip)+ (isComplete()?(" lrc:"+Formatter.ox2(lrc)):" incomplete.");
  }

  public String toSpam(){
    return toSpam(nexti);
  }

  ///////////////
  public TextList dump(TextList spam){
    if(spam==null){
      spam=new TextList();
    }
    spam.add("lrcbb.isReceiver",isReceiver);
    spam.add("lrcbb.lrcStart",lrcStart);
    spam.add("lrcbb.expecting",expectinglrc);
    spam.add("lrcbb.lrc",Formatter.ox2(lrc));
    spam.add("lrcbb.isOk()",isOk());
    return super.dump(spam);
  }

}
//$Id: LrcBufferBase.java,v 1.2 2004/03/18 00:29:28 mattm Exp $
