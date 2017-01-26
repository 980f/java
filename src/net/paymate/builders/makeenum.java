/* $Id: makeenum.java,v 1.34 2004/01/28 06:26:14 mattm Exp $ */

/**
   convert a list of text items into a labeled enumeration.
   this entails creating a java file for a class that holds the constants,
   and is a TrueEnum extension to create a type checkable
   class constrained to contain a value from the list.

  +++ TODO: allow space in the text by converting embedded spaces into '_'
   easier but less pleasant is to make the text list use '_' but remove
   that from the printable text rendition of the list.

  +++ TODO: Rework so that classes from other packages aren't pulled in (maybe recode in C).

  NOTE: you can put comments in the .Enum files using // after the entry

 */
package net.paymate.builders;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.io.FindFiles;
import  net.paymate.util.TextList;
import  net.paymate.lang.Fstring;

import  java.io.*;

// +++ create some inner classes to hold the data members as they are being passed around

public class makeenum {//read a file of strings, make an enumeration out of them

  protected static final ErrorLogStream dbg = ErrorLogStream.getForClass(makeenum.class);
  static {
    dbg.bare = true;
  }
  public String enumFilename   = null;
  public String packageName    = "net.paymate.util";
  public String enumName       = "tester";
  public String textForInvalid = null;
  public FileInputStream fis   = null;
//  public TextList enumTable      = null;

  // black box
  protected static final void bitch(String why, StreamTokenizer st){
    dbg.ERROR("Error: "+why+ " encountered where Alphanum is expected, line number:"+st.lineno());
  }

  private static final String empty = "";

  // black box
  static final int LoadList(StreamTokenizer st, TextList myEnumTable, TextList myOnechars){
//    TextList myEnumTable=new TextList(12,6); //heuristic numbers for allocator
    st.slashStarComments(true);
    st.slashSlashComments(true);
    int underchar = '_';
    st.wordChars(underchar, underchar);
    int additions = 0;
    boolean first = true;
    try {
      String onechar = empty;
      for (int tk = st.nextToken(); tk != StreamTokenizer.TT_EOF; tk = st.nextToken()) {
        switch(tk) {
          case StreamTokenizer.TT_WORD:{
            if(!first) {
              if (!onechar.equals(empty) && myOnechars.contains(onechar)) {
                bitch("TWO ENUMS HAVE THE SAME ONECHAR!", st); // +++ should we prevent it from being inserted?
              }
              myOnechars.add(onechar);
              onechar = empty;
            }
            myEnumTable.add(st.sval);
            additions++;
            first = false;
          }break;
          case StreamTokenizer.TT_NUMBER:{
            bitch("leading number stripped:" + st.nval,st);
          }break;
          case '=': {// next word better be a number
            tk = st.nextToken();
            switch(tk) {
              case StreamTokenizer.TT_NUMBER: {
                int newenumval=(int) st.nval;
                bitch("\"arbitrary enumeration value not yet supported, or maybe you didn't mean to have an = in the name.  Value would have been " + newenumval + ".\"",st);
              } break;
              case StreamTokenizer.TT_WORD: {
                onechar = st.sval;
              } break;
              default:
              case StreamTokenizer.TT_EOL:
              case StreamTokenizer.TT_EOF: {
                // fall out, handle it elsewhere
              } break;
            }
          } break;
          case '"':{
            bitch("Quoted strings not allowed ["+st.sval+"]",st);
          } break;
          default:{
            bitch("symbol [" + (char)tk+"]",st);
          } break;
        }//switch
      }//for
      myOnechars.add(onechar);
    } catch (IOException caught){
      dbg.ERROR("LoadList: IO failure while reading list - " + caught);
      return -1;
    }
    return additions;
  }

/**
 * If you call this function without going through the usual methods,
 * be sure you have set the appropriate variables first!
 */
  public boolean Generate(){
    try {
      TextList enumTable=new TextList(12,6); //heuristic numbers for allocator
      TextList oneChars=new TextList(12,6); //heuristic numbers for allocator
      // read all symbols
      if (LoadList(new StreamTokenizer(new BufferedReader(new InputStreamReader(
          fis))), enumTable, oneChars) == -1) {
        return false;
      }
      // generate the code
      String newName = enumFilename.substring(0, enumFilename.indexOf(".Enum")) + ".java";
      File f = new File(newName);
      PrintStream javacode=new PrintStream(new FileOutputStream(f));
      javacode.println("// DO NOT EDIT!  MACHINE GENERATED FILE! [" + enumFilename.replace('\\','/') + "]");
      javacode.println("package "+packageName+";");
      javacode.println("");
      javacode.println("import net.paymate.lang.TrueEnum;");
      javacode.println("");
      javacode.println("public class "+enumName+" extends TrueEnum {");
      if(textForInvalid != null) {
        javacode.println("  protected final static String invalidStr = \"" + textForInvalid + "\";");
      }
      int i;
      Fstring tabber=new Fstring(enumTable.longestEntry());
      for(i=0;i<enumTable.size();i++){
        tabber.setto(enumTable.itemAt(i));
        javacode.println("  public final static int "+tabber+"="+i+";");
      }
      javacode.println("");
      javacode.println("  public int numValues(){ return "+enumTable.size()+"; }");
      javacode.println("  private static final String[ ] myText = TrueEnum.nameVector("+enumName+".class);");
      javacode.println("  protected final String[ ] getMyText() {");
      javacode.println("    return myText;");
      javacode.println("  }");
//the next doober lets us get class values via functions that the compiler would not let be static.
      javacode.println("  public static final "+enumName+" Prop=new "+enumName+"();//for accessing class info");

      // see if we NEED the onechar stuff
      boolean needOnechars = false;
      // see if it is specified in the ennum file well
      boolean goodOnechars = true;
      for(i=0; i<oneChars.size(); i++) {
        String onechar = oneChars.itemAt(i);
        if((onechar == null) || (onechar.length()<1) || onechar.equals(empty)) {
          // this is a bad one, we don't need yet
          goodOnechars = false;
        } else {
          needOnechars = true;
          break;
        }
      }
      if(needOnechars) {
        if(!goodOnechars) {
          dbg.ERROR("Error: onechar array for " + enumName +
                    " is sparse, so can't build it.  Be sure to set all values = to some char! "+
                    "Omitting onechar code from ennum.");
        } else {
          javacode.println(
              "  private static final char [ ] oneCharArray = new char[ ] {");
          for (i = 0; i < oneChars.size(); i++) {
            String onechar = oneChars.itemAt(i);
            if ( (onechar == null) || (onechar.length() < 1)) {
              onechar = " ";
            }
            else {
              // things are okay
            }
            javacode.println("    '" + onechar.charAt(0) + "',");
          }
          javacode.println("  };");
          javacode.println("  protected final static char invalidChar = '_'; // ???");
          javacode.println("  public char CharFor(int rawValue){");
          javacode.println("    int newVal = Coerced(rawValue);");
          javacode.println("    return (newVal == invalid) ? invalidChar : oneCharArray[newVal];");
          javacode.println("  }");
          javacode.println("  public char Char(){");
          javacode.println("    return CharFor(value);");
          javacode.println("  }");
// DO NOT use String functions for the chars.  Make people do them manually to prevent them from doing bad things with strings.
//          javacode.println("  public String oneCharFor(int rawValue){");
//          javacode.println("    return String.valueOf(CharFor(rawValue));");
//          javacode.println("  }");
//          javacode.println("  public String oneChar(){");
//          javacode.println("    return oneCharFor(value);");
//          javacode.println("  }");
          javacode.println("  public int setto(char charValue){");
          javacode.println("    int tempvalue=invalid;");
          javacode.println("    for(int i = oneCharArray.length; i-->0;) {");
          javacode.println("      char mychar = oneCharArray[i];");
          javacode.println("      if(mychar == charValue) {");
          javacode.println("        tempvalue = i;");
          javacode.println("        break;");
          javacode.println("      }");
          javacode.println("    }");
          javacode.println("    return value=tempvalue;");
          javacode.println("  }");
          javacode.println("  public "+enumName+"(char charValue){");
          javacode.println("    super();");
          javacode.println("    setto(charValue);");
          javacode.println("  }");
        }
      }

      javacode.println("  public "+enumName+"(){");
      javacode.println("    super();");
      javacode.println("  }");

      javacode.println("  public "+enumName+"(int rawValue){");
      javacode.println("    super(rawValue);");
      javacode.println("  }");

      javacode.println("  public "+enumName+"(String textValue){");
      javacode.println("    super(textValue);");
      javacode.println("  }");

      javacode.println("  public "+enumName+"("+enumName+" rhs){");
      javacode.println("    this(rhs.Value());"); // +++ this could BLOW! Do in super?
      javacode.println("  }");
      //lost the value of setto(ennum) of TrueENum to unreliable class detection.

      javacode.println("  public "+enumName+" setto("+enumName+" rhs){");
      javacode.println("    setto(rhs.Value());"); // +++ this could BLOW! Do in super?
      javacode.println("    return this;");
      javacode.println("  }");

      javacode.println("  public static "+enumName+" CopyOf("+enumName+" rhs){//null-safe cloner");
      javacode.println("    return (rhs!=null)? new "+enumName+"(rhs) : new "+enumName+"();");
      javacode.println("  }");

      javacode.println("/** @return whether it was invalid */");
      javacode.println("  public boolean AssureValid(int defaultValue){//setto only if invalid");
      javacode.println("    if( ! isLegal() ){");
      javacode.println("       setto(defaultValue);");
      javacode.println("       return true;");
      javacode.println("    } else {");
      javacode.println("       return false;");
      javacode.println("    }");
      javacode.println("  }");

      javacode.println("\n}");
      javacode.println(""); // a blank line at end of file prevents certain trivial conflicts upon committal
      javacode.close();
      dbg.ERROR(" " + f.getPath()/*f.getAbsolutePath()*/);
      return true;
    } catch (IOException e) {
      dbg.Caught(e);
      return false;
    }
  }

  public boolean GenerateFromFile() {
    try {
      fis = new FileInputStream(enumFilename);
    } catch (Exception e) {
      dbg.ERROR("GenerateFromFile: Not able to open file: " + enumFilename + " - " + e);
      return false;
    }
    return Generate();
  }

  public makeenum(String enumfileName, String myPackageName, String myClassname, String myInvalidText) {
    enumFilename   = enumfileName;
    packageName    = myPackageName;
    enumName       = myClassname;
    textForInvalid = myInvalidText;
  }

  public makeenum() {
    // stub
  }

  private static final String dotenum = ".Enum";

  public static final boolean MakeEnums(TextList enumFile) {
    boolean totallySuccessful = true;
    for(int i = enumFile.size(); i-->0;) {
      String enumfile = enumFile.itemAt(i);
      totallySuccessful&=makeOneEnum(enumfile);
    }
    return totallySuccessful;
  }

  private static final boolean makeOneEnum(String enumFileName) {
    try {
      File file = new File(enumFileName);
      // determine the package name (strip filename and convert path to "."ed)
      String packageName = file.getParent();
      // open the file for reading
      // +++ get a function to do this!!!
      String classname = file.getName().substring(0, file.getName().indexOf(dotenum));
      makeenum enummaker = new makeenum(enumFileName,
        file.getParent().replace('\\','.').replace('/','.'), classname, null);
      // +++ get a function to do this !!! (replace/replace/etc)
      enummaker.GenerateFromFile();
      return true;
    } catch (Exception e) {
      dbg.ERROR("Exception making ennum " + enumFileName + " - " + e);
      return false;
    }
  }

/**
 * This returns a value so that later programs can call it and see if it succeeded.
 * it will surf directories from src/net on down (effectively "ALL")
 */
  public static final boolean MakeAllEnums() {
    TextList enums = null;
    enums = FindFiles.FindFilesUnder("net", ".Enum", true);
    if (enums.size() < 1) {
      dbg.ERROR("No enums found in subdirectories! (pwd = " +
                System.getProperty("user.dir") + ")");
      return false;
    }
    return MakeEnums(enums);
  }

}
//$Id: makeenum.java,v 1.34 2004/01/28 06:26:14 mattm Exp $
