package net.paymate.util.compress;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/compress/TarBuilder.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.compress.tar.*;
import java.io.*;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.TextList;
import net.paymate.io.IOX;
import net.paymate.lang.StringX;

// eg: parameters: /home/mattm/testoutfile1.tar com/ice/tar/tar.java com/ice/tar/package.html
public class TarBuilder {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TarBuilder.class, ErrorLogStream.VERBOSE);

  String outputFilename = null;
  String [ ] filesToInsert = null;

  // note: the outputFilename must not exist already!
  public TarBuilder(String outputFilename, String [ ] filesToInsert) {
    this.outputFilename = outputFilename;
    this.filesToInsert = filesToInsert;
  }

  public boolean tar() {
    return tar(outputFilename, filesToInsert);
  }

  private static final boolean tar(String outputFilename, String [ ] filesToInsert) {
    if(filesToInsert.length < 1) {
      dbg.ERROR("TARfile must have at least one entry!");
    } else {
      if (StringX.NonTrivial(outputFilename)) {
        File out = new File(outputFilename);
        if (out.exists()) {
          dbg.ERROR("Cannot overwrite a file that exists!");
        } else {
          FileOutputStream fos = null;
          BufferedOutputStream bos = null;
          try {
            out.createNewFile(); // +++ check return result!
            fos = new FileOutputStream(out);
            bos = new BufferedOutputStream(fos);
            TarArchive archive = new TarArchive(bos);
            archive.setDebug(false); // +++ parameterize
            archive.setVerbose(false); // +++ parameterize
            archive.setTarProgressDisplay(null); // +++ use this eventually
            archive.setKeepOldFiles(false); // +++ parameterize
            for (int i = 0; i < filesToInsert.length; i++) {
              String filename = filesToInsert[i];
              dbg.VERBOSE("Adding: " + filename);
              File f = new File(filename);
              TarEntry entry = new TarEntry(f);
              archive.writeEntry(entry, true);
              dbg.VERBOSE("Done: " + filename + ".");
            }
            archive.closeArchive();
            // try to close them here, as the TarOutputStream might do some checking on close
            IOX.Close(bos);
            IOX.Close(fos);
            dbg.WARNING("Successfully wrote: " + outputFilename);
            return true;
          } catch (Exception ex) {
            dbg.Caught(ex);
          } finally {
            // this may or may not be redundant
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
      System.out.println("TarBuilder usage: net.paymate.util.TarBuilder outputFilename [ infile1 [ ... infileN ] ]");
    } else {
      TextList tl = new TextList(args);
      String outputFilename = tl.remove(0); // take the output file name out of the input file list
      System.out.println("TARing '"+outputFilename+"' from: " + tl.toString());
      TarBuilder tarrer = new TarBuilder(outputFilename, tl.toStringArray());
      System.out.println("TAR returned: " + tarrer.tar());
    }
  }

}
