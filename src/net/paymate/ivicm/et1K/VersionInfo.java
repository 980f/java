package net.paymate.ivicm.et1K;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/VersionInfo.java,v $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: VersionInfo.java,v 1.12 2003/07/27 05:35:05 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.io.IOX;
import java.io.ByteArrayInputStream;
import net.paymate.lang.Bool;
import net.paymate.text.Formatter;

public class VersionInfo implements Callback {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(VersionInfo.class);

  protected ByteArrayInputStream blob;

  protected void spamText(String whatitis,int len){
    dbg.WARNING(whatitis+":"+IOX.fromStream(blob,len));
  }

  protected void spamLength(String whatitis){
    int len=0;
    len=blob.read();//LO byte
    len+=256*blob.read();
    dbg.WARNING(whatitis+":"+len);
  }

  protected void spamTracks(String whatitis){
    StringBuffer msg=new StringBuffer(20);
    msg.append(whatitis);
    msg.append(":");
    int trax=blob.read();
    for(int tnum=3;tnum-->0;){
      if(Bool.bitpick(trax,tnum)){
        msg.append(" ");
        msg.append((tnum+1));
        msg.append(" ");
      }
    }
    dbg.WARNING(String.valueOf(msg));
  }

  protected void spam(byte [] payload){
    blob= new java.io.ByteArrayInputStream(payload);
    spamText("Product ID",13);
    spamText("Application Version",14);
    spamText("System Version",11);
    spamText("Digitizer Version",11);
    spamLength("Flash Size");
    spamLength("Video RAM Size");
    spamTracks("Reads Tracks");
    spamText("Terminal Serial Number",21);
    spamLength("RAM Size");
    spamText("Link Library Version", 8);
    spamText("Security Module Version",8);
    spamText("Downloaded Application Version",8);
    spamText("Downloaded Parameter Version",8);
  }

  public Command Post(Command cmd){
    if(cmd.incoming.isOk()){
      int repcode=cmd.response();
      if(repcode==ResponseCode.SUCCESS){
        spam(cmd.payload());
      } else {
        dbg.ERROR("et1k version fetch reported:"+Formatter.ox2(repcode));
      }
    } else {
      dbg.ERROR("et1k version fetch failed");
    }
    return null;
  }

  public VersionInfo() {
    blob=null;
  }

}
//$Id: VersionInfo.java,v 1.12 2003/07/27 05:35:05 mattm Exp $
