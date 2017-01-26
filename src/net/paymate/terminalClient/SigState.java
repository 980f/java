package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/SigState.java,v $
 * Description:  signature acquisition state information.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */
import net.paymate.lang.TrueEnum;
class SigState {
  static final SignatureState moot    = new SignatureState(SignatureState.dontcare);
  static final SignatureState need    = new SignatureState(SignatureState.desired) ;
  static final SignatureState asked   = new SignatureState(SignatureState.onwire)   ;
  static final SignatureState gotten  = new SignatureState(SignatureState.acquired) ;
  static final SignatureState used    = new SignatureState(SignatureState.receipted);

  private SignatureState state=null;

  public int Value(){
    return state!=null ? state.Value():TrueEnum.Invalid();
  }

  public String toString(){
    return state!=null ? state.Image():"unknown";
  }

  public boolean is(SignatureState ss){
    return state!=null && state==ss; //yes, object compare.
  }

  public boolean not(SignatureState ss){
    return ! is(ss);
  }

  public boolean ready(){
    return is(gotten)||is(moot);
  }

  public boolean required(){
    return not(moot);
  }

  public SigState setto(SignatureState ss){
    state=ss;
    return this;
  }
/**
 * pick from two most popular states: @param desired ? need it : it is moot
 */
  public SigState setto(boolean desired){
    state=desired ? need : moot;
    return this;
  }

}
//$Id: SigState.java,v 1.4 2004/02/26 18:40:51 andyh Exp $