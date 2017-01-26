/* $Id: ConnectionDummy.java,v 1.38 2004/02/07 22:35:44 mattm Exp $ */
package net.paymate.connection;
import  net.paymate.util.*;

//for individual sims
import net.paymate.data.*;

public class ConnectionDummy {//dummy class for concurrent development
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(ConnectionDummy.class);
//the following "sim" functions could be pushed into  the respective classes,
//allowing for the possibility of calling them reflectively as part of the class
//hierarchy structure. On the other hand that precludes passing info from one
//simulated request to another so we shall leave these here indefinitely.

  // for a card
  protected static final PaymentReply fakeApproval(PaymentReply reply,PaymentRequest req){
//    reply.setResponse("00");
    reply.setApproval( Long.toString(req.sale.Amount().Value(),36));
    return reply;
  }

  public static final PaymentReply sim(PaymentRequest req){
    return fakeApproval(new PaymentReply(),req);
  }

  public static final ActionReply sim (ReceiptStoreRequest theRequest){
//    dbg.VERBOSE("<signature>");
//    dbg.VERBOSE(theRequest.receipt().getSignature());
//    dbg.VERBOSE("</signature>");
    return (new ReceiptStoreReply()).setState(true);
  }

//  public static final ReceiptGetReply sim(ReceiptGetRequest theRequest){
//    return new ReceiptGetReply();
//  }

  public static final LoginReply sim(LoginRequest theRequest){
    LoginReply lr= new LoginReply();
    EasyCursor ezp=new EasyCursor();
    ezp.setProperty("debitPushThreshold","$0.00");
    ezp.setBoolean("doChecks",true);
    ezp.setBoolean("doDebit",true);
    lr.setCaps(ezp);
    return lr;
  }

  public static final ActionReply simulate(ActionRequest theRequest){//+_+ crippled by PaymentRequest change
    ActionReply reply = null;
    try{

    switch(theRequest.Type().Value()){
//      case ActionType.fake         :reply = ((FakeActionRequest)theRequest).reply;break;
      case ActionType.update       :reply = new UpdateReply();break;
      case ActionType.clerkLogin   :reply = sim((LoginRequest) theRequest);break;
      case ActionType.receiptStore :reply = sim ((ReceiptStoreRequest)theRequest);break;
//      case ActionType.updateJar    :reply = new UpdateJarReply();break;
//      case ActionType.receiptGet   :reply = sim((ReceiptGetRequest)theRequest);break;
      case ActionType.payment      :reply = sim((PaymentRequest)theRequest);break;
      /* intermediate classes, not supposed to instantiate them! */
      case ActionType.unknown      :
      case ActionType.admin        :
      default                      :reply = ActionReply.Bogus("not simulated");break;
    }
    } finally {
      if(reply != null) {
        reply.status.setto(ActionReplyStatus.SuccessfullyFaked);
      } else {
        reply= ActionReply.rawReplyFor(theRequest);
        reply.Errors.Add("simulator reply was null");
        reply.setState(false);
      }
      return reply;
    }
  }

}
//$Id: ConnectionDummy.java,v 1.38 2004/02/07 22:35:44 mattm Exp $
