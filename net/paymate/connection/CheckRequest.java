/* $Source: /cvs/src/net/paymate/connection/CheckRequest.java,v $ */

package net.paymate.connection;
import  net.paymate.jpos.data.*;
import  net.paymate.ISO8583.data.*;
import  net.paymate.util.*;

public class CheckRequest extends FinancialRequest implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.check);
  }

  public MICRData check; //
  public CheckIdInfo checkId;//ssn, dl, other...

  public String ManagerOverrideData; //stuff to send back to ntn on manager approavl of denied check

  public CheckRequest(){
    super();
    checkId=new CheckIdInfo();
    check=new MICRData();
  }

  public CheckRequest(SaleInfo sale, MICRData  checkd, CheckIdInfo checkIdd){
    super(sale);
    checkId=new CheckIdInfo(checkIdd);
    check=  new MICRData(checkd);
  }

  public void save(EasyCursor ezp){
    super.save(ezp);
    check.save(ezp);
    checkId.save(ezp);
    if(Safe.NonTrivial(ManagerOverrideData)){
      ezp.setString("ManagerOverrideData",ManagerOverrideData);
    }
  }

  public void load(EasyCursor ezp){
    super.load(ezp);//was a save :|
    check.load(ezp);
    checkId.load(ezp);
    ManagerOverrideData=ezp.getString("ManagerOverrideData");
  }

  public boolean canStandin() {
    return true;
  }
}
//$Id: CheckRequest.java,v 1.14 2001/09/01 09:33:38 mattm Exp $
