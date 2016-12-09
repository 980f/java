/**
 * Title:        CommandLine<p>
 * Description:  defines, describes, and parses command lines and their arguments<p>
 * Copyright:    2000 PayMate.Net Corp<p>
 * Company:      paymate<p>
 * @author       PayMate.net
 * @version      $Id: CmdParser.java,v 1.5 2000/08/21 07:02:45 mattm Exp $
 *
 * NOTE: THIS CLASS (& package) IS NOT IN USE AND WILL PROBABLY BE DEPRECATED !!!
 *
 * After much deliberation and trial and error,
 * I decided that it would be easiest to do this as such:
 *
 * 1) ALL parameters must start with the FLAG_MARK ('-' by default)
 *      javac -0
 *
 * 2) The flag (s and f above) can be any length
 *      javac -verbose
 *
 * 3) All parameters that require a value (other information like a filename)
 *    must be distinguished by a VALUE_INDICATOR ('=' by default)
 *      javac -classpath=all.classes
 *
 * 4) Any value that contains whitespace must be enclosed in double quotation marks
 *      javac -classpath="c:\project files\all.classes"
 *
 * 5) Any compound value (like several filenames) must be separate by path
 *    separators (';' in windows and ':' in linux)
 *      javac -classpath=.;all.classes
 *
 * 6) Compound values with whitespace should have the whole value enclosed in
 *    double quotation marks
 *      javac -classpath=".;c:\project files\all.classes"
 *
 * 7) If for some reason the same parameter is entered twice with values,
 *    those values are all applied as if they were concatenated with the
 *    path separator
 *      javac -classpath=. -classpath="c:\project files\all.classes"
 *            equals
 *      javac -classpath=".;c:\project files\all.classes"
 *
 * While this all might seem like a pain, it makes parsing AND creating
 * commandlines very easy since the rules are so strict.
 * Also, I couldn't think of a situation that this wouldn't cover.
 * In other words, it seems complete and fairly concise.
 *
 * However, other ideas are welcomed.
 *
 * One issue to resolve is how to *easily* get TextLists back to the fields
 * in the program that created the parser.
 *
 * Another point is that since all of them parameters require the FLAG_MARK,
 * we could do away with it altogether.  The only argument I can think of
 * for this is that it helps when the user doesn't know what they are doing.
 * If it doesn't start with a FLAG_MARK, you can bet that the user made an error,
 * possibly by not enclosing a string with whitespace in quotation marks.
 * One way to get around this is to make the FLAG_MARK editable so that if it
 * is '', it is not searched for.  (Taking out the required FLAG_MARK would
 * also make the commandline shorter so that DOS could handle it better.)
 *
 */

package net.paymate.util.cmdline;

import  java.util.Enumeration;
import  java.util.Vector;
import  java.io.File;
import  net.paymate.util.TextList;
import  net.paymate.util.Safe;

public class CmdParser extends Vector {

  protected String command;       // just the command; eg: DIR
  protected String description;   // this is the one-line usage string, eg:
                        // "lists all files and subdirectories within a directory"
  protected String [] lastParsed; // the last String array that you parsed (will probably only ever be one)
  protected TextList unused = new TextList(10);      // list of unused strings in the last parse
  protected String parsedResults = ""; // gets set during parse()
  // constants ..
  // for parsing ...
  protected static final String FLAG_MARK       = "-"; // +++ make variable later
  protected static final String VALUE_INDICATOR = "="; // +++ make variable later
  protected static final char   QUOTATION_MARK  = '\"';
  // for console display ...
  protected static final String FLAG_INDENT     = "  ";
  protected static final String DESCRIPT_INDENT = "  ";
  protected static final String OPTIONAL_LEFT   = "[";
  protected static final String OPTIONAL_RIGHT  = "]";
  protected static final String SPACE           = " ";
  protected static final String COMMA_SPACE     = ", ";
  protected static final String UNINITIALIZED   = "<uninitialized>";
  protected static final String LINEFEED        = "\n";
  // +++ 80 columns is a standard; enhance later
  protected static final int CONSOLE_WIDTH      = 80-1; // for safety

  public String results() {
    return parsedResults;
  }

  // the data accessor methods (rather than containing a Vector, we are one)
  public Parameter parameter(int index){//casting and protection for access
    try {
      return (Parameter) elementAt(index);
    } catch (IndexOutOfBoundsException caught){
      return null;
    }
  }
  public Parameter parameter(String name){//casting and protection for access
    for(int i = size(); i-->0;) {
      Parameter p = parameter(i);
      if(p.flag.equalsIgnoreCase(name)) {
        return p;
      }
    }
    return null;
  }

  protected void splitValues(String str, TextList tl) {
    // trim the value and remove the surrounding quotation marks
    str.trim();
    if( (str.length() > 1) && (str.charAt(0) == QUOTATION_MARK) &&
        (str.charAt(str.length() - 1) == QUOTATION_MARK)) {
      str = str.substring(1, str.length()-2);
    }
    // break the values into separate strings
    int start = 0;
    for(int j = 0; j < str.length(); j++) {
      // +++ this could handle quotation marks, etc., better, but will do for now
      if(str.charAt(j) == File.pathSeparatorChar) {
        tl.add(str.substring(start, j));
        start = j+1;
      }
    }
    if(start < str.length()) {
      tl.add(str.substring(start, str.length()));
    }
  }

  // the big function!  this has got to be simpler! (no time to deal with it now)
  public boolean parse(String [] params) {
    lastParsed = params;
    unused.clear();  // clear out the old unuseds
    parsedResults = "";
    // for each parameter, clear its value vector
    for(int i = size(); i-->0;) {
      parameter(i).value.clear();
    }
    // don't do anything if didn't get any parameter strings!
    if(params == null) {
      return false;
    }
    // now parse what we got
    int len = params.length;
    String parsedParameters = "";
    for(int i = 0; i < len; i++) {  /* do them in order */
      String str = params[i];
      parsedParameters += SPACE + str;  // for printing later
      boolean used = false;
      // check for the FLAG_MARK
      if(str.startsWith(FLAG_MARK)) {
        String theRest = str.substring(FLAG_MARK.length());
        int index = theRest.indexOf(VALUE_INDICATOR);
        if(index > -1) {
          // get the names and values
          String name  = theRest.substring(0, index);
          // what happens with this if it looks like "whatever=" ?
          String value = theRest.substring(index + VALUE_INDICATOR.length());
          // locate the parameter this refers to and set the values
          Parameter p = parameter(name);
          if(p != null) {
            p.present = true;
            splitValues(value, p.value);
            used = true;
          }
        }
      }
      // since not used, put in unused array
      if(!used) {
        unused.add(str);
      }
    }

    // now check to see if we got all of the required parameters, & gen report
    StringBuffer strBuff = new StringBuffer(1000);
    // show the original string
    strBuff.append("Parsed parameters:").append(parsedParameters).append(LINEFEED);
    // deal with the parsedness
    boolean missingParam = false;
    for(int i = 0; i < size(); i++) {
      Parameter p  = parameter(i);
      // list it!
      strBuff.append(p.required ? "required " : "").append("parameter ");
      strBuff.append(FLAG_MARK).append(p.flag).append(" : ").append(p.name);
      // if it was required, tells if it wasn't present
      if(!p.present) {
        strBuff.append(" not");
        if(p.required) {
          missingParam = true;
        }
      }
      strBuff.append(" present");
      // handle default values
      if((p.defaultValue != null) && (p.value.size() == 0)) {
        p.value.add(p.defaultValue);
      }
      // deal with values and list them
      if(p.value.size() > 0) {
        strBuff.append("; values: ").append(QUOTATION_MARK);
        strBuff.append(p.value.asParagraph(COMMA_SPACE)).append(QUOTATION_MARK);
      }
      // done with the parameter
      strBuff.append(LINEFEED);
    }
    // display any unused parameters
    if(unused.size() > 0) {
      strBuff.append("unused parameters: ");
      strBuff.append(unused.asParagraph(COMMA_SPACE)).append(LINEFEED);
    }
    parsedResults = strBuff.toString();
    return !missingParam;
  }

  // this whole thing was probably a bad idea.
  // I should probably have made the class CONTAIN a ParameterList.
  protected void fromParamDefArray(ParameterDefinition [] paramList) {
    if(paramList == null) {
      return;
    }
    for(int i = paramList.length; i-->0;) {
      insertElementAt(new Parameter(paramList[i]), 0);
    }
  }

  protected String parameterUsage() {
    StringBuffer cmdBuff   = new StringBuffer(200);
    StringBuffer whereBuff = new StringBuffer(1000);

    // now, prepare for the more detailed parameter description strings ...
    // determine the max width of the flags
    int maxFlagWidth = 0;
    for(Enumeration paramList = elements(); paramList.hasMoreElements(); ) {
      Parameter p  = (Parameter) paramList.nextElement();
      int len      = p.flag.length();
      maxFlagWidth = Math.max(maxFlagWidth, len);
    }
    // flagwidth doesn't include the FLAG_MARK or an indent yet, though, so ...
    int loneDescriptIndent = FLAG_INDENT.length() + FLAG_MARK.length() +
                             maxFlagWidth + DESCRIPT_INDENT.length();
    int maxDescriptWidth   = CONSOLE_WIDTH - loneDescriptIndent; // wrap to this

    // how else to do this?
    String loneDescriptIndexStr = new String();
    for(int i = 0; i < loneDescriptIndent; i++) {
      loneDescriptIndexStr += SPACE;
    }

    for(Enumeration paramList = elements(); paramList.hasMoreElements(); ) {
      Parameter p  = (Parameter) paramList.nextElement();

      // first, create the main commandline string
      if(!p.required) {
        cmdBuff.append(OPTIONAL_LEFT);
      }
      cmdBuff.append(FLAG_MARK).append(p.flag);
      // the values
      for(int valueCount = 0; valueCount < p.typicalValueCount; valueCount++) {
        if(valueCount==0) {
          cmdBuff.append(VALUE_INDICATOR);
        }
        cmdBuff.append(p.name);
        if(p.typicalValueCount > 1) {
          cmdBuff.append(valueCount+1);
        }
        if(valueCount != (p.typicalValueCount-1)) {
          cmdBuff.append(COMMA_SPACE);
        }
      }
      // finished
      if(!p.required) {
        cmdBuff.append(OPTIONAL_RIGHT);
      }
      cmdBuff.append(SPACE);
      // now, make the more detailed parameter description strings ...
      if(p.flag.length() > 0) {
        whereBuff.append(FLAG_INDENT).append(FLAG_MARK).append(p.flag).append(DESCRIPT_INDENT);
      }
      TextList tl = new TextList(
        ((p.flag.length() < 1) ? (p.name + " - ") : "") + p.description,
        maxDescriptWidth, TextList.SMARTWRAP_ON);
      String specialEOL = System.getProperty("line.separator");
      String allAsPara = Safe.clipEOL(tl.asParagraph(loneDescriptIndexStr, specialEOL), specialEOL);
      whereBuff.append(allAsPara);
      if(p.defaultValue != null) {
        whereBuff.append("; default=").append(QUOTATION_MARK);
        whereBuff.append(p.defaultValue).append(QUOTATION_MARK);
      }
      whereBuff.append(LINEFEED);
    }
    return "Usage: " + command + SPACE + cmdBuff + LINEFEED + whereBuff;
  }

  // this returns the usage string per the programmers config
  public String usage() {
    return command + ": " + description + LINEFEED + LINEFEED + parameterUsage();
  }

  // eg: ParameterDefinition [] defs = { {"r", "recurse",   false, -1, "recurse subdirectories"},
  //                                     {"t",  "text",     true,  0,  "text to grep for"},
  //                                     {"m",  "filemask", true,  0,  "the mask indicating the files to search"} };
  //     CmdParser("grep", "searches for a text string in files", defs);
  public CmdParser(String cmd, String descript, ParameterDefinition [] paramList) {
    super(paramList.length);
    command     = Safe.TrivialDefault(cmd,      UNINITIALIZED);
    description = Safe.TrivialDefault(descript, UNINITIALIZED);
    fromParamDefArray(paramList);
  }

}