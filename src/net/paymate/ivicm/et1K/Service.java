package net.paymate.ivicm.et1K;

/* $Id: Service.java,v 1.32 2003/07/27 05:35:05 mattm Exp $ */

import net.paymate.ivicm.Base;
import net.paymate.util.*;
import net.paymate.text.Formatter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
* manage response to ping command
*/

/**
* common parts of enTouch based jpos devices.
*/
public class Service extends Base {
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(Service.class);
  static final String VersionInfo = "ET1K Service, (C) Paymate 2000 $Revision: 1.32 $";
  protected ET1K hardware;

  public Service(String s,ET1K hw){
    super(String.valueOf(hw));
    hardware = hw;
  }

  protected Command finish(Command cmd, Callback cb){
    if(cmd!=null){//pathc for setMainPoll to null +_+
      cmd.service=this;
      cmd.onReception=cb;
    }
    return cmd;
  }

    /**
   * gotData is only relevant for commands that poll for data
   */
  public boolean gotData(Command cmd){
    int rsp=cmd.response();
    if(cmd.nothingThere(rsp)){//no data present
      return false;
    }
    if(rsp!=0){
      PostFailure(cmd.errorNote+ "PollFailed:"+rsp);
      return false;
    }
    return true;
  }

  protected Command QueueCommand(Command cmd){
    hardware.QueueCommand(cmd);
    return cmd;
  }

  protected Command QueueCommand(Command cmd, Callback cb){
    return QueueCommand(finish(cmd,cb));
  }

  /**
   * sends command now but also saves it for reconnect
   */
  protected Command ConfigurationCommand(Command cmd, Callback cb){
    hardware.setStartup(finish(cmd,cb));
    return cmd;
  }

  protected void Get(int pollcommand,String locus,Callback cb){//fue
    QueueCommand(new Command(pollcommand,locus),cb);
  }

  protected void JustSend(LrcBuffer rawcmd,String forError){//fue
    JustSend(new Command(rawcmd,forError));
  }

  protected void JustSend(Command cmd){//fue
    QueueCommand(cmd,new WantZero(cmd.errorNote));
  }

  protected void Issue(int pollcommand,String locus){//fue
    QueueCommand(new Command(pollcommand,locus),new WantZero(locus));
  }

  public void getVersionInfo(){
    Command gs=new Command(OpCode.getVersionInfo,"gettingVersionInfo");
    QueueCommand(gs,new VersionInfo());
  }

  public String ErrorMessage(int responsecode){
  switch(responsecode){
  case 0x70: return "No OS loaded, or Level 0 diagnostic test failed";
  case 0x80: return "More data available";
  case 0x90: return "No Application loaded";
  case 0xC7: return "MSR Data unreadable";
  case 0xE0: return "MSR busy, already enabled with function code 62/80";
  case 0xE1: return "Mismatch of Base/Secure firmware, OS, or model";
  case 0xE6: return "Invalid DATA field in host message(not enough or too much data)";
  case 0xE7: return "Invalid hardware configuration";
  case 0xE8: return "Invalid record type";
  case 0xE9: return "Invalid page";
  case 0xEA: return "Invalid Page Offset";
  case 0xEC: return "Invalid parameter in data field";
  case 0xED: return "Cancel button touched on numeric keypad";
  case 0xEE: return "Insufficient memory";
  case 0xEF: return "Power failure occurred";
  case 0xF0: return "No data available";
  case 0xF1: return "Invalid VLI field in host message";
  case 0xF2: return "Communications time-out occurred.";
  case 0xF4: return "Invalid time value";
  case 0xF5: return "Communications adapter failure (check tallies)";
  case 0xF6: return "Invalid screen number";
  case 0xF7: return "Invalid sequence number";
  case 0xF8: return "Flash memory compression failed";
  case 0xFA: return "Invalid mode for command";
  case 0xFB: return "Receive buffer full/message overflow";
  case 0xFD: return "Invalid command, or function code missing in host message";
  case 0xFF: return "Invalid BCC in host message";
  default: return "Unusual error, code:"+Formatter.ox2(responsecode);
  }

  }

}
//$Id: Service.java,v 1.32 2003/07/27 05:35:05 mattm Exp $
