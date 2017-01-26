/* $Id: LineDisplayService.java,v 1.11 2002/07/09 17:51:26 mattm Exp $ */
package net.paymate.ivicm.ec3K;
import net.paymate.util.*;

import net.paymate.ivicm.comm.*;
import java.io.PrintStream;

//import javax.comm.CommPortIdentifier;

public class LineDisplayService extends Service {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(LineDisplayService.class);

  static final String VersionString="CM3000 display, (C) PayMate.net 2000 $Revision: 1.11 $";

  protected String lastSent;
  public LineDisplayService(String s,EC3K hw) {
    super(s,hw);
//    identifiers(VersionString,Version1dot4,"EC3K LineDisplay");
    dbg.rawMessage(dbg.VERBOSE,"Creating LDS as "+s);
  }

  public void clearText()  {
    hardware.Show("                ");
  }

  public void displayText(String s)  {
    hardware.Show(lastSent=s);
  }

  public void refresh()  {
    hardware.Show(lastSent);
  }


  public int getColumns()  {
    return 16;
  }

}
//$Id: LineDisplayService.java,v 1.11 2002/07/09 17:51:26 mattm Exp $
