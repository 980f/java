package net.paymate.ivicm.et1K;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/OpCode.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface OpCode {
//command codes
  public static final int GET_COMPRESSED_SIG = 2;
  public static final int SEND_NEXT_DATA_BLOCK = 5;
  public static final int RESEND_LAST_DATA_BLOCK = 6;
  public static final int getVersionInfo=8;
//don't use:  public static final int RESET = 9;
  public static final int ABORT = 0x0a;
  public static final int CONFIGURE = 0x0b;
//patterns used within configure command
  public static final int SET_SIG_TYPE = 5;

  public static final int SendForm = 0x11;
  public static final int GET_CONTROLBOX_DATA = 0x12;//18;
//don't use  public static final int GET_SURVEYBOX_DATA = 0x13;//19;
//don't use  public static final int GET_SCRIPTBOX_DATA = 0x14;//20;
  public static final int DISPLAY_NUM_KEYBOARD = 0x15;//21;
  public static final int GET_KEYPAD_DATA = 0x16;//22;
  public static final int DISPLAY_ALPHA_KEYBOARD = 0x17;//23;
  public static final int DISPLAY_STORED_FORM = 0x21;//33;
  //0x22,0x23,0x24 split screen displays.
  //CLEAR_FLASH=0x25;
  public static final int CLEAR_SCREEN = 0x31;//49;
  //display text=50;
  //setfont=51;
  public static final int DISPLAY_TEXT_STRING = 0x34;//52;
  //configure=64;
  public static final int ENABLE_MSR = 0x41;//65;
  public static final int POLL_MSR_DATA = 0x42;//66;
  //enable msr and pinpad =67;
  //store new payment screen=68;
  public static final int SEND_ADVERTISEMENT = 0x51;//81;
//don't use:  public static final int EMULATE4430 = 0x61;//97;
//don't use, reply framing is different from all other commands  public static final int COMM_TEST = 0x71;//113;
  public static final int Wellness=0xA0;
// send diag tallies=0xA7;
// download application=210;

  public static final int AUX_FUNCTION = 0xFF;

}
//$Id: OpCode.java,v 1.2 2002/03/14 23:31:40 andyh Exp $