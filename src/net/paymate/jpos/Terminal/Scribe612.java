package net.paymate.jpos.Terminal;
/** Namespace for printer utilities
* Title:        Scribe612
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Scribe612.java,v 1.36 2001/10/03 12:51:07 mattm Exp $
*/

import  net.paymate.jpos.awt.*;
import  net.paymate.awtx.Targa;
import  net.paymate.awtx.print.*;
import  net.paymate.jpos.data.ByteBlock;
import  net.paymate.util.*;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.lang.Math;
import java.util.Vector;

import java.io.BufferedReader;

public class Scribe612 extends PrinterModel {
  static final ErrorLogStream dbg=new ErrorLogStream(Scribe612.class.getName());

  final static int P250textWidth=42; //in standard font...from word.doc version of manual

  public int textWidth(){
    return P250textWidth;
  }

  public Scribe612(LinePrinter lp) {
    super((lp!=null)?lp:(lp=new LinePrinter("FakingScribe612")));
    //a class is looming: 'printerAttributes'
    {
      rasterWidth=420; //due to protocol, note that text mode has 546 dots (13*42)
      Aspect=new Dimension(203,68);//from the manual 8 per mm
      sigBox= new Dimension(rasterWidth,30);//68ths of an inch
      //font metrics: 13 by 24 box, typically char is 10 dot wide + 3 spacing Helvetica
    }
    dbg.VERBOSE("Scribe612 printing via "+lp.getClass().getName());
  }

  public static final int preferredOutputBufferSize(){//nice sized output chunks
    return Math.max(P250textWidth+1,linesize*2);
  }

  //raster to byte data constants:
  static final byte P250Mode          =  0x1C;
  static final byte Escape            =  0x1B;
  static final byte gmode             =  0x67; //lower case g

  static final byte [] startRaster={P250Mode,Escape,gmode};


  //some text attribute instream codes:
  public static final byte WideON            =  0x1E;
  public static final byte WideOFF           =  0x1F;
  //graphic commands
  static final byte terminatorOdd     =  0x20; //odds and just hold on
  static final byte terminatorEven    =  0x25; //evens and print line
  static final byte exitGraphics      =  0x08; //merge into either terminator above

//conifguration values: (slightly off for traceability of dynamic config)
  protected int rasterPadChars=37;  //number of padding bytes per raster line
  protected int textPadChars=36;  //number of padding bytes per raster line

  public PrinterModel configure(EasyCursor ezp){
    dbg.Enter("configure");
//    ezp.push("P612");
    try {
      rasterPadChars=ezp.getInt("rasterPad",rasterPadChars);
      textPadChars=ezp.getInt("textPad",textPadChars);
      setLineFeed(10,textPadChars); //uses newline for line feed
      setFormFeed(12,5*textPadChars); //a formfeed is about 5 lines according to manual
      dbg.VERBOSE("P612 padding:"+textPadChars+" /"+rasterPadChars);
    }
    finally{
//      ezp.pop();
      dbg.Exit();
      return super.configure(ezp);
    }
  }

  /**
  * @return send printer command string that precedes graphics data blobs
  */
  public PrinterModel startGraphics(){
    dbg.Enter("startGraphics");
    try {
      lp.Print(startRaster);
      return super.startGraphics();
    }
    finally {
      dbg.Exit();
    }
  }

  public PrinterModel startText(){
    dbg.Enter("startText");
    try {
      lp.Print(P250Mode); //needed to get 42 column text, else is 40 columns.
      return super.startText();
    }
    finally {
      dbg.Exit();
    }
  }

  final static int linesize=(420/12)+1;//36

  /**
  * @param raster image in 4th quadrant coordinates
  * @return printer data, still needs graphics header @see #startGraphics
  */

  public ByteBlock fromRaster(Raster raster){
    //graphics must be sent in increments of 12 pixels, and we need a byte for a line terminator
    dbg.Enter("fromRaster");
    try {
    int linesize=Math.min(1+((raster.Width()+11))/12,Scribe612.linesize);
    int bufsize=linesize+rasterPadChars;
    ByteBlock temp=new ByteBlock(raster.Height(),2*bufsize);
    byte[][] buffer=temp.raw(); //---

    for (int rastery = 0; rastery < raster.Height(); rastery++) {
      byte []toprint = buffer[rastery];//these point into buffer!
      for (int bytepick = linesize-1; bytepick-->0;) {//-1=room for terminator
        byte oddhex = 0x40; //book says to always set this bit, makes data be in ascii range
        byte evenhex = 0x40;
        for (int bitindex = 6; bitindex-->0;) {
          int bitpick=2*(6*bytepick+bitindex);
          if(raster.pixel(bitpick  ,rastery)){
            evenhex |= 1 << (5-bitindex);
          }
          if(raster.pixel(bitpick+1,rastery)){
            oddhex  |= 1 << (5-bitindex);
          }
        }
        toprint[bytepick]         = oddhex;
        toprint[bytepick+bufsize] = evenhex;
      }
      toprint[linesize-1]         =terminatorOdd;
      toprint[linesize-1+bufsize] =terminatorEven;
      if(false){//false==trusting array inits to zero
        for(int padder=bufsize;padder-->bufsize;){
          toprint[padder]         =0;
          toprint[padder+bufsize] =0;
        }
      }
    }
    //on last line modify final linefeed:
    buffer[raster.Height()-1][linesize-1+bufsize]|=exitGraphics;
    return temp;
    }
    finally {
      dbg.Exit();
    }
  }
////
/**
 * this had better only be called for text or complete lines of graphics..
 */
  public boolean print(byte[] bytes) {
    dbg.VERBOSE("printing:"+Safe.ox2(bytes[0]));
//we can prefix graphics and text identically:
    return super.print(Safe.insert(bytes,(int)P250Mode,0));
  }

}
//$Id: Scribe612.java,v 1.36 2001/10/03 12:51:07 mattm Exp $
