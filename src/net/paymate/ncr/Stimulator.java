package net.paymate.ncr;

/**
 * Title:        $Source: /cvs/src/net/paymate/ncr/Stimulator.java,v $
 * Description:  test EptClerk using console input rather than real device.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.11 $
 */

import net.paymate.*;
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.net.*;
import net.paymate.serial.*;

import net.paymate.terminalClient.*;
import net.paymate.connection.*;
import net.paymate.awtx.RealMoney;
import net.paymate.jpos.data.*;

import java.io.*;

public class Stimulator extends TrivialPacketService {
  protected ErrorLogStream dbg;

  EptClerk sinet;
  boolean viaSerial;

/////////////
//
  boolean waitenqack=false;//waiting for ack to ENQ
  AsciiBuffer simrequest;
  public boolean onControlEvent(int controlevent){
    dbg.VERBOSE("controlevent:"+Receiver.imageOf(controlevent));
    switch(controlevent){
      case Ascii.ACK: {
        if(waitenqack){
          waitenqack=false;
          //send simluated request
          if(Buffer.NonTrivial(simrequest)){
            response.attachTo(simrequest);
            response.writeOn(port.xmt());
          }
        } else {
          dbg.ERROR("request accepted");
        }
      } break;
      case Ascii.NAK: {
        dbg.ERROR("request rejected, don't know what to do now");
      } break;
      case Ascii.EOT: {
        dbg.ERROR("response acknowledged, all done");
      } break;
      default: {
        dbg.ERROR("unexpected control event:"+Receiver.imageOf(controlevent));
      } break;
    }
    return false;
  }

  /**
   * if simluating then we have received a response packet, parse it verbosely
   */
  public void onPacket(Buffer packet){
    BufferParser bp=BufferParser.Slack().Start(packet);
    dbg.ERROR("Stan:"+bp.getDecimalInt(8));
    dbg.ERROR("acct:"+bp.getFixed(20));
    dbg.ERROR("expir"+bp.getFixed(4));
    String twochar=bp.getFixed(2);
    if(StringX.charAt(twochar,0,'X')=='A' && StringX.charAt(twochar,1,'X')==0 ){
      dbg.ERROR("approval:"+bp.getFixed(6));
    } else {
      dbg.ERROR("declined:"+twochar);
      dbg.ERROR("message:"+bp.getTail());
    }
    port.lazyWrite(Ascii.ACK);
  }

  public void onConnect(){
    dbg.VERBOSE("onConnect() not overloaded");
  }
//
////////////////////
  int stanometer =1556;
  public AsciiBuffer simRequest(long cents){
    AsciiBuffer sim= AsciiBuffer.Newx(8+3+20+4+8+9+10 + 1);
    sim.appendNumber(8,++stanometer);
    sim.appendNumber(3,1); //"prog 54" presently always 001
    sim.appendAlpha(20,""); //unused field
    sim.appendNumber(4,0); //unused field
    sim.appendSigned(8,cents);//signed
    sim.appendNumber(9,0);//all zeroes
    sim.appendNumber(10,1111111111); //all ones "product code"
    return sim;
  }

  public Stimulator() {
    dbg=ErrorLogStream.getForClass(this.getClass()).setLevel(LogLevelEnum.VERBOSE);
  }

  protected void sendRequest(AsciiBuffer simrequest){
    if(viaSerial){
      waitenqack=true;
      this.simrequest=simrequest; //store, don't send until get ack on ENQ.
      port.lazyWrite(Ascii.ENQ);
    } else {
      sinet.onPacket(simrequest);
    }
  }

  protected boolean startSale(long cents){
    dbg.WARNING("starting a sale:"+cents);
    if(cents!=0){
      sinet.onControlEvent(Ascii.ENQ);
      sendRequest(simRequest(cents));
      return true;
    } else {
      dbg.ERROR("must be a nonzero amount");
      return false;
    }
  }

  protected void respond(PaymentRequest cardreq,PaymentReply reply){
    sinet.authResponse(cardreq,reply);
  }

  public void fakeAuth(String authcode,boolean approved){
    PaymentRequest cardreq= PaymentRequest.CreditRequest(SaleInfo.fakeOne(),MSRData.fakeOne());
    cardreq.sale.stan= STAN.NewFrom(stanometer);
    PaymentReply reply= new PaymentReply();
    reply.setState(true);
    if(approved){
      reply.setApproval(authcode);
    } else {
      reply.setAuth(AuthResponse.mkDeclined(authcode));
    }
    respond(cardreq,reply);
  }

  public void Simulate(BufferedReader inline) {
    dbg.ERROR("starting simulation");
    String command;
    while(true){
      try {
        command= inline.readLine();
        dbg.ERROR("readline:"+command);
        if(StringX.NonTrivial(command)){
          if(command.charAt(0)=='A'){
            fakeAuth(StringX.restOfString(command,1),true);
          } else if(command.charAt(0)=='D'){
            fakeAuth(StringX.restOfString(command,1),false);
          }
          else {
            dbg.ERROR("startSale"+Ascii.bracket(command)+" gives:"+startSale(LedgerValue.parseImage(command)));
          }
        } else {
          dbg.ERROR("usage: Aauthcode | Dreason | decimal= sale or refund according to sign ");
        }
      }
      catch (Exception ex) {
        dbg.Caught("reading console",ex);
      }
    }
  }


  public void load(EasyCursor ezc){
    super.load(ezc); //especially for port specification.
    dbg.setLevel(ezc.getEnumValue("loglevel",LogLevelEnum.Prop));
  }

  public static void main(String[] args) {
    Class thisClass=Stimulator.class;
    Main me= new Main(thisClass);
    LogSwitch.SetAll(LogSwitch.ERROR);
    PrintFork.SetAll(LogSwitch.VERBOSE);
    me.stdStart(args);
    LogSwitch.setOne("data.Buffer",LogSwitch.ERROR);

    EasyCursor cfg = me.Properties(thisClass); //get a clean cfg, no java env.

    Stimulator sim = new Stimulator();


    cfg.push("sim");
    sim.load(cfg);
    cfg.pop();


    cfg.push("testee");
    sim.sinet= (EptClerk) AutoClerk.makeFrom(cfg);
    sim.sinet.setLink(SimPosTerminal.fake());
    sim.sinet.onConnect();
    cfg.pop();


//    sim.load(cfg);
    BufferedReader inline=new BufferedReader(new InputStreamReader(System.in));
    sim.Simulate(inline);
  }

}
//$Id: Stimulator.java,v 1.11 2003/12/10 02:16:53 mattm Exp $