package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/PortProvider.java,v $
 * Description:   implements both the design and the dummy case of PortProvider
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: PortProvider.java,v 1.3 2001/07/21 03:19:39 andyh Exp $
 */

import net.paymate.util.*;
import net.paymate.ivicm.comm.*;
import java.io.*;

public class PortProvider {
  static final Tracer dbg=new Tracer(PortProvider.class.getName());

  public static final Port makeNullPort(String name){
    dbg.VERBOSE("Making a null port");
    return new Port(name, new NullInputStream(), new NullOutputStream());
  }

  public static final Port makeFilePort(String name){
    dbg.Enter("makeFilePort");
    try {
      if(Safe.NonTrivial(name)){
        dbg.VERBOSE("Try making a filePort named <"+name+">");
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
    }
  //on any problem make a bitbucket
    return makeNullPort(name);
  }

  public static final Port makeJavaxPort(Parameters sp) {
    try {
      dbg.VERBOSE("Making a serial port:<"+sp.getPortName()+">");
      return new JavaxPort(sp);
    }
    catch (Exception ex) {
      //appease compiler
    }
  //on any problem make a bitbucket
    return makeNullPort(sp.getPortName());
  }

}
//$Source: /cvs/src/net/paymate/serial/PortProvider.java,v $