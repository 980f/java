package net.paymate.jpos.Terminal;
/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/LinePrinter.java,v $
* Description:    class LinePrinter directly implements a NullPrinter. Normally
one extends its "sendLine" functions to route output to an actual
printing device. IF RTS is required override that too.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Rev: LinePrinter.java,v 1.21 2001/12/04 06:52:53 andyh Exp $
* @todo: use Qagent and notifies instead of idiosyncractic RTS stuff.
*/

import net.paymate.util.*;
import net.paymate.jpos.data.ByteBlock;
import net.paymate.util.ObjectFifo;
import net.paymate.text.Formatter;

public class LinePrinter {//base is complete, but prints to debug stream
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(LinePrinter.class);
  protected static final ErrorLogStream lines=ErrorLogStream.getExtension(LinePrinter.class, "lines");
/** for the configuration psuedo database  */
  protected String myName;
  protected ObjectFifo paraBuffer;
  protected Monitor paraBufferMonitor = new Monitor("paraBuffer");
  protected boolean amGraphing;

  private Monitor pageMonitor = new Monitor("pagecoherence");

  public void disConnect(){
  //stop NOW and release all resources.
  //trusts overriding clases to free paraBuffer.
    paraBuffer.Clear();
    endPage();
  }

  public void startPage (){
    pageMonitor.getMonitor();
  }
  public void endPage (){
    pageMonitor.freeMonitor();
  }

/**
 * usually overloaded
 */
  protected void sendLine(byte [] rawline){
      dbg.VERBOSE("sending raw bytes");
      if(!amGraphing){
        lines.VERBOSE(new String(rawline));
      } else {
        lines.VERBOSE(String.valueOf(Formatter.hexImage(rawline)));
      }
      CTSEvent(true); //else rest of buffer will stay filled
  }

  /**@return true if successfully buffered
   * synchronized to ensure that the .put and the setRTS are not interrupted
   * coz someone thinks that else it might let lines print out of order
   *
   */

  public boolean Print(byte[] rawline){
    try {
      paraBufferMonitor.getMonitor();
      dbg.VERBOSE("Buffering:"+Formatter.hexImage(rawline));
      paraBuffer.put(rawline);
      setRTS(true);  //rigged to give us an event if we can send data.
    } finally {
      paraBufferMonitor.freeMonitor();
      return true;  //someday may have a maximum size on paraBuffer
    }
  }

    /**@return true if successfully buffered   */

  public boolean Print(byte controlchar){//param is named for most common use
    dbg.VERBOSE("print one char:"+Formatter.ox2(controlchar));
    byte [] one=new byte [1];
    one[0]=controlchar;
    return Print(one);
  }


/**
 * hook to inform lowlevel driver that next series of junk is graphics data else is text
 * brought into existence just for god-fersaken RCB and the scribe 612.
 */
  public void setGraphing(boolean on){
    amGraphing=on;
  }

  public EasyCursor configure(EasyCursor ezp){
    return ezp;
  }

  public int maxByteBlock(){
    return 256; //arbitrary number, usually overridden
  }

  /** @return number of sub arrays (usually raster lines) that did NOT print  */
  public int Print(ByteBlock blob){//like a logo, preformatted graphics data
    boolean lastOk = true;
    int size=blob.length();
    int i;
    int end;
    for(i =0 ; lastOk && i<size;i++) {//want to retain given order
      int preblob=0;
      for(end=i; end<size; end++){
        int trial=blob.line(i).length;
        if(preblob+trial>maxByteBlock()){
          --end;
          break;
        }
        preblob+=trial;
      }
      if(end>i){//then we can merge a few (perhaps just one)
        byte [] syncytium= new byte[preblob];
        int blockstart=0;
        for(i=i;i<=end && i<size;i++){//i through end inclusive,2nd clause handles final blcok
          System.arraycopy(blob.line(i),0,syncytium,blockstart,blob.line(i).length);
          blockstart+=blob.line(i).length;
        }
        lastOk= Print(syncytium);
        i=end;//point to last one actually sent to match the else clause following.
      } else {
        lastOk = Print(blob.line(i));
      }
    }
    return size-i;
  }

/** paces objects out of the printer buffer out to the printer
 *  sync'ing the object removal and the sendLine seems needed to
 *  ensure order of printing is proper.
 */

  public void CTSEvent(boolean nowclear) {
    try {
      paraBufferMonitor.getMonitor();
      if(nowclear){
        byte []nob=(byte [])paraBuffer.next();
        if(nob!=null) {
          sendLine(nob);
        } else {
          setRTS(false); //release port
        }
      }
    } finally {
      paraBufferMonitor.freeMonitor();
    }
  }
//////////////////////////////////////////////////////////////
/** set to enable/start buffered transmission, is cleared when buffer empties  */
  protected void setRTS(boolean beOn){//usually overloaded!
    dbg.VERBOSE("setRTS:"+(beOn?"On":"Off"));
    if(beOn){
      CTSEvent(true); //start emptying buffer
    }
  }

  /**@param s object name for configuration database  */
  public LinePrinter (String s){//usually overloaded and super'd
    myName=s;
    paraBuffer= new ObjectFifo();
  }

  public static LinePrinter Null(){
    return new LinePrinter("nullLinePrinter");
  }


  public String toString(){
    return myName;
  }

} //$Id: LinePrinter.java,v 1.29 2003/07/27 05:35:06 mattm Exp $
