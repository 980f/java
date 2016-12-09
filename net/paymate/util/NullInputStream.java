/**
 * Title:        $Source: /cvs/src/net/paymate/util/NullInputStream.java,v $
 * Description:  The always at end stream
 * Copyright:    2000, PayMate.net
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Revision: 1.2 $
 */
package net.paymate.util;
import  java.io.IOException;
import  java.io.InputStream;

// +_+ need a net.paymate.io!

public class NullInputStream extends InputStream {

  public NullInputStream() {
    super();
  }

  public int read() throws IOException {
    return -1;
  }

}
//$Source: /cvs/src/net/paymate/util/NullInputStream.java,v $