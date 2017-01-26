/* $Id: FormService.java,v 1.55 2003/07/27 05:35:04 mattm Exp $ */
package net.paymate.ivicm.et1K;

//import net.paymate.ivicm.et1K.ICForm;
import net.paymate.util.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.Terminal.*;
import net.paymate.lang.StringX;
import net.paymate.awtx.*;

import java.io.*;
import java.util.*;

import net.paymate.terminalClient.IviForm.*;

public class FormService extends Service  {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(FormService.class);
  static final ErrorLogStream fff=ErrorLogStream.getExtension(FormService.class, "FormFormatter");
//  final static XDimension graphLimit= new XDimension(320,240);
  final static XDimension textLimit = new XDimension(40,30);
  public static final double PollRate=4.0; //four checks per second.

  private boolean formEnabled=false;

  //jpos defined configuration data
  private int FontCode;//write only !!!

  //default form options:
  private String compression="512dpi";

  private ByteArrayOutputStream IncomingPointPacket;//accumulate incoming point data

  private String sFormName; //4debug
  private FormCommand currentForm;
  /////////////////////////////////////////////////////////////////

  static final String VersionInfo="FormService, (C) PayMate.net 2000, $Revision: 1.55 $";

  public FormService(String s,ET1K hw){
    super(s,hw);
//    identifiers(VersionInfo,0,"");
    currentForm = new FormCommand();
    IncomingPointPacket= new ByteArrayOutputStream(); //allocated once and reset before each fresh use
    buttoner=new PolledCommand( finish(new Command(OpCode.GET_CONTROLBOX_DATA,"Polling Form"),new PostButtonData()),PollRate,this,dbg);
  }

  /**
  * determine if button report shows that form data entry is ended
  */
  private boolean enTouchHasStopped(byte [] packedButtons) {
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
      byte [] packedButtons;
      try {
        dbg.Enter("PostButtonData");//#gc
        if(gotData(cmd)){
          dbg.VERBOSE("checking button data");
          packedButtons = cmd.payload();
          if(enTouchHasStopped(packedButtons)) {//if a button says that enTouch has terminated input
            PostData(new FormButtonData(packedButtons));
          } else {
            if(formEnabled){
              dbg.VERBOSE("Still buttoning");
              buttoner.Start();//polling continues
            }
          }
        } else {
          if(cmd.response()==ResponseCode.CONTROL_NOT_DISPLAYED){
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
        dbg.Exit();//#gc
      }
    }

  }//end postbuttondata class

  public void clearFormInput()  {
    try {
      dbg.Enter("clearFormInput");//#gc
    }
    finally {
      dbg.Exit();//#gc
    }
  }

  public void clearScreen() {
    Issue(OpCode.CLEAR_SCREEN,"Clearing Screen");
    //but polling continues +_+
  }

  public void displayTextAt(int row, int col, String s){
    Illegal(row >= textLimit.height || col >= textLimit.width ,"Text positioned off screen");
    Illegal(!StringX.NonTrivial(s),"null string");
    JustSend(ET1K.DisplayTextAt(row,col,FontCode,s),"formservice displaytext");
  }

  public void endForm(){
    TerminateForm(false);//need to get buttons
  }


  public void setFont(int fnt, int stile) {
    FontCode= stile<<4 | fnt ;//presumes legal values
    return;
  }

  static final Tracer arf=new Tracer(FormService.class,"arf");

  PolledCommand buttoner;

  public void startForm(Form iviform, boolean alreadyStored) {
    arf.mark("getform");
    currentForm=FormCommand.fromForm(iviform);
    arf.mark("formnum");
    int formNumber = currentForm.FormNumber();
    arf.mark("grafindex");
    int grafNumber = currentForm.grafIndex();
    arf.mark("which type");
    BlockCommand cmd;
    if(alreadyStored) {
      arf.mark("already");
      cmd=currentForm.asStored();//commands to show a store form
      hardware.squelch(cmd);//remove anything similar that is pending.
    }
    else {//trust caller, will fubar if they are wrong
dbg.ERROR("FORM NOT STORED:"+currentForm.toSpam());
      arf.mark("fullcommand");
      cmd=currentForm.fullCommand(false /*for storing*/);//show a NON-stored form
    }
    arf.mark("testhassig");
    if(currentForm.HasSignature()){//compression is not in FormCommand.pmForm scope so we do this in service
      dbg.VERBOSE("Setting signature DPI");
      Command legacy=new Command(OpCode.CONFIGURE, OpCode.SET_SIG_TYPE,parseCompression(compression),"setting sig dpi");
      arf.mark("addsigcomp");
      cmd.addCommand(legacy.outgoing());
    }
    arf.mark("sendpreform");
    sendPreformed(cmd,new FormLoader("Sending Form",currentForm.pmForm()));
    formEnabled=true;
    if(currentForm.HasButtons()){//suppress cable detect polling. Only poll if it is needed.
      arf.mark("setpoller");
      QueueCommand(buttoner.toPoll);
    }
  }

  private void sendPreformed(Command cmd, Callback cb){
    QueueCommand(cmd,cb);
  }

/**
 *   Appendix A: Host Command and Return Status Cross Reference HEX Status Description
 *   signature capture peripheral
 */
  private String formErrorMessage(int responsecode){
  switch(responsecode){
  case 0x03: return "encryption key not loaded, or mixing MSR 4430 commands with UKPT commands";
  case 0x40: return "Invalid row or column for text or box";
  case 0x41: return "Box extends beyond LCD screen on the right";
  case 0x42: return "Box extends beyond LCD screen on the bottom";
  case 0x45: return "Text string truncated (too long)";
  case 0x4D: return "unpublished code 0x4D";//unknown error from s/n 0500209093
  case 0xC7: return "MSR Data unreadable";
  case 0xE0: return "MSR busy, already enabled with function code 62/80";
  case 0xE8: return "Invalid record type";
  case 0xE9: return "Invalid page";
  case 0xEA: return "Invalid Page Offset";
  case 0xEC: return "Invalid parameter in data field";
  case 0xED: return "Cancel button touched on numeric keypad";
  case 0xEE: return "Insufficient memory";
  case 0xF6: return "Invalid screen number";
  case 0xF7: return "Invalid sequence number";
  case 0xFA: return "Invalid mode for command";
  default: return super.ErrorMessage(responsecode);
  }
  }

  class FormLoader extends WantZero {
    Form beingLoaded;

    public FormLoader(String s,Form beingLoaded){
      super(s);
      this.beingLoaded=beingLoaded;
    }

    public Command Post(Command cmd){
      int response=cmd.response();
      switch(response){
        //trivial errors, ones that retry won't fix
        case 0x40: // Invalid row or column for text or box
        case 0x41: // Box extends beyond LCD screen on the right
        case 0x42: // Box extends beyond LCD screen on the bottom
        case 0x45:// Text string truncated (too long)
        case 0x4D://unknown error from s/n 0500209093
          dbg.ERROR(formErrorMessage(response));
//          PostFailure(new FormProblem(formErrorMessage(response),beingLoaded));
          //join
          //rewite response to be success then
          cmd.forceResponse(ResponseCode.SUCCESS);
          dbg.VERBOSE("ResponseCode After Force:"+cmd.response());
        case ResponseCode.SUCCESS: {
          return super.Post(cmd);
        }
        case ResponseCode.outofroominflash:{
          PostFailure(new FormProblem("oorinf"+formErrorMessage(response),beingLoaded));
//          return new Command(OpCode.AUX_FUNCTION,AuxCode.AUX_COMPRESSFLASH,"CompressFlash");
          return ET1K.CompressFlashCommand();
        } //break;
        //hopeless content errors, retry won't fix
        case ResponseCode.InvalidScreenNumber:{ //truncate command with an abort
          if(cmd instanceof BlockCommand){
            BlockCommand bc=(BlockCommand) cmd;
            return bc.truncate();
          } else {
            cmd.outgoing=FormCommand.AbortCommand;
            return cmd; //else bad form creates infinite loop in loader.
          }
        }
        //retry any errors that get to here.
        default:{
          return cmd.restart();
        } //break;
      }
    }
  }

  public void storeForm(Form form) {
    currentForm=FormCommand.fromForm(form);// a creator
    sendPreformed(currentForm.fullCommand(true),new FormLoader("Storing Form",currentForm.pmForm()));
  }

  private void AbortForm()  {
    Issue(OpCode.ABORT,"Abort Form");
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
//    dbg.ERROR("IPP:"+Formatter.hexImage(buffer));
    IncomingPointPacket.write(buffer,0,size);
  }

  private void sigless(){
    PostData(new SigData(new ncraSignature()));
  }

private static SignatureType NCRAType=new SignatureType(SignatureType.NCRA);

  public Command endrun(Command cmd){
    dbg.Enter("sig packet handler");//#gc
    try {
      switch(cmd.response()){
        case ResponseCode.NO_DATA_READY: {
          sigless();
        } break;
        case ResponseCode.CONTROL_NOT_DISPLAYED:{
          //supposedly the box itself is not there
          sigless();
        } break;
        case ResponseCode.MORE_DATA_READY:{//leading pieces
          dbg.VERBOSE("getting sig data");
          dbg.VERBOSE(cmd.incoming.toSpam(20));
          addPointFragment(cmd.payload(1));//skip sequence number
          //+_+ we theoretically should check the sequence number and reissue command if not correct.
          return finish(new Command(OpCode.SEND_NEXT_DATA_BLOCK,"OnSigData"),new OnSigData());//more please
        } //break;
        case 0:{ //final piece
          dbg.WARNING("got sig data");
          dbg.VERBOSE(cmd.incoming.toSpam(20));
          addPointFragment(cmd.payload(1));
          PostData(SigData.CreateFrom(JoinedPointPackets(),NCRAType));
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
      dbg.Exit();//#gc
    }
  }

  class OnSigData implements Callback {
    public Command Post(Command cmd){
      return endrun(cmd);//can probably remove this extra level of call now that we are using jdk 1.3
    }
  }

  private byte[] JoinedPointPackets()  {
    return IncomingPointPacket.toByteArray();
  }

  public byte parseCompression(String s){
    try {
      if(StringX.NonTrivial(s)){
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

  private void TerminateForm(boolean buttonsOk) {
    dbg.Enter("TerminateForm/ "+ (buttonsOk?"on button":"endForm()"));//#gc
    try {
      formEnabled=false;   //affects interpreation of various incoming data
      buttoner.Stop();
      IncomingPointPacket.reset();
      if(currentForm.HasSignature()){
        dbg.VERBOSE("Ask4sig");
        Get(OpCode.GET_COMPRESSED_SIG,"Getting Sig",new OnSigData());
      }
    }
    finally {
      dbg.Exit();//#gc
    }
  }

}
//$Id: FormService.java,v 1.55 2003/07/27 05:35:04 mattm Exp $
