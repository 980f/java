package net.paymate.connection;
/**
* Title:        Constants
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Constants.java,v 1.37 2001/10/13 11:02:28 mattm Exp $
*/

import  net.paymate.net.*; //IPSpec and KeyStoreAccess
import  net.paymate.util.*;

public class Constants {
  private ErrorLogStream dbg;
  public static final IPSpec RealServerIp=new IPSpec("txnserver:8443"); //trust local DNS dohickey
  public static final String txnPath="/servlets/txn";

  public String name              = "";
  public IPSpec ipSpec            = RealServerIp;
  public String appleId            = "";
  public KeyStoreAccess keyStore  = new KeyStoreAccess("cacerts","changeit","");
  public String UrlPath           = "";
  public boolean doSecured        = true;

  /**
   * @return true when the form is ok, even if the content is nonsense.
   */
  public boolean isViable(){
    return appleId != null && ipSpec != null && keyStore != null && UrlPath != null;
  }

  /**
   * ensure livable values in every field, most defaults are defined here.
   */
  void Validate(){
    dbg= new ErrorLogStream(Constants.class.getName()/*+"."+name*/);
    if(ipSpec==null){
      ipSpec = RealServerIp;
    }
    if(!Safe.NonTrivial(appleId)){
      appleId  = GetMacid.getIt();
    }
    if(!Safe.NonTrivial(UrlPath)){
      UrlPath = txnPath;
    }
    dbg.VERBOSE("ip: " + ipSpec.toString());
    dbg.VERBOSE("appleId: " + appleId);
    dbg.VERBOSE("urlPath: " + UrlPath);
    dbg.VERBOSE((doSecured ? "" : "NOT ") + "secured");
  }

  public Constants(String nametag,EasyCursor ezp){
  /**
   * legacy, construct from local file/environment variables.
   */
    name  =   nametag;
    appleId   =ezp.getString ("terminalId","");
    UrlPath   =ezp.getString ("path", txnPath);
    ipSpec    =new IPSpec(ezp.getString("ip",RealServerIp.toString()));
    doSecured =ezp.getBoolean("secured",true);

    Validate();
  }

/**
 * uses system defaults.
 */
  public Constants(String nametag){
    name  =   nametag;
    Validate();
  }

  public static final String LF = System.getProperty("line.separator","\n");

  public String toString() {
    // +++ toProperties().asParagraph();
    return //{
      "tag=" + name  + LF +
      "secured=" + doSecured + LF +
      "IPSpec=" + ipSpec + LF +
      "terminalid=" + appleId + LF +
      "KeyStoreAccess=" + keyStore + LF +
      "Urlpath=" + UrlPath + LF;// +
    //}
  }

}
//$Id: Constants.java,v 1.37 2001/10/13 11:02:28 mattm Exp $
