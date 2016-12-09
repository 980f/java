package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AsciiLow32.java,v $
 * Description:  standard ascii definitions for communications framing
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface AsciiLow32 {
  public final static byte NUL=0x00;
  public final static byte STX=0x02;//
  public final static byte ETX=0x03;//
  public final static byte EOT=0x04;//End Of Transmission aka ^D.
  public final static byte ENQ=0x05;//
  public final static byte ACK=0x06;//positive acknowledgement
  public final static byte BEL=0x07;//
  public final static byte BS =0x08;//
  public final static byte TAB=0x09;//
  public final static byte LF =0x0A;//

  public final static byte FF =0x0C;//
  public final static byte CR =0x0D;//
  public final static byte SO =0x0E;//SHift OUt
  public final static byte SI =0x0F;//Shift In

  public final static byte NAK=0x15;//negative acknowledgement

  public final static byte ESC=0x1B;//  escape
  public final static byte FS =0x1C;// field separator

  public final static byte RS =0x1E;// record separator

}
//$Source: /cvs/src/net/paymate/authorizer/AsciiLow32.java,v $