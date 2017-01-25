/**
* Title:        ObjectFifo
* Description:  fully synchronized queue
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ObjectFifo.java,v 1.13 2001/10/30 18:25:34 andyh Exp $
*
* Implements a Vector with a single public Iterator that operates live and utilizes a macro for next().
*/

package net.paymate.util;

import java.util.Vector;

public class ObjectFifo /*extends Vector*/ {// no time to find one+_+
  private Vector fifo=new Vector();

  public synchronized Object [] snapshot(){
    return fifo.toArray();
  }

  public synchronized Object next(){
    int size=fifo.size();
    if(size-->0){
      Object gotten=fifo.elementAt(size);
      fifo.removeElementAt(size);
      return gotten;
    } else {
      return null;
    }
  }

  public synchronized int put(Object obj){
    fifo.insertElementAt(obj, 0);
    return fifo.size();
  }

  public synchronized int atFront(Object obj){
    fifo.add(obj);
    return fifo.size();
  }

  public synchronized void Clear(){
    fifo.setSize(0);
  }

/////////////////////
//srvr standin directly fiddled with guts of list.
  protected synchronized boolean remove(Object obj){
    return fifo.remove(obj);
    //does NOT interrupt. agent does NOT need to wake up to service a removal of
    //something that it hasn't looked at yet.
  }

  /**
   * remove anything satisfying obj.equals(fifoObj)
   */
  public synchronized int removeAny(Object obj){
    int count=0;
    while(fifo.remove(obj)){
      ++count;
    }
    return count;
  }

  /**
   * replace any one object that satisfies  matching.equals(fifoObj)
   */
  public synchronized boolean replace(Object matching){
    int vi=fifo.indexOf(matching);
    if(vi>=0){
      fifo.setElementAt(matching,vi);//replace with newest instance
      return true;
    }
    return false;
  }

  /**
   * This exists for reporting purposes only.  DO NOT use it to iterate!
   */
  public final int Size() {
    return fifo.size();
  }

}
//$Id: ObjectFifo.java,v 1.13 2001/10/30 18:25:34 andyh Exp $
