package net.paymate.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import net.paymate.lang.StringX;
import  net.paymate.util.*; // Safe

public class ChatServlet
extends HttpServlet
{
  private Object forLock = new Object();
  private Object forLock1 = new Object();
  private static Hashtable cfgs;
  private static final String VERSION = "ver. 1.30";
  private static final String CPR = "&copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;";
  private static final String DEMOSTRING = "Java server-side programming &copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;ver. 1.30";
  private static final boolean DEMO = true;
  private static final int HOW_LONG = 6;
  private static final String FRAMES = "75%,25%";
  private static final int DELTA = 10;
  private static final String ACTION = "actn";
  private static final String INIT = "init";
  private static final String INIT1 = "init1";
  private static final String INIT2 = "init2";
  private static final String INIT3 = "init3";
  private static final String LOGIN = "login";
  private static final String PUT = "put";
  private static final String GET = "get";
  private static final String GET1 = "get1";
  private static final String LOGOUT = "logout";
  private static final String UPDATE = "update";
  private static final String LOG = "log";
  private static final String USERS = "list";
  private static final String DELUSER = "dlsr";
  private static final String LOGO = "logo";
  private static final String ID = "id";
  private static final String ID1 = "id1";
  private static final String ALLID = "0";
  private static final String CONFIG = "conf";
  private static final String BORDER = "border";
  private static final String VIEW = "view";
  private static final String BGCOLOR1 = "bgcolor1";
  private static final String BGCOLOR2 = "bgcolor2";
  private static final String REFRESH = "refresh";
  private static final String INACTIVITY = "inactivity";
  private static final String PRIVACY = "privacy";
  private static final String LOGFILE = "log";
  private static final String MESSAGES = "messages";
  private static final String SIZE = "size";
  private static final String FACE = "face";
  private static final String TITLE = "title";
  private static final String EDITED = "edited";
  private static final String MASTER = "master";
  private static final String ADMIN = "admin";
  private static final String ENCODING = "encoding";
  private static final String NAME = "name";
  private static final String MAIL = "mail";
  private static final String COLOR = "color";
  private static final String USER = "user";
  private static final String DEFBGCOLOR = "#FFFFFF";
  private static final String DEFBORDER = "1";
  private static final String DEFCOLOR = "#000000";
  private static final String DEFREFRESH = "30";
  private static final String DEFUSER = "noname";
  private static final long DEFINACTIVITY = 600L;
  private static final String DEFPRIVACY = "0";
  private static final long DEFMESSAGES = 60L;
  private static final String DEFVIEW = "1";
  private static final String DEFLOGOUT = "1";
  private static final String DEFTITLE = "Coldjava chat";
  private static final String DEFENCODING = "ISO-8859-1";
  private static final String DEFLOGIN = "_top";
  private static String NEWLINE = "\n";

  public void init(ServletConfig servletconfig_1_1_)
  throws ServletException {
    super.init(servletconfig_1_1_);
    NEWLINE = System.getProperty("line.separator");
    cfgs = new Hashtable();
  }

  public void doPost(HttpServletRequest httpservletrequest_1_3_, HttpServletResponse httpservletresponse_2_4_)
  throws ServletException, IOException {
    if (httpservletrequest_1_3_.getContentLength() > 15360) {
      httpservletresponse_2_4_.setContentType("text/html");
      ServletOutputStream servletoutputstream_3_5_ = httpservletresponse_2_4_.getOutputStream();
      servletoutputstream_3_5_.println("<html><head><title>Too big</title></head>");
      servletoutputstream_3_5_.println("<body><h1>Error - content length &gt;15k not ");
      servletoutputstream_3_5_.println("</h1></body></html>");
    } else
    doGet(httpservletrequest_1_3_, httpservletresponse_2_4_);
  }

  public void doGet(HttpServletRequest httpservletrequest_1_7_, HttpServletResponse httpservletresponse_2_8_)
  throws ServletException, IOException {
    String string_3_9_ = "";
    String string_4_10_ = "";
    String string_5_11_ = "";
    String string_6_12_ = "";
    String string_11_13_ = "";
    string_3_9_ = String.valueOf(HttpUtils.getRequestURL(httpservletrequest_1_7_));
    int i_12_15_;
    if ((i_12_15_ = string_3_9_.indexOf("?")) > 0)
    string_3_9_ = string_3_9_.substring(0, i_12_15_);
    if ((string_4_10_ = httpservletrequest_1_7_.getQueryString()) == null)
    string_4_10_ = "";
    if (httpservletrequest_1_7_.getHeader("Accept").indexOf("wap.wml") >= 0)
    wmlChat(string_3_9_, string_4_10_, httpservletrequest_1_7_, httpservletresponse_2_8_);
    else {
      httpservletresponse_2_8_.setContentType("text/html");
      PrintWriter printwriter_18_17_ = httpservletresponse_2_8_.getWriter();
      printwriter_18_17_.println("<html>");
      if (string_4_10_.length() == 0) {
        printwriter_18_17_.println("<br>Can not read config file");
        printwriter_18_17_.println("</html>");
        printwriter_18_17_.flush();
        printwriter_18_17_.close();
      } else {
        string_6_12_ = getFromQuery(string_4_10_, "conf=");
        if (string_6_12_.length() == 0)
        string_6_12_ = string_4_10_;
        string_11_13_ = getFromQuery(string_4_10_, "id=");
        if (string_11_13_.length() == 0)
        string_11_13_ = httpservletrequest_1_7_.getParameter("id");
        string_5_11_ = getFromQuery(string_4_10_, "actn=");
        if (string_5_11_.length() == 0) {
          if ((string_5_11_ = httpservletrequest_1_7_.getParameter("actn")) == null)
          string_5_11_ = "init";
          else if (string_5_11_.length() == 0)
          string_5_11_ = "init";
        }
        if (string_5_11_.equals("logout")) {
          Hashtable hashtable_17_21_ = getChatHash(string_6_12_);
          addMessage(hashtable_17_21_, string_6_12_, string_11_13_, "0", "<i>logout from chat ...</i>");
          deleteUser(string_6_12_, string_11_13_);
          if (((String) hashtable_17_21_.get("logout")).equals("1"))
          string_5_11_ = "init";
          else
          string_5_11_ = "init2";
        }
        if (string_5_11_.equals("init")) {
          Hashtable hashtable_17_22_ = new Hashtable();
          readConfig(string_6_12_, hashtable_17_22_);
          registerChat(string_6_12_, hashtable_17_22_);
          printwriter_18_17_.println("<head>");
          printwriter_18_17_.println("<title>" + (String) hashtable_17_22_.get("title") + "</title>");
          printwriter_18_17_.println("</head>");
          printwriter_18_17_.println("<frameset border=" + (String) hashtable_17_22_.get("border") + " rows=\"" + "75%,25%" + "\">");
          String string_7_23_ = (String) hashtable_17_22_.get("view");
          if (string_7_23_.equals("1"))
          printwriter_18_17_.println("<frame name=\"up\" src=\"" + string_3_9_ + "?" + "conf" + "=" + string_6_12_ + "&" + "actn" + "=" + "get" + "&" + "id" + "=" + getId() + "\">");
          else
          printwriter_18_17_.println("<frame name=\"up\" src=\"" + string_3_9_ + "?" + "conf" + "=" + string_6_12_ + "&" + "actn" + "=" + "init1" + "\">");
          printwriter_18_17_.println("<frame name=\"down\" src=\"" + string_3_9_ + "?" + "conf" + "=" + string_6_12_ + "&" + "actn" + "=" + "init2" + "\">");
          printwriter_18_17_.println("</frameset>");
        } else if (string_5_11_.equals("init1")) {
          Hashtable hashtable_17_24_ = getChatHash(string_6_12_);
          String string_10_25_ = getFont(hashtable_17_24_);
          String string_7_26_ = (String) hashtable_17_24_.get("bgcolor1");
          printwriter_18_17_.println("<body bgcolor=\"" + string_7_26_ + "\">");
          if (string_10_25_.length() > 0)
          printwriter_18_17_.println(string_10_25_);
          if ((string_7_26_ = (String) hashtable_17_24_.get("logo")) == null) {
            printwriter_18_17_.println("<br><br><br><br><br><center>ChatServlet&nbsp;&copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;&nbsp;ver. 1.30");
            printwriter_18_17_.println("<br><br><br><font size=+2><center>please login !</center></font>");
          } else
          printwriter_18_17_.println(getBanner(string_7_26_));
          if (string_10_25_.length() > 0)
          printwriter_18_17_.println("</font>");
          printwriter_18_17_.println("</body>");
        } else if (string_5_11_.equals("init2")) {
          Hashtable hashtable_17_28_ = getChatHash(string_6_12_);
          String string_10_29_ = getFont(hashtable_17_28_);
          String string_7_30_ = (String) hashtable_17_28_.get("bgcolor2");
          printwriter_18_17_.println("<body bgcolor=\"" + string_7_30_ + "\">");
          if (string_10_29_.length() > 0)
          printwriter_18_17_.println(string_10_29_);
          printwriter_18_17_.println("<br>");
          drawLoginScreen(hashtable_17_28_, printwriter_18_17_, string_3_9_, string_6_12_);
          printwriter_18_17_.println("<br>&copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;&nbsp;ver. 1.30");
          printwriter_18_17_.println("<script>");
          printwriter_18_17_.println("parent.frames[1].document.forms[0].name.focus();");
          printwriter_18_17_.println("function checkForm() {");
            printwriter_18_17_.println("if (document.forms[0].name.value == '') {");
              printwriter_18_17_.println("alert(\"You must enter your name\");");
            printwriter_18_17_.println("return false; }");
            printwriter_18_17_.println("document.forms[0].submit();");
            printwriter_18_17_.println("return;");
          printwriter_18_17_.println("}");
          printwriter_18_17_.println("</script>");
          if (string_10_29_.length() > 0)
          printwriter_18_17_.println("</font>");
          printwriter_18_17_.println("</body>");
        } else if (string_5_11_.equals("login")) {
          Hashtable hashtable_17_31_ = getChatHash(string_6_12_);
          String string_7_32_ = (String) hashtable_17_31_.get("border");
          printwriter_18_17_.println("<head>");
          printwriter_18_17_.println("<title>" + (String) hashtable_17_31_.get("title") + "</title>");
          printwriter_18_17_.println("</head>");
          printwriter_18_17_.println("<frameset border=" + string_7_32_ + " rows=\"" + "75%,25%" + "\">");
          registerUser(string_6_12_, hashtable_17_31_, string_11_13_, httpservletrequest_1_7_);
          string_7_32_ = (String) hashtable_17_31_.get("privacy");
          if (string_7_32_.equals("1")) {
            String string_9_34_;
            if ((string_9_34_ = httpservletrequest_1_7_.getRemoteHost()) == null)
            addMessage(hashtable_17_31_, string_6_12_, string_11_13_, "0", "<i>login from " + httpservletrequest_1_7_.getRemoteAddr() + "</i>");
            else
            addMessage(hashtable_17_31_, string_6_12_, string_11_13_, "0", "<i>login from " + string_9_34_ + "(" + httpservletrequest_1_7_.getRemoteAddr() + ")</i>");
          } else
          addMessage(hashtable_17_31_, string_6_12_, string_11_13_, "0", "<i>just login ...</i>");
          printwriter_18_17_.println("<frame name=\"up\" src=\"" + string_3_9_ + "?" + "conf" + "=" + string_6_12_ + "&" + "actn" + "=" + "get" + "&" + "id" + "=" + string_11_13_ + "\">");
          printwriter_18_17_.println("<frame name=\"down\" src=\"" + string_3_9_ + "?" + "conf" + "=" + string_6_12_ + "&" + "actn" + "=" + "init3" + "&" + "id" + "=" + string_11_13_ + "\">");
          printwriter_18_17_.println("</frameset>");
        } else if (string_5_11_.equals("init3") || string_5_11_.equals("update")) {
          Hashtable hashtable_17_35_ = getChatHash(string_6_12_);
          String string_7_36_ = (String) hashtable_17_35_.get("bgcolor2");
          String string_8_37_ = (String) hashtable_17_35_.get("privacy");
          printwriter_18_17_.println("<body bgcolor=\"" + string_7_36_ + "\">");
          printwriter_18_17_.println("<br>");
          drawMsgScreen(hashtable_17_35_, printwriter_18_17_, string_3_9_, string_6_12_, string_11_13_, string_8_37_, (String) hashtable_17_35_.get("log"));
          printwriter_18_17_.println("<script>");
          printwriter_18_17_.println("parent.frames[1].document.forms[0].name.value='';");
          printwriter_18_17_.println("parent.frames[1].document.forms[0].name.focus();");
          printwriter_18_17_.println("</script>");
          printwriter_18_17_.println("</body>");
        } else if (string_5_11_.substring(0, "get".length()).compareTo("get") == 0) {
          Hashtable hashtable_17_38_ = getChatHash(string_6_12_);
          String string_10_39_ = getFont(hashtable_17_38_);
          String string_7_40_ = (String) hashtable_17_38_.get("refresh");
          String string_8_41_;
          if (string_5_11_.equals("get1"))
          string_8_41_ = getUserName(string_6_12_, string_11_13_);
          else
          string_8_41_ = "this is a live user";
          printwriter_18_17_.println("<head>");
          if (string_8_41_.length() > 0)
          printwriter_18_17_.println("<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"" + string_7_40_ + ";URL=" + string_3_9_ + "?" + "conf" + "=" + string_6_12_ + "&" + "actn" + "=" + "get" + "&" + "id" + "=" + string_11_13_ + "\">");
          printwriter_18_17_.println("</head>");
          printwriter_18_17_.println("<body bgcolor=\"" + (String) hashtable_17_38_.get("bgcolor1") + "\">");
          if (string_10_39_.length() > 0)
          printwriter_18_17_.println(string_10_39_);
          if (string_5_11_.equals("get1")) {
            if (string_8_41_.length() == 0) {
              printwriter_18_17_.println("<br><br><br><center><b>Your session has been expired</b></center>");
              printwriter_18_17_.println("<br><center>Please <a href=\"" + string_3_9_ + "?" + string_6_12_ + "\" target=\"" + (String) hashtable_17_38_.get("login") + "\">login again !</a></center>");
              if (string_10_39_.length() > 0)
              printwriter_18_17_.println("</font>");
              printwriter_18_17_.println("</body>");
              printwriter_18_17_.println("</html>");
              printwriter_18_17_.flush();
              printwriter_18_17_.close();
              return;
            }
            printwriter_18_17_.println("<script>");
            printwriter_18_17_.println("parent.frames[1].document.forms[0].name.value='';");
            printwriter_18_17_.println("parent.frames[1].document.forms[0].name.focus();");
            printwriter_18_17_.println("</script>");
            if ((string_7_40_ = httpservletrequest_1_7_.getParameter("name")) == null)
            string_7_40_ = "";
            else
            string_7_40_ = prepareMsg(decodeString(string_7_40_, httpservletrequest_1_7_.getCharacterEncoding(), (String) hashtable_17_38_.get("encoding")));
            if (string_7_40_.length() > 0)
            addMessage(hashtable_17_38_, string_6_12_, string_11_13_, httpservletrequest_1_7_.getParameter("user"), string_7_40_);
          }
          string_7_40_ = (String) hashtable_17_38_.get("messages");
          long l_13_45_ = StringX.parseLong(string_7_40_);
          Vector vector_15_46_ = getMsgsVector(string_6_12_);
          synchronized (hashtable_17_38_.get("messages")) {
            for (i_12_15_ = 0; i_12_15_ < vector_15_46_.size() && (long) i_12_15_ < l_13_45_; i_12_15_++) {
              Vector vector_16_48_ = (Vector) vector_15_46_.elementAt(i_12_15_);
              string_7_40_ = (String) vector_16_48_.elementAt(2);
              if (string_7_40_.equals("0") || string_7_40_.equals(string_11_13_) || string_11_13_.compareTo((String) vector_16_48_.elementAt(1)) == 0)
              printwriter_18_17_.println("<br>" + (String) vector_16_48_.elementAt(3));
              if (i_12_15_ > 0 && i_12_15_ % 7 == 0)
              printwriter_18_17_.println("<br>Java server-side programming &copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;ver. 1.30");
            }
            if ((long) vector_15_46_.size() > l_13_45_ + 10L)
            clearMessages(vector_15_46_, l_13_45_, hashtable_17_38_);
          }
          if (string_10_39_.length() > 0)
          printwriter_18_17_.println("</font>");
          printwriter_18_17_.println("</body>");
        } else if (string_5_11_.equals("log")) {
          Hashtable hashtable_17_50_ = getChatHash(string_6_12_);
          ShowLogFile(printwriter_18_17_, hashtable_17_50_);
        } else if (string_5_11_.equals("list")) {
          Hashtable hashtable_17_51_ = getChatHash(string_6_12_);
          ShowUsersTable(string_3_9_, hashtable_17_51_, string_6_12_, printwriter_18_17_, getFromQuery(string_4_10_, "privacy="), string_11_13_);
        } else if (string_5_11_.equals("dlsr")) {
          deleteUser(string_6_12_, getFromQuery(string_4_10_, "id1="));
          Hashtable hashtable_17_52_ = getChatHash(string_6_12_);
          ShowUsersTable(string_3_9_, hashtable_17_52_, string_6_12_, printwriter_18_17_, getFromQuery(string_4_10_, "privacy="), string_11_13_);
        }
        printwriter_18_17_.println("</html>");
        printwriter_18_17_.flush();
        printwriter_18_17_.close();
      }
    }
  }

  private void wmlChat(String string_1_54_, String string_2_55_, HttpServletRequest httpservletrequest_3_56_, HttpServletResponse httpservletresponse_4_57_)
  throws IOException {
    httpservletresponse_4_57_.setContentType("text/vnd.wap.wml");
    PrintWriter printwriter_5_58_ = httpservletresponse_4_57_.getWriter();
    printwriter_5_58_.println("<?xml version=\"1.0\"?>");
    printwriter_5_58_.println("<!DOCTYPE wml PUBLIC \"-//PHONE.COM//DTD WML 1.1//EN\" \"http://www.phone.com/dtd/wml11.dtd\">");
    printwriter_5_58_.println("<wml>");
    printwriter_5_58_.println("<head>");
    printwriter_5_58_.println("<meta http-equiv=\"Cache-Control\" content=\"max-age=0\" forua=\"true\"/>");
    printwriter_5_58_.println("</head>");
    if (string_2_55_.length() == 0) {
      printwriter_5_58_.println("<card>");
      printwriter_5_58_.println("<p>");
      printwriter_5_58_.println("Can not open configuration file");
      printwriter_5_58_.println("</p>");
      printwriter_5_58_.println("</card>");
    } else {
      String string_6_59_ = getFromQuery(string_2_55_, "conf=");
      if (string_6_59_.length() == 0)
      string_6_59_ = string_2_55_;
      String string_7_60_ = getFromQuery(string_2_55_, "id=");
      if (string_7_60_.length() == 0)
      string_7_60_ = httpservletrequest_3_56_.getParameter("id");
      String string_8_61_ = getFromQuery(string_2_55_, "actn=");
      if (string_8_61_.length() == 0) {
        if ((string_8_61_ = httpservletrequest_3_56_.getParameter("actn")) == null)
        string_8_61_ = "init";
        else if (string_8_61_.length() == 0)
        string_8_61_ = "init";
      }
      if (string_8_61_.equals("init"))
      printwriter_5_58_.println(initWml(string_1_54_, string_6_59_));
      else if (string_8_61_.equals("login"))
      printwriter_5_58_.println(loginWml(string_1_54_, string_6_59_, string_7_60_, httpservletrequest_3_56_));
      else if (string_8_61_.equals("init1"))
      printwriter_5_58_.println(sendWml(string_1_54_, string_6_59_, string_7_60_));
      else if (string_8_61_.equals("init2"))
      printwriter_5_58_.println(acceptWml(string_1_54_, string_6_59_, string_7_60_, httpservletrequest_3_56_));
      else if (string_8_61_.equals("logout"))
      printwriter_5_58_.println(logoutWml(string_1_54_, string_6_59_, string_7_60_));
      else if (string_8_61_.equals("get"))
      printwriter_5_58_.println(readWml(string_1_54_, string_6_59_, string_7_60_));
    }
    printwriter_5_58_.println("</wml>");
    printwriter_5_58_.flush();
    printwriter_5_58_.close();
  }

  private String initWml(String string_1_63_, String string_2_64_) {
    Hashtable hashtable_3_65_ = new Hashtable();
    StringBuffer stringbuffer_4_66_ = new StringBuffer("");
    readConfig(string_2_64_, hashtable_3_65_);
    registerChat(string_2_64_, hashtable_3_65_);
    stringbuffer_4_66_.append("<card id=\"user\">" + NEWLINE);
    stringbuffer_4_66_.append("<do type=\"accept\" label=\"email\">" + NEWLINE);
    stringbuffer_4_66_.append("<go href=\"#email\"/>" + NEWLINE);
    stringbuffer_4_66_.append("</do>" + NEWLINE);
    stringbuffer_4_66_.append("<p>" + (String) hashtable_3_65_.get("title") + "</p>" + NEWLINE);
    stringbuffer_4_66_.append("<p>User: <input name=\"sUser\" emptyok=\"false\" /></p>" + NEWLINE);
    stringbuffer_4_66_.append("</card>" + NEWLINE);
    stringbuffer_4_66_.append("<card id=\"email\">" + NEWLINE);
    stringbuffer_4_66_.append("<do type=\"accept\" label=\"Login\">" + NEWLINE);
    stringbuffer_4_66_.append("<go href=\"" + string_1_63_ + "?" + "conf" + "=" + string_2_64_ + "\" method=\"post\">" + NEWLINE);
    stringbuffer_4_66_.append("<postfield name=\"name\" value=\"$(sUser)\"/>" + NEWLINE);
    stringbuffer_4_66_.append("<postfield name=\"mail\" value=\"$(sEmail)\"/>" + NEWLINE);
    stringbuffer_4_66_.append("<postfield name=\"actn\" value=\"login\"/>" + NEWLINE);
    stringbuffer_4_66_.append("<postfield name=\"id\" value=\"" + getId() + "\"/>" + NEWLINE);
    stringbuffer_4_66_.append("</go>" + NEWLINE);
    stringbuffer_4_66_.append("</do>" + NEWLINE);
    stringbuffer_4_66_.append("<p>Email: <input name=\"sEmail\" emptyok=\"true\"/></p>" + NEWLINE);
    stringbuffer_4_66_.append("</card>" + NEWLINE);
    return String.valueOf(stringbuffer_4_66_);
  }

  private String loginWml(String string_1_68_, String string_2_69_, String string_3_70_, HttpServletRequest httpservletrequest_4_71_) {
    Hashtable hashtable_5_72_ = getChatHash(string_2_69_);
    StringBuffer stringbuffer_6_73_ = new StringBuffer("");
    registerUser(string_2_69_, hashtable_5_72_, string_3_70_, httpservletrequest_4_71_);
    String string_7_74_ = (String) hashtable_5_72_.get("privacy");
    if (string_7_74_.equals("1")) {
      String string_8_75_;
      if ((string_8_75_ = httpservletrequest_4_71_.getRemoteHost()) == null)
      addMessage(hashtable_5_72_, string_2_69_, string_3_70_, "0", "<i>login from " + httpservletrequest_4_71_.getRemoteAddr() + "</i>");
      else
      addMessage(hashtable_5_72_, string_2_69_, string_3_70_, "0", "<i>login from " + string_8_75_ + "(" + httpservletrequest_4_71_.getRemoteAddr() + ")</i>");
    } else
    addMessage(hashtable_5_72_, string_2_69_, string_3_70_, "0", "<i>just login ...</i>");
    stringbuffer_6_73_.append(mainWml(string_1_68_, string_2_69_, string_3_70_, hashtable_5_72_));
    return String.valueOf(stringbuffer_6_73_);
  }

  private String sendWml(String string_1_77_, String string_2_78_, String string_3_79_) {
    StringBuffer stringbuffer_4_80_ = new StringBuffer("");
    stringbuffer_4_80_.append("<card id=\"message\">" + NEWLINE);
    stringbuffer_4_80_.append("<do type=\"accept\" label=\"Send\">" + NEWLINE);
    stringbuffer_4_80_.append("<go href=\"" + string_1_77_ + "?" + "conf" + "=" + string_2_78_ + "\" method=\"post\">" + NEWLINE);
    stringbuffer_4_80_.append("<postfield name=\"name\" value=\"$(sName)\"/>" + NEWLINE);
    stringbuffer_4_80_.append("<postfield name=\"actn\" value=\"init2\"/>" + NEWLINE);
    stringbuffer_4_80_.append("<postfield name=\"id\" value=\"" + string_3_79_ + "\"/>" + NEWLINE);
    stringbuffer_4_80_.append("</go>" + NEWLINE);
    stringbuffer_4_80_.append("</do>" + NEWLINE);
    stringbuffer_4_80_.append("<p>Msg: <input name=\"sName\" value=\"\" emptyok=\"false\"/></p>" + NEWLINE);
    stringbuffer_4_80_.append("</card>" + NEWLINE);
    return String.valueOf(stringbuffer_4_80_);
  }

  private String acceptWml(String string_1_82_, String string_2_83_, String string_3_84_, HttpServletRequest httpservletrequest_4_85_) {
    Hashtable hashtable_5_86_ = getChatHash(string_2_83_);
    String string_6_87_ = getUserName(string_2_83_, string_3_84_);
    if (string_6_87_.length() == 0)
    return initWml(string_1_82_, string_2_83_);
    String string_7_88_;
    if ((string_7_88_ = httpservletrequest_4_85_.getParameter("name")) == null)
    string_7_88_ = "";
    else
    string_7_88_ = prepareMsg(decodeString(string_7_88_, httpservletrequest_4_85_.getCharacterEncoding(), (String) hashtable_5_86_.get("encoding")));
    if (string_7_88_.length() > 0)
    addMessage(hashtable_5_86_, string_2_83_, string_3_84_, "0", string_7_88_);
    return mainWml(string_1_82_, string_2_83_, string_3_84_, hashtable_5_86_);
  }

  private String readWml(String string_1_91_, String string_2_92_, String string_3_93_) {
    Hashtable hashtable_4_94_ = getChatHash(string_2_92_);
    String string_5_95_ = getUserName(string_2_92_, string_3_93_);
    if (string_5_95_.length() == 0)
    return initWml(string_1_91_, string_2_92_);
    return mainWml(string_1_91_, string_2_92_, string_3_93_, hashtable_4_94_);
  }

  private String mainWml(String string_1_97_, String string_2_98_, String string_3_99_, Hashtable hashtable_4_100_) {
    StringBuffer stringbuffer_5_101_ = new StringBuffer("");
    int i_10_102_ = 0;
    stringbuffer_5_101_.append("<card id=\"messages\" ontimer=\"" + string_1_97_ + "?" + "conf" + "=" + string_2_98_ + "&amp;" + "actn" + "=" + "get" + "&amp;" + "id" + "=" + string_3_99_ + "&amp;f=" + getId() + "\">" + NEWLINE);
    stringbuffer_5_101_.append("<timer value=\"" + 10 * Integer.parseInt((String) hashtable_4_100_.get("refresh")) + "\"/>" + NEWLINE);
    stringbuffer_5_101_.append("<do type=\"accept\" label=\"Refresh\">" + NEWLINE);
    stringbuffer_5_101_.append("<go href=\"" + string_1_97_ + "?" + "conf" + "=" + string_2_98_ + "&amp;" + "actn" + "=" + "get" + "&amp;" + "id" + "=" + string_3_99_ + "\">" + NEWLINE);
    stringbuffer_5_101_.append("</go>" + NEWLINE);
    stringbuffer_5_101_.append("</do>" + NEWLINE);
    stringbuffer_5_101_.append("<p><a href=\"" + string_1_97_ + "?" + "conf" + "=" + string_2_98_ + "&amp;" + "actn" + "=" + "init1" + "&amp;" + "id" + "=" + string_3_99_ + "\">Send</a></p>" + NEWLINE);
    Vector vector_7_103_ = getMsgsVector(string_2_98_);
    synchronized (hashtable_4_100_.get("messages")) {
      for (int i_9_104_ = 0; i_9_104_ < vector_7_103_.size() && i_10_102_ < 5; i_9_104_++) {
        Vector vector_8_105_ = (Vector) vector_7_103_.elementAt(i_9_104_);
        String string_6_106_ = (String) vector_8_105_.elementAt(2);
        if (string_6_106_.equals("0") || string_6_106_.equals(string_3_99_) || string_3_99_.compareTo((String) vector_8_105_.elementAt(1)) == 0) {
          stringbuffer_5_101_.append("<p>" + prepareWml((String) vector_8_105_.elementAt(3)) + "</p>" + NEWLINE);
          i_10_102_++;
        }
      }
    }
    stringbuffer_5_101_.append("<p><a href=\"" + string_1_97_ + "?" + "conf" + "=" + string_2_98_ + "&amp;" + "actn" + "=" + "logout" + "&amp;" + "id" + "=" + string_3_99_ + "\">Logout</a></p>" + NEWLINE);
    stringbuffer_5_101_.append("</card>" + NEWLINE);
    return String.valueOf(stringbuffer_5_101_);
  }

  private String prepareWml(String string_1_108_) {
    String string_2_109_ = "";
    int i_3_110_ = string_1_108_.indexOf("</font>");
    if (i_3_110_ < 0)
    string_2_109_ = string_1_108_;
    else
    string_2_109_ = string_1_108_.substring(0, i_3_110_);
    if ((i_3_110_ = string_2_109_.indexOf("<font")) >= 0) {
      string_2_109_ = string_2_109_.substring(i_3_110_ + 1);
      i_3_110_ = string_2_109_.indexOf(">");
      if (i_3_110_ >= 0)
      string_2_109_ = string_2_109_.substring(i_3_110_ + 1);
    }
    if ((i_3_110_ = string_2_109_.indexOf("<a href=")) > 0) {
      string_1_108_ = string_2_109_.substring(0, i_3_110_);
      i_3_110_ = string_2_109_.indexOf("\">");
      string_2_109_ = string_1_108_ + string_2_109_.substring(i_3_110_ + 2);
      i_3_110_ = string_2_109_.indexOf("</a>");
      string_1_108_ = string_2_109_.substring(0, i_3_110_);
      string_2_109_ = string_1_108_ + string_2_109_.substring(i_3_110_ + 4);
    }
    return string_2_109_;
  }

  private String logoutWml(String string_1_121_, String string_2_122_, String string_3_123_) {
    Hashtable hashtable_4_124_ = getChatHash(string_2_122_);
    addMessage(hashtable_4_124_, string_2_122_, string_3_123_, "0", "<i>logout from chat ...</i>");
    deleteUser(string_2_122_, string_3_123_);
    return initWml(string_1_121_, string_2_122_);
  }

  private String getBanner(String string_1_126_) {
    StringBuffer stringbuffer_2_127_ = new StringBuffer("");
    try {
      BufferedReader bufferedreader_4_128_ = new BufferedReader(new InputStreamReader(new FileInputStream(string_1_126_)));
      String string_3_129_;
      while ((string_3_129_ = bufferedreader_4_128_.readLine()) != null)
      stringbuffer_2_127_.append(string_3_129_ + NEWLINE);
      bufferedreader_4_128_.close();
    } catch (Exception exception_130_) {
      /* empty */
    }
    return String.valueOf(stringbuffer_2_127_);
  }

  private String getId() {
    String string_3_132_ = "";
    synchronized (forLock) {
      long l_1_134_ = DateX.utcNow();
      Random random_4_135_ = new Random();
      string_3_132_ = String.valueOf(l_1_134_);
      for (int i_5_136_ = 1; i_5_136_ <= 6; i_5_136_++)
      string_3_132_ += (int) (1.0 + 6.0 * random_4_135_.nextDouble());
    }
    return string_3_132_;
  }

  private void registerChat(String string_1_138_, Hashtable hashtable_2_139_) {
    synchronized (forLock1) {
      Vector vector_5_140_ = (Vector) cfgs.get(string_1_138_);
      if (vector_5_140_ != null) {
        /* empty */
      } else {
        vector_5_140_ = new Vector();
        vector_5_140_.addElement(hashtable_2_139_);
        vector_5_140_.addElement(new Hashtable());
        vector_5_140_.addElement(new Vector());
        cfgs.put(string_1_138_, vector_5_140_);
      }
    }
  }

  private Hashtable getChatHash(String string_1_143_) {
    Hashtable hashtable_2_144_ = null;
    Object object_3_145_ = null;
    Vector vector_3_146_;
    synchronized (forLock1) {
      vector_3_146_ = (Vector) cfgs.get(string_1_143_);
    }
    if (vector_3_146_ != null)
    hashtable_2_144_ = (Hashtable) vector_3_146_.elementAt(0);
    if (hashtable_2_144_ == null)
    hashtable_2_144_ = new Hashtable();
    return hashtable_2_144_;
  }

  private Hashtable getUsersHash(String string_1_148_) {
    Hashtable hashtable_2_149_ = null;
    Object object_3_150_ = null;
    Vector vector_3_151_;
    synchronized (forLock1) {
      vector_3_151_ = (Vector) cfgs.get(string_1_148_);
    }
    if (vector_3_151_ != null)
    hashtable_2_149_ = (Hashtable) vector_3_151_.elementAt(1);
    if (hashtable_2_149_ == null)
    hashtable_2_149_ = new Hashtable();
    return hashtable_2_149_;
  }

  private Vector getMsgsVector(String string_1_153_) {
    Vector vector_2_154_ = null;
    Object object_3_155_ = null;
    Vector vector_3_156_;
    synchronized (forLock1) {
      vector_3_156_ = (Vector) cfgs.get(string_1_153_);
    }
    if (vector_3_156_ != null)
    vector_2_154_ = (Vector) vector_3_156_.elementAt(2);
    if (vector_2_154_ == null)
    vector_2_154_ = new Vector();
    return vector_2_154_;
  }

  private void clearMessages(Vector vector_1_158_, long l_2_159_, Hashtable hashtable_4_160_) {
    Object object_5_161_ = null;
    PrintWriter printwriter_6_162_ = null;
    String string_8_163_ = (String) hashtable_4_160_.get("log");
    String string_10_164_ = "";
    synchronized (hashtable_4_160_.get("title")) {
      if (string_8_163_ != null) {
        try {
          FileWriter filewriter_5_165_ = new FileWriter(string_8_163_, true);
          printwriter_6_162_ = new PrintWriter(filewriter_5_165_);
        } catch (Exception exception_166_) {
          object_5_161_ = null;
          printwriter_6_162_ = null;
        }
      }
      while ((long) vector_1_158_.size() > l_2_159_) {
        if (printwriter_6_162_ != null) {
          Vector vector_7_168_ = (Vector) vector_1_158_.elementAt(vector_1_158_.size() - 1);
          String string_9_169_ = (String) vector_7_168_.elementAt(3);
          string_10_164_ += "<br>" + string_9_169_ + NEWLINE;
        }
        vector_1_158_.removeElementAt(vector_1_158_.size() - 1);
      }
      if (printwriter_6_162_ != null) {
        try {
          printwriter_6_162_.println(string_10_164_);
          printwriter_6_162_.flush();
          printwriter_6_162_.close();
        } catch (Exception exception_170_) {
          /* empty */
        }
      }
    }
  }

  private void deleteUser(String string_1_172_, String string_2_173_) {
    Hashtable hashtable_3_174_ = getUsersHash(string_1_172_);
    synchronized (hashtable_3_174_) {
      if (hashtable_3_174_ != null)
      hashtable_3_174_.remove(string_2_173_);
    }
  }

  private void registerUser(String string_1_176_, Hashtable hashtable_2_177_, String string_3_178_, HttpServletRequest httpservletrequest_4_179_) {
    Hashtable hashtable_5_180_ = getUsersHash(string_1_176_);
    String string_7_181_;
    if ((string_7_181_ = httpservletrequest_4_179_.getParameter("name")) == null)
    string_7_181_ = "noname";
    else {
      string_7_181_ = string_7_181_.trim();
      if (string_7_181_.length() == 0)
      string_7_181_ = "noname";
    }
    string_7_181_ = prepareMsg(decodeString(string_7_181_, httpservletrequest_4_179_.getCharacterEncoding(), (String) hashtable_2_177_.get("encoding")));
    String string_8_184_;
    if ((string_8_184_ = httpservletrequest_4_179_.getParameter("mail")) == null)
    string_8_184_ = "";
    else
    string_8_184_ = prepareMsg(string_8_184_);
    String string_9_186_;
    if ((string_9_186_ = httpservletrequest_4_179_.getParameter("color")) == null)
    string_9_186_ = "#000000";
    String string_10_187_ = httpservletrequest_4_179_.getRemoteUser();
    String string_11_188_ = httpservletrequest_4_179_.getRemoteAddr();
    String string_12_189_ = httpservletrequest_4_179_.getRemoteHost();
    synchronized (hashtable_5_180_) {
      Vector vector_6_190_ = (Vector) hashtable_5_180_.get(string_3_178_);
      if (vector_6_190_ != null) {
        /* empty */
      } else {
        vector_6_190_ = new Vector();
        vector_6_190_.addElement(string_7_181_);
        vector_6_190_.addElement(string_8_184_);
        vector_6_190_.addElement(string_9_186_);
        vector_6_190_.addElement(string_10_187_);
        vector_6_190_.addElement(string_11_188_);
        vector_6_190_.addElement(string_12_189_);
        vector_6_190_.addElement(new Date());
        hashtable_5_180_.put(string_3_178_, vector_6_190_);
      }
    }
  }

  private String getUserName(String string_1_193_, String string_2_194_) {
    Hashtable hashtable_3_195_ = getUsersHash(string_1_193_);
    Vector vector_4_196_;
    synchronized (hashtable_3_195_) {
      vector_4_196_ = (Vector) hashtable_3_195_.get(string_2_194_);
    }
    if (vector_4_196_ != null)
    return (String) vector_4_196_.elementAt(0);
    return "";
  }

  private void getUserInfo(String string_1_198_, String string_2_199_, String[] strings_3_200_) {
    Hashtable hashtable_4_201_ = getUsersHash(string_1_198_);
    Vector vector_5_202_;
    synchronized (hashtable_4_201_) {
      vector_5_202_ = (Vector) hashtable_4_201_.get(string_2_199_);
      if (vector_5_202_ != null) {
        vector_5_202_.removeElementAt(6);
        vector_5_202_.addElement(new Date());
      }
    }
    if (vector_5_202_ != null) {
      strings_3_200_[0] = (String) vector_5_202_.elementAt(0);
      strings_3_200_[1] = (String) vector_5_202_.elementAt(1);
      strings_3_200_[2] = (String) vector_5_202_.elementAt(2);
    } else {
      strings_3_200_[0] = "";
      strings_3_200_[1] = "";
      strings_3_200_[2] = "#000000";
    }
  }

  private Vector getUsers(String string_1_204_, int i_2_205_) {
    Hashtable hashtable_3_206_ = getUsersHash(string_1_204_);
    Hashtable hashtable_4_207_ = getChatHash(string_1_204_);
    Vector vector_5_208_ = new Vector();
    Date date_6_209_ = new Date();
    long l_7_210_ = date_6_209_.getTime();
    long l_9_211_ = 600L;
    String string_11_212_ = (String) hashtable_4_207_.get("inactivity");
    l_9_211_ = StringX.parseLong(string_11_212_);
    l_9_211_ *= 1000L;
    synchronized (hashtable_3_206_) {
      Enumeration enumeration_14_214_ = hashtable_3_206_.keys();
      while (enumeration_14_214_.hasMoreElements()) {
        string_11_212_ = (String) enumeration_14_214_.nextElement();
        Vector vector_15_216_ = (Vector) hashtable_3_206_.get(string_11_212_);
        if (vector_15_216_ != null) {
          date_6_209_ = (Date) vector_15_216_.elementAt(6);
          if (l_7_210_ - date_6_209_.getTime() > l_9_211_)
          hashtable_3_206_.remove(string_11_212_);
          else {
            vector_5_208_.addElement(string_11_212_);
            for (int i_16_218_ = 0; i_16_218_ < i_2_205_; i_16_218_++)
            vector_5_208_.addElement(vector_15_216_.elementAt(i_16_218_));
          }
        }
      }
    }
    return vector_5_208_;
  }

  private void ShowUsersTable(String string_1_220_, Hashtable hashtable_2_221_, String string_3_222_, PrintWriter printwriter_4_223_, String string_5_224_, String string_6_225_) {
    Vector vector_7_226_ = getUsers(string_3_222_, 7);
    StringBuffer stringbuffer_8_227_ = new StringBuffer();
    int i_9_228_ = 0;
    boolean bool_10_229_ = string_5_224_.equals("1");
    boolean bool_11_230_ = false;
    String string_12_231_ = "#FFFFFF";
    String string_13_232_ = "#CCCCCC";
    String string_14_233_ = string_13_232_;
    String string_15_234_ = (String) hashtable_2_221_.get("admin");
    String string_17_235_ = "";
    String string_18_236_ = "<td nowrap>";
    String string_19_237_ = "</td>";
    int i_20_238_ = 2;
    string_17_235_ = getFont(hashtable_2_221_);
    if (string_15_234_ != null) {
      Hashtable hashtable_21_240_ = getUsersHash(string_3_222_);
      if (hashtable_21_240_ != null) {
        Vector vector_22_241_ = (Vector) hashtable_21_240_.get(string_6_225_);
        if (vector_22_241_ != null && string_15_234_.equals((String) vector_22_241_.elementAt(0)))
        bool_11_230_ = true;
      }
    }
    if (bool_11_230_)
    i_20_238_ = 3;
    if (string_17_235_.length() > 0) {
      string_18_236_ += string_17_235_;
      string_19_237_ = "</font></td>";
    }
    stringbuffer_8_227_.append("<head>" + NEWLINE);
    stringbuffer_8_227_.append("<title>" + (String) hashtable_2_221_.get("title") + ": users</title>" + NEWLINE);
    stringbuffer_8_227_.append("</head>" + NEWLINE);
    stringbuffer_8_227_.append("<body bgcolor=\"#FFFFFF\">" + NEWLINE);
    if (string_17_235_.length() > 0)
    stringbuffer_8_227_.append(string_17_235_ + NEWLINE);
    stringbuffer_8_227_.append("<h1>Current users:</h1>" + NEWLINE);
    stringbuffer_8_227_.append("<table cols=" + (bool_10_229_ ? i_20_238_ + 2 : i_20_238_) + "border=1 width=98%>" + NEWLINE);
    stringbuffer_8_227_.append("<tr bgcolor=\"#CCCCFF\">" + NEWLINE);
    stringbuffer_8_227_.append(string_18_236_ + "User" + string_19_237_);
    stringbuffer_8_227_.append(string_18_236_ + "Mail" + string_19_237_);
    if (bool_10_229_) {
      stringbuffer_8_227_.append(string_18_236_ + "Host" + string_19_237_);
      stringbuffer_8_227_.append(string_18_236_ + "Address" + string_19_237_);
    }
    if (bool_11_230_)
    stringbuffer_8_227_.append(string_18_236_ + "&nbsp;" + string_19_237_);
    stringbuffer_8_227_.append("</tr>" + NEWLINE);
    for (/**/; i_9_228_ <= vector_7_226_.size() - 8; i_9_228_ += 8) {
      if (string_14_233_.equals(string_12_231_))
      string_14_233_ = string_13_232_;
      else
      string_14_233_ = string_12_231_;
      stringbuffer_8_227_.append("<tr bgcolor=\"" + string_14_233_ + "\">");
      stringbuffer_8_227_.append(string_18_236_ + (String) vector_7_226_.elementAt(i_9_228_ + 1) + string_19_237_);
      String string_16_242_ = (String) vector_7_226_.elementAt(i_9_228_ + 2);
      if (string_16_242_.length() == 0)
      string_16_242_ = "&nbsp;&nbsp;&nbsp;";
      else
      string_16_242_ = "<a href=\"mailto:" + string_16_242_ + "\">" + string_16_242_ + "</a>";
      stringbuffer_8_227_.append(string_18_236_ + string_16_242_ + string_19_237_);
      if (bool_10_229_) {
        stringbuffer_8_227_.append(string_18_236_ + (String) vector_7_226_.elementAt(i_9_228_ + 6) + string_19_237_);
        stringbuffer_8_227_.append(string_18_236_ + (String) vector_7_226_.elementAt(i_9_228_ + 5) + string_19_237_);
      }
      if (bool_11_230_)
      stringbuffer_8_227_.append(string_18_236_ + "<a href=\"" + string_1_220_ + "?" + "actn" + "=" + "dlsr" + "&" + "conf" + "=" + string_3_222_ + "&" + "id" + "=" + string_6_225_ + "&" + "id1" + "=" + (String) vector_7_226_.elementAt(0) + "&" + "privacy" + "=" + string_5_224_ + "\">Delete</a>" + string_19_237_);
      stringbuffer_8_227_.append("</tr>" + NEWLINE);
    }
    stringbuffer_8_227_.append("</table>" + NEWLINE);
    stringbuffer_8_227_.append("<br><br>&copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;&nbsp;ver. 1.30" + NEWLINE);
    if (string_17_235_.length() > 0)
    stringbuffer_8_227_.append("</font>" + NEWLINE);
    stringbuffer_8_227_.append("</body>" + NEWLINE);
    printwriter_4_223_.println(String.valueOf(stringbuffer_8_227_));
    Object object_8_244_ = null;
    Object object_7_245_ = null;
  }

  private void addMessage(Hashtable hashtable_1_247_, String string_2_248_, String string_3_249_, String string_4_250_, String string_5_251_) {
    Vector vector_6_252_ = getMsgsVector(string_2_248_);
    Date date_8_253_ = new Date();
    String string_9_254_ = String.valueOf(date_8_253_);
    int i_11_255_ = string_9_254_.indexOf(":");
    String[] strings_12_256_ = {
      "", "", "#000000"
    };
    if (string_4_250_ != null && string_3_249_ != null && string_5_251_ != null) {
      if (!string_3_249_.equals("0"))
      getUserInfo(string_2_248_, string_3_249_, strings_12_256_);
      if (strings_12_256_[0].length() != 0) {
        Vector vector_7_257_ = new Vector();
        vector_7_257_.addElement(new Date());
        vector_7_257_.addElement(string_3_249_);
        vector_7_257_.addElement(string_4_250_);
        if (!strings_12_256_[1].equals(""))
        strings_12_256_[0] = "<a href=\"mailto:" + strings_12_256_[1] + "\">" + strings_12_256_[0] + "</a>";
        String string_10_258_ = "<font color=\"" + strings_12_256_[2] + "\"";
        string_10_258_ += ">";
        vector_7_257_.addElement(string_10_258_ + string_9_254_.substring(i_11_255_ - 2, i_11_255_) + ":" + string_9_254_.substring(i_11_255_ + 1, i_11_255_ + 3) + (string_4_250_.equals("0") ? "&nbsp;" : "*") + "&nbsp;" + strings_12_256_[0] + "&nbsp;:&nbsp;" + string_5_251_ + "</font>");
        synchronized (hashtable_1_247_.get("messages")) {
          vector_6_252_.insertElementAt(vector_7_257_, 0);
        }
      }
    }
  }

  private void drawMsgScreen(Hashtable hashtable_1_260_, PrintWriter printwriter_2_261_, String string_3_262_, String string_4_263_, String string_5_264_, String string_6_265_, String string_7_266_) {
    String string_8_267_ = "";
    String string_9_268_ = "";
    String string_10_269_ = getUserName(string_4_263_, string_5_264_);
    Object object_12_270_ = null;
    int i_13_271_ = 0;
    boolean bool_14_272_ = false;
    string_9_268_ = (String) hashtable_1_260_.get("logout");
    if (string_9_268_.equals("1"))
    string_9_268_ = "_top";
    else
    string_9_268_ = "_self";
    string_8_267_ = getFont(hashtable_1_260_);
    if (string_8_267_.length() > 0)
    printwriter_2_261_.println(string_8_267_);
    printwriter_2_261_.println("<table witdh=100% border=0>");
    printwriter_2_261_.println("<form method=\"post\" action=\"" + string_3_262_ + "?" + "conf" + "=" + string_4_263_ + "\" target=up>");
    printwriter_2_261_.println("<tr><td nowrap align=left>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print(string_8_267_);
    printwriter_2_261_.println("&nbsp;" + string_10_269_ + ":&nbsp;");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print("</font>");
    printwriter_2_261_.println("</td><td nowrap align=right>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print(string_8_267_);
    printwriter_2_261_.println("<input type=\"TEXT\" name=\"name\" size=\"60\">");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print("</font>");
    printwriter_2_261_.println("</td><td nowrap align=right>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print(string_8_267_);
    printwriter_2_261_.println("To:&nbsp;<select name=\"user\">");
    String string_11_276_;
    if ((string_11_276_ = (String) hashtable_1_260_.get("master")) != null) {
      if (string_10_269_.equals(string_11_276_)) {
        printwriter_2_261_.println("<option value=\"0\">ALL</option>");
        bool_14_272_ = true;
      }
      for (Vector vector_12_277_ = getUsers(string_4_263_, 1); i_13_271_ <= vector_12_277_.size() - 2; i_13_271_ += 2) {
        if (string_10_269_.equals(string_11_276_) || string_10_269_.equals((String) vector_12_277_.elementAt(i_13_271_ + 1)) || string_11_276_.equals((String) vector_12_277_.elementAt(i_13_271_ + 1))) {
          printwriter_2_261_.println("<option value=\"" + (String) vector_12_277_.elementAt(i_13_271_) + "\">" + (String) vector_12_277_.elementAt(i_13_271_ + 1) + "</option>");
          bool_14_272_ = true;
        }
      }
    } else {
      printwriter_2_261_.println("<option value=\"0\">ALL</option>");
      bool_14_272_ = true;
      for (Vector vector_12_278_ = getUsers(string_4_263_, 1); i_13_271_ <= vector_12_278_.size() - 2; i_13_271_ += 2)
      printwriter_2_261_.println("<option value=\"" + (String) vector_12_278_.elementAt(i_13_271_) + "\">" + (String) vector_12_278_.elementAt(i_13_271_ + 1) + "</option>");
      object_12_270_ = null;
    }
    printwriter_2_261_.println("</select>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print("</font>");
    printwriter_2_261_.println("</td></tr>");
    printwriter_2_261_.println("<tr><td nowrap align=left>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print(string_8_267_);
    printwriter_2_261_.print("<input type=\"");
    if (!bool_14_272_)
    printwriter_2_261_.print("button");
    else
    printwriter_2_261_.print("submit");
    printwriter_2_261_.print("\" value=\"Send \"");
    if (!bool_14_272_)
    printwriter_2_261_.print(" DISABLED");
    printwriter_2_261_.println(">");
    printwriter_2_261_.println("<input type=\"hidden\" name=\"actn\" value=\"get1\">");
    printwriter_2_261_.println("<input type=\"hidden\" name=\"id\" value=\"" + string_5_264_ + "\">");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print("</font>");
    printwriter_2_261_.println("</td></form>");
    printwriter_2_261_.print("<td align=right nowrap nosave>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.println(string_8_267_);
    else
    printwriter_2_261_.println("<font size=-1>");
    printwriter_2_261_.println("[&nbsp;<a href=\"" + string_3_262_ + "?" + "conf" + "=" + string_4_263_ + "&" + "actn" + "=" + "logout" + "&" + "id" + "=" + string_5_264_ + "\" target=" + string_9_268_ + ">LOGOUT</a>&nbsp;");
    printwriter_2_261_.println("<a href=\"" + string_3_262_ + "?" + "conf" + "=" + string_4_263_ + "&" + "actn" + "=" + "update" + "&" + "id" + "=" + string_5_264_ + "\" target=down>REFRESH</a>&nbsp;");
    if (string_7_266_ != null)
    printwriter_2_261_.println("<a href=\"" + string_3_262_ + "?" + "conf" + "=" + string_4_263_ + "&" + "actn" + "=" + "log" + "&" + "id" + "=" + string_5_264_ + "\" target=_blank>LOG</a>&nbsp;");
    printwriter_2_261_.println("<a href=\"" + string_3_262_ + "?" + "conf" + "=" + string_4_263_ + "&" + "actn" + "=" + "list" + "&" + "id" + "=" + string_5_264_ + "&" + "privacy" + "=" + string_6_265_ + "\" target=_blank>USERS</a>&nbsp;");
    printwriter_2_261_.print("]</font></td>");
    printwriter_2_261_.print("<td align=right nowrap>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.print(string_8_267_);
    else
    printwriter_2_261_.print("<font size=-1>");
    printwriter_2_261_.println("&nbsp;&copy;&nbsp;<a href=mailto:coldjava@usa.net>Coldjava</a>&nbsp;&nbsp;ver. 1.30</font></td>");
    printwriter_2_261_.println("</tr></table>");
    if (string_8_267_.length() > 0)
    printwriter_2_261_.println("</font>");
    if (!bool_14_272_) {
      printwriter_2_261_.println("<script language=\"JavaScript\">");
      printwriter_2_261_.println("document.forms[0].action.value='';");
      printwriter_2_261_.println("</script>");
    }
  }

  private void drawLoginScreen(Hashtable hashtable_1_281_, PrintWriter printwriter_2_282_, String string_3_283_, String string_4_284_) {
    String string_5_285_ = getFont(hashtable_1_281_);
    String string_6_286_ = (String) hashtable_1_281_.get("bgcolor1");
    printwriter_2_282_.println("<form method=\"post\" action=\"" + string_3_283_ + "?" + "conf" + "=" + string_4_284_ + "\" target=\"" + (String) hashtable_1_281_.get("login") + "\">");
    printwriter_2_282_.println("<table border=0>");
    printwriter_2_282_.println("<tr>");
    printwriter_2_282_.print("<td nowrap>");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print(string_5_285_);
    printwriter_2_282_.print("Name:&nbsp;<input type=\"TEXT\" name=\"name\">");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print("</font>");
    printwriter_2_282_.println("</td>");
    printwriter_2_282_.print("<td nowrap>");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print(string_5_285_);
    printwriter_2_282_.print("E-mail:&nbsp;<input type=\"TEXT\" name=\"mail\">");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print("</font>");
    printwriter_2_282_.println("</td>");
    printwriter_2_282_.print("<td nowrap>");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print(string_5_285_);
    printwriter_2_282_.println("<select name=\"color\">");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#000000\" value=\"#000000\">black</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F0F8FF\" value=\"#F0F8FF\">aliceblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FAEBD7\" value=\"#FAEBD7\">antiquewhite</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#00FFFF\" value=\"#00FFFF\">aqua</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#7FFFD4\" value=\"#7FFFD4\">aquamarine</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F0FFFF\" value=\"#F0FFFF\">azure</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F5F5DC\" value=\"#F5F5DC\">beige</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFE4C4\" value=\"#FFE4C4\">bisque</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#0000FF\" value=\"#0000FF\">blue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#8A2BE2\" value=\"#8A2BE2\">blueviolet</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#A52A2A\" value=\"#A52A2A\">brown</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#DEB887\" value=\"#DEB887\">burlywood</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#5F9EA0\" value=\"#5F9EA0\">cadetblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#7FFF00\" value=\"#7FFF00\">chartreuse</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#D2691E\" value=\"#D2691E\">chocolate</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF7F50\" value=\"#FF7F50\">coral</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFF8DC\" value=\"#FFF8DC\">cornsilk</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#DC143C\" value=\"#DC143C\">crimson</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#00FFFF\" value=\"#00FFFF\">cyan</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#00008B\" value=\"#00008B\">darkblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#008B8B\" value=\"#008B8B\">darkcyan</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#A9A9A9\" value=\"#A9A9A9\">darkgray</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#006400\" value=\"#006400\">darkgreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#BDB76B\" value=\"#BDB76B\">darkkhaki</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF8C00\" value=\"#FF8C00\">darkorange</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#9932CC\" value=\"#9932CC\">darkorhid</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#8B0000\" value=\"#8B0000\">darkred</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#9400D3\" value=\"#9400D3\">darkviolet</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF1493\" value=\"#FF1493\">deeppink</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#696969\" value=\"#696969\">dimgray</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#1E90FF\" value=\"#1E90FF\">dodgerblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#B22222\" value=\"#B22222\">firebrick</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFFAF0\" value=\"#FFFAF0\">floralwhite</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#228B22\" value=\"#228B22\">forestgreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF00FF\" value=\"#FF00FF\">fuchsia</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#DCDCDC\" value=\"#DCDCDC\">gainsboro</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F8F8FF\" value=\"#F8F8FF\">ghostwhite</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFD700\" value=\"#FFD700\">gold</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#DAA520\" value=\"#DAA520\">goldenrod</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#808080\" value=\"#808080\">gray</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#008000\" value=\"#008000\">green</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#ADFF2F\" value=\"#ADFF2F\">greenyellow</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F0FFF0\" value=\"#F0FFF0\">honeydew</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF69B4\" value=\"#FF69B4\">hotpink</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#CD5C5C\" value=\"#CD5C5C\">indianred</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#4B0082\" value=\"#4B0082\">indigo</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFFFF0\" value=\"#FFFFF0\">ivory</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F0E68C\" value=\"#F0E68C\">khaki</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#7CFC00\" value=\"#7CFC00\">lavngreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#ADD8E6\" value=\"#ADD8E6\">lightblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F08080\" value=\"#F08080\">lightcoral</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#E0FFFF\" value=\"#E0FFFF\">lightcyan</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#90EE90\" value=\"#90EE90\">lightgreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#D3D3D3\" value=\"#D3D3D3\">lightgrey</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFB6C1\" value=\"#FFB6C1\">lightpink</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#00FF00\" value=\"#00FF00\">lime</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#32CD32\" value=\"#32CD32\">limegreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FAF0E6\" value=\"#FAF0E6\">linen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF00FF\" value=\"#FF00FF\">magenta</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#800000\" value=\"#800000\">maroon</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#0000CD\" value=\"#0000CD\">mediumblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F5FFFA\" value=\"#F5FFFA\">mintcream</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFE4E1\" value=\"#FFE4E1\">mistyrose</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFE4B5\" value=\"#FFE4B5\">moccasin</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#000080\" value=\"#000080\">navy</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FDF5E6\" value=\"#FDF5E6\">oldlace</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#808000\" value=\"#808000\">olive</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#6B8E23\" value=\"#6B8E23\">olivedrab</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFA500\" value=\"#FFA500\">orange</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF4500\" value=\"#FF4500\">orangered</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#DA70D6\" value=\"#DA70D6\">orchid</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#98FB98\" value=\"#98FB98\">palegreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFDAB9\" value=\"#FFDAB9\">peachpuff</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#CD853F\" value=\"#CD853F\">peru</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFC0CB\" value=\"#FFC0CB\">pink</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#DDA0DD\" value=\"#DDA0DD\">plum</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#B0E0E6\" value=\"#B0E0E6\">powderblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#800080\" value=\"#800080\">purple</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF0000\" value=\"#FF0000\">red</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#BC8F8F\" value=\"#BC8F8F\">rosybrown</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#4169E1\" value=\"#4169E1\">royalblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FA8072\" value=\"#FA8072\">salmon</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#2E8B57\" value=\"#2E8B57\">seagreen</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFF5EE\" value=\"#FFF5EE\">seashell</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#A0522D\" value=\"#A0522D\">sienna</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#C0C0C0\" value=\"#C0C0C0\">silver</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#87CEEB\" value=\"#87CEEB\">skyblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#6A5ACD\" value=\"#6A5ACD\">slateblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#708090\" value=\"#708090\">slategray</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFFAFA\" value=\"#FFFAFA\">snow</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#4682B4\" value=\"#4682B4\">steelblue</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#D2B48C\" value=\"#D2B48C\">tan</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#008080\" value=\"#008080\">teal</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#D8BFD8\" value=\"#D8BFD8\">thistle</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FF6347\" value=\"#FF6347\">tomato</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#40E0D0\" value=\"#40E0D0\">turquoise</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#EE82EE\" value=\"#EE82EE\">violet</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F5DEB3\" value=\"#F5DEB3\">wheat</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFFFFF\" value=\"#FFFFFF\">white</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#F5F5F5\" value=\"#F5F5F5\">whitesmoke</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#FFFF00\" value=\"#FFFF00\">yellow</option>");
    printwriter_2_282_.println("<option style=\"background:" + string_6_286_ + ";color:#9ACD32\" value=\"#9ACD32\">yellowgreen</option>");
    printwriter_2_282_.println("</select>");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print("</font>");
    printwriter_2_282_.println("</td>");
    printwriter_2_282_.print("<td nowrap>");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print(string_5_285_);
    printwriter_2_282_.print("<input type=\"Button\" value=\"Login\" onClick=\"checkForm();\">");
    if (string_5_285_.length() > 0)
    printwriter_2_282_.print("</font>");
    printwriter_2_282_.println("</td></tr>");
    printwriter_2_282_.println("<input type=\"hidden\" name=\"actn\" value=\"login\">");
    printwriter_2_282_.println("<input type=\"hidden\" name=\"id\" value=\"" + getId() + "\">");
    printwriter_2_282_.println("</table>");
    printwriter_2_282_.println("</form>");
  }

  private String getFont(Hashtable hashtable_1_288_) {
    String string_2_289_ = "";
    String string_3_290_ = "";
    String string_4_291_ = "";
    string_2_289_ = (String) hashtable_1_288_.get("size");
    string_3_290_ = (String) hashtable_1_288_.get("face");
    if (string_2_289_ != null || string_3_290_ != null) {
      string_4_291_ = "<font ";
      if (string_2_289_ != null)
      string_4_291_ += " size=\"" + string_2_289_ + "\"";
      if (string_3_290_ != null)
      string_4_291_ += " face=\"" + string_3_290_ + "\"";
      string_4_291_ += ">";
    }
    return string_4_291_;
  }

  private String getFromQuery(String string_1_295_, String string_2_296_) {
    if (string_1_295_ == null)
    return "";
    int i_3_297_;
    if ((i_3_297_ = string_1_295_.indexOf(string_2_296_)) < 0)
    return "";
    String string_4_298_ = string_1_295_.substring(i_3_297_ + string_2_296_.length());
    if ((i_3_297_ = string_4_298_.indexOf("&")) < 0)
    return string_4_298_;
    return string_4_298_.substring(0, i_3_297_);
  }

  private void ShowLogFile(PrintWriter printwriter_1_301_, Hashtable hashtable_2_302_) {
    String string_4_303_ = (String) hashtable_2_302_.get("log");
    if (string_4_303_ != null) {
      printwriter_1_301_.println("<head>");
      printwriter_1_301_.println("<title>Chat log</title>");
      printwriter_1_301_.println("</head>");
      synchronized (hashtable_2_302_.get("title")) {
        try {
          BufferedReader bufferedreader_3_304_ = new BufferedReader(new InputStreamReader(new FileInputStream(string_4_303_)));
          String string_5_305_;
          while ((string_5_305_ = bufferedreader_3_304_.readLine()) != null)
          printwriter_1_301_.println(string_5_305_);
          bufferedreader_3_304_.close();
        } catch (Exception exception_306_) {
          /* empty */
        }
      }
    }
  }

  private void readConfig(String string_1_308_, Hashtable hashtable_2_309_) {
    try {
      BufferedReader bufferedreader_3_310_ = new BufferedReader(new InputStreamReader(new FileInputStream(string_1_308_)));
      String string_4_311_;
      while ((string_4_311_ = bufferedreader_3_310_.readLine()) != null) {
        string_4_311_ = string_4_311_.trim();
        if (string_4_311_.length() > 0) {
          int i_5_313_ = string_4_311_.indexOf("=");
          if (i_5_313_ > 0 && i_5_313_ < string_4_311_.length() - 1 && string_4_311_.charAt(0) != '#' && !string_4_311_.startsWith("//"))
          hashtable_2_309_.put(string_4_311_.substring(0, i_5_313_).trim(), string_4_311_.substring(i_5_313_ + 1).trim());
        }
      }
      bufferedreader_3_310_.close();
      File file_6_314_ = new File(string_1_308_);
      hashtable_2_309_.put("edited", String.valueOf(file_6_314_.lastModified()));
    } catch (Exception exception_315_) {
      /* empty */
    }
    if (hashtable_2_309_.get("border") == null)
    hashtable_2_309_.put("border", "1");
    if (hashtable_2_309_.get("bgcolor1") == null)
    hashtable_2_309_.put("bgcolor1", "#FFFFFF");
    if (hashtable_2_309_.get("bgcolor2") == null)
    hashtable_2_309_.put("bgcolor2", "#FFFFFF");
    if (hashtable_2_309_.get("refresh") == null)
    hashtable_2_309_.put("refresh", "30");
    if (hashtable_2_309_.get("logout") == null)
    hashtable_2_309_.put("logout", "1");
    if (hashtable_2_309_.get("title") == null)
    hashtable_2_309_.put("title", "Coldjava chat");
    if (hashtable_2_309_.get("encoding") == null)
    hashtable_2_309_.put("encoding", "ISO-8859-1");
    if (hashtable_2_309_.get("login") == null)
    hashtable_2_309_.put("login", "_top");
    String string_4_316_;
    if ((string_4_316_ = (String) hashtable_2_309_.get("inactivity")) == null)
    hashtable_2_309_.put("inactivity", String.valueOf(600L));
    else {
      try {
        StringX.parseLong(string_4_316_);
      } catch (Exception exception_317_) {
        hashtable_2_309_.put("inactivity", String.valueOf(600L));
      }
    }
    if (hashtable_2_309_.get("privacy") == null)
    hashtable_2_309_.put("privacy", "0");
    if (hashtable_2_309_.get("view") == null)
    hashtable_2_309_.put("view", "1");
    if ((string_4_316_ = (String) hashtable_2_309_.get("messages")) == null)
    hashtable_2_309_.put("messages", String.valueOf(60L));
    else {
      try {
        StringX.parseLong(string_4_316_);
      } catch (Exception exception_319_) {
        hashtable_2_309_.put("messages", String.valueOf(60L));
      }
    }
  }

  public String getServletInfo() {
    return "A servlet that supports chatver. 1.30";
  }

  private String decodeString(String string_1_322_, String string_2_323_, String string_3_324_) {
    String string_4_325_;
    try {
      string_4_325_ = new String(string_1_322_.getBytes(string_2_323_ == null ? "ISO-8859-1" : string_2_323_), string_3_324_);
    } catch (Exception exception_326_) {
      string_4_325_ = string_1_322_;
    }
    return string_4_325_;
  }

  private String prepareMsg(String string_1_328_) {
    StringBuffer stringbuffer_2_329_ = new StringBuffer();
    if (string_1_328_.length() == 0)
    return "";
    for (int i_4_330_ = 0; i_4_330_ < string_1_328_.length(); i_4_330_++) {
      char c_3_331_ = string_1_328_.charAt(i_4_330_);
      if (c_3_331_ == '>')
      stringbuffer_2_329_.append("&gt;");
      else if (c_3_331_ == '<')
      stringbuffer_2_329_.append("&lt;");
      else if (c_3_331_ == '&')
      stringbuffer_2_329_.append("&amp;");
      else if (c_3_331_ == '\"')
      stringbuffer_2_329_.append("&quot;");
      else
      stringbuffer_2_329_.append(c_3_331_);
    }
    return String.valueOf(stringbuffer_2_329_);
  }
}
//$Id: ChatServlet.java,v 1.6 2003/07/27 05:35:14 mattm Exp $
