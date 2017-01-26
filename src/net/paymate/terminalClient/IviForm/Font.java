package net.paymate.terminalClient.IviForm;
/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/IviForm/Font.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Font.java,v 1.12 2003/07/27 05:35:17 mattm Exp $
*/

import  net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.lang.MathX;

public class Font {
  public int font=0;
  public boolean italic=false;
  public boolean reversed=false;
  public boolean underlined=false;

  public Font(){
    clear();
  }

  public int et1k(){
    return MathX.packNibbles(attr(),(font == 3)? 6:font);
  }

  public int code(){
    return font;
  }

  public int attr(){
    int rui=0;
    if(reversed){
      rui|=4;
    }
    if(underlined){
      rui|=2;
    }
    if(italic){
      rui|=1;
    }
    return rui;
  }

  public void clear(){
    font=0;
    italic=false;
    reversed=false;
    underlined=false;
  }

  public void parse(String fontspec){
    reversed    = Bool.flagPresent("r",fontspec);
    underlined  = Bool.flagPresent("u",fontspec);
    italic      = Bool.flagPresent("i",fontspec);
    font= Character.getNumericValue(fontspec.charAt(0));
  }

  public static final Font Create(String fontspec){
    Font created=new Font(fontspec);
    return created;
  }

  public Font(String fontspec){
    parse(fontspec);
  }

  public Font Clone(){
    Font newone=new Font();
    this.reversed    = reversed;
    this.underlined  = underlined;
    this.italic      = italic;
    this.font        = font;
    return this;
  }

  public int Width(){
    switch(font){
      case 0: return 1;
      case 1: return 2;
      case 2: return 1;
      case 3: return 1;
    }
    return 0;
  }

  public int Height(){
    switch(font){
      case 0: return 1;
      case 1: return 2;
      case 2: return 2;
      case 3: return 1;
    }
    return 0;
  }

  public String description(){
    switch(font){
      case 0: return "small";
      case 1: return "big";
      case 2: return "tall";
      case 3: return "bold";
    }
    return "invalid";
  }

  public String xml(){
    return Xml.wrap("FONT",Xml.member("code",font)+
    Xml.member("italics",  italic)+
    Xml.member("underline",underlined)+
    Xml.member("reverse",  reversed));
  }

}//$Id: Font.java,v 1.12 2003/07/27 05:35:17 mattm Exp $
