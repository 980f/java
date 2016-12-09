/* $Id: EventCannon.java,v 1.3 2000/11/17 06:55:22 andyh Exp $ */
package net.paymate.jpos.common;

import java.util.Vector;

import jpos.events.DataEvent;
import jpos.events.DataListener;

import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;

import jpos.events.ErrorEvent;
import jpos.events.ErrorListener;

import jpos.events.OutputCompleteEvent;
import jpos.events.OutputCompleteListener;

import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;

import jpos.services.BaseService;
import jpos.services.EventCallbacks;
import jpos.BaseControl;

public class EventCannon implements EventCallbacks {
  protected BaseControl parent;

  protected Vector dataListeners;
  protected Vector directIOListeners;
  protected Vector errorListeners;
  protected Vector outputCompleteListeners;
  protected Vector statusUpdateListeners;


  public void fireDataEvent(DataEvent dataevent){
    synchronized(dataListeners) {
      for(int i = dataListeners.size(); i-->0;){
        ((DataListener)dataListeners.elementAt(i)).dataOccurred(dataevent);
      }
    }
  }

  public void fireDirectIOEvent(DirectIOEvent directioevent){
    synchronized(directIOListeners){
      for(int i =directIOListeners.size(); i-->0;){
        ((DirectIOListener)directIOListeners.elementAt(i)).directIOOccurred(directioevent);
      }
    }
  }

  public void fireErrorEvent(ErrorEvent errorevent){
    synchronized(errorListeners){
      for(int i = errorListeners.size(); i-->0;){
        ((ErrorListener)errorListeners.elementAt(i)).errorOccurred(errorevent);
      }
    }
  }

  public void fireOutputCompleteEvent(OutputCompleteEvent outputcompleteevent){
    synchronized( outputCompleteListeners){
      for(int i = outputCompleteListeners.size(); i-->0;){
        ((OutputCompleteListener)outputCompleteListeners.elementAt(i)).outputCompleteOccurred(outputcompleteevent);
      }
    }
  }

  public void fireStatusUpdateEvent(StatusUpdateEvent statusupdateevent){
    synchronized(statusUpdateListeners){
      for(int i =statusUpdateListeners.size(); i-->0;){
        ((StatusUpdateListener)statusUpdateListeners.elementAt(i)).statusUpdateOccurred(statusupdateevent);
      }
    }
  }

  public EventCannon(BaseControl parent){
    this.parent=parent;
    dataListeners = new Vector();
    directIOListeners = new Vector();
    errorListeners = new Vector();
    outputCompleteListeners = new Vector();
    statusUpdateListeners = new Vector();
  }

  public void addDataListener(DataListener datalistener){
    synchronized(dataListeners){
      dataListeners.addElement(datalistener);
    }
  }

  public void addDirectIOListener(DirectIOListener directiolistener){
    synchronized(directIOListeners){
      directIOListeners.addElement(directiolistener);
    }
  }

  public void addErrorListener(ErrorListener errorlistener){
    synchronized(errorListeners){
      errorListeners.addElement(errorlistener);
    }
  }

  public void addOutputCompleteListener(OutputCompleteListener listener){
    synchronized(outputCompleteListeners){
      errorListeners.addElement(listener);
    }
  }


  public void addStatusUpdateListener(StatusUpdateListener statusupdatelistener){
    synchronized(statusUpdateListeners){
      statusUpdateListeners.addElement(statusupdatelistener);
    }
  }

  public void removeDataListener(DataListener datalistener){
    synchronized(dataListeners){
      dataListeners.removeElement(datalistener);
    }
  }

  public void removeDirectIOListener(DirectIOListener directiolistener){
    synchronized(directIOListeners){
      directIOListeners.removeElement(directiolistener);
    }
  }

  public void removeErrorListener(ErrorListener errorlistener){
    synchronized(errorListeners){
      errorListeners.removeElement(errorlistener);
    }
  }

  public void removeOutputCompleteListener(OutputCompleteListener listener){
    synchronized(outputCompleteListeners){
      outputCompleteListeners.removeElement(listener);
    }
  }

  public void removeStatusUpdateListener(StatusUpdateListener statusupdatelistener){
    synchronized(statusUpdateListeners){
      statusUpdateListeners.removeElement(statusupdatelistener);
    }
  }

  public BaseControl getEventSource(){
    return parent;
  }

}
//$Id: EventCannon.java,v 1.3 2000/11/17 06:55:22 andyh Exp $
