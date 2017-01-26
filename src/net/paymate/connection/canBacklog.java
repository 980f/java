package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/canBacklog.java,v $
 * Description: the request saves a reference to the disk file to delete when backlog succeeds
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import java.io.File;
//public
interface canBacklog {
//import java.io.File;  implements canBacklog
//implements canBacklog
//  File localFile;
  File setLocalFile(File f); //{ return localFile=f;}
  File getLocalFile();       //{ return localFile;}
}
//$Id: canBacklog.java,v 1.1 2001/06/22 06:27:51 andyh Exp $