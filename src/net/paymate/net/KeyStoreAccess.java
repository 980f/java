/* $Id: KeyStoreAccess.java,v 1.11 2001/07/19 01:06:51 mattm Exp $ */

package net.paymate.net;

import java.io.File;
import java.lang.System;
import java.util.Properties;
import net.paymate.util.ErrorLogStream;

public class KeyStoreAccess {
  protected static final ErrorLogStream dbg = new ErrorLogStream(KeyStoreAccess.class.getName());
  protected String myFileName = "cacerts";
  protected String myPassword = "paymate";//+++convert to charArray...
  public String Alias="";

  public String JDK=null;    //File.separator + "jdk1.2.2" + File.separator;
  public String JRELIB=null; //JDK+"jre" + File.separator + "lib" + File.separator;

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

  public KeyStoreAccess(){
    //live with defaults
  }

  public KeyStoreAccess(String name,String pass, String alias){
    myFileName = name;
    myPassword = pass;
    JDK = System.getProperty("java.home");
    if(JDK==null) {
      JDK = File.separator + "jdk1.2.2" + File.separator + "jre" + File.separator;
    }
    JRELIB = JDK + File.separator + "lib" + File.separator;
    Alias = alias;
  }

  public String toString() {
    return FileName();
  }

}
//$Id: KeyStoreAccess.java,v 1.11 2001/07/19 01:06:51 mattm Exp $
