package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/SerialLineServer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.util.*;
import net.paymate.serial.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;

public class SerialLineServer extends LineServer {
  ErrorLogStream dbg;

  public Port port;//opened up for Techulator
  Parameters sp;
  public static SerialLineServer New(Parameters sp,LineServerUser user, boolean isDaemon){
    SerialLineServer newone= new SerialLineServer(isDaemon);
    newone.ignoreEOF=true;
    newone.sp=sp;
    newone.myUser=user;
    return newone;
  }

  private SerialLineServer(boolean isDaemon) {    //for reflective instantiation
    super(ReflectX.justClassName(SerialLineServer.class), isDaemon);
    dbg= ErrorLogStream.getForClass(this.getClass());
    dbg.WARNING("SerialLineServer debugging as "+dbg.myName());
  }

  public boolean Start(){
    try {
      port= PortProvider.makePort("SLS");
      if(!port.openas(sp)){
        dbg.WARNING("failed to openas");
        return false;
      }
      dbg.WARNING("starting lineServer");
      return super.Start();
    } catch(Exception ex){
      dbg.Caught("While opening "+sp,ex);
      return false;
    }
  }

  public void Stop(){//for use in panics only
    Port.Close(port);
  }

   public void run(){
    dbg.WARNING("Ready for Connections");
    while(!killed){
      try{
        //shall we wait for something special to indicate connection desired???
        //such as RTS?
        byte [] onconnect=myUser.onConnect();
        if(ByteArray.NonTrivial(onconnect)){
          port.xmt().write(onconnect);
        }

        try {
          core(port.rcv(),port.xmt());
        }
        catch(Exception ioe){
          dbg.Caught("while running got:",ioe);
        }
        port.close();
      } catch(Throwable ex){
        dbg.Caught("while starting got ",ex);
      }
    }//wait for another connect.
  }

}
//$Id: SerialLineServer.java,v 1.10 2003/07/27 05:35:11 mattm Exp $