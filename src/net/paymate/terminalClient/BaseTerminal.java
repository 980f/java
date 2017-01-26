package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/BaseTerminal.java,v $
 * Description:  base for any class of POS terminal, whether peripherals or external systems.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.25 $
 */

import net.paymate.data.TerminalInfo;
import net.paymate.connection.*;
import net.paymate.lang.ReflectX;
import net.paymate.util.*;

abstract public class BaseTerminal {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(BaseTerminal.class);

  public TerminalInfo termInfo;
  public Clerk clerk=new Clerk();//+_+ protect this

    // transaction agent
  protected ConnectionClient connectionClient;

  /**
  * for appliance's status grabbing.
  */
  public Standin Standin(){//4ipterm //4 formatters to do standin.
    return connectionClient.standin;
  }


  public String id(){//const
    return termInfo!=null?""+termInfo.id():"";
  }

  public BaseTerminal(TerminalInfo termInfo) {//config from server
    this.termInfo=termInfo;
  }

//  BaseTerminal() {
//    //compiler is falsely requiring this.
//  }
  /**
   * ezp contains the same data as was presented to TerminalInfo ...
   * @param ezp
   */
  public void Start(EasyCursor ezp){
    connectionClient= new ConnectionClient(termInfo);
    connectionClient.standin.startBacklog(); //called on connection
  }

  /**
   * the goal of stop is to kill all contained run()'s
   */
  public void Stop(){
    try {
      if (connectionClient != null) {
        connectionClient.Stop();
        connectionClient = null;
      }
    }
    finally {
      Appliance.terminalIsDown(this);
    }
  }

  abstract public boolean Post(Object obj);
  ////////////////////////////
  //
  public final boolean postClerkEvent(int clerkevent){//convenience function
    return Post(new ClerkCommand(new ClerkEvent(clerkevent)));
  }

  protected int Handle(TerminalCommand tc){
    switch(tc.Value()) {
      case TerminalCommand.Identify:
        System.out.println(this.toString());
        break;
      case TerminalCommand.GetConfig:
      case TerminalCommand.GetProgram:
      case TerminalCommand.StatusUp:
      case TerminalCommand.StatusDown:
      case TerminalCommand.Restart:
      case TerminalCommand.Shutdown: {
        Stop();
      }
      break;

      case TerminalCommand.Clear:
      case TerminalCommand.SERVICEMODE:
      case TerminalCommand.Normal:
      case TerminalCommand.sendSignature:
        break;

      case TerminalCommand.GoOffline: {
        Standin().setStandin(true);
      }
      break;
      case TerminalCommand.GoOnline: {
        Standin().setStandin(false);
      }
      break;
    }
    return 0;//+_+ don't have access to 'show.Nothing'
  }

  /////////////
  //
  /**
 * someday will be backed by non-volatile storage. Til then
 */
  protected int newClientStan(){
    return nextStan();
//    return psuedoStan(UTC.Now());
  }

  protected Counter seqstan;
  private void denullcount(){
    if(seqstan==null){
      seqstan = new Counter(1,999999,UTC.Now().getDigits(6,3));//each terminal has its own counter
    }
  }
  public final int nextStan(){
    denullcount();
    int purenumber=(int)seqstan.incr();
    return purenumber;//+_+ someday add checkdigit!
  }

//  public final int clearStan(){
//    denullcount();
//    return (int)seqstan.Clear();
//  }

  public static BaseTerminal instantiate(TerminalInfo termInfo){
    try {
      String shortname= ReflectX.stripNetPaymate(termInfo.className());
      if (shortname.equals("terminalClient.PosTerminal")){
          return new net.paymate.terminalClient.PosTerminal(termInfo);//both ivitrio and hypercom
      }
      if (shortname.equals("terminalClient.PosSocketTerminal")){
          return new net.paymate.terminalClient.PosSocketTerminal(termInfo);//ascii and jumpware
      }
      if (shortname.equals("terminalClient.SerialTerminal")){
          return new net.paymate.terminalClient.SerialTerminal(termInfo);//deprecated, early hypercom interface
      }
      if (shortname.equals("terminalClient.Techulator")){
          return new net.paymate.terminalClient.Techulator(termInfo);//paymentech's verifone 3200 interface
      }
      else {
        return null;
      }
    }
    catch (Exception ex) {
      dbg.Caught("While instantiating a terminal",ex);
      return null;
    }
  }

}
//$Id: BaseTerminal.java,v 1.25 2004/03/08 17:19:10 andyh Exp $