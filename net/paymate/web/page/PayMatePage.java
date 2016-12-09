/**
 * Title:        PayMatePage<p>
 * Description:  Base Page for the paymate website<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: PayMatePage.java,v 1.16 2001/10/15 22:41:38 andyh Exp $
 */

package net.paymate.web.page;
import  net.paymate.util.Safe;
import  net.paymate.web.color.*;
import  net.paymate.util.ErrorLogStream;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  java.io.ByteArrayOutputStream;

public abstract class PayMatePage extends Document implements Entities {

  private static final ErrorLogStream dbg = new ErrorLogStream(PayMatePage.class.getName());

  // some static stuff
  protected static final String COMPANY       = "PayMate.net";
  protected static final String PATHPREFIX    = "/servlets/admin/";   // +++ get this from somewhere (setting)
  protected static final String IMAGELOCATION = "/images/"; // +++ get this from somewhere (setting)
  public    static final String SUBMITBUTTON  = "action";
  protected static final ElementContainer THREESPACES = new ElementContainer(); // used to space out the links nicely in the bars
  static {
    THREESPACES.addElement(NBSP)
               .addElement(NBSP)
               .addElement(NBSP);
  }

  public static final String PERCENT100 = "100%";
  public static final BR br = new BR();
  public static final StringElement LF = new StringElement("\n"); // makes reading the generated files easier
  public static final ElementContainer BRLF;
  static {
    ElementContainer brlftmp = new ElementContainer();
    BRLF = brlftmp.addElement(br).addElement(LF);
  }

  protected boolean loggedIn = false;

  public static ColorScheme serverDefaultColors = ColorScheme.TRANQUILITY; // set elsewhere; perhaps keep in higher class and pass in for constructor?

  public ColorScheme colors = serverDefaultColors;

  public String myKey()  {
    return key(this.getClass());
  }
  public static final String key(Class c) {
    String key  = "";
    String name   = c.getName();
    int lastDotAt = name.lastIndexOf('.');
    if(lastDotAt > -1) {
      key = name.substring(lastDotAt+1);
    }
    return key;
  }


  protected String title = null;
  protected String loginInfo = null;
  public PayMatePage(String title, String loginInfo) {
    super();
    this.loginInfo = loginInfo;
    setTitle(title);
  }

  public static final String withTitle(String suffix) {
    return COMPANY + " - " + suffix;
  }

  public void setTitle(String title) {
    this.title = title;
    appendTitle(withTitle(title));
  }

  public void addToHeader(Element inHead) {
    if(inHead != null) {
      getHead().addElement(inHead);
    }
  }

  public void fillBody(Element inBody) {
    this.loggedIn = Safe.NonTrivial(loginInfo);
    // this is where we add all sub-things!

    TD tdInner = new TD();

    // the title stuff
    //tdInner.addElement(new Center( new H2(title)));//.addElement(BRLF);

    if(inBody != null) {
      tdInner.addElement(inBody);
    }
    TR trInner = new TR(tdInner);
    Table tInner = new Table();
    tInner.setWidth("95%")
          .setAlign(AlignType.CENTER)
          .addElement(trInner);

    ElementContainer ec = new ElementContainer();
    ec.addElement(headerBar())
      .addElement(BRLF)
      .addElement(tInner)
      .addElement(BRLF)
      .addElement(footerBar());

    TD td = new TD();
    td.setBgColor(colors.LIGHT.BG).addElement(ec);
    TR tr = new TR(td);
    Table t = new Table();
    t.setWidth(PERCENT100)
     .addElement(tr)
     .setAlign(AlignType.CENTER);

    getBody().setBgColor(ColorScheme.WHITE)
             .addElement(t);
  }

  // generator functions
  protected Element headerBar() {
    // First column contains paymate name
    // second column contains all possible site links (for now), specifically:
    //  home
    //  about
    //  account login/logout
    // some page has to be the current one
    // if the current page is one of the above pages,
    // don't provide the link for it, just the text
    return bar(companyLogo(), pageLinks(loggedIn ?
      makeLink(Logout.url(), Logout.name()):
      makeLink(Login.url(), /*Acct.name()*/"")), null);
  }

  public static final String contactURL = "http://www.paymate.net/contact_us!.htm";
  public static final String contactName = "Contact Us";
  public static final String copyrightURL = "http://www.paymate.net/copyright.htm";

  protected Element footerBar() {
    // first column is contact link
    // second column is copyright link
    return bar(
      makeLink(contactURL, contactName),
      makeLink(copyrightURL, copyright()),
      "-2");
  }

  protected Element bar(Element left, Element right, String fontsize) {
    String fgColor = colors.DARK.BG;
    String bgColor = ColorScheme.WHITE;
    Table t = new Table();
    t .setWidth(PERCENT100)
      .setBgColor(bgColor)
      .setBorder(0)
      .addElement(new TR(new TD(leftRightSplit(left, right, fgColor, bgColor, fontsize))));
    return t;
  }

  protected Element pageLinks(Element loginElement) {
    // make a list of the links, space-delimited
    ElementContainer ec = new ElementContainer();

    ec.addElement(loginElement);
    // @MAILER@ this one will be commented out until we get sendmail working on Monster
/*    ec.addElement(THREESPACES)
      .addElement(makeLink(""https://mailserver.paymate.net"", "Webmail"));*/
    return ec;
  }

  protected Element makeLink(String page, String contents) {
    return makeLink(page, new StringElement(contents));
  }
  protected Element makeLink(String page, Element contents) {
    return makeLink(page, contents, myKey());
  }
  protected static final Element makeLink(String page, String contents, String localKey) {
    return makeLink(page, new StringElement(contents), localKey);
  }
  protected static final Element makeLink(String page, Element contents, String localKey) {
    if(page.equalsIgnoreCase(localKey)) { /* if it is this page */
      return contents;     /* don't make a link out of it */
    }
    A a = new A();
    a.setHref(makeURI(page));
    a.addElement(contents);
    return a;
  }

  protected static final String makeURI(String page) {
    return (((page.indexOf(":") > -1) || (page.indexOf("/") == 0)) ?
      /* path is already absolute or related */ "" :
      PATHPREFIX) + page;
  }

  protected final Element companyURL() {
    return makeLink("http://www.paymate.net", COMPANY);
  }

  protected Element companyLogo() {
    ElementContainer ec = new ElementContainer();
    ec.addElement(companyURL())
      .addElement(" Account Administration")
      .addElement(new StringElement(Safe.NonTrivial(loginInfo)?(" - " + loginInfo):""));
    return ec;
  }

  // (c) 2000 PayMate.net Corporation.  All rights reserved.
  // the copyright is a link to a copyright page (legal notices), which is not on the headerBar
  // +++ set this once per day; after that, just lookup from setting, for speed
  protected static final Element copyright() {
    return new StringElement(net.paymate.Revision.CopyRight(COPY));
  }

/**
 * This method creates a table with one row and two columns, each which is aligned away from the other
 * it is for headers and footers
 */
  protected static final Element leftRightSplit(Element left, Element right, String fgColor, String bgColor, String fontSize) {

    Font fg = new Font();
    fg.setColor(fgColor);
    if(Safe.NonTrivial(fontSize)) {
      fg.setSize(fontSize);
    }
    Font fg2 = (Font) fg.clone();

    fg.addElement(left);
    fg2.addElement(right);

    TD leftCol  = new TD(fg);
    TD rightCol = new TD(fg2);
    leftCol.setAlign(AlignType.LEFT);
    rightCol.setAlign(AlignType.RIGHT);

    TR tr = new TR();
    tr.addElement(leftCol).addElement(rightCol);

    Table t = new Table();
    t .setWidth(PERCENT100)
      .setBgColor(bgColor)
      .setBorder(0)
      .addElement(tr);

    return t;
  }

  public Element contentFromStrings(String contents[]) {
    ElementContainer ec = new ElementContainer();
    return addLines(ec, contents);
  }

  public ElementContainer addLines(ElementContainer ec, String [] lines) {
    for(int i = 0; i<lines.length; i++) {
      ec.addElement(lines[i]).addElement(BRLF);
    }
    return ec;
  }

}

