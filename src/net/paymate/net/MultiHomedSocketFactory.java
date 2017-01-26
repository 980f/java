package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/MultiHomedSocketFactory.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;

public class MultiHomedSocketFactory {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(MultiHomedSocketFactory.class);

  protected MultiHomedHostList hostlist;

//  public MultiHomedSocketFactory(PanicStream ps) {
//    this();
//    this.ps=ps;
//  }
  public MultiHomedSocketFactory() { // no service
    hostlist=new MultiHomedHostList();
  }
  /**
   * the Service class is a pain on the client, even to just have in compilation scope.
   * it should NOT be used within shared modules
   * especially we shouldn't add one just for one message. Whoever called this module
   * can decide whether to panic on a switch of IP.
   */
//  private Service service = null;
//  private PanicStream ps;
//  private final void PANIC(String yell) {
//    if(ps != null) {
//      ps.PANIC(yell);
//    } else {
//      //???
//    }
//  }
//
  public MultiHomedHost preferredHost(){
    return hostlist.preferredHost();
  }

  public MultiHomedHost currentHost(){
    return hostlist.currentHost();
  }

  public MultiHomedHost getHost(boolean primary){
    return primary ? preferredHost() : currentHost();
  }

  public boolean thisFailed(MultiHomedHost host){
    boolean justfailed = hostlist.thisFailed(host);
    MultiHomedHost newhost = hostlist.currentHost();
    if(justfailed) {
      host.reset();
    }
    return justfailed;
  }

  public boolean thisWorked(MultiHomedHost host){
    return hostlist.thisWorked(host);
  }

  public synchronized boolean Initialize(MultiHomedHostList hostlist) {
    shutdown(); // clears the old stuff before setting to new stuff
    this.hostlist = hostlist!=null? hostlist: new MultiHomedHostList();//make sure list is not null, don't want to have to null check everywhere.
    return true;
  }

  public static final String HOSTSKEY="hosts";
  public static final String PREFERREDKEY="preferred";

  public synchronized void shutdown() {
    if(this.hostlist != null) {
      this.hostlist.Clear(); // shuts down the old bgsocketopener threads
    }
  }


  public boolean Initialize(EasyCursor ezc, Class hostclass) {
    try {
      hostlist.Clear();
      if(ezc!=null){//iterate through cursor making hosts
        TextList names= TextList.CreateFrom(ezc.getString(HOSTSKEY));
        if(names.size()>0){
          String preferred= ezc.getString(PREFERREDKEY);
          for (int i=names.size();i-->0;){
            String hostname=names.itemAt(i);
            MultiHomedHost host = (MultiHomedHost) ezc.getObject(hostname, hostclass);
            if(! StringX.NonTrivial(host.nickName)){
              host.nickName=hostname;
            }
            if(preferred.equals(hostname)){
              hostlist.prefer(host);
            } else {
              hostlist.alternate(host);
            }
          }
        } else {
          int count=ezc.getInt("size");
          if(count>0){//then anonymous host list, preferred is 0th entry
            while(count-->0){
              MultiHomedHost host = (MultiHomedHost) hostclass.newInstance();
              ezc.push(count);
              host.load(ezc);
              ezc.pop();
              hostlist.alternate(host);
            }
          } else {
//legacy single host load:
            MultiHomedHost host = (MultiHomedHost) hostclass.newInstance();
            host.load(ezc);
            hostlist.prefer(host);
          }
        }
      }
      return true;
    } catch (Exception ex) {
      ErrorLogStream.Global().Caught(ex);
      return false;
    }
  }

  public TextList dumpStatus(TextList dump){
    return hostlist.dumpStatus(dump);
  }

}
//$Id: MultiHomedSocketFactory.java,v 1.13 2004/02/28 01:33:21 andyh Exp $
