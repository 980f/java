package net.paymate.serial;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/serial/TerminalModem.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.util.*;
//import net.paymate.io.ByteFifo;
import java.io.ByteArrayOutputStream;

// This class is basically a test class for all subclasses.
// When its main is run, it allows the user to type over the modem and see what comes back.

public class TerminalModem extends ModemDevice {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(TerminalModem.class, ErrorLogStream.WARNING);

  public TerminalModem(String name) {
    super(name);
  }

  protected void onConnect() {
    ATcommand("I3"); // just to check the modem
    ATcommand("M1"); // turn the speaker on
  }

  //  positive values are databytes, negative values are events.
  //  @see net.paymate.serial.Receiver for event values.

  // for now, for debug
  byte[] abyte = new byte[1];
//  ByteFifo bytes = new ByteFifo();
//  OutputStream bytesout = bytes.getOutputStream();
  ByteArrayOutputStream bytesout = new ByteArrayOutputStream();
  public int onByte(int b) {
    // unix biased (why have TWO chars to designate EOT?)
    try {
      switch(b) {
        case 13: { // CR
          // ignore it
        } break;
        case 10: { // LF
          String fromBytes = new String(bytesout.toByteArray());
          bytesout.reset();
          dbg.VERBOSE("Line:"+fromBytes);
          System.out.println();
        } break;
        default: {
          abyte[0] = (byte)(b&255);
          bytesout.write(b);
          String toPrint = new String(abyte);
          dbg.VERBOSE("abyte:"+toPrint+"["+b+"]");
          System.out.print(toPrint);
        } break;
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return Receiver.TimeoutNever; //keeps receiver alive.
    }
  }

  /**
   * interactive tester
   * +++ need a ModemConfig class that we can load from commandline to set phone# (if there is one), port/rate/etc, if speaker is on/off, etc.
   * Find BBS's here: http://www.fidonews.org/findabbs/
   * I typed the following and found myself in a BBS!
   * ATDT3278598
   * Don't forget to type ATH0 to hang up!
   */
  public static final void main(String [] args) {
    TerminalModem totest=new TerminalModem("TerminalModem.tester");
    dbg.VERBOSE("parameters: " + TextList.CreateFrom(args).toString());
    totest.testerConnect(args,9600,dbg);
    //force levels here when you get sick of dicking with logcontrol.properties
    dbg.setLevel(LogSwitch.WARNING);
    totest.testloop(dbg);//tty
  }

}