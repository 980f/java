package net.paymate.util;

import java.io.PrintStream;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: VirtualPrinter.java,v 1.3 2001/05/18 12:42:30 andyh Exp $
 */

// ??? use ByteArrayFIFO?

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import net.paymate.util.TextList;

public class VirtualPrinter extends PrintStream {
  protected ByteArrayOutputStream sbos ;

  private VirtualPrinter(ByteArrayOutputStream bs) {
    super(bs);
    sbos=bs;
  }

  public VirtualPrinter() {
    this(new ByteArrayOutputStream());
  }

  public String backTrace(){
    try {
      return sbos.toString();
    } finally {
      sbos.reset();
    }
  }

  public TextList Image(int width){
    return new TextList( backTrace(),width,TextList.SMARTWRAP_ON);
  }

}
//$Id: VirtualPrinter.java,v 1.3 2001/05/18 12:42:30 andyh Exp $