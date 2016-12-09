/**
 * Title:        UrlObjConv<p>
 * Description:  Url 2 Object and Object 2 Url converter<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: UrlObjConv.java,v 1.13 2001/07/19 01:06:52 mattm Exp $
 */

package net.paymate.net;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import net.paymate.util.ErrorLogStream;
// for the testing stuff ...
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class UrlObjConv {

  protected static final ErrorLogStream dbg=new ErrorLogStream(UrlObjConv.class.getName());

  /**
   *  NOTE: be sure to cast the result of this method to the class it actually is.
   */
  public static final Object ObjectFromUrl(String url, boolean decode) {
    Object o = null;
    if((url != null) && (url.length() > 0)) {
      try {
        dbg.Enter("ObjectFromUrl");
        String strobjagain = decode ? URLDecoder.decode(url) : url;
        ByteArrayInputStream bais = new ByteArrayInputStream(strobjagain.getBytes());
        ObjectInputStream ois = new ObjectInputStream(bais);
        o = ois.readObject();
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        dbg.Exit();
      }
    }
    return o;
  }

  public static final String ObjectToUrl(Object o) {
    String estrobj = null;
    if(o != null) {
      try {
        dbg.Enter("ObjectToUrl");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        String b4encoding = baos.toString();
        estrobj = URLEncoder.encode(b4encoding);
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        dbg.Exit();
      }
    }
    return estrobj;
  }

  // for testing
  public static String Usage() {
    return "SerializedObjectFilename";
  }

  public static void Test(String[] args) {
    try {
      dbg.Enter("Test");
      ErrorLogStream.Console(ErrorLogStream.VERBOSE);
      if(args.length < 1) {
        dbg.VERBOSE(Usage());
      } else {
        FileInputStream fis = new FileInputStream(args[0]);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object o = ois.readObject();
        // now, we have an object
        // first, convert it to a URL, then convert it back, then see if it worked
        String url = UrlObjConv.ObjectToUrl(o);
        Object p = UrlObjConv.ObjectFromUrl(url, true);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
    }
  }
}
