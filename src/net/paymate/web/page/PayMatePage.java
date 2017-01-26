/**
 * Title:        PayMatePage<p>
 * Description:  Base Page for the paymate website<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: PayMatePage.java,v 1.30 2004/04/08 09:09:52 mattm Exp $
 */

package net.paymate.web.page;
import  net.paymate.web.color.*;
import  net.paymate.web.*;
import  net.paymate.util.ErrorLogStream;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  java.io.*;
import net.paymate.lang.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import java.util.Enumeration;

public abstract class PayMatePage extends Document implements Entities {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PayMatePage.class);

  // some static stuff
  protected static final String COMPANY       = "PayMate.net";
  protected static final String PATHPREFIX    = "/servlets/admin/";   // +++ get this from somewhere (setting)
  protected static final String IMAGELOCATION = "/images/"; // +++ get this from somewhere (setting)
  public    static final String SUBMITBUTTON  = "action";

  public static final String PERCENT100 = "100%";
  public static final String BR = "<BR>";
  public static final String LF = "\n";
  public static final String BRLF = BR+LF;

  protected boolean loggedIn = false;

  private ColorScheme colors = ColorScheme.defaultscheme();
  protected ColorScheme colors() {
    return colors;
  }
  public void setColors(ColorScheme colors) {
    if(colors != null) {
      this.colors = colors;
    }
  }

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


  protected String loginInfo = null;
  protected AdminOpCode opcodeUsed = null;
  protected LoginInfo linfo = null;
  private boolean archive = false;
  private StopWatch fullsw = new StopWatch();
  public PayMatePage(String title, String loginInfo, AdminOpCode opcodeUsed,
                     LoginInfo linfo, boolean archive) {
    super();
    this.opcodeUsed = opcodeUsed;
    this.loginInfo = loginInfo;
    this.linfo = linfo;
    this.archive = archive;
    setDoctype(new Doctype.Html40Transitional());
    setTitle(title);
  }

  public static final String withTitle(String suffix) {
    return COMPANY + " - " + suffix;
  }

  public void setTitle(String title) {
//    this.title = title;
    appendTitle(withTitle(title));
  }

  public void addToHeader(Element inHead) {
    if(inHead != null) {
      getHead().addElement(inHead);
    }
  }

  // the menu ...
 private static final AdminOp menuops[] = {
    // the preferred order ...
    AdminOp.TerminalsAdminOp,
    AdminOp.DrawersAdminOp,
    AdminOp.DepositsAdminOp,
    AdminOp.BatchesAdminOp,
    AdminOp.SearchAdminOp,
    AdminOp.authMsgsOp,
    AdminOp.StoresAdminOp,
    AdminOp.EnterprisesAdminOp,
    AdminOp.AssociatesAdminOp,
    AdminOp.AppliancesAdminOp,
    AdminOp.debugpg, // services
    AdminOp.NewsAdminOp,
  };

  private static final B wingit(String middle) {
    B ret = new B();
    ret.addElement(middle);
    return ret;
  }

  public static final A flyoverLink(String url, String title, String linkText) {
    A ret = new A();
    ret.setHref(url).addElement(linkText).setTitle(title);
    return ret;
  }
  private static final String EMPTYLINK = "#_self";
  public static final A flyoverEmptyLink(String title, String linkText) {
    return flyoverLink(EMPTYLINK, title, linkText);
  }

  // makes it new every time in case the permissions change while the user is logged in
  private Element makeMenu(AdminOpCode current, LoginInfo linfo) {
    boolean NEWWAY = true;
    TR tr = new TR();
    // for the new way
    boolean donefirst = false;
    ElementContainer ec = new ElementContainer();
    for(int imenu = 0; imenu < menuops.length; imenu++) {
      AdminOp op = menuops[imenu];
      if(linfo.permissionsValid(op.code())) {
        String url = op.url();
        Element name = op.code().is(current.Value()) ? (Element)wingit(op.name()) : new StringElement(op.name());
        if(NEWWAY) {
          if(donefirst) {
            ec.addElement(" | ");
          } else {
            donefirst = true;
          }
          if(StringX.NonTrivial(url)) {
            ec.addElement(new A(Acct.key()+"?"+url, name));
          } else {
            ec.addElement(name);
          }
        } else { // OLD way
          if(StringX.NonTrivial(url)) {
            tr.addElement(new TD(new Center(new A(Acct.key()+"?"+url, name))));
          } else {
            tr.addElement(new TD(new Center(name)));
          }
          tr.addElement(PayMatePage.LF);
        }
      }
    }
    if(NEWWAY) {
      tr.addElement(new TD(ec));
    }
    return tr;
  }

  protected static boolean isProduction = true; // the default

  public static final void init(boolean newIsProduction) {
    isProduction = newIsProduction;
  }

  public void fillBody(Element inBody) {
    this.loggedIn = StringX.NonTrivial(loginInfo);
    if((linfo != null) && (linfo.colors() != null)) {
      setColors(linfo.colors()); // just to be sure (in case they changed)
    }
    // this is where we add all sub-things!

    TD tdInner = new TD();

    // the title stuff
    //tdInner.addElement(new Center( new H2(title)));//.addElement(BRLF);

    // here, we need a menu
    if((opcodeUsed != null) && (linfo != null) && ! archive) {
      Element trmenu = makeMenu(opcodeUsed, linfo);
      Table t1 = new Table();
      t1.setBgColor(colors.MEDIUM.BG)
          .setBorder(0)
          .setWidth(PayMatePage.PERCENT100)
          .setCellSpacing(0)
          .setCellPadding(0)
          .addElement(trmenu);
      tdInner.addElement(t1)
          .addElement(PayMatePage.BRLF);
    }

    if(inBody != null) {
      tdInner.addElement(inBody);
    }

    // add statistics, if needed
    // for gawds, output time of page display and duration
    if((linfo != null) && linfo.isaGod() && ! archive) {
      String pre = (linfo.ltf() != null) ? (linfo.ltf().format(UTC.Now()) + " -- ") : "";
      tdInner.addElement(pre + "Page instantiation duration: " +
                         DateX.millisToSecsPlus(fullsw.Stop()) + ", stream to bytes duration: ")
          .addElement(new PageStatistics(""));
    }

    TR trInner = new TR(tdInner);
    Table tInner = new Table();
    tInner.setWidth("95%")
          .setAlign(AlignType.CENTER)
          .addElement(trInner);

    ElementContainer ec = new ElementContainer();
    if(archive) {
      ec.addElement(tInner);
    } else {
      ec.addElement(headerBar())
          .addElement(BRLF)
          .addElement(tInner)
          .addElement(BRLF)
          .addElement(footerBar());
    }

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
    return bar(companyLogo(),
               loggedIn ?
               makeLink(Logout.url(), Logout.name()):
               makeLink(Login.url(), ""), null);
  }

  // +++ get these next three (and others in this file) from configs ...
  public static final String contactURL = "http://www.paymate.net/contact_us!.htm";
  public static final String contactName = "Contact Us";
  public static final String copyrightURL = "http://www.paymate.net/copyright.htm";
  public static final String HomepageURL = "http://www.paymate.net/";
  public static final String ACCOUNTADMINISTRATION = "Account Administration";

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

  public /*protected*/ Element makeLink(String page, String contents) {
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
    return makeLink(HomepageURL, COMPANY);
  }

  protected Element companyLogo() {
    ElementContainer ec = new ElementContainer();
    ec.addElement(companyURL())
      .addElement(" "+ACCOUNTADMINISTRATION)
      .addElement(new StringElement(StringX.NonTrivial(loginInfo)?(" - " + loginInfo):""));
    return ec;
  }

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
    if(StringX.NonTrivial(fontSize)) {
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

  protected static final Element input(String inputType, String fieldName, String defaultValue, int maxlength, int size) {
    defaultValue = StringX.TrivialDefault(defaultValue, "");
    fieldName = StringX.TrivialDefault(fieldName, "");
    inputType = StringX.TrivialDefault(inputType, "");
    Input i = new Input(inputType, fieldName, defaultValue);
    if(maxlength > 0) {
      i.setMaxlength(maxlength);
    }
    if(size > 0) {
      i.setSize(size);
    }
    return i;
  }
  protected static final Element input(String inputType, String fieldName, String defaultValue) {
    defaultValue = StringX.TrivialDefault(defaultValue, "");
    fieldName = StringX.TrivialDefault(fieldName, "");
    inputType = StringX.TrivialDefault(inputType, "");
    return input(inputType, fieldName, defaultValue, -1, -1);
  }
  protected static final TR rowPrompt(String prompt1, String inputType,
                                      String fieldName, String defaultValue,
                                      String prompt2, String inputType2,
                                      String fieldName2, String defaultValue2) {
    // deal with nulls
    Element field1 = input(inputType, fieldName, defaultValue);
    boolean exists2 = (inputType2!=null) && (fieldName2!=null) && (defaultValue2!=null);
    Element field2 = exists2 ? input(inputType2, fieldName2, defaultValue2) : null;
    return rowPrompt(prompt1, field1, prompt2, field2);
  }
  protected static final TR rowPrompt(String prompt1, Element field1, String prompt2, Element field2) {
    // deal with nulls
    prompt1 = StringX.TrivialDefault(prompt1, "");
    prompt2 = StringX.TrivialDefault(prompt2, " ");
    // build the row
    TD td1 = new TD(prompt1);
    TD td2 = new TD(field1);
    TR tr  = new TR(td1);
    tr.addElement(td2);
    if(field2 != null) {
      TD td3 = new TD(prompt2);
      TD td4 = new TD(field2);
      tr.addElement(td3);
      tr.addElement(td4);
    }
    return tr;
  }
  public static final int SIZEANDLENGTH=2; // for date stuff // +++ put with the date and time stuff

  // form stuff
  private static final Counter FormNameCounter = new Counter();
  private static final String FormStub = "PMNF"+new BaseConverter().dtoa(UTC.Now().getTime(),0,true,36);
  public static final Form NewPostForm(String action) {
    return new Form(action).setName(FormStub + FormNameCounter.incr()).setMethod(Form.POST);
  }

}

