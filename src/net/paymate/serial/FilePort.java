package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/FilePort.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: FilePort.java,v 1.11 2003/07/27 18:54:34 mattm Exp $
 */

import java.io.*;
import net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.util.Executor;
import net.paymate.lang.ReflectX;

class FilePort extends Port {
  static ErrorLogStream dbg;
  /**
   * available to extended classes to deal with device type files
   */
  File fd;

  public String nickName(){
    try {
      return ReflectX.shortClassName(this,fd.getCanonicalPath());
    } catch(Exception whocares){
      return "Bad "+ReflectX.shortClassName(this);
    }
  }

  /**
   * this presumes that @param fd is already checked for validity.
   */
  public FilePort(File fd) throws FileNotFoundException {
    //open output for append, needed at least for testing with text file.
    //...due to an oversight by the java.io designers one must use String not File if one wants to append.
    super(fd.getPath(),new FileInputStream(fd),new FileOutputStream(fd.getPath(),true));
   if(dbg==null) dbg=ErrorLogStream.getForClass(FilePort.class);
    this.fd=fd;
  }

 //these parameters not yet homored
  //        jxport.setFlowControlMode(parms.flowControl);//was using unknown default
//        jxport.setOutputBufferSize(parms.obufsize);
//        jxport.setRTS(parms.initialRTS);
//        super.initRts(parms.initialRTS);
//        jxport.setDTR(parms.initialDTR);

  public boolean openas(Parameters parms){
    TextList sttycommand=new TextList();
    try {
      sttycommand.add("/root/bin/setPortParams");
      sttycommand.add("-F");
      sttycommand.add(parms.portName);
      sttycommand.add(parms.getBaudRateString());
      if(parms.haveparity){
        sttycommand.add("parenb");
        sttycommand.add( Bool.dash(parms.evenparity) + "parodd");
      } else {
        sttycommand.add("-parenb");
      }
      sttycommand.add("cs"+parms.getDatabits());
//      dbg.VERBOSE("exec'ing:"+sttycommand.asParagraph(OS.EOL));
//      return Executor.ezExec(sttycommand.asParagraph(" "),10)==0;//0 is good from experiments with commandline
      return Executor.runProcess(sttycommand.asParagraph(" "),"set file port", 1, 10, sttycommand, true, null)==0;
    }
    finally {
      dbg.VERBOSE("exec:"+sttycommand.asParagraph(OS.EOL));
    }
  }

}
//$Source: /cvs/src/net/paymate/serial/FilePort.java,v $