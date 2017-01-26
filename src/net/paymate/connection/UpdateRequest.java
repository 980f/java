package net.paymate.connection;

/**
 * Title:         $Source: /cvs/src/net/paymate/connection/UpdateRequest.java,v $
 * Description:
 * Copyright:     Copyright (c) 2001
 * Company:       PayMate.net
 * @author        PayMate.net
 * @version       $Revision: 1.18 $
 */

import net.paymate.util.*;
import java.util.*;


interface URKey {
  String opt="options";
  String txnCount = "txnCount";
  String rcptCount = "rcptCount";
  String runtimeinfo="runtimeinfo";
  String sequence="seq";

}

public class UpdateRequest extends AdminRequest implements isEasy {

  public ActionType Type(){
    return new ActionType(ActionType.update);
  }

  public boolean fromHuman(){
    return false;
  }

  protected static int seqgenerator=0;//for recognizing quantity of lost requests at server

  public int seq;
  public int txnCount = -1;
  public int rcptCount = -1;

  //////////////////

  public ApplianceOptions opt=new ApplianceOptions();
  public AppStatus runtimeinfo=new AppStatus();

  public void save(EasyCursor ezc){
    super.save(ezc);
    ezc.setBlock(opt,URKey.opt);
    ezc.setInt(URKey.txnCount, txnCount);
    ezc.setInt(URKey.rcptCount, rcptCount);
    ezc.setInt(URKey.sequence, seq);
    ezc.setBlock(runtimeinfo,URKey.runtimeinfo);//updates itself before saving.
  }

  public void load(EasyCursor ezc){
    super.load(ezc);
    ezc.getBlock(opt,URKey.opt);
    txnCount  = ezc.getInt(URKey.txnCount, txnCount);
    rcptCount = ezc.getInt(URKey.rcptCount, rcptCount);
    seq=ezc.getInt(URKey.sequence);
    ezc.getBlock(runtimeinfo,URKey.runtimeinfo);
    dbg.WARNING(String.valueOf(runtimeinfo));
  }

  protected UpdateRequest() {
    //trivial for use by iseasy, do not generate a sequence number!
  }

  public static UpdateRequest Create(){
    return new UpdateRequest();
  }

  public static UpdateRequest Generate(int txnCount, int rcptCount) {
    UpdateRequest newone=new UpdateRequest();
    newone.txnCount = txnCount;
    newone.rcptCount = rcptCount;
    newone.seq=seqgenerator++;
    return newone;
  }

  public static UpdateRequest NullRequest(){
    return new UpdateRequest();
  }

}
//$Id: UpdateRequest.java,v 1.18 2004/02/11 00:23:15 andyh Exp $