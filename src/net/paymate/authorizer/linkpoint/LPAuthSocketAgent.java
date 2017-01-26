package net.paymate.authorizer.linkpoint;

import net.paymate.authorizer.*;
import net.paymate.net.*; // IPSpec
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.net.*;
//import net.paymate.connection.Constants;
import clearcommerce.ssl.*;
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*; // MerchantInfo
import java.io.*; // File, PrintWriter, BufferedWriter, yada yada
import java.util.Vector;
import java.net.*;
//import javax.net.ssl.*;
//import javax.net.*;
//import java.security.*;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/linkpoint/LPAuthSocketAgent.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

public class LPAuthSocketAgent extends AuthSocketAgent {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LPAuthSocketAgent.class, ErrorLogStream.VERBOSE);

  public LPAuthSocketAgent(Packet vb, Authorizer handler) {
    super(vb, handler);
  }

  public boolean sendRequest(AuthTransaction authTransaction) {
    boolean ret = false;
    try {
      handler.println("sendRequest starting run method");
      int sent = 0;
      int received = 0;
      JCharge charge = null;
      LinkpointAuthRequest request = (LinkpointAuthRequest)authTransaction.request;
      LinkpointAuthResponse response = (LinkpointAuthResponse)authTransaction.response;
      // +++ code to use multiple IP's +++
      IPSpec ipspec = handler.ips.currentHost().ipSpec;
      String host = ipspec.address; // eg: staging.linkpt.net
      int port = ipspec.port; // eg: 1139
      request.req.setHost(host);
      request.req.setPort((short)port);
      try {
        handler.println("About to send [" + request + "]");
        Alarmer.reset(handler.timeout, alarmum);  // setup an alarmer to kill me if I don't come back within TIMEOUT seconds!
        JPayment payment = new JPayment(); // only used to process the transaction
        charge = payment.processTransaction(request.req, request.order);
        handler.writes.add(1);
        Alarmer.Defuse(alarmum);
      } catch(Exception exception2) {
        handler.println("AuthSocketAgent had error in transmission:"+exception2);
        dbg.Caught(exception2);
      } finally {
        if(!shouldDie) {
          handler.reads.add(1);
          // parse response ...
          response.parse(charge);
          handler.println("AuthSocketAgent received [" + response + "]"); // MUST set the charge via parse before printing it!
        }
        handler.println("AuthSocketAgent is closing.");
        kill();
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      handler.PANIC("AuthSocketAgent.sendRequest()-Exception: "+t);
    } finally {
      shouldDie = false;
      return ret;
    }
  }
}
