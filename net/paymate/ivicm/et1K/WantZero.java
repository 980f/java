/* $Id: WantZero.java,v 1.7 2001/07/03 06:18:36 andyh Exp $ */
package net.paymate.ivicm.et1K;

import net.paymate.util.*;

public class WantZero implements Callback {
  String locus;

  public Command Post(Command cmd){
    int rsp=cmd.response();
    if( rsp == Codes.SUCCESS){//we want a zero, anything else is an error
      return cmd.next();
    } else {
      cmd.service.PostFailure(locus+" Error From Device:"+ Safe.ox2(rsp));
      return null;//not all errors cna restart//cmd.restart();//start block over if this is a block command
    }
  }

  public WantZero(String commando){
    locus=commando;
  }

}
//$Id: WantZero.java,v 1.7 2001/07/03 06:18:36 andyh Exp $
