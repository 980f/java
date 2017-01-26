package net.paymate.ivicm.et1K;
/**
* Title:         $Source: /cvs/src/net/paymate/ivicm/et1K/ncraSignature.java,v $
* Description:   BAD NAME; was just ncra, but now is hypercom, too.  <BR>
*                converts hypercom and ncra signatures into jpos-style XPoint arrays.
*                should be renamed PenSignature +++ also move to np.data! +++
* Copyright:     Copyright (c) 2001
* Company:       PayMate.net
* @author        PayMate.net
* @version       $Revision: 1.18 $
* @todo   replace byte array with packet and add "wordyinputstream" members to packet.
*/

import net.paymate.awtx.XPoint;
import java.util.Vector;
import net.paymate.hypercom.SignatureParser;
import net.paymate.util.*;

public class ncraSignature  {
  static final Tracer dbg=new Tracer(ncraSignature.class,Tracer.ERROR);

  private byte rawData[];
  private XPoint points[]=null;
  private SignatureType type = new SignatureType();

  public boolean NonTrivial(){
    return rawData!=null || (points != null && points.length>2);//2 presumes one value is MARK
  }

  public static boolean NonTrivial(ncraSignature probate){
    return probate!=null && probate.NonTrivial();
  }

  public SignatureType getType() {
    return new SignatureType(type.Value()); // +++ revisit TrueEnum.clone()
  }

  /////////////////////////////
  // ncra parser components, need to create ncrIstream() out of this.
  protected final int rawDatum(int index){//byte as unsigned 8 bit integer
    return rawData[index]&255;
  }

  private int cursor;//into rawdata when parsing.
  private int next(){
    return rawData[++cursor]&255;//need unsigned most of the time.
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

  private void parseIfNeeded(){
    dbg.mark("parseIfNeeded");
    if(points==null || points.length==0){
      if(rawData!=null){
        parsePoints();
      } else {
        points=new XPoint[0]; //trivial but not null.
      }
    }
  }

  public XPoint[] getPoints(){
    parseIfNeeded();
    return points;
  }

  public byte[] getRawData(){
    return rawData;
  }

  protected void setRawData(byte rawdata[]){
    rawData = rawdata;
  }

  public static final ncraSignature fromRawData(byte [] raw, SignatureType type){
    ncraSignature thisone= new ncraSignature();
    thisone.setRawData(raw);
    thisone.setSigType(type);
    return thisone;
  }

  boolean isNCR() {
    return type.is(SignatureType.NCRA);
  }

  private void setSigType(SignatureType newtype){
    dbg.VERBOSE("about to set type = " + newtype);
    try {
      type.setto(SignatureType.Invalid());
      dbg.VERBOSE("switching on newtype of " + newtype.Image());
      switch(newtype.Value()) {
        case SignatureType.NCRA: {
          dbg.WARNING("Checking out NCRASignature");
          if(ncraChecksOut()) {
            dbg.WARNING("NCRASignature checks out");
            type.setto(SignatureType.NCRA);
          } else {
            dbg.ERROR("attempted to set type to NCRASignature, but did not check out");
          }
        } break;
        case SignatureType.Hypercom: {
          dbg.WARNING("Checking out HypercomSignature");
          if(hyperChecksOut()) {
            dbg.WARNING("HypercomSignature checks out");
            type.setto(SignatureType.Hypercom);
          } else {
            dbg.ERROR("attempted to set type to HypercomSignature, but did not check out");
          }
        } break;
        default: {
          dbg.WARNING("Setting the type to invalid since the newtype is " + newtype.Image());
        } break;
      }
    } catch (Exception ex) {
      dbg.ERROR("Had an error");
      dbg.Caught(ex);
    } finally {
      dbg.VERBOSE("set the sig type to "+type.Image());
    }
  }

  private boolean havebytes() {
    return rawData!=null;
  }

  // probably should move this code out to the entouch +++
  private boolean ncraChecksOut() {
    return havebytes() && rawData.length > 6 && rawData[0] == 1 && rawData[1] == 17;
  }

  private boolean hyperChecksOut() {
    return havebytes();// && pmSignatureImageGen.checkFormat(rawData);
  }

  private void parsePoints(){
    dbg.mark("parsePoints()");
    switch(type.Value()) {
      case SignatureType.NCRA: {
        points = parseNCR();
      } break;
      case SignatureType.Hypercom: {
        points = parseHyper();
      } break;
      default: {
        dbg.VERBOSE("not type set; making an empty pointset");
        points = new XPoint[0];
      }
    }
  }

  private XPoint[] parseHyper() {
    dbg.Enter("parseHyper");
    try {
      return SignatureParser.parse(rawData);
    } catch (Exception e) {
      dbg.Caught(e);
      return new XPoint[0];
    } finally {
      dbg.Exit();
    }
  }

  private XPoint[] parseNCR(){
    dbg.Enter("parseNCR");
    try {
      cursor=2; //points to last byte of header.
      int lookahead=0;//most recently referneced byte from buffer
      //for bringing data back into nominal 512 dpi coordinate space.
      int missinglsbs = 0;
      int rounder = 0;
      //these two are not folded into a point because class XPoint wants them separate.
      int xoff = 0;
      int yoff = 0;

      Vector vector = new Vector();
      missinglsbs = 3 - (next() & 0xf);//third byte of header is compression code
      //jigger the signfilling shift to maintain same grid as absolute coords
      int shiftback= 32 -5 - missinglsbs;

      XPoint abs=new XPoint(); //absolute
      dbg.WARNING("shifts:"+shiftback);
      int strokecount=0; //4debug
      while(stillGot(6)) {//6== abs format stroke header length
        ++strokecount;
        //        dbg.WARNING("@stroke:"+ strokecount+" Dav="+data.is().available());
        int strokeLength= highLow();//+_+ ignoring MSB
        if((strokeLength&0xF000)!=0x8000){
          //reject stroke
          return new XPoint[0];
        }
        strokeLength&=0xFFF;
        abs.y = lowHigh();
        abs.x = lowHigh();
        dbg.WARNING("stroke["+ strokecount+"] Length:"+strokeLength+" starts at:"+abs.x+":"+abs.y);
        vector.addElement(abs.clone());
        --strokeLength;//account for the absolute point from header
        int bytesreqd= ((strokeLength*10)+7)/8;
        if(stillGot(bytesreqd)) {
          for(int flub=0;flub<strokeLength;++flub) {
            //for each 5 bit field add 5 to shift amount of previous.
            //if that goes over 8 subtract 8,
            //the second piece of a split field is shifted 8 less than the first piece.
            switch(flub&3) {
            case 0:{ //on boundary
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
          dbg.VERBOSE("Adding XPoint: "+abs.x+":"+abs.y);
          vector.addElement(abs.clone());//cloning is essential here!
          }//end stroke
        }
        vector.addElement(net.paymate.jpos.data.Signature.MARK.clone());//end of stroke
      }
      //convert from vector to array now that we know the size.
      XPoint [] pa = new XPoint[vector.size()];
      for(int i = pa.length; i-->0;) {
        pa[i] = (XPoint)vector.elementAt(i);
      }
      return pa;
    } finally {
      dbg.Exit();
    }
  }

}
//$Id: ncraSignature.java,v 1.18 2003/12/08 22:45:42 mattm Exp $
