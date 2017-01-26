package net.paymate.io;

import java.io.PrintStream;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: VirtualPrinter.java,v 1.1 2003/07/27 19:36:56 mattm Exp $
 */

// ??? use ByteFifo?

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
      return String.valueOf(sbos);
    } finally {
      sbos.reset();
    }
  }

  public TextList Image(int width){
    return new TextList( backTrace(),width,TextList.SMARTWRAP_ON);
  }

}
//$Id: VirtualPrinter.java,v 1.1 2003/07/27 19:36:56 mattm Exp $