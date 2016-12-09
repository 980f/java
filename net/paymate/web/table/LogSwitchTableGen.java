/**
* Title:        LogSwitchTableGen
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LogSwitchTableGen.java,v 1.6 2001/10/27 07:17:29 mattm Exp $
*/
package net.paymate.web.table;
import  net.paymate.web.color.*;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.LogSwitch;
import  org.apache.ecs.*;
import  java.util.Vector;

public class LogSwitchTableGen extends ArrayTableGen {

  protected static final ErrorLogStream dbg = new ErrorLogStream(LogSwitchTableGen.class.getName());

  protected static final HeaderDef defaultHeaders[] = {
    new HeaderDef(AlignType.LEFT, "Debug or Fork"),
    new HeaderDef(AlignType.LEFT, "Log Level"),
  };

  public LogSwitchTableGen(String title, ColorScheme colors, HeaderDef headers[]){
    super(title, colors, null, headers, null, -1, null);
    Vector debuggers = LogSwitch.Sorted();
    data = new String [debuggers.size()][2];
    for(int i = 0; i < data.length; i++) {
      LogSwitch ls = (LogSwitch)debuggers.elementAt(i);
      if(ls != null) {
        data[i][0] = ls.Name();
        data[i][1] = ls.Level().Image();
      } else {
        data[i][0] = data[i][1] = "[not found]";
      }
    }
  }

  public static final Element output(String title, ColorScheme colors, HeaderDef headers[]) {
    return new LogSwitchTableGen(title, colors, headers);
  }

  public static final Element output(String title, ColorScheme colors) {
    return output(title, colors, null);
  }

  public HeaderDef[] fabricateHeaders() {
    return defaultHeaders;
  }

}

//$Id: LogSwitchTableGen.java,v 1.6 2001/10/27 07:17:29 mattm Exp $
