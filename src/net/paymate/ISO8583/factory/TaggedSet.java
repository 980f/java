package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/TaggedSet.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.*;
import java.util.*;

public class TaggedSet {
  Vector set=new Vector(3);//content

  TagDef spec[];

  TagDef forTag(String tag){
    for(int i=spec.length;i-->0;){
      TagDef def=spec[i];
      if(def.tag.equals(tag)){
        return def;
      }
    }
    return TagDef.Invalid;
  }

  public TaggedSet set(TaggedField tf){
    for(int i=set.size();i-->0;){
      TaggedField existing= (TaggedField)set.elementAt(i);
      if(existing.tag.equals(tf.tag)){
        set.setElementAt(tf,i); //overwrite
        return this;
      }
    }
    //not yet present so:
    set.add(tf);
    return this;
  }

  public TaggedSet set(String tag,String content){
    return set(new TaggedField(tag,content));
  }

  public String get(String tag,String onNotFound){
    for(int i=set.size();i-->0;){
      TaggedField tf= (TaggedField)set.elementAt(i);
      if(tf.tag.equals(tag)){
        return tf.content;
      }
    }
    return onNotFound;
  }

  public String get(String tag){
    return get(tag,"");
  }

    /**
   * all fields packed into the same "bit" must have the same lls
   */
  public TaggedSet parse(String msgfield){
    isoCursor ink=new isoCursor(msgfield,0);
    String twochar=ink.nextPiece(2);
    String content;

    while(ink.stillHave(2)){
      twochar=ink.nextPiece(2);
      TagDef fd=forTag(twochar);
      if(fd.isValid()){
        if(fd.isFixed()){//fixed length
          content=ink.nextPiece(fd.length);
        } else {
          //variableLength number of digits is a field that is length
          //of variable content that follows.
          int vlength= (int) ink.decimal(fd.variableLength);
          content=ink.nextPiece(vlength);
        }
        set(twochar,content);
      } else {
        //@@@ blow big time!!
        return null;
      }
    }
    return this;
  }

  /**
   * length with current content!
   */
  public int length(){
    int sum=0;
    int one=0;
    for(int i=set.size();i-->0;){
      TaggedField tf= (TaggedField)set.elementAt(i);
      TagDef fd= forTag(tf.tag);
      one=fd.lengthFor(tf.content);
      if(one!=Safe.INVALIDINTEGER){
        sum+=one;
      }
    }
    return sum;
  }

  public String forMessage(){
    StringBuffer sb=new StringBuffer();
    for(int i=set.size();i-->0;){
      TaggedField tf= (TaggedField)set.elementAt(i);
      TagDef fd= forTag(tf.tag);
      if(!fd.formatInto(sb,tf.content)){
       //bitch and moan;
      }
    }
    return sb.toString();
  }

/**
 * usage:
 * get length with length();
 * pack it into buffer using its variablelength formatting value
 * then call this.
 * @return passthrough.
 */
  public StringBuffer packInto(StringBuffer sb){
    for(int i=set.size();i-->0;){
      TaggedField tf= (TaggedField)set.elementAt(i);
      TagDef fd= forTag(tf.tag);
      if(!fd.formatInto(sb,tf.content)){
       //bitch and moan;
      }
    }
    return sb;
  }

  public TaggedSet( TagDef spec[]) {
    this.spec=spec;
  }

}