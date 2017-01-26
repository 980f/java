/**
 * Title:        Login<p>
 * Description:  PayMate Login Page<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Login.java,v 1.20 2004/04/08 09:09:52 mattm Exp $
 */

package net.paymate.web.page;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.util.*;

public class Login extends PayMatePage {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Login.class);

  public Login(String comment, String loginInfo) {
    this(new StringElement(comment), loginInfo);
  }

  public Login(Element comment, String loginInfo) {
    super(name(), loginInfo, null, null, false /*we will never create this page in archive mode*/);
    fillBody(content(comment));
  }

  public static final String USERID   = "userid";
  public static final String PASSWORD = "password";
  public static final String ENTID    = "entid";
  public static final String STOREID  = "storeid";
  public static final String ASSOCID  = "associd";
  public static final String SAXID    = "storeaccessid";

  public static final Element content(Element comment) {
    Form form = NewPostForm(Acct.key());
    // create the table & fill it with buttons and text fields
    // add it to the form

    TD td1a = new TD("UserID:");
    TD td1b = new TD(new Input(Input.TEXT, USERID, ""));
    TR tr1  = new TR(td1a);
    tr1.addElement(td1b);

    TD td2a = new TD(new StringElement("Password:"));
    TD td2b = new TD(new Input(Input.PASSWORD, PASSWORD, ""));
    TR tr2  = new TR(td2a);
    tr2.addElement(td2b);

    TD td3a = new TD(new StringElement("EnterpriseID [optional]:"));
    TD td3b = new TD(new Input(Input.TEXT, ENTID, ""));
    TR tr3  = new TR();
    tr3.addElement(td3a).addElement(td3b);

    Input loginbutton = new Input(Input.SUBMIT, SUBMITBUTTON, "Login");
    //loginbutton.setOnClick("getid()");
    TD td4 = new TD(loginbutton);
    td4.setAlign(AlignType.RIGHT).setColSpan(2);
    TR tr4 = new TR(td4);

    Table t = new Table();
    t.addElement(tr1).addElement(tr2).addElement(tr4).addElement(tr3);

    form.addElement(new Center(new H2(name())))
        .addElement(new Center(t));

    if(comment !=null) {
      form.addElement(BRLF)
          .addElement(comment)
          .addElement(BRLF);
    }

    //form.addElement(new Applet().setCode("net/paymate/hwApplet/adminapplet.class").setHeight(1).setWidth(1).setName("testapplet").addElement("P"));

    return form;
  }

  public static final String name() {
    return key();
  }
  public static final String  url() {
    return key();
  }
  public static final String key() {
    return PayMatePage.key(Login.class);
  }
}

// +++ Make login screen autofocus the cursor onto the username field (and also do the no-cache thing with ALL webpages; do autocomplete off?):
/*
<HEAD><TITLE>AMFCU - Home Banking</TITLE>
<META http-equiv="pragma" content="no-cache">
<script language=javascript>
function clearSignOn()
{
    document.forms[0].memnumber.value = "";
    document.forms[0].mempwd.value = "";
    document.signon.memnumber.focus();
}
</script>
</HEAD>
<BODY OnLoad="clearSignOn()" bgcolor=cccccc text=#000000 link=#000000
alink=#000000 vlink=#000000>
<form name="signon" AUTOCOMPLETE=off action="/scripts/ibank.dll"
method=post>
<INPUT TYPE ="HIDDEN" NAME=Func VALUE="SignOn">
<INPUT TYPE=HIDDEN NAME=Frames VALUE="150">
<INPUT TYPE ="HIDDEN" NAME=homepath VALUE="cu3">
<HR SIZE="5">
<TABLE>
<TR>
<TD><B>Please enter your member number:</td><td><input type="text"
name="memnumber" SIZE="15"></td></tr>
<TR>
<TD><B>Please enter your password:</td><td><INPUT TYPE="PASSWORD"
name="mempwd" SIZE="15"></B></td></tr>
</TABLE>
<P>
<INPUT TYPE=HIDDEN NAME=dummy Value="0">
<INPUT TYPE="submit" VALUE="Sign-On">
<INPUT TYPE="Reset" VALUE="   Clear   ">
</p>
</FORM>
Unauthorized use or access of this system is strictly prohibited!! <br>
</BODY>
*/

/* old:
<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
   <meta http-equiv="Content-Script-Type" content="text/javascript">
   <meta name="cvs" content="$Id: Login.java,v 1.20 2004/04/08 09:09:52 mattm Exp $">
   <title>TESTING APPLET</title>
<SCRIPT>
function getid() {
//  var checksuccess =
  document.testapplet.checkHardware();
//  var System = java.lang.System;
//  var outputStr = "MACID="+macid;
//  System.out.println(outputStr);
//  if(checksuccess) {
//    //  don't post all inputs when not using pentest (when doing for real)
//    var param = "username-"+document.loginform.userid.value+"%20password-";
//    param+=document.loginform.password.value+"%20encryptedMACID-"+document.testapplet.enMacid;
//    param+="%20encryptedKey-"+document.testapplet.enKey;
//    document.loginform.set.value = param;
//    document.loginform.submit();

//  } else {
//    alert("Required hardware missing.");
//  }
}
</SCRIPT>
</head>
<body>
TESTING APPLET<BR><BR>

<form NAME="loginform" ACTION="https://208.58.21.60:8443" METHOD="POST">

<applet code="adminapplet.class" HEIGHT="30" WIDTH="30" name="testapplet">P</applet>ayMate.net<BR>
<table><TR><TD>
UserID:</TD><TD>
<INPUT type="text"     name="userid"   value = ""></TD></TR><TR><TD>
Password:</TD><TD>
<INPUT type="password" name="password" value = ""></TD></TR><TR><TD COLSPAN="2" ALIGN=RIGHT>
<INPUT type="button"   name="getit"    value="Login" onclick="getid()"></TD></TR></TABLE>
<INPUT type="hidden"   name="set"       value="">


</form>

<br>&nbsp;
<br>&nbsp;
</body>
</html>
*/
