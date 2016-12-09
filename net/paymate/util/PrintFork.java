package net.paymate.util;

/**
* Title:        PrintFork
* Description:  a gated print stream, and the set of all of those.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PrintFork.java,v 1.28 2001/10/10 19:46:04 mattm Exp $
*
* NOTE that (auto)registration is independent of being enabled.
*
*/

import java.io.PrintStream;
import java.io.OutputStream;
import net.paymate.util.ErrorLogStream;
import java.util.Vector;

public class PrintFork {
  protected static int DEFAULT_LEVEL = LogLevelEnum.VERBOSE;//set for server, which has a harder time configuring than the client

  protected PrintStream ps;
  public LogSwitch myLevel;
  protected boolean registered = false;

  /////////////////////////////////
  // registried class
  protected static final Vector registry = new Vector(10);

  public static final PrintFork Fork(int i){
    return (PrintFork)registry.elementAt(i);
  }

  protected static final boolean unFork(PrintFork pf) {
    if(pf != null) {
      registry.removeElement(pf);
    }
    return true;
  }

  // self unregister
  protected void finalize() { // +++ does this ever get called?
    unFork(this);
    // +++ report !
  }

  protected static final boolean Fork(PrintFork ps) {
    if(ps==null) { // don't do this
      //      Debug.rawMessage(LogSwitch.WARNING, "Can't print to a null fork!");
      return false;
    } else {
      registry.add(ps);
      //      Debug.rawMessage(LogSwitch.VERBOSE, "Added new Logger PrintFork");
      return true;
    }
  }

  //onConstruction:
  protected void register() {
    registered = Fork(this);
  }
  /////////////////////////////////////////////
  public static final void SetAll(LogLevelEnum lle){
    DEFAULT_LEVEL = lle.Value();
    for(int i = registry.size(); i-->0;) {
      Fork(i).myLevel.setto(DEFAULT_LEVEL);
    }
  }

  /////////////////////////////////////////////

  public PrintFork(String name, PrintStream primary, int startLevel, boolean register) {
    setPrintStream(primary);
    myLevel=new LogSwitch(PrintFork.class.getName()+":"+name,startLevel);
    if(register) {
      register();//wiht errorlogstream, logswitches have an independent registry
    }
  }

  public PrintFork(String name, PrintStream primary, int startLevel) {
    this(name, primary, startLevel, true);
  }

  public PrintFork(String name, PrintStream primary) {
    this(name, primary, LogSwitch.DEFAULT_LEVEL);
  }

  public void setPrintStream(PrintStream primary) {
    ps=primary;
  }

  public String Name() {
    return myLevel.Name();
  }
  //////////////////////////////////////////////
/**
 * this stream's gated  print
 */
  public void println(String s, int printLevel){
    if((s != null) && myLevel.passes(printLevel)) {
      ps.println(s);
    }
  }

  public void println(String s){
    println(s, myLevel.Value());
  }

/**
 *  print to all PrintStreams
 *  not synched as it is not critical if we lose a stream for a while
 */
  public static final void Println(String s, int printLevel){
    for(int i = registry.size(); i-->0;) {
      try {
        PrintFork pf = Fork(i);
        if(pf !=null) {
          pf.println(s,printLevel);
          //              pf.flush();//let creator of stream use 'autoFlush' if they care about integrity
        } else {
          //stuff into backtrace buffer?
        }
      } catch (Exception e) {
        // ignore for now
      }
    }
  }

  public static final void Println(int printLevel,String s){
    Println(s,printLevel);
  }
}
//$Id: PrintFork.java,v 1.28 2001/10/10 19:46:04 mattm Exp $
