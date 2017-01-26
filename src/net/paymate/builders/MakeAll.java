/**
* Title:        MakeAll<p>
* Description:  Make all of the java files in a directory structure<p>
* Copyright:    2000, PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Id: MakeAll.java,v 1.84 2004/03/18 00:29:28 mattm Exp $
*/

package net.paymate.builders;
import  net.paymate.Revision;
import  net.paymate.util.ErrorLogStream;
import net.paymate.util.Executor;
import  net.paymate.util.TextList;
import  net.paymate.util.timer.StopWatch;
import  net.paymate.util.EasyProperties;
import  net.paymate.util.EasyCursor;

import  java.io.*;
import net.paymate.io.IOX;
import net.paymate.io.FindFiles;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

/** TODO [dependencies checking]
 * 1. Look through all .java files in all directories below src
 * 2. For each file, look for "package" and "import" statements
 * 3. For each file, check to see if .java and .class have same date/time (this is an assumption on my part). If not, mark the PACKAGE as stale.
 * 4. For each PACKAGE modified, scan all other PACKAGES that imported that package or have direct references into it and mark them as stale (recurse)
 * 5. Finally, make a list of all java files in all stale packages & call javac with that list
 * 6. The enum generator should stamp the time on a generated java file with the time of the enum file.
 * create javadocs
 * maybe create manifest (although probably not needed)
 */

public class MakeAll {
  private static final ErrorLogStream dbg     = ErrorLogStream.getForClass(MakeAll.class);

  public MakeAll(EasyProperties configs) {
    load(configs);
  }

  private EasyProperties configs = null; // save it in case we want to inspect it later

  private boolean debug = false;
  private String listFile = "";
  private String classOutputDir = "";
  private String jarPath = "";
  private String includePath = "";
  private String excludePath = "";
  private String tag = "";
  private boolean doEnums = false;
  private boolean doFileList = false;
  private boolean doCompile = false;
  private boolean doJar = false;

  private String [ ] roots = null;
  private String [ ] stale = null;

  // parameter names
  private static final String DEBUGPARAM = "debug";
  private static final String LISTFILEPARAM = "listFile";
  private static final String CLASSDIRPARAM = "classOutputDir";
  private static final String JARPATHPARAM = "jarPath";
  private static final String INCLPATHPARAM = "includePath";
  private static final String EXCLPATHPARAM = "excludePath";
  private static final String DOENUMSPARAM = "doEnums";
  private static final String DOLISTPARAM = "doFileList";
  private static final String DOCOMPILEPARAM = "doCompile";
  private static final String DOJARPARAM = "doJar";

  // default values
  private static final boolean JARDEFAULT = true;
  private static final boolean COMPILEDEFAULT = true;
  private static final boolean LISTDEFAULT = true;
  private static final boolean ENUMSDEFAULT = true;
  private static final String INCLUDEDEFAULT = "net";
  private static final String EXCLUDEDEFAULT = "net"+File.separator+"paymate"+File.separator+"ivicm"+File.separator+"svc";
  private static final String JARPATHDEFAULT = "paymate.jar";
  private static final String OUTPUTDIRDEFAULT = "jm.classes";
  private static final String LISTFILEDEFAULT = "builders" + File.separator + "all.list";
  private static final boolean DEBUGDEFAULT = false;

  private void load(EasyProperties configs) {
    this.configs = configs;

    debug = configs.getBoolean(DEBUGPARAM, DEBUGDEFAULT);
    listFile = configs.getString(LISTFILEPARAM, LISTFILEDEFAULT);
    classOutputDir = configs.getString(CLASSDIRPARAM, OUTPUTDIRDEFAULT);
    jarPath = configs.getString(JARPATHPARAM, JARPATHDEFAULT);
    // +_+ put the tag into the filename:
    tag = net.paymate.Revision.Buildid();
    if(StringX.NonTrivial(tag)) {
      // just hope that we get the case correct
      jarPath = StringX.replace(tag, ".jar", "_" + tag + ".jar");
    }
    includePath = configs.getString(INCLPATHPARAM, INCLUDEDEFAULT);
    excludePath = configs.getString(EXCLPATHPARAM, EXCLUDEDEFAULT);
    doEnums = configs.getBoolean(DOENUMSPARAM, ENUMSDEFAULT);
    doFileList = configs.getBoolean(DOLISTPARAM, LISTDEFAULT);
    doCompile = configs.getBoolean(DOCOMPILEPARAM, COMPILEDEFAULT);
    doJar = configs.getBoolean(DOJARPARAM, JARDEFAULT);

    ErrorLogStream.stdLogging("makeall", false, true);
    net.paymate.util.LogSwitch.SetAll(new net.paymate.util.LogLevelEnum(net.paymate.util.LogLevelEnum.VERBOSE));
    dbg.bare=true;
    dbg.WARNING("MakeAll Starting as Version "+Revision.Rev()+' '+Revision.Buildid() + "]...");
    dbg.WARNING("Configuration: \n"+configs);
    dbg.WARNING("Members...");
    dbg.WARNING("debug="+debug);
    dbg.WARNING("listFile="+listFile);
    dbg.WARNING("classOutputDir="+classOutputDir);
    dbg.WARNING("jarPath="+jarPath);
    dbg.WARNING("includePath="+includePath);
    dbg.WARNING("excludePath="+excludePath);
    dbg.WARNING("doEnums="+doEnums);
    dbg.WARNING("doFileList="+doFileList);
    dbg.WARNING("doCompile="+doCompile);
    dbg.WARNING("doJar="+doJar);
  }

  private final boolean makeJavaListFile() {
    PrintStream javalist;
    try {
      javalist=new PrintStream(new FileOutputStream(listFile));
    } catch (FileNotFoundException e) {
      dbg.ERROR("ERROR! File not found: '" + listFile + "'.");
      return false;
    }
    int count = 0;
    for(int rooti = roots.length; rooti-->0;) {
      TextList javas = FindFiles.FindFilesUnder(roots[rooti], ".java", true);
      javas= FindFiles.ExcludeFilesFrom(javas, stale);

      for(int i = javas.size(); i-->0;) {
        String javafilename = javas.itemAt(i);
        javalist.println(javafilename);
        if(debug) {
          dbg.VERBOSE(javafilename);
        }
      }
      count += javas.size();
    }
    javalist.close();
    dbg.WARNING( " Found " + count + " java source files" + ", listed to '" + listFile + "'.");
    return true;
  }

  /**
   *  Compile the given Java class.  Synchronized to improve our chances we won't
   *  stuff up our reassignment of System.out and .err.
   *
   * Requires the tools.jar archive from the Sun jdk.
   */
  private final boolean compile() {
    IOX.createDir(classOutputDir);
    // build the commandline
    TextList cmdwords = new TextList();
    cmdwords.add("-g"); // too hard to debug with -g:none!
    cmdwords.add("-nowarn");
    if (debug) {
      cmdwords.add("-verbose");
    }
    if (classOutputDir != null) {
      cmdwords.add("-d");
      cmdwords.add(classOutputDir);
    }
    cmdwords.add("@" + listFile);
    String cmd = cmdwords.asParagraph(" ");
    dbg.ERROR(" Compiling: " + cmd);
    boolean success = false;
    String compilerOutput = "";
    // the new way, using the java classes provided by sun for this ...
    // +++ replace system.out with a bytearrayoutputstream, and read it with a buffered line reader to make a testlist of lines
    dbg.ERROR(" Compiling.  Please wait ...");
    PrintStream oldOut = null;
    PrintStream oldErr = null;
    try {
      String [ ] args = cmdwords.toStringArray();
      com.sun.tools.javac.Main javac = new com.sun.tools.javac.Main();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintStream output = new PrintStream(baos);
      // save old System.out
      oldOut = System.out;
      oldErr = System.err;
      System.setOut(output);
      System.setErr(output);
      int stat = javac.compile(args);
      success = (stat == 0);
      dbg.ERROR(" done compiling. returned " + stat);
      String result = new String(baos.toByteArray());
      if(StringX.NonTrivial(result)) {
        dbg.ERROR(result);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      try {
        if (oldOut != null) {
          System.setOut(oldOut);
        }
      } catch (Exception e) {
        // Nothing. We're just cleaning up
      }
      try {
        if (oldErr != null) {
          System.setErr(oldErr);
        }
      } catch (Exception e) {
        // Nothing. We're just cleaning up
      }
    }
    return success;
  }

  private final boolean jar(String [ ] roots) {
    // build the manifest, if we should ...
    boolean willManif = StringX.NonTrivial(tag);
    String manif = jarPath+".manif";
    if(willManif) {
      try {
        FileWriter fw = new FileWriter(manif);
        EasyProperties p = new EasyProperties();
        p.setString("Extension-Name"          , "net.paymate");
        p.setString("Implementation-Vendor-Id", "net.paymate");
        p.setString("Implementation-Vendor"   , "Paymate.net, Inc.");
        p.setString("Implementation-Version"  , tag);
        p.setString("Specification-Title"     , "siNET Specification");
        p.setString("Specification-Version"   , "1.0");
        p.setString("Specification-Vendor"    , "Paymate.net, Inc.");
        fw.write(p.asParagraph("\n", ": "));
        fw.flush();
        fw.close();
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    }
    // build the commandline   // jar cfvm manifestextras jarpath -C jm.classes net [-C for other root packages]
    TextList cmdwords = new TextList();
    cmdwords.add("c" + (debug ? "v" : "") + (willManif ? "m" : "") + "f");
    if(willManif) {
      cmdwords.add(manif);
    }
    cmdwords.add(jarPath);
    for(int i = roots.length; i-->0;) { // +++ is this going to work with roots that are dotted?
      cmdwords.add("-C").add(classOutputDir).add(roots[i]);
    }
    dbg.ERROR(" Jarring: " + cmdwords.asParagraph(" "));
    boolean success = false;
    try {
      sun.tools.jar.Main jarrer = new sun.tools.jar.Main(System.out, System.out, jarPath);
      jarrer.run(cmdwords.toStringArray());
      success = true;
    } catch (Exception e) {
      dbg.Caught(e);
    }
    return success;
  }

  /**
  * pass null's for it to use the default values
  * The above subfunctions can be called separately
  * This function could be called within code without reference to main()
  */
  private void make() {
    boolean retval = true;
    // first, find our roots
    roots = TextList.CreateFrom(includePath).toStringArray();
    // then find the exclusion directories; packages to leave out of the build
    stale = TextList.CreateFrom(excludePath).toStringArray();
    // now handle the operations
    int numstages = 4;
    for(int i=0;i<numstages;i++){
      dbg.VERBOSE("Making stage: "+(i+1)+"/"+numstages);
      String stagename = "";
      StopWatch timer = new StopWatch(true);
      switch(i) {
        case 0:
          stagename = DOENUMSPARAM;
          if(doEnums) {
            retval = makeenum.MakeAllEnums();
          }
          break;
        case 1:
          stagename = DOLISTPARAM;
          if(doFileList) {
            retval = makeJavaListFile();
          }
          break;
        case 2:
          stagename = DOCOMPILEPARAM;
          if(doCompile) {
            retval = compile();
          }
          break;
        case 3:
          stagename = DOJARPARAM;
          if(doJar) {
            retval = jar(roots);
          }
          break;
      }
      dbg.WARNING(stagename + " took " + timer.seconds() + " seconds.");
      if(!retval) {
        dbg.ERROR("Error in "+stagename+". MakeAll aborted.");
        break;
      }
    }
    dbg.WARNING("... MakeAll DONE.");
  }

  public static final String usage =
      "USAGE: MakeAll configfile\n" +
      "\tconfigfile is the path to the MakeAll configuration file \n"+
      "\tusing -h instead will get you this help screen.\n"+
      "\t\n"+
      "\tValid configfile parameters and their defaults:\n"+
      "\t"+DEBUGPARAM+"="+DEBUGDEFAULT+"\n"+
      "\t"+LISTFILEPARAM+"="+LISTFILEDEFAULT+"\n"+
      "\t"+CLASSDIRPARAM+"="+OUTPUTDIRDEFAULT+"\n"+
      "\t"+JARPATHPARAM+"="+JARPATHDEFAULT+"\n"+
      "\t"+INCLPATHPARAM+"="+INCLUDEDEFAULT+"\n"+
      "\t"+EXCLPATHPARAM+"="+EXCLUDEDEFAULT+"\n"+
      "\t\n"+
      "\tNote that includePath and excludePath should have commas for path separators.\n"+
      "\t\tThese are the commands that you can run, in the order they will occur:\n"+
      "\t"+DOENUMSPARAM+"="+ENUMSDEFAULT+"\n"+
      "\t"+DOLISTPARAM+"="+LISTDEFAULT+"\n"+
      "\t"+DOCOMPILEPARAM+"="+COMPILEDEFAULT+"\n"+
      "\t"+DOJARPARAM+"="+JARDEFAULT+"\n";

  private static final String [ ] helps = {
      "h",
      "-h",
      "-help",
      "--help",
  };

  public static final void main(String args[]) {
    int len = args.length;
    String cmd0 = (len > 0) ? args[0] : "";
    if(!StringX.NonTrivial(cmd0) ||
       (StringX.equalStrings(cmd0, helps, /* ignorecase */true) > ObjectX.INVALIDINDEX)) {
      System.out.println(usage);
      return;
    }
    try {
//      ErrorLogStream.stdLogging("javamake.log",false,true);
      MakeAll maker = new MakeAll(EasyCursor.FromDisk(cmd0));
      maker.make();
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
    System.exit(0); // -- having a problem if the program drops out, in that the threads never end ?!?!? +++ maybe try printing threads here?
  }
}

//$Id: MakeAll.java,v 1.84 2004/03/18 00:29:28 mattm Exp $
