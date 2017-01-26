package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/Techuliner.java,v $
 * Description:  detects CR terminated lines as well as visabuffers
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.net.*;
import net.paymate.util.*;

public class Techuliner extends VisaLineRecognizer {
  public boolean endOfLineDetected(StringBuffer context,int incoming){
    if(!lrcing){//an LRC can look like any char.
      switch(incoming){
        case '?':{
          dbg.ERROR("Scarfing q-mark");
          context.setLength(context.length()-1);
        } break;
        case '+':{
          int end=context.length();
          try {
            return context.substring(end-3,end).equals("+++");
          }
          catch (Exception ex) {
            return false;
          }
        }
        case Ascii.CR:
          dbg.VERBOSE("CR");
          return true;       //potential at command.
        case Ascii.ACK:
        case Ascii.NAK: { //blow off buffer,  return control char
          dbg.VERBOSE("ACKER");
          context.setCharAt(0,(char)incoming);
          return true;
        }
      }
    }
    return super.endOfLineDetected(context,incoming);
  }

  public static Techuliner New() {
    Techuliner newone=new Techuliner();
    newone.config(true);
    return newone;
  }

}
//$Id: Techuliner.java,v 1.5 2003/01/14 14:55:25 andyh Exp $