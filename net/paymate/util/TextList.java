/* $Id: TextList.java,v 1.48 2001/10/10 19:46:04 mattm Exp $ */
package net.paymate.util;

import java.util.Vector;
import java.util.Collections;
//+++ add wrappers for writing functions that stretch for reasonable indices.
import java.util.Enumeration;

public class TextList {
  protected static final ErrorLogStream dbg = new ErrorLogStream(TextList.class.getName());
  ////////////////////////////////////////////////////
  protected Vector storage;
  public String whenTrivial="";

  //wrap all of Vector's functions that we preserve:

  protected String safe(String o){
    return Safe.OnTrivial(o,whenTrivial);
  }

  public TextList add(String o) {
    storage.addElement(safe(o));
    return this;
  }

  public void insert(int i, String o) {//an insertion
    //+++if i > size then stretch it with emptys
    storage.add(i, safe(o));
  }

  public String set(int i, String o) {//an overwrite
    //+++if i > size then stretch it with emptys
    return (String) storage.set(i, safe(o));
  }

  public void ensureCapacity(int newsize){
    storage.ensureCapacity(newsize);
  }

  public int size(){
    return storage.size();
  }

  public void setSize(int newsize){
    storage.setSize(newsize);
  }

  public int indexOf(String lurker){
    return storage.indexOf(lurker);
  }

  public boolean contains(String key){
    return storage.contains(key); //contains uses .equals
  }

  public Object [] toArray(){
    return storage.toArray();
  }

  private static final String [] trashArray = new String [0];
  public String [] toStringArray(){
    return (String []) storage.toArray(trashArray);
  }

  public void clear(){
    storage.clear();
  }

  public String remove(int index){
    try {
      return itemAt(index);
    } finally {
      storage.remove(index);
    }
  }
  ////////////////////////////////////////////////////
/** Add an item to list and
 * @return index of item just added
 *
 */
  public int Add(String o) {
    storage.addElement(safe(o));
    return storage.size()-1;
  }

  /**
   * assure that this string is in list, by content not object match!
   * @return true if string was already present
   * String.equals
   */
  public boolean assurePresent(String s){
    if(this.contains(s)){
      return true;
    }
    Add(s);
    return false;
  }

  public TextList insertEnumeration(Enumeration enump){
    while(enump.hasMoreElements()) {
      String name = (String)enump.nextElement();
      if(name != null) {
        assurePresent(name);
      }
    }
    return this;
  }

//////////////////////
  public TextList(int initialCapacity, int capacityIncrement){
    storage=new Vector(initialCapacity, capacityIncrement);
  }

  public TextList(int initialCapacity){
    storage=new Vector(initialCapacity);
  }

  public TextList(){
    storage=new Vector();
  }

  public TextList(String [] array) {
    storage=new Vector();
    fromStringArray(array);
  }

  public int longestEntry(){
    int max=0;
    for(int i=storage.size();i-->0;){//
      int esize=itemAt(i).length();
      if(max<esize){
        max=esize;
      }
    }
    return max;
  }

  public TextList(String toSplit, int lineLength, boolean smartWrap) {
    this();
    this.split(toSplit,lineLength,smartWrap);
  }

  public TextList split(String toSplit, int lineLength, boolean smartWrap) {
    if(Safe.NonTrivial(toSplit)){
      if(lineLength < 1) {
        lineLength = 1;
      }
//      dbg.VERBOSE("splitting:"+toSplit+" len:"+lineLength);
      storage.ensureCapacity(size() + (toSplit.length() / lineLength) + 1);
      boolean canSplit;

      while(toSplit.length()>0) {
        int scanner=-1;
        int lastspace=-1;
        boolean foundSplit=false;
        if(lineLength>toSplit.length()){//rest of input can fit
          lineLength= toSplit.length(); //so we are just looking for new lines now
          smartWrap=false;
//          dbg.VERBOSE("shrinking:"+toSplit+" len:"+lineLength);
        }
        while(++scanner<lineLength){
          char ch=toSplit.charAt(scanner);
          if(Character.isWhitespace(ch)){//any word separator
            if(Character.isSpaceChar(ch)){//just blanks
              lastspace=scanner;
            } else {//is some form of line terminator
              foundSplit=true;
              break;
            }
          }
        }
        if(smartWrap&&!foundSplit&&lastspace>=0){
          scanner=lastspace;
          foundSplit=true;
        }

        storage.addElement(toSplit.substring(0,scanner).trim());//substring excludes last char in range
//        dbg.VERBOSE("split @ scanner="+scanner+":["+storage.lastElement()+"] "+foundSplit);
        if(foundSplit){//almost always true
          ++scanner;
        }
        toSplit=Safe.restOfString(toSplit,scanner);
      }
    }
    return this;
  }

  // this is a special function that takes a single string
  // and breaks it into several strings of a specified length
  public TextList oldersplit(String toSplit, int lineLength, boolean smartWrap) {
    if(lineLength < 1) {
      lineLength = 1;
    }
    if(toSplit != null) {
      storage.ensureCapacity(toSplit.length() / lineLength + 1);
      //+++add even more to above if smartwrapping
      StringBuffer buff = new StringBuffer(toSplit);
      int index;
      int cutat;
      while(buff.length() > 0) {

        index = Math.min(lineLength, buff.length());
        cutat = buff.toString().indexOf('\n');//+++ use System property

        int forFun = buff.toString().indexOf("\\n");
        if(cutat<0 || cutat>=index){//gotta cut short of the newline
          cutat=index;//will seek down from here
          if(smartWrap && (lineLength < buff.length())) {
            while((cutat > 1) && !Character.isWhitespace(buff.charAt(cutat))) {
              --cutat;
            }
            if(cutat < 2) {//didn't find a breaking point
              cutat = index;
            } else {
              ++cutat;//to point to whitespace character
            }
          }
        }
        String piece=buff.substring(0, cutat);
        add(piece);
//        dbg.VERBOSE("TextList() adding: " + piece);
        //+++ collapse multi-whitespace at cutpoint.
        if(cutat<index){//then remove the whitespace
          ++cutat;
        }
        buff.delete(0, cutat);
      }
    }
    return this;
  }

  public static final boolean SMARTWRAP_ON = true;
  public static final boolean SMARTWRAP_OFF = false;

  public String itemAt(int index){//casting and protection for access
    String retval = "";  //the empty string, not a null object!
    if((index < storage.size()) && (index >= 0)) {
      retval = (String) storage.elementAt(index);
    }
    return retval;
  }

  // returns how many it added
  public int appendMore(TextList from) {
    if(from!=null){
    int count = from.storage.size();
    for(int i=0;i<count;i++){
      add(from.itemAt(i));
    }
    return count;
    } else {
    return 0;
    }
  }

  // returns how many it added
  public int fromStringArray(String [] array) {
    // empty first?
    int len = array.length;
    storage.ensureCapacity(array.length);
    for(int i=0;i<len;i++){
      add(array[i]);
    }
    return array.length;
  }

  public static TextList CreateFrom(String [] array){
    TextList newone=new TextList();
    newone.fromStringArray(array);
    return newone;
  }

  public TextList centered(int span){//span=this.longest... is a cute choice
    TextList fatter=new TextList(this.size());
    for(int i=0;i<this.size();i++){
      fatter.add(Fstring.centered(this.itemAt(i),span,' '));
    }
    return fatter;
  }

  public String csv(boolean quoteit,boolean started){
    StringBuffer block= new StringBuffer(250); //wag

    for(int i=0;i<storage.size();i++){
      if(started){
        block.append(quoteit? ",\"" : ",");
      } else {
        started=true;
      }
      block.append(itemAt(i));
      if(quoteit){
        block.append('"');
      }
    }
    return block.toString();
  }

  public String asParagraph(String specialPrefix, String specialEOL) {
    StringBuffer block= new StringBuffer(250); //wag
    String EOL        = Safe.TrivialDefault(specialEOL, System.getProperty("line.separator"));
    String PREFIX     = Safe.TrivialDefault(specialPrefix, "");

    for(int i=0;i<storage.size();i++){
      block.append(PREFIX);
      block.append(itemAt(i));
      block.append(EOL);
    }
    return block.toString();
  }

  public String asParagraph(String specialEOL) {
    return asParagraph(null, specialEOL);
  }

  public String asParagraph(){
    return asParagraph(null, null);
  }

  public String toString(){
    return asParagraph();
  }

  public static final boolean NonTrivial(TextList arf){
    return arf!=null && arf.size()>0;
  }

  public Vector Vector(){
    return storage;
  }

  public void toLowerCase() {
    for(int i = 0; i < size(); i++) {
      set(i, itemAt(i).toLowerCase());
    }
  }

  public void sort(/* +++ boolean ascending */) {
    Collections.sort(storage);
  }

  public TextList wordsOfSentence(String sentence) {
    if(Safe.NonTrivial(sentence)){
      StringBuffer parsee = new StringBuffer(sentence);
      while(true) {
        String word = Safe.cutWord(parsee);
        if(Safe.NonTrivial(word)) {
          add(word);
        } else {
          break;
        }
      }
    }
    return this;
  }
}
//$Id: TextList.java,v 1.48 2001/10/10 19:46:04 mattm Exp $
