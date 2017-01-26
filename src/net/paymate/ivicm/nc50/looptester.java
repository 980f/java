package net.paymate.ivicm.nc50;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/nc50/looptester.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import net.paymate.serial.*;

import net.paymate.ivicm.comm.*;

public class looptester {
  ErrorLogStream dbg= ErrorLogStream.getForClass(looptester.class,ErrorLogStream.VERBOSE);

  NC50 sender;
  NC50 recver;

  static NC50 makeOne(String id,String portname){
    NC50 newone=new NC50("NC50."+id);
    newone.configmode=true;//squelch normal startup commands.
    Parameters params=Parameters.Traditional(portname+",9600,E71");
    newone.Connect(SerialConnection.makeConnection(params));
    return newone;
  }

  public looptester(TextListIterator arg) {
    sender=makeOne("sender",arg.next());
    recver=makeOne("recver",arg.next());
  }

  public void test(TextListIterator arg){
    recver.simulating=true;
    sender.speedtest1();
  }

  /**
   *
   */
  static public void main(String[] args) {
    NC50.testerMain(NC50.class);
    NC50.dbg.setLevel(LogSwitch.ERROR);
    NC50.dbk.setLevel(LogSwitch.ERROR);
//
    TextListIterator arg= TextListIterator.New(args);
    looptester arf=new looptester(arg);
    arf.test(arg);
  }

}
//$Id: looptester.java,v 1.6 2003/07/24 17:43:51 andyh Exp $