package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/Port.java,v $
 * Description:  for passing paired streams around.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Port.java,v 1.23 2003/07/27 05:35:13 mattm Exp $
 */

import java.io.InputStream;
import java.io.OutputStream;
import net.paymate.util.*;
import net.paymate.io.IOX;
import net.paymate.lang.ReflectX;

public class Port {
  protected String nickname="BasePort";
  Parameters parms;

  protected Shaker rts;
  protected Shaker dtr;

  public Shaker RTS(){
    if(rts==null){
      rts=Shaker.Virtual("RTS",Shaker.OFF);
    }
    return rts;
  }

  public boolean RTS(boolean on){
    return RTS().setto(on);
  }

  public Shaker DTR(){
    if(dtr==null){
      dtr=Shaker.Virtual("DTR",Shaker.OFF);
    }
    return dtr;
  }

  public boolean DTR(boolean on){
    return DTR().setto(on);
  }

  public String nickName(){
    return ReflectX.shortClassName(this,nickname);
  }

  protected int niceness;
  public int setNiceness(int millis){
    niceness=millis;
    return niceness;
  }

/**
 * a nice port doesn't spew eof's when the source is idle.
 * a few now and then are allowed even when nice.
 */
  public boolean isNice(){
    return false; //very few variations are nice
  }

  InputStream is;
  public InputStream rcv(){
    return is;
  }

  /**
 * @return bytes available on input stream
 */
  public int available(){
    try {
      return is.available();
    }
    catch (Exception ex) {
      return 0; //may add error codes later. Serial ports just don't do this.
    }
  }


  /**
 * use for diagnostic reads
 */
  public int ezRead(){
    try {
      return is.read();
    }
//    catch(NullPointerException npe){
//      return Receiver.Defective;
//    }
    catch (Exception ex) {
      return Receiver.EndOfInput;//. Serial ports just don't do this. !! unless they fail to open !!
    }
  }

  /**
   * for when the underlying stream's flush() doesn't please you.
   */
  public int reallyFlushInput(int limit){
    int flushed=0;
    while(available()>0&&++flushed<limit){//recheck available() in case input is still coming in.
      System.out.println(Receiver.imageOf(ezRead())); // --- remove the system.out from here
    }
    return flushed;
  }

  OutputStream os;
  public OutputStream xmt(){
    return os;
  }
  /**
   * start and end logic per String.substring();
   */
  public Exception lazyWrite(byte [] buffer,int off, int len){
    try {
      os.write(buffer,off,len);
      return null;
    } catch(Exception whocares){
      return whocares;
    }
  }

  public Exception lazyWrite(byte [] buffer){
    return lazyWrite(buffer,0,buffer.length);
  }

  public Exception lazyWrite(byte bee){
    try {
      os.write(bee);
      return null;
    } catch(Exception whocares){
      return whocares;
    }
  }

  public Exception lazyWrite(Object obj){
    if(obj instanceof Packetizer){
      return ((Packetizer) obj).writeOn(xmt());
    } else if(obj instanceof Byte){
      return lazyWrite(((Byte)obj).byteValue());
    } else if(obj instanceof byte[]){
      return lazyWrite((byte [])obj);
    } else if(obj instanceof String){
      return lazyWrite(((String)obj).getBytes());
    } else {
      return new ClassCastException("can't write unregistered type");
    }
  }

  public Port changeSettings(Parameters parms){
    this.parms=parms;
    return this;
  }

  public Parameters getSettings() {
    return parms;
  }

  public boolean openas(Parameters parms){
    this.parms=parms;
    return false;
  }

  /**
   * flush and close both directions
   */
  public Port close(){
    IOX.Close(rcv());
    IOX.Close(xmt());
    return this;
  }

  public static final Port Close(Port thisone){
    if(thisone!=null){
      thisone.close();
    }
    return thisone;
  }

  protected Port(String name,InputStream is, OutputStream os) {
    this(name);
    setStreams(is,os);
  }

  /**
   * for use by extensions only.
   */
  protected void setStreams(InputStream is, OutputStream os) {
    this.is=is;
    this.os=os;
  }

  protected Port(String name) {
    this.nickname=name;
    setStreams(new deadInputStream(), new deadOutputStream());
  }

  public String toSpam(){
   if(this.parms==null){
     return nickName()+" never opened!";
   }
   return nickName()+" "+parms.fullSpam();
 }


}
//$Source: /cvs/src/net/paymate/serial/Port.java,v $