package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/Backlog.java,v $
* Description:  a cached list of backlog'd objects.
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.17 $
*/
import net.paymate.util.*;

import java.util.*;
import java.io.*;

public class Backlog {
  private ErrorLogStream dbg;//shares debugger with parent
  /**
  * a directory of objects
  */
  File root;
  /**
  * and a cache for it
  */
  protected Vector files=new Vector();
  Monitor listlock;
  static TimeSortFile sorter=new TimeSortFile();

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
  * @return the file(name) that should be the next receipt to store
  */
  private File nextFile(){//throws
    return (File) files.remove(files.size()-1);//humm looks like an objectstack
  }

  /**
  * @return next action request
  */
  public ActionRequest next(){
    dbg.Enter("next("+files.size()+" left)");
    File frap=null;//4debug
    try {
      return ActionRequest.fromFile(frap=nextFile());
    }
    catch (ArrayIndexOutOfBoundsException mt){//used as 'empty' check.
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
        dbg.ERROR(fullpath+" already exists");
        return null;
      }
      FileOutputStream fos=new FileOutputStream(fullpath);
      fos.write(request.toEasyCursorString().getBytes());
      return Safe.Close(fos)? fullpath : null;
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

  static final boolean markDone(canBacklog request) {
    return Safe.deleteFile(request.getLocalFile());
  }

  protected int init(){
    try {
      listlock.getMonitor();
      dbg.Enter("init:"+root.getAbsolutePath());
      File [] dirlist=Safe.listFiles(root);
      dbg.WARNING("# of files:"+dirlist.length);
      Arrays.sort(dirlist,sorter);
      //it is astonishing that collections cannot be initialized from arrays!
      //I couldn't find anything in the source.jar that implies that they can
      //need to look into reflection... not there either! but can be used to make one
      files= new Vector(dirlist.length);
      for(int i=0;i<dirlist.length;i++){//preserve sorter's order
        files.add(dirlist[i]);
      }
      return files.size();
    }
    finally {
      dbg.Exit();
      listlock.freeMonitor();
    }
  }

  public Backlog(File root,ErrorLogStream dbg) {
    this.root=root;
    this.dbg= dbg!=null?dbg: new ErrorLogStream(Backlog.class.getName());
    dbg.WARNING("Backlog directory is "+root.getAbsolutePath());
    sorter=new TimeSortFile(true);//makes oldest come out first
    listlock= new Monitor (root.getPath()+".BackLock");
    Safe.createDir(root);
    init();
  }

}
//$Id: Backlog.java,v 1.17 2001/11/17 00:38:33 andyh Exp $
