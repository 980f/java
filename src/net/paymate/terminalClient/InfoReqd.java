package net.paymate.terminalClient;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/terminalClient/InfoReqd.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */
import net.paymate.util.TextList;//4debug
public class InfoReqd {
  boolean login; //name and password
  boolean xfertype; //sale, return
  boolean paytype; //credit, debit
  boolean merchref; //arbitrary memo
  boolean origref; //stan to void or modify
  boolean authcode; //approval code for force
  boolean amount;
  boolean track2;
  boolean cardnum;
  boolean expiration;
  boolean usersok;
  boolean PIN;
  boolean sig;
  boolean AVSstreet;
  boolean AVSzip;
  boolean AnotherCopy;
  boolean id4check;

  void Nothing() {
    login = false; //name and password
    xfertype = false; //sale, return
    paytype = false; //credit, debit
    merchref = false; //arbitrary memo=false
    origref = false; //stan to void or modify
    authcode = false; //approval code for force
    amount = false;
    track2 = false;
    cardnum = false;
    expiration = false;
    usersok = false;
    PIN = false;
    sig = false;
    AVSstreet=false;
    AVSzip=false;
    AnotherCopy=false;
    id4check=false;
  }

  public InfoReqd() {
    Nothing();
  }


  public String toString(){
    TextList dump=new TextList(20);

    dump.add("Need:");
    if(login){dump.add("user login");}
    if(xfertype){dump.add("xfertype");}
    if(paytype){dump.add("paytype");}
    if(merchref){dump.add("merchref");}
    if(origref){dump.add("origref");}
    if(authcode){dump.add("authcode");}
    if(amount){dump.add("amount");}
    if(track2){dump.add("realSwipe");}
    if(cardnum){dump.add("acctNum");}
    if(expiration){dump.add("expiry");}
    if(usersok){dump.add("usersOK");}
    if(PIN){dump.add("PIN");}
    if(sig){dump.add("signature");}
    if(AVSstreet){dump.add("AVSstreet");}
    if(AVSzip){dump.add("AVSzip");}
    if(AnotherCopy){dump.add("anotherCopy");}
    if(id4check){dump.add("id4Check");}

    if(dump.size()==1){
      dump.add("nothing");
    }
    return dump.csv(false);
  }

}
//$Id: InfoReqd.java,v 1.3 2004/02/25 18:39:55 andyh Exp $