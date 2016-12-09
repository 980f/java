/* $Id: ConnectionDummy.java,v 1.27 2001/07/19 01:06:46 mattm Exp $ */
package net.paymate.connection;
import  net.paymate.util.*;

//for individual sims
import net.paymate.ISO8583.data.*;
import net.paymate.data.*;

public class ConnectionDummy {//dummy class for concurrent development
  static final ErrorLogStream dbg=new ErrorLogStream(ConnectionDummy.class.getName());
//the following "sim" functions could be pushed into  the respective classes,
//allowing for the possibility of calling them reflectively as part of the class
//hierarchy structure. On the other hand that precludes passing info from one
//simulated request to another so we shall leave these here indefinitely.

  protected static final CardReply fakeApproval(CardReply reply,CardRequest req){
    reply.setResponse("00");
    reply.setApproval( Long.toString(req.sale.Amount().Value(),36));
    return reply;
  }

  public static final CardReply sim(CreditRequest req){
    return fakeApproval(new CreditReply(),req);
  }

  public static final CardReply sim(DebitRequest theRequest){
    return fakeApproval(new DebitReply(),theRequest);
  }

  public static final ActionReply sim (ReceiptStoreRequest theRequest){
    dbg.VERBOSE("<signature>");
    dbg.VERBOSE(theRequest.receipt().getSignature().toTransport());
    dbg.VERBOSE("</signature>");
    return (new ReceiptStoreReply()).setState(true);
  }

  public static final ReceiptGetReply sim(ReceiptGetRequest theRequest){
    return new ReceiptGetReply();
  }

  public static final LoginReply sim(LoginRequest theRequest){
    LoginReply lr= new LoginReply();
    EasyCursor ezp=new EasyCursor();
    ezp.setProperty("debitPushThreshold","$0.00");
    ezp.setBoolean("doChecks",true);
    ezp.setBoolean("doDebit",true);
    lr.setCaps(ezp);
    return lr;
  }

  public static final ActionReply simulate(ActionRequest theRequest){
    ActionReply reply = null;
    try{

    switch(theRequest.Type().Value()){
//      case ActionType.fake         :reply = ((FakeActionRequest)theRequest).reply;break;
      case ActionType.update       :reply = new UpdateReply();break;
      case ActionType.clerkLogin   :reply = sim((LoginRequest) theRequest);break;
      case ActionType.tolog        :reply = new ToLog("Log note from server");break;
      case ActionType.toclerk      :reply = new ToClerk("message stream to clerk");break;

      case ActionType.check        :reply = new CheckReply( );break;
      case ActionType.receiptStore :reply = sim ((ReceiptStoreRequest)theRequest);break;
      case ActionType.credit       :reply = sim((CreditRequest)theRequest);break;
      case ActionType.debit        :reply = sim((DebitRequest) theRequest);break;
//      case ActionType.updateJar    :reply = new UpdateJarReply();break;
      case ActionType.receiptGet   :reply = sim((ReceiptGetRequest)theRequest);break;
      /* intermediate classes, not supposed to instantiate them! */
      case ActionType.unknown      :
      case ActionType.admin        :
      case ActionType.message      :
      case ActionType.financial    :
      case ActionType.card         :
      case ActionType.toprinter    ://this guy just ain't 100% implemented

      default                      :reply = new ActionReply();break;
    }
    } finally {
      if(reply != null) {
        reply.status.setto(ActionReplyStatus.SuccessfullyFaked);
      }
      return reply;
    }
  }

}
//$Id: ConnectionDummy.java,v 1.27 2001/07/19 01:06:46 mattm Exp $
