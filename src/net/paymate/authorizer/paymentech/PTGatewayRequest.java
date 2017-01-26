package net.paymate.authorizer.paymentech;


/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/paymentech/PTGatewayRequest.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.authorizer.*;
import net.paymate.data.VisaBuffer;

public class PTGatewayRequest extends AuthorizerGatewayRequest {

  public PTGatewayRequest(byte [] bytes, PaymentechAuth auth) {
    VisaBuffer vb = VisaBuffer.NewSender(bytes.length);
    vb.setClipLRC();
    vb.append(bytes);
    vb.end();
    this.bytes = auth.addHeader(vb.packet());
  }

}