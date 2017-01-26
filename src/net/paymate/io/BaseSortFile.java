package net.paymate.io;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/io/BaseSortFile.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */
import java.util.*;
import java.io.*;

abstract public class BaseSortFile implements Comparator {
  protected boolean descending;
  protected Comparator secondCriterion;

  protected BaseSortFile(boolean descending) {
    this.descending=descending;
    this.secondCriterion=null;
  }

  public Comparator setNextCritierion(Comparator nextone){
    this.secondCriterion=nextone;//someday chain if class supports it.
    return this;
  }

  protected int chain(int first,Object o1, Object o2){
    return first!=0? first : (secondCriterion!=null? secondCriterion.compare(o1,o2):0);
  }

  abstract public int compare(Object o1, Object o2);

  public boolean equals(Object obj){
    return this==obj || 0==compare(this,obj);
  }

} //$Id: BaseSortFile.java,v 1.1 2004/03/08 22:54:14 andyh Exp $
