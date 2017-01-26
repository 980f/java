package net.paymate.authorizer.paymentech;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/paymentech/PTGatewayResponse.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.EasyUrlString;
import net.paymate.authorizer.*;
import net.paymate.data.*;
import net.paymate.util.*;

public class PTGatewayResponse extends AuthorizerGatewayResponse {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PTGatewayResponse.class);

  public PTGatewayResponse() {
  }

  public void process(Packet toFinish){
    // let the cast blow.  If it does, you coded things incorrectly
    VisaBuffer vb = (VisaBuffer)toFinish;
    if((vb!=null) && vb.isComplete()){
      action=ActionCode.Approved;
      authcode="";
      authmsg="";
    } else {
      action=ActionCode.Failed;
      authcode="";
      authmsg="incomplete packet";
    }
    if(vb!=null) {
      rawresponse.setrawto(vb.body());
      dbg.WARNING("PTGatewayResponse.process():---"+vb.dump((TextList)null).toString());
    }
  }
}