package net.paymate.util;

/**
* Title:        null<p>
* Description:  null<p>
* Copyright:    null<p>
* Company:      PayMate.net<p>
* @author PayMate.net
* @version $Id: LogSwitch.java,v 1.25 2001/10/27 07:17:28 mattm Exp $
*/

import java.util.Vector;
import java.util.Enumeration;
import java.util.Collections;
import net.paymate.util.*;
/**
* comparable is just for sorting for user displays.
*/
public class LogSwitch extends LogLevelEnum implements Comparable {
  protected String guiName;
  // +++ protect :: -->
  protected static int DEFAULT_LEVEL = LogLevelEnum.VERBOSE;//set for server, which has a harder time configuring than the client

  public final static LogLevelEnum VERBOSE= new LogLevelEnum(LogLevelEnum.VERBOSE);
  public final static LogLevelEnum WARNING= new LogLevelEnum(LogLevelEnum.WARNING);
  public final static LogLevelEnum ERROR= new LogLevelEnum(LogLevelEnum.ERROR);
  public final static LogLevelEnum OFF= new LogLevelEnum(LogLevelEnum.OFF);

  public static EasyCursor preloads=null;//+_+ PROTECT

  ///////////////////////////////////
  // registry
  protected static Vector registry ;//separate declaration and the following
  //static initializer were needed to make the bootup not loop in circles.
  // +++ try a static final with no static block:
  // protected static final Vector registry = new Vector(100, 10);
  static {
    registry= new Vector(100, 10);//100 debug objects, add in groups of 10
  }

  private static final char [] lvlLtr = {'-','/','!'};
  public static final char letter(int msgLevel) {
    return (((msgLevel > -1) && (msgLevel < lvlLtr.length)) ? lvlLtr[msgLevel] : ' ');
  }

  /**
   *   NOTE: there is a problem with this!
   *   Finalize will only get called when the program exits,
   *   as keeping the reference in the list causes the object to never get finalized otherwise!
   *  Who Cares? this function is a formality that could be deleted.
   */
  protected void finalize() {//of an individual switch
    remove();
  }

  public void remove() {
    registry.remove(this);
  }

  void register(){
    if(registry != null){//omits class preload instance
      registry.add(this); // sort +++ !!!
    }
  }

  protected static final Vector All() {
    return registry;
  }

  /**
   * interface for editing:
   *
   * Gives a "copy" of the list of pointers (but doesn't copy each item, still points to originals)
   * and sorts them.  Use this for external classes where possible.
   *
   * +++ Create a separate LogSwitchList class that has an itemAt() function
   * that this class and other classes can use! +++
   *
   */
  public static final Vector Sorted() {
    Vector debuggers=new Vector(LogSwitch.All().size(),10);
    debuggers.addAll(LogSwitch.All());
    Collections.sort(debuggers);  // sort them by name
    return debuggers;
  }

//  public static final LogSwitch item(int num) {
//    return (LogSwitch) All().elementAt(num);
//  }

  public static final LogSwitch find(String name) {
    Vector debuggers = Sorted(); // get a second list copy to prevent exceptions
    for(int i=0;i<debuggers.size();i++){
      LogSwitch test = (LogSwitch)debuggers.elementAt(i);
      if(test.Name().equals(name)) {
        return test;
      }
    }
    return null;
  }

  // for sorting for display purposes
  public int compareTo(Object o) {
    if((o == null) || (!(o instanceof LogSwitch))) {
      throw(new ClassCastException());//per the interface specification
    }
    LogSwitch that = (LogSwitch)o;
    return Name().compareTo(that.Name());
  }

  public static final EasyCursor asProperties(){
    EasyCursor blob=new EasyCursor(/* presize to di*/);
    Vector debuggers = Sorted(); // get a second list copy to prevent exceptions
    for(int i=0;i<debuggers.size();i++){
      LogSwitch bugger = (LogSwitch)debuggers.elementAt(i);
      blob.saveEnum(bugger.Name(),bugger);
    }
    return blob;
  }

  public String toSpam(){
    return Name() + ": " + Level().Image();
  }

  public static final void dump(int level) {
    try {
      Vector debuggers = Sorted(); // get a second list copy to prevent exceptions
      for(int i=0;i<debuggers.size();i++){
        LogSwitch lt = (LogSwitch)debuggers.elementAt(i);
        PrintFork.Println(lt.toSpam(),level);
      }
    } catch (Exception e) {
      // +++ whatever
    }
  }


  ////////////////////////////////
  protected void checkOverride(){
    if(preloads!=null && preloads.containsKey(Name())){
      setto(preloads.getString(Name()));
    }
  }

  /**intended for use just after preloads is initialized
  * and only that one time
  * MAJOR SIDE EFFECT: prelaods classes that appear in the logcontrol.properties
  */
  public static final void apply(EasyCursor ezp){
    preloads=ezp;
    apply();
    // now that we got the ones that already exist, try to get the others loaded and set:
    // the (truncated) classname should be the name of the property
    try {
      for(Enumeration enump = ezp.sorted(); enump.hasMoreElements(); ) {
        String name = "net.paymate." + (String)enump.nextElement();
        Safe.preloadClass(name); // ignore errors; we are just trying.  if we don't succeed, then oh, well.
      }
    }
    catch(NoClassDefFoundError ignore){
      //global catch didn't work! above is a throwable!
    }
    catch (Exception e) {
      // +++ ???
    }
    // again to set values for the newly loaded classes
    apply();
  }

  private static final void apply() {
    Vector debuggers = All();// get a second list copy to prevent exceptions//---what possible exceptions???
    for(int i=debuggers.size();i-->0;){
      ((LogSwitch)debuggers.elementAt(i)).checkOverride();
    }
  }


  public static final boolean setOne(String name, LogLevelEnum lle) {
    LogSwitch ls = find(name);
    if(ls==null) {
      return false;
    } else {
      ls.setLevel(lle.Value());
      return true;
    }
  }

  public static final void SetAll(String lvl){
    Vector debuggers = All(); // get a second list copy to prevent exceptions
    //---what possible exceptions??? add/remove while using can cause ArrayOutofBounds Exceptions
    for(int i=debuggers.size();i-->0;){
      ((LogSwitch)debuggers.elementAt(i)).setto(lvl);
    }
  }

  public static final void SetAll(LogLevelEnum lle){
    DEFAULT_LEVEL=lle.Value();
    Vector debuggers = All(); // get a second list copy to prevent exceptions
    //---what possible exceptions??? add/remove while using can cause ArrayOutofBounds Exceptions
    for(int i=debuggers.size();i-->0;){
      ((LogSwitch)debuggers.elementAt(i)).setto(DEFAULT_LEVEL);
    }
  }

  public static final TextList listLevels() {
    Vector debuggers = Sorted();
    TextList responses = new TextList();
    int count = debuggers.size();
    for(int i=0;i<count;i++){
      LogSwitch ls = (LogSwitch)(debuggers.elementAt(i));
      responses.add(ls.Name() + ": " + ls.Level());
    }
    return responses;
  }

  //////////////////////////////////////////////////////
  public LogSwitch(String lookup,int spam) {
    super(spam);
    if(!isLegal()){
      setto(DEFAULT_LEVEL);//truenum's leaves it more than verbose.
    }
    //since most of our classes have this prefix we remove it for readability sake.
    //classes NOT in this package usually don't have our debug stuff in them
    guiName=Safe.replace(lookup, "net.paymate.", "");
    register();
    checkOverride();
  }

  public LogSwitch(String lookup) {
    this(lookup,DEFAULT_LEVEL);
  }

  public LogSwitch(){
    //for class bootstrapper only
  }

  /////////////////////////////////////

  public LogLevelEnum Level(){
    return this;//super;
  }

  public LogSwitch setLevel(String fromenum){
    setto(fromenum);
    return this;
  }

  public LogSwitch setLevel(int level){
    setto(level);
    // +++ can we have this send out a message when it changes, to show that it changed?
    return this;
  }

  public String Name(){
    return guiName;
  }
  ////////////////////

  /**
  * if argument is the same or more severe than internal level we shall do something
  */
  public boolean passes(int llev){
    return (llev>=value) && (value!=LogLevelEnum.OFF) && (value!=LogLevelEnum.invalid);
  }

}
//$Id: LogSwitch.java,v 1.25 2001/10/27 07:17:28 mattm Exp $
