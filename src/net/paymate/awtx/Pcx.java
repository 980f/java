/**
* Title:        $Source: /cvs/src/net/paymate/awtx/Pcx.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Pcx.java,v 1.14 2003/12/08 22:45:40 mattm Exp $
*/
package net.paymate.awtx;
import net.paymate.util.ErrorLogStream;
import net.paymate.jpos.awt.Raster;//wierd location
import net.paymate.awtx.*;
import java.io.*;

public class Pcx {

  public static final byte [] packedLine(boolean [] rasterLine){
    int bytewidth=(rasterLine.length+7)/8;//round up
    byte [] byteline=new byte[bytewidth];
    int packout=0;
    int packed=0;
    int mask=1<<7;
    for(int packer=0;packer<rasterLine.length;packer++){
      if(!rasterLine[packer]){//1 for white
        packed|=mask;
      }
      if((mask>>=1)==0){
        byteline[packout++]=(byte)packed;
        packed=0;
        mask=1<<7;
      }
    }
    if(rasterLine.length%8!=0){//factional final byte
      byteline[packout++]=(byte)packed;
    }
    return byteline;
  }

  protected static final void encodeEmit(OutputStream os, int runLength, int datum) throws java.io.IOException {
    if(runLength>1||datum>=192){
      os.write(192+runLength);
    } //else skip outputing the run length
    os.write(datum);
  }

  public static final OutputStream encode(Raster image,OutputStream os)throws java.io.IOException{
    WordyOutputStream wus=new WordyOutputStream(os);
    os.write(10);
    os.write(5);//perhaps this is what ivi measn by version 5???
    os.write(1);//alwasyu compressed
    os.write(1);//our rasters are alwasy one bit per pixel
    wus.word(0).word(0).word(image.Width()-1).word(image.Height()-1);
    wus.word(0);//H dpi
    wus.word(0);//V dpi
    os.write(new byte[3*16]);//unused VGA palette
    os.write(0);//reserved
    os.write(1);//number of planes
    int bytewidth=(image.Width()+15)/8;//round way up
    bytewidth&=~1;//and then force even
    wus.word(bytewidth);
    wus.word(0);// palette type code
    wus.word(image.Width());//h pixels
    wus.word(image.Height());
    os.write(new byte[54]);//filler
    //header is complete

    for(int ri=0;ri<image.Height();ri++){//encoding is on scan line boundaries
      byte []byteline=packedLine(image.line(ri));
      int reader=0;
      int previous=255&byteline[reader];//will baos if it works out
      int current;//was using byte but sign extension was byting us
      int runLength=1;
      while(++reader<byteline.length){
        current =255&byteline[reader];
        if(current!=previous){//run ended
          encodeEmit(os,runLength,previous);
          previous=current;
          runLength=1;
        } else {
          if(++runLength==63){//maximum run
            encodeEmit(os,runLength,previous);
            runLength=1;
          } else {
            //run continues
          }
        }
      }
      encodeEmit(os,runLength,previous);
    }
    return os;
  }

  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(Pcx.class);

  protected static final void Assert(boolean assertion,String explanation,int actual){
    if(!assertion){
     dbg.WARNING(explanation+actual);
    }
  }

  protected static final Raster decode(XDimension shape, InputStream is, int bytewidth) throws java.io.IOException {
    Raster deck=new Raster(shape);
    int runner;
    int runLength;
    int count;//pixel count
    for(int ri=0;ri<shape.height;ri++){
      boolean [] rasterLine=deck.line(ri);
      count=0;
      while(count<shape.width){
        runner=is.read();
        if(runner<192){
          runLength=1;
//are implied bits 1's or 0's?
          runner&=63; //uncommneted==zeroess, commented out==ones
        } else {
          runLength=runner-192;
          runner=is.read();
        }
        for(int mask=0x80;(mask>>=1)!=0 && count<shape.width;){
          rasterLine[count++]=(runner&mask)==0;//1==white,0==black.
        }
        while(--runLength>0){
          //copy previous 8
          int len=Math.min(8,shape.width-count);
          System.arraycopy(rasterLine,count-8,rasterLine,count,len);
          count+=len;
        }
      }
    }
    return deck;
  }

  public static final Raster decode(InputStream is){
    Raster ret = null;
    try {
      WordyInputStream wis=new WordyInputStream(is);
      int item;
      Assert((item=wis.unsigned8())==10,"header[0] !=10:",item);
      Assert((item=wis.unsigned8())>=5,"unknown version:",item);
      Assert((item=wis.unsigned8())==1,"should be 1:",item);
      Assert((item=wis.unsigned8())==1,"bits per pixel should be 1:",item);
      XDimension outline=new XDimension(wis.point(),wis.point());
      XDimension aspect=wis.Xdimension();
      is.skip(3*16);//unused EGA/VGA palette
      is.skip(1);//ignore value of reserved items
      Assert((item=wis.unsigned8())==1,"#planes should be 1:",item);
      int bytewidth=wis.u16();
      is.skip(1);// palette type code
      XDimension shape=wis.Xdimension() ;
      //compare shape to outline's dimensions
      Assert(outline.getSize().equals(shape),"inconsistent bounds and size",0);
      is.skip(54);//filler
      //header is complete
      ret = decode(shape,is,bytewidth);
    } catch (Exception any){
      ret = Raster.EmptyOne();
    } finally {
      return ret;
    }
  }

}
//$Id: Pcx.java,v 1.14 2003/12/08 22:45:40 mattm Exp $
