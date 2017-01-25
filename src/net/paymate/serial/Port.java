package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/Port.java,v $
 * Description:  for passing paired streams around.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Port.java,v 1.4 2001/07/21 03:19:39 andyh Exp $
 */

import java.io.InputStream;
import java.io.OutputStream;
import net.paymate.util.*;

public class Port {
  protected String nickname="BasePort";

  public String nickName(){
    return this.getClass().getName()+"."+nickname;
  }

  InputStream is;
  public InputStream rcv(){
    return is;
  }

  OutputStream os;
  public OutputStream xmt(){
    return os;
  }

  /**
   * flush and close both directions
   */
  public Port close(){
    Safe.Close(rcv());
    Safe.Close(xmt());
    return this;
  }

  public static final Port Close(Port thisone){
    if(thisone!=null){
      thisone.close();
    }
    return thisone;
  }

  public Port(String name,InputStream is, OutputStream os) {
    this(name);
    setStreams(is,os);
  }

  /**
   * for use by extensions only.
   */
  protected void setStreams(InputStream is, OutputStream os) {
    this.is=is;
    this.os=os;
  }

  protected Port(String name) {
    this.nickname=name;
  }

}
//$Source: /cvs/src/net/paymate/serial/Port.java,v $