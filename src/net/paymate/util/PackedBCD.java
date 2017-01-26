package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/PackedBCD.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.lang.StringX;

public class PackedBCD {
  private String bcd;
  public String toString(){
    return bcd;
  }

  public static PackedBCD New(String bcd) {
    PackedBCD newone=new PackedBCD();
    newone.bcd=bcd;
    return newone;
  }

  /**
   * @return integer value of packed bcd number
   * empty string returns 0.
   */
  public long Value(){
    return parseString(bcd);
  }

  public static int fromChar(char onebcd){
    return onebcd&15 + 10*(onebcd>>4);
  }

  public static long parseString(String bcd){
    if(StringX.NonTrivial(bcd)){
      long ell=0;
      long power=1;
      for(int i=bcd.length();i-->0;){
        ell+=power*fromChar(bcd.charAt(i));
        power*=100;
      }
      return ell;
    } else {
      return 0;
    }
  }

}
//$Id: PackedBCD.java,v 1.2 2003/07/27 05:35:23 mattm Exp $