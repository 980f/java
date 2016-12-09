/**
 * Title:        PrinterModel<p>
 * Description:  base class for printer output formatting <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: PrinterModel.java,v 1.37 2001/10/22 23:33:37 andyh Exp $
 */
package net.paymate.awtx.print;

import net.paymate.data.Value;

import  net.paymate.util.*;
import  net.paymate.jpos.Terminal.LinePrinter;
import  net.paymate.jpos.awt.Hancock;
import  net.paymate.jpos.data.ByteBlock;

import  net.paymate.jpos.awt.Raster;
import  net.paymate.awtx.Targa;
import  net.paymate.awtx.print.*;
import  net.paymate.jpos.data.ByteBlock;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.lang.Math;
import java.util.Vector;

import java.io.*;

public class PrinterModel {
  protected static final ErrorLogStream dbg = new ErrorLogStream(PrinterModel.class.getName());
  protected LinePrinter lp = new LinePrinter("PrinterModelInit");//prints to debug stream

  public void startPage (){
    if(lp!=null){
      lp.startPage();
    }
  }

  public void endPage (){
    if(lp!=null){
      lp.endPage();
    }
  }

  //a class is looming: 'printerAttributes'
  public int rasterWidth=0;
  protected int myTextWidth=40;//0 caused an infinite loop when used via NullPrinter.

  public int textWidth(){//because it may not be a construction-time constant.
    return myTextWidth;
  }

  /** shape of a printer dot in dpi by dpi: */
  public /*final*/ Dimension Aspect=new Dimension(1,1);//square

  private byte [] makeFeeder(int lf,int padding){
    if(padding<0){//so that formulae can be sloppy
      padding=0; //minumum padding == none
    }
    byte [] feed= new byte[padding+1];
    feed[0]=(byte)lf;
    return feed;
  }

  /** lineFeed is byte not char because it is used by raw printing */
  public byte [] lineFeed={'\n'};//set to most common value

  public PrinterModel setLineFeed(int lf,int padding){
    lineFeed= makeFeeder(lf,padding);
    return this;
  }

     /** formFeed is byte not char because it is used by raw printing */
  public byte [] formFeed={12};//set to most common value

  public PrinterModel setFormFeed(int ff,int padding){
    formFeed= makeFeeder(ff,padding);
    return this;
  }

  /**
  * A best size for printing signatures, +_+ can be formulaicly derived from
  * abstract values and the Aspect and rasterWidth members. i.e. inches and dpi.
  */
  public Dimension sigBox= new Dimension(rasterWidth,30);//72nds of an inch

  public PrinterModel() {
    // stub
  }

/**
 * @param lp is the physical printer interface
 */
  public PrinterModel(LinePrinter lp) {
    this();
    // +_+ create a null printer here if lp==null?
    this.lp = lp;
  }

/**
 * a clue for outside agents to adjust I/O
 */
  protected boolean amGraphing=false;
  public boolean isGraphing(){
    return amGraphing;
  }

/**
 * @param stuff is a string that might need to be wrapped to fit.
 * @return whether we wrapped and therfore did all of the printing here.
 */
  protected boolean autoWrap(String stuff){
    int payload=Safe.lengthOf(stuff);
    if(payload>this.textWidth()){
      TextList wrapper=new TextList(stuff,this.textWidth(),TextList.SMARTWRAP_ON);// as in "word wrap"
      print(wrapper);//which calls back into println(String)
      return true;
    }
    return false;
  }
  //////////////////////////////////
/// text printing
/**
 * all text printing eventually goes through here
 * @param stuff text to print, line terminator will be added
 */
  public void println(String stuff) {
    dbg.VERBOSE("println: <"+stuff+">");
    if(lp != null ) {
      if(!autoWrap(stuff)){//else autoWrap has already called back to here, twice!
        int payload=Safe.lengthOf(stuff);
        int padding=lineFeed.length;
        byte [] terminated=new byte[payload+padding];
        if(stuff!=null){
          System.arraycopy(stuff.getBytes(),0,terminated,0,payload);
        }
        System.arraycopy(lineFeed,0,terminated,payload,padding);
        print(terminated);
      }
    }
  }

  public void println() {
    // prints an empty line (maybe)
    println("");
  }

  public void print(TextList textList){
    int size=textList.size();
    for(int i =0 ; i<size;i++) {//!!retain given order
      println(textList.itemAt(i));
    }
  }

  public void print(FormattedLineItem fl) {
    String str = "";
    if(fl != null) {//this test simplifies conditionally presented lines at a higher level
//    dbg.Message("FORMATTING:"+fl.toSpam());
      switch(fl.justification.Value()) {
        case ColumnJustification.PLAIN: {
          str = fl.name + Safe.TrivialDefault(fl.value , "");
        } break;
        case ColumnJustification.JUSTIFIED: {
          str = Fstring.justified(textWidth(), fl.name,fl.value, (fl.filler == 0) ? '.' : fl.filler);
        } break;
        case ColumnJustification.CENTERED: {
          str = Fstring.centered(fl.name, textWidth(), fl.filler);
        } break;
        case ColumnJustification.WINGED: {
          str = Fstring.winged(fl.name, textWidth());
        } break;
      }
      println(str);
    }
  }

  public void print(FormattedLines fls) {
    if(fls != null) {
      for(int i = 0; i < fls.size(); i++) {
        print(fls.itemAt(i));
      }
    }
  }

  public void print(String name,String value){//FUE
    print(new FormattedLineItem(name,value));
  }

  ///////////////////////////////////
  // graphics printing

  public PrinterModel print(Raster rasta){
    startGraphics();
    print(fromRaster(rasta));
    return this;
  }

  public PrinterModel print(Hancock hk) {
    dbg.VERBOSE( "print(Hancock): " + "sigBox.width=" + sigBox.width + ", sigBox.height=" + sigBox.height +
      ", Aspect.width=" + Aspect.width + ", Aspect.height=" + Aspect.height);
    Hancock cooked=hk.remapped(sigBox,Aspect);
    print(cooked.rasterTo(sigBox)/*.box()*/);//signature boxing
    return this;
  }

  /////////////////////////////////////////////////////////
  //actual printing
  //sometimes overridden in extensions
  public boolean print(byte[] bytes) {
    return (lp != null) ? lp.Print(bytes) : false;
  }

  public int print(ByteBlock bb) {
    return (lp != null) ? lp.Print(bb) : 0;
  }
  /////////////////////////////////////////////////////////
  // normally overridden and super'd in extensions:
  public PrinterModel configure(EasyCursor ezp){
    startText();
    return this;
  }

  public static int preferredOutputBufferSize(){//nice sized output chunks
    return 0;
  }

  public PrinterModel startGraphics(){//usually overridden
  //usually an escape sequence, such as esc K for Epson's
    lp.setGraphing(amGraphing=true);
    return this;
  }

  public PrinterModel startText(){//usually overridden
    lp.setGraphing(amGraphing=false);
    return this;
  }

  public ByteBlock fromRaster(Raster raster){
   dbg.VERBOSE("FORMAT GRAPHIC");
    return ByteBlock.EmptyBlock();//usually overridden!
  }

  public boolean formfeed() {
    dbg.VERBOSE("FORM FEED");
    return lp!=null && lp.Print(formFeed);
  }

  public String toSpam(){
    return this.getClass().getName()+ (lp!=null? (" on "+lp.getClass().getName()):", abstract");
  }

}
//$Id: PrinterModel.java,v 1.37 2001/10/22 23:33:37 andyh Exp $
