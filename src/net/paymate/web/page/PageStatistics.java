package net.paymate.web.page;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/PageStatistics.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import org.apache.ecs.*;
import org.apache.ecs.html.*;
import net.paymate.util.timer.*;
import java.io.*;
import net.paymate.util.*;
import net.paymate.lang.*;

public class PageStatistics extends GenericElement {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PageStatistics.class);
  private StopWatch sw = null;
  private String title = "";

  public PageStatistics(String title) {
    sw = new StopWatch();
    this.title = title;
  }

  public void output(OutputStream out) {
    try {
      out.write((title + DateX.millisToSecsPlus(sw.Stop()) + ", page length: " + FILLERFIX).getBytes());
      out.flush();
    } catch(Exception e) {
      // swallow
    }
  }

  private static final String FILLERFIX = "%PAGESTATISTICS_LENGTH%";
  private static final byte [ ] FILLERFIXB = FILLERFIX.getBytes();
  private static final int FILLERFIXLEN = FILLERFIXB.length;
  public static final void SubstituteLength(byte [ ] bytes, int length) {
    ByteArray.replace(bytes, FILLERFIXB,
                      StringX.fill(String.valueOf(length),' ',FILLERFIXLEN,true).getBytes());
  }
  public static final void SubstituteLength(byte [ ] bytes) {
    SubstituteLength(bytes, bytes.length);
  }

  // crap
  public Element addElementToRegistry(Element element) {
    return this;
  };
  public Element addElementToRegistry(String element){
    return this;
  };
  public Element removeElementFromRegistry(Element element){
    return this;
  };
  public Element removeElementFromRegistry(String element){
    return this;
  };
  public boolean registryHasElement(Element element){
    return false;
  };
  public boolean registryHasElement(String element){
    return false;
  };
}
