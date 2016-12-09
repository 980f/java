package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/FilePort.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: FilePort.java,v 1.2 2001/06/21 20:06:57 andyh Exp $
 */

import java.io.*;
class FilePort extends Port {
  /**
   * available to extended classes to deal with device type files
   */
  File fd;

  public String nickName(){
    try {
    return this.getClass().getName()+"."+fd.getCanonicalPath();
    } catch(Exception whocares){
      return "Bad "+this.getClass().getName();
    }
  }

  /**
   * this presumes that @param fd is already checked for validity.
   */
  public FilePort(File fd) throws FileNotFoundException {
    //open output for append, needed at least for testing with text file.
    //...due to an oversight by the java.io designers one must use String not File if one wants to append.
    super(fd.getPath(),new FileInputStream(fd),new FileOutputStream(fd.getPath(),true));
    this.fd=fd;
  }

}
//$Source: /cvs/src/net/paymate/serial/FilePort.java,v $