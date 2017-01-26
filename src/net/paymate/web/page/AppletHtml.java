package net.paymate.web.page;
import java.util.*;

/**
 * Generating HTML to launch an applet that is compatible with IE and Netscape
 * of various versions is an enormous mess. See
 * <A href="http://java.sun.com/products/plugin/1.3/docs/tags.html">
 * http://java.sun.com/products/plugin/1.3/docs/tags.html</A> for all the gory
 * details. This class generates HTML according to the spec that is, according
 * to the Sun standard, compatible across browser versions.
 * @author Chris Bitmead
 */
public class AppletHtml {
  String width = "200";
  String height = "300";
  String archive;
  String pluginWinUrl = "http://java.sun.com/products/plugin/1.3/jinstall-13-win32.cab#Version=1,3,0,0";
  String pluginUrl = "http://java.sun.com/products/plugin/1.3/plugin-install.html";
  String javaClass;
  String codeBase;
  Map args = new HashMap();

  /**
   * Add a parameter to pass to the applet
   */
  public void addParameter(String name, String value) {
    args.put(name, value);
  }

  public void setWidth(int width) {
    this.width = Integer.toString(width);
  }

  public void setHeight(int height) {
    this.height = Integer.toString(height);
  }

  public void setJavaClass(String javaClass) {
    this.javaClass = javaClass;
  }

  public void setCodeBase(String codeBase) {
    this.codeBase = codeBase;
  }

  public void setArchive(String archive) {
    this.archive = archive;
  }

  /**
   * Get the header HTML. If you have multiple applets, you only need include
   * this once.
   */
  static public String getHeader() {
    return "<!-- The following code is specified at the beginning of the <BODY> tag. -->\n"
      + "<SCRIPT LANGUAGE=\"JavaScript\"><!--\n"
      + "var _info = navigator.userAgent; var _ns = false;\n"
      + "var _ie = (_info.indexOf(\"MSIE\") > 0 && _info.indexOf(\"Win\") > 0\n"
      + "		&& _info.indexOf(\"Windows 3.1\") < 0);\n"
      + "//--></SCRIPT>\n"
      + "<COMMENT><SCRIPT LANGUAGE=\"JavaScript1.1\"><!--\n"
      + "var _ns = (navigator.appName.indexOf(\"Netscape\") >= 0\n"
      + "	   && ((_info.indexOf(\"Win\") > 0 && _info.indexOf(\"Win16\") < 0\n"
      + "	   && java.lang.System.getProperty(\"os.version\").indexOf(\"3.5\") < 0)\n"
      + "	   || _info.indexOf(\"Sun\") > 0));\n"
      + "//--></SCRIPT></COMMENT>\n";
  }

  /**
   * Get the body HTML. Include this for each applet.
   */
  public String getBody() {
    String rtn = "<SCRIPT LANGUAGE=\"JavaScript\"><!--\n"
      + "if (_ie == true) document.writeln('<OBJECT"
      + " classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\"";
    if (width != null) {
      rtn += " width=\"" + width + "\"";
    }
    if (height != null) {
      rtn += " height=\"" + height + "\"";
    }
    rtn += " align=\"baseline\""
      + " codebase=\"" + pluginUrl + "\">"
      + "<NOEMBED><XMP>');\n"
      + "else if (_ns == true) document.writeln('<EMBED"
      + " type=\"application/x-java-applet;version=1.3\" width=\"" + width + "\" height=\"" + height + "\""
      + " align=\"baseline\" code=\"" + javaClass + "\"";
    if (codeBase != null) {
      rtn += " codebase=\"" + codeBase + "\"";
    }
    if (archive != null) {
      rtn += " archive=\"" + archive + "\"";
    }
    Iterator it = args.keySet().iterator();
    while (it.hasNext()) {
      String name = (String)it.next();
      String value = (String)args.get(name);
      rtn += " " + name + "=\"" + value + "\"";
    }
    rtn += " pluginspage=\"" + pluginUrl + "\">"
      + "<NOEMBED><XMP>');\n"
      + "//--></SCRIPT>\n"
      + "<APPLET code=\"" + javaClass + "\"";
    if (codeBase != null) {
      rtn += " codebase=\"" + codeBase + "\"";
    }
    if (archive != null) {
      rtn += " archive=\"" + archive + "\"";
    }
    rtn += " align=\"baseline\"\n"
      + " width=\"" + width + "\" height=\"" + height + "\"></XMP>\n"
      + "<PARAM NAME=\"java_code\" VALUE=\"" + javaClass + "\">\n";
    if (codeBase != null) {
      rtn += "<PARAM NAME=\"java_codebase\" VALUE=\"" + codeBase + "\">\n";
    }
    if (archive != null) {
      rtn += "<PARAM NAME=\"java_archive\" VALUE=\"" + archive + "\">\n";
    }
    rtn	+= "<PARAM NAME=\"java_type\" VALUE=\"application/x-java-applet;version=1.3\">\n";
    it = args.keySet().iterator();
    while (it.hasNext()) {
      String name = (String)it.next();
      String value = (String)args.get(name);
      rtn += "<PARAM NAME=\"" + name + "\" VALUE=\"" + value + "\">\n";
    }
    rtn	+= "<PARAM NAME=\"scriptable\" VALUE=\"true\">\n"
      + "No Java 2 SDK, Standard Edition v 1.3 support for APPLET!!\n"
      + "</APPLET></NOEMBED></EMBED></OBJECT>\n";
    return rtn;
  }
  /**
   * The header + body.
   */
  public String toString() {
    return getHeader() + getBody();
  }

}