/* $Id: POSKeyboardService.java,v 1.12 2002/07/09 17:51:26 mattm Exp $ */

package net.paymate.ivicm.ec3K;
import net.paymate.util.*;

public class POSKeyboardService extends Service {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(POSKeyboardService.class);
  static final String VersionInfo= "CM3000 POSKeybd (C) PayMate.net 2000 $Revision: 1.12 $";

  public POSKeyboardService(String s, EC3K hw){
    super(s,hw);
//    identifiers(VersionInfo,Version1dot4,"EC3K Keyboard");
  }

  public void Post(boolean failed, RcvPacket incoming){
    if(!failed){
      if(incoming.response()==EC3K.STATUS_SUCCESS){
        PostData( new Integer(incoming.bight(4)));
      }
    }
    else {
      PostFailure("ec3k.posky.post.command failed");
    }
  }

}
//$Id: POSKeyboardService.java,v 1.12 2002/07/09 17:51:26 mattm Exp $
