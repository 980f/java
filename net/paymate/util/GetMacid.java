/**
 * Title:        GetMacid<p>
 * Description:  Gets the macid for win9x & linux<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: GetMacid.java,v 1.37 2001/10/30 19:37:24 mattm Exp $
 */

package net.paymate.util;
import  net.paymate.util.timer.StopWatch;
import  net.paymate.util.OS;

import  java.io.*;

public class GetMacid {
  // these will be sent to the OS, format accordingly
  protected static final String linuxCmd = "/sbin/ifconfig";// for me, eg
  protected static final String [] linuxSearchStrs = {"HWaddr",};
  protected static final String solarisCmd = "/sbin/ifconfig -a"; // for our suns
  protected static final String [] solarisSearchStrs = {"ether",};
  protected static final String windowsCmd = "ipconfig /all";
  protected static final String [] windowsSearchStrs = {"Physical Address",};
  protected static final ErrorLogStream dbg=new ErrorLogStream(GetMacid.class.getName());
  protected static final String notInited = "NOTINITIALIZED";
  protected static final TextList macids = new TextList(5);

  // only use for testing in a stand-alone way
  public static String Usage() {
    return "no parameters required";
  }
  public static void Test(String[] args) {
    ErrorLogStream.Console(ErrorLogStream.VERBOSE);
    getIt();
  }

  private static final Monitor monitor = new Monitor("GetMacid.class");

  public static final String getIt() {
    try {
      monitor.getMonitor();
      if(macids.size() == 0) {
        try {
          dbg.Enter("getIt");
          Runtime rt = Runtime.getRuntime();
          String cmd = null;
          String [] searches = null;
          TextList msgs = new TextList(30,2); // even if we don't use it
          // the 2 switches are separate since the fall-throughs are different
          // configure the data
          switch(OS.os.Value()) {
            case OS.SunOS: {
              cmd = solarisCmd;
              searches = solarisSearchStrs;
            } break;
            case OS.Linux: {
              cmd = linuxCmd;
              searches = linuxSearchStrs;
            } break;
            case OS.Windows2000:
            case OS.NT: {
              dbg.VERBOSE("os = " + OS.os.Image());
            } //break;
            case OS.Windows: {
              cmd = windowsCmd;
              searches = windowsSearchStrs;
            } break;
            default: { // includes unknown
              dbg.ERROR("unknown/incompatible os: " + OS.os.Image());
              return notInited;
            } //break;
          }
          // go get the strings
          StopWatch sw = new StopWatch(true);
          switch(OS.os.Value()) {
            case OS.SunOS: {
            } //break;
            case OS.Windows: {
            } //break;
            case OS.Linux: {
              Executor.runProcess(cmd, "os = " + OS.os.Image(), 0, 8 /* +++ parameterize */, msgs, dbg.myLevel.is(dbg.VERBOSE));
            } break;
            case OS.Windows2000:
            case OS.NT: {
              Process p = rt.exec(cmd); // ipconfig /all
              BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
              dbg.VERBOSE("waiting for process to exit ...");
              ThreadX.sleepFor(2000);// +++ parameterize
              while(in.ready()) {
                msgs.add(in.readLine());
              }
              in.close();
            } //break;
          }
          sw.Stop();
          dbg.VERBOSE("... process took " + sw.seconds() + " seconds.");
          boolean found = false;
          dbg.VERBOSE( "Output lines: " + msgs.size());
          for(int strs = 0; !found && (strs<searches.length); strs++) {
            String search = searches[strs];
            dbg.VERBOSE("searching for [" + strs + "/" + searches.length + "]: " + search);
            for(int iStr = msgs.size(); iStr-->0;) {//find all
              String str = msgs.itemAt(iStr);
              found |= parseit(str, search);
              dbg.VERBOSE( "--> " + str);
            }
            dbg.VERBOSE("Macid key string '" + search + "' " + (found ? "" : "NOT ") + "found.");
          }
          dbg.VERBOSE("Macids", macids.toStringArray());
        } catch (Exception e) {
          dbg.Caught(e);
        } finally {
          dbg.Exit();
        }
      }
    } finally {
      monitor.freeMonitor();
      return macids.itemAt(0);//arbitrary choice if more than one remains; function returns "" if no 0th item.
    }
  }

  private static final boolean parseit(String load, String search) {
    if((load == null) || (search==null)) {
      return false;
    }
    int where = load.indexOf(search);
    if(where >= 0) {
      // it is here!  get it!
      String loadStr = load.substring(where+search.length()).trim();
      StringBuffer sb = new StringBuffer();
      // windows uses -'s to separate instead of :'s
      loadStr = Safe.replace(loadStr, ". :", " "); // windows puts crap before the string
      loadStr = Safe.replace(loadStr, ".", " "); // windows puts crap before the string
      loadStr = Safe.replace(loadStr, "-", ":");
      // solaris removes preceding zeroes from each unit between :'s (sometimes '08' shows up as just '8')
      // need to detoken this.  Should get 5 :'s.  this results in 6 sets of hex numbers
      int colonIndex = loadStr.indexOf(':');
      String thisOne = "";
      Fstring twoChars = new Fstring(2, '0');
      while(colonIndex>=0) {
        thisOne = Safe.subString(loadStr, 0, colonIndex).trim(); // check this
        dbg.WARNING("thisOne = " + thisOne);
        thisOne = twoChars.righted(thisOne).toString();
        sb.append(thisOne);
        loadStr = Safe.restOfString(loadStr, colonIndex+1);
        colonIndex = loadStr.indexOf(':');
      }
      thisOne = loadStr;
      dbg.WARNING("lastOne = " + thisOne);
      sb.append(thisOne);
      // drop windows loopbacks
      if(sb.toString().indexOf("44455354" /* HEX of 'DEST'*/)==0) {
        dbg.VERBOSE("Dismissing a windows loopback.");
      } else {
        macids.add(sb.toString().toUpperCase());
        return true;
      }
    }
    return false;
  }
}
//$Source: /cvs/src/net/paymate/util/GetMacid.java,v $
