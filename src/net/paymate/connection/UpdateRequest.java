package net.paymate.connection;

/**
 * Title:         $Source: /cvs/src/net/paymate/connection/UpdateRequest.java,v $
 * Description:
 * Copyright:     Copyright (c) 2001
 * Company:       PayMate.net
 * @author        PayMate.net
 * @version       $Revision: 1.12 $
 */

import net.paymate.util.*;
import java.util.*;

public class UpdateRequest extends AdminRequest implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.update);
  }

  public boolean fromHuman(){
    return false;
  }

  public int txnCount = -1;
  public int rcptCount = -1;

//////////////////

  public ApplianceOptions opt=new ApplianceOptions();
  public AppStatus runtimeinfo=new AppStatus();
  final static String optKey="options";
  final static String txnCountKey = "txnCount";
  final static String rcptCountKey = "rcptCount";
  final static String runtimeinfoKey="runtimeinfo";

  public void save(EasyCursor ezc){
    super.save(ezc);
    opt.saveas(optKey,ezc);
    ezc.setInt(txnCountKey, txnCount);
    ezc.setInt(rcptCountKey, rcptCount);
    //
    runtimeinfo.saveas(runtimeinfoKey,ezc);//updates itself before saving.
  }

  public void load(EasyCursor ezc){
    super.load(ezc);
    opt.loadfrom(optKey,ezc);
    txnCount  = ezc.getInt(txnCountKey, txnCount);
    rcptCount = ezc.getInt(rcptCountKey, rcptCount);
    runtimeinfo.loadfrom(runtimeinfoKey,ezc);
    dbg.WARNING(runtimeinfo.toString());
  }

  public UpdateRequest() {
    //trivial for request
  }

  public UpdateRequest(int txnCount, int rcptCount) {
    this();
    this.txnCount = txnCount;
    this.rcptCount = rcptCount;
  }

  public static UpdateRequest NullRequest(){
    return new UpdateRequest();
  }

}
//$Id: UpdateRequest.java,v 1.12 2001/10/24 04:14:18 mattm Exp $