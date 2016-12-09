/* $Id: Constants.java,v 1.1 2000/11/21 02:56:15 andyh Exp $ */
package net.paymate.ivicm.ec3K;

public interface Constants {
  public static final byte FX_COMM_TEST                 = 16;
  public static final byte FX_ENABLE_READER             = 32;
  public static final byte FX_POLL                      = 33;
  public static final byte FX_GET_PARSED_DATA           = 34;
  public static final byte FX_DISABLE_READER            = 35;
  public static final byte FX_GET_ENCODING              = 36;

  public static final byte ENCODING_E13B                = 0;
  public static final byte ENCODING_CMC17               = 1;
  public static final byte ENCODING_OTHER               = 9;

  public static final byte FX_ENABLE_KEYBOARD           = 48;
  public static final byte FX_DISABLE_KEYBOARD          = 49;
  public static final byte FX_CLEAR_KEYBOARD_BUFFER     = 50;
  public static final byte FX_POLL_KEYBOARD             = 51;

  public static final byte FX_CLEAR_DISPLAY             = 17;
  public static final byte FX_BLINK_DISPLAY             = 18;
  public static final byte FX_SET_INTERCHAR_DELAY       = 19;
  public static final byte FX_DISPLAY_TEXT              = 20;
  public static final byte FX_SET_SCROLL_TEXT           = 21;
  public static final byte FX_START_SCROLL_TEXT         = 22;
  public static final byte FX_STOP_SCROLL_TEXT          = 23;
  public static final byte FX_DISPLAY_MAXCHARS          = 40;//added by alh

  public static final byte STATUS_SUCCESS               = 0;
  public static final byte STATUS_INVALID_LRC           = 1;
  public static final byte STATUS_INVALID_MODE          = 2;
  public static final byte STATUS_INSTRUCTION_FAILED    = 5;
  public static final byte STATUS_KEYBOARD_BUFFER_FULL  = 6;
  public static final byte STATUS_BAD_READ              = 96;
  public static final byte STATUS_NO_DATA_AVAILABLE     = -128;
  public static final byte STATUS_DATA_RETRIEVED        = -127;

  public static final byte FX_STX                       = 2;
  public static final byte FX_ETX                       = 3;//added by alh
  public static final byte FX_NULL                      = 0;

  //public static final byte CHAR_FS                      = 28;
  public static final char CHAR_CR                      = 13;
  public static final char CHAR_NL                      = 10;
  public static final char CHAR_LINE                    = 10;
}
//$Id: Constants.java,v 1.1 2000/11/21 02:56:15 andyh Exp $
