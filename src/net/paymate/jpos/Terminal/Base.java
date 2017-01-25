/* $Id: Base.java,v 1.14 2001/07/19 01:06:50 mattm Exp $ */
package net.paymate.jpos.Terminal;

import net.paymate.jpos.data.*;
import net.paymate.jpos.Terminal.Fundamental;

import net.paymate.util.ErrorLogStream;

import jpos.*;
import jpos.JposConst;
import jpos.events.*;

public class Base {//implement methods left off of BaseControl:
  static final ErrorLogStream dbg= new ErrorLogStream(Base.class.getName());

  private static final JposException Attach(DEService jposdev, String namer) {//legacy
    dbg.Enter("Attach:"+namer);
    try {
      JposException jape;
      jape=Fundamental.Attach((BaseControl)jposdev,namer);
      if(jape==null){
        jape=Flush(jposdev);
        //ignore error on flush and procede. the possible exceptions are TRIVIAL
        jposdev.addDataListener(jposdev);
        jposdev.addErrorListener(jposdev);
      } else {
        dbg.ERROR("Couldn't attach "+namer);
      }
      return jape;
    }
    finally {
      dbg.Exit();
    }
  }

  public static final JposException Attach(DEService jposdev, String group, String type) {
    return Attach(jposdev,DeviceName.fullname(group,type));
  }

  public static final JposException Release(DEService jposdev) {
    JposException jape=Flush(jposdev);
    if (jape==null) {
      return Fundamental.Release((BaseControl)jposdev);
    }
    return jape;
  }

  protected static final JposException Flush(DEService jposdev)  {
   try {
      jposdev.setDataEventEnabled(false);//???was this needed
      jposdev.clearInput();
      return null;
    } catch (JposException jape){
      return jape;
    }
  }

  public static final JposException Acquire(DEService jposdev) {
    dbg.Enter("Acquire:"+jposdev.toString());
    try {
      Flush(jposdev);
      //all the services have this but noooo it isn't in the BaseControl...
      jposdev.setDataEventEnabled(true);
      return null;
    } catch (JposException jape){
      return jape;
    } finally {
      dbg.Exit();
    }
  }

}
//$Id: Base.java,v 1.14 2001/07/19 01:06:50 mattm Exp $
