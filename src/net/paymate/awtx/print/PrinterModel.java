/**
 * Title:        $Source: /cvs/src/net/paymate/awtx/print/PrinterModel.java,v $
 * Description:  base class for printer output formatting <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: PrinterModel.java,v 1.61 2004/02/26 18:40:50 andyh Exp $
 */
package net.paymate.awtx.print;

import net.paymate.lang.Value;
import net.paymate.lang.ReflectX;
import  net.paymate.util.*;
import  net.paymate.jpos.Terminal.LinePrinter;
import  net.paymate.jpos.awt.Hancock;
import  net.paymate.jpos.data.ByteBlock;
import net.paymate.lang.StringX;
import  net.paymate.jpos.awt.Raster;
import  net.paymate.awtx.*;
import  net.paymate.awtx.print.*;
import  net.paymate.jpos.data.ByteBlock;
import java.lang.Math;
import java.util.Vector;

import java.io.*;

public class PrinterModel {
  protected ErrorLogStream dbg;
//while each object gets its own stream they share loglevels. this is what we should USUALLY do, rather than our static ones.
  protected LinePrinter lp;

  public void disConnect(){
//    lp.
  }

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
  public int rasterWidth=320;//0 caused defects when running ReceiptAgent.main(...)
  protected int myTextWidth=40;//0 caused an infinite loop when used via NullPrinter.

  public int textWidth(){//because it may not be a construction-time constant.
    return myTextWidth;
  }

  /** shape of a printer dot in dpi by dpi: */
  public /*final*/ XDimension Aspect=new XDimension(1,1);//square
  public Quadrant quad=Quadrant.Fourth();//most common choice.

  private byte [] makeFeeder(int lf,int padding){
    if(padding<0){//so that formulae can be sloppy
      padding=0; //minumum padding == none
    }
    byte [] feed= new byte[padding+1];
    feed[0]=(byte)lf;
    return feed;
  }

  /** lineFeed is byte not char because it is used by raw printing */
  public byte [] lineFeed={Ascii.LF};//set to most common value

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
  public XDimension sigBox= new XDimension(rasterWidth,30);//72nds of an inch

  protected PrinterModel() {
    dbg = ErrorLogStream.getForClass(this.getClass());
  }

/**
 * @param lp is the physical printer interface
 */
  /*protected*/ public PrinterModel(LinePrinter lp) {
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
    int payload=StringX.lengthOf(stuff);
    if(payload>textWidth()){
      TextList wrapper=new TextList(stuff,textWidth(),TextList.SMARTWRAP_ON);// as in "word wrap"
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
        int payload=StringX.lengthOf(stuff);
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

  /**
   * prints each item, wrapping each line individually.
   * does NOT merge tail of one wrapped line with next item.
   * NB: extracting an array from the textlist and using the String[] method would
   * be expensive as that function makes a COPY of the textlist's content
   */
  public void print(TextList textList){
    int size=textList.size();
    for(int i =0 ; i<size;i++) {//!!retain given order
      println(textList.itemAt(i));
    }
  }

  /**
   * prints each item, wrapping each line individually.
   * does NOT merge tail of one wrapped line with next item.
   */
  public void print(String [] textList){
    int size=textList.length;
    for(int i =0 ; i<size;i++) {//!!retain given order
      println(textList[i]);
    }
  }

  public void print(FormattedLineItem fl) {
    if(fl != null) {//this test simplifies conditionally presented lines at a higher level
      println(fl.formatted(textWidth()));
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
    print(FormattedLineItem.pair(name,value));
  }

  public void printPage(String [] content){
    startPage();
    try {
      print(content);
      formfeed();
    }
    finally {
      endPage();
    }
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
      ", Aspect.width=" + Aspect.width + ", Aspect.height=" + Aspect.height+", Quadrant="+quad.toString());
    try {
      Hancock cooked=hk.remapped(sigBox,Aspect,quad);
      print(cooked.rasterTo(sigBox)/*.box()*/);//signature boxing
    } catch (Exception e) {
      dbg.Caught(e);
    }
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
    if(lp!=null){
      lp.setGraphing(amGraphing=true);
    }
    return this;
  }

  public PrinterModel startText(){//usually overridden
    if(lp!=null){
      lp.setGraphing(amGraphing=false);
    }
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
    return ReflectX.shortClassName(this)+ (lp!=null? (" on "+ReflectX.shortClassName(lp)):", abstract");
  }

  public boolean HasCutter(){//override for printers that cut paper on formfeed.
    return false;
  }
///////////
  public static PrinterModel BugPrinter(int width){
    PrinterModel newone=new PrinterModel(net.paymate.awtx.print.StreamPrinter.Out());
    newone.myTextWidth=width;
    newone.dbg.setLevel(LogSwitch.ERROR);//suppress logstream
    return newone;
  }

  public static PrinterModel Null(){
    PrinterModel newone=new PrinterModel(null);
    newone.myTextWidth=0;
    newone.dbg.setLevel(LogSwitch.OFF);//suppress logstream
    return newone;
  }

}
//$Id: PrinterModel.java,v 1.61 2004/02/26 18:40:50 andyh Exp $
