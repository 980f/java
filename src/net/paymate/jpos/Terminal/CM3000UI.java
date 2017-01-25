/**
* Title:        CM3000UI
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CM3000UI.java,v 1.36 2001/10/12 04:11:37 andyh Exp $
*/
package net.paymate.jpos.Terminal;

import net.paymate.data.EnumValue;
import net.paymate.data.ContentType;
import net.paymate.data.ContentValid;
import net.paymate.data.Value;

import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.Safe;
import  net.paymate.util.timer.StopWatch;

import  net.paymate.awtx.*;//uses just about everything in here

public class CM3000UI {
  static final ErrorLogStream dbg=new ErrorLogStream(CM3000UI.class.getName());
  Keyboard myKeys;
  LineDisplay myDisplay;

  Question beingAsked;
  AnswerListener replyTo;

  class KeyFiddler {
    /*
    the caret '^' brackets sets of characters to rotate through.
    Note that lower case is not enterable nor shifted. It can appear through
    preloaded values only.
    */

    String keyloops="^1QZ.^2ABC^3DEF^4GHI^5JKL^6MNO^7PRS^8TUV^9WXY^*,'\"^0- ^";
    char bracket=keyloops.charAt(0); //this is true regardless of which char is designated as a bracket

    public char shift(char previous){
      int present=keyloops.indexOf(previous);
      if(present>=0){
        if(keyloops.charAt(++present)==bracket){//rewind
          present=keyloops.lastIndexOf(bracket,present-1)+1;
        }
        return previous=keyloops.charAt(present);
      } else {
        return 0;
      }
    }

  }//end class fiddler


  public class KeyBuffer implements KeyReceiver {

    StringBuffer textvalue=new StringBuffer("NOT INITED");
    boolean translating;
    KeyFiddler phonepad;
    boolean preloaded;

    boolean singlekey;
    int keyval; //so as not to couple to what is displayed
    boolean micrmode; //really need to make up the DataString hierarchy!

    int cursor;       //micr needs to remember this for backspacing.
    boolean dirty;

    //need to hunt down ec3k services for import
    final static int EC3K_KEY_ALPHA=26; //^z
    final static int EC3K_KEY_ENTER=13; // '\r'
    final static int EC3K_KEY_BACKSPACE=8;
    final static int EC3K_KEY_CLEAR=24; //^x


    protected String EnumImage(int val){
      return ((EnumValue)(beingAsked.inandout)).ImageFor(val);
    }

    public void KeyStroked(char key){
      StopWatch responseTime=new StopWatch(true);//starts on creation
      try {
        dbg.Enter("KeyStroked:"+(int)key);

        if(micrmode){
          String searchable=textvalue.toString();
          switch(key){
            case 3: {
              return; //saw these via debugger, not in spec???
            } //break;
            case EC3K_KEY_CLEAR: {
              if(dirty && !preloaded){
                /*re-*/Start();
              } else {
                //kills time, risk data entry hanging in display.            myDisplay.Display("-"+beingAsked.prompt);
                replyTo.onReply(beingAsked,AnswerListener.CANCELLED);
              }
            } return;
            case '*': {
              textvalue.deleteCharAt(cursor=0);
            } break;
            case '#':{
              textvalue.insert(cursor=0,'?');
            } break;
            default:{ //normal keys
              cursor=searchable.indexOf('?');
              if(cursor>=0 && Character.isDigit(key)){//there is a qmark
                textvalue.setCharAt(cursor,key); //write over leftmost one
                dirty=true;
              }
            } break;
            case EC3K_KEY_ENTER: {
              boolean noChange=beingAsked.inandout.Image().equals(textvalue);
              beingAsked.inandout.setto(textvalue.toString());
              //              myDisplay.Display("="+beingAsked.inandout.Image());
              replyTo.onReply(beingAsked,noChange?AnswerListener.ACCEPTED: AnswerListener.SUBMITTED);
              return;
            } //break;
            case EC3K_KEY_BACKSPACE: {
              if(cursor>=0){//then it is last char edited
                textvalue.setCharAt(cursor,'?');//restore ambiguity marker
              } else { //preload buffer
                preLoad();
              }
            } break;
            case EC3K_KEY_ALPHA: {//
              if(!dirty){//bring value to display
                preloaded=false;
              }
            } break;
          }
        } else {
          cursor=textvalue.length();//simple cursor
          switch(key){
            case 3: {
              return; //saw these via debugger, not in spec???
            } //break;
            case EC3K_KEY_CLEAR: {
              if(cursor>0&&!preloaded){
                /*re-*/Start();
              } else {
                //don't waste time flashing them---                myDisplay.Display("-"+beingAsked.prompt);
                replyTo.onReply(beingAsked,AnswerListener.CANCELLED);
              }
            } return;
            default: {//normal keys
              if (preloaded) {
                //clear
                textvalue.setLength(0);
                preloaded=false;
              }

              if(singlekey){ //&&in select range...
                if(key=='*'){
                  dbg.ERROR("Can see stars in menues!");
                }
                keyval=Character.digit(key,10);
                textvalue=new StringBuffer("["+keyval+"] "+EnumImage(keyval));
              } else {
                textvalue.append(key);
              }
            }break;
            //else each key is its own ENTER as well:
            case EC3K_KEY_ENTER: {

              if(singlekey){
                beingAsked.inandout.setto(EnumImage(keyval));
                //allow display to persist
                replyTo.onReply(beingAsked,AnswerListener.SUBMITTED);
              } else {
                boolean noChange=beingAsked.inandout.Image().equals(textvalue);
                beingAsked.inandout.setto(textvalue.toString());
                //              if(preloaded){//perhaps !dirty???
                  //                 myDisplay.Display("="+beingAsked.inandout.Image());//4debug
                //              }
                replyTo.onReply(beingAsked,noChange?AnswerListener.ACCEPTED: AnswerListener.SUBMITTED);
              }
              return;
            } //break;
            case EC3K_KEY_BACKSPACE: {
              if(cursor>0){
                preloaded=false;
                textvalue.setLength(--cursor);
              } else { //preload buffer
                preLoad();
              }
            } break;
            case EC3K_KEY_ALPHA: {//fiddle previous char
              if (preloaded) {
                //prompt is showing , show default value instead
                preloaded=false; //then start operating normally
              } else {
                if(translating){
                  if(cursor>0){
                    --cursor;
                    key=phonepad.shift(textvalue.charAt(cursor));
                    //then shift previous letter
                    if(key>0){                  //if it actually is shiftable
                      textvalue.setCharAt(cursor,key);
                    } else {
                      //???ignored alpha, special functions on '#'???
                    }
                  } else {
                    //???alpha pressed on empty line== help?
                    myDisplay.Display("(Help Yourself:)");
                    return;
                  }
                }
              }
            } break;
          }
        }
        //redisplay         //+_+ class Value needs to incorporate this...
        if(beingAsked.inandout.charType().is(ContentType.money)){
          RealMoney rm=new RealMoney(Safe.parseLong(textvalue.toString()));
          dbg.VERBOSE("elapsed at display:"+responseTime.millis());
          myDisplay.Display(rm.Image());
        } else {
          dbg.VERBOSE("elapsed at display:"+responseTime.millis());
          myDisplay.Display(textvalue.toString());
        }
      }
      catch(Exception  alwaysbogus){
        dbg.WARNING("Ignoring bogus exception:"+alwaysbogus.getMessage());
      }
      finally {
        dbg.VERBOSE("total:"+responseTime.Stop());
        dbg.Exit();
      }
    }

    void preLoad(){
      ContentType ct=beingAsked.charType(); //FUE
      micrmode=ct.is(ContentType.micrdata);
      translating=!ContentValid.IsNumericType(ct);
      singlekey=  ct.is(ContentType.select);
      dbg.VERBOSE(ct.Image()+" is "+(translating?"Alpha":"Numeric")+(singlekey?",direct":",buffered"));
      if(ct.is(ContentType.money)){//will encapsulate this somehow later
        textvalue = new StringBuffer(beingAsked.inandout.toString());
      } else {
        textvalue = new StringBuffer(beingAsked.inandout.Image());
      }
      dbg.VERBOSE("Preloading "+textvalue.toString());
      preloaded=true;
      dirty=false; //needed by micrld have just
    }

    public void Start(){
      Exception e = myDisplay.Display(beingAsked.prompt);
      if(e!=null) {
        dbg.VERBOSE("Couldn't display prompt '" + beingAsked + "': ");
        dbg.Caught(e);
      }
      preLoad();
    }

    public KeyBuffer(){
      phonepad = new KeyFiddler();
      textvalue= new StringBuffer();
    }

  }
  KeyBuffer inputbuffer;

  public void ask(Question toAsk, AnswerListener replyTo){
//do not disturb question if already being asked:
dbg.VERBOSE("beingAsked" + ((beingAsked!=toAsk)?"!":"") + "=toAsk, this.replyTo" + ((this.replyTo!=replyTo)?"!":"") + "=replyTo");
    if(beingAsked!=toAsk || this.replyTo!=replyTo){//2nd clause is proforma.
      beingAsked=toAsk;
      this.replyTo=replyTo;
      inputbuffer.Start();
    }
  }

  String terminalid;
  public CM3000UI(String id) {
    terminalid  = id;
    inputbuffer = new KeyBuffer();
    myKeys      = new Keyboard();
    myDisplay   = new LineDisplay();
  }

/**
 * complete user entry IF
 * @param ofInterest is the question being asked, if null then any question...
 */
  public boolean autoEnterIf(Question ofInterest){
    if(ofInterest==null || beingAsked==ofInterest){//checking for same object!
      inputbuffer.KeyStroked((char)inputbuffer.EC3K_KEY_ENTER);
      return true;
    } else {
      //refresh the display
      refresh();
      return false;
    }
  }

  public void Start(){
    myDisplay.Attach(terminalid);
    myDisplay.Display("(user I/F start)");
    myKeys.Attach(terminalid);
    myKeys.Acquire(inputbuffer);
  }

  public void refresh(){
    myDisplay.refresh();
  }

  public String WhatsUp(){
    return beingAsked.prompt;
  }

  public void flash(String blink){
    myDisplay.Display(blink);
  }

}
//$Id: CM3000UI.java,v 1.36 2001/10/12 04:11:37 andyh Exp $
