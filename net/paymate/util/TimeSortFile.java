package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/TimeSortFile.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import java.util.*;
import java.io.*;

public class TimeSortFile implements Comparator {
  protected boolean descending;

  public TimeSortFile(boolean descending){
    this.descending=descending;
  }

  public TimeSortFile(){
    this(false);
  }

  public long modified(Object f){
    if(f!=null && f instanceof File){
      return ((File)f).lastModified();
    } else {
      //if(pleaseBarf)
      throw new ClassCastException();
    }
    //      return -1;//in case we suppress throwing
  }

  public int compare(Object o1, Object o2){
    return (int)(modified(o1) - modified(o2));
  }

  public boolean equals(Object obj){
    return this==obj; //strictest version
  }

}
//$Id: TimeSortFile.java,v 1.1 2001/10/16 22:35:10 andyh Exp $