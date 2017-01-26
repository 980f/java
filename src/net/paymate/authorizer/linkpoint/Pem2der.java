package net.paymate.authorizer.linkpoint;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/Pem2der.java,v $
 * Description:  convert human readable cert / key data into binary
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 * @todo: take in a Comparator for the begin and end delimiters.
 * that comparator can return <0 for begin, >0 for end, =0 for everything else.
 */

import java.io.*;
import sun.misc.BASE64Decoder;

public class Pem2der {

  /**
   * convert human readable @param pem stream into binary @param der stream.
   * @param started if false makes this guy look for "^--*BEGIN*--$" and following "^--*$"
   * delimiters. the actual regexp for the above do not include the quotes.
   */
  public static void Pem2Der(InputStream pem,OutputStream der,boolean started) throws java.io.IOException {
    BufferedReader is = new BufferedReader(new InputStreamReader(pem));
    StringBuffer pemData = new StringBuffer();
    String line = null;
    while((line = is.readLine()) != null) {
      line=line.trim(); //removes crlf debris as well as blanks
      if(line.length() == 0) {
        continue;
      }
      boolean nondata=line.startsWith("--");
      if(nondata){
        if(started) {
          break;
        } else if (line.indexOf("BEGIN", 0) != -1) {
          started = true;
        }
      } else if(started){
        pemData.append(line);
      }
    }
    der.write((new BASE64Decoder()).decodeBuffer(pemData.toString()));
  }

  public static void main(String arg[]) {
    try {
      if ((arg.length > 1) || ((arg.length == 1) && (!arg[0].equals("-r")))) {
        System.err.println("Usage: java net.paymate.authorizer.linkpoint.Pem2der [-r] < file.pem > file.der");
        System.err.println("  -r means raw, i.e., no BEGIN/END lines");
        return;
      }
      boolean started=(arg.length>0);
      Pem2Der(System.in,System.out,started);
      System.out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
//$Id: Pem2der.java,v 1.4 2003/03/24 19:00:27 mattm Exp $
