/**
* Title:        Raster
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Raster.java,v 1.13 2003/12/08 22:45:42 mattm Exp $
*/
package net.paymate.jpos.awt;
import net.paymate.awtx.XPoint;
import net.paymate.awtx.*;

public class Raster {//+_+ replace with awt canvas

  boolean [][] content;

  int width;
  int height;

/**
 * @return true if both dimensions are > 0
 */
  public boolean NonTrivial(){//non zero size
    return width>0 && height>0 /* && content!=null*/; //third term was redundant
  }

  public static final boolean NonTrivial(Raster one){//non zero size
    return one!=null && one.NonTrivial();
  }

  public int Height(){
    return height;
  }

  public int Width(){
    return width;
  }

  public boolean validX(int x){
    return x>=0&&x<width;
  }

  public boolean validY(int y){
    return y>=0 && y< height;
  }

  public boolean validPoint(int x,int y){
    return validX(x)&&validY(y);
  }

  public boolean validPoint(XPoint p){
    return validPoint(p.x,p.y);
  }

  public boolean[] line(int y){
    return validY(y)? content[y]:  new boolean[width];
  }

  public boolean pixel(int x,int y){
    return validPoint(x,y)? content[y][x] : false;
  }

  public boolean pixel(XPoint p){
    return pixel(p.x,p.y);
  }

  public boolean set(int x,int y,boolean value){
    if(validPoint(x,y)){
      content[y][x]=value;
      return true;
    }
    return false;
  }

  public Raster setHorizontalRule(int y){//+++ add width and centering?
    if(validY(y)){
      for(int x=content[y].length;x-->0;){
        content[y][x]=true;
      }
    }
    return this;
  }

  public Raster setVerticalRule(int x){//+++ add width and centering?
    if(validX(x)){
      for(int y=content.length;y-->0;){
        content[y][x]=true;
      }
    }
    return this;
  }

  public Raster box(){
    setVerticalRule(0);
    setVerticalRule(width-1);
    setHorizontalRule(0);
    setHorizontalRule(height-1);
    return this;
  }

  public Raster checkerBoard(int cellsize){
    int y=0;
    int x=0;
    try{
    for(y=height;y-->0;){
      for(x=width;x-->0;){
        //maximally computationally intensive!!
        content[y][x]= ((x/cellsize)&1)!=((y/cellsize)&1);
      }
    }
    } catch(Exception ex){
      System.out.println(x+","+y);//#unlikely to happen
    }
    return this;
  }

  public boolean set(XPoint p,boolean value){
    if(validPoint(p)){
      content[p.y][p.x]=value;
      return true;
    }
    return false;
  }

  public static final Raster EmptyOne(){
    return new Raster(0,0);
  }

  public Raster(int width,int height) {//throws ArraySomethingException
    content = new boolean[this.height=height][this.width=width];
  }

  public Raster(XDimension d) {//throws ArraySomethingException
    this(d.width,d.height);
  }

  private Raster(){
  //must know size to construct
  }

}
//$Id: Raster.java,v 1.13 2003/12/08 22:45:42 mattm Exp $
