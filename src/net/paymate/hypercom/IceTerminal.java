package net.paymate.hypercom;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/hypercom/IceTerminal.java,v $
 * Description:  hypercom simpleTerminal
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.35 $
 */
import net.paymate.util.*;
import net.paymate.serial.*;
import net.paymate.data.*;
import net.paymate.terminalClient.ButtonTag;
import net.paymate.peripheral.*;
import net.paymate.awtx.print.*;
import net.paymate.jpos.data.Problem;
import net.paymate.io.IOX;
import net.paymate.lang.ThreadX;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;

public class IceTerminal implements QReceiver, PacketService, QActor, IceEasyKey {
  static ErrorLogStream dbg;
  QReceiver posterm;
  private PacketServer ps;
  private IcePacketizer out;
  private IcePacketizer receiver;
  boolean readyForAction=false;
  Port port;

  EasyCursor config;//save for debug
  boolean pollForInput=false;
  boolean waitForPacer=false;
//  boolean asciiStrokes=false;
  double samplingInterval; //for polling
  long ackNakDelay=Ticks.forSeconds(0.060);
  IceCommand extraStartupCommands[]; //

  String termRevision="1.33";//not sent at this revision
  String termBuild="iceterm8"; //not sent at this revision

  IcePrinter lp;
  IceSwipe swiper;
  IcePinPad pinpad;
  IceDisplayHardware display;
  DisplayPad dispad;

  boolean sigCapturing;
  boolean swipeCapturing;
  boolean pinCapturing;
  int displayContrast;


  public boolean sigCapturing() {
    return sigCapturing;
  }

  public boolean swipeCapturing() {
    return swipeCapturing;
  }

  public boolean pinCapturing() {
    return pinCapturing;
  }

  private void clearCaptureState(){
    sigCapturing=false;
    swipeCapturing=false;
    pinCapturing=false;
  }

  /**
   * @return false if object is not accepted. true if successfully received
   */
  public boolean Post(Object arf) {
    return posterm != null && posterm.Post(arf);
  }

  public PrinterModel LinePrinter() {
    if (lp == null) {
      lp = new IcePrinter(this);
      lp.configure(config);
    }
    return lp;
  }

  public CardSwipe CardSwipe() {
    if (swiper == null) {
      swiper = new IceSwipe(this);
    }
    return swiper;
  }

  public IcePinPad PinPad() {
    if (pinpad == null) {
      pinpad = new IcePinPad(this);
    }
    return pinpad;
  }

  public DisplayPad DisplayPad() {
    if (display == null) {
      display = new IceDisplayHardware(this);
    }
    if (dispad == null) {
      dispad = DisplayPad.Null();
      dispad.attachTo(display);
    }
    return dispad;
  }

  IceTablet tablet;
  public IceTablet getTablet() {
    if (tablet == null) {
      tablet = new IceTablet(this);
    }
    return tablet;
  }


  ///////////////////////
  //comm protocol management
  ///////////////
  // outgoing
  private QAgent cmdBuffer;
  private Waiter forRetry; //ack/nak transmission protocol, mates to OS comm driver performance.
  private Waiter pacing;   //timeout terminal application response to message.

  private static InstanceNamer agentNamer=new InstanceNamer(IceCommand.protocolId);

  private void startInput(){
    if (cmdBuffer == null) {
      cmdBuffer = QAgent.New(agentNamer.Next(), this);
    }
    if(pollForInput){
      cmdBuffer.setIdleObject(IceCommand.Poll());
      if (samplingInterval <= 0.1) { //10Hz is good enough for any one port
        samplingInterval = 2.0; //until parameter load works right
      }
      cmdBuffer.config(samplingInterval);
    }

    if (forRetry == null) {
      forRetry = Waiter.Create(Ticks.forSeconds(2), false, dbg);
    }
    if (pacing == null) {//create this even if it won't be used.
      pacing = Waiter.Create(Ticks.forSeconds(8), false, dbg);
    }
    cmdBuffer.Start(); //after all config is completed.
    ps.Start(); //don't start receiver until we can respond to input
    readyForAction=true;
  }
  /**
   * @synched only with commandFailed.
   */
  public boolean sendCommand(IceCommand cmd) {
    if (cmdBuffer == null  || ! readyForAction) {
      return false;
    }
    return cmdBuffer.Post(cmd);
  }

  public boolean sendEnableCommand(byte devicecode, boolean beon) {
    IceCommand cmd = IceCommand.Enabler(devicecode, beon);
    //resolve conflicts here:
    if (devicecode == IceCommand.Sigcap) {
      sigCapturing = beon; //referenced by keystroke handler
    }
    if (devicecode == IceCommand.Swiper) {
      swipeCapturing = beon;
    }
    if (devicecode == IceCommand.Pinpad) {
      pinCapturing = beon;
    }
    return sendCommand(cmd);
  }
  /**
   * @synched only with sendCommand
   */

  private void commandFailed(IceCommand cmd){  //command failed, notify PosTerminal:
    clearCaptureState();
    cmdBuffer.putUnique(IceCommand.Ahem());
    //really abysmal timing would allow one other command to get into queue between the Ahem above
    //and whatever happens in response to teh post below.
    if(cmd!=null){
      //if command is corrupt internally then detrroy it
      Post(Problem.Noted("IceTerminal Comm Failure:"+cmd.toSpam()));
      cmd.reset();
    } else {
      // post(Problem.Noted("IceTerminal Comm Failure:"+cmd.toSpam()));
      Post(Problem.Noted("IceTerminal Restarted:"+cmd.toSpam()));
    }
  }

  private boolean emitCommand(IceCommand cmd) {
    out.attachTo(cmd);
    dbg.VERBOSE("Sending:" + out.body().toSpam());
    int nakTries;
    for (nakTries = 3; nakTries-- > 0; ) {
      forRetry.prepare();
      Exception e = out.writeOn(port.xmt());
      if (e == null) {
        int arf = forRetry.run();
        dbg.VERBOSE("emitCommand:" + forRetry.toSpam());
        switch (arf) {
          case Waiter.Ready:
          case Waiter.Notified:
            return true; //command was accepted
          case Waiter.Timedout: // no anser
            return false; //command timedout
          case Waiter.Interrupted:
          case Waiter.Excepted:
            continue; //try again, maybe it will work
        }
      }
    }
    return false;//failed multiple retries
  }

  public void runone(Object fromq) {
    if (fromq instanceof IceCommand) {
      IceCommand runningCmd;
      runningCmd = (IceCommand) fromq;
      if( ! runningCmd.isValid()){
        return; //we null out bad commands so that they don't get resent forever.
      }
      pacing.prepare();
      if (emitCommand(runningCmd)) { //then command was sent without error
        if (waitForPacer) { //have to wait for pacing signal
          switch (pacing.run()) {
            case Waiter.Ready:
            case Waiter.Notified:
              dbg.VERBOSE("Message processed");
              ThreadX.sleepFor(ackNakDelay);//pause between good commands, give terminal a chance to breathe
                  /*+++add separate ack to command delay*/
              return; //command was processed
            case Waiter.Timedout:
            case Waiter.Interrupted:
            case Waiter.Excepted:
              dbg.ERROR("No response after message received");
              break; //response timedout
          }
        }
      } else {
        commandFailed(runningCmd);
      }
    }
  }

  private void ackornak(byte cc){
    //delay for the slow hypercom
    if(ackNakDelay!=0){//double using as flag to say that we ack/nak at all.
      IOX.Flush(port.xmt());
      ThreadX.sleepFor(ackNakDelay);
      port.lazyWrite(cc);
      if(out.addLF){
        port.lazyWrite(Ascii.LF);
      }
      IOX.Flush(port.xmt());
    }
  }
  /**we get here when the packet recognizer finds the character not allowed within its message
   * @return whether to restart/reset packet parser
   */
  public boolean onControlEvent(int controlevent) {//happens on reader thread
    if( ! readyForAction){
      return false;
    }
    boolean restart = false;//normally controls are independent of received packets
    try {
      switch (controlevent) {
        case Ascii.ENQ: {//terminal has power cycled or otherwise lost its mind.
          if (cmdBuffer.isStopped()) {
            dbg.WARNING("Starting transmission agent");
            cmdBuffer.Start();
          }
          if(forRetry.is(Waiter.Ready)){
             //@todo: if an Ahem command then don't fret, it is just an ENQ that was in teh pipe when we started to respond to the previous
             // ... as long as the delay between ENQ's s long enough we can ignore this condition
             forRetry.forceTimeout(); //we need to start over.
          } else {
            commandFailed(null);
          }
        }
        break; //return;
        case Ascii.ACK: {//terminal was happy with the command
          forRetry.Stop();//this is good. we can stop waiting
        }
        break; //return;
        case Ascii.NAK: {//this means that the terminal couldn't recognize what we sent it.
          dbg.WARNING("Nak on:" + out.body().toSpam());
          forRetry.forceException(); //this is bad, keep distinct from timeout
          //can't send retry data here, thread problems prevent simple sharing of sending code.
        }
        break; //return;
        case Receiver.TimedOut: {
          //driver provides these spuriously.
        }
        break;
        case Receiver.ByteError:{//maybe input overrun? super sized signature

        } break;
        default: {

          //post error event if not handled locally
          dbg.ERROR("Unexpected Control Event:" + Receiver.imageOf(controlevent));
          //common cause of the above is "port in use"
          //must restart recognizer ..
          ackornak(Ascii.NAK); //whether terminal resends or not is up to it.
          restart=true;//if we don't know what it is it may be an stx after a corrupt packet.
        }
      }
    }
    finally {
      return restart;
    }
  }

  public void Stop() {
    //empty body is ok.
  }

  public IceCommand setDebug(){
    IceCommand newone = IceCommand.Create();
    newone.append(IceCommand.System);
    boolean spew = LogSwitch.getFor(IceCommand.protocolId + ".spew",LogLevelEnum.OFF).is(LogSwitch.VERBOSE);
    newone.append(spew ? 'V' : 'Q');
    return newone;
  }

  public IceCommand setContrast() {
    IceCommand newone = IceCommand.Create();
    newone.append(IceCommand.System);
    newone.append('D'); //set display contrast
    newone.appendNumericFrame(displayContrast, 1);
    //'P' is for protocol, so far we stick with the powerup protocol.
    return newone;
  }

  private void sendConfig() {
    sendCommand(setDebug());
    sendCommand(setContrast());
    sendExtraInits();
    //@todo: PosTemrinal to refresh
  }

  private void iceInfo(AsciiBufferParser bp) {
    while (bp.remaining() > 0) {
      switch (bp.getChar()) {
        case IceCommand.Ahem: {
          dbg.VERBOSE("Terminal Claims to be: " + bp.getROF());//+++ message is missing FS in early revs @todo
          if (bp.remaining() <= 0) { //iceterm8 or earlier
            clearCaptureState();
            sendConfig();
            Post(Problem.Noted("Iceterminal Restarted"));
          }
        } break;
        case 'R': {
          termRevision = bp.getROF();
          termRevision=Formatter.ValueFromCvstag(termRevision);
          dbg.VERBOSE("Terminal's Revision: " + termRevision);
          sendConfig();//wait until we know what we are talking to...
        } break;
        case 'N': {
          termBuild = bp.getROF();
          termBuild=Formatter.ValueFromCvstag(termBuild);
          dbg.VERBOSE("Terminal's Build: " + termBuild);
        } break;
        case '!':{
          dbg.ERROR("Error Message From Terminal:" + bp.getTail());
        } break;
        default: {
          dbg.VERBOSE("Unknown iceInfo: " + bp.getTail());
        } break;
      }
    }
  }

  final static String formKeys="!=`^";//credit nothign debit cancel

  private static ButtonTag hackedButt(int hackdex){
    switch (hackdex) {
      case 0:
        hackdex = ButtonTag.CustomerAmountOk;    break;
      case 1:
        hackdex = ButtonTag.NullButton;  break;
      case 2:
        hackdex = ButtonTag.DoDebit;     break;
      case 3:
        hackdex= ButtonTag.CustomerCancels; break;
    }
    return new ButtonTag(hackdex);//remove bias added by IcePick.command()
  }
  //////////////////////
  // incoming
  public void onPacket(Buffer packet) {

    ackornak(Ascii.ACK);
    //parsing and posting goes here!
    if (packet != null && packet instanceof AsciiBuffer) {
      dbg.VERBOSE ("Received:"+packet.toSpam());
      AsciiBufferParser bp = AsciiBufferParser.Easy();
      bp.Start(packet);
      //expect T0."
      String protocol = bp.getPrefix();
      if (StringX.NonTrivial(protocol)) {
        if (protocol.equals("T0")) {
          do {
            byte packetType = (byte) bp.getByte();
            dbg.WARNING("Packet Type Code:" + (char) packetType);
            switch (packetType) {
              case IceCommand.Proceed: { //command pacing message
                pacing.Stop();
                dbg.rawMessage(bp.getByte() - '0', bp.getTail());
              }
              break;
              case IceCommand.FormInput: {
                ButtonTag bt = new ButtonTag(bp.getByte() - 'A'); //remove bias added by IcePick.command()
                Post(bt);
              }
              break;
              case IceCommand.Pinpad: {
                if (pinpad != null) {
                  pinpad.process(bp);
                }
              }
              break;
              case IceCommand.System: { //terminal identifier stuff
                iceInfo(bp);
              }
              break;
              case IceCommand.KeyInput: //keystroke
                int hackedform = (int) bp.getDecimalFrame();
                int hackdex = formKeys.indexOf(hackedform); //squeezed one form into wierd keystrokes.
                if (hackdex >= 0) {
                  Post(hackedButt(hackdex));
                }
                else {
                  DisplayPad().Post(new Integer(hackedform));
                }
                break;
              case IceCommand.StringInput: //string acquired within terminal
                DisplayPad().Post(bp.getROF());
                break;
              case IceCommand.Swiper: { //card swipe, some seem to have wierd chars in them
                CardSwipe(); //asserts its existence
                //if display is doing a sale then enter amount
                DisplayPad().autoEnterIf(net.paymate.terminalClient.ClerkItem.SalePrice);
                //if waiting for invoice number then enter invoice number.
                DisplayPad().autoEnterIf(net.paymate.terminalClient.ClerkItem.MerchRef);
                //any other questions that precede getting the card type should be added here
                Post(swiper.swipeFrom(bp));
              }
              break;
              case IceCommand.Sigcap: { //signature
                getTablet().process(bp);
              }
              break;
              case IceCommand.Logger: { //log
                int msglevel = bp.getByte() - '0'; //terminal better send 0..3!
                dbg.rawMessage(msglevel, bp.getTail());
              }
              break;
              default:
                dbg.ERROR("unknown message content");
                break;
            }
   //when we implement this we can shrink the present timeout value         pacing.stretch();
          }
          while (bp.getByte() == Ascii.RS);
        }
        else {
          dbg.ERROR("unknown protocol:" + protocol);
        }
      }
      else {
        dbg.ERROR("no protocol prefix");
      }
      dbg.VERBOSE("tail is:" + bp.getTail());
    }
    else {
      Post(new Exception("null packet in IceTerminal")); //purely for debug purposes
    }
  }

  ///////////////////////
  // packetService

  /**
   * called when line is established, usefull for init of internal logic
   * @return same as onPacket()
   * called by PacketServer Creation, that probably is an evil thing to do
   */
  public void onConnect() {
    //send 'ahem', expect abilities in return
    //too soon, not fully configured sendCommand(IceCommand.Ahem());
  }

  public void sendExtraInits(){
    for (int i = 0; i < extraStartupCommands.length; i++) {//# preserve file order
      sendCommand(extraStartupCommands[i]);
    }
  }

  //////////////
  public IceTerminal(Port port) {
    this.port = port;
     if(dbg==null){
      dbg = ErrorLogStream.getForClass(this.getClass());
    }
    out = new IcePacketizer();
    receiver=(IcePacketizer) IcePacketizer.forReception();
    ps = PacketServer.Create(this, PacketReceiver.MakeFrom(receiver), port);
  }


  private void makeStartupList(TextList rawcommands) {
    try {
      extraStartupCommands = new IceCommand[rawcommands.size()];
      for (int i = extraStartupCommands.length; i-- > 0; ) {
        extraStartupCommands[i] = IceCommand.FromString(rawcommands.itemAt(i));
      }
    }
    catch (Exception ex) {
      extraStartupCommands = new IceCommand[0];
    }
  }

  public IceTerminal config(EasyCursor cfg){//called from parent class load(), this class is not itself 'easy'
    config=cfg.EasyExtract(null); //get copy of current node.
    displayContrast=config.getInt(IceEasyKey.displayContrast,3);
    samplingInterval=config.getNumber(IceEasyKey.samplingInterval,0.200);

    makeStartupList(config.getTextList(IceEasyKey.extraStartupCommands));

    ackNakDelay=config.getLong(IceEasyKey.ackDelayMillis,0);//defaulting to values used during initial test
    receiver.hasLrcNullBug=false; out.addLF=true;

    startInput();
    sendCommand(IceCommand.Ahem());//can't start until we are configured! (was way too confusing)
    return this;
  }

  public void attachTo(QReceiver posterm) {
    this.posterm = posterm;
//wait until we are ready to send commands    ps.Start(); //don't start until we have a place to hand off incoming stuff to
  }

}
//$Id: IceTerminal.java,v 1.35 2005/03/13 23:52:35 andyh Exp $
