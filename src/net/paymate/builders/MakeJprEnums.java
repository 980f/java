/**
 * Title:        MakeJprEnums<p>
 * Description:  Creates all of the TrueEnum class from .Enum entries in a JPR<p>
 * Copyright:    2000<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: MakeJprEnums.java,v 1.13 2001/07/19 01:06:45 mattm Exp $
 *
 * This should be run from the src directory.
 *
 */

package net.paymate.builders;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.FindFiles;
import  net.paymate.util.TextList;
import  java.io.*;

public class MakeJprEnums {

  protected static final ErrorLogStream dbg = new ErrorLogStream(MakeJprEnums.class.getName());
  private static final String dotenum = ".Enum";

  public static final boolean MakeEnums(TextList enumFile) {
    boolean totallySuccessful = true;
//    dbg.WARNING("Constructing enums ...");
    for(int i = enumFile.size(); i-->0;) {
      String enumfile = enumFile.itemAt(i);
      try {
        File file = new File(enumfile);
        // determine the package name (strip filename and convert path to "."ed)
        String packageName = file.getParent();
        // open the file for reading
        FileInputStream fis = new FileInputStream(file);
        // +++ get a function to do this!!!
        String classname = file.getName().substring(0, file.getName().indexOf(dotenum));
        makeenum enummaker = new makeenum(enumfile,
          file.getParent().replace('\\','.').replace('/','.'), classname, null);
        // +++ get a function to do this !!! (replace/replace/etc)
        enummaker.GenerateFromFile();
      } catch (Exception e) {
        dbg.ERROR("Exception making ennum " + enumfile + " - " + e);
        totallySuccessful = false;
      }
    }
    return totallySuccessful;
  }

  public static final TextList ExtractEnums(String JprFilename) {
    // each line is formatted as such:
    // #1=path\file.Enum
    TextList v = new TextList(0);
    try {
      StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(new FileInputStream(JprFilename))));
      st.slashStarComments(false);
      st.slashSlashComments(false);
      st.wordChars('.', '.');
      st.wordChars('\\', '\\');
      int state = 0;  // eventually, maybe enumerate
      for (int tk = st.nextToken(); tk != StreamTokenizer.TT_EOF; tk = st.nextToken()) {
        // proper sequence is '#', TT_NUMBER, '=', -3 (get it at that point)
        switch(tk) {
          case '#':{
            state = 1;
          } break;
          case StreamTokenizer.TT_NUMBER:{
            state = (state == 1) ? 2 : 0;
          } break;
          case '=': {// next word better be a number
            state = (state == 2) ? 3 : 0;
          } break;
          case StreamTokenizer.TT_WORD:{
            if(state == 3) {
              // is either a loose file or a package; just get our ennum files
              if(((String)st.sval).indexOf(dotenum) > -1) {
                String str = (String)st.sval;
                v.add(str.replace('\\',File.separatorChar).replace('/',File.separatorChar));
              }
            }
            state = 0;
          } break;
          default:{
            state = 0;
          } break;
        }//switch
      }//for
    } catch (IOException caught){
      dbg.ERROR("IO failure reading list from project file (" + JprFilename + ")- " + caught);
      return null;
    }
    return v;
  }

/**
 * This returns a value so that later programs can call it and see if it succeeded.
 * Note that passing a JprFilename of null will cause it to surf
 * directories from src/net on down (effectively is "ALL")
 * Passing main() "-d" gets the same effect.
 */
  static final String notFoundprefix = "No enums found in ";
  public static final boolean Make(String JprFilename) {

    TextList enums = null;
    if(JprFilename == null) {
      enums = FindFiles.FindFilesUnder("net", ".Enum", true);
      if(enums.size() < 1) {
        dbg.ERROR(notFoundprefix + "subdirectories! (pwd = " + System.getProperty("user.dir") + ")");
        return false;
      }
    } else {
      enums = ExtractEnums(JprFilename);
      if(enums == null) {
        return false;
      }
      if(enums.size() < 1) {
        dbg.ERROR(notFoundprefix + "project!");
        return false;
      }
    }
    return MakeEnums(enums);
  }

  // -- main

  public static final void main(String args[]) {
    ErrorLogStream.Console(ErrorLogStream.VERBOSE);
    dbg.bare = true;
    if(args.length < 1) {
      dbg.ERROR("parameters: [-d | <project.jpr>]" + System.getProperty("line.separator") +
        "   where -d is for surfing directories (basically, get ALL)," + System.getProperty("line.separator") +
        "   or a jpr file from which to extract them");
      return;
    }
    Make(args[0].equalsIgnoreCase("-d") ? null : args[0]);
  }

}
//$Id: MakeJprEnums.java,v 1.13 2001/07/19 01:06:45 mattm Exp $
