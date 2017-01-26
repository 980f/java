package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/BatchFilter.java,v $
 * Description:  for batch reports
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.util.*;
import net.paymate.lang.Bool;
import java.util.*;

public class BatchFilter implements isEasy{

  public boolean approved;
  public boolean voided;
  public boolean declined;
//using capitol B boolean to enable loading via reflection.
  public Bool paytype[]=new Bool[PayType.Prop.numValues()];

  public void save(EasyCursor ezp){
    ezp.saveEnumeratedArray("paytype",paytype,PayType.Prop);
    ezp.setBoolean("approved",approved);
    ezp.setBoolean("voided",voided);
    ezp.setBoolean("declined",declined);
  }

  public void load(EasyCursor ezp){
    approved=ezp.getBoolean("approved");
    voided=ezp.getBoolean("voided");
    declined=ezp.getBoolean("declined");
    paytype= (Bool [])(ezp.loadEasyEnumeratedVector("paytype",Bool.class,PayType.Prop).toArray());
  }

  private BatchFilter() {
//
  }

  public static BatchFilter Approved(){//legacy, someday will be generated from auth config
    BatchFilter newone=new BatchFilter();
    newone.approved=true;
    newone.voided=false;
    newone.declined=false;

    newone.paytype[PayType.Credit]=Bool.TRUE;
    newone.paytype[PayType.Debit]= Bool.TRUE;
    newone.paytype[PayType.Check]= Bool.TRUE;
    newone.paytype[PayType.Cash]=  Bool.FALSE;
    newone.paytype[PayType.GiftCard]=   Bool.TRUE;

    return newone;
  }

  public static BatchFilter None(){
    BatchFilter newone=new BatchFilter();
    //all falses everywhere== don't return anything.
    return newone;
  }

}
//$Id: BatchFilter.java,v 1.5 2003/10/25 20:34:19 mattm Exp $