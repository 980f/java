/**
* Title:        EventQueuer
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: EventQueuer.java,v 1.14 2001/11/15 03:15:45 andyh Exp $
*/
package net.paymate.jpos.common;

import net.paymate.util.ObjectFifo;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.LogSwitch;

import jpos.services.EventCallbacks;
import jpos.events.*;
import jpos.*;

/* ObjectFifo's are synchronized put() vs. next() */

public class EventQueuer implements JposConst {
  public EventCallbacks myCallbacks;//pool of listeners
  public boolean autoDisable;

  protected InputServer cacher;
  protected ObjectFifo myEvents;  //mixed data and error events
  protected ObjectFifo data;      //data associated with data events.
  protected boolean amEnabled;    //to post upon entry

  protected /*synchronized*/ void Fire(){
    Object event;
    while( (event=myEvents.next())!=null){
      if(autoDisable){
        TurnMe(false);
      }
      if(myCallbacks!=null){
        if(event instanceof DataEvent){
          cacher.prepareForDataEvent(data.next());
          myCallbacks.fireDataEvent((DataEvent)event);
        }
        else if(event instanceof DirectIOEvent){
          myCallbacks.fireDirectIOEvent((DirectIOEvent)event);
        }
        else if(event instanceof ErrorEvent){
          myCallbacks.fireErrorEvent((ErrorEvent)event);
        }
        else {
          myCallbacks.fireErrorEvent(new ErrorEvent(this,JPOS_E_EXTENDED,0,0,0));
        }
      }
    }
  }

  protected /*synchronized*/ void Post(Object obj){
    myEvents.put(obj);
    if(amEnabled){
      Fire();
    }
  }

  public void TurnMe(boolean on){
    if(on && !amEnabled){
      amEnabled=true;
      Fire();//and post at LEAST one event (if any are present)
    } else {
      amEnabled=on;
    }
  }

//the public Post's ensure that only valid types are put into the underlying fifo.
  public void Post(DataEvent event,Object dataobj){
    data.put(dataobj);
    Post((Object)event);
  }

//  private void Post(ErrorEvent event){
//    Post((Object)event);
//  }

  public void PostFailure(String s){
    String xs;
    if(cacher==null){
      xs="Null service ";
    } else {
      xs=cacher.toString();
    }
    ErrorLogStream.Debug.ERROR(xs+" Posts Failure:"+s);
    Post((Object)(new ErrorEvent(xs,JposConst.JPOS_E_FAILURE,0,0,0)));
  }

  public void Post(DirectIOEvent event){
    Post((Object)event);
  }

  public void Clear(){
    myEvents.Clear();
    data.Clear();
  }

  public void Attach(InputServer service, EventCallbacks ecbs){//pool of listeners
    myCallbacks=ecbs;
    cacher = service;
    data.Clear();
    myEvents.Clear();
  }

  public EventQueuer (){
    myCallbacks = null;
    myEvents    = new ObjectFifo();
    amEnabled   = false;
    autoDisable = false;
    cacher      = null;
    data        = new ObjectFifo();
  }

}
//$Id: EventQueuer.java,v 1.14 2001/11/15 03:15:45 andyh Exp $
