package net.paymate.io;

/**
 * Title:        $Source: /cvs/src/net/paymate/io/TimeSortFile.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.*;
import java.io.*;
import net.paymate.lang.MathX;

public class TimeSortFile extends BaseSortFile implements Comparator {

  private TimeSortFile(boolean descending){
    super(descending);
  }

  private static TimeSortFile makeone(boolean descending,Comparator nextone){
    TimeSortFile newone=new TimeSortFile(descending);
    newone.setNextCritierion(nextone);
    return newone;
  }

  public static TimeSortFile Ascending(Comparator nextone){
    return makeone(false,nextone);
  }

  public static TimeSortFile Descending(Comparator nextone){
    return makeone(true,nextone);
  }

  public long modified(Object f){
    if(f!=null && f instanceof File){
      return ((File)f).lastModified();
    } else {
      throw new ClassCastException();
    }
  }

  public int compare(Object o1, Object o2){
    long compvalue=modified(o1) - modified(o2);
    return chain(MathX.signum(descending? - compvalue:compvalue),o1,o2);
  }


}
//$Id: TimeSortFile.java,v 1.2 2004/03/08 22:54:14 andyh Exp $