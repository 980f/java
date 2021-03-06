/**
 * Title:        FindFiles<p>
 * Description:  Used to find files within a directory (and below)<p>
 * Copyright:    2000<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: FindFiles.java,v 1.14 2001/07/19 01:06:54 mattm Exp $
 */
package net.paymate.util;
import  java.io.File;
import  java.io.FileFilter;

// +++ This needs to use regular expressions!
// +++ See: http://www.meurrens.org/ip-Links/java/regex/index.html



public class FindFiles {

  protected static final ErrorLogStream dbg = new ErrorLogStream(FindFiles.class.getName());

/**
 * filterStr is something like ".java" (JUST LOOKS FOR EXTENSIONS RIGHT NOW!)
 * it is case-sensitive
 * try to use these first:
 */
  public static final TextList FindFilesUnder(String startDirStr, String filterStr, boolean recurse) {
    return FindFilesFrom(new File(startDirStr), new TailFilter(filterStr), recurse);
  }

  public static final TextList FindDirsUnder(String startDirStr, boolean recurse) {
    return FindFilesFrom(new File(startDirStr), new DirFilter(), recurse);
  }

/**
 * try to use the above ones first (protect these?)
 */
  public static final TextList FindFilesFrom(File startDir, FileFilter filter, boolean recurse) {
    TextList v = new TextList(60, 20);
    FindFilesFrom(v, startDir, filter, recurse ? new DirFilter() : null);
    return v;
  }

  /**
   * @return a new list which is reduced from the given list
   * @param fileList list of names generated by 'FindFilesFrom'
   * @param stale leave out stale files and directories per this list
   */

  public static final TextList ExcludeFilesFrom(TextList fileList, String stale[]){
    TextList reduced=new TextList(fileList.size());
    for(int i = fileList.size(); i-->0;) {
      String filename = fileList.itemAt(i);
      boolean passed = true;
      for(int ex=stale.length; ex-->0;){
        String exclude=stale[ex];
        if(filename.indexOf(exclude)>-1){
          passed = false;
          break;
        }
      }
      if(passed){
        reduced.add(filename);
      }
    }
    return reduced;
  }

  public static final void FindFilesFrom(TextList files, File startDir, FileFilter filter, DirFilter df) {
    /*
    // debugging
    String [] filenames = startDir.list();
    dbg.Message(filenames.length + " files & dirs found in " + startDir.getName());
    for(int i = filenames.length; i-->0;) {
      dbg.Message(" -- " + filenames[i]);
    }
    */
    // could also probably do this with a stack and not go recursive
    // find all files in this directory & add to the list
    File subfiles[] = Safe.listFiles(startDir,filter);
    for(int i = subfiles.length; i-->0;) {
      files.add(subfiles[i].getPath());
    }
    if(df != null) {  // user says to recurse
      // find all directories in this directory, look through them recursively
      subfiles = Safe.listFiles(startDir,df);
      for(int i = subfiles.length; i-->0;) {
        FindFilesFrom(files, subfiles[i], filter, df);
      }
    }
  }

  public static String Usage() {
    return "FindFiles startDir \".java\"";
  }

  // +++ give more options later!
  public static void Test(String args[]) {
    ErrorLogStream.Console(ErrorLogStream.VERBOSE);
    if(args.length==2) {
      TextList files = FindFilesUnder(args[0], args[1], true);
      int len = files.size();
      for(int i = 0; i < len; i++) {
        dbg.VERBOSE(files.itemAt(i));
      }
    } else {
      dbg.VERBOSE(Usage());
    }
  }

}
//$Id: FindFiles.java,v 1.14 2001/07/19 01:06:54 mattm Exp $
