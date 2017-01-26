package net.paymate.util;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/util/OrderedVector.java,v $
 * Description:  vector whose members are always in order, and of the same class
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 * @todo replace synchronized methods with synchs on the storage.
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.lang.ObjectX;

public class OrderedVector {
  private Vector storage;
  Comparator ordering;
  Class filter=null;
  boolean unique=false;

  public synchronized Object [] snapshot(){
    return storage.toArray();
  }

  //NOT sync'ed. user must be robust against spontaneous changes!
  public int length(){
    return storage.size();
  }

  public synchronized boolean insert(Object arf){
    if(ObjectX.typeMatch(arf,filter)){//chagne to "if arf can be cast to filter..."
      if(unique){
        return VectorX.uniqueInsert(storage,arf,ordering);
      } else {
        VectorX.orderedInsert(storage,arf,ordering);
        return true;
      }
    }
    return false;
  }

  public synchronized Object itemAt(int i){
    if(i>=0 && i<storage.size()){
      return storage.elementAt(i);
    } else {
      try {
        return filter.newInstance();
      }
      catch (Exception ex) {//npe, IllegalAccessException
        return null;
      }
    }
  }

  private OrderedVector(Comparator ordering,int prealloc,Class filter,boolean unique) {
    storage=new Vector(prealloc);
    this.ordering= ordering!=null? ordering : new NormalCompare();//swallow exceptions on ordering
    this.filter=filter; //null is ok
    this.unique=unique;
  }

  public static OrderedVector New(Comparator ordering,Class filter,boolean unique,int prealloc){
    return new OrderedVector(ordering,prealloc,filter,unique);
  }
  public static OrderedVector New(Comparator ordering,Class filter,int prealloc){
    return new OrderedVector(ordering,prealloc,filter,false);
  }
  public static OrderedVector New(Comparator ordering,Class filter,boolean unique){
    return new OrderedVector(ordering,0,filter,unique);
  }
  public static OrderedVector New(Class filter,boolean unique){
    return new OrderedVector(null,0,filter,unique);
  }
  public static OrderedVector New(Class filter){
    return new OrderedVector(null,0,filter,false);
  }

}
//$Id: OrderedVector.java,v 1.3 2005/02/28 05:01:38 andyh Exp $
