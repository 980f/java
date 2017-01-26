package net.paymate.connection;

/**
* Title:
* Description: returns terminal configurations to appliance
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author $Author: mattm $
* @version $Id: ConnectionReply.java,v 1.31 2003/10/25 20:34:18 mattm Exp $
* @see ConnectionRequest
*/

import net.paymate.util.*;
import java.util.*;
import net.paymate.data.*; // Terminalid
import net.paymate.lang.StringX;

public class ConnectionReply extends AdminReply implements isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ConnectionReply.class);

  public ActionType Type(){
    return new ActionType(ActionType.connection);
  }
  final static String appidKey=ActionRequest.applianceKey;
  public String applianceID;//very convenient redundancy
  /**
  * storewide information
  */
  final static String cfgKey="config";
  public StoreConfig cfg=new StoreConfig();

  /**
  * the set of multiple terminal's info
  */
  private final static String termlistKey="termlist";
  protected Vector terminalArray=new Vector();

  public ConnectionReply add(TerminalInfo ti){
    dbg.VERBOSE("adding terminalinfo:"+ti.toSpam());
    terminalArray.add(ti);
    return this;
  }

  public int numTerminals(){
    return terminalArray.size();
  }
  /**
  * return all terminals or none.
  */
  public TerminalInfo terminal(int i){
    if(0<=i && i<terminalArray.size()){
      return (TerminalInfo)terminalArray.elementAt(i);
    }
    return null;
  }

  ///////////////////////////
  // transport

  public void save(EasyCursor ezc){
    dbg.Enter("save");
    try{
      super.save(ezc);
      ezc.setString(appidKey,applianceID);
      ezc.setBlock(cfg,cfgKey);
      StringBuffer namelist=new StringBuffer(numTerminals()*18);//WAG
      dbg.ERROR("num terminals:"+numTerminals());
//use hashtable and then saveMap
      for(int i=numTerminals();i-->0;){
        TerminalInfo ti= terminal(i);
        if(ti!=null){
          dbg.VERBOSE("terminalinfo:"+ti.toSpam());
          String name=""+ti.id();
          if(namelist.length()!=0){
            namelist.append(" ");
          }
          namelist.append(name);
          ezc.setBlock(ti,name);
        }
      }
      dbg.VERBOSE("namelist:"+namelist);
      ezc.setString(termlistKey,String.valueOf(namelist));
    }
    finally {
      dbg.Exit();
    }
  }

  public void load(EasyCursor ezc){//called by ActionReply
    dbg.Enter("load");
    try {
      super.load(ezc);
      applianceID=ezc.getString(appidKey);
      ezc.getBlock(cfg,cfgKey);
      TextList namelist=new TextList();
      namelist.wordsOfSentence(ezc.getString(termlistKey));
      dbg.VERBOSE("namelist:"+namelist.asParagraph(" "));
      for(int i=namelist.size();i-->0;){
        String name=namelist.itemAt(i);
        dbg.VERBOSE("name["+i+"] :"+name);
        TerminalInfo ti=new TerminalInfo(new Terminalid(name));
        ti.si=cfg.si;//legacy data path
        ezc.getBlock(ti,name);
        dbg.VERBOSE("loaded:"+ti.toSpam());
        add(ti);
      }
    } finally {
      dbg.Exit();
    }
  }

  public ConnectionReply(String appleID) {
    applianceID=appleID;
  }

  public ConnectionReply() {//needed by action reply
    //initialized in declarations
  }

  //////////
  public boolean compliesWith(ConnectionReply newone){//npe newone
    if(newone==null){
      return false; //don't think this ever happend but...
    }
    if(numTerminals()!=newone.numTerminals()){
      return false; //even if there might have been a match
    }
    for(int i=numTerminals();i-->0;){
      TerminalInfo ti= terminal(i);
      if(ti!=null){
        if(!ti.compliesWith(newone.terminal(i))){
          return false;//---has to be a perfect matc
        }
      }
    }
    //these seem like they should be checked first, but efficiency is irrelevant here.
    //dropped checking cfg when change to always update it was made.
    return StringX.equalStrings(applianceID,newone.applianceID);//&& cfg.equals(newone.cfg);
  }

}
//$Id: ConnectionReply.java,v 1.31 2003/10/25 20:34:18 mattm Exp $
