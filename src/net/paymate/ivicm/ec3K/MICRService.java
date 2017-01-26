/* $Id: MICRService.java,v 1.14 2002/07/09 17:51:26 mattm Exp $ */
package net.paymate.ivicm.ec3K;
import net.paymate.jpos.data.MICRData;
import net.paymate.util.*;

import java.util.*;

public class MICRService extends Service {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(MICRService.class);
  static final String ModuleInfo = "MICRService (C) PayMate.net 2000";
  static final String VersionInfo="$Revision: 1.14 $";

  public MICRService(String s, EC3K hw) {
    super(s, hw);
//    identifiers(ModuleInfo,VersionInfo,"CM3000 Check Reader");
  }

  public void onTimeout() {
    //placeholder.
  }

  public void Acquire() {
    //this device is always willing to acquire, the app can ignore undesired input.
  }

  /**
   * called when receiver has a packet identified as a mcirline
   */
  public void Post(boolean failed, RcvPacket cmd) {
    MICRData newone=new MICRData();
    try {
      if(failed){
        dbg.ERROR("Bad Packet Received");
        PostFailure("Corrupt micr data received");//+++ extend problem class
      } else {
        if(cmd.response()==0){
          TextList micrErrors= newone.Parse(cmd.payload());
          if (micrErrors.size()>0) {
            dbg.WARNING("Micr Parser Errors:\n"+micrErrors.asParagraph());
          }
          PostData(newone);
        } else {
          dbg.ERROR("Device reports: " + cmd.response());
          PostFailure("Unhandled MICR communications status:"+ cmd.response());
        }
      }
    }
    catch (Exception  jape){
      dbg.Caught(jape);
      PostException("In posting raw micr data",jape);
    }
  }

}
//$Id: MICRService.java,v 1.14 2002/07/09 17:51:26 mattm Exp $
