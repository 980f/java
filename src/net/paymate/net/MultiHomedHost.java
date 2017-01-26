package net.paymate.net;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/net/MultiHomedHost.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.*;
import java.net.Socket;

public class MultiHomedHost implements isEasy,HostToken {

  private static final ErrorLogStream classdbg= ErrorLogStream.getForClass(MultiHomedHost.class);
  protected ErrorLogStream dbg;

  public String nickName;
  public int creationTimeoutMs;
  public IPSpec ipSpec;
  public boolean viaPPP;

  protected backgroundSocketOpener opener;
  protected boolean freshen=true;//next request should be fresh

  /**
   * these are referenced and managed only by multihomedlist. they should be moved out of this class, or lumped into a subclass
   */
  public boolean isUp=false;
  public boolean gooseIt=true;//when is next at bat open a socket, to trigger pppd dialing.

  /**
   * called when this host has been moved into 'currenthost' position in some list.
   */
  public void ondeck(){
    //hook
  }
  /**
   * this executes on the scheduler's qagent run thread
   * it is the routine that takes too long to timeout
   */
  /**
   * NB: only run on backgroundSocketOpener's thread
   * @param request info passed from foreground thread to background, not of public interest
   * @return socket to host
   */
  public Socket makeSocket(SocketRequest request) {
    dbg.Enter("runone");
    try {
      //this is also where lots of time is taken
      return new Socket(ipSpec.address, ipSpec.port);
    } catch (Exception caught){
      return onError(caught);
    } finally {
      dbg.Exit();
    }
  }

  /**
   * @param timeoutMs is time to wait for read data response
   * @return socket to host
   */
  public Socket open(int timeoutMs) {
    return open(timeoutMs, false);
  }

  public Socket open(int timeoutMs, boolean finishWaitOnFailure) {
    Socket newone = Open(finishWaitOnFailure);
    if(newone != null) {
      try {
        newone.setSoTimeout(timeoutMs);
        dbg.WARNING(SocketInfo.socketSpam(newone));
      } catch (java.net.SocketException se) {
        dbg.ERROR("open: SocketException setting timeout - " + se);
      }
    } else {
      // ???
    }
    return newone;
  }

  /** blocking with locally enforced timeout.
   * @return new socket
   * @param finishWaitOnFailure when true wait on failure for the full timeout period before returning
   */
  private Socket Open(boolean finishWaitOnFailure) {
    dbg.Enter("Open");
    try {
      //must have a waiter per request.
      Waiter toe=Waiter.Create(creationTimeoutMs,false,dbg);
      SocketRequest request = new SocketRequest(this, toe,dbg);
      opener.Post(request);
      int done=toe.run();
      //someday run timer statistics on this to get a better timeout value.
      dbg.ERROR("Socket creation [took "+ toe.elapsedTime() +" ms] "+ Waiter.stateString(done) +" to " + ipSpec.toString() + "!");
      switch(done){
        case Waiter.Excepted:
        case Waiter.Timedout: {
          request.Abandon();//formerly just a close()
        }
        default: {
        }
      }
      if(finishWaitOnFailure) {
        if (request.socket == null) {
          toe.finishWaiting(); // this allows us to prevent spamming a server!
        } else {
          dbg.WARNING("Socket not null, so not going to finish waiting.");
        }
      }
      return request.socket;
    } finally {
      dbg.Exit();
    }
  }

  private Socket onError(Exception ex){
    dbg.WARNING(ex.getMessage());
    return null;
    //+++ and finally Notify()!
  }

  /**
   * freshen doesn't need synchronization. it is only referenced in one place.
   */
  public void reset(){
    freshen=true;
  }

  public void shutdown() {
    opener.stopAgent(); // shuts down the old thread
  }

  public void save(EasyCursor ezc){
    ezc.setObject(IPSpecKey,   ipSpec );
    ezc.setInt(portKey,ipSpec.port);//slurm compatibilty
    ezc.setString(nickNameKey,nickName);
    ezc.setInt(creationTimeoutKey,creationTimeoutMs);
  }

  public void load(EasyCursor ezc) {
    ipSpec = IPSpec.New(ezc.getString(IPSpecKey));
    ipSpec.port = ezc.getInt(portKey, ipSpec.port); //port param overrides colonated suffix on address
    creationTimeoutMs = ezc.getInt(creationTimeoutKey);
    nickName = ezc.getString(nickNameKey,ipSpec.address);//ensure nonTrivial name for dump.
    viaPPP=ezc.getString("interface").startsWith("ppp");
  }

  public MultiHomedHost(String name) {
    dbg=classdbg;
    opener=new backgroundSocketOpener(dbg, name);
  }

  public MultiHomedHost() {
    this("nameless");
  }

  public TextList dumpStatus(TextList dump) {
    if(dump == null) {
      dump = new TextList(3);
    }
    dump.add(this.nickName+" connection ", (isUp?"Up":"Down"));
    dump.add("ip",ipSpec.toString());
    return dump;
  }

}
//$Id: MultiHomedHost.java,v 1.8 2004/03/22 21:46:13 andyh Exp $