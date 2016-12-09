package net.paymate.connection;
/**
* Title:        ActionList<p>
* Description:  List of actions that have been processed<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: ActionList.java,v 1.6 2001/10/18 05:33:02 andyh Exp $
*/

import  java.util.Vector;
import net.paymate.util.*;

public class ActionList {
  protected final Vector actionList = new Vector();
  protected int maxActions = 1;

  ///////////////////////////////////
  // performance monitor
  public long successes = 0;
  public long others = 0;
  long avgTimeTotal = 0;  // in milliseconds

  public long total(){
    return successes + others;
  }

  public float avgMillis(){
    long total=total();
    return (float)(total>0? (float)avgTimeTotal/(float)total:0.0);
  }


  ////////////////////
  // list proper
  public ActionList() {
    //all in initializers
  }

  public ActionList(int maxActions) {
    this.maxActions = maxActions;
  }

  public void setMaxActions(int setto) {
    if(setto < 1) {
      setto = 1; // need at least one
    }
    maxActions = setto;
    validateActionListSize();
  }

  public int count() {
    return actionList.size();
  }

  public Action getAction(int index) {
    return (actionList.size() > index) ? (Action)actionList.elementAt(index) : null;
  }

  public int register(Action action) {
    actionList.insertElementAt(action, 0);
    validateActionListSize();
    return count();
  }

  // only call this one when you are already synchronized
  private void validateActionListSize() {
    if(actionList.size() > maxActions) {
      actionList.setSize(maxActions);
    }
  }

  public TextList ActionStatsReport(TextList responses){//4ipterm
    responses.add("  Successes: " + successes);
    responses.add("  Others:    " + others);
    responses.add("  ==========================");
    responses.add("  Total:     " + total());
    responses.add("  Avg Time:  " + Safe.millisToSecsPlus((long)avgMillis()));
    return responses;//pass thru
  }

  public TextList ActionHistoryReport(TextList responses){
    responses.add(Action.historyHeader());
    for(int i = count(); i-->0 ;) {
      Action action = getAction(i);
      if(action != null) {
        responses.add(action.historyRec());
      }
    }
    return responses;
  }

}
//$Id: ActionList.java,v 1.6 2001/10/18 05:33:02 andyh Exp $
