package net.paymate.web.page.accounting;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/page/accounting/News.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.web.*;
import net.paymate.web.page.*;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.util.*; // Safe
import net.paymate.lang.StringX;

public class News extends Acct {

  public News(String comment, LoginInfo linfo, AdminOpCode opcodeused) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(contents(comment));
  }

  public static Element contents(String comment) {
    ElementContainer ec = new ElementContainer();
    if(StringX.NonTrivial(comment)) {
      ec.addElement(comment);
    } else {
      ec.addElement(new Center(new H2(name())))
        .addElement(BRLF)
        .addElement(new Center().addElement(new StringElement("Welcome to the "+COMPANY+" "+ACCOUNTADMINISTRATION+" Website.")))
        .addElement(BRLF)
        .addElement(BRLF)
        .addElement(new Center().addElement(new StringElement(
                      "Please keep your eye on this page.  In the future, it will contain exciting news and important information to help you get the most out of your PayMate.net accounts.")))
        .addElement(BRLF);
    }
    return ec;
  }

//  public static final String name() {
//    return key();
//  }
  public static final String url() {
    return key();
  }
//  public static final String key() {
//    return PayMatePage.key(News.class);
//  }
}
//$Id: News.java,v 1.3 2004/04/08 09:09:53 mattm Exp $