/* $Id: TrueEnum.java,v 1.39 2001/07/19 01:06:55 mattm Exp $ */
/*
The commented out static's indicate that the method would logically be static
but since we always use these from a derived class we need the context of some object
to determine which derived class.
It is not surprising that something that SHOULD be fundamental to the language
is hard to implement using that language.

 */
package net.paymate.util;

//import net.paymate.util.ErrorLogStream;

//we use reflection to associated text with enumerated values.
import java.lang.reflect.*;

public abstract class TrueEnum {//extensions define symbols, this enforces range

  // the only thing you have to override to use this class:
  protected abstract TextList getMyText();
  public abstract int numValues();

//  protected static final ErrorLogStream dbg = new ErrorLogStream(TrueEnum.class.getName());
  protected final static int invalid =-1; //alh made this public for things that operate upon TrueEnums, and trueenum croaked!
  protected final static String invalidStr = "BAD CHOICE";//!!!! this text makes it to the clerk interface for undefined keys...
  protected int value=invalid;

  public void Clear(){
    value=invalid;
  }

  public static final int Invalid(){//public access fubar'd the symbol extraction stuff.
    return invalid;
  }

  protected /*static */ TextList getText() {
    return getMyText();
  }

  public /*static */ boolean Legal(int rawValue){
    return rawValue>invalid && rawValue<numValues();
  }

  public /*static */ int Coerced(int rawValue){
    return Legal(rawValue) ? rawValue : Invalid();
  }

  public /*static */ String TextFor(int rawValue){
    int newVal = Coerced(rawValue);
    return (newVal == invalid) ? invalidStr : text(newVal);
  }
/**
 * @mmdeprecated IS BORKED
 * @return whether the rhs's underlying class is the same as 'this's underlying class.
 */
//  public boolean isCompatibleWith(TrueEnum rhs){//+++ always returns FALSE!
//    return this.getClass()==rhs.getClass();
//  }

  public boolean isLegal(){
    return Legal(value);
  }

  public boolean is(int rawValue){
    /* use of Coerced here makes all kinds of invalidity indistinguishable */
    return value == Coerced(rawValue);
  }

  /**
   * @mmdeprecated
   */
//  public boolean is(TrueEnum rhs) {
//    return isCompatibleWith(rhs) ? (value == rhs.value) : false;
//  }

  public int Value(){
    return value;
  }

  public String Image(){
    return TextFor(value);
  }

  public String toString(){
    return TextFor(value)+"["+value+"]";
  }

  public int setto(int rawValue){
    return value= Coerced(rawValue);
  }

//  public int setto(TrueEnum rhs) {
//    return setto( isCompatibleWith(rhs) ? rhs.Value() : invalid);
//  }

  public int setto(String textValue){
    //+++remove susceptibility to leading and trailing whitespace.
    int i;
    for(i=numValues();--i>invalid;){ //reverse search to reduce calls to numValues
      if(text(i).equals(textValue)){
        break;
      }
    }
    return value=i; //will be 'invalid' if text is not found
  }


  public TrueEnum(){
    value=invalid;
  }

  public TrueEnum(int rawValue){
    value=setto(rawValue);
  }

  public TrueEnum(String textValue){
    value=setto(textValue);
  }

  // for the new "combined" enumerations; you can overload these if you want to do something special
  // but be sure to handle the "invalid" case
  public String text(int i){
    return getText().itemAt(Coerced(i));
  }

  protected static final boolean isEnumField(Field fld){
    int modbits=fld.getModifiers();
    return  Modifier.isFinal(modbits)
    && Modifier.isPublic(modbits)
    && Modifier.isStatic(modbits)
    && fld.getType() == int.class ;
  }

  protected static final TextList nameVector(Class extension) {
    try {
      Field[] f=extension.getFields();
      TextList t=new TextList(f.length+1);
      int num;
      String name;
      for(int i = f.length; i-->0;) {
        Field fld = f[i];
        if(isEnumField(fld)){
          num = fld.getInt(null);
          name = fld.getName();
//            dbg.ERROR("found " + name + "=" + num);
          if(num >= t.size()) {
            t.setSize(num+1);
          }
          if(t.contains(name)) {
//            dbg.ERROR(name + " is a duplicate entry.");
          } else {
            t.set(num,name);
          }
        }
      }
      // +++ what to do about pockets?  For now we fill them in:
      for(int i = t.size(); i-->0;) {
        if(t.itemAt(i) == null) {
          t.set(i,invalidStr);
        }
      }
      return t;
    } catch (Exception e) {
//      dbg.Caught(e);
      return new TextList();
    }
  }

  public String toSpam() {
    return toString() + "/{" + dump("").asParagraph(" | ") + "}";
  }

  // for debugging:
  public /*static*/ void dump() {
//    dbg.logArray( LogSwitch.VERBOSE, this.getClass().getName(), getMyText().toArray()); // --- why is this commented?
  }

  public TextList dump(String prefix) {
    TextList tl = getMyText();
    TextList retval = new TextList();
    prefix = Safe.TrivialDefault(prefix, "");
    Fstring formatter = new Fstring((int)(((1.0*numValues())/10.0)+.5),' ');
    for(int i = 0; i < numValues(); i++){//in ascending order for clarity
      formatter.righted(""+i);
      String test = prefix + formatter + ": " + tl.itemAt(i);
      retval.add(test);
    }
    return retval;
  }

  // for testing
  public static String Usage() {
    return "not implemented";
  }

  public static void Test(String[] args) {
//    ErrorLogStream.Console(LogSwitch.VERBOSE);
    ErrorLogStream.Debug.ERROR("TrueEnum.Test not implemented");
  }

}

//$Id: TrueEnum.java,v 1.39 2001/07/19 01:06:55 mattm Exp $
