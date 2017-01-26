package net.paymate.ivicm.nc50;
/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/nc50/NC50.java,v $
 * Description:  NC50 pinpad service routine
 * every time something is displayed follow up response with request for keystroke.
 * every keystroke received respond with request for keystroke
 *
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.24 $
 */
import net.paymate.ivicm.*;
import net.paymate.serial.*;
import net.paymate.lang.ThreadX;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.data.*;
import net.paymate.lang.StringX;
import net.paymate.*;
import net.paymate.lang.Fstring;
import net.paymate.lang.ContentType;
import net.paymate.awtx.*;

public class NC50 extends DisplayDevice implements DisplayHardware, QActor {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(NC50.class);
  static final ErrorLogStream dbk=ErrorLogStream.getExtension(NC50.class,"comm");
  static final ErrorLogStream dbtimes=ErrorLogStream.getExtension(NC50.class,"rspt");

  static final String VersionInfo= "NC50 I/F, (C) PayMate.net 2002 $Revision: 1.24 $";
  static String shortInfo(){
    return "Paymate release $Name:  $";
  }
  /**
   * convenience for test programs
   */
  protected String restOfLine(TextListIterator args){
    return args.hasMoreElements()? args.tail():shortInfo();
  }
  public boolean hasTwoLines(){//has *at least* two lines
    return false;
  }
  /**
   * theoretically we won't call this when hasTwoLines is false, but just in case ...
   */
  public void Echo(String forDisplay){//for keystroke echoing on second line
    Display(forDisplay);
  }

/**
 * configuration
 */
  static final int DisplayWidth=16;
  static final int commandtout=(int)Ticks.forSeconds(5);
  static final int interchartout=250*10; //10 times an already generous timeout.
  boolean configmode;
////////////////////
// internal state
  private Waiter packetWait;
  private Command currentCommand;
  int incomingFailurecounter; // on 3rd send EOT and report error.
  int outNaks;
  private AckNakReceiver incoming;//only extnesion is ENQ powerup signal
  private QAgent CommandProcessor;
  private boolean amInputting=true; //gets us a power up reset.

  int sendcounter=0;

  /**
   * @return true for trouble, false for data arrived
   */
  private boolean waitFor(Object arf, int tout){
//    System.err.println("wait at most "+tout+" millis for:"+arf);
    dbk.WARNING("about to wait");
//this successfully tested notify-before-wait    ThreadX.sleepFor(tout+100);//to get notify before wait
    packetWait.Start(tout);
    dbk.WARNING("response "+packetWait.toSpam());
    suckInput(dbk); //pick up any post-notification bytes, in case of false timeout.
    return ! currentCommand.acked;
  }
/**
 * wait for acknowledge on command,
 * shared by EOT.
 */
  private boolean waitForAck(long tout){
    return waitFor(currentCommand,(int)tout);
  }


  /**
   * called when outgoing command not acknowledged
   */
  private void onComFailure(String onWhat,String detail){
    dbg.ERROR(detail+" on command:"+onWhat);
  }

/**
 * core of encrypt management thread's run.
 */
  public void runone(Object fromq){
    outNaks=0;

    if(fromq instanceof Command){
      dbk.WARNING("sending queued command");
      if(sendCommand((Command)fromq)){
        dbk.WARNING("waiting on ack");
        waitForAck(commandtout);
        if(currentCommand.acked){//waiting for ack
          dbtimes.VERBOSE("ACK"+Ascii.bracket(sendcounter)+" response:"+packetWait.elapsedTime());
          dbk.WARNING("got ACK");
        } else {
          dbk.WARNING("command timedout:"+currentCommand.forSpam());
          onComFailure(currentCommand.forSpam(),"[---]");
        }
      } else {//to send , musta been a defective queue entry, do what we need to do to process next
        dbk.WARNING("command not sent");
        onComFailure(currentCommand.forSpam(),"[XXX]");
      }
    }
    //else unknown trash in queue
  }

  public void Stop(){
    //never invoked.
  }

/**
 * never queue a command from within driver loop! use sendCommand()
 */
  /*package*/ void QueueCommand(Command complete){
    if(CommandProcessor.putUnique(complete)){
      dbg.WARNING("Command replaced one already in queue:"+complete.forSpam());
    }
    //and driver picks it up when finished with last operation.
  }

  /** synch'd indirectly to incoming. only call from onbyte()
   * call when it is clear to send.
   */
  private void Proceed(boolean happy){
    try {
      if(currentCommand!=null){
        dbg.WARNING("about to proceed");
        currentCommand.acked=happy;
//        incoming.restart();//on ack nak enq etal.
        packetWait.Stop();
      } else {
        dbg.WARNING("Proceed without command");
      }
    }
    finally {
      incoming.restart();//nc50 spamcrash
    }
  }

  private boolean writeCC(int acknaker){
    try {
      myPort.os.write(acknaker);
      return true;
    }
    catch (Exception ex) {
      return false;
    }
  }

  /**
   * threads: serial, serial timeout
   */
  private void giveup(){
    incomingFailurecounter=0;
    Proceed(false);//timeout and friends
  }

  /**
   * threads: serial port, alarmer (invoked by serial port)
   */
  private void onTimeout(){
    //tell whoever is waiting that there was an error
    dbg.WARNING("Timeout!");
    giveup();//in on timeout
  }

  private int onError(){  //here is where triple NAK EOT gets done.
    if(++incomingFailurecounter>3){
      dbk.VERBOSE("sending EOT");
      writeCC(Ascii.EOT);
      giveup();//response garbled
      return Receiver.TimeoutNever;
    } else {
      dbk.VERBOSE("sending NAK");
      writeCC(Ascii.NAK);
      return commandtout;
    }
  }

  /*
  the only commands we use are:
  0B config function keys 0B3 is only variantused
  13 set baud rate, 134  is 9600, only issued by special jar invocation.
  Z2 display text     on response do a Z42
  Z42 get keystroke "Z420"
  Z43 received keystroke (response to Z42)
  */
  final static String revision="Paymate.Net";

  /**
   * @return true when command is sent flawlessly.
   * owned by QAgent
   */
  private boolean sendCommand(Command outgoing){
    synchronized (incoming) {
      dbg.VERBOSE("sendCommand:"+outgoing.toSpam());
      currentCommand=outgoing; //save in case of NAK
      //must clear ack before sending bytes!
      currentCommand.acked=false;//allows reuse of commands.(versus just clearing on construction)
      ++sendcounter;
      packetWait.prepare();
      incoming.restart();
      Exception oops=currentCommand.packer().writeOn(myPort.os);
      if(oops!=null){
        dbg.Caught("failed to send because:",oops);
        return false;
      } else {
        return true;
      }
    }
  }

  /**
   * change state and deliver input data
   */
  private void postInput(Object theinput){
    amInputting=false;//cuase input is completed
    incomingFailurecounter=0;
    if(keypadif!=null){
      keypadif.Post(theinput);
    }
  }

  /**
   * acknowledge input data and process it.
   */
  private void ackInput(String whatkind,Object theinput){
    dbk.VERBOSE("got "+whatkind);
    writeCC(Ascii.ACK);
    postInput(theinput);//should result in a display request
  }

// person who request input must time it out. Can't afford to spontaneously send cancels to the app.
//  private void postCancel(){
//    postInput(new Object());
//  }

  private void postTickle(){
    postInput(new Integer(DisplayPad.IGNORE));//tickle the displaypad
  }
///////////////////////////
  private boolean simulate(int responsechar){
    if(simulating){
      writeCC(responsechar);
    }
    return simulating;
  }

  private boolean simulac(){
    if(simulating){
      writeCC(Ascii.ACK);
    }
    return simulating;
  }

/**
 * @param rawkey is between A and N
 */
 //char from NC50 ABCDEFGHIJKLMNOP
  String decoder="963F8520741EABCD";
  protected Integer defunked(int rawkey,boolean wierd){
    return new Integer(wierd? StringX.charAt(decoder,rawkey-'A','?'):rawkey);
  }

  private void ackStroke(int keystroke,boolean wierd){
    dbg.VERBOSE("kw:"+Ascii.bracket(keystroke)+wierd);
    if(keystroke>0){
      ackInput("known keystroke", defunked(keystroke,wierd));
    } else {
      writeCC(Ascii.NAK);
      postTickle();
    }
  }


  /** !! belongs to runone()
   * this is called when either the given Buffer is a command that has been
   * acknowledged, or when it is data from device. The designers of the encrypt
   * tagged all return data, don't need to track request to know what reply is.
   * @return amount of time to wait for a response.
   * synch'd as timeout may call it as well as serial port thread
   */
  private void processBuffer(Buffer cmd){
    try {
      dbk.VERBOSE("Processing:"+cmd.toSpam());
      BufferParser bp=BufferParser.Slack();
      bp.Start(cmd);
      String prefix=bp.getUntil('.');
      switch(StringX.parseInt(prefix)){
        case Command.HereisKey:{//happens for function keys
          dbk.WARNING("Function key while "+currentCommand.forSpam());
          int keystroke=bp.getByte();
          int inputmode=bp.getByte();
          ackStroke(keystroke,false);//reset gets 11.A8
        } break;
        case Command.HereisString:{
          dbk.WARNING("Answer to "+currentCommand.forSpam());
          if(currentCommand.isa(Command.GetKey)){
            ackStroke(bp.getByte(),true);//return is A..N encoded keyboard code
          } else {
            ackInput("string response",bp.getTail());
          }
        } break;
        default:{
          dbk.WARNING("Unknown response:"+prefix+'.'+bp.getTail());
        } break;
      }
    }
    finally {
      incoming.restart();//nc50 spamcrash
    }
  }

  private void onAck(){
    if(currentCommand!=null){
      amInputting=currentCommand.isInputter();//input may have started, or non input command completed.
      Proceed(true);//normal command complete signaled.
    } else {
      incoming.restart();//nc50 spamcrash
    }
  }
  /**
   * ensure that we are ready to receieve
   */
  private void ensureDTR(){//this is probably a misnomer.
    if(currentCommand==null){
      currentCommand=Command.Command(Command.DisableInput,0);
    }
    if(incoming.body()==null){//prepare to accept worst case response
      incoming.startBuffer(Buffer.New(Command.MaxReceivedBytes()));
    }
  }

  /**
 * synch'd on incoming via being called only from onbyte().
 */
  private int onComplete(){
    dbk.WARNING("recpt complete");
    if(incoming.isAckNak()){
      switch(incoming.body().bight(0)){
        case Ascii.ACK:{
          dbk.WARNING("ACK on "+currentCommand.forSpam());
          onAck();
        } break;
        case Ascii.ENQ:{
          dbk.WARNING("ENQ'd");
          writeCC(Ascii.ACK);
          //and then wait 128 ms per encrypt specification, before sending anything
          ThreadX.sleepFor(128);
          Proceed(false);//encrypt spontaneously restarted
        } break;
        case Ascii.NAK:{//device didn't like our message, resend last packet.
          dbk.WARNING("got NAK'd, resending. took:"+packetWait.elapsedTime());
          if(++outNaks<3){
            sendCommand(currentCommand);
            //this is the only case where we do not 'proceed' to next command.
            return commandtout;   //incoming state stays where it is.
          } else {
            dbk.WARNING("Command NAK'ED:"+currentCommand.toSpam());
            Proceed(false);
          }
        }
      }
    }
    else {
      processBuffer(incoming.body());
    }
    return Receiver.TimeoutNever;
  }

  public int onByte(int commchar){
    synchronized (incoming) {//synched with sendCommand, need to know why +_+
      String charspam=incoming.toSpam(commchar);
      dbk.VERBOSE(charspam);
      ensureDTR();
      if(commchar<=Receiver.TimedOut){//lump other serious errors in with timeout
        dbk.WARNING("depressing incoming event:"+charspam);
        onTimeout();//#has a proceed
      } else {
        if(incoming.receive(commchar)){//if input is accepted
          dbk.VERBOSE("char accepted");
          if(incoming.receptionComplete()){
            return onComplete();
          } else {
            return interchartout;
          }
        } else {//packet error
          dbk.WARNING("packet error on:"+charspam);
          incoming.restart();//packet error
          return onError();//will proceed when hopeless to retry.
        }
      }
    }
    return Receiver.TimeoutNever;
  }

  public void Display(String forDisplay){//access for tester.
    if(amInputting){
      QueueCommand(Command.cancelSession());
    }
    QueueCommand(Command.String(Command.GetKey,forDisplay));
  }

//+++ this seems to not be working!
  public boolean doesStringInput(ContentType ct){
    return ! (ct.is(ContentType.select) || ct.is(ContentType.unknown));
  }

  public void getString(String prompt,String preload,ContentType ct){
    if(amInputting){
      QueueCommand(Command.cancelSession());
    }
    QueueCommand(Command.GetAnswer(prompt,preload,ct));
  }

  protected void onConnect() { //send volatile configuration items
    int flushed=myPort.reallyFlushInput(30);//+_+WAG maximum buffered data. see 'get version' command
    dbg.WARNING("Flushed "+flushed+" bytes");
    CommandProcessor.Start();// starts it.
    QueueCommand(Command.cancelSession());
    Display("$Revision: 1.24 $");
  }

    ////////////////////
  //
  public void disConnect() {
    //    stopReception();
    CommandProcessor.Stop();
    //+++ wait until packet completed then...
    super.disConnect();
  }

  public NC50(String id) {
    super(id);
    incoming=AckNakReceiver.Create(Command.forReception());
    //since we don't implement comparble we don't need a comparator below:
    CommandProcessor= QAgent.New(id,this,null);
    //we are now trusting that Qagents don't miss anything.
    CommandProcessor.config(dbg).config(Ticks.forSeconds(1000));
    packetWait=Waiter.Create(0,false,dbk);
  }

  //////////////////
  void speedtest1(){
    Display("speedtest1");
    //have to use direct put() function else the commands
    //purge previous from queue.
    for(char ascii=126;ascii-->32;){//printable ascii
      Display(Fstring.fill("",5,ascii));
    }
    ThreadX.sleepFor(94*250);//so as to not be clipped by next test
  }

  /**
   *
   */
  static public void main(String[] args) {
    NC50 totest=new NC50("NC50.tester");
    totest.configmode=false;//IYF
    totest.testConnect2(4800,dbg);
    dbk.setLevel(LogSwitch.VERBOSE);
    dbg.setLevel(LogSwitch.VERBOSE);
    dbtimes.setLevel(LogSwitch.VERBOSE);
//    totest.speedtest1();
//    testapad padtester=new testapad(nc50DisplayPad.makePad(totest));
    totest.testloop(dbg);
  }

}
//$Id: NC50.java,v 1.24 2003/07/27 05:35:05 mattm Exp $