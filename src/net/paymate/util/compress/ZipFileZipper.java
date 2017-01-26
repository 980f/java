package net.paymate.util.compress;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/compress/ZipFileZipper.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.io.*;
import java.util.zip.*;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.TextList;
import net.paymate.io.Streamer;
import net.paymate.io.IOX;
import net.paymate.lang.StringX;

public class ZipFileZipper {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ZipFileZipper.class, ErrorLogStream.VERBOSE);

  String outputFilename = null;
  String [ ] filesToInsert = null;

  // note: the outputFilename must not exist already!
  public ZipFileZipper(String outputFilename, String [ ] filesToInsert) {
    this.outputFilename = outputFilename;
    this.filesToInsert = filesToInsert;
  }

  public boolean zip() {
    return zip(outputFilename, filesToInsert);
  }

  private static final boolean zip(String outputFilename, String [ ] filesToInsert) {
    if(filesToInsert.length < 1) {
      dbg.ERROR("Zipfile must have at least one entry!");
    } else {
      if (StringX.NonTrivial(outputFilename)) {
        File out = new File(outputFilename);
        if (out.exists()) {
          dbg.ERROR("Cannot overwrite a file that exists!");
        } else {
          FileOutputStream fos = null;
          BufferedOutputStream bos = null;
          ZipOutputStream zout = null;
          try {
            out.createNewFile(); // +++ check return result!
            fos = new FileOutputStream(out);
            bos = new BufferedOutputStream(fos);
            zout = new ZipOutputStream(bos);
            zout.setLevel(9); // +++ parameterize
            for (int i = 0; i < filesToInsert.length; i++) {
              String filename = filesToInsert[i];
              dbg.VERBOSE("Adding: " + filename);
              // name the entry
              ZipEntry entry = new ZipEntry(filename);
              // insert it into the file
              zout.putNextEntry(entry);
              // write data to the entry
              FileInputStream fis = new FileInputStream(filename);
              Streamer.Buffered(fis, zout);
              // close the entry
              zout.closeEntry();
              dbg.VERBOSE("Done: " + filename + ".");
            }
            // try to close them here, as the ZipOutputStream does some checking on close
            IOX.Close(zout);
            IOX.Close(bos);
            IOX.Close(fos);
            dbg.WARNING("Successfully wrote: " + outputFilename);
            return true;
          } catch (Exception ex) {
            dbg.Caught(ex);
          } finally {
            // this may or may not be redundant
            IOX.Close(zout);
            IOX.Close(bos);
            IOX.Close(fos);
          }
        }
      } else {
        dbg.ERROR("Outputfilename is trivial!");
      }
    }
    return false;
  }

  public static final void main(String [] args) {
    // +++ do Main.logging stuff here and replace the System.out stuff with dbg's.
    // arg0 is the output file, and must NOT exist
    // arg1-argN are the files to put in it
    // we will use the paths just like they are given to us
    if(args.length < 1) {
      System.out.println("ZipFileZipper usage: net.paymate.util.ZipFileZipper outputFilename [ infile1 [ ... infileN ] ]");
    } else {
      TextList tl = new TextList(args);
      String outputFilename = tl.remove(0); // take the output file name out of the input file list
      System.out.println("Zipping '"+outputFilename+"' from: " + tl.toString());
      ZipFileZipper zipper = new ZipFileZipper(outputFilename, tl.toStringArray());
      System.out.println("Zipping returned: " + zipper.zip());
    }
  }

}