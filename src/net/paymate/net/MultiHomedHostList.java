package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/MultiHomedHostList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import java.util.*;
import net.paymate.util.TextList;
import net.paymate.io.IOX;

public class MultiHomedHostList {
  /**
   * constants for getHost() function.
   */
  public static final boolean GETPREFERREDHOST = true;
  public static final boolean GETCURRENTHOST = !GETPREFERREDHOST;

  private Vector /*<MultiHomedHost>*/ thelist;
  private MultiHomedHost preferredOne;
  private final static int current=0;//presently rotate the actual list content!

  public MultiHomedHost getHost(boolean primary){
    return primary ? preferredHost() : currentHost();
  }

  /* preferredOne is modified only during initialization, therefore there is never a reason to synch.*/
  public /*synchronized*/ MultiHomedHost preferredHost(){
    try {
      if(preferredOne == null) {
        preferredOne = (MultiHomedHost) thelist.elementAt(0);
      }
      return preferredOne;
    }
    catch (Exception ex) {
      return null;
    }
  }

  public /*synchronized*/ MultiHomedHost currentHost(){
    try {
      return (MultiHomedHost) thelist.elementAt(current);
    }
    catch (Exception ex) {
      return null;
    }
  }

  /**
   * called whenever the current host has (possibly) changed
   */
  protected void onDeck(){
    try {
      if(currentHost().gooseIt){
        currentHost().ondeck();//let host know that it will be called upon for next socket.
      }
    }
    catch (Exception ex) {
      //this is just a hook, ok to not run it if nothing has hooked in.
    }
  }
  /** move to end of list, if was current one then there is a new current one
   * @return whether failedone just failed, false on any gross errors
   * synched so that list changes are atomic.
   */
  public boolean thisFailed(MultiHomedHost failedone){
    synchronized(thelist){
      if(failedone != null) {
        boolean wasok = failedone.isUp;
        failedone.isUp = false;
        // move to the end
        if(thelist.remove(failedone)) {
          thelist.add(failedone);
          onDeck();
        } else {
          //not in list, ignore it. possibly bitch to a log.
          return false;
        }
        return wasok;
      } else {
        return false;
      }
    }
  }

  /**
   * @returns whether the current host is the preferredone.
   * not made public as this is can change the moment after this is called.
   */
  private boolean preferredIsCurrent(){
    return currentHost()==preferredOne;
  }

  /**
   * @presumes this is usually only called when a change is to occur, and always within already synchronized scope
   * synched to make change to list atomic.
   */
  private void makeCurrent(MultiHomedHost somehost){
    synchronized(thelist) {
      if(thelist.remove(somehost)) {
        thelist.insertElementAt(somehost, current);
      }
    }
  }

  /**
   * @return whether working one just started to work, false on any gross errors
   *  become current if:
   * current is not working
   * current not preferred and this one is
   */
  public boolean thisWorked(MultiHomedHost working){
    synchronized (thelist) {
      if (working!=null) {
        boolean wasok=working.isUp;
        working.isUp=true;
        int index=thelist.indexOf(working);
        if (index>=0) {
          boolean isprefered = working==preferredOne;
          boolean iscurrent  = index==current;
          boolean currentok = currentHost().isUp;
          if( ! currentok || ( ! preferredIsCurrent() && isprefered) ){
            if(!iscurrent){//minor optimization
              makeCurrent(working);
            }
          }
          return !wasok;
        } else {
          //error, not in our list!
          return false;
        }
      } else {
        return false;
      }
    }
  }

  ///////
  // configuration

  /**
   * register @param preferredOne if not already in list and make it current
   * @return this
   */

  public MultiHomedHostList prefer(MultiHomedHost preferredOne){
    synchronized (thelist) {//gratuitous, only used in initialization, well before other threads could access this object
      if(preferredOne != null) {
        if(thelist.indexOf(preferredOne) < 0) {
          thelist.add(preferredOne);
        }
        this.preferredOne = preferredOne;
        makeCurrent(preferredOne);
      }
    }
    return this;
  }

  /**
   * register @param newhost in the list, make it be preferred if it is the only one.
   * @return this
   */

  public MultiHomedHostList alternate(MultiHomedHost newhost){
    synchronized (thelist) {//gratuitous, only used in initialization, well before other threads could access this object
      if(newhost!=null){
        if(thelist.indexOf(newhost)<0){//note:indexof uses the .equals funciton of the objects
          thelist.add(newhost);
        }
        if(preferredOne == null){
          preferredOne=newhost;
        }
      }
    }

    return this;
  }

  public MultiHomedHostList() {
    thelist=new Vector(2,1);//uncommon on client to have more than 2, primary and dial backup. Usually 2, sometimes 3 IP's for authorizers.
  }

  public void Clear() {
    synchronized (thelist) {//lock so that we get a coherent report
      for(int i=thelist.size(); i-->0;){
        ((MultiHomedHost)thelist.elementAt(i)).shutdown();  // shuts down the old bgsocketopener threads
      }
    }
    thelist.removeAllElements();
    this.preferredOne=null;
  }

  /**
   * freezes list of hosts to get a snapshot of status.
   * @param dump place for info
   * @return param dump, or newly created TextList if given null.
   */
  public TextList dumpStatus(TextList dump){
    boolean pppIsRelevent=false;
    if(dump==null){
      dump=new TextList (3*thelist.size());
    }
    synchronized (thelist) {//lock so that we get a coherent report
      for(int i=thelist.size(); i-->0;){
        MultiHomedHost ahost=(MultiHomedHost)thelist.elementAt(i);
        ahost.dumpStatus(dump);
        if(ahost.viaPPP){
          pppIsRelevent=true;
        }
      }
      if(pppIsRelevent){
        String dialupstatusfile = "/tmp/ppp.status"; //@todo: somehow share this filename with C++ code.
        if(IOX.FileExists(dialupstatusfile)) {
          dump.add(IOX.TextFileContent(dialupstatusfile));//file is a properties file, not using that fact here.
        }
      }
    }
    return dump;
  }

}
//$Id: MultiHomedHostList.java,v 1.13 2004/05/23 16:30:10 andyh Exp $