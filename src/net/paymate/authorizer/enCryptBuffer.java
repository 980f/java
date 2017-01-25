package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/enCryptBuffer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class enCryptBuffer extends E71Buffer implements AsciiLow32 {
  boolean altForm=false;//most by frequency of use are normal

  enCryptBuffer setAlt(){
    stx=SO;
    etx=SI;
    altForm=true;
    //+_+ and fixup buffer if already built???
    return this;
  }


  protected enCryptBuffer(int maxsize,boolean rcv) {
    super(maxsize,rcv);
  }

  public static enCryptBuffer NewSender2(int maxsize){
    return new enCryptBuffer(maxsize,false);
  }

  public static enCryptBuffer NewReceiver2(int maxsize){
    return new enCryptBuffer(maxsize,true);
  }

}