package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/SinetHost.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.19 $
 */

import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.lang.StringX;
import net.paymate.net.*;

import java.net.*;
import java.util.Vector;
import javax.net.ssl.*;
import javax.net.*;

import java.io.File;
import java.io.BufferedReader;
import java.util.Enumeration;

public class SinetHost extends MultiHomedHost implements isEasy, SinetHostToken {
  private static final ErrorLogStream classdbg= ErrorLogStream.getForClass(SinetHost.class);
//  private ErrorLogStream dbg;

///////////// configured parts
  /**
   * servlet to talk to for txn's.
   */
  public String UrlPath;
  /**
   * validation token of the appliance for this host
   */
  private String appleId;
  public String appleId(){
    return appleId;
  }

  private String ifaceStyle="unknown";//@todo: enumerate.

  private static final int legacyHack=20000;//@todo find a source for this prophylactic constant.
  private Timeout timeout=new Timeout(legacyHack);
/////////// derived/cached parts
  public static KeyStoreAxess keyStore;//shared until we resolve whether we will separate ones.
  //sharing this in case it leaves the file open for tangible periods of time.
  private com.sun.net.ssl.SSLContext context = null;
  private SocketFactory factory = null;

//shared parts
/**
 * we only allow ourselves to use the strongest available suite (at time of compilation).
 */
  public static final String suites[] = {
    "SSL_RSA_WITH_RC4_128_MD5",
  };
  private static String macid;//eth0 ethernet address, NOT (necessarily) the applname.

////////////

  public TextList dumpStatus(TextList dump) {
    dump = super.dumpStatus(dump);
    //@netstat@ add pppinfo if host is ppp type.
    if(ifaceStyle.startsWith("ppp")){
      //a properties file of tidbits.
      EasyCursor pppstats=EasyCursor.FromDisk("/var/tmp/ppp/stats");
      pppstats.push("ppp");
      TextListIterator iter= TextListIterator.New (pppstats.branchKeys());
      while(iter.hasMoreElements()){
        String key=iter.next();
        dump.add(key,pppstats.getString(key));
      }
    } else {
      //get eth stats
    }
    return dump;
  }

/**
 * NB: only run on backgroundSocketOpener's thread
 * @return java socket, ssl protocol to host.
 */

  private Socket makeSecureSocket()throws java.net.UnknownHostException,java.io.IOException{
    SSLSocket sslsock = null;
    dbg.Enter("makeSecureSocket()");
    try {
      sslsock = (SSLSocket)factory.createSocket(ipSpec.address, ipSpec.port);
      setSecurity(sslsock);
      dbg.WARNING("SSL socket created.  Returning now.");
    }
//catch these here just to get full stack trace
    catch(ClassCastException cce){
      dbg.Caught("Could not create SSLSocket from SocketFactory!",cce);
    }
    catch(NullPointerException npe){
      dbg.Caught("Could not createSocket from SocketFactory!",npe);
    }
//  let makeSocket() catch the rest.
    finally {
      dbg.Exit();
      return sslsock;
    }
  }

  /**
   * sets our preferred security options
   * @throws null pointer exception on socket not created by factory.
   * @throws java.io.IOException when startHandshake() feels like it.
   *    * NB: only run on backgroundSocketOpener's thread
   * @param socket already opened socket
   */
  private void setSecurity(SSLSocket socket) throws java.io.IOException {
    socket.setEnableSessionCreation(true); //allow for session ID for fast reconnect
//was always bogus, these are server side options
// the next two default to false and true, respectively
// they are based on whether the socket is a client socket or a server socket.
// you can't flip either of them and have the thing work.  Leave them as defaulted
//--    socket.setNeedClientAuth(true);   // only valid for server-mode sockets (which this isn't)
//--    socket.setUseClientMode (true);   //we let apache serve secure sockets :)
    socket.setEnabledCipherSuites(suites);
//    dbg.ERROR("NeedClientAuth="+socket.getNeedClientAuth()+", UseClientMode="+socket.getUseClientMode());
    socket.startHandshake(); //so that next message might be more meaningful
    dbg.WARNING("at end of setSecurity:"+socket.toString());
  }

  /**
   * NB: only run on backgroundSocketOpener's thread
   * @return whether factory now exists
   */
  private boolean assureFactory(){
    if(freshen) {
      factory = null;
      context = null;
      freshen=false;
    }
    if(context == null) {
//      dbg.WARNING("Making Context For:"+host.toSpam());
      context = Trustee.makeContext(keyStore);
    }
    if(context == null) {
      dbg.ERROR("Could not create context!");
      return false;
    }
    if(factory == null) {
      SSLSocketFactory ssfc = context.getSocketFactory();
      if(ssfc == null) {
        dbg.ERROR("Could not get SSLSocketFactory from context!");
        return false;
      }
      StopWatch getfac=new StopWatch(true);
      factory = ssfc.getDefault();
      dbg.VERBOSE("getDefault() factory took "+getfac.Stop()+" millis");
    }
    if(factory == null) {
      dbg.ERROR("Could not get default SocketFactory from SSLSocketFactory!");
      return false;
    }
    return true; //we got a factory
  }

  private Socket onError(Exception ex){
    dbg.WARNING(ex.getMessage());
    return null;
    //+++ and finally Notify()!??? justify this note or delete it soon
  }

  public void ondeck(){
    //if host is ppp then we will have set 'gooseit'
    if(gooseIt){
      try{
        Socket goose=open(25,false);//don't wait long for a response, don't care if it opens
        goose.close();//despite the short timeout the system will still have triggered ppp demand dial before this close() occurs.
      } catch (Exception any){
        //we are just trying to crank up the ppp earlier, don't care if this fails in any way whatsoever.
      }
    }
  }

  /**
   * this executes on the scheduler's qagent run thread
   * it is the routine that takes too long to timeout
   */
  /**
   * NB: only run on backgroundSocketOpener's thread
   * @param request is internal object for foreground to backgroudn communication
   * @return socket to host
   */
  public Socket makeSocket(SocketRequest request) {
    dbg.Enter("runone");
    try {
      if(assureFactory()){//we have a factory so:
        return makeSecureSocket(); //this is where lots of time is taken
      } else {
        return onError(new java.net.UnknownHostException(StringX.bracketed("No factory for [", ipSpec.toString())));
      }
    } catch (Exception caught){
      return onError(caught);
    } finally {
      dbg.Exit();
    }
  }

  private int timeoutIndexFor(ActionType requestType){
    switch (requestType.Value()) {
      case ActionType.admin:        return SinetTimeout.configuration; //abnormal
      case ActionType.stoodin:      return SinetTimeout.single;
      case ActionType.connection:   return SinetTimeout.configuration;
      case ActionType.update:       return SinetTimeout.configuration;
      case ActionType.clerkLogin:   return SinetTimeout.configuration;
      case ActionType.payment:      return SinetTimeout.single; //shouldn't happen
      case ActionType.adminWeb:     return SinetTimeout.configuration; ////shouldn't happen
      case ActionType.batch:        return SinetTimeout.multiple;
      case ActionType.receiptStore: return SinetTimeout.configuration;
      case ActionType.receiptGet:   return SinetTimeout.configuration;
      case ActionType.store:        return SinetTimeout.multiple;
      case ActionType.ipstatupdate: return SinetTimeout.configuration;
      case ActionType.gateway:      return SinetTimeout.single;
      default:                      return SinetTimeout.single;
    }
  }

  public int timeoutFor(ActionType requestType){
    return timeout.ticksFor(new SinetTimeout(timeoutIndexFor(requestType)));
  }

  public void save(EasyCursor ezc){
    super.save(ezc); //flat storage, no pushing and popping here.
    ezc.setObject(applianceKey,appleId );
    ezc.setObject(UrlpathKey,  UrlPath );
    ezc.setBlock(timeout,timeoutKey);
    ezc.setString(interfaceKey,ifaceStyle);
  }
  /**
   * @return appliance's token to validate client to server.
   */
  private String sharedValidationToken(){
    if(macid==null){//only hit the interface once.
      macid=GetMacid.getIt();
    }
    return macid;
  }

  public void load(EasyCursor ezc){
    //set defaults for super class, which doesn't have the concept (of defaults):
    ezc.Assert(HostToken.creationTimeoutKey,21000);
    ezc.Assert(HostToken.portKey,"443");
    ezc.Assert(HostToken.IPSpecKey,"txnserver:443");
    super.load(ezc);
    if(StringX.NonTrivial(nickName)){//try to make a per-host debugger
      dbg=ErrorLogStream.getExtension(this.getClass(),nickName);
    }
    ifaceStyle=ezc.getString(interfaceKey,"unknown");//default value is for debug legibility

    if(macid==null){//only hit the interface once.
      macid=GetMacid.getIt();
    }
    appleId   =ezc.getString (applianceKey,sharedValidationToken());
    UrlPath   =ezc.getString (UrlpathKey, "/servlets/txn/");
    if(keyStore==null){
      keyStore = new KeyStoreAxess();//
    }
    ezc.getBlock(timeout,timeoutKey);
    timeout.validateTimeouts(super.creationTimeoutMs);//fill in any holes.
    //fyi: retries are used only in slurm
  }

  public SinetHost(String name) {
    super(name);
    dbg=classdbg;
  }

  public SinetHost() {//#needed for newInstance() call in MHSocketFactory single host legacy load routine
    this("txn");
  }

//  public static SinetHost New(EasyCursor ezc){
//    SinetHost newone = new SinetHost(null);
//    newone.load(ezc);
//    return newone;
//  }

  public String toSpam() {
    return EasyCursor.spamFrom(this);
  }

  public String toString() {
    return this.nickName +"@"+this.ipSpec.toString();
  }

}
//$Id: SinetHost.java,v 1.19 2004/04/06 16:01:17 mattm Exp $