package net.paymate.lang;

/**
 * Title:        $Source: /cvs/src/net/paymate/lang/RawEnum.java,v $
 * Description:  unsafe enumeration useful for when you don't have time to make a TrueEnum
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.Ascii; // +++ move into lang !!!

public class RawEnum {
  protected int value;

  public String toString(){
    return Ascii.bracket(value);
  }

  public boolean is(int eye){
    return value==eye;
  }

  public int setto(int any){
    return value=any;
  }

  public int Value(){
    return value;
  }

  protected RawEnum() {
    value=ObjectX.INVALIDINDEX;
  }

}
//$Id: RawEnum.java,v 1.1 2003/07/27 05:35:10 mattm Exp $