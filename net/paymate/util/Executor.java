/**
* Title:        Executor<p>
* Description:  Executes external programs as a processes<p>
* Copyright:    2000<p>
* Company:      paymate<p>
* @author       PayMate.net
* @version      $Id: Executor.java,v 1.26 2001/11/17 00:38:35 andyh Exp $
*/

package net.paymate.util;
import  java.util.Vector;
import  java.io.*;

public class Executor {
  protected static final ErrorLogStream dbg = new ErrorLogStream(Executor.class.getName());

  private void pStream(BufferedReader in, TextList msgs) { // hehe
    try {
      while(in.ready()) {
        msgs.add(in.readLine());
      }
    } catch (IOException ioe) {
      dbg.Enter("pStream");
      dbg.Caught(ioe);
      dbg.Exit();
    }
  }

    public int displayUpdateSeconds;
    public int timeoutSeconds;
    public TextList msgs; //need to use a PrintStream
    public boolean verbose;

    public Executor(//{
      int displayUpdateSeconds,
      int timeoutSeconds,
      TextList msgs,
      boolean verbose
    /*}*/
    ){
      this.displayUpdateSeconds= displayUpdateSeconds;
      this.timeoutSeconds=       timeoutSeconds;
      this.msgs=                 msgs;
      this.verbose=              verbose;
    }


  public void runFiles(String wildCommand,TextList fileset){
    String commandLine=wildCommand; //in case there are no substitutions...
    int F2= wildCommand.indexOf(2);  //^PB on my editor.
    for(int i=fileset.size();i-->0;){
      if(F2>=0){//the first of PFM's substitution functions:
        commandLine=wildCommand.substring(0,F2)+fileset.itemAt(i)+wildCommand.substring(F2+1);
      }
      int retcode=runProcess(commandLine,"run :"+fileset.itemAt(i));
      //report retcode to the nonexistent reporting stream...
    }
  }


  /**
  * +++ make this accept a character on the commandline (System.in) that will kill it without killing the whole thread/process so that errors will get printed

  * +++ divide this up some!!!!
  *
  * +++ make some of the display stuff parameterized? (in constructor?)
  *
  */

  public static final int runProcess(String commandline, String startMsg,
      int displayUpdateSeconds, int timeoutSeconds, TextList msgs) {
    return runProcess(commandline, startMsg,displayUpdateSeconds, timeoutSeconds, msgs, false);
  }

  public static final int runProcessSilently(String commandline, int timeoutSeconds, TextList msgs) {
    dbg.WARNING("runProcessSilently:"+commandline);
    return runProcess(commandline, commandline ,0, timeoutSeconds, msgs, false);
  }

  public static final int ezExec(String commandline, int timeoutSeconds) {
    dbg.WARNING("ezExec:"+commandline);
    return runProcess(commandline, "timeout:"+timeoutSeconds ,0, timeoutSeconds, null, false);
  }


  // if timeoutSeconds == 0 and updateSeconds == 0,
  // the process will not be watched at all
  // and there won't be any output to the screen until it's done
  // if displayUpdateSeconds == 0, the display will not be shown
  // if timeoutSeconds == 0, the process won't be killed; will run until done
  // for display purposes, it's best to make displayUpdateSeconds == 1
  // it's also best if they are both even, or if displayUpdateSeconds == 1

  public static final int runProcess(String commandline, String startMsg, int displayUpdateSeconds, int timeoutSeconds, TextList msgs, boolean verbose) {
    Executor legacy=new Executor(displayUpdateSeconds, timeoutSeconds, msgs, verbose);
    return legacy.runProcess(commandline, startMsg);
  }

  public int runProcess(String commandline, String startMsg){
    // these are while-running fields
    Process process = null;
    BufferedReader in;
    BufferedReader err;
    PrintStream    out;
    boolean done;
    if(commandline==null){
      return -2;
    }
//    if(startMsg==null){
//      return -3;
//    }
    if(msgs == null) {
      // create one for our local use if you won't give us one
       msgs = new TextList(100, 10);
    }
    if(startMsg!=null){
      dbg.VERBOSE( startMsg);
    }

    //    dbg.Message(new TextList("Executing: " +  commandline, 78, false).asParagraph(" ", null));
    commandline = commandline.trim();
    try {
      process = Runtime.getRuntime().exec(commandline);
    } catch (Exception ioe) {
      dbg.ERROR("Could not start process [" + commandline + "] due to exception:");
      dbg.Caught(ioe);
      // just quit
      return -1;
    }

    in  = new BufferedReader(new InputStreamReader(process.getInputStream()));
    err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    out = new PrintStream(process.getOutputStream());
    boolean kill = ( timeoutSeconds > 0);
    boolean display = ( displayUpdateSeconds > 0); // now we can muck with it
    if( displayUpdateSeconds == 0) {
       displayUpdateSeconds = 1;
    }
    int numDots = (kill ?
                  (( timeoutSeconds * 1000) / ( displayUpdateSeconds * 1000))
                  : 20);
    TextProgress progress = new TextProgress(numDots, kill); // the progress bar
    progress.indent = 2;
    if(kill) {
      progress.fill = ' ';
    }
    if(verbose) {
      display = false;
    }
    if(display) {
      progress.set(1); // displays the progress bar
    }
    done = false;
    if(( timeoutSeconds < 1) && ( displayUpdateSeconds < 1)) {
      // don't watch the process; trust that it will finish
      try {
        process.waitFor();
      } catch (InterruptedException ie) {
        Thread.interrupted(); // clears interrupted bits
        // who cares; just carry on
      }
    } else {
      int counter = numDots;
      int lastSize = 0;
      while(!done && !(kill && !(counter>0))) {
        // these have to be run regardless of whether it is verbose or not!!!! (for getmacid)
        pStream(in,  msgs);
        pStream(err,  msgs);
        if(verbose) {
          while( msgs.size() > lastSize) {
            dbg.VERBOSE( msgs.itemAt(lastSize++));
          }
        }
        out.println(""); // slap?

        ThreadX.sleepFor(Ticks.forSeconds(displayUpdateSeconds));

        if(display) {
          progress.step();
        }
        try {
          process.exitValue();  // throws if process is not complete; how I tell it isn't
            done = true;
          } catch (IllegalThreadStateException itse) { }
          counter--;
        if(!done && !kill && (counter == 0)) {
          counter = numDots;// cycle around
        }
      }
    }
    if(display) {
      progress.clear();
    }

    pStream(in,  msgs);  // these must be used always for getmacid
    pStream(err,  msgs);

    if(!done) {
      process.destroy();
      dbg.ERROR(kill ? "Process took too long and was terminated!" : "User killed process.");
    }

    int x = -1;
    try {
      x = done ? process.exitValue() : -1;
    } catch (IllegalThreadStateException itse2) {
      // who cares
    }
    if(x!=0) {
      dbg.WARNING("Process exited with value " + x);
    }
    return x;
  }

}
//$Id: Executor.java,v 1.26 2001/11/17 00:38:35 andyh Exp $
