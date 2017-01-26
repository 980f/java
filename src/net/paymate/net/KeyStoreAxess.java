package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/KeyStoreAxess.java,v $
 * Description:  like java.net.InetAddress but doesn't do nasty library loads.
 * Copyright:    Copyright (c) 2001-2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import java.io.File;
import java.lang.System;
import java.util.Properties;
import net.paymate.util.ErrorLogStream;

public class KeyStoreAxess {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(KeyStoreAxess.class);
  private String myFileName = "cacerts";
  private String myPassword = "changeit";//+++convert to charArray...
  private String Alias="";
  /*package*/ String Alias(){
    return Alias;
  }

  private String JDK=null;
  private String JRELIB=null;

  /**
   * the keystore is NOT supposed to be the same as cacerts.
   * the path to it will NOT be the path to cacerts.
   * @todo separate out our server keys from cacerts.
   */
  public String FileName(){
    String fn = JRELIB + "security" + File.separator + myFileName;
    // log an error if it doesn't exist and tell the user where it should go
    File f = new File(fn);
    if(!f.exists()) {
      dbg.ERROR("cacerts file [" + fn + "] does not exist!");
    }
    return fn;
  }

  public char[] Password(){
    return myPassword.toCharArray();
  }

  public KeyStoreAxess(){
    JDK = System.getProperty("java.home");
    JRELIB = JDK + File.separator + "lib" + File.separator;
  }

  public KeyStoreAxess(String name,String pass, String alias){
    this();
    myFileName = name;
    myPassword = pass;
    Alias = alias;
  }

  public String toString() {
    return FileName();
  }

}
//$Id: KeyStoreAxess.java,v 1.5 2003/01/23 00:15:16 andyh Exp $
