/* $Id: makeenum.java,v 1.19 2001/10/22 23:31:46 andyh Exp $ */

/**
   convert a list of text items into a labeled enumeration.
   this entails creating a java file for a class that holds the constants,
   and is a TrueEnum extension to create a type checkable
   class constrained to contain a value from the list.

  TODO: allow space in the text by converting embedded spaces into '_'
   easier but less pleasant is to make the text list use '_' but remove
   that from the printable text rendition of the list.

  NOTE: you can put comments in the .Enum files using // after the entry

 */
package net.paymate.builders;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.TextList;
import  net.paymate.util.Fstring;

import  java.io.*;

// +++ create some inner classes to hold the data members as they are being passed around

public class makeenum {//read a file of strings, make an enumeration out of them

  protected static final ErrorLogStream dbg = new ErrorLogStream(makeenum.class.getName());
  static {
    dbg.bare = true;
  }
  public String enumFilename   = null;
  public String packageName    = "net.paymate.util";
  public String enumName       = "tester";
  public String textForInvalid = null;
  public FileInputStream fis   = null;
  public TextList enumTable      = null;

  // black box
  protected static final void bitch(String why, StreamTokenizer st){
    dbg.ERROR("Error: "+why+ " encountered where Alphanum is expected, line number:"+st.lineno());
  }

  // black box
  static final TextList LoadList(StreamTokenizer st){
    TextList myEnumTable=new TextList(12,6); //heuristic numbers for allocator
    st.slashStarComments(true);
    st.slashSlashComments(true);
    try {
      for (int tk = st.nextToken(); tk != StreamTokenizer.TT_EOF; tk = st.nextToken()) {
        switch(tk) {
          case StreamTokenizer.TT_WORD:{
            myEnumTable.add(st.sval);
          }break;
          case StreamTokenizer.TT_NUMBER:{
            bitch("leading number stripped:" + st.nval,st);
          }break;
          case '=': {// next word better be a number
            tk = st.nextToken();
            if(tk==StreamTokenizer.TT_NUMBER){
              int newenumval=(int) st.nval;
              bitch("arbitrary enumeration value not yet supported, or maybe you didn't mean to have an = in the name",st);
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
    } catch (IOException caught){
      dbg.ERROR("LoadList: IO failure while reading list - " + caught);
      return null;
    }
    return myEnumTable;
  }

/**
 * If you call this function without going through the usual methods,
 * be sure you have set the appropriate variables first!
 */
  public boolean Generate(){
    //dbg.Enter("Generate");
    try {
      if(enumTable == null) { // set to your Vector before Generate(), if want
        // read all symbols
        if((enumTable = LoadList(new StreamTokenizer(new BufferedReader(new InputStreamReader(fis))))) == null) {
          return false;
        }
      }
      // generate the code
      String newName = enumFilename.substring(0, enumFilename.indexOf(".Enum")) + ".java";
      File f = new File(newName);
      PrintStream javacode=new PrintStream(new FileOutputStream(f));
      javacode.println("// DO NOT EDIT!  MACHINE GENERATED FILE! [" + enumFilename.replace('\\','/') + "]");
      javacode.println("package "+packageName+";");
      javacode.println("");
      javacode.println("import net.paymate.util.TrueEnum;");
      javacode.println("import net.paymate.util.TextList;");
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
      javacode.println("  private static final TextList myText = TrueEnum.nameVector("+enumName+".class);"); // --- 0718 testing with a private; remove if it doesn't work
      javacode.println("  protected final TextList getMyText() {");
      javacode.println("    return myText;");
      javacode.println("  }");
//the next doober lets us get class values via functions that the compiler would not let be static.
      javacode.println("  public static final "+enumName+" Prop=new "+enumName+"();");
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
      javacode.println("    this(rhs.Value());");
      javacode.println("  }");
      //lost the setto(enum) of TrueENum to unreliable class detection.
      javacode.println("  public "+enumName+" setto("+enumName+" rhs){");
      javacode.println("    setto(rhs.Value());");
      javacode.println("    return this;");
      javacode.println("  }");

      javacode.println("\n}");
      javacode.println("//$Id: makeenum.java,v 1.19 2001/10/22 23:31:46 andyh Exp $");
      javacode.close();
      dbg.ERROR(" " + f.getPath()/*f.getAbsolutePath()*/);
      return true;
    } catch (IOException e) {
      dbg.Caught(e);
      return false;
    }
    finally {
      //dbg.Exit();
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

  public static final void main(String [] arg) throws FileNotFoundException {
    int argc=arg.length;
    ErrorLogStream.Console(ErrorLogStream.WARNING);
    makeenum me /* hehe */ = new makeenum();
    switch(argc){
      default: dbg.ERROR("extra args on command line");
      case 0:{
        dbg.ERROR("usage: packagename classname [inputfile [textforinvalid]]");
      } return;
      /* trying one way to do progressive command line: */
      case 4: me.textForInvalid = arg[--argc]; // hold onto your panties for this next one ...
      case 3: me.enumFilename   = arg[--argc];
      case 2: me.enumName       = arg[--argc];
      case 1: me.packageName    = arg[--argc];
    }
    me.GenerateFromFile();
  }
}
//$Id: makeenum.java,v 1.19 2001/10/22 23:31:46 andyh Exp $
