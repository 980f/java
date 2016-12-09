package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ReceiptBacklog.java,v $
 * Description:  receipts needing to go to server
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ReceiptBacklog.java,v 1.7 2001/11/17 00:38:34 andyh Exp $
 */

import net.paymate.util.*;

import java.util.Vector;
import java.io.*;

public class ReceiptBacklog extends Backlog {
  static final ErrorLogStream dbg=new ErrorLogStream(ReceiptBacklog.class.getName());

  public boolean register(ReceiptStoreRequest rsr){
    return super.register(rsr,rsr.tid.image('.'));
  }

  public ReceiptBacklog(File root) {
    super(new File(root,"rcp"),dbg);
  }

/**
 *  pseudoclone
 *  creates a new one with the same path, but because of different threads, contents may differ
 */
//  public
  private ReceiptBacklog Clone() {
    return new ReceiptBacklog(root.getParentFile());
  }

}
//$Id: ReceiptBacklog.java,v 1.7 2001/11/17 00:38:34 andyh Exp $
