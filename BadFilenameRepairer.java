/**
 * Title:        BadFilenameRepairer<p>
 * Description:  makes sure all filenames are lower case a-z, 0-9 and "-" and "_" only!!!<p>
 * Copyright:    copyleft<p>
 * Company:      none<p>
 * @author       Matt Mello
 * @version      0.01
 */

import java.io.File;
import java.lang.reflect.Array;
import java.util.Vector;

public class BadFilenameRepairer {

  class BadNameInfo {
    File file;
    String badChars;
    String oldName; // not filled in until a newname is generated
    String newName;
    boolean success;
    public BadNameInfo(File f, String bc) {
      file = f;
      badChars = bc;
    }
  }

  Vector files = new Vector(100, 10);
  Vector dirs  = new Vector(100, 10);

  // even though caps are accepted, we really want to make them lower !!!!!!
  // +++ eventually, instead of automatically doing this, we should prompt for user to rename
  public static final String acceptedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz01234567890-_. ";
//  static final String acceptedChars = "abcdefghijklmnopqrstuvwxyz01234567890-_. ";
  protected static boolean Verbose = false;
  protected static boolean Fix = false;

  protected static String getBadNameChars(String name) {
    String badcharlist = "";
    int len = name.length();
    for(int i = 0; i < len; i++) {
      char c = name.charAt(i);
      if(acceptedChars.indexOf(c) < 0) {
        if(badcharlist.indexOf(c) < 0) {
          badcharlist += c;
        }
      }
    }
    return badcharlist;
  }

  public void findBadFilenames(String startDir) {
    Vector subDirsTemp = new Vector(100, 100);
    subDirsTemp.add(startDir);
    while(subDirsTemp.size() > 0) {
      String dir = (String)subDirsTemp.elementAt(0);
      subDirsTemp.removeElementAt(0);
      if(Verbose) {
        System.out.println("Outer loop checking: " + dir);
      }
      File currentdir = new File(dir);
      File subfiles[] = currentdir.listFiles();
      for(int i = Array.getLength(subfiles); i-->0;) {
        File file = subfiles[i];
        if(Verbose) {
          System.out.println("Inner loop checking: " + file.getName());
        }
        String badChars = getBadNameChars(file.getName());
        if(file.isDirectory()) {
          subDirsTemp.add(file.getPath());
        }
        if(badChars.length()>0) {
          BadNameInfo bni = new BadNameInfo(file, badChars);
          if(file.isDirectory()) {
            if(Verbose) {
              System.out.println("--> adding dir " + file.getPath());
            }
            dirs.add(bni);
          } else {
            if(Verbose) {
              System.out.println("--> adding file " + file.getPath());
            }
            files.add(bni);
          }
          if(!Fix) {
            printBadInfoItem(bni);
          }
        }
      }
    }
  }

  protected static void printBadInfoItem(BadNameInfo info) {
    if(Fix) {
      System.out.println(info.oldName + " [" + info.badChars + "] " +
        (info.success ? "" : "UNSUCESSFULLY ") + "changed to: " + info.newName);
    } else {
      System.out.println(info.file.getPath() + " [" + info.badChars + "]");
    }
  }

  protected static void printBadInfoVector(String header, Vector v) {
    System.out.println(header + " [" + v.size() + "]:");
    int unsuccesses = 0;
    for(int i = v.size(); i-->0;) {
      BadNameInfo info = (BadNameInfo)v.elementAt(i);
      if(Fix && !info.success) {
        unsuccesses++;
      }
      printBadInfoItem(info);
    }
    if(Fix) {
      System.out.println("Failures: " + unsuccesses);
    }
  }

//  public void printBadFilenames() {
//    printBadInfoVector("Bad directories", dirs, false);
//    printBadInfoVector("Bad filenames", files, false);
//  }

  public void printFixedFilenames() {
    printBadInfoVector("Fixed directories", dirs);
    printBadInfoVector("Fixed filenames", files);
  }

  protected String fixFilename(String oldName) {
    StringBuffer newName = new StringBuffer(oldName.length());
    int len = oldName.length();
    for(int i = 0; i < len; i++) {
      char c = oldName.charAt(i);
      // "&" == "and" and all others are "_"
      if(acceptedChars.indexOf(c) < 0) {
        if(c == '&') {
          newName.append("and");
        } else {
          newName.append('_');
        }
      } else {
        newName.append(c);
      }
    }
    return newName.toString();
  }

  protected void fixOneVector(Vector infov) {
    int len = infov.size();
    for(int i = 0; i < len; i++) {
      BadNameInfo info = (BadNameInfo)infov.elementAt(i);
      // the rename function requires a complete path!  How to do that?
      // need: justpath + pathSeparator + fixed(justname)
      File newName = new File(info.file.getParentFile() + File.separator + fixFilename(info.file.getName()));
      info.oldName = info.file.getPath();
      info.newName = newName.getPath();
      try {
        info.success = info.file.renameTo(newName);
      } catch (Exception e) {
        info.success = false;
      }
    }
  }

  public void fixBadDirectories() {
    fixOneVector(dirs);
  }

  public void fixBadFilenames() {
    fixOneVector(files);
  }

  public BadFilenameRepairer() {
    // stub
  }

  public static String Usage() {
    return "Usage: \"BadFilenameRepairer [-f] [-v] <startDir>\"\n" +
           "  where -f means to fix\n" +
           "  and -v means to log verbosely\n" +
           "  and <startDir> is the directory to start in.";
  }

  public static void main(String[] args) {
    int argCount = Array.getLength(args);
    int startDirArg = -1;
    boolean fix = false;
    boolean verbose = false;
    for(int i = argCount; i-->0;) {
      if(args[i].equalsIgnoreCase("-f")) {
        fix = true;
      } else {
        if(args[i].equalsIgnoreCase("-v")) {
          verbose = true;
        } else {
          startDirArg = i;
        }
      }
    }
    if((argCount < 1) || (startDirArg == -1)) {
      System.out.println(Usage());
      return;
    }
    Verbose = verbose;
    Fix = fix;
    BadFilenameRepairer bfr = new BadFilenameRepairer();
    // need to do this whether or not we fix them:
    bfr.findBadFilenames(args[startDirArg]);
    if(fix) {
      // do the directories
      bfr.fixBadDirectories();  // must find them first!
      // redo the files (in case the directories above them changed)
      bfr.dirs.setSize(0);
      bfr.files.setSize(0);
      bfr.findBadFilenames(args[startDirArg]);
      // do the files
      bfr.fixBadFilenames();  // must find them first!
      bfr.printFixedFilenames();
//    } else {
//      bfr.printBadFilenames();
    }
  }
}
