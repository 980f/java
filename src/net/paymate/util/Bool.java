/* $Id: Bool.java,v 1.9 2001/07/19 01:06:54 mattm Exp $ */

package net.paymate.util;

public class Bool {

  public static final String toString(boolean b){//--- seems to be a native thing
    return b?"true":"false";
  }

  public static final boolean bitpick(int pattern, int bitnum){
    return (pattern & (1<<bitnum))!=0;
  }

  public static final boolean bitpick(long pattern, int bitnum){
    return (pattern & (1L<<bitnum))!=0;
  }

  public static final boolean For(int eye){
    return eye!=0;
  }

  public static final boolean For(long ell){
    return ell!=0;
  }

  public static final boolean[] MapFromLong(long ell){
    boolean[] map=new boolean[64];
    int i=0;
    for(long bitp=Long.MIN_VALUE;i<64;bitp>>>=1){
      map[i++]=(ell&bitp)!=0;
    }
    return map;
  }

  public static final long LongFromMap(boolean [] map){
    long ell=0;
    int i=0;
    for(long bitp=Long.MIN_VALUE;i<64;bitp>>>=1){
      if(map[i++]){
        ell|=bitp;
      }
    }
    return ell;
  }

  public static final int signum(boolean positive){
    return positive? 1: -1;
  }

  public static final char signChar(boolean positive){
    return positive? '+': '-';
  }

}

//$Id: Bool.java,v 1.9 2001/07/19 01:06:54 mattm Exp $
