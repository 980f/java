package net.paymate.ivicm.pinpad;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/Service.java,v $
 * Description:   Service base for encrypt100 services
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author        PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.ivicm.Base;
import net.paymate.util.*;

public class Service extends Base {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(Service.class);
  static final String VersionInfo = "encrypt100 Service (C) PayMate.net 2002 $Revision: 1.3 $";
  protected encrypt100 hardware;

  public Service(String s, encrypt100 hw){
    super(s);
    hardware = hw;
  }

}
//$Id: Service.java,v 1.3 2002/07/09 17:51:28 mattm Exp $