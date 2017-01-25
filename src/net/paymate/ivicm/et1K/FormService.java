/* $Id: FormService.java,v 1.33 2001/11/15 03:15:44 andyh Exp $ */
package net.paymate.ivicm.et1K;

//import net.paymate.ivicm.et1K.ICForm;
import net.paymate.util.*;
import net.paymate.jpos.common.*;
import net.paymate.jpos.Terminal.LinePrinter;//a misplaced class..

import jpos.*;
import jpos.events.*;
import jpos.services.*;

import java.awt.Point;
import java.awt.Dimension;


import java.io.*;
import java.util.Vector;

public class FormService extends Service implements FormService14, SignatureCaptureService14, InputServer,FormControlConstant, JposConst {
  static final ErrorLogStream dbg=new ErrorLogStream(FormService.class.getName());
  static final ErrorLogStream fff=new ErrorLogStream("FormFormatter");
  protected final static Dimension graphLimit= new Dimension(320,240);
  protected final static Dimension textLimit = new Dimension(40,30);
  public static final double PollRate=4.0; //four checks per second.

  private boolean formEnabled=false;

  //jpos defined configuration data
  private int FontCode;//write only

  //default form options:
  private String compression="512dpi";

  //jpos defined read back data
  //these are illegitimate re jpos spex but I can't afford to fix it now.
  //+++ need to store in eventqueue on creation and parse on preparefordataevent
  //they work because we don't allow polling the entouch until after dataevents
  //are re-enabled.
  private byte [] packedButtons;
  private ncraSignature Signature;

  private ByteArrayOutputStream IncomingPointPacket;//accumulate incoming point data

  private String sFormName; //4debug
  private FormCommand currentForm;
  /////////////////////////////////////////////////////////////////

  static final String VersionInfo="FormService, (C) PayMate.net 2000, $Revision: 1.33 $";

  public FormService(String s,ET1K hw){
    super(s,hw);
    identifiers(VersionInfo,0,"");
    currentForm = new FormCommand();
    IncomingPointPacket= new ByteArrayOutputStream(); //allocated once and reset before each fresh use
    buttoner=new PolledCommand( finish(new Command(Codes.GET_CONTROLBOX_DATA,"Polling Form"),new PostButtonData()),PollRate,this,dbg);
  }

  public synchronized void open(String s, EventCallbacks eventcallbacks) throws JposException {
    compression=(String)ServiceTracker.getService("SignatureCompression");
    FontCode = 0;
    super.open(s,eventcallbacks);
  }

  public synchronized void close() throws JposException {
    //    storedPcxes.clear();//so that close() then open() forces full reload
    super.close();
  }

  public void claim(int i) throws JposException {
    try {
      super.claim(i);
    }
    catch(Exception exception){
      Failure("in claim", exception);
    }
  }
  ////////////////////////////////////////////////////////////////////
  public void prepareForDataEvent(Object blob){//about to post DataEvent
    //we do nothing, data has already been **illegally** prepared.
  }

  /**
  * determine if button report shows that form data entry is ended
  */
  private boolean enTouchHasStopped() {
    if(packedButtons != null){
      for(int i = 0; i < packedButtons.length; i += 2){
        if(packedButtons[i] != 2 && packedButtons[i + 1] == 1){
          return true;
        }
      }
    }
    return false;
  }

  class PostButtonData implements Callback {
    public Command Post(Command cmd){
      try {
        dbg.Enter("PostButtonData");
        if(gotData(cmd)){
          dbg.VERBOSE("checking button data");
          packedButtons = cmd.payload();
          if(enTouchHasStopped()) {//if a button says that enTouch has terminated input
            PostData(packedButtons);
          } else {
            if(formEnabled){
              dbg.VERBOSE("Still buttoning");
              buttoner.Start();//polling continues
            }
          }
        } else {
          if(cmd.response()==Codes.CONTROL_NOT_DISPLAYED){
            dbg.VERBOSE("no buttons to poll");//trivial error. we poll even when there are no buttons so we don't have to ask if there are any.
          }
          else {
            dbg.VERBOSE("corrupt response");
          }
          //???check if we have stopped it? (race condition)
          buttoner.Start();//polling continues
        }
        return null;
      }
      finally {
        dbg.Exit();
      }
    }

  }//end postbuttondata class

  public void clearFormInput() throws JposException {
    try {
      dbg.Enter("clearFormInput");
      assertClaimed();
      Signature = null;
      packedButtons = null;
    }
    finally {
      dbg.Exit();
    }
  }

  public void clearScreen() throws JposException {
    Issue(Codes.CLEAR_SCREEN,"Clearing Screen");
    //but polling continues +_+
  }

  public void displayTextAt(int row, int col, String s) throws JposException {
    assertEnabled();
    Illegal(row >= textLimit.height || col >= textLimit.width ,"Text positioned off screen");
    Illegal(!Safe.NonTrivial(s),"null string");
    JustSend(ET1K.DisplayTextAt(row,col,FontCode,s),"formservice displaytext");
  }

  public void endForm() throws JposException {
    assertEnabled();
    TerminateForm(false);//need to get buttons
  }

  /**
   * @return possibly null or zero length array of button:pressed? pairs
   */
  public byte[] getButtonData() throws JposException {
    assertEnabled();
    return packedButtons;
  }

  public int getCols() throws JposException {
    assertClaimed();
    return textLimit.width;
  }

  public int getMaximumX() throws JposException {
    assertOpened();
    return graphLimit.width;
  }

  public int getMaximumY() throws JposException {
    assertOpened();
    return graphLimit.height;
  }

  public Point[] getPointArray() throws JposException {
    return Signature!=null? Signature.getPoints(): new Point[0];
  }

  public Point[] getPointArray(int i) throws JposException {
    assertClaimed();
    if(i == 0) {
      return Signature!=null? Signature.getPoints(): new Point[0];
    }
    return null;
  }

  public byte[] getRawData() throws JposException {
    assertClaimed();
    return getRawSigData();
  }

  public byte[] getRawScriptData()  throws JposException  {
    assertClaimed();
    return null;
  }

  public byte[] getRawSigData() throws JposException {
    assertClaimed();
    return Signature!=null?Signature.getRawData():new byte[0];
  }

  public int getRows() throws JposException  {
    assertClaimed();
    return textLimit.height;
  }

  public byte[] getSurveyData() throws JposException {
    NotImplemented ("Surveys");
    return null;//packedSurveyData;
  }

  public void setFont(int fnt, int stile) throws JposException {
    assertClaimed();
    FontCode= stile<<4 | fnt ;//presumes legal values
    return;
  }

  //made a member so that we can purge it from queue
  LrcBuffer showStoredCmd=null;

  private LrcBuffer showStoredCommand(int formNumber){
    if(showStoredCmd==null){
      showStoredCmd=Command.Buffer(Codes.DISPLAY_STORED_FORM,2);
      showStoredCmd.append(0);
      showStoredCmd.append(formNumber);
      showStoredCmd.end();
    }
    showStoredCmd.replace(5,(byte)formNumber);
    return showStoredCmd;
  }

  static final Tracer arf=new Tracer("ARF");

  PolledCommand buttoner;

  public void startForm(String s, boolean alreadyStored) throws JposException {
    arf.mark("getform");
    currentForm=FormCommand.getJposForm(s);
    arf.mark("formnum");
    int formNumber = currentForm.FormNumber();
    arf.mark("grafindex");
    int grafNumber = currentForm.grafIndex();
    arf.mark("whic type");
    BlockCommand cmd;
    if(alreadyStored) {//if supposedly not stored then
      arf.mark("already");
      cmd=currentForm.asStored();//commands to show a store form
      hardware.squelch(cmd);//remove anything similar that is pending.
    } else {//trust caller, will fubar if they are wrong
      arf.mark("fullcommand");
      cmd=currentForm.fullCommand(false /*for storing*/);//show a NON-stored form
    }
    arf.mark("testhassig");
    if(currentForm.HasSignature()){//compression is not in FormCommand.pmForm scope so we do this in service
      dbg.VERBOSE("Setting signature DPI");
      Command legacy=new Command(Codes.CONFIGURE, Codes.SET_SIG_TYPE,parseCompression(compression),"setting sig dpi");
      arf.mark("addsigcomp");
      cmd.addCommand(legacy.outgoing());
    }
    arf.mark("sendpreform");
    sendPreformed(cmd,new FormLoader("Sending Form"));
    formEnabled=true;
    arf.mark("setpoller");
    QueueCommand(buttoner.toPoll);
  }

  private void sendPreformed(Command cmd, Callback cb){
    QueueCommand(cmd,cb);
  }

  class FormLoader extends WantZero {
    public FormLoader(String s){
      super(s);
    }
    public Command Post(Command cmd){
      int response=cmd.response();
      switch(response){
        //commm errors now handled in thing that calls this.
        case Codes.SUCCESS: {
          return super.Post(cmd);
        }
        case Codes.InvalidScreenNumber:{ //+++ repost as failure!
          return null; //else bad form creates infinite loop in loader.
        }
        default:
        return cmd.restart();
      }
    }
  }

  public void storeForm(String s) throws JposException {
    currentForm=FormCommand.getJposForm(s);// a creator
    sendPreformed(currentForm.fullCommand(true),new WantZero("Storing Form"));
  }

  private void AbortForm() throws JposException {
    Issue(Codes.ABORT,"Abort Form");
  }
  /////////////////////////////


  private byte [] extractedSignature(byte [] joined, int cursor){
    //int subpacket length from two bytes:
    int sigsize = (joined[cursor] & 0xff) + ((joined[cursor + 1] & 0xff) <<8);
    byte extract[] = new byte[sigsize-2];
    System.arraycopy(joined,cursor+2,extract,0,sigsize-2);
    return extract;
  }

  private void addPointFragment(byte [] buffer){
    int size = buffer.length;
//    dbg.ERROR("IPP:"+Safe.hexImage(buffer));
    IncomingPointPacket.write(buffer,0,size);
  }

  private void sigless(){
    Signature= null;
    PostData(Signature);
  }

  public Command endrun(Command cmd){
    dbg.Enter("sig packet handler");
    try {
      switch(cmd.response()){
        case Codes.NO_DATA_READY: {
          sigless();
        } break;
        case Codes.CONTROL_NOT_DISPLAYED:{
          //supposedly the box itself is not there
          sigless();
        } break;
        case Codes.MORE_DATA_READY:{//leading pieces
          dbg.VERBOSE("getting sig data");
          dbg.VERBOSE(cmd.incoming.toSpam(20));
          addPointFragment(cmd.payload(1));//skip sequence number
          //+_+ we theoretically should check the sequence number and reissue command if not correct.
          return finish(new Command(Codes.SEND_NEXT_DATA_BLOCK,"OnSigData"),new OnSigData());//more please
        } //break;
        case 0:{ //final piece
          dbg.WARNING("got sig data");
          dbg.VERBOSE(cmd.incoming.toSpam(20));
          addPointFragment(cmd.payload(1));
          ncraSignature newSignature= ncraSignature.fromRawData(JoinedPointPackets());
          Signature=newSignature;
          PostData(Signature);
        } break;
        default: {
          PostFailure("In OnSigData: status="+cmd.response());
        }break;
      }
      return null;
    } catch (Exception ex) {
      PostFailure("In OnSigData:"+ex);
      dbg.Caught(ex);
      return null;
    }
    finally {
      dbg.Exit();
    }
  }

  class OnSigData implements Callback {
    public Command Post(Command cmd){
      return endrun(cmd);//can probalby remove this extra level of call now that we are using jdk 1.3
    }
  }

  private byte[] JoinedPointPackets() throws JposException {
    return IncomingPointPacket.toByteArray();
  }

  public byte parseCompression(String s){
    try {
      if(Safe.NonTrivial(s)){
        if(s.equals("64dpi")){
          return  1; //??? seems likethis should be 0 +_+
        }
        if(s.equals("128dpi")){
          return  1;
        }
        if(s.equals("256dpi")){
          return  2;
        }
        if(s.equals("512dpi")){
          return  3;
        }
      } else {
        s="undefined"; //for diags message
      }
      return  3;//512dpi for anything wrong.
    } finally {
      dbg.VERBOSE("compression selected:"+s);
    }
  }

  public boolean getCapDisplay() throws JposException {
    assertOpened();
    return true;
  }

  private void TerminateForm(boolean buttonsOk) {
    dbg.Enter("TerminateForm/ "+ (buttonsOk?"on button":"endForm()"));
    try {
      formEnabled=false;   //affects interpreation of various incoming data
      buttoner.Stop();
      IncomingPointPacket.reset();
      ErrorLogStream.Debug.ERROR("signature slot on form?"+currentForm.HasSignature());
      ErrorLogStream.Debug.ERROR("currentForm:"+currentForm.toSpam());

      if(currentForm.HasSignature()){
        dbg.VERBOSE("Ask4sig");
        Get(Codes.GET_COMPRESSED_SIG,"Getting Sig",new OnSigData());
      }
    }
    finally {
      dbg.Exit();
    }
  }

  ////////////////////////////////////////////////
  //sig capture
  public void beginCapture(String formkey) throws JposException {
    //we can keep a "registry" of forms that were stored to supply the boolean below.
    startForm(formkey,false);//or should we require the form be stored???
    //NO: the only reason to store is to get graphic background...
    //but this is a big hole in the jpos spec.
  }

  public void endCapture(){
    try {
      endForm();
    }
    catch (Exception ex) {
      //blow it off
    }
  }

  /////////////////////
  //these should be a new device type!
  //so that cm3000 can be one of that type as well.
  public void displayKeyboard() throws JposException {
    NotImplemented("Keyboard");
  }

  public void displayKeypad()throws JposException{
    NotImplemented("Keypad");
  }

  public String getKeyedData() throws JposException {
    NotImplemented("KeyPad");
    return null;
  }

  public void setKeyboardPrompt(String s) throws JposException {
    NotImplemented("Keyboard");
  }

  public void setKeypadPrompts(String s, String s1) throws JposException {
    NotImplemented("Keypad");
  }

}
//$Id: FormService.java,v 1.33 2001/11/15 03:15:44 andyh Exp $
