package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/Backlog.java,v $
* Description:  a cached list of backlog'd objects.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.36 $
*/
import net.paymate.util.*;
import net.paymate.io.*;
import java.util.*;
import java.io.*;
import net.paymate.util.codec.CipherP86B2;

public class Backlog {
  private ErrorLogStream dbg;//shares debugger with parent
  int myCryptoKey=2345987; //someday have a per terminal variable. set non-zero for encryption.
  private static final int padlength=12;

  /**
  * a directory of objects
  */
  File root;
  /**
  * and a cache for it
  */
  protected Vector files=new Vector();
  Monitor listlock;
  TimeSortFile sorter;

  ///////////////
  private void enlist(File f){
    try {
      listlock.getMonitor();
      //extract this to Safe
      int location=Collections.binarySearch(files,f,sorter);
      if(location<0){//not found but we know where it should go
        files.insertElementAt(f,~location);//-location-1;
      }
    }
    finally {
      listlock.freeMonitor();
    }
  }

  //
  ///////////////////////

  public boolean isEmpty(){
    return files.isEmpty();
  }

  public int size(){
    return files.size();
  }

  /**
  * @return the file(name) that should be the next thing to send to server
  */
  private File nextFile(){//throws
    return (File) files.remove(files.size()-1);//humm looks like an objectstack
  }

  public ActionRequest loadFile(File frap) {
    try {
      if (frap != null) {
        String before = IOX.FileToString(frap.getAbsolutePath());
        dbg.VERBOSE("loadFile()-before="+before);
        String after = (myCryptoKey > 0) ? new String(CipherP86B2.crypt(myCryptoKey, frap.toString().getBytes(), before.getBytes())) : before;
        dbg.VERBOSE("loadFile()-after="+after);
        EasyCursor ezp = new EasyCursor(after);
        ActionRequest ar = ActionRequest.fromProperties(ezp);
        if (ar != null && ar instanceof canBacklog) {
          ( (canBacklog) ar).setLocalFile(frap);
        }
        return ar;
      }
      else {
        return null;
      }
    }
    catch (Exception ex) {
     return null;
    }
  }

  /**
  * @return next action request
  */
  public ActionRequest next(){
    dbg.Enter("next("+files.size()+" left)");
    File frap=null;//4debug
    try {
      return loadFile(nextFile());
    }
    catch (ArrayIndexOutOfBoundsException mt){//#normal. used as 'empty' check.
      dbg.VERBOSE("empty.");
      return null;
    }
    catch(Exception ignored){
      dbg.Caught(ignored);
      return null;
    } finally{
      dbg.VERBOSE("File:"+frap);
      dbg.Exit();
    }
  }

  private File storeRequest(ActionRequest request, File fullpath){
    dbg.Enter("storeRequest");
    try {
      if(fullpath.exists()){//we need to kill the reply.
        dbg.ERROR(String.valueOf(fullpath)+" already exists");
        return null;
      }
      FileOutputStream fos=new FileOutputStream(fullpath);
      EasyProperties rqp= request.toProperties();
      //@todo: apply stream encrypter here.
      OutputStream crypter;
      if(myCryptoKey>0){
        crypter= net.paymate.util.codec.CipherP86B2.getOutputStream(fos,fullpath.toString().getBytes(),myCryptoKey);
      } else {
        crypter= fos;
      }
//      crypter.write(AsciiPadder.);
      rqp.store(crypter,fullpath.toString());
      return IOX.Close(fos)? fullpath : null;
    } catch (Exception any){
      dbg.Caught(any);
      return null;
    } finally {
      dbg.Exit();
    }
  }

  protected boolean register(ActionRequest request,String name){
    dbg.VERBOSE("register:"+name);
    File rqfile=storeRequest(request, new File(root,name));
    if(rqfile!=null){
      enlist(rqfile);
      return true;
    } else {
      return false;
    }
  }


  /**
   * while this could be static it isn't so that extensions can do something upon this event.
   * but such extensions had damn well better super this.
   * but now it is static ... someone needed to call it without an object in scope.
   */
  static boolean markDone(canBacklog request) {
    return IOX.deleteFile(request.getLocalFile());
  }

  protected int init(){
    try {
      listlock.getMonitor();
      dbg.Enter("init:"+root.getAbsolutePath());
      File [] dirlist=IOX.listFiles(root);
      dbg.WARNING("# of files:"+dirlist.length);
      files= new Vector(dirlist.length);
      for(int i=dirlist.length;i-->0;){
      //using enlist() function despite the overhead to ensure identical sorting.
        enlist(dirlist[i]);
      }
      return files.size();
    }
    finally {
      dbg.Exit();
      listlock.freeMonitor();
    }
  }

  public Backlog(File root,ErrorLogStream adbg) {
    this.root = root;
    dbg = adbg != null ? adbg : ErrorLogStream.getForClass(Backlog.class);
    dbg.WARNING("Backlog directory is " + root.getAbsolutePath());
    sorter = TimeSortFile.Descending(NameSortFile.Descending()); //makes oldest come out first
    listlock = new Monitor(root.getPath() + ".BackLock");
    IOX.createDir(root);
    init();
  }

}
//$Id: Backlog.java,v 1.36 2004/03/08 22:54:14 andyh Exp $
