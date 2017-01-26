/**
 * Title:        adminapplet<p>
 * Description:  administrative applet<p>
 * Copyright:    Copyright (c) 2000 paymate<p>
 * Company:      paymate<p>
 * @author paymate
 * @version 1.0
 * $Id: adminapplet.java,v 1.3 2000/08/01 07:37:52 mattm Exp $
 */

package net.paymate.hwApplet;
import  java.applet.Applet;
import  java.net.URL;
import  net.paymate.util.GetMacid;

public class adminapplet extends Applet {
  public adminapplet() {}
  public void init(){}
  public void start(){
    System.out.println("started");
//    macid = GetMacid.getIt();
//    System.out.println("got macid = " + macid);
  }
  public void stop(){}

  public String macid="trash";

  public void checkHardware(String userid, String password) {
    System.out.println("Received userid=" + userid + ", password=" + password);
/*    String enMacid = "";
    URL doc=null;
    URL docTemp=null;
    try {
      docTemp = new URL(getDocumentBase() + "?unknown");
      enMacid = GetMacid.getIt();
      doc = new URL(getDocumentBase() + "?action=login&userid="+userid+
        "&password="+password+"&m="+enMacid);
      this.getAppletContext().showDocument(doc);
    } catch (Exception e) {
      // +++ eventually do something about this
      this.getAppletContext().showDocument(docTemp);
    }
*/
  }

}
