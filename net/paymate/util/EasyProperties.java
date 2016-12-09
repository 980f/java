/**
* Title:        $Source: /cvs/src/net/paymate/util/EasyProperties.java,v $
* Description:  wrapper for type safe usage of java.util.Properties, plus other
*               conveniences in using such.
*               @see EasyCursor
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: EasyProperties.java,v 1.66 2001/09/14 21:10:39 andyh Exp $
*/
package net.paymate.util;
import  java.util.*;
import  java.lang.reflect.Field;
import  java.io.*;
import  java.net.URLDecoder;
import  java.net.URLEncoder;

// !!! NOTE: There should only be ONE instance each
// !!!       of setProperty() and getProperty(), and NONE of get() and put()
// !!!       All other functions should use getString() and setString()
// !!!       so that we can extend and do transformations on data!


class EasyProperties extends Properties {

  protected static final ErrorLogStream dbg = new ErrorLogStream(EasyProperties.class.getName(), ErrorLogStream.WARNING);

  public EasyProperties(){
    super();
  }

  public EasyProperties(Properties rhs){
//    super(rhs); //can't do it this way...
    super();
    addMore(rhs);//...gotta copy to go deep on easycursor stuff
  }

  public EasyProperties(String from){
    super();
    this.fromString(from, false /* don't need to clear a new one */);
  }
  /**
   * coz of the file storage format this is ...
   * ...the one char that no property value can ever start with:
   */
  public static final boolean isLegit(String value){
    return Safe.NonTrivial(value);// at least need this much!!! else 'default' values don't work
  }

  /**
   * if property is trivial replace with given
   * @return always true
   */
  public boolean assert(String key, String defawlt){
    if(!Safe.NonTrivial(getString(key))){
      setProperty(key,defawlt);
      //return false; //new setting is used
    }
    return true;//was already good
  }

  public EasyProperties setBoolean(String key, boolean newValue){
    setString(key,Bool.toString(newValue));
    return this;
  }

  public EasyProperties assertBoolean(String key, boolean newValue){
    assert(key,Bool.toString(newValue));
    return this;
  }

  public boolean getBoolean(String key, boolean defaultValue){
    for(int star=5;star-->0;) {//to recover from cycle of indirection
      String prop=getString(key);
      if(!Safe.NonTrivial(prop)) break;
      if(prop.equalsIgnoreCase("true"))  return true;
      if(prop.equalsIgnoreCase("false")) return false;
      key=prop; //see if it points to another property
    }
    return defaultValue;
  }

  public boolean getBoolean(String key){
    return getBoolean(key,false);
  }

  public EasyProperties setInt(String key, int newValue){
    setString(key,Integer.toString(newValue));
    return this;
  }

  public EasyProperties assertInt(String key, int newValue){
    assert(key,Integer.toString(newValue));
    return this;
  }


  public int getInt(String key, int defaultValue){
    try {
      String prop=getString(key);
      return Safe.NonTrivial(prop)?Safe.parseInt(prop):defaultValue;
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

  public EasyProperties assertLong(String key, long newValue){
    assert(key,Long.toString(newValue));
    return this;
  }

  public long getLong(String key, long defaultValue){
    try {
      String prop=getString(key);
      return Safe.NonTrivial(prop)?Safe.parseLong(prop):defaultValue;
    } catch (Exception caught){
      return defaultValue;      //be silent on errors
    }
  }

  public long getLong(String key){
    return getLong(key,0);
  }

  public EasyProperties setNumber(String key, double newValue){
    setString(key,Double.toString(newValue));
    return this;
  }

  public double getNumber(String key, double defaultValue){
    try {
      return Double.parseDouble(getString(key));
    } catch (Exception caught){
      return defaultValue;      //be silent on errors
    }
  }

  public double getNumber(String key){
    return getNumber(key,0.0);
  }

  public void setChar(String key, char chr) {
    setString(key, ""+chr);
  }

  public char getChar(String key) {
    String str = getString(key);
    if(str.length() > 0) {
      return str.charAt(0);
    } else {
      return 0;
    }
  }

  /**
  // filter all sets through here so we can do stuff if we want, except ...
  // NOTE: These strings MUST be 64KB or smaller!
  // (which means length < ~65535, but it still might be too long depending on the type of char!)
  // if you need longer data types, use getBytes() and setBytes() or a similar array handler
   * ! we want to be able to overwrite a non-null value with a null one!
   */
  public void setString(String key, String newValue){
    setProperty(key,newValue);
  }

  public String getProperty(String key){//4debug, to see param going to Properties.getProperty
    return Safe.NonTrivial(key)? super.getProperty(key):"";
  }

  /**
   * remove items whose values are null
   */
  public EasyProperties purgeNulls(boolean trivialsToo){
    Enumeration allprops=super.keys();
    Object okey; //key as object
    Object value;
    while(allprops.hasMoreElements()){
      okey=allprops.nextElement();
      value=super.get(okey);
      if(value==null){
        remove(okey);
      } else {
        if(trivialsToo){
           if(!Safe.NonTrivial(value)){
            remove(okey);
           }
        }
      }
    }
    return this;
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

  public String getString(String key){
    return getString(key,"");//empty string rather than null!!!
  }

  public void setURLedString(String key, String newValue){
    if(Safe.NonTrivial (newValue) ) {
      setString(key,URLEncoder.encode(newValue));
    }
  }
  /**
   * @param defaultValue is not encoded, it is used when there is nothing to decode
   */

  public String getURLedString(String key, String defaultValue) {
    try {//java.net.URLDecode code stupidly explicitly throws Exception rather than the particular that it throws
      return URLDecoder.decode(getString(key, defaultValue));
    } catch (Exception e){
      //+_+ could complain!
      return defaultValue;
    }
  }

  // +_+ if you add empty lines to this, it will truncate the list!
  public TextList getTextList(String key) {
    int size = getInt(textListSizeKey(key));
    TextList tl = new TextList(size);//reserves space
    tl.setSize(size);//allocates cells
    for(int i =size;i-->0;) {
      String prop = getString(textListKey(key, i/* was 'i++' --- a bug!!!???!!! */));
      tl.set(i,prop);//using this for order independence
    }
    return tl;
  }
  public void setTextList(String key, TextList tl) {
    setInt(textListSizeKey(key), tl.size());
    for(int i = 0; i<tl.size();i++) {
      setString(textListKey(key, i), tl.itemAt(i));
    }
  }

  protected String textListKey(String key, int i) {
    return key + "_" + i;
  }

  protected String textListSizeKey(String key) {
    return key + "_SIZE";
  }

  public TextList getPackedList(String key){
    TextList list=new TextList();
    String packed=getString(key);
    list.wordsOfSentence(packed);
    return list;
  }

  // maybe we should eventually add a string
  // to tell how many bytes we should find (and chunks there are)
  // for data validation
  public byte[] getBytes(String key) {
    StringBuffer ofBytes = new StringBuffer(128000); // sigh
    // since we ALWAYS suffix the key with an integer,
    // we can be guaranteed that the keys will have them now.
    // 64000 chunks makes 4 gigabytes; should be PLENTY
    for(int chunk = 0; chunk < MAXLENGTH; chunk++) {
      String chunky = key + chunk;
      String chunkStr = getString(chunky, null);
      if(chunkStr == null) {
        break;
      }
      ofBytes.append(chunkStr);
    }
    return ofBytes.toString().getBytes();
  }

  public void setBytes(String key, byte[] b) {
    // have to break this up into separate strings
    int MAXLENGTH = 64000;  // arbitrary < 64K
    if(b.length == 0) {
      setString(key + "0", "");
      return;
    }
    int chunks    = (b.length+MAXLENGTH-1) / MAXLENGTH; // ROUNDUP
    int length    = b.length;
    int done      = 0;
    for(int chunk = 0; chunk < chunks; chunk++) {
      String chunkStr = new String(b, chunk * MAXLENGTH, Math.min(MAXLENGTH, length-done));
      String chunky = key + chunk; // hehe
      setString(chunky, chunkStr);
      done += chunkStr.length();
    }
  }

  protected static final int MAXLENGTH = 64000;  // arbitrary < 64K

  public EasyCursor getEasyCursor(String key) {
    return new EasyCursor(getURLedString(key, ""));
  }

  public void setEasyCursor(String key, EasyCursor ezp) {
    setURLedString(key, ezp.toString());
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
    return getDate(key,Safe.Now());
  }

  /**
   * why synch? someone can change the underlying set the moment after synch is gone
   * I dropped the sync.  Does that seem reasonable? MMM 20010521
   */
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
    }
    return this;
  }

  // for outputting in different ways
  public static final String defaultValueSeparator = "=";
  public static final String defaultPropertySeparator = ",";

  public String asParagraph(String specialEOL) {
    return asParagraph(specialEOL, defaultValueSeparator);
  }

  public String asSwitchedParagraph(String specialEOL, String valueSeparator, boolean raw) {
    StringBuffer block=new StringBuffer(250); //wag
    for(Enumeration enump = propertyNames(); enump.hasMoreElements(); ) {
      String name = (String)enump.nextElement();
      block.append(name);
      block.append(valueSeparator);
      block.append(raw ? getProperty(name) : getString(name));
      block.append(specialEOL);
    }
    return block.toString();
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
    Enumeration enump = this.propertyNames();//goes deep on defaults.
    unsorted.insertEnumeration(enump);
    return unsorted;
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
      return baos.toString();
    } catch (Exception caught) {
      dbg.Enter("toString");
      dbg.Caught(caught);
      dbg.Exit();
      return "#"+header+" is faulty";
    }
  }

  public String toURLdString(String header) {
    return URLEncoder.encode(toString(header));
  }

  public void fromURLdString(String from, boolean clearFirst) {
    try {
      dbg.VERBOSE("fromURLdString() BEFORE decode: " + from);
      String newFrom = URLDecoder.decode(from);
      dbg.VERBOSE("fromURLdString() AFTER decode: " + newFrom);
      fromString(newFrom, clearFirst);
    } catch (java.lang.Exception jle){// added coz Sun 1.2.2 gives compilation error #360 if we don't
      //boo hoo....
      dbg.Caught(jle);
    }
  }

  public void fromString(String from, boolean clearFirst) {
    if(clearFirst) {
      clear();
    }
    if(Safe.NonTrivial(from)) {
      try {
        byte bytes[] = from.getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        dbg.VERBOSE("fromString() BEFORE load: " + asParagraph());
        /*super.*/load(bais);
        dbg.VERBOSE("fromstring loaded " + toString(""));
        dbg.VERBOSE("class: '" + getString("class") + "'");
      } catch (Exception caught) {
        dbg.Enter("fromString");
        dbg.Caught(caught);
        dbg.Exit();
      } finally {
      }
    }
  }

  public void storeSorted(OutputStream out, String header) throws IOException{
    // +++ convert this into a line sorter that happens on closing the stream
    StringBuffer buffer = new StringBuffer();
    StringWriter writer = new StringWriter();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    store(baos, header);
    BufferedReader reader = new BufferedReader(new StringReader(baos.toString()));
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

//$Id: EasyProperties.java,v 1.66 2001/09/14 21:10:39 andyh Exp $
