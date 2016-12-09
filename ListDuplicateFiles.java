/**
 * Title:        ListDuplicateFiles<p>
 * Description:  This class finds all files with duplicate content within and below a certain directory<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ListDuplicateFiles.java,v 1.1 2001/06/27 00:53:52 mattm Exp $
 */

// NOTE: this class was for my (MMM) personal use (to find duplicate files on my HD),
// but I kept it as the property of PayMate.net since it will use the PayMate.net libraries
// PLEASE notify me before changing or deleting this file!

import  java.io.*;
import  java.util.*;
import  net.paymate.util.*;

public class ListDuplicateFiles {

  private static final ErrorLogStream dbg = new ErrorLogStream(ListDuplicateFiles.class.getName());

  private static FileInfoList all = new FileInfoList(100, 100); // <sigh>
  private static Vector founds = new Vector();

  public static void main(String[] args) {
    ErrorLogStream.stdLogging("ListDuplicateFiles.log");
    // the only arg received is the start path
    // if no arg received, presume "\" (or "/"; pathSeparator?)
    String startPath = ((args != null) && (args.length > 0)) ? args[0] : System.getProperty("file.separator");
    long startTime = System.currentTimeMillis();
    dbg.rawMessage("Starting the search from " + startPath + " [" + startTime + " ms]");
    // using the startPath, find ALL files (but not directories) underneath
    dbg.rawMessage("Cataloging all files from '" + startPath + "'.");
    FindFilesFrom(new File(startPath), new AllFilter(), new DirFilter());
    dbg.rawMessage("Found " + all.size() + " files.");
    // output the list to rawfiles.txt
    dump("rawfiles.txt");
    if(all.size() > 1) {
      dbg.rawMessage("Garbage collecting.");
      System.gc();
      // sort the files
      dbg.rawMessage("Sorting files (takes a while; be patient).");
      Collections.sort(all);
      // output the list to sortedfiles.txt
      dump("sortedfiles.txt");
      // stepping through the list, find all files that match size and group them and send off on another thread for comparison
      dbg.rawMessage("Comparing files.");
      // find a contiguous chunk of same-sized files
      int count = all.size();
      for(int start = 0; start < count; start++) {
        FileInfo startInfo = all.itemAt(start);
        for(int finish = start+1; finish < count; finish++) {
          FileInfo finishInfo = all.itemAt(finish);
          if(startInfo.size() != finishInfo.size()) {
            if(finish > start+1) {
              finish--;
              long refSize = startInfo.size();
              compare(refSize, start, finish);
              start = finish;
            }
            finish = count;
            break;
          }
        }
      }
    }
    long stopTime = System.currentTimeMillis();
    dbg.rawMessage("Finished the search from " + startPath + " [" + stopTime + " ms]");
    dbg.rawMessage("Took " + Safe.millisToSecsPlus(stopTime - startTime) + " .");
    // print any available reports and remove the lists
    int count = founds.size();
    for(int j = 0; j < count; j++) {
      printIt((FileInfoList)founds.elementAt(j));
    }
  }

  public static void FindFilesFrom(File startDir, FileFilter filter, DirFilter df) {
    dbg.rawMessage("Searching " + startDir.getPath() + ".");
    // could also probably do this with a stack and not go recursive
    // find all files in this directory & add to the list
    File subfiles[] = startDir.listFiles(filter);
    if(subfiles != null) {
      all.addMultiple(subfiles);
      // find all directories in this directory, look through them recursively
      subfiles = startDir.listFiles(df);
      for(int i = subfiles.length; i-->0;) {
        FindFilesFrom(subfiles[i], filter, df);
      }
    }
  }

  public static void compare(long originalSize, int start, int finish) {
    byte[] b1;
    byte[] b2;
    Vector matchedLists = new Vector();
    dbg.rawMessage("Comparing " + (finish - start + 1) + " files [" + start + " - " + finish + " / " + all.size() + "] of size " + originalSize + ".");
    if(originalSize == 0) {
      // this will only happen once
      FileInfoList matchedRoots = new FileInfoList(10/*factorydefault*/);
      for(int i = start; i <= finish; i++) {
        matchedRoots.add(all.itemAt(i));
      }
      if(matchedRoots.size() > 0) {
        dbg.rawMessage("Found " + matchedRoots.size() + " matching files of size " + originalSize + ".");
        // add them to the big list
        founds.add(matchedRoots);
        printIt(matchedRoots);
      }
    } else {
      b1 = new byte[(int)Math.min((long)10000, originalSize)];
      b2 = new byte[(int)Math.min((long)10000, originalSize)];
      // compare every byte of all files in the list
      // to do this, start at the first one, compare it with the 2nd, 3rd, etc.
      // if it matches any, mark them
      // when done with the first one, move to the next one and continue
      for(int item = start; item <= finish; item++) {
        FileInfoList matchedRoots = new FileInfoList(10/*factorydefault*/);
        FileInfo reference = all.itemAt(item);
        if(reference.matched) {
          continue;
        }
        for(int i = item+1; i<=finish; i++) {
          FileInfo compared = all.itemAt(i);
          // don't try to match an already matched content with this reference
          if(compared.matched) {
            continue;
          }
          if(match(reference.file, compared.file, b1, b2)) {
            if(!reference.matched) {
              matchedRoots.add(reference);
              reference.matched = true;
            }
            matchedRoots.add(compared);
            compared.matched = true;
          }
        }
        if(matchedRoots.size() > 0) {
          dbg.rawMessage("Found " + matchedRoots.size() + " matching files of size " + originalSize + ".");
          // add them to the big list
          founds.add(matchedRoots);
          //printIt(matchedRoots);
        }
      }
    }
    System.gc();
  }

  public static boolean match(File file1, File file2, byte [] b1, byte [] b2) {
    boolean ret = true;
    try {
      // compare every byte!
      FileInputStream file1in = new FileInputStream(file1);
      FileInputStream file2in = new FileInputStream(file2);
      while(true) {
        int count1 = file1in.read(b1);
        int count2 = file2in.read(b2);
        if(count1 != count2) {
          ret = false;
          break;
        }
        if(!Arrays.equals(b1, b2)) {
          ret = false;
          break;
        }
        // since they are the same, just use count1 for testing
        if(count1 < b1.length) {
          break;
        }
      }
      file1in.close();
      file2in.close();
    } catch (Exception e) {
      dbg.Message("Exception comparing '" + file1.getPath() + "' and '" + file2.getPath() + "'.");
      dbg.Caught(e);
      ret = false;
    }
    return ret;
  }

  private static void printIt(FileInfoList fil) {
    dbg.rawMessage("The following files of size " + fil.itemAt(0).size() + " have the same content:");
    for(int k = fil.size(); k-->0;) {
      dbg.rawMessage("     " + fil.itemAt(k).filename());
    }
  }

  private static final void dump(String filename) {
    String crlf = System.getProperty("line.separator");
    try {
      PrintWriter pw = new PrintWriter(new FileOutputStream(new File(filename)));
      for(int i = 0; i < all.size(); i++) {
        FileInfo fi = all.itemAt(i);
        pw.print("[" + fi.size() + "] " + fi.filename() + crlf);
      }
      pw.close();
      dbg.rawMessage("dumped to " + filename);
    } catch (Exception e) {
      dbg.Message("Excepted trying to dump to " + filename);
      dbg.Caught(e);
    }
  }

}


class AllFilter implements FileFilter {
  public boolean accept(File pathName) {
    return pathName.isFile();
  }
}

class FileInfo implements Comparable {
  public boolean matched = false;

  File file = null;

  public FileInfo(String filename) {
    // set variables; find size!
    this(new File(filename));
  }

  public FileInfo(File file) {
    // set variables; find size!
    this.file = file;
  }

  public String filename() {
    return (file != null) ? file.getPath() : null;
  }

  public long size() {
    return (file != null) ? file.length() : -1;
  }

  public int compareTo(Object o) {
    if((o == null) || (!(o instanceof FileInfo))) {
      throw(new ClassCastException());
    }
    FileInfo that = (FileInfo)o;
    if(size() < that.size()) {
      return -1;
    }
    if(size() > that.size()) {
      return 1;
    }
    // then they must be equal
    return 0;
  }
}

// just a class for easy usage
class FileInfoList extends Vector {
  public void addMultiple(File [] files) {
    for(int i = files.length; i-->0;) {
      File file = files[i];
      if(file.isFile()) {
        FileInfo fi = new FileInfo(file);
        add(fi);
      }
    }
  }
  public FileInfo itemAt(int i) {
    return (FileInfo)elementAt(i);
  }
  public FileInfoList(int howMany) {
    super(howMany);
  }
  public FileInfoList(int howMany, int howMuch) {
    super(howMany, howMuch);
  }
}
