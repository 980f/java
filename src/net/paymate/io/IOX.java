package net.paymate.io;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/io/IOX.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import java.io.*;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.UTC;
import net.paymate.util.TextList;
import net.paymate.util.DateX;
import net.paymate.lang.StringX;

public class IOX {
  private IOX() {
    // don't construct me; I am for static functions
  }

  public static final File [] listFiles(File dir){
    File [] list=dir.listFiles();
    return list!=null ? list: new File[0];
  }

  public static final File [] listFiles(File dir, FileFilter filter){
    File [] list=dir.listFiles(filter);
    return list!=null ? list: new File[0];
  }

  /**
   * quickie DOSlike file attribute control
   * @return true if file was made read/write.
   * just CREATING a FilePermission object modifies the underlying file,
   * that is really bogus syntax. Need to wrap all of that in a FileAttr class.
   */
    public static final boolean makeWritable(File f){
      try {
        FilePermission attr=new FilePermission(f.getAbsolutePath(),"read,write,delete");
        return true;
      } catch(Exception oops){
        return false; //errors get us here
      }
    }
  /**
   * @see makeWritable for complaints about java implementation.
   * @return true if file was made readonly.
   */
    public static final boolean makeReadonly(File f){
      try {
        FilePermission attr=new FilePermission(f.getAbsolutePath(),"read");
        return true;
      } catch(Exception oops){
        return false; //errors get us here
      }
    }
  /**
   * returns true is something actively was deleted.
   */
    public static final boolean deleteFile(File f){
      try {
        return f!=null && f.exists()&& makeWritable(f) && f.delete();
      } catch(Exception oops){
        return false; //errors get us here
      }
    }


  public static Exception Flush(OutputStream os){
    try {
      os.flush();
      return null;
    }
    catch (Exception ex) {
      return ex;
    }
  }
  /**
   * @return true if stream closes Ok, or didn't need to.
   */
  public static final boolean Close(OutputStream fos){
    if(fos != null) {
      try {
        fos.flush(); //to make this like C
        fos.close();
      }
      catch(IOException ioex){
        ErrorLogStream.Global().WARNING("IOX.Close(OutputStream):"+ioex);
        return true;
      }
      catch (Exception tfos) {
        ErrorLogStream.Global().Caught("IOX.Close(OutputStream):",tfos);
        return false;
      }
    }
    return true;
  }

  /**
   * @return true if stream closes Ok, or didn't need to.
   */
  public static final boolean Close(InputStream fos){
    if(fos != null) {
      try {
        fos.close();
      }
      catch(IOException ioex){
        ErrorLogStream.Global().WARNING("IOX.Close(OutputStream):"+ioex);
        return true;
      }
      catch (Exception tfos) {
        return false;
      }
    }
    return true;
  }

  /**
   * returns true if dir now exists
   */
  public static final boolean createDir(String filename) {
    return createDir(new File(filename));
  }
  public static final boolean createDir(File file) {
    if(!file.exists()) {
      return file.mkdir();
    }
    return true;
  }
  public static final void createDirs(File file) {
    try {
      file.mkdirs();
    } catch (Exception ex) {
      // gulp
    }
  }
  public static final void createParentDirs(File file) {
    String parent = file.getParent();
    createDirs(new File(parent));
  }
  public static final void createParentDirs(String filename) {
    createParentDirs(new File(filename));
  }

  public static final String fromStream(ByteArrayInputStream bais,int len){
    byte [] chunk=new byte[len];
    bais.read(chunk,0,len);
    String s=new String(chunk);
    return s;
  }

  //////////
  public static final boolean FileExists(File f){
    return f!=null && f.exists();
  }
  public static final boolean FileExists(String fname){
    return FileExists(new File(fname));
  }

  public static final FileOutputStream fileOutputStream(String filename) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(filename);
    } catch (Exception e) {
      // +++ bitch
    }
    return fos;
  }

  public static final String FileToString(String filename) {
    String ret = "";
    try {
      FileInputStream filein = new FileInputStream(filename);
      ByteArrayOutputStream baos = new ByteArrayOutputStream((int)fileSize(filename));
      Streamer.Buffered(filein, baos);
      ret = new String(baos.toByteArray());
    } catch (Exception ex) {
      // +++ bitch
    } finally {
      return ret;
    }
  }

  /**
   * Creates a unique filename given a particular pattern
   *
   * @param pathedPrefix - the path (just directory) where the file will be located + the first part of the filename
   * @param suffix - the last part of the filename
   *
   * ie: filename = path + datetimestamp + suffix
   *
   * eg: createUniqueFilename("c:\temp", "myfile", ".txt") = c:\temp\myfile987654321.txt"
   */
  public static final String createUniqueFilename(String pathedPrefix, String suffix) {
    File file = null;
    String filename = null;
    int attempts = 0;
    try {
      do {
        filename = pathedPrefix + DateX.timeStampNow() + suffix;
        file = new File(filename);
      } while (((file != null) || file.exists()) && (++attempts < 20));//no infinite loops
    } catch (Exception e) {
      // +++ bitch
    }
    return filename;
  }

  public static final long fileModTicks(String filename){
    try {
      return (new File(filename)).lastModified();
    } catch(Exception anything){
      return -1;
    }
  }

  public static final UTC fileModTime(String filename){
    return UTC.New(fileModTicks(filename));
  }

  public static final long fileSize(String filename){
    try {
      return (new File(filename)).length();
    } catch(Exception anything){
      return -1;
    }
  }

  public static TextList showAsciiProfile(String filename) {
    TextList ret = null;
    try {
      FileInputStream fis = new FileInputStream(filename);
      ret = showAsciiProfile(fis);
    } catch (Exception ex) {
      ret.add("Exception Ascii profiling \"" + filename + "\": " + ex);
    } finally {
      return ret;
    }
  }

  public static int [ ] asciiProfile(InputStream in) throws IOException {
    int [ ] profile = new int[256];
    int one;
    while((one=in.read()) != -1) {
      profile[one]++;
    }
    return profile;
  }

  public static TextList showAsciiProfile(InputStream in) {
    TextList ret = new TextList();
    int [ ] profile = null;
    try {
      profile = asciiProfile(in);
      for(int i = 0; i < profile.length; i++) {
        ret.add("Value " + i + " had " + profile[i] + " occurrences.");
      }
    } catch (Exception ex) {
      ret.add("IOException Ascii profiling stream: " + ex);
    } finally {
      return ret;
    }
  }

  public static BufferedReader StreamLineReader(InputStream stream){
    try {
      if (stream!=null) {//what other stream state should we check?
        return new BufferedReader(new InputStreamReader(stream));
      } else {
        return null;
      }
    }
    catch (Exception ex) {
      return null;
    }
  }

  /**
   * @param fname name of file to read lines from
   * @return reader that can read whole lines from a file
   * @todo return a reader that passes back error messages instead of returning null.
   */
  public static BufferedReader FileLineReader(File file){
    try {
      return StreamLineReader(new FileInputStream(file));
    }
    catch (Exception ex) {
      return null;
    }
  }

  /**
   * @param fname name of file to read lines from
   * @return reader that can read whole lines from a file
   * @todo return a reader that passes back error messages instead of returning null.
   */
  public static BufferedReader FileLineReader(String fname){
    try {
      return StreamLineReader(new FileInputStream(fname));
    }
    catch (Exception ex) {
      return null;
    }
  }

  public static final PrintStream FilePrinter(String fname){
    try {
      return new PrintStream(new FileOutputStream(new File(fname)));
    }
    catch (Exception ex) {
      System.err.print("Making a FilePrinter got:"+ex);
      System.err.println("...output will go to this stream instead of file ["+fname+"]");
      return System.err;
    }
  }
  /**
   * read contents of a regular file into an array of strings
   * @param file
   * @return textlist with each item one line from file
   */
  public static TextList TextFileContent(File file){
    TextList content=new TextList();
    BufferedReader reader=FileLineReader(file);
    try {
      while(reader.ready()){
        content.add(reader.readLine());
      }
    }
    catch (Exception ex) {
    //on exception break while and keep what we got, adding note:
      content.add("Exception while reading file");
      content.add(ex.getLocalizedMessage());
    }

    return content;
  }
  /**
   *
   * @param fname
   * @return see TextFileContent(File file)
   */
  public static TextList TextFileContent(String fname){
    return TextFileContent(new File(fname));
  }

  public static final void main(String [] args) {
    System.out.println(showAsciiProfile(args[0]));
  }
}
//$Id: IOX.java,v 1.7 2004/05/23 16:30:10 andyh Exp $
