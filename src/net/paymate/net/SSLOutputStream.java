package net.paymate.net;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: SSLOutputStream.java,v 1.3 2002/07/09 17:51:30 mattm Exp $
 */

import java.io.OutputStream;
import java.io.IOException;
import net.paymate.util.ErrorLogStream;

public class SSLOutputStream extends OutputStream {
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(SSLOutputStream.class);

    protected OutputStream innerstream=null;
    public void write(int b) throws IOException {
      try {
        innerstream.write(b);
      } catch (IOException ioex){
        dbg.Caught("SSLOutputStream",ioex);
      }
    }

    public void close() throws IOException {
      try {
        innerstream.close();
      } catch (IOException ioex){
          //++++
      }
    }

    public void flush() throws IOException {
      try {
        innerstream.flush();
      } catch (IOException ioex){
          //++++
      }
    }

    public SSLOutputStream(OutputStream os){
      innerstream=os;
    }

    public void reconnect(OutputStream os){
      if(innerstream!=null){
        try {
          innerstream.close();
        } catch (IOException ioex){
          //++++
        }
      }
      innerstream=os;
    }

}
//$Id: SSLOutputStream.java,v 1.3 2002/07/09 17:51:30 mattm Exp $