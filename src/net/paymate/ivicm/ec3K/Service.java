package net.paymate.ivicm.ec3K;
/* $Id: Service.java,v 1.12 2002/07/09 17:51:26 mattm Exp $ */

import net.paymate.ivicm.*;
import net.paymate.util.*;

public class Service extends Base  {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(Service.class);
  static final String VersionInfo = "EC3K Service (C) PayMate.net 2000 $Revision: 1.12 $";
  protected EC3K hardware;

  public Service(String s, EC3K hw){
    super(s);
    hardware = hw;
  }

}
//$Id: Service.java,v 1.12 2002/07/09 17:51:26 mattm Exp $
