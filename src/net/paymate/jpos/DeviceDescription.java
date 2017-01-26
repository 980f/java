package net.paymate.jpos;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/DeviceDescription.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import java.util.Vector;
import net.paymate.lang.ReflectX;

public class DeviceDescription implements isEasy {
  static final Tracer dbg=new Tracer(DeviceDescription.class);

  public String name;
  public String className;
  Vector resources=new Vector();

  public void save(EasyCursor ezp){
    ezp.setString("className",className);
  }

  /**
   * @#deprecated public so that we can use isEasy, but this instance should never be called
   * i.e. we don't load base class DeviceDescription
   */
  public void load(EasyCursor ezp){
//    className=ezp.getString("className");
  }

  public static final DeviceDescription Create(EasyCursor ezp, String name){
    DeviceDescription newone=null;
    dbg.Enter("Create");
    ezp.push(name);
    String className=null;
    try {
      className = ezp.getString("class");
      newone = (DeviceDescription)ReflectX.newInstance(className);
      newone.load(ezp);
      newone.name=name;
      newone.className=className;
    }
    catch(NullPointerException npe){
      dbg.ERROR("No properties or no className therein for class named'" + className + "'!");
    }
    catch (Exception e) {
      dbg.Caught(e);
    } finally {
      ezp.pop();
      dbg.Exit();
      return newone;
    }
  }

}
//$Id: DeviceDescription.java,v 1.6 2003/07/27 05:35:06 mattm Exp $