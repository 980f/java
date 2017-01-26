package net.paymate.awtx;
/**
* Title:        $Source: /cvs/src/net/paymate/awtx/WordyInputStream.java,v $
* Description:  an input stream wrapper with word and other reader functions
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: WordyInputStream.java,v 1.14 2003/12/08 22:45:40 mattm Exp $
*/

import net.paymate.util.*;
import net.paymate.io.NiceInputStream;
import net.paymate.awtx.*;

import java.io.InputStream;
import java.io.ByteArrayInputStream;


/**
 * fail easy input stream reader.
 * reading past end of stream gets "MathX.INVALIDINTEGER" rather than exceptions
 * explicitly check for end of stream, don't rely upon exceptions.
 */
public class WordyInputStream extends NiceInputStream {
  //gratuitous constructors.
  public WordyInputStream(byte [] data,boolean msbfirst){
    super(data,msbfirst);
  }

  public WordyInputStream (InputStream is,boolean msbfirst){
    super(is,msbfirst);
  }

  public WordyInputStream (InputStream is){
    super(is);
  }
/////////////////
// additional types used by awtx users:

  public XDimension Xdimension() throws java.io.IOException {
    return new XDimension(u16(),u16());
  }

  public XPoint point() throws java.io.IOException {
    return new XPoint(u16(),u16());
  }

  public XPoint pointYX() throws java.io.IOException {
    int Y=u16();
    int X=u16();
    return new XPoint(X,Y);
  }

}

//$Id: WordyInputStream.java,v 1.14 2003/12/08 22:45:40 mattm Exp $
