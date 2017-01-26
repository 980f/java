package net.paymate.net;
/**
 * Title:        $Source: /cvs/src/net/paymate/net/SSLInputStream.java,v $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: SSLInputStream.java,v 1.5 2002/07/09 17:51:30 mattm Exp $
 */
import net.paymate.util.ErrorLogStream;
import java.io.*;

public class SSLInputStream extends InputStream {
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(SSLInputStream.class);

  protected InputStream innerstream;
  public final static int NOTHING = -1; //InputStream really should have a symbol like this.

  public SSLInputStream(InputStream is) {
    innerstream = is;
  }

  public int read() throws java.io.IOException {
    try {
      return innerstream.read();
    } catch(IOException ioex){
      dbg.Caught("Read",ioex);
      return NOTHING;
    }
  }

}
//$Id: SSLInputStream.java,v 1.5 2002/07/09 17:51:30 mattm Exp $