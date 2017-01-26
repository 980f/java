package net.paymate.ivicm.et1K;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/AuxCode.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface AuxCode {
  public static final int AUX_COMPRESSFLASH =  1;
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
//$Id: AuxCode.java,v 1.1 2001/12/14 02:39:40 andyh Exp $