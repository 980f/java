/* $Id: Codes.java,v 1.14 2001/11/15 03:15:44 andyh Exp $ */
package net.paymate.ivicm.et1K;

public interface Codes {
//framing stuff
  public static final int STX = 5;
  public static final int maxPacketSize=229; //
  public static final int maxPacketBody= maxPacketSize-4; //229-stx-length-command-bcc, needs to come from 5992!

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
//undocumented! the UD flags control whether graphics get erased before form gets drawn
  public static final int UDFlags =0xFF;

  //34,35,36 not readily used via jpos.
  //psfb CLEAR_FLASH=37;
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
  public static final int DUKPT_PIN_INPUT = 0x62;//98;
  public static final int GET_PIN_DATA = 0x63;//99;
//don't use, reply framing is different from all other commands  public static final int COMM_TEST = 0x71;//113;

  public static final int Wellness=0xA0;
// send diag tallies=167;
// download application=210;
//////////////////////////////
// status field entries:
  public static final int SUCCESS = 0;
  public static final int MORE_DATA_READY = 0x80;//128

  public static final int INVALID_PARM = 0xEC;
  public static final int KEYPAD_CANCELED = 0xed;//237
  public static final int outofroominflash =0xee;//from developer's guid
  public static final int POWERUP=0xEF;// Power failure occurred

  public static final int NO_DATA_READY = 0xf0;//240
  public static final int PACKETERROR=0xF1;// Invalid VLI field in host message
  public static final int PACKETTIMEOUT=0xF2;// Communications time-out occurred
  public static final int InvalidScreenNumber=0xF6;//see 0x21 commands
  public static final int INVALIDSEQUENCENUMBER=0xF7;
  public static final int OPERATION_FAILURE = 0xF8;

  public static final int CONTROL_NOT_DISPLAYED = 0xfa;//250//aka invalid mode for command
  public static final int INVALID_PORT_STATE = 0xFA; //usually ok.

  public static final int BUFFER_OVERFLOW = 0xFB;
  public static final int MESSAGEOVERFLOW=0xFB;//aka receive buffer full

  public static final int INVALIDLRC=0xFF;

/*
F4 Invalid time value
F5 Communications adapter failure (check tallies)
F8 Flash memory compression failed
FD Invalid command, or function code missing in host message
*/


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

  public static final int AUX_FUNCTION = 255;

  public static final int AUX_ENABLE =  2;
  public static final int AUX_DISABLE = 3;
  public static final int AUX_SEND =    4;
  public static final int AUX_RECEIVE = 5;

  public static final int AUX_PORT_1 = 1;
  public static final int AUX_PORT_2 = 2;
  public static final int AUX_PORT_3 = 3;
  public static final int AUX_PORT_4 = 4;
  public static final int AUX_BAUD_1200 = 1;
  public static final int AUX_BAUD_2400 = 2;
  public static final int AUX_BAUD_4800 = 3;
  public static final int AUX_BAUD_9600 = 4;
  public static final int AUX_BAUD_19200 = 5;
  public static final int AUX_PARITY_NONE = 0;
  public static final int AUX_PARITY_EVEN = 1;
  public static final int AUX_PARITY_ODD = 2;
  public static final int AUX_DATABITS_7 = 7;
  public static final int AUX_DATABITS_8 = 8;
  public static final int AUX_DATABITS_9 = 9;
  public static final int AUX_STOPBITS_1 = 1;
  public static final int AUX_STOPBITS_2 = 2;

}
//$Id: Codes.java,v 1.14 2001/11/15 03:15:44 andyh Exp $
