package net.paymate.awtx.png;

import net.paymate.data.Value;

//import java.awt.*;
//import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.zip.*;
import java.io.*;

/**
 * PngEncoderB takes a Java BufferedImage object and creates a byte string which can be saved as a PNG file.
 * The encoder will accept BufferedImages with eight-bit samples
 * or 4-byte ARGB samples. <br><br>
 *
 * The PngEncoderB class has been designed to allow encoding of images that are based
 * on an indexed color model with a palette. PngEncoderB takes a Java BufferedImage as
 * its input and produces a corresponding set of PNG bytes. Because it uses a
 * BufferedImage, this class requires Java 1.2. <br><br>
 *
 * There is also code to handle 4-byte samples returned as
 * one int per pixel, but that has not been tested.<br><br>
 *
 *  Usage: TestEncoderB -alpha -filter n -compress c -depth d <br>
 *      -alpha means to use alpha encoding (default none) <br>
 *      n is filter number 0=none (default), 1=sub, 2=up <br>
 *      c is compression factor (0-9); 1 default <br>
 *      d is pixel depth (8 or 24 bit); 24 default <br> <br>
 *
 * @version $Id: PngEncoderB.java,v 1.3 2001/02/03 01:10:07 mattm Exp $
 */

public class PngEncoderB extends PngEncoder {
  protected BufferedImage bimage;
  protected WritableRaster wRaster;
  protected int tType;

  /**
   * Class constructor
   *
   */
  public PngEncoderB() {
    this( null, false, FILTER_NONE, 0 );
  }

  /**
   * Class constructor specifying BufferedImage to encode, with no alpha channel encoding.
   *
   * @param bimage A Java BufferedImage object
   */
  public PngEncoderB( BufferedImage bimage ) {
    this(bimage, false, FILTER_NONE, 0);
  }

  /**
   * Class constructor specifying BufferedImage to encode, and whether to encode alpha.
   *
   * @param bimage A Java BufferedImage object
   * @param encodeAlpha Encode the alpha channel? false=no; true=yes
   */
  public PngEncoderB( BufferedImage bimage, boolean encodeAlpha ) {
    this( bimage, encodeAlpha, FILTER_NONE, 0 );
  }

  /**
   * Class constructor specifying BufferedImage to encode, whether to encode alpha, and filter to use.
   *
   * @param bimage A Java BufferedImage object
   * @param encodeAlpha Encode the alpha channel? false=no; true=yes
   * @param whichFilter 0=none, 1=sub, 2=up
   */
  public PngEncoderB( BufferedImage bimage, boolean encodeAlpha, int whichFilter ) {
    this( bimage, encodeAlpha, whichFilter, 0 );
  }

  /**
   * Class constructor specifying BufferedImage source to encode, whether to encode alpha, filter to use, and compression level
   *
   * @param bimage A Java BufferedImage object
   * @param encodeAlpha Encode the alpha channel? false=no; true=yes
   * @param whichFilter 0=none, 1=sub, 2=up
  * @param compLevel 0..9
    */
  public PngEncoderB( BufferedImage bimage, boolean encodeAlpha, int whichFilter, int compLevel ) {
    super(null, encodeAlpha, whichFilter, compLevel );
    //this.bimage = bimage;
    setImage(bimage);
  }

  /**
   * Set the BufferedImage to be encoded
   *
   * @param BufferedImage A Java BufferedImage object
   */
  public void setImage( BufferedImage bimage ) {
    this.bimage = bimage;
    pngBytes = null;
  }

  /**
   * Creates an array of bytes that is the PNG equivalent of the current bimage, specifying whether to encode alpha or not.
   *
   * @param encodeAlpha boolean false=no alpha, true=encode alpha
   * @return an array of bytes, or null if there was a problem
   */
  public byte[] pngEncode( boolean encodeAlpha ) {
    byte[]  pngIdBytes = { -119, 80, 78, 71, 13, 10, 26, 10 };
    int     i;

    if (bimage == null) {
      return null;
    }
    width = bimage.getWidth( null );
    height = bimage.getHeight( null );

    if (!establishStorageInfo()) {
      return null;
    }

    /*
     * start with an array that is big enough to hold all the pixels
     * (plus filter bytes), and an extra 200 bytes for header info
     */
    pngBytes = new byte[((width+1) * height * 3) + 200];

    /*
     * keep track of largest byte written to the array
     */
    maxPos = 0;

    bytePos = writeBytes( pngIdBytes, 0 );
    hdrPos = bytePos;
    writeHeader();
    dataPos = bytePos;
    if (writeImageData()) {
      writeEnd();
      pngBytes = resizeByteArray( pngBytes, maxPos );
    } else {
      pngBytes = null;
    }
    return pngBytes;
  }

  /**
   * Creates an array of bytes that is the PNG equivalent of the current bimage.
   * Alpha encoding is determined by its setting in the constructor.
   *
   * @return an array of bytes, or null if there was a problem
   */
  public byte[] pngEncode() {
    return pngEncode( encodeAlpha );
  }

  /**
   *
   * Get and set variables that determine how picture is stored.
   *
   * Retrieves the writable raster of the buffered bimage,
   * as well its transfer type.
   *
   * Sets number of output bytes per pixel, and, if only
   * eight-bit bytes, turns off alpha encoding.
   * @return true if 1-byte or 4-byte data, false otherwise
   */
  protected boolean establishStorageInfo() {
    int dataBytes;

    wRaster = bimage.getRaster();
    dataBytes = wRaster.getNumDataElements();
    tType = wRaster.getTransferType();

    if (((tType == DataBuffer.TYPE_BYTE) && (dataBytes == 4)) ||
            ((tType == DataBuffer.TYPE_INT) && (dataBytes == 1)) ) {
      bytesPerPixel = (encodeAlpha) ? 4 : 3;
    } else if ((tType == DataBuffer.TYPE_BYTE) && (dataBytes == 1)) {
      bytesPerPixel = 1;
      encodeAlpha = false;    // one-byte samples
    } else {
      return false;
    }
    return true;
  }

  /**
   * Write a PNG "IHDR" chunk into the pngBytes array.
   */
  protected void writeHeader() {
    int startPos;

    startPos = bytePos = writeInt4( 13, bytePos );
    bytePos = writeString( "IHDR", bytePos );
    width = bimage.getWidth( null );
    height = bimage.getHeight( null );
    bytePos = writeInt4( width, bytePos );
    bytePos = writeInt4( height, bytePos );
    bytePos = writeByte( 8, bytePos ); // bit depth
    if (bytesPerPixel != 1) {
      bytePos = writeByte( (encodeAlpha) ? 6 : 2, bytePos ); // direct model
    } else {
      bytePos = writeByte( 3, bytePos ); // indexed
    }
    bytePos = writeByte( 0, bytePos ); // compression method
    bytePos = writeByte( 0, bytePos ); // filter method
    bytePos = writeByte( 0, bytePos ); // no interlace
    crc.reset();
    crc.update( pngBytes, startPos, bytePos-startPos );
    crcValue = crc.getValue();
    bytePos = writeInt4( (int) crcValue, bytePos );
  }

  protected void writePalette( IndexColorModel icm ) {
    byte[] redPal = new byte[256];
    byte[] greenPal = new byte[256];
    byte[] bluePal = new byte[256];
    byte[] allPal = new byte[768];
    int i;

    icm.getReds( redPal );
    icm.getGreens( greenPal );
    icm.getBlues( bluePal );
    for (i=0; i<256; i++) {
      allPal[i*3  ] = redPal[i];
      allPal[i*3+1] = greenPal[i];
      allPal[i*3+2] = bluePal[i];
    }
    bytePos = writeInt4( 768, bytePos );
    bytePos = writeString( "PLTE", bytePos );
    crc.reset();
    crc.update("PLTE".getBytes());
    bytePos = writeBytes( allPal, bytePos );
    crc.update( allPal );
    crcValue = crc.getValue();
    bytePos = writeInt4( (int) crcValue, bytePos );
  }

  /**
   * Write the bimage data into the pngBytes array.
   * This will write one or more PNG "IDAT" chunks. In order
   * to conserve memory, this method grabs as many rows as will
   * fit into 32K bytes, or the whole bimage; whichever is less.
   *
   *
   * @return true if no errors; false if error grabbing pixels
   */
  protected boolean writeImageData() {
    int rowsLeft = height;  // number of rows remaining to write
    int startRow = 0;       // starting row to process this time through
    int nRows;              // how many rows to grab at a time

    byte[] scanLines;       // the scan lines to be compressed
    int scanPos;            // where we are in the scan lines
    int startPos;           // where this line's actual pixels start (used for filtering)
    int readPos;            // position from which source pixels are read

    byte[] compressedLines; // the resultant compressed lines
    int nCompressed;        // how big is the compressed area?

    byte[] pixels;          // storage area for byte-sized pixels
    int[] iPixels;          // storage area for int-sized pixels

    Deflater scrunch = new Deflater( compressionLevel );
    ByteArrayOutputStream outBytes = new ByteArrayOutputStream(1024);

    DeflaterOutputStream compBytes = new DeflaterOutputStream( outBytes, scrunch );

    if (bytesPerPixel == 1) {
      writePalette( (IndexColorModel) bimage.getColorModel() );
    }

    try {
      while (rowsLeft > 0) {
        nRows = Math.min( 32767 / (width*(bytesPerPixel+1)), rowsLeft );
        // nRows = rowsLeft;

        /*
         * Create a data chunk. scanLines adds "nRows" for
         * the filter bytes.
         */
        scanLines = new byte[width * nRows * bytesPerPixel +  nRows];

        if (filter == FILTER_SUB) {
          leftBytes = new byte[16];
        } else {
          if (filter == FILTER_UP) {
            priorRow = new byte[width*bytesPerPixel];
          }
        }

        if (tType == DataBuffer.TYPE_BYTE) {
          pixels = (byte[]) wRaster.getDataElements( 0, startRow, width, nRows, null );
          iPixels = null;
        } else {
          iPixels = (int[]) wRaster.getDataElements( 0, startRow, width, nRows, null );
          pixels = null;
        }

        scanPos = 0;
        readPos = 0;
        startPos = 1;
        for (int i=0; i<width*nRows; i++) {
          if (i % width == 0) {
            scanLines[scanPos++] = (byte) filter;
            startPos = scanPos;
          }

          if (bytesPerPixel == 1) {
            scanLines[scanPos++] = pixels[readPos++];
          } else if (tType == DataBuffer.TYPE_BYTE) {
            scanLines[scanPos++] = pixels[readPos++];
            scanLines[scanPos++] = pixels[readPos++];
            scanLines[scanPos++] = pixels[readPos++];
            if (encodeAlpha) {
              scanLines[scanPos++] = pixels[readPos++];
            } else {
              readPos++;
            }
          } else {
            scanLines[scanPos++] = (byte) ((iPixels[readPos] >> 16) & 0xff);
            scanLines[scanPos++] = (byte) ((iPixels[readPos] >>  8) & 0xff);
            scanLines[scanPos++] = (byte) ((iPixels[readPos]      ) & 0xff);
            if (encodeAlpha) {
              scanLines[scanPos++] = (byte) ((iPixels[readPos] >> 24) & 0xff );
            }
            readPos++;
          }
          if ((i % width == width-1) && (filter != FILTER_NONE)) {
            if (filter == FILTER_SUB) {
              filterSub( scanLines, startPos, width );
            } else {
              if (filter == FILTER_UP) {
                filterUp( scanLines, startPos, width );
              }
            }
          }
        }
        /*
         * Write these lines to the output area
         */
        compBytes.write( scanLines, 0, scanPos );

        startRow += nRows;
        rowsLeft -= nRows;
      }
      compBytes.close();

      /*
       * Write the compressed bytes
       */
      compressedLines = outBytes.toByteArray();
      nCompressed = compressedLines.length;

      crc.reset();
      bytePos = writeInt4( nCompressed, bytePos );
      bytePos = writeString("IDAT", bytePos );
      crc.update("IDAT".getBytes());
      bytePos = writeBytes( compressedLines, nCompressed, bytePos );
      crc.update( compressedLines, 0, nCompressed );

      crcValue = crc.getValue();
      bytePos = writeInt4( (int) crcValue, bytePos );
      scrunch.finish();
      return true;
    } catch (IOException e) {
      System.err.println( e.toString());
      return false;
    }
  }

}

