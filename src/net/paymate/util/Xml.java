package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Xml.java,v $
 * Description:  xml helper functions,placeholder for a real xml genetiaon system
 * Copyright:    Copyright (c) 2000,2001
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version $Revision: 1.4 $
 */

import java.util.*;

public class Xml {

  public static final StringBuffer attributed(String token, EasyProperties attribs){
    StringBuffer sb=new StringBuffer(10+20*attribs.size());
    sb.append(" <");
    sb.append(token);
    for(Enumeration ennum=attribs.keys();ennum.hasMoreElements();){
      String key=(String)ennum.nextElement();
      String property=attribs.getString(key);
      sb.append(quoted(key,property));//+++ only if trivial or includes whitespace
    }
    sb.append("> ");
    return sb;
  }

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
    return nameEquals(name)+value+" ";
  }

  public static final String member(String name,double value){
    return nameEquals(name)+value+" ";
  }

  public static final String member(String name,boolean value){
    return nameEquals(name)+value+" ";
  }

  public static final String quoted(String name,String value){
    return nameEquals(name)+"\""+value+"\" ";
  }

  public static final String xypair(String name,int x,int y){
    return nameEquals(name)+" ("+x+","+y+") ";
  }

  private static final String nameEquals(String name) {
    return " "+name+"=";
  }
}
//$Id: Xml.java,v 1.4 2001/12/04 15:14:03 mattm Exp $
