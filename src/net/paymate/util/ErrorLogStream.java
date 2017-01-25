/*$Id: ErrorLogStream.java,v 1.76 2001/11/17 00:38:35 andyh Exp $ */
/**
WARNING:
turning a stream on OR off while in an enter/exit scope screws up the stack.
this was done to improve efficiency the rest of the time.

TODO:
<li>on a global level do this: <BR>
create a constructor(s) that classes must call to get an instance: <BR>
public static final ErrorLogStream create(Class myclass) {<BR>
  return create(myclass, "");
}<BR>
public static final ErrorLogStream create(Class myclass, String more) {<BR>
  return new ErrorLogStream(myclass.getName() + Safe.TrivialDefault(more, ""));
}<BR>
And they will call it like:<BR>
private static final ErrorLogStream = ErrorLogStream.create(myClassName.class);<BR>
Then, we can do lots of management in the create function instead of in constructors (maybe)<BR>
!!!by using a create instead of an explicit construct we can prevent duplications..
*/

package net.paymate.util;

import java.io.*;
import java.util.Date;
import java.util.Vector;

import net.paymate.util.PrintFork;
// for the embedded exceptions
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.lang.reflect.Field;


class NullBugger extends ErrorLogStream {
  NullBugger(){
    super("NullBugger");
  }
  public void rawMessage(int msgLevel, String message){
    //do nothing
  }
}

public class ErrorLogStream implements AtExit {
  public static Tracer Debug;//globally sharable one; +++ mutex creation?

  private static NullBugger bitbucket=new NullBugger();
  public static ErrorLogStream Null(){
    return bitbucket;
  }
/**
 * @return a debugger that will work. EIther the given one or one that consumes all messages.
 */
  public static ErrorLogStream NonNull(ErrorLogStream dbg){
    return dbg!=null ? dbg : ErrorLogStream.Null();
  }
  /* the console fork is in the regular list, but we wish to be able to
  manipulate it implicitly for legacy reasons */
  public static PrintFork cpf;//special console print fork; +++ mutex creation of this?
  public static final LogSwitch globalLeveller;

  public void AtExit() {
    // stub
  }
  public static final void atExit() {
    if(fpf != null) {
      fpf.AtExit();
    }
  }
  public boolean IsDown() {
    // stub
    return false;
  }
  public static final boolean isDown() {
    return (fpf != null) ? fpf.IsDown() : true;
  }

  public String toSpam() {
    // just deal with THIS stream ...
    return ((myLevel!= null) ? (myLevel.Name() + ".level is " + myLevel.Image()) : "");
  }

  static {    // order of these is critical!
    Safe.preloadClass("net.paymate.util.LogSwitch",true);
    Safe.preloadClass("net.paymate.util.PrintFork",false);

    cpf = new PrintFork("System.out", System.out, LogSwitch.DEFAULT_LEVEL);
    Debug=new Tracer(ErrorLogStream.class.getName()+":Debug");//used by printfork management.
    globalLeveller = new LogSwitch(ErrorLogStream.class.getName()+":GLOBALGATE");
  }

//for legacy code
  public final static int VERBOSE=LogLevelEnum.VERBOSE;
  public final static int WARNING=LogLevelEnum.WARNING;
  public final static int ERROR  =LogLevelEnum.ERROR;
  public final static int OFF    =LogLevelEnum.OFF;


  public LogSwitch myLevel=null;
  protected StringStack context= new StringStack(); //the stack for ActiveMethod
  protected String ActiveMethod="?";//cached top of context stack

  public String Location(){
    return ActiveMethod;
  }
  public boolean bare=false; //+_+ made a state to expedite big change.
//bareness should be stacked in parallel with name

  public static final boolean isGlobalVerbose() {
    return globalLeveller.is(VERBOSE);
  }

  public int myLevel() {
    int ret = -1;
    if(myLevel != null) {
      ret = myLevel.Value();
    }
    return ret;
  }

  public ErrorLogStream(String cname,int spam){
    myLevel=new LogSwitch(cname,spam);
  }

  public ErrorLogStream(String cname){
    this(cname,LogSwitch.Invalid());//picks up default level of logswitch
  }

//////////////////////////////////////////////////////////
  public void rawMessage(int msgLevel, String message){
//the global leveller is being a royal pain right now, value is out of control!
    if(/*globalLeveller.passes(msgLevel) &&*/ myLevel.passes(msgLevel)){
      PrintFork.Println(message, msgLevel);
    }
  }
/**
 * If the message level is not VERBOSE, a date/time stamp is printed, otherwise it is left bare
 * --- but, if you do that, what is the point of the bare switch?  I'm turning this off for now.   I'm not getting times on anything! REVISIT LATER +++
 */
  public String timestamp(){
    return /*myLevel.is(VERBOSE) ? "" :*/ Safe.timeStampNow();
  }

  // !!! run javamake to test this! (works today, but any changes will EASILY break this!) MMM20001229
  protected void Message(int msgLevel, String message){
    String prefix = bare?"": (timestamp()+ LogSwitch.letter(msgLevel) + Thread.currentThread()
                + "@"  + myLevel.Name()
                + "::" + ActiveMethod
                + ":");
    rawMessage(msgLevel, prefix + message);
  }


  /**
   * three message levels are supported.
   */
  public void VERBOSE(String message){
    Message(VERBOSE, message);
  }

  public void WARNING(String message){
    Message(WARNING, message);
  }

  public void ERROR(String message){
    Message(ERROR, message);
  }

  public void VERBOSE(String message,Object[]  clump){
    logArray(VERBOSE, message,clump);
  }

  public void WARNING(String message,Object[]  clump){
    logArray(WARNING, message,clump);
  }

  public void ERROR(String message,Object[]  clump){
    logArray(ERROR, message,clump);
  }

  public void logArray(int msgLevel, String tag, Object[] clump){
    if(tag != null) { // just cleaner and easier to separate
      rawMessage(msgLevel, "<ol name="+tag+">");
      for (int i = 0; i < clump.length; i++){//in ascending order for clarity
        rawMessage(msgLevel, "<li> " + clump[i].toString());
      }
      rawMessage(msgLevel, "</ol name="+tag+">");
    } else {
      for (int i = 0; i < clump.length; i++){//in ascending order for clarity
        rawMessage(msgLevel, clump[i].toString());
      }
    }
  }

  public void VERBOSE(String message,Vector clump){
    logArray(VERBOSE, message,clump.toArray());
  }

  public void WARNING(String message,Vector clump){
    logArray(WARNING, message,clump.toArray());
  }

  public void ERROR(String message,Vector clump){
    logArray(ERROR, message,clump.toArray());
  }

  public void Enter(String methodName){
    context.push(ActiveMethod);
    ActiveMethod=methodName;
    VERBOSE("Entered");
  }

  public void Caught(Throwable caught){
    Caught("", caught);
  }

  public void Caught(String title, Throwable caught){
    int localLevel=ERROR;//FUE
    TextList tl = Caught(title, caught, new TextList());
    for(int i = 0; i < tl.size(); i++) {
      Message(localLevel, tl.itemAt(i));
    }
  }

  public static final TextList Caught(String title, Throwable caught, TextList tl){
    int localLevel=ERROR;//FUE
    tl.add("<Caught> " + Safe.TrivialDefault(title, ""));
    resolveExceptions(caught, tl);
    tl.add("</Caught>");
    return tl;
  }

  // for now, this can only detect exceptions in JPosExceptions,
  // but we might be able to detect them in other special exceptions later
  // +++ we might toss this since it might take too long
  //+++use refelction to seek first of any members of class exception
  protected static final Throwable NextException(Throwable t1) {
    Throwable t2 = null;
    // look for an exception in the exception:
    // for JposException, the function for getting the contained exception is
    // getOrigException()
    try {
      Class c = t1.getClass();
      int SQLItem = 1;
      String possibilities[] = {"getOrigException", "getNextException"};
      boolean isSql = false;
      // see if it is a JPosException or SQLException
      for(int i = possibilities.length; i-->0 && (t2==null);) {
        Method method = c.getMethod(possibilities[i], null);
        if(method != null) {
          t2 = (Exception)method.invoke(t1, null);
        }
      }
    } catch (Exception e) {
      /* abandon all hope ye who enter here */
    }
    return t2;
  }

  protected static final String extendedInfo(Throwable t) {
    String ret = null;
    if((t != null) && (t instanceof SQLException)) {
      SQLException e = (SQLException)t;
      ret += "\n  SQLState:" + e.getSQLState();
      ret += "\n  SQLMessage:" + e.getMessage();
      ret += "  SQLVendor:" + String.valueOf(e.getErrorCode());
    }
    return ret;
  }


  /**
   * New Exception resolver stuff
   */
  public static final TextList resolveExceptions(Throwable t) {
    return resolveExceptions(t, new TextList());
  }

  public static final TextList resolveExceptions(Throwable t, TextList tl) {
    tl.add("Error: " + t); // the trace doesn't always give enough detail!
    VirtualPrinter buffer= new VirtualPrinter();
    if(t != null) {
      t.printStackTrace(buffer);//all that ar elistneing to errors...
    }
    tl.add(buffer.backTrace());
    String ei = extendedInfo(t);
    if(ei != null) {
      tl.add(ei);
    }
    // here, see if the exception CONTAINS an exception, and if so, do it too
    Throwable t2 = NextException(t);
    if(t2 != null) {
      Caught("", t2, tl);
    }
    return tl;
  }

  public void Exit() {
    VERBOSE("Exits");
    ActiveMethod= context.pop();
  }

  //stream managers:
  public static final int Console(int consoleLevel){//return new setting...
    return cpf.myLevel.setto(consoleLevel);
  }

  // +++ mutex the management of these?
  public static LogFile fpf=null;
  private static PrintFork pf = null;

  public static final void stdLogging(String logName, int logLevels, boolean background, boolean overwrite){
    ErrorLogStream.Console(logLevels);
    ErrorLogStream.globalLeveller.setLevel(logLevels);

    if(Safe.NonTrivial(logName)){
      if(background){
        fpf = new LogFile(logName, overwrite, System.out);
        pf = fpf.getPrintFork(logLevels, true);
      } else {
        try {
          pf = new PrintFork(logName, new PrintStream(new FileOutputStream(logName)), logLevels);
        } catch (Exception filennotfound){
        //??? who cares
        }
      }
    }
    Debug.ERROR("stdLogging Started:"+logName+(background?" buffered":" straight")+(overwrite?" overwrite":" append"));
  }

  public static final void stdLogging(String logName, int logLevels, boolean background){
    stdLogging(logName, logLevels, background, false);//defaulted for server.
  }

  public static final void stdLogging(String logName, int logLevels){
    stdLogging(logName, logLevels, true);//defaulted for server.
  }

  public static final void stdLogging(String logName){
    stdLogging(logName,VERBOSE);//defaulted for server.
  }

  public static final void endLogging() {
    if(fpf != null) {
      fpf.flush();
    }
  }

// dumpage
  // path = the object's variable name
  public static final void objectDump(Object o, String path) {
    objectDump(o, path, null);
  }
  public static final void objectDump(Object o, String path, TextList tl) {
    Debug._objectDump(o, path, tl);
  }
  public void _objectDump(Object o, String path, TextList tl) {
    try {
      try {
        Enter("objectDump");
        objectSubDump(o,o.getClass(),path,tl);
      } catch (Exception e) {
        Caught(e);
      }
    } catch (Exception e2) {
      Caught(e2);
    } finally {
      Exit();
    }
  }
  void objectSubDump(Object o, Class c, String path, TextList tl) {
    // see if the object has a "toSpam()" function, if so, call it
    Method method = null;
    boolean logit = (tl == null);
    Class[] paramTypes = {Object.class, String.class, TextList.class};
    try {
      Enter("objectSubDump: " + path + "[" + c.getName() + "]");
      method = c.getMethod("toSpam", null);
      try {
        Object result = method.invoke(o, null);
        if(result instanceof TextList) {
          TextList tlResult = (TextList)result;
          for(int i = 0; i < tlResult.size(); i++) {
            tl.add(path+"."+tlResult.itemAt(i));
          }
        } else {
          String resultStr = result.toString();
          if(resultStr.indexOf('=') > -1) {
            tl.add(path+"."+resultStr);
          } else {
            tl.add(path+"="+resultStr);
          }
        }
      } catch (Exception e) {
        Caught(e);
      }
    } catch (NoSuchMethodException e) {
      WARNING(c.getName() + " does not have a method 'toSpam'");
      try {
        method = c.getMethod("objectDump", paramTypes);
        Object [] args = {c, path, tl};
        try {
          method.invoke(o, args);
        } catch (Exception e31) {
          Caught(e31);
        }
      } catch (NoSuchMethodException e45) {
        try {
          // if not, recurse through the class's supers and members
          Field [] fields1 = c.getFields();
          Field [] fields2 = c.getDeclaredFields();
          java.util.ArrayList list1 = new java.util.ArrayList(java.util.Arrays.asList(fields1));
          java.util.ArrayList list2 = new java.util.ArrayList(java.util.Arrays.asList(fields2));
          list1.addAll(list2);
          removeDuplicates(list1); // +++ need to remove duplicates
          Object [] fields = list1.toArray();
          Class [] classes = c.getClasses();
          if((fields.length==0) && (classes.length==0)) {
            // this is a primitive, I think
            String msg = path+"="+o;
            if(logit) {
              VERBOSE(msg);
            } else {
              tl.add(msg);
            }
          } else {
            dumpFields(o, c, path, tl, fields, logit);
            // then the classes
            for(int i=0; i<classes.length; i++) {
//              objectSubDump(o, classes[i], path+"["+classes[i].getName()+"]",tl);
            }
          }
        } catch (Exception e5) {
          Caught(e5);
        }
      }
    } catch (Exception e4) {
      Caught(e4);
    } finally {
      Exit();
    }
  }

  private void removeDuplicates(java.util.ArrayList list) {
    for(int i = list.size(); i-->1;) {
      for(int j = i; j-->0;) {
        if(list.get(i).equals(list.get(j))) {
          Debug.VERBOSE("removed["+i+"]: " + list.get(i));
          list.remove(i);
          break;
        }
      }
    }
  }

  void dumpFields(Object o, Class c, String path, TextList tl, /*Field[]*/ Object[] fields, boolean logit) {
    if(tl==null) {
      logit = true;
    }
    // first, do the fields
    for(int i=0; i<fields.length; i++) {
      Object fo = null;
      Field f = (Field)fields[i];
      String newPath = path+"."+f.getName();
      try {
        fo = f.get(o);
      } catch (IllegalAccessException e2) {
        String msg = newPath+"= <INACCESSIBLE!>";
//        if(logit) {
          VERBOSE(msg);
//        } else {
//          tl.add(msg);
//        }
      } catch (IllegalArgumentException e3) {
        String msg = newPath+"= <NOT INSTANCE OF CLASS:" + c.getName() + "?>";
        if(logit) {
          VERBOSE(msg);
        } else {
          tl.add(msg);
        }
      }
      if(fo!=null) {
        if(
           (fo instanceof Boolean)      ||
           (fo instanceof Character)    ||
           /*(fo instanceof Class)        || */
           (fo instanceof Number)       || /* handles byte, foublde, float, integer, long, short */
           (fo instanceof String)       ||
           (fo instanceof StringBuffer) ) {
          String msg = newPath+"="+fo;
          if(logit) {
            VERBOSE(msg);
          } else {
            tl.add(msg);
          }
        } else {
          objectSubDump(fo, fo.getClass(), newPath,tl);
        }
      }
    }
  }

}

//$Id: ErrorLogStream.java,v 1.76 2001/11/17 00:38:35 andyh Exp $
