package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ExternalTerminal.java,v $
 * Description:  deal with external terminal
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.35 $
 */
import net.paymate.*;
import net.paymate.net.*;
import net.paymate.connection.*;
import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.*;
import net.paymate.util.*;
import net.paymate.terminalClient.PosSocket.*;
import net.paymate.lang.ReflectX;

public class ExternalTerminal extends BaseTerminal implements LineServerUser,ConnectionCallback {
  static ErrorLogStream dbg;

  long connmanTimeout=Appliance.txnHoldoff()+Ticks.forSeconds(1); //eventually a cfg parameter from server.
  Formatter former;
  Action response;
  Waiter waiter=new Waiter();

  public boolean onePerConnect(){
    return former!=null && former.onePerConnect();
  }

  public void ActionReplyReceipt(Action action){
    dbg.VERBOSE("received response");
    response=action;
    dbg.VERBOSE("Saved response:"+response.TypeInfo());
    waiter.Stop();
  }

  public void extendTimeout(int millis) {
    dbg.WARNING("Extending Timeout by:" + millis);
    connmanTimeout = millis; //in case we aren't started yet
    waiter.Extend(connmanTimeout);
  }

  public byte[] onReception(byte[] line){//have received a packet from terminal
    dbg.Enter("onReception");
    try {
      String forspam=Ascii.bracket(line);
      dbg.VERBOSE("input from client:"+forspam);
      ActionRequest request = former.requestFrom(line);//make terminal bytes into paymate request
      if (request==null) {//presume this is an error!
        dbg.ERROR("incoming message not understood:"+forspam);
        return former.replyFrom(null,false);
      }
      //before we add in our privileged info ...
      if (request instanceof BypassRequest) {
        dbg.WARNING("bypassing paymate server");
        return ((BypassRequest)request).response;
      }
      if (request.isFinancial() ) {//insert a txnid if formatter hasn't
        PaymentRequest  freq=(PaymentRequest )request;//cast for legibility
        if(!STAN.isValid(freq.sale.stan)){
          freq.sale.stan = new STAN(newClientStan());//can be more refined later, such as not putting one on reversals
        }
        //+_+ here is where we could fix up the "unknown entry source" by determining if this is a
        //non-debit card transaction, an d if so then seeing if track 2 is real.
      }
      request.setCallback(this);
      //very last point at which clerk info can be added in.
      request.clerk=clerk.Value();
      dbg.WARNING("sending to paymate server:"+request.TypeInfo());
//      dbg.VERBOSE("Request details:"+request.toEasyCursorString());

      //+++ idiot check arguments here. Make PosTerminal's checks sharable (not trivial)
      waiter.prepare();
//dbg.VERBOSE("externalTerminal"+Ascii.bracket(request.origMessage()));
      connectionClient.StartAction(request);
      waiter.Start(connmanTimeout,dbg);
      boolean timedout= ! waiter.is(waiter.Notified);//lump all errors in with timeout
      //if this timeout occurs then the connectionClient itself is hosed, do not standin.
      if(former.isGateway() && !timedout && Action.isComplete(response) && response.Type().is(ActionType.gateway)){
        //2nd chance to standin.
        if(response.reply.ComFailed()){//entry into standin mode will already have occured
          dbg.WARNING("investigating doing a standin");
          response.request=former.openGatewayRequest(response.request.origMessage());
          //need to attach terminal info to the above request!
          // +_+ presently that is a function of the TxnAgent
          response.reply= Standin().whileStandin(response.request);
        }
      }
      return former.replyFrom(response,timedout);
    }
    catch(Exception any){
      dbg.Caught(any);
      return former.onException(any);
    }
    finally {
      dbg.VERBOSE("exiting after replyFrom");
      dbg.Exit();
    }
  }

  public byte [] onConnect(){
    dbg.VERBOSE("just got a connection");
    return null;
  }

  protected void fancyExit(String why){
//what do we do? close port???
    //null references to quash (otherwise irrelevent) memory leaks.
    former=null;
    response=null;
    waiter=null;
    Stop();
  }

  public ExternalTerminal(TerminalInfo termInfo) {////public for load by reflection, uses config from server
    super(termInfo);
    if(dbg==null) dbg=ErrorLogStream.getForClass(ExternalTerminal.class);
    former=null;
    try {
      former=termInfo.getFormatter();
    } catch (Exception ex) {
      dbg.Caught("instantiating a formatter by name",ex);
    } finally {
      if(former==null){
        dbg.ERROR("BAD FORMATTER, filling null with AsciiFormatter");
        former=Formatter.New("AsciiFormatter");
      }
    }
  }

  public static final ExternalTerminal Mock(){
    return new ExternalTerminal(TerminalInfo.fake());
  }

  public final void Start(String name){
    //use the above ezp for any parameters that the server did NOT put into terminfo
    dbg.VERBOSE("setting time format");      //+++ set formatter timezone from termInfo @JW@

    former.setTimeFormat(Appliance.StoreInfo().si.timeZoneName,Appliance.StoreInfo().receipt.TimeFormat);
    dbg.VERBOSE("Instantiated:"+ReflectX.justClassName(former));
    former.setParent(this);
  }


/**
 * only one command seems to matter: "STOP"
 */
  protected int Handle(TerminalCommand tc){
    dbg.VERBOSE("Handling TC:"+tc.Image());
    switch(tc.Value()) {
      default:
        return super.Handle(tc);
      case TerminalCommand.Identify:
  //      System.out.println(this.toString());
      break;

      case TerminalCommand.Shutdown: {
        fancyExit("powering down");
      } break;
    }
    return 0;
  }

  public boolean Post(Object obj){
    if (obj instanceof TerminalCommand) {
      Handle((TerminalCommand)obj);
      return true;//probably ignored.
    }
    if (obj instanceof StoreConfig) {
      connectionClient.setStoreInfo( ((StoreConfig)obj).si);
    }

    return false;
  }

}
//$Id: ExternalTerminal.java,v 1.35 2004/03/10 00:36:35 andyh Exp $