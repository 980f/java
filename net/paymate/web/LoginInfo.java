package net.paymate.web;
/**
 * Title:        LoginInfo<p>
 * Description:  Info retreived from DB upon user login<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: LoginInfo.java,v 1.22 2001/11/17 06:16:59 mattm Exp $
 */

import  net.paymate.web.color.*;
import net.paymate.util.*;

import  net.paymate.connection.ClerkIdInfo;
import net.paymate.ISO8583.data.TerminalInfo;

public class LoginInfo {
  public ClerkIdInfo clerk;
  public TerminalInfo ti; //was already in the query!
  public int terminalID; // passed in
  public ColorScheme colors;
  public int enterpriseID; // from the login
  public String companyName; // from the store
  public String longName; // from the associate
  public String permissions; // from the user+store
  public int storeid; // the store where the terminal is located
  public String terminalName; // from the terminal
  public String colorschemeid; // for generating the colors
  public String loginError;
  public String authTermId; // do not save or load
  public LocalTimeFormat ltf;
  public int associd;

  public boolean permits(UserPermissions userlevel){
    return permissions!=null&&permissions.indexOf(userlevel.Image()) > -1;
  }

  public LoginInfo() {
    clear();
  }

  public String toString() {
    return ""+enterpriseID+":"+storeid+"/"+companyName+":"+terminalID+"/"+terminalName+":"+clerk.Name()+"/"+longName;
  }

  public String forDisplay() {
    return longName + " of " + companyName; // +++ need to put enterprisename, storename, and username, or at least enterprisename and username, or at least username, depends on how we handle this!
  }

  public LoginInfo clear() {
    clerk= new ClerkIdInfo();
    terminalID     = 0;
    colors    = ColorScheme.MONEY;  // the default (from terminal -- should be user ??? ) // +++ set from UserSession when constructed on the server so that the default represents the server default
    enterpriseID   = 0;
    associd        = 0;
    companyName    = "";
    longName       = "";
    permissions    = "";
    storeid        = 0;
    terminalName   = "";
    colorschemeid  = "";
    loginError     = "";
    authTermId     = "";
    return this;
  }
}
//$Id: LoginInfo.java,v 1.22 2001/11/17 06:16:59 mattm Exp $