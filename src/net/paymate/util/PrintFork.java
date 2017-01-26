package net.paymate.util;

/**
* Title:        PrintFork
* Description:  a gated print stream, and the set of all of those.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PrintFork.java,v 1.40 2003/10/14 02:36:17 mattm Exp $
*
* NOTE that (auto)registration is independent of being enabled.
*
*/

import java.io.PrintStream;
import java.io.OutputStream;
import net.paymate.util.ErrorLogStream;
import java.util.Vector;
import net.paymate.lang.StringX;

public class PrintFork {
  protected static int DEFAULT_LEVEL = LogLevelEnum.OFF;//set for server, which has a harder time configuring than the client

  protected PrintStream ps;
  public LogSwitch myLevel;
  protected boolean registered = false;

  public static final PrintFork Fork(int i){
    return (PrintFork)LogSwitchRegistry.printForkRegistry.elementAt(i);
  }

  protected static final boolean unFork(PrintFork pf) {
    if(pf != null) {
      LogSwitchRegistry.printForkRegistry.removeElement(pf);
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
      LogSwitchRegistry.printForkRegistry.add(ps);
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
    for(int i = LogSwitchRegistry.printForkRegistry.size(); i-->0;) {
      Fork(i).myLevel.setto(DEFAULT_LEVEL);
    }
  }

  /////////////////////////////////////////////

  protected PrintFork(String name, PrintStream primary, int startLevel, boolean register) {
    setPrintStream(primary);
    myLevel=LogSwitch.getFor(LogSwitch.shortName(PrintFork.class,name), startLevel);
    if(register) {
      register();//wiht errorlogstream, logswitches have an independent registry
    }
  }

  protected PrintFork(String name, PrintStream primary, int startLevel) {
    this(name, primary, startLevel, true);
  }

  protected PrintFork(String name, PrintStream primary) {
    this(name, primary, LogSwitch.DEFAULT_LEVEL);
  }

  public void setPrintStream(PrintStream primary) {
    ps=primary;
  }


  public static PrintFork New(String name, PrintStream primary, int startLevel, boolean register) {
    return new PrintFork(name, primary, startLevel, register);
  }

  private static PrintFork New(String name, PrintStream primary, int startLevel) {
    return new PrintFork(name, primary, startLevel, true);
  }

  public static PrintFork New(String name, PrintStream primary) {
    return new PrintFork(name, primary, DEFAULT_LEVEL);
  }

  public String Name() {
    return myLevel.Name();
  }
  //////////////////////////////////////////////
/**
 * this stream's gated  print
 */
  public void VERBOSE(String s) {
    println(s, LogLevelEnum.VERBOSE);
  }
  public void WARNING(String s) {
    println(s, LogLevelEnum.WARNING);
  }
  public void ERROR(String s) {
    println(s, LogLevelEnum.ERROR);
  }
  public void println(String s, int printLevel){
    if((s != null) && myLevel.passes(printLevel)) {
      if(StringX.lastChar(s) == ','){//allows for packing multiple outputs on one line.
        ps.print(s);
      } else {
        ps.println(s);
      }
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
    for(int i = LogSwitchRegistry.printForkRegistry.size(); i-->0;) {
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

//  public static final void Println(int printLevel,String s){
//    Println(s,printLevel);
//  }

  public static final EasyCursor asProperties(){
    EasyCursor blob=new EasyCursor(/* presize to di*/);
    Vector debuggers = LogSwitchRegistry.printForkRegistry; // get a second list copy to prevent exceptions
    for(int i=0;i<debuggers.size();i++){
      PrintFork bugger = (PrintFork)debuggers.elementAt(i);
      blob.setString(bugger.Name(), bugger.myLevel.Image());
    }
    return blob;
  }

  public static final void DUMPDEBUG() {
    System.out.println(PrintFork.asProperties().toString());
  }
}
//$Id: PrintFork.java,v 1.40 2003/10/14 02:36:17 mattm Exp $
