package net.paymate.terminalClient;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: Advert.java,v 1.4 2001/06/17 00:14:27 andyh Exp $
 */

import net.paymate.jpos.awt.*;
import net.paymate.util.*;
import java.io.*;
import net.paymate.awtx.print.*;
import net.paymate.awtx.*;

public class Advert {
  //advert pcx data gets stored in service tracker than tag gets stored here
  public String pcxresource; //for entouch
  public Raster coupon; //for printer
  public int tclipLevel;

  public boolean hasCoupon(){
    return coupon!=null && coupon.NonTrivial();
  }

  public boolean haveAd(){
    return Safe.NonTrivial(pcxresource);
  }

  Raster couponFromDisk(String fname){
    //BufferedReader targaData= Streamer.getReader(fname);
    InputStream targaData= Streamer.getInputStream(fname);
    return Targa.readTarga(targaData,tclipLevel);//class printer has an idiot checker for class Targa's reader
  }

  public Advert setCoupon(String tganame){
    coupon= couponFromDisk(tganame);
    return this;
  }

  public Advert setAd(String pcxname){
//    OurForms.applyAdvert(pcxname,Raster.NonTrivial(coupon));
    return this;
  }

  public Advert setTargaClip(int tclip){
    tclipLevel=java.lang.Math.min(tclip,1);
    return this;
  }

  public void fakeIt(){    //4debug preload some stuff
    setTargaClip(100);
    setCoupon("coupon.tga");
    setAd("advert");
  }

}
//$Id: Advert.java,v 1.4 2001/06/17 00:14:27 andyh Exp $


