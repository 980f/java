package net.paymate.net;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: CrapIn.java,v 1.2 2001/07/19 01:06:51 mattm Exp $
 */

import java.util.Random;
import java.io.*;

class CrapIn extends FilterInputStream {
  Random crap;

  private CrapIn(InputStream is) {
    super(is);
  }


  public static final CrapIn New(InputStream is,Random crap){
    CrapIn newone=new CrapIn(is);
    newone.crap=crap;
    return newone;
  }

  public int read() throws IOException {
    return super.read()^crap.nextInt();
  }

  public long skip(long n) throws IOException {
    for(long i=n;i-->0;){//have to roll the key as many times as sequential reading would have
      crap.nextInt();
    }
    return super.skip(n);
  }
  /**
   * the stream is not reversible
   * callers who try to go back to a mark but don't check this function will
   * get trashed.
   */
  public boolean markSupported() {
    return false;
  }

}
//$Id: CrapIn.java,v 1.2 2001/07/19 01:06:51 mattm Exp $
