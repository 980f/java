/**
* Title:        Keyboard
* Description:  make a java keyboard out of a jpos keyboard
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Keyboard.java,v 1.14 2001/10/12 04:11:37 andyh Exp $
*/

package net.paymate.jpos.Terminal;

import  net.paymate.util.*;

import jpos.*;
import jpos.events.*;
import jpos.events.DataEvent;

public class Keyboard extends jpos.POSKeyboard implements DEService  { //arcane non-java keyboard interface
  static final ErrorLogStream dbg=new ErrorLogStream(Keyboard.class.getName());

  KeyReceiver keyListener;  //person we are creating keyevents for
  boolean fakeit;
//  protected Listener thePOSapp;

  String id;
  public String toString(){
    return id;
  }


  public void dataOccurred (DataEvent e){
    try {
      dbg.Enter("dataOccurred");
      //make a KeyEvent out of the assoicated key stroke and send it to our one listener
      //POSKeyEventType= KBD_KET_KEYDOWN or KBD_KET_KEYUP ;
      char keycode=(char) getPOSKeyData();
      keyListener.KeyStroked(keycode);
    }
//    catch (JposException jape) {
//      thePOSapp.Handle(jape);
//    }
    catch (Exception caught){
      dbg.Caught(caught);
    }
    finally {
      try {
        setDataEventEnabled(true); //enable more keys
      } catch (Exception trash) {
        dbg.Caught(trash);
      } finally {
        dbg.Exit();
      }
    }
  }

  public void errorOccurred(jpos.events.ErrorEvent jape){
    try {
      setDataEventEnabled(true); //fire off the next event
    } catch (Exception trash) {
      dbg.ERROR("ErrorEvent occurred: " + jape);
      dbg.Caught(trash);
    }
  }

  protected void Attach(String id) {
    fakeit=(Base.Attach(this,this.id=id,DeviceName.Keyboard)!=null);
    if (fakeit) {
      //
    }
  }

  public void Release() {
    Base.Release(this);
  }

  public void Flush() throws JposException {
    Base.Flush(this);
  }

  public void Acquire(KeyReceiver newListener) {
    keyListener= newListener;
    if(!fakeit){
      Base.Acquire(this);
    }
  }

//  public Keyboard (Listener thePOSapp) {
//    this.thePOSapp=thePOSapp;
//  }

}
//$Id: Keyboard.java,v 1.14 2001/10/12 04:11:37 andyh Exp $
