package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/BufferParser.java,v $
 * Description:  parses an external buffer.
 *   if configured to be slack then all exceptions are stifled and "" is returned for strings
 *   if picky then either exceptions are thrown or nulls are returned for defective string access.
 *   as of this rev 1.3 the only exceptions possible are NPE's due to not attaching the parser to any buffer.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;

public class BufferParser {
  /**
   * parser is left pointing to the next unread byte.
   */
  int parser;
  Buffer buffer;

  //modes:
  public boolean slack=false; //slack data rule enforcement

  private String slackNull(){
    return slack? "" : null;
  }
/**
 * point parser at @param buffer
 */
  public BufferParser Start(Buffer buffer){
    this.buffer=buffer;
    parser=0;
    return this;
  }

  public BufferParser Start(String s){
    Buffer newone=Buffer.New(s.length());
    newone.append(s);
    return Start(newone);
  }

  /**
   * @return number of unparsed bytes.
   */
  public int remaining(){
    if(buffer!=null){
      return buffer.used()-parser;
    } else {
      return 0;
    }
  }

/**
 * @return value in 0..255 if byte exists, or MathX.INVALIDINTEGER if not.
 */
  public int getByte(){
    if(buffer!=null){
      return buffer.bight(parser++);
    } else {
      return MathX.INVALIDINTEGER;
    }
  }
  /**
   * @return value of next byte as char, CharX.INVALIDCHAR if not ascii.
   */
  public char getChar(){
    return Ascii.Char(getByte());
  }

  /**
   * @return fixed number of bytes converted to chars.
   */
  public String getFixed(int len) {
    try {
      if(buffer == null){
        return slackNull();
      }

      if(parser+len<buffer.used() || slack){
        return String.valueOf(buffer.subString(parser,len));
      } else {
        return slackNull();
      }
    }
    finally {
      parser+=len;//even when we have run off end, we run off it some more!
    }
  }

  /**
   * @return rest of bytes converted to chars.
   */
  public String getTail(){
    try {
      if(buffer == null){
        return slackNull();
      }
      return String.valueOf(buffer.subString(parser,remaining()));
    }
    finally {
      parser=buffer.used();
    }
  }

/**
 * @return integer from fixed number of decimal chars.
 */
  public int getDecimalInt(int len) {
    return StringX.parseInt(getFixed(len));
  }
/**
 * @return long integer from fixed number of decimal chars.
 */
  public long getDecimalLong(int len) {
    return StringX.parseLong(getFixed(len));
  }
  /** for reading encrypted PINs and the like.
   * @return a long from exactly 16 hex digits
   */
  public long getHex16() {
    return StringX.parseLong(getFixed(16),16);
  }
/**
 * @return bytes converted to chars until byte @param delim is found. delim is NOT included in returned string.
 */

  public String getUntil(int delim){
    int start=parser;
    while(parser<buffer.used()){
      if(delim == buffer.bight(parser++)){//not a string so we don't have indexOf()
        return String.valueOf(buffer.subString(start,parser-start-1));//-1 deletes delim from returned string
      }
    }
    return slack? "" : null;
  }
/**
 * @return a fastidious parser, all input must be perfect.
 */
  public static BufferParser Picky() {
    return new BufferParser();
  }
/**
 * @return a slack parser, one that tries to fixup short fields and such.
 */
  public static BufferParser Slack() {
    BufferParser newone= new BufferParser();
    newone.slack=true;
    return newone;
  }

}
//$Id: BufferParser.java,v 1.5 2003/07/27 05:34:56 mattm Exp $