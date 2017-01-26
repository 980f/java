package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/PortProvider.java,v $
 * Description:   implements both the design and the dummy case of PortProvider
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: PortProvider.java,v 1.17 2003/08/22 20:16:03 andyh Exp $
 */

import net.paymate.util.*;
import net.paymate.*;
import net.paymate.lang.StringX;
import net.paymate.ivicm.comm.*;
import net.paymate.net.*; //for Socket Ports
import net.paymate.io.NullOutputStream;
import net.paymate.io.NullInputStream;
import java.io.*;

public class PortProvider {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(PortProvider.class);
  private static boolean oldway=true;

  public static void Config(boolean whichway){
    oldway=whichway;
    dbg.VERBOSE("oldway:"+oldway);
  }

  public static void Config(EasyCursor ezc){
    ezc.push("PortProvider");
    try {
      Config(ezc.getBoolean("oldway",oldway));
    }
    finally {
      ezc.pop();
    }
  }

  public static final Port makeNullPort(String name){
    dbg.VERBOSE("Making a null port");
    return new Port(name, new NullInputStream(), new NullOutputStream());
  }

  public static final Port makeFilePort(String name){
    dbg.Enter("makeFilePort");
    try {
      if(StringX.NonTrivial(name)){
        dbg.VERBOSE("making a filePort named <"+name+">");
        File fd = new File(name); //this pass user must provide complete path
        if (fd!=null) {
          dbg.VERBOSE("As file <"+fd.getAbsolutePath()+">");
          if(fd.exists()){ //we aren't going to insist on a real device yet
            dbg.VERBOSE("Actually making a file port<"+fd.getAbsolutePath()+">");
            return new FilePort(fd);
          } else {
            dbg.VERBOSE("file didn't exist");
          }
        }
      }
    } catch (Exception ex) {
      dbg.Caught("ignoring:",ex);   //do nothing
    } finally {
      dbg.Exit();//#gc  was missing
    }
  //on any problem make a bitbucket
    return makeNullPort(name);
  }

  public static final Port makeJavaxPort(String name) {
    try {
      dbg.VERBOSE("Making a javax port:<"+name+">");
      return new JavaxPort(name);
    }
    catch (Exception ex) {
      //appease compiler
    }
  //on any problem make a bitbucket
    return makeNullPort(name);
  }

  /**
   * legacy, make a serial port
   */
  public static final Port makePort(String name) {
    dbg.VERBOSE(oldway ? "javax": "fileio" );
    return oldway?makeJavaxPort(name):makeFilePort(name);
  }

  public static final Port openSerialPort(String dbgname,Parameters parms) {
    Port port = PortProvider.makePort(dbgname);
    if( ! port.openas(parms)){
      //port did  not open
      //@todo return a stubbed port that won't scream events
      return new FailedPort(dbgname+":"+parms.toSpam());
    }
    return port;
  }

  public static final Port openSerialPort(String dbgname,EasyCursor ezc) {
    Parameters parms=(Parameters) ezc.getObject("port",Parameters.class);
    return openSerialPort(dbgname,parms);
  }

  public static final Port openSocketPort(String dbgname,EasyCursor ezc) {
    IPSpec ip= IPSpec.New(ezc.getString("ip"));
    Port port= new SocketPort(ip);
    return port;
  }

  public static final Port openPort(String dbgname, EasyCursor ezc){
    boolean bySocket=ezc.getBoolean("socket",false);
    return bySocket ? openSocketPort(dbgname,ezc) : openSerialPort(dbgname,ezc) ;
  }


}
//$Source: /cvs/src/net/paymate/serial/PortProvider.java,v $