package net.paymate.ivicm.et1K;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/ResponseCode.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface ResponseCode {
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


}
//$Id: ResponseCode.java,v 1.1 2001/12/14 02:39:40 andyh Exp $