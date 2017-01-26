package net.paymate.awtx.print;

/**
 * Title:        $Source: /cvs/src/net/paymate/awtx/print/StreamPrinter.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */
import net.paymate.util.*;
import net.paymate.jpos.Terminal.LinePrinter;//woefully misplaced class
import java.io.*;
import net.paymate.data.*; //

public class StreamPrinter extends LinePrinter {
  PrintStream os;

  protected void sendLine(byte [] rawline){
    os.print(new String(rawline));
  }

  // --- this was private.  why?
  public StreamPrinter(PrintStream os){
    super("stream printer");
    this.os=os!=null?os:System.err;//#leave as system.err
    super.dbg.setLevel(LogSwitch.WARNING);
    super.dbg.setLevel(LogSwitch.ERROR);// --- what is this?  why twice?
  }

  public static LinePrinter Err(){
    return new StreamPrinter(System.err);//#leave as system.err
  }

  public static LinePrinter Out(){
    return new StreamPrinter(System.out);
  }

}
//$Id: StreamPrinter.java,v 1.7 2002/07/17 22:03:54 mattm Exp $