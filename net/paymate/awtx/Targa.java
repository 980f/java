package net.paymate.awtx;

/**
* Title:        $Source: /cvs/src/net/paymate/awtx/Targa.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.13 $
*/

import net.paymate.util.Streamer;

import net.paymate.jpos.awt.*;//most of these should move to this package

import net.paymate.util.*;

import java.awt.geom.Point2D;
import java.lang.Math;
import java.util.Vector;
import java.io.*;

/**
 * Targa file processor
 */
public class Targa {
  private static final ErrorLogStream dbg=new ErrorLogStream(Targa.class.getName());

  static final String ImageType(int code){
    switch (code) {
      case 0 : return "Hmmm. No Image data. .";
      case 1 : return "Uncompressed Color-Mapped image.";
      case 2 : return "Uncompressed True-Color image.";
      case 3 : return "Uncompressed Black & White image. Good.";
      case 9 : return "Compressed Color-Mapped image.";
      case 10: return "Compressed True-Color Image.";
      case 11: return "Compressed Black and White Image.";
    }
    return "Ummm, I can't read ImageType.";
  }

  public static final class ReadException extends Exception {
    ReadException(String theError){
      super(theError);
      dbg.ERROR(theError);
    }
  }

  public static final Raster parseStream(InputStream ksStream,int clipLevel) throws ReadException {
    dbg.Enter("parseStream");
    try {
    WordyInputStream winp=new WordyInputStream(ksStream,false);//little endian

    if(ksStream==null){
      throw new ReadException("Input Stream Not Present");
    }
    int width;
    int height;
    int depth;      //4debug
    int descriptor; //4debug
//    int y = 0;

    try {
      int temp;

      //The first byte is the Text-ID length. Not used.
      //The second byte is the Color-Map type. Not used.
      ksStream.skip(1);
      int colorMapType= ksStream.read(); //used to presume 00
      //the third byte is the Image-Type indicator..
      temp = ksStream.read();
      if(temp!=3 && temp!=1){
        throw new ReadException("Bad File Type:"+ImageType(temp));
      }

      //the next 5 bytes are color-map entries, lengths, etc. Not Used.
      ksStream.skip(5);

      //The next 4 bytes are pixel positions, usually 0. Not used.
      ksStream.skip(4);

      //Horizontal size of the image in Pixels. Important!
      width = winp.u16();

      //Vertical Size of the image in Pixels. Important!
      height = winp.u16();
      String blah="Image is " + height + " pixels high and "+width+" wide.";
      dbg.VERBOSE(blah);
      if(height>1023 || width >1023){//can't handle such a monstrous raster
        throw new ReadException("TOO BIG! "+blah);
      }
      depth = ksStream.read();

      switch (depth) {
        case 8 : dbg.VERBOSE("8 bpp. Correct color depth. Good. "); break;
        case 1 : throw new ReadException("Not enough color depth.");
        case 16:
        case 24: throw new ReadException("Too much Color-depth.");
        default: throw new ReadException("Can't determine color depth.");
      }

      //the last byte describes the picture orientation:
      descriptor = ksStream.read();
      boolean flipy=!Bool.bitpick(descriptor,5);//code originally built in y flipping
      boolean flipx= Bool.bitpick(descriptor,4);//but x was copacetic.

      if(colorMapType==1){//skip over 3 8 bit lookup tables
        ksStream.skip(256*3);
      }

      Raster raster=new Raster(width,height);
      for (int rastery = 0; rastery < height; ++rastery) {
        for (int rasterx = 0; rasterx < width; ++rasterx) {
          int octet=ksStream.read();
          if(octet==-1){
            dbg.ERROR("Graphics file read too few bytes.");
            break;
          }
          // the operator +~ is a cute way of doing lhs-1-rhs
          //we are mapping 0..n-1 to n-1..0
          raster.set( flipx?(width+~rasterx):rasterx,flipy?(height+~rastery):rastery,octet<=clipLevel);//we want 1's for black.
        }
      }
      return raster;
    } catch (java.io.IOException up){
      throw new ReadException("File Error"+up.getMessage());
    }
    }
    finally {
      dbg.Exit();
    }
  }

  public static final Raster readTarga(InputStream ksStream, int clipLevel){
    try {
      return Targa.parseStream(ksStream,clipLevel);
    } catch (Exception tre){
      dbg.ERROR("TargaReadError:"+tre.getMessage());
      return Raster.EmptyOne();
    }
  }

}
//$Source: /cvs/src/net/paymate/awtx/Targa.java,v $
