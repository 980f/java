package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/Receiver.java,v $
 * Description:  bitbucket implementation of serial data stream receiver
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Receiver.java,v 1.2 2001/06/26 20:14:52 andyh Exp $
 */

public class Receiver {
//////////////////
//psuedo bytes to send for exceptional cases
  public static final int EndOfInput=-1;  //see inputstream class
  public static final int DataLost=-2;    //parity error, framing error, overrun error
  public static final int TimedOut=-3;     //no data received

  /////////////////////////////////
  // timeout special values, for 'onByte()' return
/**
 * may lock the receiver...
 */
  public static final int TimeoutNever=-10;
  /**
   * quit calling the receiver onByte()
   */
  public static final int TimeoutNow=-11;


  /**
   * @return timeout for next byte
   * synchronized with onTimeout
   */
  public synchronized int onByte(int b){
    return Receiver.TimeoutNow;
  }

  /**
   * called when a wad of bytes are available.
   */
  public int onBytes(byte [] b){
    int accum=0;
    for(int i=0;i<b.length;i++){
      accum=onByte(b[i]);
    }
    return accum;
  }

}
//$Id: Receiver.java,v 1.2 2001/06/26 20:14:52 andyh Exp $