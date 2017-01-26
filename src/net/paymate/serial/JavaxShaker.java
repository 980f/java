package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/JavaxShaker.java,v $
 * Description:  javax access to outgoing control lines
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */
import javax.comm.*;
import net.paymate.util.*;

public class JavaxShaker extends Shaker {
/*javax.comm*/  SerialPort jxport;
  boolean beRTS;
  /**
   * returns whether this is a change
   */
  public boolean setto(boolean on){
    if(super.setto(on)){
      //actually set the bit.
      if(jxport!=null){
        if(beRTS){
          jxport.setRTS(on);
        } else {
          jxport.setDTR(on);
        }
      }
      return true;
    } else {
      return false;
    }
  }

  private JavaxShaker(SerialPort jxport,boolean beRTS,String tla,boolean initvalue) {
    super(tla,initvalue);
    this.jxport=jxport;
    this.beRTS=beRTS;
  }

  public static Shaker makeRTS(SerialPort jxport){
    Shaker newone;
    try {
      newone=new JavaxShaker(jxport,true,"RTS",jxport.isRTS());
    }
    catch (Exception ex) {
      newone=Shaker.Virtual("RTS",OFF);
    }
    return newone;
  }

  public static Shaker makeDTR(SerialPort jxport){
    Shaker newone;
    try {
      newone=new JavaxShaker(jxport,false,"DTR",jxport.isDTR());
    }
    catch (Exception ex) {
      newone=Shaker.Virtual("DTR",OFF);
    }
    return newone;
  }

}
//$Id: JavaxShaker.java,v 1.1 2002/09/06 18:56:12 andyh Exp $