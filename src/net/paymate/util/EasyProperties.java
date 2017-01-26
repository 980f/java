/**
* Title:        $Source: /cvs/src/net/paymate/util/EasyProperties.java,v $
* Description:  wrapper for type safe usage of java.util.Properties, plus other
*               conveniences in using such.
*               @see EasyCursor
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: EasyProperties.java,v 1.100 2003/08/22 20:54:05 mattm Exp $
*/
package net.paymate.util;
import  java.util.*;
import  java.lang.reflect.*;
import  java.io.*;
import net.paymate.io.IOX;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;
import net.paymate.lang.ObjectX;
import net.paymate.lang.TrueEnum;

// !!! NOTE: There should only be ONE instance each
// !!!       of setProperty() and getProperty(), and NONE of get() and put()
// !!!       All other functions should use getString() and setString()
// !!!       so that we can extend and do transformations on data!

public  //Receipt needs some legacy stuff
class EasyProperties extends Properties {

  protected static final ErrorLogStream dbg = ErrorLogStream.getForClass(EasyProperties.class, ErrorLogStream.WARNING);

  public EasyProperties(){
    super();
  }

  public EasyProperties(Properties rhs){
//#can't do it this way...    super(rhs);
    super();
    addMore(rhs);//...gotta copy to go deep on easycursor stuff
  }

  public EasyProperties(String from){
    super();
    this.fromString(from, false /* don't need to clear a new one */);
  }

  /**
   * @return list of all full length names in set, regardless of internal cursor position
   */
  public TextList allKeys(){
    TextList keylist=new TextList(this.size());
    //HashTable.propertyNames gives full length names
    for(Enumeration enump = super.propertyNames(); enump.hasMoreElements(); ) {
      keylist.add((String)enump.nextElement());
    }
    return keylist;
  }

  public static final boolean isLegit(String value){
    return StringX.NonTrivial(value);// at least need this much!!! else 'default' values don't work
  }

  /**
   * if property is trivial replace with given
   * @return true if already was set
   */
  public boolean Assert(String key, String defawlt){
    if(!StringX.NonTrivial(getString(key))){
      setProperty(key,defawlt);
      return false; //new setting is used
    }
    return true;//was already good
  }

  public EasyProperties setObject(String key,Object obj){
    setString(key,String.valueOf(obj));
    return this;
  }

  /**
   * get named object
   */
  public Object getObject(String key,Class act){
    return extractObject(getString(key),act);
  }

/**
 * we can get objects if they have a string based constructor OR
 * public null constructors and a setto(String) function.
 */
  public Object extractObject(String objimage,Class act){
    try {
      try {
        Constructor [] ctors=act.getConstructors();
        Constructor nullCtor=null;
        Object []arglist=new Object[1];
        arglist[0]=objimage;

        for(int i=ctors.length;i-->0;){
          Constructor ctor=ctors[i];
          Class[] plist=ctor.getParameterTypes();
          if(plist.length==0){
            nullCtor=ctor;
          }
          if(plist.length==1 && plist[0]==String.class){
            return ctor.newInstance(arglist); //preferred exit
          }
        }
       //2nd chance: cosntruct then set:
        if(nullCtor!=null){
          Object obj=nullCtor.newInstance(new Object[0]);
          Class[] plist=new Class[1];
          plist[0]=String.class;
          Method setto=act.getMethod("setto",plist);
          if(setto!=null){
            setto.invoke(obj,arglist); //if we could trust all setto's to return this...
            //we could return the return value of the setto above.
            //at present some return their arg rather than 'this'
          }
          return obj;
        }
        return act.newInstance();
      }
      catch (Exception ex) {
        return act.newInstance();
      }
    } catch(Exception arf){
      dbg.Caught("getObject got",arf);
      return null;
    }
  }

  public Object getObject(String key,Object def){
    try {
      Object obj=getObject(key,def.getClass());//might goto EasyCursor.getobject
      return obj!=null ? obj : def;//.clone();
    }
    catch (Exception ex) {
      return def;//.clone();
    }
  }

  public EasyProperties setClass(String key, Class claz){
    setString(key,claz.getName());
    return this;
  }

  /**
   * @return a Class for the given @param key, null on any problem
   */
  public Class getClass(String key){
    return ReflectX.classForName(getString(key));
  }

  public EasyProperties setBoolean(String key, boolean newValue){
    setString(key,Bool.toString(newValue));
    return this;
  }

//this had been commentedout without a reason why.
  public boolean assertBoolean(String key, boolean newValue){
    return Assert(key,Bool.toString(newValue));
  }


  public boolean getBoolean(String key, boolean defaultValue){
    for(int star=5;star-->0;) {//to recover from cycle of indirection
      String prop=getString(key);
      if(!StringX.NonTrivial(prop)) break;
      return Bool.For(prop);
    }
    return defaultValue;
  }

  public boolean getBoolean(String key){
    return getBoolean(key,false);
  }

  public EasyProperties setInt(String key, int newValue){
    setString(key,String.valueOf(newValue));
    return this;
  }

  public boolean Assert(String key, int newValue){
    return Assert(key,String.valueOf(newValue));
  }

  public int getInt(String key, int defaultValue){
    try {
      String prop=getString(key);
      return StringX.NonTrivial(prop)?StringX.parseInt(prop):defaultValue;
    } catch (Exception caught){
      return defaultValue;      //be silent on errors
    }
  }

  public EasyProperties setChar(String key, char newValue){
    setString(key,String.valueOf(newValue));
    return this;
  }

  public char getChar(String key, char defaultValue){
    try {
      String prop=getString(key);
      return StringX.NonTrivial(prop)?StringX.firstChar(prop):defaultValue;
    } catch (Exception caught){
      return defaultValue;      //be silent on errors
    }
  }

  public int getInt(String key){
    return getInt(key,0);
  }

  public EasyProperties setLong(String key, long newValue){
    setString(key,Long.toString(newValue));
    return this;
  }

  public boolean assertLong(String key, long newValue){
    return Assert(key,Long.toString(newValue));
  }

  public long getLong(String key, long defaultValue){
    try {
      String prop=getString(key);
      return StringX.NonTrivial(prop)?StringX.parseLong(prop):defaultValue;
    } catch (Exception caught){
      return defaultValue;      //be silent on errors
    }
  }

  public long getLong(String key){
    return getLong(key,0);
  }

  public boolean assertDouble(String key, double newValue){
    return Assert(key,Double.toString(newValue));
  }

  public EasyProperties setNumber(String key, double newValue){
    setString(key,Double.toString(newValue));
    return this;
  }

  public double getNumber(String key, double defaultValue){
    assertDouble(key,defaultValue); //regular default syntax doesn't get us a default
    return StringX.parseDouble(getString(key),defaultValue);      //be silent on errors
  }

  public double getNumber(String key){
    return getNumber(key,0.0);
  }

  public char getChar(String key) {
    return getChar(key,(char)0);
  }

  /**
  // filter all sets through here so we can do stuff if we want, except ...
  // NOTE: These strings MUST be 64KB or smaller!
  // (which means length < ~65535, but it still might be too long depending on the type of char!)
  // if you need longer data types, use getBytes() and setBytes() or a similar array handler
   * ! we want to be able to overwrite a non-null value with a null one!
   */
  public EasyProperties setString(String key, String newValue){
    setProperty(key,newValue);
    return this;
  }

/**
 * added trim() here as crlf/lf stuff in properties files was killing us.
 * we could do a scan on load-from-file to optimize this
 */
  public String getProperty(String key){//4debug, to see param going to Properties.getProperty
    if(StringX.NonTrivial(key)){
      String raw=super.getProperty(key);
      if(raw!=null){
        return raw.trim();//which might become zero length but won't be a null.
      }
    }
    return "";
  }

  /**
   * remove items whose values are null
   */
  private EasyProperties purgeNulls(boolean trivialsToo){
    Enumeration allprops=super.keys();
    Object okey; //key as object
    Object value;
    while(allprops.hasMoreElements()){
      okey=allprops.nextElement();
      value=super.get(okey);
      if( value==null || (trivialsToo && !ObjectX.NonTrivial(value))){
        remove(okey);
      }
    }
    return this;
  }

  public EasyProperties purgeNulls(){
    return purgeNulls(false);
  }

  public EasyProperties purgeTrivials(){
    return purgeNulls(true);
  }

  public String getString(String key, String defaultValue){
    String retval = defaultValue;
    try {
      String prop=getProperty(key);
      if(isLegit(prop)) {
        retval = prop;
      } else {
        dbg.VERBOSE("getString: Did not find key '" + key + "'!");
      }
    } catch (Exception caught){
      dbg.Caught("getString keyed '" + key + "'!",caught);
    } finally {
      return retval;
    }
  }

  // ++++++++++++++ should we endeavor to make this case-INsensitive?

  public String getString(String key){
    return getString(key,"");//empty string rather than null!!!
  }

  public void setURLedString(String key, String newValue){
    if(StringX.NonTrivial(newValue)) {
      setString(key,EasyUrlString.encode(newValue));
    }
  }

  /**
   * @param defaultValue is not urlencoded, it is used when there is nothing to decode
   */
  public String getURLedString(String key, String defaultValue) {
    return StringX.TrivialDefault(EasyUrlString.decode(getString(key, defaultValue)), defaultValue);
  }

  public TextList getPackedList(String key){
    TextList list=new TextList();
    String packed=getString(key);
    list.wordsOfSentence(packed);
    return list;
  }

  public void saveEnum(String key, TrueEnum target){
    setString(key,target.Image());
  }

  public void loadEnum(String key, TrueEnum target){
    String prop=getString(key);
    if(prop!=null){
      target.setto(prop);
    }
  }

  /**
   * get int value of an enumeraiton using @param proto as a definition of the class involved.
   * it is strongly suggested that the TrueEnum.Prop member be used for this.
   * only in that case is this function (somewhat) multi-thread safe.
   */
  public int getEnumValue(String key, TrueEnum proto){
    String prop=getString(key);
    if(StringX.NonTrivial(prop)){
      synchronized (proto) {
      //this is synched presuming that proto is one of the TrueEnum.Prop objects,
      //and that this is the only place in the universe that uses the value of
      //that object.
        proto.setto(prop);
        return proto.Value();
      }
    } else {
      return TrueEnum.Invalid();
    }
  }

  public void setDate(String key,Date d){
    setLong(key,d.getTime());
  }

  public Date getDate(String key,Date def){
    long utcmillis =getLong(key);
    if(utcmillis>0){
      return new Date(utcmillis);//previously was always "NOW"!
    } else {
      return def;
    }
  }

  public Date getDate(String key){
    return getDate(key,DateX.Now());
  }

  public void setUTC(String key,UTC d){
    setLong(key,d != null ? d.utc : 0L);
  }

  public UTC getUTC(String key,UTC def){
    return new UTC().setto(getLong(key,def != null ? def.utc : 0L));
  }

  public UTC getUTC(String key){
    return new UTC().setto(getLong(key));
  }

  public Enumeration sorted() {
    return new OrderedEasyPropertiesEnumeration(this);
  }

  public EasyProperties Load(java.io.InputStream is){
    try {
      /*super.*/load(is);
    } catch (java.io.IOException caught){
      //silent failure...
    } catch (NullPointerException caught){
      //also ignored.
    } finally {
      IOX.Close(is);
    }
    return this;
  }

  // for outputting in different ways
  public static final String defaultValueSeparator = "=";
  public static final String defaultPropertySeparator = ",";

  public String asParagraph(String specialEOL) {
    return asParagraph(specialEOL, defaultValueSeparator);
  }

  /**
   * when called from EasyCursor this should list only the branch
   */
  public String asSwitchedParagraph(String specialEOL, String valueSeparator, boolean raw) {
    StringBuffer block=new StringBuffer(250); //wag
    for(Enumeration enump = propertyNames(); enump.hasMoreElements(); ) {
      String name = (String)enump.nextElement();
      block.append(name);
      block.append(valueSeparator);
      block.append(raw ? super.getProperty(name) : getString(name));
      if(enump.hasMoreElements()){//in case 'specialEOL' is a comma
        block.append(specialEOL);
      }
    }
    return String.valueOf(block);
  }

  public String asParagraph(String specialEOL, String valueSeparator) {
    return asSwitchedParagraph(specialEOL, valueSeparator, false);
  }

  public String asRawParagraph(String specialEOL, String valueSeparator) {
    return asSwitchedParagraph(specialEOL, valueSeparator, true);
  }

  public String asParagraph(){
    return asParagraph(defaultPropertySeparator);
  }

  public void debugDump(String header, String specialEOL, String valueSeparator) {
    dbg.VERBOSE(header + asParagraph(specialEOL, valueSeparator));
  }

  public TextList propertyList(){
    TextList unsorted=new TextList(this.size());
    return unsorted.assureItems(this.propertyNames());
  }

  // returns the number added
  public int addMore(Properties moreps) {
    int count = 0;
    for(Enumeration enump = moreps.propertyNames(); enump.hasMoreElements(); ) {
      String key = (String)enump.nextElement();
      String value = moreps.getProperty(key);
      setString(key, value);
      count++;
    }
    return count;
  }

  public String toString() {
    return toString("");
  }

  public String toString(String header) {
    String s = "";
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      store(baos, header);
      if(defaults!=null){//+_+ can only recurse one level deep with this approach
        defaults.store(baos,header);
      }
      return String.valueOf(baos);
    } catch (Exception caught) {
      dbg.Enter("toString");
      dbg.Caught(caught);
      dbg.Exit();
      return "#"+header+" is faulty";
    }
  }

  public String toURLdString(String header) {
    return EasyUrlString.encode(toString(header));
  }

  public void fromURLdString(String from, boolean clearFirst) {
    dbg.VERBOSE("fromURLdString() BEFORE decode: " + from);
    String newFrom = EasyUrlString.decode(from);
    dbg.VERBOSE("fromURLdString() AFTER decode: " + newFrom);
    fromString(newFrom, clearFirst);
  }

  public void fromString(String from, boolean clearFirst) {
    if(clearFirst) {
      clear();
    }
    if(StringX.NonTrivial(from)) {
      try {
        byte bytes[] = from.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        /*super.*/load(bais);
      } catch (Exception caught) {
        dbg.Caught(caught);
      }
    }
  }

  public static EasyProperties FromString(String streamed){
    EasyProperties newone = new EasyProperties();
    newone.fromString(streamed,false);
    return newone;
  }

  public void storeSorted(OutputStream out, String header) throws IOException{
    // +++ convert this into a line sorter that happens on closing the stream
    StringBuffer buffer = new StringBuffer();
    StringWriter writer = new StringWriter();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    store(baos, header);
    BufferedReader reader = new BufferedReader(new StringReader(String.valueOf(baos)));
    TextList tl = new TextList();
    String test = "";
    while(test != null) {
      test = reader.readLine();
      tl.add(test);
    }
    tl.sort();
    OutputStreamWriter osw = new OutputStreamWriter(out);
    String linefeed = System.getProperty("line.separator");
    for(int i = 0; i < tl.size(); i++) {
      osw.write(tl.itemAt(i));
      osw.write(linefeed);
    }
    osw.close();
  }

}

class OrderedEasyPropertiesEnumeration implements Enumeration {
  Vector names = null;
  EasyProperties ezp = null;
  int iterator = -1;

  public OrderedEasyPropertiesEnumeration(EasyProperties ezp) {
    this.ezp = ezp;
    names = ezp.propertyList().storage ;
    Collections.sort(names);
  }

  public Object nextElement() {
    return names.elementAt(++iterator);
  }

  public boolean hasMoreElements() {
    return (iterator < (names.size()-1));
  }

}

//$Id: EasyProperties.java,v 1.100 2003/08/22 20:54:05 mattm Exp $
