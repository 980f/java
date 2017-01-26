/**
* Title:
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: WordyOutputStream.java,v 1.1 2000/12/02 04:05:09 andyh Exp $
*/
package net.paymate.awtx;
//import net.paymate.util.ErrorLogStream;

import java.io.OutputStream;



public class WordyOutputStream {//will export later
  OutputStream wrapped;
  public WordyOutputStream (OutputStream os){
    wrapped=os;
  }

  public OutputStream os(){
    return wrapped;
  }

  public WordyOutputStream write(int datum) throws java.io.IOException {
    wrapped.write(datum);
    return this;
  }

  public WordyOutputStream word(int datum)throws java.io.IOException {
    return write(datum).write(datum>>8);
  }

  public WordyOutputStream dword(int datum)throws java.io.IOException {
    return write(datum).write(datum>>8).write(datum>>16).write(datum>>24);
  }

}
//$Id: WordyOutputStream.java,v 1.1 2000/12/02 04:05:09 andyh Exp $
