package net.paymate.ivicm.et1K;
/* $Id: ncraSignature.java,v 1.6 2001/10/18 05:33:03 andyh Exp $ */

import java.awt.Point;
import java.util.Vector;
import net.paymate.awtx.WordyInputStream;//bad location , but good class.

import net.paymate.util.*;

public class ncraSignature  {
  static final Tracer dbg=new Tracer(ncraSignature.class.getName(),Tracer.ERROR);
  public static final Point MARK=new Point(-1, -1);

  protected byte rawData[];
  /////////////////////////////
  // ncra parser components, need to create ncrIstream() out of this.
  protected final int rawDatum(int index){//byte as unsigned 8 bit integer
    return rawData[index]&255;
  }

  private int cursor;//into rawdata when parsing.
  private int next(){
    return rawData[++cursor]&255;//need unsgined most of the time.
  }

  private boolean stillGot(int thismany){
    return cursor+thismany < rawData.length;
  }

  private int highLow(){
    return (next()<<8)+next();
  }

  protected int lowHigh(){
    return next()+(next()<<8);
  }
//////////////////////
//
  protected Point Points[]=null;

  private void parseIfNeeded(){
    dbg.mark("parseIfNeeded");
    if(Points==null || Points.length==0){
      if(rawData!=null){
        parsePoints();
      } else {
        Points=new Point[0]; //trivial but not null.
      }
    }
  }

  public Point[] getPoints(){
    parseIfNeeded();
    return Points;
  }

  public byte[] getRawData(){
    return rawData;
  }

  protected void setRawData(byte rawdata[]){
    rawData = rawdata;
  }

  public static final ncraSignature fromRawData(byte [] raw){
    ncraSignature thisone= new ncraSignature();
    thisone.setRawData(raw);
    thisone.checkSigType();
//deferred    thisone.parsePoints ();
    return thisone;
  }

  boolean isNCR=false;
  protected void checkSigType(){
    isNCR=rawData.length > 6 && rawData[0] == 1 && rawData[1] == 17;
  }

  public void parsePoints(){
    dbg.mark("parsePoints()");
    Points= isNCR? parseNCR(): new Point[0];
  }

  protected Point[] parseNCR(){
    dbg.Enter("parseNCR");
    try {
      cursor=2; //points to last byte of header.
      int lookahead=0;//most recently referneced byte from buffer
      //for bringing data back into nominal 512 dpi coordinate space.
      int missinglsbs = 0;
      int rounder = 0;
      //these two are not folded into a point because class Point wants them separate.
      int xoff = 0;
      int yoff = 0;

      Vector vector = new Vector();
      missinglsbs = 3 - (next() & 0xf);//third byte of header is compression code
      //jigger the signfilling shift to maintain same grid as absolute coords
      int shiftback= 32 -5 - missinglsbs;

      Point abs=new Point(); //absolute
      dbg.WARNING("shifts:"+shiftback);
      int strokecount=0; //4debug
      while(stillGot(6)){//6== abs format stroke header length
        ++strokecount;
        //        dbg.WARNING("@stroke:"+ strokecount+" Dav="+data.is().available());
        int strokeLength= highLow();//+_+ ignoring MSB
        if((strokeLength&0xF000)!=0x8000){
          //reject stroke
          return new Point[0];
        }
        strokeLength&=0xFFF;
        abs.y = lowHigh();
        abs.x = lowHigh();
        dbg.WARNING("stroke["+ strokecount+"] Length:"+strokeLength+" starts at:"+abs.x+":"+abs.y);
        vector.addElement(abs.clone());
        --strokeLength;//account for the absolute point from header
        int bytesreqd= ((strokeLength*10)+7)/8;
        if(stillGot(bytesreqd))
        for(int flub=0;flub<strokeLength;flub++) {
          //for each 5 bit field add 5 to shift amount of previous.
          //if that goes over 8 subtract 8,
          //the second piece of a split field is shifted 8 less than the first piece.
          switch(flub&3){
            case 0:{//on boundary
              lookahead=next();//();
              yoff=lookahead<<(24);   //0
              xoff=lookahead<<(24+5); //0+5=5
              lookahead=next();
              xoff|= lookahead<<(24-3);
            } break;
            case 1:{
              yoff=lookahead<<(24+2); //5+5=10,-8= 2
              xoff=lookahead<<(24+7); //2+5=7
              lookahead=next();
              xoff|= lookahead<<(24-1);
            } break;
            case 2:{
              yoff=lookahead<<(24+4);   //7+5=12 , -8 = 4
              lookahead=next();//();
              yoff|= lookahead<<(24-4);
              xoff=lookahead<<(24+1);   //4+5=9 , -8=1
            } break;
            case 3:{
              yoff=lookahead<<(24+6);   //1+5 = 6
              lookahead=next();
              yoff |= lookahead<<(24-2);
              xoff=lookahead<<(24+3); //6+5=11, -8 = 3
            } break;
          }
          //only the 5 msbs belong here.
          yoff&=0xf8000000;
          xoff&=0xf8000000;
          // rounder: 0.5 is a 1 in the sixth bit
          yoff|=0x04000000;
          xoff|=0x04000000;
          // move back to scaling of absolute coordinates
          yoff >>= shiftback;
          xoff >>= shiftback;

          dbg.VERBOSE("offset: "+xoff+":"+yoff);
          //points are relative to previous point
          abs.translate(xoff,yoff);
          dbg.VERBOSE("Adding Point: "+abs.x+":"+abs.y);
          vector.addElement(abs.clone());//cloning is essential here!
        }//end stroke
        vector.addElement(MARK);//end of stroke
      }
      //convert from vector to array now that we know the size.
      Point [] pa = new Point[vector.size()];
      for(int i = pa.length; i-->0;) {
        pa[i] = (Point)vector.elementAt(i);
      }
      return pa;
    }
    finally {
      dbg.Exit();
    }
  }

}
//$Id: ncraSignature.java,v 1.6 2001/10/18 05:33:03 andyh Exp $
