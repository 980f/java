/**
* Title:        MakeAll<p>
* Description:  Make all of the java files in a directory structure<p>
* Copyright:    2000, PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Id: MakeAll.java,v 1.45 2001/10/13 11:02:28 mattm Exp $
*/

package net.paymate.builders;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.FindFiles;
import  net.paymate.util.Executor;
import  net.paymate.util.TextList;
import  net.paymate.util.Safe;
import  net.paymate.util.timer.StopWatch;
import  java.io.*;

public class MakeAll{
  // eventually make this next one a passsed parameter:
  protected static boolean DEBUGGING = false; // set this when testing builder!!!!
  protected static final ErrorLogStream dbg     = new ErrorLogStream(MakeAll.class.getName());

  protected static final String libdir = ".." + File.separator + "lib" + File.separator; // +++ make this a parameter !
  // put needed libs in <jredir>/lib/ext
  protected static final String roots[] = {
    "net",
    "jpos",
  };

  protected static final String stale[] = {
    "net"+File.separator+"paymate"+File.separator+"builders",
    "net"+File.separator+"paymate"+File.separator+"ivicm"+File.separator+"svc",
  };

  protected static final String javaFileExt = ".java";
  protected static final String javaSourceFiles = " java source files";

  public static final TextList makeJavaList(){
    TextList files=new TextList();
    for(int rooti = roots.length; rooti-->0;) {
      files.appendMore(FindFiles.ExcludeFilesFrom(FindFiles.FindFilesUnder(roots[rooti], javaFileExt, true),stale));
    }
    return files;
  }


  public static final boolean makeJavaListFile(String listFileName) {
    //    dbg.Message(true, "Searching for" + javaSourceFiles + "...");
    PrintStream javalist;
    try {
      javalist=new PrintStream(new FileOutputStream(listFileName));
    } catch (FileNotFoundException e) {
      dbg.ERROR("ERROR! File not found: '" + listFileName + "'.");
      return false;
    }
    int count = 0;
    for(int rooti = roots.length; rooti-->0;) {
      TextList javas = FindFiles.FindFilesUnder(roots[rooti], javaFileExt, true);
      javas= FindFiles. ExcludeFilesFrom(javas, stale);

      for(int i = javas.size(); i-->0;) {
        String javafilename = javas.itemAt(i);
        javalist.println(javafilename);
        if(DEBUGGING) {
          dbg.VERBOSE(javafilename);
        }
      }
      count += javas.size();
    }
    javalist.close();
    dbg.WARNING( " Found " + count +
    javaSourceFiles + ", listed to '" + listFileName + "'.");
    return true;
  }

  public static final boolean compile(String listFileName, String classOutputDir) {
    Safe.createDir(classOutputDir);
    // build the commandline

    String cmd = "javac -g -deprecation "; // too hard to debug with -g:none!

    if(DEBUGGING){
      cmd+= " -verbose ";
    }
    if(classOutputDir!=null){
      cmd += " -d " + classOutputDir;
    }
    cmd += " @" + listFileName;
    if(DEBUGGING){
      dbg.VERBOSE(cmd);
    }
    TextList msgs = new TextList();
    boolean success = (Executor.runProcess(cmd, null/*"Compiling all ..."*/, 1, 0, msgs, DEBUGGING)==0);


    // +++ use the tools.jar that comes with java to compile ?!?!?!?!?
/*
    String[] cl = {
      cmd,
    };
    sun.tools.javac.Main.main(cl);
    boolean success = ...
*/

    if(!DEBUGGING) {
      String tmp = msgs.asParagraph().trim();
      if(Safe.NonTrivial(tmp) && (tmp.length() > 2)) { // +++ fixup
        dbg.ERROR(tmp);
      }
    }
    return success;
  }

  public static final boolean jar(String jarFile, String classOutputDir) {
    // build the commandline   // jar cf ../lib/paymate.jar -C all.classes net -C all.classes icaframe
    TextList rootList = new TextList(roots);
    String jardirs = "";
    for(int i = roots.length; i-->0;) {
      jardirs += " -C " + classOutputDir + " " + roots[i];
    }
    String cmd = "jar c" + (DEBUGGING ? "v" : "") + "f " + jarFile + jardirs;
    String msg = "Jarring all to '" + jarFile + "'...";
    return Executor.runProcess(cmd, null/*msg*/, 1, /*DEBUGGING ? 0 : 20*/DEBUGGING ? 20 : 0, null, DEBUGGING)==0;
  }

  protected static final boolean makeManifest(String classOutputDir) {
    // +++ need to make the manifest!!!!! FINISH ME
    // it contains the package versioning stuff

    String manifestDir  = "META-INF";
    String manifestFile = "MANIFEST.INF";
    String Vendor       = "PayMate.net Corp";
    String SpecTitle    = "All.Classes";
    String SpecVersion  = "All.Classes";
    // META-INF/MANIFEST.INF ...
    // Manifest-Version: .0
    // Created-By: 1.2.2 (Sun Microsystems, Inc.) // don't really need this one
    // Specification-Title: Java Platform API Specification
    // Specification-Vendor: Sun Microsystems, Inc.
    // Specification-Version: 1.2
    // Implementation-Title: Java Runtime Environment
    // Implementation-Vendor: Sun Microsystems, Inc.
    // Implementation-Version: 1.2.2

    boolean retval = false;
    try {
      dbg.ERROR("makeManifest not yet implemented.");
      /*
      String filepath = classOutputDir + File.separator + manifestDir;
      File f = new File(filepath);
      f.mkdirs();
      // +++ first, read it in so can increment it

      // then write it out fresh (overwrite it)
      PrintWriter pw = new PrintWriter(new FileWriter(filepath  + File.separator + manifestFile));
      pw.println("Manifest-Version: .0"); // fix the version of this ????
      pw.println("Specification-Title: "); // +++
      pw.println("Specification-Vendor: "); // +++
      pw.println("Specification-Version: "); // +++
      pw.println("Implementation-Title: "); // +++
      pw.println("Implementation-Vendor: "); // +++
      pw.println("Implementation-Version: "); // +++
      */
      retval = true;
    } catch (Exception e) {
      // just catch and return false;
    }
    return retval;
  }


  public static final boolean javadoc(/* what parameters? */) {
    boolean retval = true;

    // +++ needs to go find all applicable packages and build its own packages.list
    dbg.ERROR("Javadoc not yet implemented.");

    return retval;
  }

  static final String oplist="ELCMJD";//match this to cases below! (l's are hard to see lower case)
  static final String [] opDescripts = {
    "Enumerate",
    "List",
    "Compile",
    "Manifest",
    "Jar",
    "Document"
  };

  /**
  * pass null's for it to use the default values
  * The above subfunctions can be called separately
  * This function could be called within code without reference to main()
  */
  public static final boolean make(String listFileName, String classOutputDir, String jarFile, String opt) {
    boolean retval = true;
    opt = opt.toUpperCase();
    for(int i=0;i<oplist.length();i++){
      char stage= oplist.charAt(i);
      if(opt.indexOf(stage)>=0){
        dbg.VERBOSE("Making stage: "+stage+" ["+opDescripts[i] + "]");
        StopWatch timer = new StopWatch(true);
        switch(i){
          case 0: retval = MakeJprEnums.Make(null);               break;
          case 1: retval = makeJavaListFile(listFileName);        break;
          case 2: retval = compile(listFileName, classOutputDir); break;
          case 3: retval = makeManifest(classOutputDir);          break;
          case 4: retval = jar(jarFile, classOutputDir);          break;
          case 5: retval = javadoc();                             break; // +++ what params?
        }

        dbg.WARNING(" '" + opDescripts[i] + "' took " + timer.seconds() + " seconds.");
        if(!retval) {
          dbg.ERROR( "Error in step "+stage+" [check 4th commandline arg: " + opt + "]");
          return false;
        }
      }
    }
    return retval;
  }

  /**
  * run it from src !!!!!!  (Can't figure out how to cd in java!)
  * The following have sufficient defaults to allow you to leave them out.
  * arg[0] = list filename
  * arg[1] = class output dir
  * arg[2] = jar file
  * arg[3] = flags
  */
  public static final void main(String args[]) {
    int len = args.length;

    String arg0= (len > 0) ? args[0] : "builders" + File.separator + "all.list";        // relative to "src"
    String arg1= (len > 1) ? args[1] : "all.classes";                                   // rel2src
    String arg2= (len > 2) ? args[2] : libdir + "paymate.jar";                          // rel2src
    String opts= (len > 3) ? args[3] : "elc";  //remake list, makeenums, compile

    DEBUGGING = opts.indexOf("d")>=0;
    ErrorLogStream.stdLogging("makeall", ErrorLogStream.VERBOSE, false, true);
    dbg.bare=true;
    dbg.WARNING("MakeAll Starting [" + opts + "]...");
    make(arg0,arg1,arg2,opts);//i was in a hurry and didn't feel like making good names
    dbg.WARNING("... MakeAll DONE.");
    System.exit(0); // -- having a problem if the program drops out, in that the threads never end ?!?!? +++ maybe try printing threads here?
  }

}
//$Id: MakeAll.java,v 1.45 2001/10/13 11:02:28 mattm Exp $
