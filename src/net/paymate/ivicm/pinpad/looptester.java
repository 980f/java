package net.paymate.ivicm.pinpad;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/looptester.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.serial.*;

import net.paymate.ivicm.comm.*;

public class looptester {
  encrypt100 sender;
  encrypt100 recver;

  static encrypt100 makeOne(String id,String portname){
    encrypt100 newone=new encrypt100("encrypt100."+id);
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
    encrypt100.testerMain(encrypt100.class);
    encrypt100.dbg.setLevel(LogSwitch.ERROR);
    encrypt100.dbk.setLevel(LogSwitch.ERROR);

    TextListIterator arg= TextListIterator.New(args);
    looptester arf=new looptester(arg);
    arf.test(arg);
  }

}
//$Id: looptester.java,v 1.5 2003/07/24 17:43:51 andyh Exp $