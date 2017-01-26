package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/AsciiBufferParser.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class AsciiBufferParser extends BufferParser {
  /**
   * get record as parsable buffer
   * @todo make byte[] version of getUntil() to make this more efficient.
   */
  public AsciiBuffer getRecord(){
    String record=getUntil(Ascii.RS);
    AsciiBuffer newone=AsciiBuffer.Newx(record.length());
    newone.append(record);
    return newone;
  }

  public String getROF(){//rest of frame
    return getUntil(Ascii.FS);
  }

  public String getPrefix(){
    return getUntil('.');
  }

  /**
 * @return variable length decimal integer
 * note: hexadecimal fields are always fixed size.
 */
  public long getDecimalFrame(){
    return StringX.parseLong(getROF());
  }
/**
 * @return new textlist made of remaining frames.
 */
  public TextList fields(){
    TextList fields=new TextList();
    while(parser<buffer.used()){//should be a method for this. gotMore()
      fields.add(getROF());
    }
    return fields;
  }

/**
 * @return a fastidious parser, all input must be perfect.
 */
  public static AsciiBufferParser Strict() {
    return new AsciiBufferParser();
  }
/**
 * @return a slack parser, one that tries to fixup short fields and such.
 */
  public static AsciiBufferParser Easy() {
    AsciiBufferParser newone= new AsciiBufferParser();
    newone.slack=true;
    return newone;
  }

}
//$Id: AsciiBufferParser.java,v 1.6 2003/10/01 04:23:44 andyh Exp $