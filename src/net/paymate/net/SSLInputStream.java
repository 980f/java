package net.paymate.net;
/**
 * Title:        $Source: /cvs/src/net/paymate/net/SSLInputStream.java,v $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: SSLInputStream.java,v 1.3 2001/06/02 13:33:10 andyh Exp $
 */
import net.paymate.util.ErrorLogStream;
import java.io.*;

public class SSLInputStream extends InputStream {
  protected static final ErrorLogStream dbg=new ErrorLogStream(SSLInputStream.class.getName());

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
//$Id: SSLInputStream.java,v 1.3 2001/06/02 13:33:10 andyh Exp $