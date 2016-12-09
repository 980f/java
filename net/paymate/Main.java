/**
* Title:        Main
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Main.java,v 1.45 2001/11/14 01:47:48 andyh Exp $
* @todo add temp directory redirector: File tempFile(name);
*/
package net.paymate;
import net.paymate.util.*;
import net.paymate.connection.*;
import java.io.*;
import java.util.*;

public class Main {
  private static final ErrorLogStream dbg=new ErrorLogStream(Main.class.getName());

  public File Home;
  public File Logcontrol;
  public File Logger;
  public File Properties;

  //add a Properties from command line args. execute inside stdStart
  protected EasyCursor stdProperties;
  protected TextList freeArgs=new TextList(); //args that are not in property syntax

  public static final String logcontrolName="logcontrol.properties";
  public static Main Application=null;

  static final Vector /*AtExit*/ exiters=new Vector();

  public static final void OnExit(AtExit cleaner){
    exiters.add(cleaner);
  }

  public static final void OnExit(AtExit cleaner,int priority){
//priority ignored right now, later on will do insertion sort
    exiters.add(cleaner);
  }

  private static final void atExit(){
    for(int i=exiters.size();i-->0;){
      ((AtExit)exiters.elementAt(i)).AtExit();
    }
  }

  public static final EasyCursor props(){
    if(Application !=null && Application.stdProperties!=null){
      return new EasyCursor(Application.stdProperties);
    }
    return new EasyCursor(System.getProperties());
  }

  public String [] argv(){
    return freeArgs.toStringArray();
  }

  public static final String [] Argv(){
    if(Application !=null){
      return Application.freeArgs.toStringArray();
    } else {
      return new String[0];
    }
  }

  public static final EasyCursor props(String firstpush){
    EasyCursor ezc=props();
    ezc.push(firstpush);
    return ezc;
  }

  public EasyCursor makeProperties(){//probably not as useful as stdProperties field..
    EasyCursor ezp=new EasyCursor(System.getProperties());
    try {
      ezp.load(new FileInputStream(Properties));
    } catch (Exception ex){
      dbg.Caught("Main::getProp:properties load.",ex);   //it is ok to not have any properties.
    }
    return ezp;
  }

  public File localFile(String filename){
    File f=new File(filename);
    if(!f.isAbsolute()){
      f= new File(Home,filename);
    }
    return f;
  }

  public static final File LocalFile(String filename){
    return Application.localFile(filename);
  }

  public static final File LocalFile(String fileroot, String extension){
    return Application.localFile(fileroot+'.'+extension);
  }

  public static final void saveLogging(String aName){
    Tracer mark=new Tracer("saveLogging");
    if(!Safe.NonTrivial(aName)){
      aName=logcontrolName;
    }
    try {
      mark.mark("1");
      File logcontrol=new File(aName);
      mark.mark("2");
      if(!logcontrol.exists()){
        mark.mark("3");
        if(!logcontrol.isAbsolute()){
          mark.mark("4");
          logcontrol=OS.TempFile(aName);
        }
        mark.mark("6");
      }
      mark.mark("7");
      OutputStream saver=new FileOutputStream(logcontrol);
      mark.mark("8");
      LogSwitch.preloads=LogSwitch.asProperties();
      mark.mark("9");
      LogSwitch.preloads.storeSorted(saver,"debug stream controls");
      mark.mark("closing");
      saver.close();
      mark.mark("propping");
    } catch (IOException ignored){
      mark.Caught(ignored);
    }
  }

  public static final void stdExit(int exitvalue){
    saveLogging("logcontrol.improperties");//there is an IPTerminal command to do a save.
    saveProps();
    ErrorLogStream.endLogging();
    atExit();//kill everyone else that is listed to be killed
    end();//kill ourselves
    System.exit(exitvalue);
  }

  public static final void saveProps(){
    Tracer t=new Tracer("saveProps");
    try {
      t.mark("begin");
      EasyCursor combo=new EasyCursor(); //do NOT use defaulting!
      //done the following way gets ALL properties, even the system stuff
      t.mark("add app.prop");
      combo.addMore(Application.props());
      t.mark("add loggers");
      combo.addMore(LogSwitch.asProperties());
      t.mark("storesorted");
      combo.storeSorted(new FileOutputStream(OS.TempFile("combined.properties")) ,"COMBINED Main Properties");
    } catch (Exception ingored){
      //ignore errors as this method exists for debug only.
      t.Caught(ingored);
    }
  }

  public EasyCursor logStart(){
    EasyCursor fordebug=null;
    try{
      if(Logcontrol.exists()){
        LogSwitch.apply(fordebug= EasyCursor.New(new FileInputStream(Logcontrol)));
      }//best if precedes many classes starting up.
    } catch (IOException ignored){
      //yep, ignored. this is just debug control after all...
    }
    return fordebug;
  }

  public void stdStart(String argv[]){//will be prettier when we use cmdLine for arg processing
    logStart();
    stdProperties=makeProperties(); //starting with Java's properties
    int rawargi=0;
    for(int i=0;i<argv.length; ++i){ //we add in command line args of our own format
      int cut=argv[i].indexOf(':');//coz windows absorbs the more obvious '='
      if(cut>=0){
        stdProperties.setString(argv[i].substring(0,cut),argv[i].substring(cut+1));
        // +_+ fix the above to toelrate whitespace between lable and value.
      } else { //add in "nth unlabeled value or valueless label"
        freeArgs.add(argv[i]);
      }
    }
    //    stdProperties.loadEnum("debug", LogSwitch.dump(););
    boolean bufferlogger=stdProperties.getBoolean("paymate.bufLog",false);
    ErrorLogStream.stdLogging(Logger.getAbsolutePath(),LogLevelEnum.VERBOSE,bufferlogger);
    saveProps();
  }

  private static boolean keepAlive = true;
  private static Thread toKeepAlive = null;

  public static final void keepAlive() {
    toKeepAlive = Thread.currentThread();
    while(keepAlive) {
      try {
        long stayAliveTime=Ticks.forMinutes(30);
        dbg.WARNING("Staying Alive:"+stayAliveTime);
        ThreadX.sleepFor(stayAliveTime); // whatever
      } catch (Exception e) {
        dbg.Caught(e);
      }
    }
  }

  public static final void end() {
    keepAlive = false;
    if((toKeepAlive != null) && toKeepAlive.isAlive()) {
      try {
        toKeepAlive.interrupt();
      } catch (Exception e) {
        dbg.Caught("while ending:",e);
      }
    }
  }

  public Main(Class mained) {
    String ourPath= Safe.TrivialDefault(System.getProperty("user.dir"),File.listRoots()[0].getAbsolutePath());

    Home=new File(ourPath);
    Logcontrol= localFile(logcontrolName);
    Properties= localFile(mained.getName()+".properties");
    Logger= OS.TempFile("paymate.log");

    if(Application==null){ //first class in is the application...
      Application=this;  //an arbitrary rule we will learn to love.
    }
  }

}
//$Id: Main.java,v 1.45 2001/11/14 01:47:48 andyh Exp $
