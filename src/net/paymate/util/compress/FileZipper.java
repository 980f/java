/**
 * Title:        $Source: /cvs/src/net/paymate/util/compress/FileZipper.java,v $
 * Description:  Zips files using a separate thread, which can have its priority raised or lowered
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: FileZipper.java,v 1.4 2004/02/07 22:35:47 mattm Exp $
 */

package net.paymate.util.compress ;
import  java.util.zip.GZIPOutputStream;
import  java.util.Vector;
import  java.io.FileInputStream;
import  java.io.FileOutputStream;
import  java.io.File;
import  net.paymate.util.AtExit;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.TextList;
import  net.paymate.util.LogSwitch;
import net.paymate.lang.ThreadX;
import net.paymate.io.Streamer;
import net.paymate.lang.ThreadX;

/**
 * To zip a file in the background, just call:
 *    backgroundZipFile(String filename);
 * where filename is the original filename.
 * It will be copied (zipped) to filename.gz
 * and the original will get deleted (if it can).
 */
public class FileZipper extends Thread implements AtExit {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(FileZipper.class);
  private static final FileZipperRegistry registry = new FileZipperRegistry();

  // call these last so that they don't starve the other threads ???
  public void AtExit() {
    setPriority(Thread.NORM_PRIORITY); // hurry up and get it done.
  }
  public boolean IsDown() {
    return !isAlive();
  }

  /**
   * How to report errors?
   */
  public static final FileZipper zipFile(String filename, int threadPriority) {
    FileZipper fz = new FileZipper(filename, threadPriority);
    fz.start();
    return fz;
  }

  public static final FileZipper zipFile(String filename) {
    FileZipper fz = new FileZipper(filename, Thread.currentThread().getPriority()); // defaults to current priority
    fz.start();
    return fz;
  }

  public static final FileZipper backgroundZipFile(String filename) {
    return zipFile(filename, Thread.MIN_PRIORITY);
  }

  public static final FileZipper zipFileNow(String filename) {
    FileZipper fz = new FileZipper(filename, Thread.currentThread().getPriority()/*0*/); // defaults to current priority
    fz.run();
    return fz;
  }

  private String inFilename = null;

  public String outFilename = null;
  public TextList errors = new TextList();

  private FileZipper(String inFilename, int threadPriority) {
    super(LogSwitch.shortName(FileZipper.class,inFilename));
    this.inFilename = inFilename;
    setPriority(threadPriority);
    registry.register(this);
  }

  private boolean done = false;

  public boolean isDone() {
    return done;
  }

  public void run() {
    try {
      // calculate the output filename
      outFilename = inFilename + ".gz";
      // open the input file
      File inputFile = new File(inFilename);
      FileInputStream fis = new FileInputStream(inputFile);
      // open the output file
      FileOutputStream fos = new FileOutputStream(outFilename);
      GZIPOutputStream zipout = new GZIPOutputStream(fos);
      Streamer streamer = Streamer.Buffered(fis, zipout);
//      streamer.run();
      // close the output stream
      zipout.flush();
      fos.flush();
      zipout.close();
      fos.close(); // --- will except?
      zipout = null;
      fos = null;
      fis.close();
      fis = null;
      // now cleanup can handle those streams
      // delete the original file
      inputFile.delete(); // +++ check return value
      // so mark it done
      done = true;
    } catch (Exception e) {
      String err = "Exception zipping a file: " + e;
      errors.add(err);
      dbg.ERROR(err);
      dbg.Caught(e);
    } finally {
      done = true;
    }
  }
}

/**
 * +++ use the ObjectPool ???
 * The registry is used to keep up with which ones are currently running.
 * If you don't remove them when they are done, they will never leave!
 * (Can't force a destroy.)
 */
class FileZipperRegistry extends Vector {
  public void register(FileZipper item) {
    add(item);
  }
  public void unregister(FileZipper item) {
    remove(item);
  }
}
