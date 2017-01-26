package net.paymate.ivicm.et1K;

/**
* Title:
* Description:  special purpose bit packing stream.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author $Author: andyh $
* @version $Id: ncrOstream.java,v 1.4 2001/06/26 01:35:21 andyh Exp $
*/

import java.io.OutputStream;

public class ncrOstream {

  OutputStream os;
  byte buffer;
  int bitsfree;
  int truncator;

  public ncrOstream(OutputStream os, int truncator){
    this.os=os;
    this.truncator=truncator;
    buffer=0;
    bitsfree=8;
  }

  /**
  * @param field better be an unsigned 5 bit number
  */
  public void writeField(int field){
    try{
      bitsfree-=5;
      if(bitsfree<=0){//field splits
        buffer |= field>>-bitsfree;
        os.write(buffer);
        bitsfree+=8;
        buffer=0;
      }
      buffer |= field<<bitsfree;
    } catch(java.io.IOException arf){
      //ignored
    }
  }

  public void writeDiff(int current, int prev){
    writeField( ( (current-prev)>> truncator) &0x1f);
  }

  public void flush(){
    try{
      if(bitsfree<8){
        os.write(buffer);
        buffer=0;
        bitsfree=8;
      }
    } catch(java.io.IOException arf){
      //ignored
    }
  }

  public void writeLow(int raw){
    try{
      flush();
      os.write(raw);
    } catch(java.io.IOException arf){
      //ignored
    }
  }

  public void writeHigh(int raw){
    writeLow(raw>>8);
  }

  public void writeLohi(int raw){
    writeLow(raw);
    writeHigh(raw);
  }

  public void writeHilo(int raw){
    writeHigh(raw);
    writeLow(raw);
  }

  public void writeHeader(){    //block header
    writeLow(0);//1st byte
    writeLow(0);//2nd byte
    writeLow(0);//3rd byte
    writeLow(3-truncator);//1ast byte of header
  }

}
//$Id: ncrOstream.java,v 1.4 2001/06/26 01:35:21 andyh Exp $
