package net.paymate.util.timer;
/**
* Title:        $Source: /cvs/src/net/paymate/util/timer/Alarmer.java,v $
* Description:  do timedelayed actions
*  the actions execute from the Alarmer thread, so if they do work that takes
*  a significant amount of time they screw up the other time delayed actions.
*  Such an activity should spawn a thread.
*  This class won't do that since most instances of onTimeout run fast
*  The active alarm list is mutexed. Please keep it that way!
* Copyright:    2001 Paymate.net
* @author PayMate.net
* @version $Revision: 1.12 $
*/

import  net.paymate.util.*;
import java.util.*;

class AlarmList {
  ErrorLogStream dbg;
  Vector list;
  Monitor lock;

  AlarmList (ErrorLogStream dbg){
    this.dbg=dbg;
    lock=new Monitor("alarmlist",dbg);
    list=new Vector();
  }

  int size(){
    return list.size();
  }

  Alarmum alarm(int i){
    return  (Alarmum)list.elementAt(i);
  }

  Alarmum next(){//!!!check size>0 before accessing this else get NoSuchElementException
    return (Alarmum)list.lastElement();
  }

  /**
  * @return the earliest that any will blow.
  */
  long soonest(){
    lock.LOCK("soonest");
    try {
      return size()>0? next().blowtime:0;
    }
    finally {
      lock.UNLOCK("soonest");
    }
  }

  /**
  * @return true if new one MIGHT already have timed out.
  * so we always return true for now, just in case.
  */
  boolean insert(Alarmum newalarm){
    if(!newalarm.isEnabled()|| newalarm.isTicking()){
    //don't insert one that won't fire off.
    //don't insert one that is somehow already on any list
      dbg.ERROR("USELESS ALARM "+newalarm.toSpam());
      return false;//caller should treat this as an error.
    }

    try {
      lock.LOCK("insert "+newalarm.toSpam());
      for(int i=list.size();i-->0;){
        Alarmum alarm= alarm(i);
        if(newalarm.blowtime<alarm.blowtime){
          if(i==list.size()){ //new "next to fire"
            list.add(newalarm);
          } else {
            list.add(i+1,newalarm);
          }
          return newalarm.listed=true;
        }
      }
      list.add(0,newalarm);
      return newalarm.listed=true;
    } catch (Exception any){
      return newalarm.listed=false;
    }
    finally {
      lock.UNLOCK("insert");
    }
  }

  /**
  * @return true if new one goes to top of stack.
  */
  boolean remove(Alarmum newalarm){
    try {
      lock.LOCK("remove:"+newalarm.toSpam());
      for(int i=list.size();i-->0;){
        Alarmum alarm= alarm(i);
        if(alarm==newalarm){//same object
          list.remove(i);
          newalarm.listed=false;
          return true; //presume single instance
        }
      }
      return false;
    }
    finally {
      lock.UNLOCK("remove");
    }
  }

  /**
  * @return COPY of object if it is in list
  */
  Alarmum info(Alarmum analarm){
    try {
      lock.LOCK("info");
      for(int i=list.size();i-->0;){
        Alarmum alarm= alarm(i);
        if(alarm==analarm){//same object
          return analarm.Clone();//return snapshot, one that is NOT in the active list.
        }
      }
      return null;
    }
    finally {
      lock.UNLOCK("info");
    }
  }

  AlarmList ringers(long now){
    AlarmList ringers=new AlarmList(dbg);
    try {
      lock.LOCK("ringers");
      for(int i=list.size();i-->0;){
        Alarmum alarm= alarm(i);
        dbg.WARNING("Checking:"+alarm.toSpam());
        if (alarm.blowtime<=now) {
          alarm.listed=false;
          dbg.WARNING("Ringing:"+alarm.toSpam());
          ringers.list.add(alarm);
          list.remove(i);//because of how we fill the list this is tolerably efficient
        }
        else {
          break;
        }
        //+_+ recode above to find the break point then make an ARRAY for ringers.
      }
      return ringers;
    }
    finally {
      lock.UNLOCK("ringers");
    }
  }

  /**
   * adjust the time value of all alarms in list
  */
  void adjust(int diff){
    try {
      lock.LOCK("adjust");
      for(int i=list.size();i-->0;){
        alarm(i).blowtime+=diff;
      }
    }
    finally {
      lock.UNLOCK("adjust");
    }
  }

  TextList toSpam(TextList spam){
    try {
      lock.LOCK("spamlist");
      spam.Add("<ActiveAlarms size="+size()+">");
      for(int i=list.size();i-->0;){
        spam.Add(alarm(i).toSpam());
      }
      spam.Add("</ActiveAlarms>");
      return spam;
    }
    finally {
      lock.UNLOCK("spamlist");
    }
  }

}//end AlarmList

public class Alarmer implements Runnable {
  static ErrorLogStream dbg;

  static Alarmer my;//the default Alarm manager
  Thread thread;
  int priority;
  AlarmList active;

  boolean paused=false; //for clock update

  private Alarmer(){//only one instance allowed at present.
    dbg=new ErrorLogStream(Alarmer.class.getName(),ErrorLogStream.OFF);//##ignores logcontrol##
    thread=new Thread(this,"PM.Alarmer");
    priority=Thread.NORM_PRIORITY;//-1;
    active=new AlarmList(dbg);
    ErrorLogStream.Debug.ERROR("Alarmer debug level is:"+dbg.myLevel.Image());
    //seems to be one of those that ignore the config file!
  }

  /**
   * seems to startup already interrupted. I.e. first sleep doesn't sleep. Ok but curious.
   */
  public void run(){
   dbg.WARNING("RUNNING");
    while(true){
      try {
        long nexttime=active.soonest();
        if(paused || nexttime==0){
          dbg.WARNING("no alarms, sleeping for along time");
          ThreadX.sleepFor(100000);//just a little safer than 'forever'
        } else {
          long interval=nexttime-Safe.utcNow();//convert to timedifference
          if(interval>0){
            dbg.WARNING("next check at "+Safe.timeStamp(nexttime)+" Sleeping for "+interval);
            ThreadX.sleepFor(interval);
          }
        }
        if(!paused){
          dbg.WARNING("check alarms");
          //ignore why we quit sleeping, try to do some alarms NOW
          doRingers(active.ringers(Safe.utcNow()));
        }
      }
      catch (Throwable ex) {
        dbg.Caught("Unexpected exception:",ex);
        continue;
      }
    }
  }

  private void check(){
    thread.interrupt();
  }

  public  static void Check(){
    my.check();
  }

  private static void doRingers(AlarmList ringers){
    dbg.WARNING("doing ringers:"+ringers.size());
//    dump(dbg,dbg.VERBOSE);
    try {
      for(int i=ringers.size();i-->0;){
        Alarmum alarm = ringers.alarm(i);
//        if(alarm.isEnabled()){//allows for last moment disabling by other threads
          alarm.defuse();
          Thread.currentThread().setPriority(alarm.ThreadPriority);
          dbg.WARNING("RINGING:"+alarm.dynamite);
          try {
            alarm.dynamite.onTimeout();
          } catch(Exception any){
            dbg.Caught(any);
          }
          dbg.WARNING("RUNG:"+alarm.dynamite);
//        }
      }
    }
    finally {
      Thread.currentThread().setPriority(my.priority);
    }
  }

  public static final int alarmCount() {
    return (my!=null && my.active !=null) ? my.active.size():0;
  }

  private  Alarmum getState(Alarmum alarm){
    return active.info(alarm);
  }

  public  static Alarmum GetState(Alarmum alarm){
    return my.getState(alarm);
  }

  public  static void Defuse(Alarmum alarm){
    if(alarm!=null){
      alarm.defuse();
      my.active.remove(alarm);
    }
  }

  public  static boolean isTicking(Alarmum alarm){
    return (alarm!=null)?alarm.isTicking():false;
  }

  private  static Alarmum Set(Alarmum newone){
    if(my==null){
      my=new Alarmer();
      my.thread.setDaemon(true);
      my.thread.start();
    }
    if(my.active.insert(newone)){
      my.thread.interrupt();
    }
    return newone;
  }

  public  static Alarmum New(int fuse,TimeBomb dynamite){
      return Set(new Alarmum(dynamite,fuse));//to provide access so that it can be defused.
  }

  /**
   * we may someday reuse the objects.
   */
  public static Alarmum reset(int fuse,Alarmum alarm){
    Defuse(alarm);
    dbg.Enter("NewAlarm");
    try {
      return Set(alarm.refuse(fuse));//to provide access so that it can be defused.
    } finally {
      dbg.Exit();
    }
  }

  private TextList dump(TextList spam){
    if(spam==null){
      spam=new TextList();
    }
    return my.active.toSpam(spam);
  }

  public static TextList dump(){
    return my.dump(null);
  }

  public static void dump(ErrorLogStream els,int importance){
//    els.rawMessage(importance,dump().asParagraph());
  }

/**
 * @totally untested!
 * stop watches are screwed by this!
 */
  public static synchronized int adjustSystemClock(long whatnowshouldbe){
  // stop the alarm checking,
    my.paused=true;
  //save "now",
    long was=Safe.utcNow();
    Safe.setSystemClock(whatnowshouldbe);
  //read the system clock,   //diff with "now"
    int diff=(int)(Safe.utcNow()-was);
  //and adjust all alarms in the active list,
    my.active.adjust(diff);//diff gets added
  //then reenable alarm checking.
    my.paused=false;
    my.thread.interrupt();
  //FYI return value.
    return diff;
  }

}
//$Id: Alarmer.java,v 1.12 2001/11/14 01:47:58 andyh Exp $