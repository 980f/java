/**
* Title:        TailFilter
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TailFilter.java,v 1.2 2003/11/09 21:11:26 mattm Exp $
*/
package net.paymate.io;
import  java.io.FileFilter;
import  java.io.File;

public class TailFilter implements FileFilter {
  // +++ make a version of this that accepts an array of extensions
  protected String myFilter = null;

  public boolean accept(File pathName) {
    // +_+ case sensitive?
    return (myFilter != null) && pathName.isFile() &&
        pathName.getName().endsWith(myFilter);
  }

  public TailFilter(String filter) {
    myFilter = filter;
  }

}
//$Id: TailFilter.java,v 1.2 2003/11/09 21:11:26 mattm Exp $
