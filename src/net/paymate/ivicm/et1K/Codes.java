/* $Id: Codes.java,v 1.17 2001/12/14 02:40:10 andyh Exp $ */
package net.paymate.ivicm.et1K;

public interface Codes {
//framing stuff
  public static final int STX = 5;
  public static final int maxPacketSize=229; //
  public static final int maxPacketBody= maxPacketSize-4; //229-stx-length-command-bcc, needs to come from 5992!

//undocumented! the UD flags control whether graphics get erased before form gets drawn
  public static final int UDFlags =0xFF;

  public static final int[] trackSelect={//jpos code index, entouch code value
    0,1,2,4,3,6,5,7
  }; //3&4 are swapped, 5&6 are swapped.

  public static final int NULL = 0;

  public static final int MSR_ERR_NO_START = 1;
  public static final int MSR_ERR_NO_END = 3;
  public static final int MSR_ERR_BAD_PARITY = 2;
  public static final int MSR_ERR_BAD_LRC = 4;
  public static final int MSR_ERR_OVERFLOW = 5;
  public static final int MSR_ERR_NO_TRACK = 5;
  public static final int MSR_ERR_NOT_CONFIG = 6;

  public static final int CMFONT_PLAIN = 0;
  public static final int CMFONT_REVERSE = 4;
  public static final int CMFONT_ITALIC = 1;
  public static final int CMFONT_UNDERLINE = 2;
  public static final int CMFONT_8X8 = 0;
  public static final int CMFONT_16X16 = 1;
  public static final int CMFONT_8X16 = 2;
  public static final int CMFONT_8X8_BOLD = 6;

}
//$Id: Codes.java,v 1.17 2001/12/14 02:40:10 andyh Exp $
