package net.paymate.ncr;

import net.paymate.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/ncr/Simulator.java,v $
 * Description:  simulate ncr register via console
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */


import net.paymate.serial.*;
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.net.*;
import net.paymate.terminalClient.*;
import net.paymate.connection.*;
import net.paymate.awtx.RealMoney;
import net.paymate.jpos.data.*;


import java.io.*;

public class Simulator extends TrivialPacketService {

  boolean waitenqack=false;
  AsciiBuffer simrequest;

  int stanometer =1556;
  public AsciiBuffer simRequest(long cents){
    AsciiBuffer sim= AsciiBuffer.Newx(8+3+20+4+8+9+10 + 1);
    sim.appendNumber(8,++stanometer);
    sim.appendNumber(3,1); //"prog 54" presently always 001
    sim.appendAlpha(20,""); //unused field
    sim.appendNumber(4,0); //unused field
    sim.appendNumber(8,cents);//signed
    sim.appendNumber(9,0);//all zeroes
    sim.appendNumber(10,1111111111); //all ones "product code"
    return sim;
  }

  void simevent(int controlevent){
    switch(controlevent){
      case Ascii.ACK: {
        if(waitenqack){
          waitenqack=false;
          //send simluated request
          if(Buffer.NonTrivial(simrequest)){
            response.attachTo(simrequest);
            response.writeOn(port.xmt());//resend last
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


  protected boolean startSale(long cents){
    dbg.WARNING("starting a sale:"+cents);
    if(cents!=0){
      simrequest=simRequest(cents);
      waitenqack=true;
      port.lazyWrite(Ascii.ENQ);
    } else {
      dbg.ERROR("must be a nonzero amount");
      waitenqack=false;
    }
    return waitenqack;
  }

  public void Simulate(BufferedReader inline) {
    dbg.ERROR("starting simulation");
    String saleamount;
    LedgerValue saleamt;
    while(true){
      try {
        saleamount= inline.readLine();
        dbg.ERROR("readline:"+saleamount);
        if(StringX.NonTrivial(saleamount)){
          dbg.ERROR("startSale"+Ascii.bracket(saleamount)+" gives:"+startSale(LedgerValue.parseImage(saleamount)));
        } else {
          dbg.ERROR("invalid amount, non zero value required");
        }
      }
      catch (Exception ex) {
        dbg.ERROR("reading console:"+ex);
      }
    }
  }

  public static void main(String[] args) {
    Main me= new Main(Simulator.class);
    LogSwitch.SetAll(LogSwitch.ERROR);
    PrintFork.SetAll(LogSwitch.VERBOSE);
    EasyCursor cfg = me.Properties(Simulator.class); //get a clean cfg, no java env.

    Simulator pos = new Simulator();
    pos.dbg.setLevel(LogLevelEnum.VERBOSE);
    pos.load(cfg);
    BufferedReader inline=new BufferedReader(new InputStreamReader(System.in));
    pos.Simulate(inline);
  }

}
//$Id: Simulator.java,v 1.5 2003/10/25 20:34:23 mattm Exp $