package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ReceiptBacklog.java,v $
 * Description:  receipts needing to go to server
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ReceiptBacklog.java,v 1.12 2002/07/09 17:51:21 mattm Exp $
 */

import net.paymate.util.*;

import java.util.Vector;
import java.io.*;

public class ReceiptBacklog extends Backlog {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(ReceiptBacklog.class);

  public boolean register(ReceiptStoreRequest rsr){
    return super.register(rsr,rsr.image('.'));//
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
//$Id: ReceiptBacklog.java,v 1.12 2002/07/09 17:51:21 mattm Exp $
