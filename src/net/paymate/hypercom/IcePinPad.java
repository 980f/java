package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IcePinPad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.15 $
 */
import net.paymate.peripheral.*;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.RealMoney;//amount, needed for prompt, may or may not get folded into encryption


public class IcePinPad extends PinPad {
  private IceTerminal parent;
  private String prompt;
  private PinRequest pinreq;

  public PosPeripheral setEnable(boolean beon){
    IceCommand getpin=null;
    if(beon){
      if (pinreq!=null) {
        getpin= IceCommand.Create(2+1+20+12+16);//max accountnumber, ridiculous amount, prompt.
        getpin.append(IceCommand.Pinpad);
        getpin.append(IceCommand.Start);
        getpin.append(pinreq.isRefund ? 'C' : 'D' );
        getpin.frame(pinreq.amount);
        getpin.frame(pinreq.account);
        getpin.frame(prompt);
        pinreq=null; //only tolerate one enable per request
      } else {
        setEnable(false); //enhances chances of defective caller retrying.
      }
    } else {
      getpin= IceCommand.Enabler(IceCommand.Pinpad,false);
    }
    if(getpin!=null){
      parent.sendCommand(getpin);
    }
    return this;
  }

  public PinPad Acquire(PinRequest pinreq){
    this.pinreq=pinreq;
    prompt= (pinreq.isRefund ? "You Get:" : "You Pay:") + pinreq.amount.Image();//max 11 of text to get room for $9999.99
    setEnable(true);
    return this;
  }

  public void process(AsciiBufferParser bp){
    switch (bp.getChar()) {
      case 'P':{//ksn and pin
        String ksn=bp.getROF();//more than 16 hex chars, must keep as string
        Post(PINData.Dukpt(bp.getROF(),ksn));
      } break;
      case 'E':{//error, with message
        Post(PINData.Error("Hardware Failure:"+bp.getTail()));
      } break;
      case 'K': {//too many keys
        int howmany=(int)bp.getDecimalFrame();
        Post(PINData.Error("Too "+ (howmany>4?"Many":"Few")+" PIN digits"));
      } break;
      case 'C':{ //user cancelled
        postCancel();
      } break;
    }

  }

  public IcePinPad(IceTerminal parent) {
    super(parent);//IceTerminal wraps the posterminal to ease order of construction (this reference is a QReceiver)
    this.parent=parent; //(this reference stays an IceTerminal)
  }

  void testpin(){
    Acquire(PinRequest.From(new CardNumber("5123456789012346"),new RealMoney(985),false));
  }

}
//$Id: IcePinPad.java,v 1.15 2003/06/17 16:06:30 andyh Exp $
