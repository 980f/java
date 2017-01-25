/**
* Title:
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: WordyInputStream.java,v 1.4 2001/04/10 17:31:31 andyh Exp $
*/
package net.paymate.awtx;
import net.paymate.util.ErrorLogStream;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;

import java.io.InputStream;
import java.io.ByteArrayInputStream;


/**
 * fail easy input stream reader.
 * reading past end of stream gets nulls.
 * explicitly check for end of stream, don't rely upon exceptions.
 */
public class WordyInputStream {
  ErrorLogStream dbg= new ErrorLogStream(WordyInputStream.class.getName());

  protected boolean bigendian;
  protected InputStream wrapped;
  protected int bitbuff=0;
  protected int bitPtr=32;//past last bit, ensures load on first call to signedField

  byte []pushback=new byte[3];
  int pushed=0;//==empty

  public boolean pushBack(byte b){
    dbg.WARNING("Pushback:"+b+" ["+pushed+"]");
    if(pushed<pushback.length){
      pushback[pushed++]=b;
      return true;
    }
    return false;
  }

  public WordyInputStream(byte [] data,boolean msbfirst){
    this(new ByteArrayInputStream(data),msbfirst);
  }

  public WordyInputStream (InputStream is,boolean msbfirst){
    wrapped=is;
    bigendian=msbfirst;
  }

  public WordyInputStream (InputStream is){
    this(is,false);
  }

  public WordyInputStream changeEndian(boolean bigend){
    bigendian=bigend;
    return this;
  }

  public WordyInputStream skip(int count) {
    try {
      wrapped.skip(count);
    } catch(java.io.IOException ignored) {

    }
    return this;
  }

  /**
   * @return internal stream
   */
  public InputStream is(){
    return wrapped;
  }
  /**
   * @return unsigned 8 bits
   */
  public int unsigned8() throws java.io.IOException {
    if(pushed>0){
      return pushback[--pushed];//+_+ could use a byte stack...
    }
    return wrapped.available()>0? wrapped.read():0;
  }

  public int signed8() throws java.io.IOException {
    int rawbyte=unsigned8();
    return rawbyte>=128? rawbyte-255:rawbyte;
  }


  /**
   * @return unsigned 16 bits
   */
  public int u16(boolean bigendian)throws java.io.IOException {
    if(bigendian){//override's this's
      return (unsigned8()<<8)+unsigned8();
    } else {
      return unsigned8()+(unsigned8()<<8);
    }
  }
  public int u16()throws java.io.IOException {
    return u16(bigendian);
  }

 /**
   * @return unsigned 16 bits
   */
  public int s16() throws java.io.IOException {
    int usigned=u16();
    return usigned>=65536? usigned-65536:usigned;
  }

  public int signedField(int numbits) throws java.io.IOException {
    int piece;
    piece= bitbuff<<bitPtr;//if bitPtr>=32 then this yields 0
    bitPtr+=numbits;
    if(bitPtr>32){//then we need some bits form next word
      bitbuff=dword(true);//alwasy do this bigendian, without affecting stream setting
      bitPtr-=32;
      piece |= bitbuff>>>(numbits-bitPtr);
    }
    piece>>=(32-numbits); //&= ~((1<<bitPtr)-1);
    return piece;
  }

  public void flushBits(){
    //remove fraction of a byte
    int bytesFree= 4-((bitPtr+7)>>3);
    dbg.WARNING("flushBits:"+bitPtr+" "+bytesFree);
    // now points to msb of first unused byte in bitbuff
    while(bytesFree-->0){
      pushBack((byte)bitbuff);
      bitbuff>>=8 ;
    }
    bitPtr=32;
  }

  /**
   * @deprecated should be fucking simple....bu tdoesn't seem to work.
   */
  public int bitsAvailable(){
    int bitsonhand=(32-bitPtr);
    try {
      return bitsonhand + 8*wrapped.available();
    } catch (java.io.IOException dammit){
      //won't ever happen but compiler is dumb;
    } finally {
      return bitsonhand;
    }
  }

//////////////////////////////

  public int dword() throws java.io.IOException {
    return dword(bigendian);
  }
  /**
   * @return 32 bits
   */
  public int dword(boolean bigendian) throws java.io.IOException {
    if(bigendian){//overrides this's
      return (u16(bigendian)<<16)+u16(bigendian);
    } else {
      return u16(bigendian)+(u16(bigendian)<<16);
    }
  }


  public Dimension dimension() throws java.io.IOException {
    return new Dimension(u16(),u16());
  }

  public Point point() throws java.io.IOException {
    return new Point(u16(),u16());
  }

  public Point pointYX() throws java.io.IOException {
    int Y=u16();
    int X=u16();
    return new Point(X,Y);
  }

}

//$Id: WordyInputStream.java,v 1.4 2001/04/10 17:31:31 andyh Exp $
