package net.paymate.hypercom;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/hypercom/S8peripheral.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */
import net.paymate.*;//for testing
import net.paymate.peripheral.*;
import net.paymate.util.*;
import net.paymate.serial.*;
import net.paymate.data.*;
import net.paymate.lang.StringX;

public class S8peripheral implements PacketService,QActor {
  static ErrorLogStream dbg;
  QReceiver posterm;

  private PacketServer ps;//retained for debug
  private Packetizer out;//for making commands
  Port port;

//EasyCursor config;

//
//  public PosPeripheral setEnable(boolean beon){
//    //
//    return this;
//  }
//
//  public PinPad Acquire(PinRequest pinreq){
//    S8Command req=S8Command.PinEntryRequest(pinreq.account.Image(),(int)pinreq.amount.Value(),pinreq.isRefund);
//    //send req, reply goes to posterm as a pinDatum.
//    return this;
//  }
  // outgoing
  private QAgent cmdBuffer;//expensive way to get a well managed thread :P
  private Waiter forRetry; //


  public boolean sendCommand(S8Command cmd) {
    if (cmdBuffer == null) {
      cmdBuffer = QAgent.New(port.nickName()+".Q", this);
      cmdBuffer.Start();
    }
    if (forRetry == null) {
      forRetry = Waiter.Create(Ticks.forSeconds(2), false, dbg);//there is no spec for a good timeout value.
    }
    return cmdBuffer.Post(cmd);
  }

  public void runone(Object fromq) {
    if (fromq instanceof S8Command) {
      out.attachTo( (S8Command) fromq);
      for (int retry = 3; retry-- > 0; ) {
        forRetry.prepare();
        Exception e = out.writeOn(port.xmt());
        if (e == null) {
          switch (forRetry.run()) {
            case Waiter.Ready:
            case Waiter.Notified:
              return; //command was accepted
            case Waiter.Timedout:
            case Waiter.Interrupted:
            case Waiter.Excepted:
              continue; //command timedout
          }
        }
      }
      dbg.WARNING("Gave up on retrying:"+fromq);
    }
  }

  public void Stop() {
    //useless piece of legacy...
  }
  /**
 * called when a good packet is received
 */
  public void onPacket(Buffer packet){
    if(Buffer.NonTrivial(packet)){
      AsciiBufferParser reply= AsciiBufferParser.Easy();
      reply.Start(packet);
      String replyType=reply.getUntil('.');
      if(StringX.NonTrivial(replyType)){
        switch(StringX.parseInt(replyType,16)){
          case 0x91:{//revision request
            dbg.VERBOSE("RevisionReply:"+reply.getROF());
          } break;
          case 0x93:{
            dbg.VERBOSE ("Configuration Load Reply received");
          } break;
          case 0x3D:{

          } break;
        }
      } else {
        dbg.WARNING("packet prefix not found");
      }
    } else {
      dbg.WARNING("Trivial packet received");
    }
  }

  public boolean onControlEvent(int controlevent){
    switch (controlevent) {
      case Ascii.ACK: {
        forRetry.Stop();
      } break; //this is good. we can stop waiting
      case Ascii.NAK: {
        dbg.WARNING("Nak on:" + out.body().toSpam());
        forRetry.forceTimeout(); //this is bad, fold in with timeout
      } break;
      case Ascii.EOT:{
        //can send a new command, if were waiting on a response.
      } break;
      case Receiver.TimedOut: {//driver provides these spuriously.
      }
      break;

      default: {
        //post error event if not handled locally
        dbg.ERROR("Unexpected Control Event:" + Receiver.imageOf(controlevent));
      }
    }
    return false;
  }
/**
 * called when line is established, usefull for init of internal logic
 * @return same as onPacket()
 */
  public void onConnect(){
    //reset device, in case we were talking to it
   // sendCommand(S8Command.PinEntryCancel());
  }

  public S8peripheral(Port port) {
    this.port = port;
    dbg = ErrorLogStream.getForClass(this.getClass());
    out = Packetizer.Ascii(110);//MAC generate response.
    ps = PacketServer.Create(this, PacketReceiver.MakeFrom(IcePacketizer.forReception()), port);
  }


  private void test() {
    dbg.setLevel(ErrorLogStream.VERBOSE);
    ps.Start();
    Parameters sp= port.getSettings();
//    for(int baud=19200;baud>=1200;baud>>=1){
//      sp.setBaudRate(baud);
//      port.changeSettings(sp);
//      sendCommand(S8Command.GetRevision());
//      ThreadX.sleepFor(8.0);
//    }
    try {
      int c;
      while ( (c = System.in.read()) != Receiver.EndOfInput) { //blocking read from console
        switch (c) {
          case '1':  sendCommand(S8Command.GetRevision()); break;
          case '2':{
            for(int i=1000000000;i-->0;){
              port.xmt().write(0);
              Thread.yield(); //to make it easier to kill the app.
            }
          } break;
        }
      }
    }
    catch (Exception t) {
      System.out.println("Exception on keybd->commport:" + t);
    }
  }

  public static void main(String[] args) {
    Main thisapp=new Main(S8peripheral.class);
    thisapp.stdStart(args);
    TextListIterator arg=TextListIterator.New(args);
    Parameters sp=Parameters.CommandLine(arg,2400,"N81");
    Port port=PortProvider.openSerialPort("S8tester",sp);
    S8peripheral unit=new S8peripheral(port);
    unit.test();
  }

}
//$Id: S8peripheral.java,v 1.3 2003/07/27 05:35:03 mattm Exp $