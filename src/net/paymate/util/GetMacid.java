/**
 * Title:        $Source: /cvs/src/net/paymate/util/GetMacid.java,v $
 * Description:  Gets the macid for win9x & linux<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: GetMacid.java,v 1.52 2004/01/09 11:46:07 mattm Exp $
 * NOTE: You can lookup the vendor of a macid here: http://standards.ieee.org/regauth/oui/index.shtml
 */

package net.paymate.util;
import  net.paymate.util.timer.StopWatch;
import  net.paymate.util.OS;
import  java.io.*;
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;
import net.paymate.lang.Fstring;

public class GetMacid {
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(GetMacid.class);

  // these will be sent to the OS, format accordingly
  private static final String primaryNIC="eth0";//+_+ make this a config parameter.
  private static final String linuxCmd = "ifconfig "+primaryNIC;
  private static final String [] linuxSearchStrs = {"HWaddr",};

  private static final String solarisCmd = "/sbin/ifconfig -a"; // for our suns
  private static final String [] solarisSearchStrs = {"ether",};

  private static final String windowsCmd = "ipconfig /all";
  private static final String [] windowsSearchStrs = {"Physical Address",};

  private static final String notInited = "NOTINITIALIZED";
  private static final TextList macids = new TextList(5);

  private static final Monitor monitor = new Monitor(GetMacid.class.getName());

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
          switch(OS.OsValue()) {
            case OsEnum.SunOS: {
              cmd = solarisCmd;
              searches = solarisSearchStrs;
            } break;
            case OsEnum.Linux: {
              cmd = linuxCmd;
              searches = linuxSearchStrs;
            } break;
            case OsEnum.Windows2000:
            case OsEnum.NT: {
              dbg.VERBOSE("os = " + OS.OsName());
            } //break;
            case OsEnum.Windows: {
              cmd = windowsCmd;
              searches = windowsSearchStrs;
            } break;
            default: { // includes unknown
              dbg.ERROR("unknown/incompatible os: " + OS.OsName());
              return notInited;
            } //break;
          }
          // go get the strings
          StopWatch sw = new StopWatch(true);
          switch(OS.OsValue()) {
            case OsEnum.SunOS: {
            } //break;
            case OsEnum.Windows: {
            } //break;
            case OsEnum.Linux: {
              Executor.runProcess(cmd, "os = " + OS.OsName(), 0, 8 /* +++ parameterize */, msgs, dbg.myLevel.is(dbg.VERBOSE), null);
            } break;
            case OsEnum.Windows2000:
            case OsEnum.NT: {
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
              dbg.VERBOSE( "=> " + str);
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
      loadStr = StringX.replace(loadStr, ". :", " "); // windows puts crap before the string
      loadStr = StringX.replace(loadStr, ".", " "); // windows puts crap before the string
      loadStr = StringX.replace(loadStr, "-", ":");
      // solaris removes preceding zeroes from each unit between :'s (sometimes '08' shows up as just '8')
      // need to detoken this.  Should get 5 :'s.  this results in 6 sets of hex numbers
      int colonIndex = loadStr.indexOf(':');
      String thisOne = "";
      Fstring twoChars = new Fstring(2, '0');
      while(colonIndex>=0) {
        thisOne = StringX.subString(loadStr, 0, colonIndex).trim(); // check this
        dbg.WARNING("thisOne = " + thisOne);
        thisOne = String.valueOf(twoChars.righted(thisOne));
        sb.append(thisOne);
        loadStr = StringX.restOfString(loadStr, colonIndex+1);
        colonIndex = loadStr.indexOf(':');
      }
      thisOne = loadStr;
      dbg.WARNING("lastOne = " + thisOne);
      sb.append(thisOne);
      // drop windows loopbacks
      if(String.valueOf(sb).indexOf("44455354" /* HEX of 'DEST'*/)==0) {
        dbg.VERBOSE("Dismissing a windows loopback.");
      } else {
        macids.add(String.valueOf(sb).toUpperCase());
        return true;
      }
    }
    return false;
  }

  public static void main(String[] args) {
    System.out.println(getIt());
  }

}
//$Source: /cvs/src/net/paymate/util/GetMacid.java,v $
