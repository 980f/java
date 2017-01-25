package net.paymate.net;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: AnalyzeRoute.java,v 1.1 2001/05/10 01:07:29 andyh Exp $
 */

import java.net.*;

public class AnalyzeRoute {
  IPSpec target;

  /**
   * return description of last accessible component in path.
   */
  public String lastWorking(){
    //localhost
    //localgateway
    //DNS server
    //targetgateway
    //our target!
    return "";
  }

  public AnalyzeRoute(IPSpec target) {
    this.target=target;
  }

}
//$Id: AnalyzeRoute.java,v 1.1 2001/05/10 01:07:29 andyh Exp $