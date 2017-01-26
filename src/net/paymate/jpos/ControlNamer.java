package net.paymate.jpos;

import net.paymate.jpos.common.JposWrapper;

/**
 * <p>Title: $Source:
 * /home/andyh/localcvs/pmnet/cvs/src/builders/serversinet.jpx,v $</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: PayMate.net</p>
 *
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */
public class ControlNamer {
  private static interface control { //values cribbed from an JCL.xml sample.
    String sigcap = "SignatureCapture"; //to jpos loader
    String swipe = "MSR";
    String pinpad = "PINPad";
    String keyboard = "Keyboard";
    String display = "LineDisplay";
  }

  StringBuffer ctrlNamer;
  final int catpoint;

  public ControlNamer(String root) {
    //demo{
    root="";
    //}
    ctrlNamer = new StringBuffer(root);
    if(ctrlNamer.length() > 0) {
      ctrlNamer.append('.');
    }
    catpoint = ctrlNamer.length();
  }

  public String name(String category) {
    ctrlNamer.setLength(catpoint);
    ctrlNamer.append(category);
    return ctrlNamer.toString();
    //   return category; //to see if names are magic.-- they are not.
  }

  public String sigName() {
    return name(control.sigcap);
  }

  public String msrName() {
    return name(control.swipe);
  }

  public String pinName() {
    return name(control.pinpad);
  }

  public String kbdName() {
    return name(control.keyboard);
  }

  public String dispName() {
    return name(control.display);
  }

}
