package net.paymate.connection;
/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ActionList.java,v $
 * Description:  List of actions that have been processed
 * Copyright:    2000-2004, PayMate.net
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ActionList.java,v 1.11 2004/03/10 00:36:34 andyh Exp $
 * @todo: put elapsed time onto actionReply and complete the gathering of stats.
 */

import java.util.Vector;
import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.lang.MathX;

public class ActionList {
  protected final Vector actionList = new Vector();//we will be randomly accessing this data set.
  protected int maxActions = 1; //how much to remember

  ///////////////////////////////////
  // performance monitor
  public int successes = 0;
  public int others = 0;
  int avgTimeTotal = 0;  // in milliseconds

  public int total(){
    return successes + others;
  }

  public double avgMillis(){
    return MathX.ratio(avgTimeTotal,total());
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
  /**
   * @param index if >=0 then count from most recent, if <0 then count from when last emptied.
   * @return desired action from list, null if index is invalid
   */
  public Action getAction(int index) {
    if(index<0){
      index = count()+index;
      // if there are 4, and you want the 0th, you pass ~0 which is -1 so 4+~0=4-1=3.
      // if there are 4, and you want the 2nd, pass ~2 which is -3, 4+~2 = 1
    }
    return (actionList.size() > index) ? (Action)actionList.elementAt(index) : null;
  }

  private void updateStats(ActionReply reply) {
    if(reply!=null){
      if(reply.Succeeded()){
        ++successes;
      } else {
        ++others;
      }
      avgTimeTotal+=UTC.Elapsed(reply.refTime(),DateX.UniversalTime());
    }
  }

  private void rollStats(Action action) {
    if(action!=null && action.reply!=null){
      if(action.reply.Succeeded()){
        --successes;
      } else {
        --others;
      }
      avgTimeTotal-=0;//need to store elpased time on each ActionReply at time that it is discovered.
    }
  }


  public void register(Action action) {
    actionList.insertElementAt(action, 0);
    validateActionListSize();
    //@todo: adjust statistics
  }
  /**
   * @param reply response to most recently added Action.
   * @return whether reply was attached to an action.
   */
  public boolean applyReply(ActionReply reply) {
    Action a = getAction(0);
    if(a != null) {
      a.reply = reply;
      //@todo: adjust statistics
      return true;
    } else {
      return false;
    }
  }

  // only call this one when you are already synchronized
  private void validateActionListSize() {
    if(actionList.size() > maxActions) {
      actionList.setSize(maxActions);
      //@todo: adjust statistics
    }
  }

  private static final String divider = StringX.fill("", '=', 26, true);

  public TextList ActionStatsReport(TextList responses){//4ipterm
    if(responses==null){
      responses=new TextList(5);
    }
    responses.add("  Successes: " + successes);
    responses.add("  Others:    " + others);
    responses.add("  "            + divider);
    responses.add("  Total:     " + total());
    responses.add("  Avg Time:  " + DateX.millisToSecsPlus((long)avgMillis()));
    return responses;//pass thru
  }

  public TextList ActionHistoryReport(TextList responses){
    if(responses==null){
      responses=new TextList(5);
    }
    responses.add(Action.historyHeader());
    for(int i = count(); i-->0 ;) {
      Action action = getAction(i);
      if(action != null) {
        responses.add(action.historyRec());
      }
    }
    return responses;
  }

  public void clear() {
    actionList.clear();
  }

}
//$Id: ActionList.java,v 1.11 2004/03/10 00:36:34 andyh Exp $
