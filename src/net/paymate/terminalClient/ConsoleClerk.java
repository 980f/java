package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ConsoleClerk.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.net.*;
import net.paymate.connection.*;

import net.paymate.jpos.data.*;
import net.paymate.awtx.*;

import java.io.*;

public class ConsoleClerk extends AutoClerk implements Runnable, isEasy {

 /**
 * can filter all questions being directed to other clerk interfaces.
 * @return true if question has been handled/answered.
 */
  public boolean ask(int clerkitem){// @see ClerkItem.Enum
    switch (clerkitem) {
      default: return super.ask(clerkitem); //ask someone else
      case ClerkItem.SalePrice:  progress.setto(progress.awake); return false; //posterm is ready for sales action.
    }
  }

  //////////////////////////////////////////
  // console based interface
  private Server console;

  protected void commonStart(){
    super.commonStart();
    console=new Server("AutoClerk",this,true);
    console.Start();
  }

  public void run(){//#  interface Runnable
    dbg.WARNING("Running");
    BufferedReader inline=new BufferedReader(new InputStreamReader(System.in));
    String saleamount;
    LedgerValue saleamt;
    while(console.isRunning()){
      try {
        saleamount= inline.readLine();
        dbg.VERBOSE("readline:"+saleamount);
        if(StringX.NonTrivial(saleamount)){
          startSale(LedgerValue.parseImage(saleamount));
        }
      }
      catch (Exception ex) {
        dbg.Caught("reading console",ex);
      }
    }
  }

  private ConsoleClerk() {
  //super() creates debugger
  }

/**
 * this is public so that AutoClerk.makeFrom() can access it.
 */
  public static AutoClerk Create(EasyCursor ezc){
    return new ConsoleClerk();
  }

}
//$Id: ConsoleClerk.java,v 1.4 2003/10/25 20:34:24 mattm Exp $