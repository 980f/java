/**
 * Title:        $Source: /cvs/src/net/paymate/awtx/print/image/PNGModel.java,v $
 * Description:  Printing to PNG formatter
 * Copyright:    2000-2002
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Rev: PNGModel.java,v 1.11 2001/10/15 22:40:06 andyh Exp $
 */
package net.paymate.awtx.print.image;
import net.paymate.awtx.print.*;
import  net.paymate.awtx.png.*;
import  java.io.OutputStream;

public class PNGModel extends ImageModel {

  public PNGModel(PrinterModel lp, OutputStream os, int linesOfText, boolean signature, String fontName) {
    super(lp, os, linesOfText, signature, fontName);
  }

  protected void encode() {
    PngEncoder png =  new PngEncoder( image, PngEncoder.NO_ALPHA, PngEncoder.FILTER_NONE, 9); // might change settings
    bytes = png.pngEncode();
  }

}

//$Id: PNGModel.java,v 1.1 2003/12/09 00:10:33 mattm Exp $
