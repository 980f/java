package net.paymate.ivicm.pinpad;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/Configure.java,v $
 * Description:  a main() to get access to encrypt setup functions.
 *        baud rate defaults to 1200 baud. The setbaud command screws things up
 *        as the ACK comes back at the NEW baud rate, which doesn't give us time
 *        to reprogram the driver.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import net.paymate.util.*;
import net.paymate.serial.*;
import net.paymate.lang.StringX;
import net.paymate.ivicm.comm.*;

public class Configure {

  static public void main(String[] cmdline) {
    ErrorLogStream dbg= ErrorLogStream.getForClass(Configure.class,ErrorLogStream.VERBOSE);
    encrypt100 totest= new encrypt100("encrypt100.Configure");
    totest.configmode= true;//squelch normal startup commands.
    encrypt100.testerMain(encrypt100.class);
    TextListIterator args=totest.testerConnect(cmdline,1200,dbg);
    args.onError="";

    while(args.hasMoreElements()){
      String arg=args.next();
      if(arg.equals("dukpt")){
        totest.QueueCommand(Command.configDukpt());
      } else if(arg.equals("brag")){
        totest.QueueCommand(Command.configBrag(totest.restOfLine(args)));
        totest.QueueCommand(Command.mkDisplay("cycle device."));
      } else if(arg.equals("keys")){
        totest.QueueCommand(Command.configFunctionKey(true));//function key slive during string input
        totest.QueueCommand(Command.configCancelKey(true));//cancel key ends input, including pin input.
        testapad padtester=new testapad(EC100DisplayPad.makePad(totest));
      } else if(arg.equals("baud")){
        totest.QueueCommand(Command.configBaud(StringX.OnTrivial(args.next(),9600)));
        totest.QueueCommand(Command.mkDisplay("baud set."));
      } else if(arg.equals("text")){
        totest.QueueCommand(Command.mkDisplay(totest.restOfLine(args)));
      }
    }
    //totest.disConnect();//hopefullt that will cause program to exit
  }

}
//$Id: Configure.java,v 1.12 2003/07/27 05:35:05 mattm Exp $