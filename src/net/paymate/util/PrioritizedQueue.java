package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/PrioritizedQueue.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 * note: a defective comparator can yield an infinite binary search.
 */

import java.util.*;

public class PrioritizedQueue extends ObjectFifo {
  private Comparator ordering;

  public synchronized int put(Object obj){ //insertion sort
    if(obj instanceof Comparable){
//    try {
//      listlock.getMonitor();//we would do this if worried that comparator were defective
      int location=Collections.binarySearch(fifo,obj,ordering);
      //if of equal priority to something present then newest goes after first one
      //if nothing equal then goes whereever the search leaves it
      fifo.insertElementAt(obj,location<0?~location:location+1);
      return Size();
//    }
//    finally {
//      listlock.freeMonitor();
//    }
    }
    else {
      return super.put(obj);
    }
  }

  public PrioritizedQueue() {
    this(PriorityComparator.Normal());
  }

  public PrioritizedQueue(Comparator ordering) {
    this.ordering=ordering;
  }

}
//$Id: PrioritizedQueue.java,v 1.4 2002/03/27 02:00:55 andyh Exp $