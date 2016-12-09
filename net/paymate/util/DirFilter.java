/**
* Title:        DirFilter
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DirFilter.java,v 1.1 2000/08/06 19:05:47 andyh Exp $
*/
package net.paymate.util;
import  java.io.FileFilter;
import  java.io.File;

public class DirFilter implements FileFilter {
  public boolean accept(File pathName) {
    return pathName.isDirectory();
  }

}
//$Id: DirFilter.java,v 1.1 2000/08/06 19:05:47 andyh Exp $
