package net.paymate.ivicm.pinpad;
/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/encrypt100.java,v $
 * Description:  encrypt100 pinpad service routine
 * every time something is displayed follow up response with request for keystroke.
 * every keystroke received respond with request for keystroke
 *
 * Copyright:    Copyright (c) 2001-2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.31 $
 * @todo: implement string input for types that we can tolerate.
 */
import net.paymate.ivicm.*;
import net.paymate.serial.*;
import net.paymate.lang.ThreadX;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.data.*;
import net.paymate.awtx.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ContentType;
import net.paymate.lang.Fstring;

class commstate {
  boolean linebusy;
  boolean waitingonEOT;
  boolean waitingonresponse;
  boolean abortInput;

  boolean amInputting(){
    return waitingonresponse;
  }
  boolean expectingEOT(){
    return waitingonEOT;
  }
  boolean lineBusy(){//smae as waiting on ack
    return linebusy;
  }
  boolean amWaiting(){
    return lineBusy()|| amInputting() || expectingEOT() ;
  }
  void setIdle(){
    linebusy=waitingonEOT=waitingonresponse=false;
  }
  commstate(){
    setIdle();
  }
}//local class.

public class encrypt100 extends DisplayDevice implements DisplayHardware, QActor {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(encrypt100.class);
  static final ErrorLogStream dbk=ErrorLogStream.getExtension(encrypt100.class,"comm");
  static final ErrorLogStream dbtimes=ErrorLogStream.getExtension(encrypt100.class, "rspt");

  static final String VersionInfo= "encrypt100 I/F, (C) PayMate.net 2002 $Revision: 1.31 $";
  static String shortInfo(){
    return "Paymate rev "+net.paymate.Revision.stripped(VersionInfo);
  }
  /**
   * convenience for test programs
   */
  String restOfLine(TextListIterator args){
    return args.hasMoreElements()? args.tail():shortInfo();
  }
  public boolean hasTwoLines(){ //has *at least* two lines
    return false;
  }
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
  boolean simulating;
  QReceiver keypadif; //on keystroke received pass it to this guy.

  public void setKeyListener(QReceiver keypadif){
    this.keypadif=keypadif;
  }
////////////////////
// internal state
  commstate I=new commstate();

  private Command currentCommand;
  int incomingFailurecounter; // on 3rd send EOT and report error.
  private EncryptReceiver incoming;
  private QAgent CommandProcessor;

  StopWatch responsetime=new StopWatch(false);
  int sendcounter=0;

  private boolean CancellingSession(){
    return currentCommand!=null && currentCommand.isa(Command.CancelSession);
  }

  /**
   * @return true for trouble, false for data arrived
   */
  private boolean waitFor(Object arf, int tout){
    dbk.WARNING("about to wait");
    ThreadX.waitOn(arf,tout);//#Waiter later
    suckInput(dbk); //don't trust return, kinda hacked at the moment--belongs to ET1K
    return ! incoming.isWellFormed();//ack'ed eot'd or packet received, can't tell here!
      //but the above return value should make runone inspect the incoming.
  }
/**
 * wait for acknowledge on command,
 * shared by EOT.
 */
  private boolean waitForAck(long tout){
    return waitFor(currentCommand,(int)tout);
  }
/**
 *
 */
  private boolean waitForIncoming(long tout){
    I.waitingonresponse=true;
    try {
      return waitFor(incoming,(int)tout);
    }
    finally {
      I.waitingonresponse=false;
    }
  }

  private void killInputWait(){
    I.abortInput=true; //kill any input command in progress
//    if(I.amInputting()){//only if we are in the final wait do we want to issue this.
      ThreadX.notify(incoming);//break up the wait, all subsequent logic must be run from runone().
//    }
  }

  private void WhackDevice(String why, boolean reallyHard){
    if(reallyHard){
      CommandProcessor.Start(); // start command processor!
    }
//bullshit, gets no response    QueueCommand(Command.powerupReset());
    QueueCommand(Command.cancelSession());
    QueueCommand(Command.powerupClear());
    Show(why);
  }

  /**
   * called when outgoing command not acknowledged
   */
  private void onComFailure(String onWhat,String detail){
    WhackDevice("! "+onWhat+detail,true /*reallyHard*/);
  }
  /**
   * some commands get acked but then we have to wait for an EOT
   * before sending another command.
   * We do NOT break out of this kind of session... yet.
   */
  private void waitForEot(){
    I.waitingonEOT=true;
    dbk.WARNING("wait on EOT");
    if(waitForAck(Ticks.forSeconds(15))){//waiting on EOT
      dbk.ERROR("session timedout");
    }
    I.waitingonEOT=false;
  }

/**
 * core of encrypt management thread's run.
 */
  public void runone(Object fromq){
    try {
      boolean failed=true;
      I.setIdle();
      if(fromq instanceof Command){
        dbk.WARNING("sending queued command");
        failed = sendCommand((Command)fromq);
      }
      if(failed){//to send , musta been a defective queue entry, do what we need to do to process next
        dbk.ERROR("command not sent");
        onComFailure(currentCommand.forSpam(),"[XXX]");
        return;
      }
      I.linebusy=true;
      dbk.WARNING("waiting on ack");
      failed=waitForAck(commandtout);
      responsetime.Stop();
      I.linebusy=false;

      if(failed){//waiting for ack
        dbk.ERROR("command timedout:"+currentCommand.forSpam());
        if(!CancellingSession()){
          onComFailure(currentCommand.forSpam(),"[---]");
        }
        return;
      }

      if(incoming.isAcked()){
        dbtimes.VERBOSE("ACK time:"+sendcounter+"\t"+responsetime.millis());
        dbk.WARNING("got ACK");
        processBuffer(currentCommand.body());//normal handling of response to a command
        if(currentCommand.getsResponse && ! I.abortInput){
          dbk.WARNING("wait on input");
          if(waitForIncoming(Ticks.forMinutes(3))){//timedout
            dbk.ERROR("input timedout");
            postTickle();
          } else {      //gotInput
            if(incoming.isWellFormed()){
              processBuffer(incoming.body());
            } else {
              dbk.ERROR("input timedout or abandoned");//doesn't matter which, we are
              postTickle();//garbled response is still a response.
            }
          }
        }
        if(currentCommand.wait4EOT){//then must wait for an EOT before sending anything
          waitForEot();
        }
      } else {//not acked, but not timedout
        dbk.ERROR(Ascii.bracket(incoming.ackornak)+" on response to:"+currentCommand.forSpam());
        if(! CancellingSession()){
          onComFailure(currentCommand.forSpam(),Ascii.bracket(incoming.ackornak));//didn't get aacked
        }
        return;
      }
    }
    finally {
      I.abortInput=false;
    }
  }

  public void Stop(){
    //never invoked.
  }

/**
 * used for reference when purging queue, these instances are never actually sent.
 */
  Command cfgetkey=Command.mkEnableKey();
  Command cfgetstring=Command.mkEnableString();

/**
 * never queue a command from within driver loop! use sendCommand()
 */
  /*package*/ void QueueCommand(Command complete){
    killInputWait();
    //removes comflicting commands and always inserts new one:
    if(CommandProcessor.putUnique(complete)){
      dbg.WARNING("Command replaced one already in queue:"+Ascii.bracket(complete.body().packet()));
    }
    //and driver picks it up when it is ready to send.
  }

  /**
   * call when it is clear to send. @return's whether it really is clear to send.
   */
  private void Proceed(){
    if(currentCommand!=null){
      dbg.WARNING("about to proceed");
      ThreadX.notify(currentCommand);
    } else {
      dbg.ERROR("Proceed without command");
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
    I.abortInput=true;//ok to set even if we aren't inputting
    Proceed();//try to survive
  }

  /**
   * threads: serial port, alarmer (invoked by serial port)
   */
  private void onTimeout(){
    //tell whoever is waiting that there was an error
    dbg.ERROR("Timeout!");
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
   * @return true whether command FAILS.
   * owned by QAgent
   */
  private boolean sendCommand(Command outgoing){
    dbg.VERBOSE("sendCommand:"+outgoing.body().toSpam());
    currentCommand=outgoing; //save in case of NAK
    ++sendcounter;
    responsetime.Start();
    Exception oops=currentCommand.packer().writeOn(myPort.os);
    if(oops!=null){
      dbg.Caught(oops);
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return command timeout when command successfully sent as we are timing out
   * the ACK, not the ultimate response.
   */

  Buffer keyInput;
  private void enableKeystroke(){
    if(keyInput==null){
      keyInput=Buffer.New("Z43*".length());
    }
    incoming.startBuffer(keyInput);
  }

  Buffer stringInput;
  private void enableStringinput(){
    if(stringInput==null){
      stringInput=Buffer.New(Command.MaxLength);
    }
    incoming.startBuffer(stringInput);
  }

  /**
   * called wehn device notifies us that it is waiting for input.
   */
  private void waitingForInput(String whywaiting){
    dbk.VERBOSE("waiting on "+whywaiting);
  }

  /**
   * change state and deliver input data
   */
  private void postInput(Object theinput){
    incomingFailurecounter=0;
    I.waitingonresponse=false;
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
  private void postCancel(){
    postInput(new Object());
  }

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

  /** !! belongs to runone()
   * this is called when either the given Buffer is a command that has been
   * acknowledged, or when it is data from device. The designers of the encrypt
   * tagged all return data, don't need to track request to know what reply is.
   * @return amount of time to wait for a response.
   * synch'd as timeout may call it as well as serial port thread
   */
  private void processBuffer(Buffer cmd){
    dbk.VERBOSE("Processing:"+cmd.toSpam());
    try {
      BufferParser bp=BufferParser.Slack();
      bp.Start(cmd);
      if(bp.remaining()<=0){ //handles EOT cancelling input.
//not our job        cancelInput();
        return;
      }
      switch (bp.getByte()) {
        case 'Z':{
          switch(bp.getByte()){
            case '2'://sent display data, enable keyboard input
            case '3':{//ditto, for multi-line display
              dbk.VERBOSE("display done");
              simulac();
            } break;
            case '4':{//getKey interaction
              switch(bp.getByte()){
                case '2':{ //asked for input, got ACK'd
                  if(!simulac()){
                    enableKeystroke();
                    waitingForInput("keystroke");//might start over
                  }
                } break;
                case '3':{ //input is here, ack device
                  ackInput("keystroke",new Integer(bp.getByte()));
                }
              }
            } break;
            case '5':{//getString interaction
              switch(bp.getByte()){
                case '0':{ //asked for input, we just got an ACK on command
                  if(!simulac()){
                    enableStringinput();
                    waitingForInput("string");
                  }
                } break;
                case '1':{ //input is here, ack device
                  ackInput("string",bp.getTail());
                } break;
              }
            } break;
          }//switch
        } break;
        case '7': {//cancel session issued when irrelevent
          dbk.VERBOSE("ACK'ed 72");
          simulac();
        } break;
        case '0':{//factory config or diag
          //only one used so far is set keyboard rules:
          if(!simulate(Ascii.NAK)){
            dbk.VERBOSE("keyboard config or brag");
          }
        } break;
        case '1':{//factory config or diag
          //only one used so far is set baud rate:
          if(!simulate(Ascii.NAK)){
            dbk.VERBOSE("baud is set, shutting down to restart");
          }
          //+_+ shutdown system and restart at new baud rate!
        } break;
        default:{ //unknown command/response
          dbk.ERROR("Bad response!");
//user will figure it out without us          cancelInput();//punch user in the nose.
        }
      }
    }
    finally {
      if(incoming!=null){
        incoming.restart(); //else following control characters are 'escaped'
      }
    }
  }

  /**
   * ensure that we are ready to receieve
   */
  private void ensureDTR(){//this is probably a misnomer.
    if(currentCommand==null){
      currentCommand=Command.mkEnableString();//present worst case command and
      if(incoming.body()==null){//prepare to accept worst case response
        incoming.startBuffer(Buffer.New(Command.MaxLength));
      }
    }
  }

  public int onByte(int commchar){
    String charspam=incoming.toSpam(commchar);
    dbk.VERBOSE(charspam);
    if(commchar<=Receiver.TimedOut){//lump other serious errors in with timeout
      dbk.ERROR("outrageously bad incoming event ");
      onTimeout();//#has a proceed
    } else {
      ensureDTR();//makes all references below non-null
      //the following might now be relocatable into EncryptReceiver.
      if(!incoming.escaping()){
        dbk.VERBOSE("not escaped");
        switch(commchar){
          case Ascii.EOT:{
            if(currentCommand.wait4EOT){//EOT == ready for next command
              dbk.VERBOSE("EOT, ready for next command");
              incoming.setAck(Ascii.EOT);
              Proceed();//EOT, which was expected
            } else {//unexpected EOT
              dbk.VERBOSE("EOT on "+currentCommand.forSpam());
              if(CancellingSession()){
                Proceed();//ACK:runone advances to next state
              } else {
                if(currentCommand.getsResponse){
                  dbk.WARNING("send CANCEL");
                  postCancel(); //presume user is banging on cancel key.
                }
                giveup();//device tells us to give up sending to it.
              }
            }
          } return Receiver.TimeoutNever;
          case Ascii.ACK:{
            dbk.VERBOSE("ACK on "+currentCommand.forSpam());
            incoming.setAck(Ascii.ACK);
            Proceed();//ACK:runone advances to next state
          } return Receiver.TimeoutNever;
          case Ascii.NAK:{//device didn't like our message, resend last packet.
            dbk.ERROR("got NAK'd, resending");
            sendCommand(currentCommand);
            return commandtout;
            //incoming state stays where it is.
          }
          //if not special char join with is escaped char.
        }
      }
      dbk.VERBOSE("buffering: "+Ascii.image(commchar));
      if(incoming.receive(commchar)){//if input is accepted
        dbk.VERBOSE("char accepted");
        if(incoming.receptionComplete()){
          dbk.WARNING("recpt complete");
          ThreadX.notify(incoming);
        } else {
          return interchartout;
        }
      } else {//packet error
        dbk.ERROR("packet error");
        incoming.restart();
        return onError();//will proceed when hopeless to retry.
      }
    }
    return Receiver.TimeoutNever;
  }

/**
 * will use to reduce interruption of input on stuttered display.
 * I.e. if waiting for input when asked to displaying what is already on display
 * don't cancel input, just keep on waiting.
 */
  String lastshown=null;
  public void Show(String forDisplay){
    lastshown= StringX.tail(forDisplay,DisplayWidth);
    dbk.VERBOSE("Show:"+forDisplay+" ["+lastshown+"]");
    //almost ALWAYS need to issue this:
    QueueCommand(Command.cancelSession());
    QueueCommand(Command.mkDisplay(lastshown));
  }

  public void Display(String forDisplay){//access for tester.
    Show(forDisplay);
    QueueCommand(Command.mkEnableKey());
  }

  public boolean doesStringInput(ContentType ct){
    return false; //+_+ there are some types it does do!
  }

  public void getString(String prompt,String preload,ContentType ct){//+_+ need to research manual again.
    Show(prompt);
    QueueCommand(Command.mkEnableString());
  }

  protected void onConnect() { //send volatile configuration items
    int flushed=myPort.reallyFlushInput(30);//+_+WAG maximum buffered data. see 'get version' command
    dbg.WARNING("Flushed "+flushed+" bytes");
    WhackDevice(shortInfo(),true);//(re) starts driver
  }

    ////////////////////
  //
  public void disConnect() {
    //    stopReception();
    CommandProcessor.Stop();
    //+++ wait until packet completed then...
    super.disConnect();
  }

  public encrypt100(String id) {
    super(id);
    incoming=EncryptReceiver.Create(Packetizer.Ascii(-1));
    //since we don't implement comparble we don't need a comparator below:
    CommandProcessor= QAgent.New(id,this,null);
    //poll once a second in case a command fell through the qagent hole.
    CommandProcessor.config(dbg).config(Ticks.forSeconds(1));
  }

  //////////////////
  void speedtest1(){
    QueueCommand(Command.mkDisplay("speedtest1"));
    //have to use direct put() function else the commands
    //purge previous from queue.
    for(char ascii=126;ascii-->32;){//printable ascii
      CommandProcessor.Post(Command.mkDisplay(Fstring.fill("",5,ascii)));
    }
    ThreadX.sleepFor(94*250);//so as to not be clipped by next test
  }

  void speedtest2(){
    QueueCommand(Command.mkDisplay("speedtest2"));
    //have to use direct post() function else the commands
    //purge previous ones from queue.
    for(char ascii=126;ascii-->32;){//printable ascii
      CommandProcessor.Post(Command.mkDisplayAppend(ascii));
    }
    ThreadX.sleepFor(94*250);//so as to not be clipped by next test
    //done. read log file for results.
  }

  /**
   *
   */
  static public void main(String[] args) {
    encrypt100 totest=new encrypt100("encrypt100.tester");
    totest.configmode=false;//IYF
    totest.testerConnect(args,9600,dbg);
//    dbk.setLevel(LogSwitch.ERROR);
//    dbg.setLevel(LogSwitch.ERROR);
//    totest.speedtest1();
//    totest.speedtest2();
    testapad padtester=new testapad(EC100DisplayPad.makePad(totest));
    totest.testloop(dbg);
  }

}
//$Id: encrypt100.java,v 1.31 2003/07/27 05:35:06 mattm Exp $