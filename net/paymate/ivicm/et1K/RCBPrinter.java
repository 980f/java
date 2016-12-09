package net.paymate.ivicm.et1K;
/**
* Title:        RCBPrinter
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: RCBPrinter.java,v 1.22 2001/10/22 23:33:39 andyh Exp $
*/

import net.paymate.util.*;

import net.paymate.util.ObjectFifo;
import net.paymate.util.ErrorLogStream;

import net.paymate.jpos.Terminal.LinePrinter;

public class RCBPrinter extends LinePrinter {
  static final ErrorLogStream dbg=new ErrorLogStream(RCBPrinter.class.getName());

  RCBIO port;//mixture of port and arbitration therefore

  protected void setRTS(boolean beOn){
    dbg.VERBOSE("setRTS:"+beOn);
    port.setRTS(beOn);
  }

  public static double textTweak=0.085;
  public static double grafTweak=0.120;

  public static EasyCursor Configure(EasyCursor ezp){
    textTweak=ezp.getNumber("textLineDelay",textTweak);
    grafTweak=ezp.getNumber("graphicLineDelay",grafTweak);
    return ezp;
  }

  public void setGraphing(boolean on){
    //hook to inform lowlevel driver that next series of junk is graphics data else is text
    //just for god forsaken RCB and the scribe 612.
    port.delayPerLine=on? grafTweak: textTweak;
//alwasy need speed--    port.directPriority=on; //graphics==high priority, need the speed
    dbg.VERBOSE("Delay set to: "+port.delayPerLine);
  }

  public int maxByteBlock(){
    return port.maxByteBlock();
  }

  private Monitor thisMonitor;

  protected void sendLine(byte [] rawline){
    try {
      thisMonitor.LOCK("sendLine(byte[])");
      if(!port.SendLine(Safe.insert(rawline,Codes.AUX_PORT_1,0))){
        //should try to deal with it, til then:
        dbg.ERROR("Printer.FailureToSend");
      } else {
        dbg.VERBOSE("Line Sent to RCBIO");
      }
    } finally {
      thisMonitor.UNLOCK("sendLine(byte[])");
    }
  }

  public RCBPrinter (String s, ET1K hw){
    super(s);
    thisMonitor = new Monitor("LinePrinter",dbg);
    this.port = new RCBIO(s,hw);
    //must add listener AFTER we are fully prepared for directIO events.
    port.Attach(this);
  }

} //$Id: RCBPrinter.java,v 1.22 2001/10/22 23:33:39 andyh Exp $
