package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/SerialTerminal.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

import net.paymate.*;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.terminalClient.PosSocket.*;

import net.paymate.serial.*;

public class SerialTerminal extends ExternalTerminal {//implements LineServerUser,CnxnUser
  static ErrorLogStream dbg;
  SerialLineServer xterm=null;
  String portname="COM1";

  public SerialTerminal(TerminalInfo termInfo) {////public for load by reflection, uses config from server
    super(termInfo);
    if(dbg==null) dbg=ErrorLogStream.getForClass(SerialTerminal.class);
    portname=termInfo.equipmentlist.getString("port2serve");
  }
//  public SerialTerminal() {////public for load by reflection, uses config from server
//    super();
//  }

  public byte [] onConnect(){
    dbg.VERBOSE("just got a connection");
    return (Ascii.CRLF+"Connected,format."+former.formatId()+","+params.toSpam()+",END"+Ascii.CRLF).getBytes();
  }

  Parameters params;

  protected void setPortDefaults(EasyCursor ezp){
    ezp.Assert(Parameters.baudRateKey,"9600");
  }

  public void Start(EasyCursor hacks){//called once, local config tweaks
    super.Start(hacks); //system properties

    hacks.push("XTERM");//non-server origin;
    try {
      hacks.push(portname);
      try {
        setPortDefaults(hacks);
        params= new Parameters(portname,hacks);
      } finally {
        hacks.pop();
      }
    } finally {
      hacks.pop();//xterm
    }
    xterm=SerialLineServer.New(params, (LineServerUser) this, true);
    xterm.Start(); //start serving bytes at port
    super.Start("XTERM."+params.getPortName());  //start host interface
  }

  protected void fancyExit(String why){
    xterm.Stop();
    super.fancyExit(why);
  }

}
//$Id: SerialTerminal.java,v 1.16 2003/10/25 20:34:25 mattm Exp $