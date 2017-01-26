package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/VisaLineRecognizer.java,v $
 * Description:  detects end of Visabuffer, configurable for presence of lrc.
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */
import net.paymate.util.*;

public class VisaLineRecognizer implements LineRecognizer {
  protected ErrorLogStream dbg;

  private boolean withLrc;
  protected boolean lrcing;

  /**
   * either the etx is the end, or the lrcbyte that follows it.
   */
  public boolean endOfLineDetected(StringBuffer context,int incoming){
    dbg.VERBOSE("vleold:"+Ascii.bracket(incoming));
    if(lrcing){
      lrcing=false;
      dbg.WARNING("lrc");
      return true;
    }
    switch(incoming){
      case Ascii.STX:{//reset input buffer on this.
        dbg.WARNING("STX");
        context.setLength(0);
        context.append((char)Ascii.STX);
        return false;
      }
      case Ascii.ETX:{
        dbg.WARNING("ETX");
        lrcing=withLrc;
        return ! lrcing;
      }
    }
    return false;
  }

  protected VisaLineRecognizer config(boolean withLrc){
    this.withLrc=withLrc;
    this.lrcing=false;
    dbg= ErrorLogStream.getForClass(this.getClass());
    dbg.WARNING("this message comes from VisaLineRecognizer");
    return this;
  }

  public static VisaLineRecognizer Lrceed() {
    return new VisaLineRecognizer().config(true);
  }

  public static VisaLineRecognizer Simple() {
    return new VisaLineRecognizer().config(false);
  }

}
//$Id: VisaLineRecognizer.java,v 1.3 2003/01/14 14:55:25 andyh Exp $