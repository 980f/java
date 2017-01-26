package net.paymate.ivicm;

/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/Base.java,v $
* Description:  shared parts of et1k and ec3k drivers.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.23 $
*/

import net.paymate.jpos.data.*;
import net.paymate.lang.ReflectX;
import net.paymate.util.*;

import java.io.*;

public abstract class Base  {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(Base.class);

  static final String VersionInfo="Jpos Base, (C) PayMate.net 2000 $Revision: 1.23 $";

  //set in constructors via 'identifiers'
  private String ServiceVersion="Unknown!";
  private String PhysicalDescription="Unknown!";
  private String InstanceName="Unknown!";

  protected String myName;

  protected boolean DataEventEnabled;
  protected boolean DeviceEnabled;

  private QReceiver posterm;
  public void setReceiver(QReceiver posterm){
    this.posterm=posterm;
  }

  public void PostFailure(Problem p){
    if(posterm!=null) {//was null on a 05 04 08 09.
      posterm.Post(p);
    }
  }

  public void PostFailure(String s){
    PostFailure(Problem.Noted(s+" " + String.valueOf(this)));
  }

  public void PostException(String msg,Exception jape){
    posterm.Post(new StifledException(msg,jape));
  }

  public void PostData(Object obj){
    try {
      dbg.Enter("PostData");//#gc
      posterm.Post(obj);
    }
    catch(Exception arful){
      dbg.ERROR(arful+" posting a "+ ReflectX.shortClassName(obj)+"=="+obj);
      dbg.Caught(arful);
    } finally {
      dbg.Exit();//#gc
    }
  }

  public void Illegal(String comment) {
    PostFailure("IllegalArgument:"+comment);
  }

  public  boolean Illegal(boolean expression, String comment) {
    if(expression){
      Illegal(comment);
    }
    return expression;
  }

  public void release()  {
    if(DeviceEnabled) {
      setDeviceEnabled(false);
    }
  }

  public void setDataEventEnabled(boolean flag) {
    DataEventEnabled = flag;
  }

  public synchronized void setDeviceEnabled(boolean beEnabled)  {
    DeviceEnabled = beEnabled;
    if(!DeviceEnabled){
      setDataEventEnabled(false);
    }
  }

  public String toString(){
    return InstanceName;
  }

  public String getPhysicalDeviceDescription(){
    return PhysicalDescription;
  }

  public Base(String s){
    myName=InstanceName=s;
  }

}
//$Id: Base.java,v 1.23 2003/07/27 05:35:03 mattm Exp $
