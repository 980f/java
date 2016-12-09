package net.paymate.util.timer;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/timer/tester.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class tester implements TimeBomb {

  static ErrorLogStream dbg=new ErrorLogStream("tester",ErrorLogStream.VERBOSE);


  String id="";
  StopWatch sw;
  Alarmum alarm;

  public void onTimeout(){
    dbg.WARNING(id+ "");
    alarm=Alarmer.New(alarm.ticks,this);
  }

  public String toString(){
    return id;
  }


  public tester(String id,int ticks) {
    this.id=id;
    sw=new StopWatch();
    alarm= Alarmer.New(ticks,this);
  }

  public static void main(String[] args) {
    int number=args.length>0? Integer.parseInt(args[0]):3;
    tester tester[] = new tester[number];
    for(int i=tester.length;i-->0;){
      tester[i]=new tester("t"+i,i*1000);
    }
    while(Alarmer.alarmCount()>0){
      Alarmer.dump(dbg,dbg.VERBOSE);
      Thread.currentThread().yield();
    }
  }

}