/**
 * Title:        ImageModel<p>
 * Description:  Printing to image formatter <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ImageModel.java,v 1.25 2001/10/18 06:50:49 mattm Exp $
 *
 * Note that an object of this class cannot be resused.  You use it once per drawing.
 *
 */

package net.paymate.awtx.print;
import  net.paymate.jpos.Terminal.LinePrinter;
import  java.io.OutputStream;
import  java.io.IOException;
import  java.awt.Color;
import  java.awt.Graphics;
import  java.awt.Font;
import  java.awt.FontMetrics;
import  java.awt.Dimension;
import  net.paymate.util.*;
import  net.paymate.jpos.awt.Hancock;
import  net.paymate.jpos.awt.Raster;
// new PJA stuff
import  com.eteks.awt.*;

/*
  +_+ maybe later actually make it 1/2 inch (?)
  +_+ or something that will work on inkjet printers?
*/

abstract public class ImageModel extends PrinterModel {

  protected static final ErrorLogStream dbg = new ErrorLogStream(ImageModel.class.getName(), ErrorLogStream.VERBOSE);

  // need 10 pixels on top, bottom and sides as a buffer, at least
  protected static final int left = 10;
  protected static final int right = 10;
  protected static final int top = 10;
  protected static final int bottom = 20;
  protected static final int fontPoints = 14;//12; // 12 point font
  protected static final Color black = new Color(  0,   0,   0);
  protected static final Color white = new Color(255, 255, 255);
  protected static final String fontName = "Lucida Console";
  protected static final int style = Font.BOLD/*Font.PLAIN*/;

  protected int gWidth;
  protected int magicFontWidth;
  protected int magicFontHeight;
  protected int sigBoxHeight;

  protected OutputStream os = null;
  protected byte[] bytes = null;
  protected PJAImage image = null;
  protected PJAGraphics g;
  protected int soFar = top; // pixels printed so far
  protected int lines = 0;
  protected int gHeight;

  protected static int textWidth; // --- should this be static?  If we ever have a different model, it might screw it up.
  public int textWidth(){
    return textWidth;
  }
  private void doOnce(PrinterModel lp, boolean signature) {//only called by constructor
      textWidth=lp.textWidth(); // this is the only thing we copy from lp
      // create an unshown frame & get the sizes
      PJAImage imageTemp = new PJAImage(412, 217); // bogus; just for testing the measurements of the font
      PJAGraphics gTemp = imageTemp.getPJAGraphics();     // bogus for same reason
      gTemp.setFont(fontName, style, fontPoints);
      FontMetrics fm = gTemp.getFontMetrics();
      magicFontWidth = fm.charWidth('0'); // should be equal sizes for all chars in Courier
      magicFontHeight = fm.getHeight();
      dbg.VERBOSE( "magicFontHeight = " + magicFontHeight);
      // get graphics region
      rasterWidth=(magicFontWidth * textWidth);
      dbg.VERBOSE( "rasterWidth = " + rasterWidth + ", magicFontWidth = " + magicFontWidth + ", textWidth = " + textWidth + ", magicFontWidth * textWidth = " + (magicFontWidth * textWidth));
      double multiplicand = (1.0 * lp.Aspect.width / lp.Aspect.height) * (1.0 * lp.sigBox.height / lp.sigBox.width);
      // +++ might be better (show more detail): double multiplicand = .25;
      sigBoxHeight = signature ? (int)(multiplicand * rasterWidth) : 0;
      sigBox= new Dimension(signature ? rasterWidth : 0,sigBoxHeight);//72nds of an inch
      gWidth = rasterWidth + left + right;
      Aspect = new Dimension(1, 1); // undo what referencing the Scribe612 did (sigh)
  }

/**
 * This constructor takes a PrinteModel, from which it copies the textWidth variable.
 * This is so that it can have the same number of chars per line as the printer that was used.
 *  Perhaps we should add "chars per line" as a member of receipt?
 *  Then, it can be recovered from the file and reproduced without guessing what kind of printer was used.
 *  Or we could store the printer model in the database receipt record.
 *  Then, we could store the chars per line in the database printers table.
 * >>> TextList has a "lingest string" function which you can run on the receipt,
 * we can make a rule that at lesat one line goes the width of the printer...givne
 * that most lines run the full width right now this is easy!
 */

  public ImageModel(PrinterModel lp, OutputStream os, int linesOfText, boolean signature) {
    super();
    this.os = os;
    doOnce(lp, signature);
    dbg.VERBOSE( "lines = " + linesOfText);
    gHeight = (magicFontHeight * linesOfText) + top + bottom + sigBoxHeight;
    // now create the real image and Graphics for drawing
    image = new PJAImage(gWidth, gHeight);
    g = image.getPJAGraphics();
    g.setFont(fontName, style, fontPoints);
    // make it look like white paper
    g.setColor(white);
    g.fillRect(0, 0, gWidth, gHeight);
    // now you can draw on it
  }

  abstract protected void encode();

  public void println(String str) {
    if(g!=null){
      dbg.VERBOSE( "printing: " + str);
      g.setColor(black);
      soFar += magicFontHeight; // do this first since fonts start at the baseline
      g.drawString(str, left, soFar);
    }
  }

  public PrinterModel print(Raster rasta){
    drawLines(g, rasta);
    return this;
  }

  public boolean formfeed() {
    // output it
    boolean okay = true;
    try {
      dbg.Enter("formfeed");
      encode();
      if(os != null) {
        if (bytes == null) {
          dbg.WARNING("Null image");
          okay = false;
        } else {
          os.write( bytes );
        }
        os.flush();
      } else {
        dbg.WARNING("Null stream");
        okay = false;
      }
      dbg.VERBOSE("Wrote " + bytes.length + " bytes to stream");
    } catch (IOException e) {
      okay = false;
      dbg.WARNING("Exception streaming");
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return okay;
    }
  }

  protected void drawLines(Graphics g, Raster rast) {
    // print signature here !!!!
    soFar+=(magicFontHeight/2); // to let the bottoms of lower case letters be seen
    int maxy = rast.Height();
    for(int yi = 0; yi<maxy; yi++) {
      soFar++;
      for(int xi = rast.Width(); xi-->0;) {
        boolean bit = rast.pixel(xi, yi);
        int x = xi + left;
        g.setColor(bit ? black : white);
        g.drawLine(x, soFar, x, soFar);
      }
    }
  }

}
//$Id: ImageModel.java,v 1.25 2001/10/18 06:50:49 mattm Exp $
