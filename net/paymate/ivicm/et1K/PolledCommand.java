package net.paymate.ivicm.et1K;
/**
* Title:        PolledCommand
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PolledCommand.java,v 1.8 2001/11/15 03:15:45 andyh Exp $
*/

import net.paymate.util.timer.*;
import net.paymate.util.*;

public class PolledCommand implements TimeBomb {
  Service service;
  Command toPoll;
  int ticks;

  static ErrorLogStream DBG=new ErrorLogStream(PolledCommand.class.getName());
  private ErrorLogStream dbg;

  Alarmum delay;

  public String toSpam(){
    return (delay!=null?delay.toSpam():"No alarm ")+ toString()+" delay:"+ticks+" served by:"+service.toString();
  }

  public String toString(){//used by alrmlist dump()
    return toPoll!=null? toPoll.errorNote: "No Command! ";
  }

/**
 * timeout used to delay action
 */
  public synchronized void onTimeout(){
    try {
      if(service!=null && toPoll != null){
        dbg.VERBOSE("ready to poll "+toPoll.errorNote);
        service.QueueCommand(toPoll);
      }
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  public synchronized void Start(){
    if(toPoll!=null && ticks >0 ){
      if(!Alarmer.isTicking(delay)){
        dbg.VERBOSE(toPoll.errorNote+" starting delay before poll:"+ticks);
        delay=Alarmer.New(ticks,this);
      }
//      Alarmer.dump(dbg,dbg.ERROR);
    }
  }

  public synchronized void Stop(){
    dbg.VERBOSE("Stopping "+toPoll.errorNote);
    Alarmer.Defuse(delay); //delay might not exist yet
    //and remove any held by service queue
    service.hardware.squelch(toPoll);
  }

  public PolledCommand(Command toPoll,double rate,Service service,ErrorLogStream dbg) {
    this.ticks=(int)Ticks.forSeconds(rate>0? 1.0/rate : 0);
    dbg.VERBOSE("Rate:"+rate+" becomes Ticks:"+ticks);
    this.service=service;
    this.toPoll=toPoll;
    this.toPoll.isaPoller=true; //filters this out of debug
    this.dbg= dbg!=null? dbg:DBG;
    dbg.VERBOSE(this.toSpam());
  }

  public PolledCommand(Command toPoll,Service service,ErrorLogStream dbg) {
    this(toPoll,1,service,dbg);
  }

}
//$Id: PolledCommand.java,v 1.8 2001/11/15 03:15:45 andyh Exp $
