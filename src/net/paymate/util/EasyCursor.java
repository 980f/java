package net.paymate.util;

/**
* Title:
* Description:  for blocks, prefix the block with "<blockname>."
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: andyh $
* @version $Id: EasyCursor.java,v 1.16 2001/07/21 03:19:40 andyh Exp $
* @todo: <ol>
* <li>implement rootedness for branches. Present context is doubling as root and cache
* </ol>
*/
import java.util.*;
import java.io.*;

public class EasyCursor extends EasyProperties {
  static final ErrorLogStream dbg=new ErrorLogStream(EasyCursor.class.getName());
  protected String context="";
  protected StringStack tree=new StringStack();

  public final static String SEP=".";

  public static final String makeKey(String morekey){
    return Safe.NonTrivial(morekey)? morekey+SEP:""  ;
  }

  public EasyCursor setKey(String newcontext){
    if(newcontext!=null){//but it is OK to be trivial
      if(tree.isEmpty()){
        context=newcontext;
      } else {//ez way to replace last segment of context:
        pop().push(newcontext);
      }
    }
    return this;
  }

  public EasyCursor push(String more){
    tree.push(context);
    if(Safe.NonTrivial(more)){//double covered
      context +=makeKey(more);
    }
    return this;
  }

  public EasyCursor pop(){//users should put this in finally clauses if exceptions can throw
    this.context= tree.isEmpty()? "" : tree.pop();
    return this;
  }

  public EasyCursor Reset(){
    tree.Clear();
    context="";
    return this;
  }

  public String preFix(){//not the same as what was set with setKey...
    return this.context;
  }

  public static final EasyCursor New(InputStream is){
    EasyCursor newone=new EasyCursor();
    newone.Load(is);
    return newone;
  }

  public EasyCursor(){
    super();
  }

  public EasyCursor(Properties rhs){
    super(rhs);
  }

//  /**
//  * new cursor with same properties and STARTING with same context but no stack
//  */
//  private EasyCursor(EasyCursor rhs){
//    //not working!
//  }

  public EasyCursor(String context,Properties rhs){
    super(rhs);
    setKey(context);
  }

  private EasyCursor(String context,EasyCursor rhs){
    //not yet figured out.

  }

  public EasyCursor(String context,String from){
    super(from);
    setKey(context);
  }

  public EasyCursor(String from){
    super(from);
    //    this.fromString(from, false /* not needed */);
  }


  public String fullKey(String key){
    boolean rooted= Safe.NonTrivial(key)? key.charAt(0)=='.':false;
    return (!rooted&&Safe.NonTrivial(context))?(context+key):key;
  }

  final static String nullString="(null)";
  /**
  * overloading the get and set property is all we need to modify as they are
  * religiously used within all other gets and sets.
  */
  public Object setProperty(String key, String newValue){
    // remove property, java.uti.Properties doesn't allow for null properties.
    return (newValue==null)?remove(key):super.setProperty(fullKey(key),newValue);
  }

  public String getProperty(String key){
    return Safe.NonTrivial(key)? super.getProperty(fullKey(key)):"";
  }

  /**
  * @undeprecated, seem to have fixed via underlying class works under jre 1.3, fails under 1.2.2-L
  * @return an EasyProperties that is a subset of this one
  */
  public EasyCursor EasyExtract(String context){//makes a real copy
    EasyProperties subset=new EasyProperties();
    //for each property, if it starts with context then cut that off and stick in subset
    if(context!=null){
      push(context);
    }
    try {
      String fullkey= preFix();     //FUE
      int clipper=fullkey.length(); //FUE
      //propertyNames gives full length names
      for(Enumeration enump = propertyNames(); enump.hasMoreElements(); ) {
        String name = (String)enump.nextElement();
        if(name.startsWith(fullkey)) {
          try {
            String subkey=name.substring(clipper);
            subset.setString(subkey,getProperty(subkey));//inefficent, but works
          } catch(Exception oops){
            dbg.Caught("Extracting",oops);
            //presume that the item's key is defective...
            //continue with looking for other items.
          }
        }
      }
    } finally {
      if(context!=null){
        pop();
      }
      return new EasyCursor(subset);
    }
  }

//  public synchronized Enumeration keys() {
//  	return getEnumeration(KEYS);
//  }


  public String toSpam(){
    return preFix()+" has "+super.propertyList().size()+" items, defs has "+(defaults!=null?defaults.size():0);
  }
  /////////////////////////////////////
  // isEasy utilities
/**
 * @deprecated untested
 */
  public EasyCursor saveVector(String key,Vector v){
    int i=v.size();
    push(key);
    try {
      setInt("size",i);//even if zero.
      while(i-->0){
        push(Integer.toString(i));
        try {
          ((isEasy)v.elementAt(i)).save(this);
        }
        catch(Exception e){
          continue; //try to finish iteration.
        }
        finally {
          pop();
        }
      }
    }
    finally {
      return pop();
    }
  }

/**
 * @deprecated untested
 */
  public Vector loadVector(String key,Class act){
    Vector v=null;
      push(key);
      try {
        v=new Vector(getInt("size"));
        int i=v.size();
        while(i-->0){
          push(Integer.toString(i));
          try {
            Object newone=act.newInstance();
            ((isEasy)newone).load(this);
            v.set(i,newone);
          }
          catch(Exception e){
            continue; //try to finish iteration.
          }
          finally {
            pop();
          }
        }
      }
      finally {
        pop();
        return v;
      }
  }

  public static final EasyCursor makeFrom(isEasy object){
    EasyCursor spammy=new EasyCursor();
    object.save(spammy);
    return spammy;
  }

  /**
 * @undeprecated testing //untested
 */

  public static final String spam(isEasy object){
    return makeFrom(object).asParagraph();
  }
/**
 * @undeprecated untested
 */

  public EasyCursor getBlock(isEasy ezo,String key){
    push(key);
    ezo.load(this);
    return pop();
  }
/**
 * @undeprecated untested
 */
  public EasyCursor addBlock(isEasy ezo,String key){
    push(key);
    ezo.save(this);
    return pop();
  }

  public static EasyCursor FromDisk(File f){
//    dbg.VERBOSE("restoring from disk file:"+cacheFile().getAbsolutePath());
    EasyCursor fromdisk=new EasyCursor();
    try {
      fromdisk.Load(new FileInputStream(f));
    }
    catch(Exception ignored){
    }
    finally {
      return fromdisk;
    }
  }

}

//$Id: EasyCursor.java,v 1.16 2001/07/21 03:19:40 andyh Exp $
