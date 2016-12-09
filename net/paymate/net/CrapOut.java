package net.paymate.net;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: CrapOut.java,v 1.2 2001/07/19 01:06:51 mattm Exp $
 */

import java.util.Random;
import java.io.*;


public class CrapOut extends FilterOutputStream {
  Random crap;

  private CrapOut(OutputStream os){
    super(os);
  }

  public static final OutputStream New(OutputStream os, Random crap){
    CrapOut newone=new CrapOut(os);
    newone.crap=crap;
    return newone;
  }

  public void write(int b) throws java.io.IOException {
    super.write(b^crap.nextInt());
  }

}
//$Id: CrapOut.java,v 1.2 2001/07/19 01:06:51 mattm Exp $