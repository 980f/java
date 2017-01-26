package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/SimpleLineRecognizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.Ascii;

public class SimpleLineRecognizer implements LineRecognizer {
  protected int eolflag=Ascii.LF;
 /**
  * changing terminator in middle of reception leads to unspecified behavior.
  * should set protocol before issuing "start"
  */
  public SimpleLineRecognizer setEndOfLine(int simpleterminator){
    eolflag=simpleterminator;
    return this;
  }

  public boolean endOfLineDetected(StringBuffer context,int incoming){
    //we get to review and modify if we wish the whole buffer here
    return incoming==eolflag;
  }

  private SimpleLineRecognizer() {
  }

  public static SimpleLineRecognizer Ascii(int simpleterminator){
    return new SimpleLineRecognizer().setEndOfLine(simpleterminator);
  }

}