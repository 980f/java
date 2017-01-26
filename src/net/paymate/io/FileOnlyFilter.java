package net.paymate.io;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/io/FileOnlyFilter.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import  java.io.FileFilter;
import  java.io.File;

public class FileOnlyFilter implements FileFilter {
  public boolean accept(File pathName) {
    return pathName.isFile();
  }

}


