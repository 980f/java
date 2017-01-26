/**
* Title:        $Source: /cvs/src/net/paymate/util/ObjectFifo.java,v $
* Description:  fully synchronized queue
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ObjectFifo.java,v 1.17 2002/11/19 18:42:09 mattm Exp $
*
* Implements a Vector with a single public Iterator that operates live and utilizes a macro for next().
*/

package net.paymate.util;

import java.util.Vector;

public class ObjectFifo {// no time to find one+_+
  protected Vector fifo=null;

  public ObjectFifo() {
    this(100);
  }

  public ObjectFifo(int presize) {
    fifo=new Vector(presize, presize);
  }

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
   * This exists for reporting purposes only.  DO NOT use it to iterate!
   */
  public final int Size() {
    return fifo.size();
  }

  public boolean isEmpty(){
    return fifo.size()<=0;
  }

}
//$Id: ObjectFifo.java,v 1.17 2002/11/19 18:42:09 mattm Exp $
