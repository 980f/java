package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/E71Buffer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: E71Buffer.java,v 1.4 2001/10/05 17:28:51 andyh Exp $
 */

public class E71Buffer extends VisaBuffer {

  boolean incoming=false;
  boolean incomingError=false;
  final static byte msb=(byte)0x80;
  /**
   * constructed for write
   */
  public void setIncoming(){
    incoming=true;
  }

/**
 * both generates and checks the parity bit
 * i.e. running this twice yields the original data.
 */
  public static final byte paritized(byte b){
    for(byte bit=msb>>1;bit!=0;bit>>=1){
      if((b & bit)!=0){
        b^=msb;
      }
    }
    return b;
  }
///////////////////
// overrides

/**
 * sending: add parity bit on way into buffer
 * receiving: check+clear parity bit on way into buffer.
 * @param b better be ascii when writing, we mask errors rather than report them
 */
  public boolean append(byte b){
    b=paritized(b);
    if(incoming && (b&msb)!=0){
      incomingError=true;
    }
    return super.append(b)&& !incomingError;
  }

  /**must fixup parity bit on lrc byte
   *
   */
  public boolean end(){
    super.end();
    int lrcindex=nexti-1;//place in buffer where lrc resides
    buffer[lrcindex]=paritized(buffer[lrcindex]);
    return isOk();
  }

  public boolean isOk(){
    return super.isOk()&& !incomingError;
  }

  protected E71Buffer(int maxsize,boolean rcv) {
    super(maxsize,rcv);
  }

  public static E71Buffer NewSender1(int maxsize){
    return new E71Buffer(maxsize,false);
  }

  public static E71Buffer NewReceiver1(int maxsize){
    return new E71Buffer(maxsize,true);
  }



//  public E71Buffer() {
//    super();
//  }

}
//$Id: E71Buffer.java,v 1.4 2001/10/05 17:28:51 andyh Exp $