package net.paymate.lang;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/lang/Enum.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

// DEFECTIVE.  See bottom of file to find out why!

import java.lang.reflect.*;  // use reflection to associated text with enumerated values.
import java.util.Hashtable;  // used to store text and char lists
import net.paymate.lang.Fstring;

public class Enum {
  // static finals: can't be changed at all
  private final static int invalid =ObjectX.INVALIDINDEX; // if this is public, it will get used!!!!
  protected final static String invalidStr = "BAD CHOICE";//!!!! this text makes it to the clerk interface for undefined keys...
  protected final static char invalidChar = ' ';

  // statics: set only at class load time!
  private static final Hashtable nameLists = new Hashtable();
  private static final Hashtable charLists = new Hashtable();

  // statics: can be set at class load time
  private String [ ] names; // long names
  private char [] chars; // must be constructed with onClassLoad().  See EnumTest class for an example.  This also determines the numValues!

  // instances: can be set at class instantiation time
  protected int value=invalid; // set to invalid here

  public Enum() {
    loadInstance(this.getClass()); // which class will this be, Enum or its extension?
  }

  // Can't do these kinds of constructors due to the way we load the chars and names
//  public Enum() {
//    this(invalid);
//  }
//  public Enum(String textValue) {
//    this(setto(textValue));
//  }
//  public Enum(int rawValue) {
//    value = setto(rawValue);
//  }

  public final int numValues() {
    return chars.length;
  }

  public int Value(){
    return value;
  }

  public char Char() {
    return (Coerced(value) == invalid) ? invalidChar : chars[value];
  }

  public void Clear(){
    value=invalid;
  }

  public static final int Invalid(){//public access fubar'd the symbol extraction stuff.
    return invalid;
  }

  protected final String [ ] getText() {
    return names;
  }

  // makes a copy for safety reasons
  protected final char [ ] getChars() {
    char [ ] outchars = new char[chars.length];
    System.arraycopy(chars, 0, outchars, 0, chars.length);
    return outchars;
  }

  public /*static */ boolean Legal(int rawValue){
    return rawValue>invalid && rawValue<chars.length;
  }

  public int Coerced(int rawValue){
    return Legal(rawValue) ? rawValue : Invalid();
  }

  public String TextFor(int rawValue){
    int newVal = Coerced(rawValue);
    return (newVal == invalid) ? invalidStr : text(newVal);
  }

  public boolean isLegal(){
    return Legal(value);
  }

  public static boolean IsLegal(Enum probate){
    return probate!=null && probate.isLegal();
  }

/**
 * @return whether @param rawValue matches internal value.
 * <br>If both are invalid then even if they are different this returns true.
 */
  public boolean is(int rawValue){
    return value == Coerced(rawValue);
  }

  public String Image(){
    return TextFor(value);
  }

  public String toString(){
    return TextFor(value)+"["+value+"]";
  }

  /**
   * @return "3: option3"
   */
  public String menuImage(){
    return String.valueOf(value)+":"+Image();
  }

  public int setto(int rawValue){
    return value= Coerced(rawValue);
  }

  public int setto(char rawValue){
    int ret = invalid;
    for(int i = chars.length; i-->0;) {
      if(chars[i] == rawValue) {
        ret = i;
        break;
      }
    }
    return value=Coerced(ret);
  }

  public int setto(String textValue){
    if(StringX.NonTrivial(textValue)) {
      textValue = textValue.trim();
    }
    return value=indexOf(textValue, getText());//#inline assign
  }

  /**
   * @return whether this and the other exist and are of the same actual class and have the same numerical value.
   * NOTE that the other.getClass()== this.getClass() will always return true, REGARDLESS!
   */
  public boolean equals(Enum other){
    return other!=null && (other.getClass()== this.getClass()) && other.value==this.value ;
  }

  //+++remove susceptibility to leading and trailing whitespace.
  protected static final int indexOf(String textValue, String [ ] tl){
    for(int i = tl.length; i-->0;) {
      if(StringX.equalStrings(tl[i], textValue)) {
         return i;
      }
    }
    return invalid;
  }

  // for the new "combined" enumerations; you can overload these if you want to do something special
  // but be sure to handle the "invalid" case
  public String text(int i){
    return getText()[Coerced(i)];
  }

  /// used by text array builder.
  private static final boolean isEnumField(Field fld){
    int modbits=fld.getModifiers();
    return /* Modifier.isFinal(modbits) &&  -> can't use final or else can't set them at class load time! */
    Modifier.isPublic(modbits)
    && Modifier.isStatic(modbits)
    && fld.getType() == int.class ;
  }

  private final void loadInstance(Class extension) {
    Object temp = nameLists.get(extension.getName());
    if(temp == null) {
      names = new String[0];
    } else {
      names = (String [ ])temp;
    }
    temp = charLists.get(extension.getName());
    if(temp == null) {
      chars = new char[0];
    } else {
      chars = (char [ ])temp;
    }
  }

  // ONLY ONLY ONLY call this from a static{ } constructor in an extension !!!
  protected static final synchronized void onClassload(Class extension) {
    if (extension == null) {
      System.out.println("Not running onClassLoad since extension is null!");
      return;
    }
    String classname = extension.getName();
    StringBuffer toBeChars = new StringBuffer();
    try {
      System.out.println("running onClassLoad for class " + classname);
      Field[] f = extension.getFields();
      String[] t = new String[f.length + 1];
      int num;
      int THElength = 0;
      String name;
      for (int i = f.length; i-- > 0; ) {
        Field fld = f[i];
        if (isEnumField(fld)) {
          num = fld.getInt(null);
          THElength = Math.max(num + 1, THElength);
          name = fld.getName();
          int len = toBeChars.length();
          if (THElength >= t.length) {
            String t2[] = new String[THElength];
            System.arraycopy(t, 0, t2, 0, t.length);
            t = t2;
            String cat = StringX.fill("", ' ', num + 1 - len, false);
            toBeChars.append(cat);
          }
          char unique = uniqueChar(toBeChars, num, name);
          toBeChars.setCharAt(num, unique);
          fld.setInt(extension, num);
          boolean found = false;
          for (int j = t.length; j-- > 0; ) {
            if (StringX.equalStrings(name, t[j], false)) {
              found = true;
              break;
            }
          }
          if (found) {
            // skip
          } else {
            t[num] = name;
          }
        }
      }
      // +++ what to do about pockets?  For now we fill them in:
      for (int i = t.length; i-- > 0; ) {
        if (t[i] == null) {
          t[i] = invalidStr;
        }
      }
      // now, return the appropriate subset if needed
      if (THElength < t.length) {
        String t2[] = new String[THElength];
        System.arraycopy(t, 0, t2, 0, THElength);
        t = t2;
      }
      // set the chars list for this class
      char[] chars = toBeChars.toString().toCharArray();
      charLists.put(classname, chars);
      nameLists.put(classname, t);
      System.out.println("EnumTest loaded.\n" +
                         "numvalues = " + chars.length +
                         "\nand chars are:" +
                         new String(chars).getBytes() +
                         "\nand names are:\n" + t);

    }
    catch (Exception e) {
      System.out.println(classname + " load error! " + e + "\n" +
                         e.fillInStackTrace());
    }
  }

  // this limits our selection of Enum size to 26 [or 36]
  private static final char uniqueChar(StringBuffer bytes, int ignore, String name) {
    char bite = 0;
    bite = uniqueChar(name.toUpperCase(), bytes, ignore);
    if(bite == 0) {
      // find any character
      bite = uniqueChar("ABCDEFGHIJKLMNOPQRSTUVWXYZ", bytes, ignore);
    }
    if(bite == 0) {
      // find any number
      bite = uniqueChar("0123456789", bytes, ignore);
    }
    if(bite == 0) {
      // give up
    }
    return bite;
  }

  private static final char uniqueChar(String from, StringBuffer into, int ignore) {
//    System.out.println("Running uniqueChar: from="+from+", into="+into+", ignore="+ignore);
    char bite = 0;
    for(int prospecti = 0; prospecti < from.length(); prospecti++) {
      char prospect = from.charAt(prospecti);
      boolean found = false;
      for(int i = 0; i < into.length(); i++) {
        if((i != ignore)  && (into.charAt(i) == prospect)) {
          found = true;
          break;
        }
      }
      if(!found) {
        bite = prospect;
        break;
      }
    }
    return bite;
  }

  public String toSpam() {
    return toString() + "/{" + dump("", " | ") + "}";
  }

  public String dump(String prefix, String divider) {
    StringBuffer retval = new StringBuffer();
    prefix = StringX.TrivialDefault(prefix, "");
    divider = StringX.TrivialDefault(divider, "");
    Fstring formatter = new Fstring((int)(((1.0*chars.length)/10.0)+.5),' ');
    for(int i = 0; i < chars.length; i++){//in ascending order for clarity
      formatter.righted(""+i);
      String test = prefix + formatter + ": " + names[i] + divider;
      retval.append(test);
    }
    return retval.toString();
  }

  public static Enum Clone(Enum rhs){
    try {
      Enum newone= (Enum) rhs.getClass().newInstance();
      newone.value=rhs.value;
      return newone;
    }
    catch (Exception ex) {
      return null;
    }
  }

  /**
   * this guy violates the spirit of Enum by allowing decimal strings to be used
   * to set a value. this is very useful for our debuggers.
   */
  public static Enum makeEnum(Enum prototype,String value){
    if(prototype instanceof Enum){
      Enum newone= Enum.Clone(prototype);
      if(StringX.NonTrivial(value)){
        newone.setto(value);
        if(!newone.isLegal()){
          int pick=StringX.parseInt(value);
          newone.setto(pick);
        }
      }
      return newone;
    } else {
      return null;
    }
  }

  public static final boolean test2ataTime(Class class1, Class class2) {
    if(test(class1) && test(class2)) {
      System.out.println("\nEverything tested okay!\n");
      return true;
    } else {
      System.out.println("\nERRORS ERRORS ERRORS !!!\n");
      return false;
    }
  }

  public static final boolean test(Class enumclass) {
    int errors = 0;
    try {
      Enum enumTest = (Enum) enumclass.newInstance();
      Enum enumTest2 = (Enum) enumclass.newInstance();
      System.out.println("Default constructor gives:\n" + enumTest);
      System.out.println("\nRunning int/image tests...\n");
      for (int i = enumTest.numValues(); i-- > 0; ) {
        enumTest.setto(i);
        System.out.println("setto " + i + " gives["+enumTest.Char()+"]: " + enumTest);
        enumTest2.setto(enumTest.Image());
        System.out.println("setto " + enumTest.Image() + " gives["+enumTest2.Char()+"]: " + enumTest2);
        if ( (i == enumTest.Value()) && (i == enumTest2.Value())) {
          System.out.println("TESTOK");
        } else {
          System.out.println("\n ERROR ERROR ERROR !!! \n\n");
          errors++;
        }
      }
      System.out.println("\nRunning int/char tests...\n");
      for (int i = enumTest.numValues(); i-- > 0; ) {
        enumTest.setto(i);
        System.out.println("setto " + i + " gives["+enumTest.Char()+"]: " + enumTest);
        enumTest2.setto(enumTest.Char());
        System.out.println("setto " + enumTest.Image() + " gives["+enumTest2.Char()+"]: " + enumTest2);
        if ( (i == enumTest.Value()) && (i == enumTest2.Value())) {
          System.out.println("TESTOK");
        } else {
          System.out.println("\n ERROR ERROR ERROR !!! \n\n");
          errors++;
        }
      }
    } catch (Exception ex) {
      System.out.println("Error: " + ex);
    } finally {
      return errors == 0;
    }
  }

  public static final void main(String [ ] args) {
    test2ataTime(EnumTest.class, SecondEnumTest.class);
    switch(0) {
// these won't compile since the value of the int must be known at compile time!!!!!!
// AAAAAAAAAAAAAAAAAAAAAAAAAAAAARRRRRRRRRRRRRRRRRRRRGGGGGGGGGGGGGHHHHHHHHHHH!!!!!!!!!
//      case EnumTest.testA: {
//
//      } break;
//      case EnumTest.testC: {
//
//      } break;
    }
  }
}

class EnumTest extends Enum {
  // this doesn't work either:
  //   protected static int start=-1;
  //   public static final int testA=start++;
  public static int testA;
  public static int test2;
  public static int testC;
  public static int test4;
  public static int testE;
  public static int gobbledyGook;
  public static int other;
  static {
    onClassload(EnumTest.class);
  }


}

class SecondEnumTest extends Enum {
  public static int twoA;
  public static int two2;
  public static int twoC;
  public static int two4;
  public static int twoE;
  public static int hocusPocus;
  public static int general;
  static {
    onClassload(SecondEnumTest.class);
  }
}
