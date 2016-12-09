/**
* Title:        Packet
* Description:
*               this class doesn't synchronize stuff because it is intended to be
*               used sequentially by one agent at a time. It is filled by an agent
*               that fills it, then WHEN COMPLETE is handed to an agent that reads
*               it generally without modification.
*               the creator should ensure that only one thread can write it at a time.
*               the read functions do NOT modify it so multiple simultaneous readers
*               is cool.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Packet.java,v 1.12 2001/11/15 03:15:44 andyh Exp $
*/
package net.paymate.authorizer;
import  net.paymate.util.*;

public class Packet {
  protected ErrorLogStream dbg= ErrorLogStream.Null();

  protected byte [] buffer;
//for comparing instances, don't want to trust object compare. // --- this shouldn't be necessary; test to see
  static int instanceCounter=0;
  int instance;


  protected int nexti; //next available location==bytes present
  protected boolean ended=false;
  protected int size;  //pre-allocated space

  public int Size(){
    return size;
  }

  public int ptr(){
    return nexti;
  }

  public boolean hasEnded(){//use instead of 'isComplete' for non-lrc uses
    return ended;
  }

  /////////////
  public int errorCount=0;

  /**
   * @param ok if <b> false </b> will mark teh packet as having an error
   */
  protected boolean anError(boolean ok){
    if(!ok){
      ++errorCount;
    }
    return ok;//pass-thru convenience
  }

  protected boolean anError(){
    return anError(false);//awkward looking here, but looks good at point of use
  }

  //////////////
  public byte [] extract(int start, int length){
    if(start+length>nexti){//asking for data past end of input
      length=nexti-start;//tweak to return what is here
      if(length<0){//no data
        return new byte[0];//return empty, not null
      }
    }
    byte [] toReturn= new byte [length];
    System.arraycopy(buffer,start,toReturn,0,length);
    return toReturn;
  }

  public byte [] packet(){
    return extract(0,nexti);
  }

  public StringBuffer subString(int start, int length){
    //byte to string conversion happens here!
    StringBuffer sub=new StringBuffer(length);
    sub.setLength(length);
    while(length-->0){
      sub.setCharAt(length,(char)(buffer[start+length]&255));//+_+ is the 255 needed?
    }
    return sub;
  }

  public boolean start(int size){
    if(this.size<size){
      buffer=new byte[size];
      this.size=buffer.length;
      nexti=0;
    }
    //erase if didn't allocate fresh buffer, 4debug's sake
    while(nexti-->0){
      buffer[nexti]=0;
    }
    ++nexti;
    return size>0;//mostly just so that other extensions can return a status on their starts.
  }

  public boolean append(byte b){//often extended
    dbg.Enter("Packet.append");
    try {
      if(nexti<size){
        buffer[nexti++]=b;
        dbg.VERBOSE("ok");
        return true;
      }
      else {
        dbg.ERROR("no room!");
        return anError();
      }
    }
    finally {
      dbg.Exit();
    }
  }
/**
 * @return a positive 8 bit value, unless there is an error in which case returns -1
 */
  public int bight(int index){
    try {
      return 255& ((int)buffer[index]);
    } catch(Exception any){
      return Safe.INVALIDINTEGER;
    }
  }

  /**
   * @return last bight in buffer, 256 if buffer empty.
   */
  public int last(){
    return nexti>0? bight(nexti-1): 256;//bogus value on error
  }

  public boolean append(int n){
    return append((byte)n);
  }

  public boolean append(String s){
    int size=s.length();
    for(int i=0;i<size;i++){
      if(!append(s.charAt(i))){
        return false;
      }
    }
    return true;
  }

/**
 * append a string of ascii decimal or ascii hex
 * padder is added at end if the given string was odd in length
 */
  public boolean appendNibbles(String s,char padder){
    byte pack2=0;
    int i=0;
    while(i<s.length()){
      pack2= (byte)((s.charAt(i++)&0xF)<<4);
      pack2|=(byte)((s.charAt(i++)&0xF));
      if(!append(pack2)){
        return false;
      }
    }
    if( (i&1) !=0){
      pack2|=(byte)(padder&0xF);
      if(!append(pack2)){
        return false;
      }
    }
    return true;
  }

  public boolean append(byte []ba){
    return append(ba,0,ba.length);
  }

  public boolean append(byte []ba,int start){
    return append(ba,start,ba.length);
  }

  /**
   * @param end one past the last byte, like string.substring()
   * @param start first byte desired
   */

  public boolean append(byte []ba,int start,int end){
    while(start<end){
      if(!append(ba[start++])){
        return false;
      }
    }
    return true;
  }

  protected boolean end(){
    dbg.VERBOSE("setting ended flag");
    ended=true;
    return true;
  }

  public boolean isOk(){
    return buffer!=null;
  }

  public void reset(){
    ended=false;
    errorCount=0;
    while(nexti-->0){
      buffer[nexti]=0;
    }
    nexti=0;
  }

  public Packet(int maxsize) {
    start(maxsize);
    instance=++instanceCounter;
  }

  public String toSpam(int clip){
    StringBuffer readable=new StringBuffer(buffer.length);
    readable.append("buffer[#"+instance+"]:");
    if(clip>nexti){
      clip=nexti;
    }
    for(int i=0;i<clip;i++){
      readable.append(" "+Safe.ox2(buffer[i]));
    }
    return readable.toString();
  }

  public String toSpam(){
    return toSpam(last());
  }

}
//$Id: Packet.java,v 1.12 2001/11/15 03:15:44 andyh Exp $
