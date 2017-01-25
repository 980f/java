package net.paymate.data;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: LicenseMask.java,v 1.3 2001/07/19 01:06:47 mattm Exp $
 */

import net.paymate.util.Safe;

public class LicenseMask {
  String mta;
  int legacy;
  String mask;

  LicenseMask(String one,int two,String three){
    mta=one;
    legacy=two;
    mask=three;
  }

  static final LicenseMask[] table={
    new LicenseMask("TX",1,"NNNNNNNN"),
    //truncated until debugged---
  };

  public static final boolean expectAlpha(char code){
    return code=='A' || code== 'a';
  }

  public static final char Code(String mask,int index){
    if(Safe.NonTrivial(mask)&&index<mask.length()){
      return mask.charAt(index);
    } else {
      return 'x';
    }
  }

  public static final String [] forState(MajorTaxArea mta){
    int lastat;
    String state=mta.Abbreviation();//for faster search
    for(lastat=table.length;lastat-->0;){//relying on table order
      LicenseMask cursor=table[lastat];
      if(cursor.mta.equals(state)){//found final entry for this state
        String[]maskset=new String[cursor.legacy];
        for(int mi=maskset.length;mi-->0;){
          maskset[mi]=cursor.mask;
          if(mi>0){//still more
            cursor=table[--lastat];
          }
        }
        return maskset;
      }
    }
    return new String[0];//no masks==no match
  }

  private LicenseMask() {
  //no uninit'ed ones allowed
  }
}