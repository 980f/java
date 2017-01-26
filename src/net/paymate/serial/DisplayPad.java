package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/DisplayPad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.31 $
 * @todo: restruture into package np.peripheral
 */

import net.paymate.util.*;
import net.paymate.lang.ContentType;
import net.paymate.lang.ContentValid;
import net.paymate.data.*;
import net.paymate.awtx.*;
import net.paymate.lang.StringX;
import net.paymate.util.Ascii;
import net.paymate.lang.TrueEnum;
import net.paymate.jpos.Terminal.KeyFiddler;
import net.paymate.ivicm.*;

public class DisplayPad implements DisplayInterface, QReceiver {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(DisplayPad.class);
  //exists to feed this:
  protected AnswerListener replyTo;
  //what we actually hook to
  protected DisplayHardware hardware;  //has a "display" function
  //here is where keyboard differences are accomodated
  public final static char BACKSPACE= (char)Ascii.BS;
  public final static char ENTER=     (char)Ascii.CR;
  public final static char CLEAR=     (char)Ascii.CAN;
  public final static char ALPHA=     (char)Ascii.SI;
  public final static char IGNORE=    (char)Ascii.NUL;
  public final static char FUNCTION=  (char)Ascii.ESC;  //escape,0x1B,chr(27)

  protected boolean doesStringInput(ContentType ct){
    return hardware!=null && hardware.doesStringInput(ct);
  }

  public void getString(Question q){
    if(hardware!=null){
      hardware.getString(q.prompt,q.inandout.Image(),q.charType());
    }
  }

  protected char cookedKey(int rawkey){//allow extensions to define keymap via code.
    return (char )rawkey;
  }

  String showing; //what we thinkj is on display
  boolean keying; //true when we are handling each key
  protected Question beingAsked;  //content for current display
  boolean flashing=false;//the display is temporarily being overridden


  private void Display(String showem){
    if(hardware!=null){
      hardware.Display(showing=showem);
    }
  }

  private void Echo(String echo){
    if(hardware!=null){
      if(hardware.hasTwoLines()){
        hardware.Echo(echo);
      } else {
        hardware.Display(echo);
      }
    }
  }


  /**
   * value class was giving out cents for money type, we want dollar sign etc.
   * @todo: the whole Value class hierarchy proves itself to be crap here (this note is written by the author of those classes)
   */
  private String echoImage(){
    if(beingAsked.inandout.charType().is(ContentType.money)){//this class breaks the class pattern
      RealMoney rm=new RealMoney(StringX.parseLong(String.valueOf(textvalue)));
      return rm.Image();
    }
    else if(beingAsked.inandout.charType().is(ContentType.password)){
      return  StringX.fill("",'*',textvalue.length(),true);
    }
    else {
      return String.valueOf(textvalue);
    }
  }

  /**
   * in case owner of this object bypassed it to show some urgent message
   * @return whether display was modified by this call.
   */
  protected boolean refresh(){
    if(flashing){
      flashing = false;
      if (hardware != null) {
        if (keying) {
          hardware.Display(showing);
          if (hardware.hasTwoLines()) {
            hardware.Echo(echoImage());
          }
          return true;
        }
        else { //this will probably jerk the user around.
          getString(beingAsked); //@expiration date lockout@
          return true;
        }
      }
    }
    return false;
  }
  /**
   * interrupt the question being asked for a special announcement
   * at next keystroke we refresh with local data.
   * @todo add timer to reset without keystroke, after some unspecified delay.
   */
  public void flash(String blink){
    flashing=true;
    if(hardware!=null){
      hardware.Display(blink);
    }
  }

/**
 * diagnostic on this class.
 */
  public String WhatsUp(){
    return beingAsked.prompt;
  }

  StringBuffer textvalue=new StringBuffer("NOT INITED");
  boolean translating;
  KeyFiddler phonepad;
  boolean preloaded;
  boolean singlekey;
  int keyval; //so as not to couple to what is displayed
  boolean micrmode; //really need to make up the DataString hierarchy!
  int cursor;       //micr needs to remember this for backspacing.
  boolean dirty;

  private void sendAnswer(String textvalue){
    boolean noChange=beingAsked.inandout.Image().equals(textvalue);
    beingAsked.inandout.setto(String.valueOf(textvalue));
    replyTo.onReply(beingAsked,noChange?AnswerListener.ACCEPTED: AnswerListener.SUBMITTED);
  }

  public boolean Post(Object arf){//receive keystrokes
    if (arf instanceof Integer) {//then it is a keystroke
      KeyStroked(((Integer)arf).intValue());
      return true;
    }
    if (arf instanceof String){
      sendAnswer(StringX.OnTrivial((String) arf,beingAsked.inandout.Image()));
      return true;
    }
    //anyting else is CANCEL
    replyTo.onReply(beingAsked,AnswerListener.CANCELLED);
    return false;
  }

  public void KeyStroked(int keystroke){
    try {
      dbg.Enter("KeyStroked:"+Ascii.image(keystroke));
      if(flashing){
        refresh(); //first keystroke after a flash
        return;     //  and ignore key
      }
      char key= cookedKey(keystroke);
      dbg.VERBOSE("cooked:"+Ascii.image(key));
      if(key==IGNORE){
        return;
      }
      cursor=textvalue.length();//simple cursor
      switch(key){
        case FUNCTION:{
            replyTo.onReply(beingAsked,AnswerListener.FUNCTIONED);
        } return;
        case  CLEAR: {
          if(cursor>0&&!preloaded){
            /*re-*/StartQuestion();
          } else {
            replyTo.onReply(beingAsked,AnswerListener.CANCELLED);
          }
        } return;
        default: {//normal keys
          if (preloaded) { //clear, then use incoming as a regular key
            textvalue.setLength(0);
            preloaded=false;
          }
          if(singlekey){ //&&in select range...
            keyval=Character.digit(key,16);//changed from 10 to 16 for encrypt100.
            textvalue=new StringBuffer(beingAsked.EnumImage(keyval));
          } else {
            textvalue.append(key);
          }
        } break;
        //else each key is its own ENTER as well:
        case  ENTER: {
          if(singlekey){
            if(beingAsked.isMenu()){
              TrueEnum buried= ((EnumValue)beingAsked.inandout).Content();
              buried.setto(keyval);
            } else {
              beingAsked.inandout.setto(String.valueOf(key));
            }
            replyTo.onReply(beingAsked,AnswerListener.SUBMITTED);
          } else {
            boolean noChange=beingAsked.inandout.Image().equals(textvalue);
            beingAsked.setAnswer(textvalue);
            replyTo.onReply(beingAsked,noChange?AnswerListener.ACCEPTED: AnswerListener.SUBMITTED);
          }
          return; //by returning here we keep from redrawing the display twice when the onReply function
          //asks another question, which it almost always does.
        } //break;
        case  BACKSPACE: {
          if(singlekey){
            replyTo.onReply(beingAsked,AnswerListener.CANCELLED);//CLEAR,<= map to RESET when menuing.
          } else {
            if (cursor > 0) {
              preloaded = false;
              textvalue.setLength(--cursor);
            }
            else { //preload buffer
              preLoad();
            }
          }
        } break;
        case  ALPHA: {//fiddle previous char
          if(singlekey){
            return; //ignored
          } else {
            if (preloaded) { //prompt is showing , show default value instead
              preloaded = false; //then start operating normally
            }
            else {
              if (translating &&cursor > 0) { //then shift previous letter
                key = phonepad.shift(textvalue.charAt(--cursor));
                if (key > 0) { //if it actually is shiftable
                  textvalue.setCharAt(cursor, key);
                }
              }
            }
          }
        } break;
      }
      //a lot of code can execute between an onReply above and getting to this point
      Echo(echoImage());
    }
    catch(Exception alwaysbogus){
      dbg.WARNING("Ignoring bogus exception:"+alwaysbogus.getMessage());
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * preloading analyzes the question setting various control flags for
   * what would otherwise be frequently used expressions.
   */
  private void preLoad(){
    ContentType ct=beingAsked.charType(); //FUE
    micrmode=ct.is(ContentType.micrdata);
    translating=!ContentValid.IsNumericType(ct);
    singlekey=  ct.is(ContentType.select);
    dbg.VERBOSE(ct.Image()+" is "+(translating?"Alpha":"Numeric")+(singlekey?",direct":",buffered"));
    if(ct.is(ContentType.money)){//will encapsulate this somehow later
      textvalue = new StringBuffer(String.valueOf(beingAsked.inandout));
    } else {
      textvalue = new StringBuffer(beingAsked.inandout.Image());
    }
    dbg.VERBOSE("Preloading "+textvalue);
    preloaded=true;
    dirty=false; //needed by micrld have just
  }

  public void StartQuestion(){
    if(beingAsked!=null){
      keying= !doesStringInput(beingAsked.charType());
      preLoad();//even when getting a string as function keys still get through to 'keystroked'
      if(keying){
        Display(beingAsked.prompt);
//        if(hardware!=null && hardware.hasTwoLines()){
//          //the below often showed stuff we'd rather not explain
//          //Echo(echoImage());//show default value
//        }
      } else {
        getString(beingAsked);
      }
    }
  }

  public void ask(Question toAsk){
    //do not disturb question if already being asked:
    if(beingAsked!=toAsk){
      dbg.VERBOSE("beingAsked new question");
      beingAsked=toAsk;
      StartQuestion();
    } else {
      dbg.VERBOSE("reasking current question");
      //set flashing=true here if this must restart question.
      refresh();//question being reasked
    }
  }
  /**
  * complete user entry IF
  * @param quid is the question being asked,
  * @return true if an enter was generated
  * only works when individual keystrokes are being acquired.
  */
  public boolean autoEnterIf(int quid){
    if(beingAsked.guid==quid && keying && ! preloaded){//was autoentering 0 at sale amount/swipe before any key pressed
        KeyStroked(ENTER);
        return true;
    } else {
      return false;
    }
  }

  public DisplayInterface attachTo(AnswerListener replyTo){
    this.replyTo=replyTo;
    return this;
  }

  public DisplayPad attachTo(DisplayHardware hardware){
    this.hardware=hardware;
    return this;
  }

  protected DisplayPad() {
  //want to construct DP and attach it to device separately
    phonepad = new KeyFiddler();
    textvalue= new StringBuffer();
  }

  public static DisplayPad Null(){
    return new DisplayPad();
  }

}
//$Id: DisplayPad.java,v 1.31 2004/02/26 18:40:51 andyh Exp $
