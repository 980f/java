/**
* Title:        TailFilter
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TailFilter.java,v 1.1 2000/08/06 19:05:47 andyh Exp $
*/
package net.paymate.util;
import  java.io.FileFilter;
import  java.io.File;

public class TailFilter implements FileFilter {
  protected String myFilter = null;

  public boolean accept(File pathName) {
    return (myFilter != null) && pathName.isFile() && pathName.getName().endsWith(myFilter);
  }

  public TailFilter(String filter) {
    myFilter = filter;
  }

}
//$Id: TailFilter.java,v 1.1 2000/08/06 19:05:47 andyh Exp $
