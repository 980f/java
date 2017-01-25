package net.paymate.terminalClient;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: IdMangler.java,v 1.3 2001/03/28 05:57:24 andyh Exp $
 */

/**
* a seriously mangled clas for mangling check ID onfo.
* it is full of provisional ideas, shorted out to take
* texas driver's licenses.
*/
import net.paymate.util.ErrorLogStream;
import net.paymate.util.Safe;

import net.paymate.data.*;
import net.paymate.ISO8583.data.*;
import net.paymate.jpos.data.*;


public class IdMangler {
  ErrorLogStream dbg=new ErrorLogStream(IdMangler.class.getName());
  protected CheckIdInfo info=new CheckIdInfo();
  MajorTaxArea localState=new MajorTaxArea();

  protected boolean forced;

  public IdMangler Force(boolean ok){
    forced=ok;
    return this;
  }

  public CheckIdInfo Info(){
    return info.enForced(forced);
  }

  public IdMangler Clear(){
//    dbg.Message("Before:"+Spam());
    if(info==null){
      info=new CheckIdInfo();
    }
    info.Clear();
    if(localState!=null){
      localState.Clear();
    } else {
      localState = new MajorTaxArea();
    }
    forced=false;
//    dbg.Message("Cleared:"+Spam());
    return this;
  }

  public boolean isOk(){//may add rules beyond idinfo's rules
//    dbg.Message(forced?"forced":"at isOk?");
    return CheckIdInfo.useAble(info)||forced;
  }

  /**call when a real card has been swiped
  * @param card swipe data, better be a dl
  * @return this
  */
  public IdMangler onSwipe(MSRData aCard){
    dbg.Enter("onSwipe");
    info.license= new DriversLicense(aCard.track(1).Data());
    dbg.VERBOSE("Parsed To:"+info.license.Image());
    if(!info.license.isLegal()){
      aCard.ParseFinancial();
      info.otherCard.Number=aCard.accountNumber.Image();
      info.otherCard.idType.setto(AltIDType.OT);
    }
    dbg.Exit();
    return this;
  }

  /** call if the id card is supposed to be a license from the given state
  * @param theState two char abbreviation, trivial for (default) local state
  *  @return this
  */
  public IdMangler cardIsDL(String theState){//override state just now.
    info.license= Safe.NonTrivial(theState)? new DriversLicense(theState,info.otherCard.Number):
    new DriversLicense(localState,info.otherCard.Number);
    info.otherCard.Clear();
    return this;
  }

  /**
  * sets the state to expect when interpreting idinfo
  * @param state two letter abbreviation for state
  * @return this
  */
  public IdMangler setLocale(String state){
    this.localState= new MajorTaxArea(state);
    return this;
  }

  public IdMangler setNumber(String number){
    dbg.VERBOSE("setting license from number ["+number+"] while local state [" + localState + "].");
    info.license= new DriversLicense(localState,number);
    dbg.VERBOSE("license has been set:"+Spam());
    return this;
  }

  public IdMangler(){
    localState=new MajorTaxArea();
    info=new CheckIdInfo();
  }

  public String Spam(){
    return "@"+localState.Abbreviation()+info.Spam();
  }

}

//$Id: IdMangler.java,v 1.3 2001/03/28 05:57:24 andyh Exp $