package net.paymate.io;

import java.io.FilterOutputStream;

/**
 * Title:        $Source: /cvs/src/net/paymate/io/CSVOutStream.java,v $
 * Description:  add quotes and commas on the fly
 *                use universal eol of '\n', not platform specific
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import java.io.*;

public class CSVOutStream extends FilterOutputStream {

  boolean linestarted=false;
  public int EOL='\n'; //do NOT use system dependent info, use internet defaults.

  public void write(int b) throws IOException {
    if(b==EOL){
      linestarted=false;
    }
    super.write(b);
  }

  CSVOutStream write(String processed,boolean quoteit) throws IOException {
    if(linestarted){
      write(',');
    } else {
      linestarted=true;
    }
    if(quoteit){
      write('"');
    }
    write(processed.getBytes());
    if(quoteit){
      write('"');
    }
    return this;
  }

  public CSVOutStream flushLine()throws IOException {
    write(EOL);
    super.flush();
    return this;
  }

  public CSVOutStream append(String s)throws IOException {
    return write(s,true); //quote all strings
  }

  public CSVOutStream append(int eye)throws IOException {
    return write(Integer.toString(eye),false);
  }

  public CSVOutStream append(long ell)throws IOException {
    return write(Long.toString(ell),false);
  }

/**
 * convert embedded special characters to \ escape codes before output.
 */
//  public CSVOutStream escape(String s){
//    write(s,true); //quote all strings
//  }

  public CSVOutStream(OutputStream out) {
    super(out);
  }

}
//$Id: CSVOutStream.java,v 1.1 2003/07/27 19:36:53 mattm Exp $