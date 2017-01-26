package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IcePacketizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import net.paymate.serial.*;
import net.paymate.util.*;
import net.paymate.lang.MathX;
import java.io.*; //it is so freaking inane to have to re-import a class/package in an extension that is present in the parent class' public interface.
import net.paymate.io.ByteFifo;

public class IcePacketizer extends Packetizer {

  public IceCommand received(){
    return (IceCommand ) body();
  }

  boolean hasLrcNullBug=false;//set to true if terminal fails to send nul lrc's
  boolean addLF; //set to true if using hypercom CHKRD protocol

  /**
    * afterEtx() will get called after receiver has done its processing of etx
    */
   public int afterEtx(){
     if(hasLrcNullBug && lrc.Sum()[0]==0){//at iceterm8 the terminal didn't send these!
       return Ascii.NUL; //simulate receiving lrc of nul
     }
     return Receiver.EndOfInput;//does nothign
   }

  /**
   * number of bytes to expect in trailer.
   */
  protected int trailerLen() {
    return 0; //not used, only exists for formal reasons
  }

  public byte[] trailer(){
    if(addLF){
      // Use a ByteArrayOutputStream here.  That is all you need
      ByteArrayOutputStream os = new ByteArrayOutputStream();
//      ByteFifo tail=new ByteFifo(5);
//      OutputStream os = tail.getOutputStream();
      try {
        lrc.checksum(body());
        os.write(Ascii.FS); //pad with a frame separator to ensure all messages are nicely framed.
        lrc.checksum(Ascii.FS);
        //will be adding etx, this checksum is not dependent upon position.
        lrc.checksum(Ascii.ETX);
        switch (lrc.Sum()[0]) { //if the lrc is one of the problem ones
          case Ascii.NUL: //seems to be swallowed by terminal
          case Ascii.LF: //flushes reader in terminal
          case Ascii.STX: //to make logs easier to read
          case Ascii.ETX: //... ditto
          case Ascii.ENQ: { //pad message with antoher FS to modify lrc
            os.write(Ascii.FS);
            lrc.checksum(Ascii.FS);
          }
          break;
        }
        //now we add the etx
        os.write(Ascii.ETX);
        //whic is already summed into the lrc.
        //then the lrc
        os.write(lrc.Sum());
        os.write(Ascii.LF);//and now we flush
      } catch (Exception ex) {
        //+++???
      }
      return os.toByteArray();//tail.toByteArray();
    } else {
      return super.trailer();
    }
  }

  private byte []header;
  public byte[] header(){//extended header, could make part of IceCommand...
    if(header==null){
      StringBuffer makeHeader=new StringBuffer(10);
      makeHeader.append((char)Ascii.STX);//withour cast this gave as an ascii '2'!
      makeHeader.append(IceCommand.protocolId);
      makeHeader.append('.');
      header= makeHeader.toString().getBytes();
    }
//really big goofup: not including our header in checksum!
    lrc.reset();
    lrc.checksum(header);
    lrc.checksum(Ascii.STX);//but leading stx is not to be included in checksum.
    return header;
  }
  /**
   * keeping this object is runtime efficient in performance but holds on to the biggest buffer that ever gets sent.
   * truly large transmissions will be broken up per xmodem like protocol, hypercom can only receive about 1K with present settings.
   */
  private ByteArrayOutputStream fullmessage=new ByteArrayOutputStream();

  /**
   * so that the hypercom doesn't have to knit together multiple reads we have to ensure we have an uninterrupted send so,
   * we must accumulate the data so that we may use a single write command, then hope that the jni+OS doesn't chop up the transmission
   */
  public Exception writeOn(OutputStream os){
    try {
      if(addLF){
        super.writeOn(os);
      } else {//reduces the chance but doesn't guarantee that hypercom won't end its read routine early
        fullmessage.reset();
        super.writeOn(fullmessage);
        fullmessage.writeTo(os);
      }
      return null;
    } catch (Exception ex) {
      return ex;
    }
  }

  public static Packetizer T0(int size){
    IcePacketizer newone=new IcePacketizer();
    newone.Config(Ascii.STX,Ascii.ETX,true/*hasLrc*/,MathX.INVALIDINTEGER);//recognition stuff
    newone.lrc= LrcChecksum.Create();
    if(size>0){
      newone.attachTo(IceCommand.Create(size));
    } //else it is someone else's problem to provide a buffer
    return newone;
  }

  public static Packetizer forReception(){
    return T0(IceCommand.maxReceivePacket);
  }

}
//$Id: IcePacketizer.java,v 1.12 2004/01/19 17:03:25 mattm Exp $
