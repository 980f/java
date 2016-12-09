package net.paymate.web.page;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/page/News.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import  org.apache.ecs.*;
import  org.apache.ecs.html.*;

public class News extends PayMatePage {

  public News(String comment, String loginInfo) {
    super(name(), loginInfo);
    fillBody(contents(comment));
  }

  public static Element contents(String comment) {
    ElementContainer ec = new ElementContainer();
    ec.addElement(new Center(new H2(name())))
      .addElement(BRLF)
      .addElement(new Center().addElement(new StringElement("Welcome to the PayMate.net Account Administration Website.")))
      .addElement(BRLF)
      .addElement(BRLF)
      .addElement(new Center().addElement(new StringElement(
                    "Please keep your eye on this page.  In the future, it will contain exciting news and important information to help you get the most out of your PayMate.net accounts.")))
      .addElement(BRLF);
    if(comment != null) {
      ec.addElement(comment);
    }
    return ec;
  }

  public static final String name() {
    return key();
  }
  public static final String url() {
    return key();
  }
  public static final String key() {
    return PayMatePage.key(News.class);
  }
}
