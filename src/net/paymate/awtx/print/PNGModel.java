/**
 * Title:        PNGModel<p>
 * Description:  Printing to PNG formatter <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: PNGModel.java,v 1.11 2001/10/15 22:40:06 andyh Exp $
 */
package net.paymate.awtx.print;
import  net.paymate.awtx.png.*;
import  java.io.OutputStream;
// for testing
import net.paymate.terminalClient.*;
import net.paymate.connection.*;
import java.io.*;
import net.paymate.jpos.awt.*;
import net.paymate.jpos.Terminal.*;
import net.paymate.util.Streamer;

public class PNGModel extends ImageModel {

  public PNGModel(PrinterModel lp, OutputStream os, int linesOfText, boolean signature) {
    super(lp, os, linesOfText, signature);
  }

  protected void encode() {
    PngEncoder png =  new PngEncoder( image, PngEncoder.NO_ALPHA, PngEncoder.FILTER_NONE, 9); // might change settings
    bytes = png.pngEncode();
  }

  // +_+ this should be in the ImageModel class.
  public static final void Test(String[] args) {
    try {
      for(int i = args.length; i-->0; ) {
        String filename = args[i];
        // load it from file
        File in = new File(filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(in);
        Streamer.swapStreams(fis, baos);
        ReceiptGetReply ar= new ReceiptGetReply(baos.toString());
        Receipt rcpt = ar.receipt();
//        Receipt rcpt = new Receipt(new CardReply(), new CardRequest());
//        rcpt.setto(new Hancock());
        String outFile = "c:\\temp\\imagetest" + i + ".png";
        FileOutputStream fos = new FileOutputStream(outFile);
        PNGModel png = new PNGModel(new Scribe612(null), fos, rcpt.totalLines(), false);
        rcpt.print(png,0);
        fos.close();
        dbg.VERBOSE("PNG was output to " + outFile);
      }
    } catch (Exception t) {
      dbg.Caught(t);
    }
  }
}
