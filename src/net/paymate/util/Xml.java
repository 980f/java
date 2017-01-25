package net.paymate.util;

/**
 * Title:        placeholder for a real xml genetiaon system
 * Description:  xml helper functions
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: Xml.java,v 1.2 2001/07/19 01:06:55 mattm Exp $
 */

public class Xml {
  public static final String start(String token){
    return " <"+token+"> ";
  }

  public static final String end(String token){
    return " </"+token+">\n";
  }

  public static final String wrap(String token,String body){
    return start(token)+body+end(token);
  }
//java needs templates dammit:
  public static final String member(String name,int value){
    return " "+name+"="+value+" ";
  }

  public static final String member(String name,double value){
    return " "+name+"="+value+" ";
  }

  public static final String member(String name,boolean value){
    return " "+name+"="+value+" ";
  }

  public static final String quoted(String name,String value){
    return name+"=\""+value+"\" ";
  }

  public static final String xypair(String name,int x,int y){
    return name+"= ("+x+","+y+") ";
  }

}
//$Id: Xml.java,v 1.2 2001/07/19 01:06:55 mattm Exp $