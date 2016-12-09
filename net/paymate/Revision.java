package net.paymate;

/**
 * Title:        $Source: /cvs/src/net/paymate/Revision.java,v $
 * Description:  revision of terminal client
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version      $Revision: 1.5 $
 */

import net.paymate.util.*;
import java.util.Calendar;

public class Revision {
  public static String stripped(String tagged){
    int first=tagged.indexOf(':');
    if(first>=0){
      int second=tagged.indexOf('$',first);
      if(second>=0){ //remove the single spaces on either side
        tagged=tagged.substring(++first,--second);
      }
    }
    return tagged.trim();
  }

  public static String Buildid(){
    return stripped("$Name:  $");
  }

  public static String Rev(){
    return stripped("$Revision: 1.5 $");
  }

  public static long jarSize() {
    long fsize = 0;
    for(int i = 0; (fsize == 0) && (i < 4); i++) {
      String filename = "";
      switch(i) {
        case 0: {
          filename = "paymate.jar";
        } break;
        case 1: {
          filename = System.getProperty("user.dir") + System.getProperty("file.separator") + "paymate.jar";
        } break;
        case 2: {
          filename = System.getProperty("java.ext.dirs") + System.getProperty("file.separator") + "paymate.jar";
        } break;
        case 3: {
          if(System.getProperty("java.class.path").indexOf("paymate.jar") > -1) {
            filename = System.getProperty("java.class.path");
          }
        } break;
        default: break;
      }
      fsize = Safe.fileSize(filename);
    }
    return fsize;
  }

  public static final String WIPSTR = "Wip.";

  public static String Version(){
    return Safe.OnTrivial(Buildid(), WIPSTR+jarSize());
  }

  public static String CopyRight(){
    return CopyRight("(c)");
  }

  public static final String CORPORATION   = "PayMate.net Corporation";

  // (c) 2000 PayMate.net Corporation.  All rights reserved.
  // the copyright is a link to a copyright page (legal notices)
  public static final String CopyRight(String copyrightSymbol) {
//    return "(C) 2000-2001 PayMate.Net corporation";
    Calendar c = Calendar.getInstance();
    int STARTYEAR = 2000;
    int year = c.get(Calendar.YEAR);
    int diff = year - STARTYEAR;
    String years = "" + STARTYEAR + ((diff == 0) ? "" : ("-" + year));
    return "Copyright " + copyrightSymbol + " " + years + " " +
      CORPORATION + ".  All rights reserved.";
  }

  /**
   * command line dump
   */
  public static void main(String argv[]){
    System.out.println();
    String main=Revision.class.getPackage().toString();
    System.out.println("Application "+main.substring(main.indexOf(" ")+1));
    System.out.println("Version "+Rev()+' '+Buildid());//after stripped finally worked we need to restore some space.
    System.out.println(CopyRight());
  }

}
