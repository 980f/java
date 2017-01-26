package net.paymate.awtx.print;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/awtx/print/PrinterModelToken.java,v $</p>
 * <p>Description: configuration parameter names</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public interface PrinterModelToken {
  /**
   * number of nulls to send at the end of a line of graphic data
   */
  String rasterPadKey="rasterPad";
  /**
   * number of nulls to send at the end of a line of text data
   */
  String textPadKey="textPad";
  /**
   * number of nulls to send after a formfeed
   */
  String formPadKey="formfeedPad";
}
//$Id: PrinterModelToken.java,v 1.1 2003/01/09 00:25:32 andyh Exp $
