 /* $Id: testpos.java,v 1.73 2001/11/14 01:47:58 andyh Exp $
this class was a stub for the real program launcher. Now it is the real thing.
*/
package net.paymate.terminalClient;
import net.paymate.Main;
import net.paymate.util.*;
import net.paymate.connection.*;

import java.io.*;
import java.util.Vector;

public class testpos {
  private static final ErrorLogStream dbg=new ErrorLogStream(testpos.class.getName());
  public static Main app;
  protected static Appliance apple;

  public static final void main(String argv[]){
    try {
      dbg.Enter("main");
      app=new Main(testpos.class);
      //in case we don't have a logcontrol file:
      LogSwitch.SetAll(LogSwitch.ERROR);
      PrintFork.SetAll(LogSwitch.OFF);//output NOTHING. without IP terminal assistance
      //now get overrides from file:
      app.stdStart(argv); //starts logging etc. merges argv with system.properties and thisclass.properties
      OurForms.SetConfig(app.props());//mostly to get graphic bakgrounds
      Constants deprecatethis=new Constants("defawlt",Main.props());
      apple =new Appliance(deprecatethis);//container for the terminals
      apple.Start();//will read cached config
      dbg.ERROR("Appliance is started");
      // now, just keep the app alive (hopefully)
      Main.keepAlive();//still needed as app start doesn't stasrt quickly enough to keep us alive.
      dbg.ERROR("keepAlive quit");
    } catch (Throwable caught) {
      dbg.Caught(caught);
      //will need different codes for different exceptions.
      Main.stdExit(net.paymate.ExitCode.MainCaught);
    } finally {
      Main.stdExit(net.paymate.ExitCode.MainDied);
      dbg.Exit();
    }
  }

}
//$Id: testpos.java,v 1.73 2001/11/14 01:47:58 andyh Exp $
