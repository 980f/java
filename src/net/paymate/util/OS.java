/**
* Title:        OS
* Description:  os dependent information, manager thereof.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: OS.java,v 1.14 2001/11/17 00:38:35 andyh Exp $
*/
package net.paymate.util;
import net.paymate.Main;
import net.paymate.util.*;

import java.io.File;
import java.util.*;

public class OS extends OsEnum {

  OS() {
    String oser = Safe.replace(Main.props("os").getString("name"), " ", "");
    for(int osi=numValues();osi-->0;){
      if (oser.indexOf(TextFor(osi)) != -1){
        setto(osi);
        return;
      }
    }
    System.out.println("Guessing Linux, Unknown OS:"+oser);
    setto(Linux);
  }

  public static final OS os = new OS();//class contains this one instance of itself

  ////////////////////////////////
  // smart questions

  public static final boolean isLinux() {
    return os.is(OsEnum.Linux);
  }

  public static final boolean isWindows() {
    return os.is(OsEnum.Windows); // really just 9X
  }

  public static final boolean isNT() {
    return os.is(OsEnum.NT) || os.is(OsEnum.Windows2000); // includes Win2K, in behaviour
  }

  public static final boolean isWin2K() {
    return os.is(OsEnum.Windows2000);
  }

  public static final boolean isSolaris() {
    return os.is(OsEnum.SunOS);
  }

  public static final boolean isUnish(){
    return isLinux()|| isSolaris();
  }

  public static final boolean isEmbedded(){
    return isLinux();//at present we are detecting only our own brand of linux
  }

  ///////////////////////
  // extra crap

  public static final String TempRoot(){
    return isUnish()? "/tmp":"C:\\paymate.tmp";
  }

  public static final File TempFile(String particular){
    return new File(TempRoot(),particular);
  }

  public static final void setClock(Date now){
    switch(os.Value()){
      case OsEnum.Linux: {
        LocalTimeFormat busybox= LocalTimeFormat.Utc("MMddHHmmyyyy");//seconds were getting ignored so we dropped them
        Executor.runProcess("date -s -u "+busybox.format(now),"fixing clock",0,0,null,false);
      } break;
    }
  }

///////////////////////
// tester
  public static String Usage() {
    return "no args";
  }

  public static void Test(String[] args) {
    // ignore args
    System.out.println("System.getProperty(\"os.name\") = " + System.getProperty("os.name"));
    System.out.println("OS test = " + os.Image());
  }

}
//$Id: OS.java,v 1.14 2001/11/17 00:38:35 andyh Exp $
