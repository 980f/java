/* $Id: WantZero.java,v 1.9 2003/07/27 05:35:05 mattm Exp $ */
package net.paymate.ivicm.et1K;

import net.paymate.util.*;
import net.paymate.text.Formatter;

public class WantZero implements Callback {
  String locus;

  public Command Post(Command cmd){
    int rsp=cmd.response();
    if( rsp == ResponseCode.SUCCESS){//we want a zero, anything else is an error
      return cmd.next();
    } else {
      cmd.service.PostFailure(locus+" Error From Device:"+ Formatter.ox2(rsp));
      return null;//not all errors cna restart//cmd.restart();//start block over if this is a block command
    }
  }

  public WantZero(String commando){
    locus=commando;
  }

}
//$Id: WantZero.java,v 1.9 2003/07/27 05:35:05 mattm Exp $
