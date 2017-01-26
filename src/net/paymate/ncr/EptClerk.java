package net.paymate.ncr;

/**
 * Title:        $Source: /cvs/src/net/paymate/ncr/EptClerk.java,v $
 * Description:  ncr's ept payment over serial port
 * Copyright:    Copyright (c) 2001-2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.25 $
 * @todo: make posterminal report failed transaction attempts
 * @todo: don't ACK ENQ's when we are waiting for an auth response.
 * @todo: finish configuration and startup code!
 */

import net.paymate.serial.*;
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.ReflectX;
import net.paymate.net.*;
import net.paymate.terminalClient.*;
import net.paymate.connection.*;
import net.paymate.awtx.RealMoney;
import net.paymate.jpos.data.*;

public class EptClerk extends AutoClerk implements PacketService {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EptClerk.class);
  /*final*/ static String goodauth="AP";//from 'scope, must match 'register #54'
  /*final*/ static String declined="08";//from 'scope,// +_+ two digit Visa code

  final static int requestSize=8+3+20+4+8+9+10 + 1; ////size of packet that we receive
  final static int replySize=8+20+4+2+16 + 1;//size of what we send

  Port            port;
  Packetizer      response;
  PacketReceiver  receiver;
  PacketServer    server;

  int lane;

  protected void sendCtl(byte ctl){
    dbg.WARNING("sendCtl:"+Ascii.image(ctl));
    if(port!=null){
      port.lazyWrite(ctl);
    }
  }

  private void localError(AsciiBuffer buf,String detail){
    buf.append(declined);// +_+ should be two digit Visa code
    buf.appendAlpha(16,detail);
  }
/**
 * take paymate format data and produce a PNC330 style packet
 */
  private void translateAuth(AsciiBuffer buf,PaymentRequest cardreq,PaymentReply reply){
    dbg.Enter("translateAuth");
    try {
      response.reset();//were holding on to last one for NAK
      if(reply.Succeeded()){
        STAN echo = cardreq.sale.stan;
        buf.appendNumber(8,echo.value());
        buf.appendAlpha(20,greeked(cardreq.card.accountNumber));
//truncation now includes expiration date
//        buf.appendAlpha(4, cardreq.card.expirationDate.mmYY());
        buf.appendAlpha(4, "****");//truncated expiration date.
        if(reply.isApproved()){
          dbg.WARNING("approved");
          buf.append(goodauth);
          buf.appendAlpha(6,reply.Approval()) ;
        } else {
          dbg.WARNING("declined");
        // spec was wholly wrong on this. We will pass auth message and hope that suffices.
          buf.append(declined);
          buf.appendAlpha(16,reply.authMessage());
        }
      } else {
        dbg.ERROR("failed, what do we do???");
        localError(buf,"No Response (1)");
      }
    } catch (Exception ex) {
      dbg.Caught("while processing reply",ex);
      localError(buf,"No Response (2)");
    } finally {
      dbg.Exit();
    }
  }

  protected int Become(int AutoClerkStateValue){
    //detect state changes here
    int previous=super.Become(AutoClerkStateValue);
    if(! progress.is(previous)){
      dbg.VERBOSE("statechange:"+ AutoClerkState.toString(previous)+" => "+progress);
      //just hooked for now
    }
    return previous;
  }

  public boolean ask(int clerkitem){// @see ClerkItem.Enum
    switch (clerkitem) {
      default:                        return super.ask(clerkitem);
/* if we are cancelling sig acquisition because a new request has come in
then we also need to not hang around waiting for a second copy
Since we expect to NOT have a printer we will universally stifle this question,
there will NEVER be a second copy */
      case ClerkItem.SecondCopy:    return cancelThis(clerkitem);
    }
  }

  public void authResponse(PaymentRequest cardreq,PaymentReply reply){
    dbg.Enter("authResponse");
    try {
      translateAuth((AsciiBuffer) response.body(),cardreq,reply);
    } finally {
      response.writeOn(port.xmt());//send to NCR register
      Become(AutoClerkState.responded);
      dbg.Exit();
    }
  }

  private void posCancel(){
    Post(new TerminalCommand(TerminalCommand.Clear));
  }

  private void ack(){
    sendCtl(Ascii.ACK);
  }

  private void NAK(){
    sendCtl(Ascii.NAK);
  }

  public boolean onControlEvent(int controlevent){
    dbg.Enter("controlevent."+Receiver.imageOf(controlevent));
    try {
      dbg.VERBOSE("state:"+progress.toString());
      switch(controlevent){
        case Ascii.ENQ: {
          switch (posstate.Value()) {
            default:
            case PosTerminalState.Occupied:{ //very bad.
              dbg.ERROR("ENQ when authing is ignored, posterm is busy");
              NAK();
            } break;
            case PosTerminalState.Loafing: //+_+ is cancel too severe for this?
            case PosTerminalState.Cancellable: //then cancel it
              posCancel(); //get it ready for new operation
            case PosTerminalState.Ready: //
              receiver.restart();//in case fragmentary packet has been received.
              response.reset();//4debug
              Become(AutoClerkState.awake);
//              dbg.ERROR("Acking ENQ");
              ack();
//              dbg.ERROR("after Acking ENQ");
            break;
          }
        } break;
        case Ascii.ACK: {
          if(progress.is(AutoClerkState.responded)){
            Become(AutoClerkState.idle);
            sendCtl(Ascii.EOT);
          } else {
            dbg.ERROR("unexpected ACK in state "+progress);
          }
        } break;
        case Ascii.NAK: {
          dbg.WARNING("about to resend response");
          response.writeOn(port.xmt());//resend last
        } break;
        default: {
          dbg.WARNING("garbage event");
          //garbage in, silence out --- sendCtl(Ascii.NAK);//--- this is probably a bad thing to do.
        } break;
      }//
    }
    catch(Exception any){
      dbg.Caught(any);
    }
    finally {
      dbg.Exit();
      return false;
    }
  }

  private STAN expandStan(int suffix){
    int decimalplace= IntegralPower.Above(suffix,10).power;
    if(decimalplace<100){//@installation, numbers 1..9 nned to be 01..09.
      decimalplace=100;
    }
    suffix+= decimalplace*lane;
    return STAN.NewFrom(suffix);
  }
/**
 * when packet received send a request to siNet server.
 */
  public void onPacket(Buffer packet){
    dbg.Enter("onpacket");
    try {
      dbg.VERBOSE(packet.toSpam());
      BufferParser bp=BufferParser.Slack().Start(packet);
      //to fixed 8 integer sequence number
      //strip leading zeroes and insert lane number.
      STAN seqnum=expandStan(bp.getDecimalInt(8));
      if(seqnum.isValid()){
        dbg.WARNING("have stan:"+seqnum);
        Post(seqnum);
        int actioncode=bp.getDecimalInt(3); //"prog 54" presently always 001
        dbg.WARNING("actioncode:"+actioncode);

        String twentyspaces=bp.getFixed(20); //unused field
        int fourzeroes=bp.getDecimalInt(4); //unused field
        long saleamount=bp.getDecimalLong(8);
        //shall we check or discard rest of packet?
        int morezeroes=bp.getDecimalInt(9);//all zeroes
        int tenones=bp.getDecimalInt(10); //all ones "product code"

        if(actioncode==2){
          saleamount=-saleamount;
        }
        if(startSale(saleamount)){//if successfully extracted info then
          dbg.WARNING("ACKing request");
          ack();
          return;
        }
      }
      dbg.ERROR("NAKing bad request packet");
      //we only get here if something is wrong with packet
      NAK(); //trditionally one would count these and send an EOT after the third.
    }
    finally {
      dbg.Exit();
    }
  }

  boolean havetranzport;
  private void spam2port(String msg){
    if(havetranzport){
      sendCtl(Ascii.DC1); //to printer port
      port.lazyWrite(msg);//for testing rs232 monitor.
      sendCtl(Ascii.DC3); //switches mux box to port A
    }
  }

  /**
   * @return same as onPacket()
   */
  public void onConnect(){
    dbg.WARNING("Kicking mux box");
    spam2port("paymate online");
    Become(AutoClerkState.idle);
  }

  /**
   * this is called when AutoClerk is ready for actions
   */
  protected void commonStart(){
    dbg.VERBOSE("commonStart");
    super.commonStart();//first establish posterm end of connections
    server.Start();
  }

  void prelaod(){
    dbg.VERBOSE("eptclerk prelaod");
    lane=0; //might be freshChoice specific...
    amountImpliesFunction=true; //NCR doesn't do this yet.
    alwaysSetFunction=true;    //letting clerkpad set function.
    response= Packetizer.Ascii(replySize);
    if(response==null){
      dbg.ERROR("eptclerk failed to create response object");
    }
    dbg.VERBOSE("exit constructor");
  }

  public void load(EasyCursor ezc){
    dbg.VERBOSE("loading");
    prelaod();//dammit
    super.load(ezc);
    ezc.push(ReflectX.shortClassName(this));
    try {
      havetranzport=ezc.getBoolean("TRANZport");//must precede opening port so that port open debug can use what this controls.
      lane= ezc.getInt("lane");
      amountImpliesFunction=ezc.getBoolean(amountImpliesFunctionKey);
      alwaysSetFunction=ezc.getBoolean(alwaysSetFunctionKey);

      port = PortProvider.openPort("ncrept",ezc);
      dbg.VERBOSE("portspam:"+port.toSpam());
      server = PacketServer.ServeVisaBasic(this,requestSize,this.port);
      receiver=server.getReceiver();
      if(receiver==null){
        dbg.ERROR("BAD RECEIVER");
      }
    }
    finally {
      ezc.pop();
    }
  }

  private EptClerk(){
    super(); //4breakpoint
    dbg.VERBOSE("eptclerk constructor");
    prelaod();
    dbg.VERBOSE("exit constructor");
  }

  /**
   * this is for use by AutoClerk.makeFrom()
   */
  public static AutoClerk Create(EasyCursor ezc){
    dbg.VERBOSE("eptclerk Create");
    try {
      return new EptClerk();//make someone else run load();
    } finally {
      dbg.VERBOSE("eptclerk Create");
    }
  }

}
//$Id: EptClerk.java,v 1.25 2003/10/25 20:34:23 mattm Exp $