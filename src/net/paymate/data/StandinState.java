package net.paymate.data;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/StandinState.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.util.*;
import net.paymate.lang.Bool;

public class StandinState {
  int state=sistate.not;
  private final static String modeKey="SIstate";
  private final static int clientbit=0;
  private final static int serverbit=1;

  public StandinState setServer(boolean stood){
    state=Bool.stuffMask(state,serverbit,stood);
    return this;
  }
  public boolean byServer(){
    return Bool.bitpick(state,serverbit);
  }
  public StandinState setClient(boolean stood){
    state=Bool.stuffMask(state,clientbit,stood);
    return this;
  }
  public boolean byClient (){
    return Bool.bitpick(state,clientbit) ;
  }

  public boolean wasStoodin(){
    return state!=sistate.not;
  }
  public void save(EasyCursor ezc){
    ezc.saveEnum(modeKey,new sistate(state));
  }
  public void load(EasyCursor ezc){
    state=ezc.getEnumValue(modeKey,sistate.Prop);
    if(state<0){
      state=sistate.not;
    }
  }
  public StandinState setto(StandinState sis){
    state= sis!=null ? sis.state : sistate.not;
    return this;
  }
  public StandinState() {
    //set to not stoodin.
  }

}
//$Id: StandinState.java,v 1.2 2003/07/27 05:34:58 mattm Exp $